package com.khqr.rin.backend.controller;

import com.khqr.rin.backend.dto.PaymentRequest;
import com.khqr.rin.backend.service.KhqrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final KhqrService khqrService;
//    @CrossOrigin(origins = "*", allowCredentials = "true")
    @PostMapping("/generate-khqr")
    public ResponseEntity<Map<String, Object>> generateKhqr(@RequestBody PaymentRequest request) {
        return ResponseEntity.ok(khqrService.generateKhqr(request));
    }
//    @CrossOrigin(origins = "*", allowCredentials = "true")
    @PostMapping("/check-payment")
    public ResponseEntity<Map<String, Object>> checkPayment(@RequestBody Map<String, String> payload) {
        String md5 = payload.get("md5");
        if (md5 == null || md5.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "MD5 is required"));
        }

        Map<String, Object> response = khqrService.checkPayment(md5);
        return ResponseEntity.ok(response);
    }
}
