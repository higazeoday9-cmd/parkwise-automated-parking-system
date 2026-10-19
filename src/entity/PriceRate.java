package entity;

public class PriceRate {
    private int priceListID;
    private int lotNumber;
    private int effectiveYear;
    private int priceFirstHour;
    private int priceAdditionalHour;
    private int priceFullDay;

    public int getPriceListID() { return priceListID; }
    public void setPriceListID(int priceListID) { this.priceListID = priceListID; }

    public int getLotNumber() { return lotNumber; }
    public void setLotNumber(int lotNumber) { this.lotNumber = lotNumber; }

    public int getEffectiveYear() { return effectiveYear; }
    public void setEffectiveYear(int effectiveYear) { this.effectiveYear = effectiveYear; }

    public int getPriceFirstHour() { return priceFirstHour; }
    public void setPriceFirstHour(int priceFirstHour) { this.priceFirstHour = priceFirstHour; }

    public int getPriceAdditionalHour() { return priceAdditionalHour; }
    public void setPriceAdditionalHour(int priceAdditionalHour) { this.priceAdditionalHour = priceAdditionalHour; }

    public int getPriceFullDay() { return priceFullDay; }
    public void setPriceFullDay(int priceFullDay) { this.priceFullDay = priceFullDay; }
}
