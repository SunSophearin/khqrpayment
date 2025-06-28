package com.khqr.rin.backend.dto;

public record PaymentRequest(double amount,String transactionId,String email,String username) {
}
