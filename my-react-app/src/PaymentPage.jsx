import React, { useState } from "react";
import "./PaymentPage.css";

function PaymentPage() {
  const [amount, setAmount] = useState(500); // default payment amount
  const [paymentData, setPaymentData] = useState({
    orderId: "",
    paymentId: "",
    signature: "",
    amount: "",
    status: "SUCCESS",
  });

  // Handle manual input change
  const handleChange = (e) => {
    setPaymentData({ ...paymentData, [e.target.name]: e.target.value });
  };

  // Save payment manually
  const savePayment = async () => {
    try {
      const res = await fetch("http://localhost:8080/payment/save", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(paymentData),
      });

      if (res.ok) alert("Payment Data Saved Successfully!");
      else alert("Failed to save payment data!");
    } catch (err) {
      console.error(err);
      alert("Error saving payment data!");
    }
  };

  // Initiate Razorpay payment
  const initiatePayment = async () => {
    try {
      // 1. Create order in backend
      const orderResponse = await fetch("http://localhost:8080/payment/create-order", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ amount }),
      });

      const order = await orderResponse.json();

      if (!order.success) {
        alert("Failed to create order: " + order.message);
        return;
      }

      // 2. Razorpay options
      const options = {
        key: "rzp_test_RqI2lqWExmSJEE", // your Razorpay test key
        amount: order.order.amount * 100, // paise
        currency: "INR",
        order_id: order.order.orderId,
        name: "Demo Payment",
        description: "Test Razorpay Payment",
        handler: async function (response) {
          const data = {
            orderId: order.order.orderId,
            paymentId: response.razorpay_payment_id,
            signature: response.razorpay_signature,
            amount: order.order.amount,
            status: "SUCCESS",
          };

          try {
            const res = await fetch("http://localhost:8080/payment/verify", {
              method: "POST",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify(data),
            });

            if (res.ok) alert("Payment Verified Successfully!");
            else alert("Failed to verify payment!");
          } catch (err) {
            console.error(err);
            alert("Error verifying payment!");
          }
        },
        theme: { color: "#3399cc" },
      };

      const rzp = new window.Razorpay(options);
      rzp.open();
    } catch (err) {
      console.error(err);
      alert("Error initiating payment!");
    }
  };

  return (
    <div className="payment-container">
      <div className="payment-box">
        <h2 className="payment-title">Cloud Nexus Payment</h2>
        
        <input
          type="number"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          className="amount-input"
          min="1"
        />
        
        <button
          onClick={initiatePayment}
          className="pay-button"
        >
          Pay Now ₹{amount}
        </button>
        
        <div className="razorpay-badge">
          <p className="razorpay-text">Powered by Razorpay</p>
        </div>
      </div>
    </div>
  );
}

export default PaymentPage;
