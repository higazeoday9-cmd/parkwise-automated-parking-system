package control;

import java.sql.Timestamp;

public class ActiveSessionView {
    public int sessionId;
    public String vehicleNumber;
    public int lotNumber;
    public String lotName;
    public String city;
    public Timestamp startTime;

    public Integer finalSpaceId;
    public Integer currentConveyorId;

    public String locationText;
}

