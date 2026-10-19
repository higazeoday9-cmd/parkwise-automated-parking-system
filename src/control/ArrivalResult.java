package control;

public class ArrivalResult {
    private boolean success;
    private String message;

    private boolean needsRegistration;
    private String scannedVehicleNumber;
    private int lotNumber;

    private Integer createdSessionId;

    public static ArrivalResult ok(String msg, Integer sessionId) {
        ArrivalResult r = new ArrivalResult();
        r.success = true;
        r.message = msg;
        r.createdSessionId = sessionId;
        return r;
    }

    public static ArrivalResult fail(String msg) {
        ArrivalResult r = new ArrivalResult();
        r.success = false;
        r.message = msg;
        return r;
    }

    public static ArrivalResult registrationRequired(int lotNumber, String vehicleNumber, String msg) {
        ArrivalResult r = new ArrivalResult();
        r.success = false;
        r.needsRegistration = true;
        r.lotNumber = lotNumber;
        r.scannedVehicleNumber = vehicleNumber;
        r.message = msg;
        return r;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }

    public boolean isNeedsRegistration() { return needsRegistration; }
    public String getScannedVehicleNumber() { return scannedVehicleNumber; }
    public int getLotNumber() { return lotNumber; }

    public Integer getCreatedSessionId() { return createdSessionId; }
}

