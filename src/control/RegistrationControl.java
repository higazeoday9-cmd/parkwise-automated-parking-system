package control;

import entity.*;

public class RegistrationControl {

    private final ClientDAO clientDAO = new ClientDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private final ParkingLotDAO lotDAO = new ParkingLotDAO();
    private final ParkingSessionDAO sessionDAO = new ParkingSessionDAO();

    public ArrivalResult registerAndArrive(
            int lotNumber,
            String firstName,
            String lastName,
            String phone,
            VehicleExtractionResult extracted
    ) {
        try {
            if (phone == null || phone.trim().isEmpty()) return ArrivalResult.fail("Phone is required.");
            if (extracted == null) return ArrivalResult.fail("Vehicle extraction is missing.");
            if (extracted.getVehicleNumber() == null || extracted.getVehicleNumber().trim().isEmpty())
                return ArrivalResult.fail("Vehicle number is missing.");

            ParkingLot lot = lotDAO.getById(lotNumber);
            if (lot == null) return ArrivalResult.fail("Parking lot not found.");
            if (!lot.isAvilaible()) return ArrivalResult.fail("Parking lot is not available.");
            if (lot.getAvailableSpaces() <= 0) return ArrivalResult.fail("Parking lot is FULL (availableSpaces=0).");

            // Upsert client by phone (acts as unique key for UI)
            int clientId = clientDAO.upsertByPhone(firstName, lastName, phone.trim());

            // Upsert vehicle
            Vehicle v = new Vehicle();
            v.setVehicleNumber(extracted.getVehicleNumber().trim());
            v.setType(extracted.getType());
            v.setColor(extracted.getColor());
            v.setSize(extracted.getSize());
            v.setWeightKg(extracted.getWeightKg());
            v.setClientID(clientId);

            vehicleDAO.upsert(v);

            // Ensure association rule (vehicle -> exactly one client)
            vehicleDAO.setClientForVehicle(v.getVehicleNumber(), clientId);

            // open barrier (simulated) + create session
            int sessionId = sessionDAO.createSession(v.getVehicleNumber(), lotNumber);
            lotDAO.decrementAvailableSpaces(lotNumber);

            return ArrivalResult.ok("Registration saved. Barrier opened. Session created: " + sessionId, sessionId);

        } catch (Exception e) {
            return ArrivalResult.fail("Registration failed: " + e.getMessage());
        }
    }
}
