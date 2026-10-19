package entity;

import java.time.LocalDateTime;

public class PreferredLotSelection {
    private int selectionID;
    private int memberID;
    private int lotNumber;
    private LocalDateTime selectionDate;

    public int getSelectionID() { return selectionID; }
    public void setSelectionID(int selectionID) { this.selectionID = selectionID; }

    public int getMemberID() { return memberID; }
    public void setMemberID(int memberID) { this.memberID = memberID; }

    public int getLotNumber() { return lotNumber; }
    public void setLotNumber(int lotNumber) { this.lotNumber = lotNumber; }

    public LocalDateTime getSelectionDate() { return selectionDate; }
    public void setSelectionDate(LocalDateTime selectionDate) { this.selectionDate = selectionDate; }
}

