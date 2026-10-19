package entity;

public class ParkingSpace {
    private int spaceID;
    private int lotNumber;
    private int floorNumber;
    private int positionX;
    private int positionY;
    private String size; // small/medium/large

    public int getSpaceID() { return spaceID; }
    public void setSpaceID(int spaceID) { this.spaceID = spaceID; }

    public int getLotNumber() { return lotNumber; }
    public void setLotNumber(int lotNumber) { this.lotNumber = lotNumber; }

    public int getFloorNumber() { return floorNumber; }
    public void setFloorNumber(int floorNumber) { this.floorNumber = floorNumber; }

    public int getPositionX() { return positionX; }
    public void setPositionX(int positionX) { this.positionX = positionX; }

    public int getPositionY() { return positionY; }
    public void setPositionY(int positionY) { this.positionY = positionY; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
}
