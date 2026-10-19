package control;

import entity.*;

public class ParkingEntryControl {

    private final ParkingLotDAO parkingLotDAO = new ParkingLotDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private final ParkingSessionDAO sessionDAO = new ParkingSessionDAO();

    /** Simulates arrival event from Gate Sensor (GS). */
    public ArrivalResult arrive(int lotNumber, String scannedVehicleNumber) {
        try {
            scannedVehicleNumber = normalizeVehicleNumber(scannedVehicleNumber);
            if (scannedVehicleNumber.isEmpty()) {
                return ArrivalResult.fail("Vehicle number is required.");
            }

            ParkingLot lot = parkingLotDAO.getById(lotNumber);
            if (lot == null) return ArrivalResult.fail("Parking lot not found: " + lotNumber);

            if (!lot.isAvilaible()) return ArrivalResult.fail("Parking lot is not available.");
            if (lot.getAvailableSpaces() <= 0) return ArrivalResult.fail("Parking lot is FULL (availableSpaces=0).");

            Vehicle v = vehicleDAO.getByNumber(scannedVehicleNumber);
            if (v == null) {
                return ArrivalResult.registrationRequired(lotNumber, scannedVehicleNumber,
                        "Vehicle not found. Registration is required before opening the barrier.");
            }

            // open barrier (simulated) + create parking session
            int sessionId = sessionDAO.createSession(scannedVehicleNumber, lotNumber);
            parkingLotDAO.decrementAvailableSpaces(lotNumber);

            return ArrivalResult.ok("Barrier opened. Parking session created: " + sessionId, sessionId);

        } catch (Exception e) {
            return ArrivalResult.fail("Arrival failed: " + e.getMessage());
        }
    }

    private String normalizeVehicleNumber(String s) {
        if (s == null) return "";
        return s.trim();
    }
}

