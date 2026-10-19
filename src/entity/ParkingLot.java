package entity;

public class ParkingLot {
    private int lotNumber;
    private String lotName;
    private String adress; // keep spelling as in DB
    private String city;
    private int availableSpaces;
    private boolean isAvilaible;

    public int getLotNumber() { return lotNumber; }
    public void setLotNumber(int lotNumber) { this.lotNumber = lotNumber; }

    public String getLotName() { return lotName; }
    public void setLotName(String lotName) { this.lotName = lotName; }

    public String getAdress() { return adress; }
    public void setAdress(String adress) { this.adress = adress; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public int getAvailableSpaces() { return availableSpaces; }
    public void setAvailableSpaces(int availableSpaces) { this.availableSpaces = availableSpaces; }

    public boolean isAvilaible() { return isAvilaible; }
    public void setAvilaible(boolean avilaible) { isAvilaible = avilaible; }
}
