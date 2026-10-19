package entity;

import java.time.LocalDateTime;

public class ParkingSession {
    private int sessionID;
    private String vehicleNumber;
    private int lotNumber;

    private LocalDateTime startTime;
    private LocalDateTime endTime;                 // nullable

    private Integer calculatedAmount;              // nullable
    private Integer finalParkingSpaceID;           // nullable
    private boolean isFreeSession;
    private Integer currentConveyorID;             // nullable
    private Integer amountPaid;                    // nullable

    public int getSessionID() { return sessionID; }
    public void setSessionID(int sessionID) { this.sessionID = sessionID; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public int getLotNumber() { return lotNumber; }
    public void setLotNumber(int lotNumber) { this.lotNumber = lotNumber; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Integer getCalculatedAmount() { return calculatedAmount; }
    public void setCalculatedAmount(Integer calculatedAmount) { this.calculatedAmount = calculatedAmount; }

    public Integer getFinalParkingSpaceID() { return finalParkingSpaceID; }
    public void setFinalParkingSpaceID(Integer finalParkingSpaceID) { this.finalParkingSpaceID = finalParkingSpaceID; }

    public boolean isFreeSession() { return isFreeSession; }
    public void setFreeSession(boolean freeSession) { isFreeSession = freeSession; }

    public Integer getCurrentConveyorID() { return currentConveyorID; }
    public void setCurrentConveyorID(Integer currentConveyorID) { this.currentConveyorID = currentConveyorID; }

    public Integer getAmountPaid() { return amountPaid; }
    public void setAmountPaid(Integer amountPaid) { this.amountPaid = amountPaid; }
}
