package control;

import java.sql.Timestamp;

public class ParkingSessionViewRow {
    public int sessionId;

    public String vehicleNumber;

    public Integer clientId;
    public String clientName;
    public String phone;

    public int lotNumber;
    public String lotName;
    public String city;

    public Timestamp startTime;
    public Timestamp endTime;

    public Integer finalSpaceId;
    public Integer currentConveyorId;

    public Integer calculatedAmount;
    public Integer amountPaid;

    public boolean active;

    public String locationText() {
        if (finalSpaceId != null) return "Space #" + finalSpaceId;
        if (currentConveyorId != null) return "Conveyor #" + currentConveyorId;
        return "Gate/Unknown";
    }
}

