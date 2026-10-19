package control;

import java.util.UUID;

public class PaymentGatewayStub {

    public static class PaymentResponse {
        public boolean approved;
        public String transactionId;
        public String message;
    }

    public PaymentResponse charge(String phone, int amount) {
        PaymentResponse r = new PaymentResponse();
        // For project: always approve (you can randomize later if needed)
        r.approved = true;
        r.transactionId = "PG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        r.message = "APPROVED";
        return r;
    }
}
