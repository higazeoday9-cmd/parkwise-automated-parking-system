package control;

public class ConveyorAssignmentResult {
    private boolean success;
    private String message;

    private Integer sessionId;
    private Integer chosenConveyorId;
    private Integer chosenSpaceId;

    public static ConveyorAssignmentResult ok(String msg, Integer sessionId, Integer conveyorId, Integer spaceId) {
        ConveyorAssignmentResult r = new ConveyorAssignmentResult();
        r.success = true;
        r.message = msg;
        r.sessionId = sessionId;
        r.chosenConveyorId = conveyorId;
        r.chosenSpaceId = spaceId;
        return r;
    }

    public static ConveyorAssignmentResult fail(String msg) {
        ConveyorAssignmentResult r = new ConveyorAssignmentResult();
        r.success = false;
        r.message = msg;
        return r;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }

    public Integer getSessionId() { return sessionId; }
    public Integer getChosenConveyorId() { return chosenConveyorId; }
    public Integer getChosenSpaceId() { return chosenSpaceId; }
}
