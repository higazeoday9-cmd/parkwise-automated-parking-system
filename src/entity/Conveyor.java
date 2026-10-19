package entity;

public class Conveyor {

    private int conveyorID;
    private int lotNumber;
    private int maxWeightKg;

    private int floorNumber;
    private int positionX;
    private int positionY;

    private String state;
    private boolean isBusy;
    private int attemptCount;

    private boolean pendingWeight;
    private boolean weightConfirmed;
    private Integer pendingMaxWeightKg;   // nullable

    private String pausedFromState;
    private String lastFailureReason;

    // =====================
    // Constructors
    // =====================

    /** No-args constructor */
    public Conveyor() {
    }

    /** Full constructor (used by DAO when loading from DB) */
    public Conveyor(int conveyorID,
                    int lotNumber,
                    int maxWeightKg,
                    int floorNumber,
                    int positionX,
                    int positionY,
                    String state,
                    boolean isBusy,
                    int attemptCount,
                    boolean pendingWeight,
                    boolean weightConfirmed,
                    Integer pendingMaxWeightKg,
                    String pausedFromState,
                    String lastFailureReason) {

        this.conveyorID = conveyorID;
        this.lotNumber = lotNumber;
        this.maxWeightKg = maxWeightKg;
        this.floorNumber = floorNumber;
        this.positionX = positionX;
        this.positionY = positionY;
        this.state = state;
        this.isBusy = isBusy;
        this.attemptCount = attemptCount;
        this.pendingWeight = pendingWeight;
        this.weightConfirmed = weightConfirmed;
        this.pendingMaxWeightKg = pendingMaxWeightKg;
        this.pausedFromState = pausedFromState;
        this.lastFailureReason = lastFailureReason;
    }

    // =====================
    // Getters & Setters
    // =====================

    public int getConveyorID() {
        return conveyorID;
    }

    public void setConveyorID(int conveyorID) {
        this.conveyorID = conveyorID;
    }

    public int getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(int lotNumber) {
        this.lotNumber = lotNumber;
    }

    public int getMaxWeightKg() {
        return maxWeightKg;
    }

    public void setMaxWeightKg(int maxWeightKg) {
        this.maxWeightKg = maxWeightKg;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public int getPositionX() {
        return positionX;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isBusy() {
        return isBusy;
    }

    public void setBusy(boolean busy) {
        isBusy = busy;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public boolean isPendingWeight() {
        return pendingWeight;
    }

    public void setPendingWeight(boolean pendingWeight) {
        this.pendingWeight = pendingWeight;
    }

    public boolean isWeightConfirmed() {
        return weightConfirmed;
    }

    public void setWeightConfirmed(boolean weightConfirmed) {
        this.weightConfirmed = weightConfirmed;
    }

    public Integer getPendingMaxWeightKg() {
        return pendingMaxWeightKg;
    }

    public void setPendingMaxWeightKg(Integer pendingMaxWeightKg) {
        this.pendingMaxWeightKg = pendingMaxWeightKg;
    }

    public String getPausedFromState() {
        return pausedFromState;
    }

    public void setPausedFromState(String pausedFromState) {
        this.pausedFromState = pausedFromState;
    }

    public String getLastFailureReason() {
        return lastFailureReason;
    }

    public void setLastFailureReason(String lastFailureReason) {
        this.lastFailureReason = lastFailureReason;
    }

    // =====================
    // Utility
    // =====================

    @Override
    public String toString() {
        return "Conveyor{" +
                "conveyorID=" + conveyorID +
                ", lotNumber=" + lotNumber +
                ", maxWeightKg=" + maxWeightKg +
                ", floorNumber=" + floorNumber +
                ", positionX=" + positionX +
                ", positionY=" + positionY +
                ", state='" + state + '\'' +
                ", isBusy=" + isBusy +
                ", attemptCount=" + attemptCount +
                ", pendingWeight=" + pendingWeight +
                ", weightConfirmed=" + weightConfirmed +
                ", pendingMaxWeightKg=" + pendingMaxWeightKg +
                ", pausedFromState='" + pausedFromState + '\'' +
                ", lastFailureReason='" + lastFailureReason + '\'' +
                '}';
    }
}

