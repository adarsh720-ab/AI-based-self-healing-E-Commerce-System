package com.ecommerce.payment.service;

import com.ecommerce.payment.entity.dtos.request.InitiatePaymentRequest;
import com.ecommerce.payment.entity.dtos.response.PaymentResponse;

import java.util.UUID;

public interface PaymentService {
    PaymentResponse initiatePayment(InitiatePaymentRequest request);
    PaymentResponse getPaymentByOrderId(UUID orderId);
    void handleStripeWebhook(String payload, String signature);
}
