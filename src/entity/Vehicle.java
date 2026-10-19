package entity;

public class Vehicle {
    private String vehicleNumber; // PK
    private String type;
    private String color;         // white/black/silver/blue/other
    private String size;          // small/medium/large
    private int weightKg;
    private int clientID;         // FK to Client

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public int getWeightKg() { return weightKg; }
    public void setWeightKg(int weightKg) { this.weightKg = weightKg; }

    public int getClientID() { return clientID; }
    public void setClientID(int clientID) { this.clientID = clientID; }
}
