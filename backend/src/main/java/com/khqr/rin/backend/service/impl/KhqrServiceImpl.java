package com.khqr.rin.backend.service.impl;

import com.khqr.rin.backend.dto.PaymentRequest;
import com.khqr.rin.backend.service.KhqrService;
import jakarta.annotation.PreDestroy;
import kh.org.nbc.bakong_khqr.BakongKHQR;
import kh.org.nbc.bakong_khqr.model.IndividualInfo;
import kh.org.nbc.bakong_khqr.model.KHQRCurrency;
import kh.org.nbc.bakong_khqr.model.KHQRData;
import kh.org.nbc.bakong_khqr.model.KHQRResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.*;
@Slf4j
@Service
@RequiredArgsConstructor
public class KhqrServiceImpl implements KhqrService {

    private final SimpMessagingTemplate messagingTemplate;
    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, ScheduledFuture<?>> runningTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Value("${bakong.token}")
    private String bakongToken;

    @Value("${bakong.account.id}")
    private String bakongAccountId;

    @Value("${merchant.name}")
    private String merchantName;

    @Value("${merchant.city}")
    private String merchantCity;

    @Value("${merchant.mobile}")
    private String merchantMobile;

    private static final String PAYMENT_TOPIC = "/topic/khqr-payment";
    private static final String CHECK_PAYMENT_URL = "https://api-bakong.nbc.gov.kh/v1/check_transaction_by_md5";

    /**
     * បង្កើត QR Code KHQR និងកំណត់ការត្រួតពិនិត្យស្ថានភាពការបង់ប្រាក់ជាប្រចាំ។
     */
    public Map<String, Object> generateKhqr(PaymentRequest request) {
        IndividualInfo info = buildIndividualInfo(request);

        KHQRResponse<KHQRData> response = BakongKHQR.generateIndividual(info);
        if (response.getKHQRStatus().getCode() == 0) {
            String qr = response.getData().getQr();
            String md5 = response.getData().getMd5();

            schedulePaymentCheck(md5);
            return Map.of("qr", qr, "md5", md5);
        } else {
            log.error("Failed to generate KHQR: {}", response.getKHQRStatus());
            return Map.of("error", "Failed to generate QR");
        }
    }

    /**
     * បង្កើត IndividualInfo ពី PaymentRequest.
     */
    private IndividualInfo buildIndividualInfo(PaymentRequest request) {
        IndividualInfo info = new IndividualInfo();
        info.setBakongAccountId(bakongAccountId);
        info.setCurrency(KHQRCurrency.KHR);
        info.setAmount(request.amount());
        info.setMerchantName(merchantName);
        info.setMerchantCity(merchantCity);
        info.setBillNumber(request.transactionId());
        info.setMobileNumber(merchantMobile);
        info.setStoreLabel("Mrr.Black");
        return info;
    }

    /**
     * កំណត់ការត្រួតពិនិត្យស្ថានភាពការបង់ប្រាក់ជាប្រចាំ
     * ប្រើ ScheduledExecutorService និងផ្ញើសារតាម WebSocket នៅពេលបង់ប្រាក់ជោគជ័យ។
     */
    private void schedulePaymentCheck(String md5) {
        if (runningTasks.containsKey(md5)) {
            log.info("Payment check already running for md5: {}", md5);
            return;
        }

        Runnable task = () -> {
            try {
                log.info("Checking payment status for md5: {}", md5);

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + bakongToken);
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<Map<String, String>> entity = new HttpEntity<>(Map.of("md5", md5), headers);
                ResponseEntity<Map> response = restTemplate.postForEntity(CHECK_PAYMENT_URL, entity, Map.class);

                Map<String, Object> body = response.getBody();

                if (body != null && Integer.valueOf(0).equals(body.get("responseCode"))) {
                    log.info("Payment confirmed for md5: {}", md5);
//                    input savedata this
                    // ផ្ញើសារតាម WebSocket ទៅ client
                    messagingTemplate.convertAndSend(PAYMENT_TOPIC, Map.of(
                            "md5", md5,
                            "status", "PAID"
                    ));

                    // បញ្ឈប់ការត្រួតពិនិត្យ
                    ScheduledFuture<?> future = runningTasks.remove(md5);
                    if (future != null) {
                        future.cancel(true);
                        log.info("Stopped polling for md5: {}", md5);
                    }
                }
            } catch (Exception e) {
                log.error("Error during payment check for md5 {}: {}", md5, e.getMessage(), e);
            }
        };

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(task, 5, 5, TimeUnit.SECONDS);
        runningTasks.put(md5, future);

        // បញ្ឈប់ការត្រួតពិនិត្យបន្ទាប់ពី 2 នាទី
        scheduler.schedule(() -> {
            ScheduledFuture<?> running = runningTasks.remove(md5);
            if (running != null) {
                running.cancel(true);
                log.info("Timeout reached, stopped polling for md5: {}", md5);
            }
        }, 2, TimeUnit.MINUTES);
    }

    /**
     * ត្រួតពិនិត្យស្ថានភាពការបង់ប្រាក់តាម md5 តាម API Bakong (REST).
     */
    public Map<String, Object> checkPayment(String md5) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + bakongToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(Map.of("md5", md5), headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(CHECK_PAYMENT_URL, entity, Map.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to check payment status for md5 {}: {}", md5, e.getMessage(), e);
            return Map.of("error", "Failed to check payment");
        }
    }

    /**
     * បិទ scheduler យ៉ាងសុភាពនៅពេល service ត្រូវបានបិទ។
     */
    @PreDestroy
    public void shutdownScheduler() {
        log.info("Shutting down payment scheduler...");
        scheduler.shutdownNow();
    }
}
