package com.khqr.rin.backend.service;

import com.khqr.rin.backend.dto.PaymentRequest;

import java.util.Map;

public interface KhqrService {
    Map<String, Object> generateKhqr(PaymentRequest request);
    Map<String, Object> checkPayment(String md5);
}
