package control;

public class SmsService {

    /**
     * Stub SMS service (since real SMS is external).
     * This satisfies the story requirement: "An SMS message with parking details will be sent".
     */
    public void sendSms(String phoneNumber, String message) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) return;

        // In a real system: call external SMS provider API here.
        System.out.println("=== SMS SENT ===");
        System.out.println("To: " + phoneNumber);
        System.out.println("Msg: " + message);
        System.out.println("===============");
    }
}
