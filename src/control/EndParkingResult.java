package control;

public class EndParkingResult {
    public boolean success;
    public String message;

    public Integer sessionId;
    public Integer amountBeforeDiscount;
    public Integer amountFinal;
    public Integer totalHours;

    public String transactionId;
    public String receiptText;

    public static EndParkingResult ok(String msg) {
        EndParkingResult r = new EndParkingResult();
        r.success = true;
        r.message = msg;
        return r;
    }

    public static EndParkingResult fail(String msg) {
        EndParkingResult r = new EndParkingResult();
        r.success = false;
        r.message = msg;
        return r;
    }
}

