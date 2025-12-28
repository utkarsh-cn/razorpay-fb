package com.example.razorpaydemo.controller;

import com.example.razorpaydemo.model.PaymentEntity;
import com.example.razorpaydemo.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // Allow React frontend
public class PaymentController {

    private final PaymentService service;

    // ========================
    // Create Razorpay Order
    // ========================
    @PostMapping("/create-order")
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> data) {
        Long amount = Long.valueOf(data.get("amount").toString());
        return service.createOrder(amount); // already returns Map
    }

    // ========================
    // Verify Payment
    // ========================
    @PostMapping("/verify")
    public Map<String, Object> verifyPayment(@RequestBody Map<String, String> data) {
        String orderId = data.get("orderId");
        String paymentId = data.get("paymentId");
        String signature = data.get("signature");

        String message = service.verifyPayment(orderId, paymentId, signature);

        Map<String, Object> response = new HashMap<>();
        response.put("success", message.contains("Successfully"));
        response.put("message", message);

        return response;
    }

    // ========================
    // Manual Save Payment
    // ========================
    @PostMapping("/save")
    public Map<String, Object> savePayment(@RequestBody Map<String, Object> data) {
        PaymentEntity payment = PaymentEntity.builder()
                .orderId((String) data.get("orderId"))
                .paymentId((String) data.get("paymentId"))
                .signature((String) data.get("signature"))
                .amount(Long.valueOf(data.get("amount").toString()))
                .status((String) data.getOrDefault("status", "SUCCESS"))
                .build();

        service.saveManual(payment);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Payment saved successfully!");

        return response;
    }
}
