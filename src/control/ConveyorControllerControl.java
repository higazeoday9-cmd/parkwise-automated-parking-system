package control;

import entity.*;

import java.util.List;

public class ConveyorControllerControl {

    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private final ClientDAO clientDAO = new ClientDAO();
    private final ParkingSessionDAO sessionDAO = new ParkingSessionDAO();
    private final ConveyorDAO conveyorDAO = new ConveyorDAO();
    private final ParkingSpaceDAO spaceDAO = new ParkingSpaceDAO();

    private final OptimizationControl optimization = new OptimizationControl();
    private final SmsService smsService = new SmsService();

    /**
     * CC scans vehicleNumber, assigns a vacant conveyor, finds nearest compatible space,
     * updates session+conveyor, and sends SMS with parking details.
     */
    public ConveyorAssignmentResult parkVehicle(String vehicleNumber) {
        try {
            if (vehicleNumber == null || vehicleNumber.trim().isEmpty())
                return ConveyorAssignmentResult.fail("Vehicle number is required.");

            vehicleNumber = vehicleNumber.trim();

            Vehicle v = vehicleDAO.getByNumber(vehicleNumber);
            if (v == null) return ConveyorAssignmentResult.fail("Vehicle not found in DB: " + vehicleNumber);

            ParkingSession session = sessionDAO.getActiveByVehicle(vehicleNumber);
            if (session == null) return ConveyorAssignmentResult.fail("No ACTIVE parking session for this vehicle.");

            // Find a vacant conveyor in same lot: Operational_Idle, not busy, not paused
            Conveyor chosen = findVacantConveyor(session.getLotNumber(), v.getWeightKg());
            if (chosen == null) {
                return ConveyorAssignmentResult.fail("No available conveyor in lot " + session.getLotNumber()
                        + " that can carry weight " + v.getWeightKg() + "kg.");
            }

            // Find compatible vacant spaces in same lot by size
            List<ParkingSpace> candidates = spaceDAO.getVacantCompatibleSpaces(session.getLotNumber(), v.getSize());
            if (candidates.isEmpty()) {
                return ConveyorAssignmentResult.fail("No vacant compatible parking spaces for size '" + v.getSize()
                        + "' in lot " + session.getLotNumber());
            }

            ParkingSpace best = optimization.findNearest(chosen, candidates);
            if (best == null) return ConveyorAssignmentResult.fail("Optimization could not select a space.");

            // Simulate move:
            // 1) assign session to conveyor (vehicle is currently "on conveyor")
            sessionDAO.setCurrentConveyor(session.getSessionID(), chosen.getConveyorID());
            conveyorDAO.updateBusy(chosen.getConveyorID(), true);
            conveyorDAO.updateState(chosen.getConveyorID(), "Operational_Executing");

            // 2) "arrived" -> set final space, clear current conveyor, update conveyor position, set idle
            sessionDAO.setFinalParkingSpace(session.getSessionID(), best.getSpaceID());
            sessionDAO.setCurrentConveyor(session.getSessionID(), null);

            updateConveyorLocation(chosen.getConveyorID(), best.getFloorNumber(), best.getPositionX(), best.getPositionY());

            conveyorDAO.updateState(chosen.getConveyorID(), "Operational_Idle");
            conveyorDAO.updateBusy(chosen.getConveyorID(), false);

            String msg = "Parked successfully. Session=" + session.getSessionID()
                    + ", Conveyor=" + chosen.getConveyorID()
                    + ", Space=" + best.getSpaceID()
                    + " (floor " + best.getFloorNumber()
                    + ", x=" + best.getPositionX()
                    + ", y=" + best.getPositionY() + ")";

            // === SMS requirement (send parking details to client phone) ===
            try {
                Client cl = clientDAO.getById(v.getClientID());
                if (cl != null) {
                    String smsText = "ParkWise: Vehicle " + vehicleNumber
                            + " parked in lot " + session.getLotNumber()
                            + ", space " + best.getSpaceID()
                            + " (floor " + best.getFloorNumber()
                            + ", x=" + best.getPositionX()
                            + ", y=" + best.getPositionY() + ")."
                            + " Session #" + session.getSessionID() + ".";
                    smsService.sendSms(cl.getMobilePhone(), smsText);
                }
            } catch (Exception ignored) {
                // SMS failure should not fail parking.
            }

            return ConveyorAssignmentResult.ok(msg, session.getSessionID(), chosen.getConveyorID(), best.getSpaceID());

        } catch (Exception e) {
            return ConveyorAssignmentResult.fail("Parking failed: " + e.getMessage());
        }
    }

    private Conveyor findVacantConveyor(int lotNumber, int vehicleWeightKg) throws Exception {
        for (Conveyor c : conveyorDAO.getAll()) {
            if (c.getLotNumber() != lotNumber) continue;

            String state = c.getState() == null ? "" : c.getState().trim();
            if (!state.equals("Operational_Idle")) continue;
            if (c.isBusy()) continue;
            if (vehicleWeightKg > c.getMaxWeightKg()) continue;

            if (state.equals("Paused")) continue;

            return c;
        }
        return null;
    }

    private void updateConveyorLocation(int conveyorId, int floor, int x, int y) throws Exception {
        conveyorDAO.updateLocation(conveyorId, floor, x, y);
    }
}
