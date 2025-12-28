package com.example.razorpaydemo.service;

import com.example.razorpaydemo.model.PaymentEntity;
import com.example.razorpaydemo.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository repo;
    private RazorpayClient client;

    @Value("${razorpay.key_id}")
    private String keyId;

    @Value("${razorpay.key_secret}")
    private String keySecret;

    @PostConstruct
    public void init() throws RazorpayException {
        this.client = new RazorpayClient(keyId, keySecret);
    }

    // Create Razorpay order + save in DB + return Map (Spring automatically converts to JSON)
    public Map<String, Object> createOrder(Long amount) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Create order request as JSONObject directly
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount * 100); // amount in paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "txn_" + System.currentTimeMillis());

            // Create order via Razorpay client
            Order order = client.orders.create(orderRequest);
            String orderId = order.get("id").toString();

            // Save order in DB
            PaymentEntity payment = PaymentEntity.builder()
                    .orderId(orderId)
                    .amount(amount)
                    .status("CREATED")
                    .build();
            repo.save(payment);

            // Prepare response to frontend
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("orderId", orderId);
            orderData.put("amount", amount);
            orderData.put("currency", "INR");
            orderData.put("status", payment.getStatus());

            response.put("success", true);
            response.put("message", "Order Created Successfully");
            response.put("order", orderData);

        } catch (RazorpayException e) {
            response.put("success", false);
            response.put("message", "Razorpay Error: " + e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Server Error: " + e.getMessage());
        }

        return response;
    }

    // Verify payment + update DB
    public String verifyPayment(String orderId, String paymentId, String signature) {
        PaymentEntity payment = repo.findByOrderId(orderId);
        if (payment == null) return "Order not found";

        payment.setPaymentId(paymentId);
        payment.setSignature(signature);
        payment.setStatus("SUCCESS");
        repo.save(payment);

        return "Payment Verified & Saved Successfully!";
    }

    // Manual save of payment
    public void saveManual(PaymentEntity payment) {
        repo.save(payment);
    }
}
