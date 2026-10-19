package control;

import entity.Conveyor;
import entity.ConveyorDAO;

import java.util.List;
import java.util.concurrent.*;

public class ConveyorControl {

    private final ConveyorDAO dao = new ConveyorDAO();
    private final AlertService alertService = new AlertService();

    // Spec: 10 minutes. You can override for testing via JVM option:
    // -Dparkwise.integrity.timeoutMs=8000
    private static final long INTEGRITY_TIMEOUT_MS =
            Long.getLong("parkwise.integrity.timeoutMs", 10L * 60L * 1000L);

    // Optional: add tiny delay per step to "feel" sequential checks (default 0)
    // -Dparkwise.integrity.stepDelayMs=200
    private static final long STEP_DELAY_MS =
            Long.getLong("parkwise.integrity.stepDelayMs", 0L);

    /** Load a conveyor by id (for status display in the GUI). */
    public Conveyor getById(int conveyorId) throws Exception {
        return dao.getById(conveyorId);
    }

    /** Turn ON a single conveyor using full spec integrity-check with timeout + retry. */
    public String turnOn(int conveyorId) throws Exception {
        Conveyor c = dao.getById(conveyorId);
        if (c == null) return "Conveyor not found: " + conveyorId;

        // Must confirm pending weight change before turning on
        if (c.isPendingWeight() && !c.isWeightConfirmed()) {
            return "Blocked: pending weight change not confirmed for conveyor " + conveyorId;
        }

        // Only turn on from OffStable
        if (!"OffStable".equals(c.getState())) {
            return "Blocked: cannot turn on from state=" + c.getState() + " (expected OffStable)";
        }

        return runIntegrityAndTurnOn(conveyorId, false);
    }

    /** Turn OFF a single conveyor (only if not busy, and not executing). */
    public String turnOff(int conveyorId) throws Exception {
        Conveyor c = dao.getById(conveyorId);
        if (c == null) return "Conveyor not found: " + conveyorId;

        // Cannot turn off while executing/busy
        if (c.isBusy() || "Operational_Executing".equals(c.getState())) {
            return "Blocked: conveyor " + conveyorId + " is busy/executing (cannot turn off).";
        }

        dao.updateState(conveyorId, "OffStable");
        return "Turned OFF conveyor " + conveyorId + " -> OffStable";
    }

    /** Alias for GUI button naming. */
    public String resume(int conveyorId) throws Exception {
        return resumeOperation(conveyorId);
    }

    /** Admin presses "Turn on conveyors" (ALL) */
    public String turnOnConveyors() throws Exception {
        List<Integer> ids = dao.getAllConveyorIds();

        int turnedOn = 0;
        int paused = 0;
        int blocked = 0;

        StringBuilder sb = new StringBuilder();
        sb.append("Turn ON ALL conveyors started...\n");

        for (int id : ids) {
            Conveyor c = dao.getById(id);
            if (c == null) continue;

            if (c.isPendingWeight() && !c.isWeightConfirmed()) {
                blocked++;
                sb.append("• ID=").append(id).append(" BLOCKED (pending weight not confirmed)\n");
                continue;
            }

            if (!"OffStable".equals(c.getState())) {
                blocked++;
                sb.append("• ID=").append(id).append(" BLOCKED (state=").append(c.getState()).append(")\n");
                continue;
            }

            String res = runIntegrityAndTurnOn(id, false);
            Conveyor after = dao.getById(id);
            if (after != null && "Operational_Idle".equals(after.getState())) turnedOn++;
            else if (after != null && "Paused".equals(after.getState())) paused++;

            sb.append("• ID=").append(id).append(" -> ").append(after == null ? "?" : after.getState())
                    .append(" | ").append(res.replace("\n", " ")).append("\n");
        }

        sb.append("DONE. Turned on: ").append(turnedOn)
                .append(", paused: ").append(paused)
                .append(", blocked: ").append(blocked);

        return sb.toString();
    }

    /** Admin presses "Turn off conveyors" (ALL) */
    public String turnOffConveyors() throws Exception {
        List<Integer> ids = dao.getAllConveyorIds();
        int turnedOff = 0;
        int blockedBusy = 0;

        StringBuilder sb = new StringBuilder();
        sb.append("Turn OFF ALL conveyors started...\n");

        for (int id : ids) {
            Conveyor c = dao.getById(id);
            if (c == null) continue;

            if (c.isBusy() || "Operational_Executing".equals(c.getState())) {
                blockedBusy++;
                sb.append("• ID=").append(id).append(" BLOCKED (busy/executing)\n");
                continue;
            }

            dao.updateState(id, "OffStable");
            turnedOff++;
            sb.append("• ID=").append(id).append(" -> OffStable\n");
        }

        sb.append("DONE. Turned off: ").append(turnedOff)
                .append(", busy (not turned off): ").append(blockedBusy);

        return sb.toString();
    }

    /** Admin sets new max weight */
    public String changeMaxWeight(int conveyorId, int newWeightKg) throws Exception {
        Conveyor c = dao.getById(conveyorId);
        if (c == null) return "Conveyor not found: " + conveyorId;

        if (newWeightKg <= 0) return "Invalid weight: " + newWeightKg;

        // If conveyor is ON, require confirmation before turning on again
        if (!"OffStable".equals(c.getState())) {
            dao.setPendingMaxWeight(conveyorId, newWeightKg);
            dao.setPendingWeight(conveyorId, true);
            dao.setWeightConfirmed(conveyorId, false);
            return "Pending maxWeightKg=" + newWeightKg + " set for conveyor " + conveyorId + ". Please confirm.";
        }

        dao.updateMaxWeight(conveyorId, newWeightKg);
        return "Max weight updated to " + newWeightKg + " for conveyor " + conveyorId;
    }

    public String confirmWeightChange(int conveyorId) throws Exception {
        Conveyor c = dao.getById(conveyorId);
        if (c == null) return "Conveyor not found: " + conveyorId;

        Integer pending = c.getPendingMaxWeightKg();
        if (pending == null) return "No pendingMaxWeightKg to confirm for conveyor " + conveyorId;

        dao.updateMaxWeight(conveyorId, pending);
        dao.setPendingMaxWeight(conveyorId, null);
        dao.setPendingWeight(conveyorId, false);
        dao.setWeightConfirmed(conveyorId, true);
        dao.updateState(conveyorId, "OffStable");

        return "Confirmed maxWeightKg=" + pending + " for conveyor " + conveyorId;
    }

    /** Pause allowed while waiting/executing (Operational_*). */
    public String pause(int conveyorId) throws Exception {
        Conveyor c = dao.getById(conveyorId);
        if (c == null) return "Conveyor not found: " + conveyorId;

        if (!c.getState().startsWith("Operational")) {
            return "Cannot pause from state: " + c.getState();
        }

        dao.setPausedFromState(conveyorId, c.getState());
        dao.updateState(conveyorId, "Paused");
        return "Paused conveyor " + conveyorId + " (from " + c.getState() + ")";
    }

    /** Resume only if paused by operator (has pausedFromState). If paused due to failure -> must Restart. */
    public String resumeOperation(int conveyorId) throws Exception {
        Conveyor c = dao.getById(conveyorId);
        if (c == null) return "Conveyor not found: " + conveyorId;

        if (!"Paused".equals(c.getState())) {
            return "Cannot resume (not Paused). Current state: " + c.getState();
        }

        // If it was paused due to integrity failure => blocked, must restart after repair
        if ((c.getPausedFromState() == null || c.getPausedFromState().isBlank())
                && c.getLastFailureReason() != null && !c.getLastFailureReason().isBlank()) {
            return "Blocked: paused due to failure (" + c.getLastFailureReason() + "). Use Restart after repair.";
        }

        String backTo = c.getPausedFromState();
        if (backTo == null || backTo.isBlank()) backTo = "Operational_Idle";

        dao.updateState(conveyorId, backTo);
        dao.setPausedFromState(conveyorId, null);
        return "Resumed conveyor " + conveyorId + " -> " + backTo;
    }

    /** Command handling (kept as you had). */
    public String assignCommand(int conveyorId) throws Exception {
        Conveyor c = dao.getById(conveyorId);
        if (c == null) return "Conveyor not found: " + conveyorId;

        if (!"Operational_Idle".equals(c.getState())) {
            return "Cannot assign command from state: " + c.getState();
        }

        dao.updateState(conveyorId, "Operational_Executing");
        dao.updateBusy(conveyorId, true);
        return "Command assigned: conveyor " + conveyorId + " -> Operational_Executing";
    }

    public String commandCompleted(int conveyorId) throws Exception {
        Conveyor c = dao.getById(conveyorId);
        if (c == null) return "Conveyor not found: " + conveyorId;

        if (!"Operational_Executing".equals(c.getState())) {
            return "Cannot complete command from state: " + c.getState();
        }

        dao.updateBusy(conveyorId, false);
        dao.updateState(conveyorId, "Operational_Idle");
        return "Command completed: conveyor " + conveyorId + " -> Operational_Idle";
    }

    /**
     * Restart (after repair):
     * - allowed only from Paused
     * - reruns integrity checks within additional 10 minutes (same timeout)
     */
    public String restart(int conveyorId) throws Exception {
        Conveyor c = dao.getById(conveyorId);
        if (c == null) return "Conveyor not found: " + conveyorId;

        if (!"Paused".equals(c.getState())) {
            return "Restart allowed only from Paused. Current state: " + c.getState();
        }

        return runIntegrityAndTurnOn(conveyorId, true);
    }

    public List<Conveyor> listConveyors() throws Exception {
        return dao.getAllConveyors();
    }

    // ============================
    // Integrity-check core (MECH + ELEC in parallel)
    // ============================

    private String runIntegrityAndTurnOn(int conveyorId, boolean fromRestart) throws Exception {
        dao.updateState(conveyorId, "Initializing");
        dao.updateBusy(conveyorId, false);
        dao.setLastFailureReason(conveyorId, null);
        dao.setPausedFromState(conveyorId, null);

        StringBuilder report = new StringBuilder();
        report.append(fromRestart ? "RESTART integrity checks:\n" : "TURN ON integrity checks:\n");
        report.append("Timeout=").append(INTEGRITY_TIMEOUT_MS).append("ms\n");

        // Up to 3 attempts (timeout retries)
        for (int attempt = 1; attempt <= 3; attempt++) {
            dao.updateAttemptCount(conveyorId, attempt);
            report.append("\nAttempt ").append(attempt).append("/3\n");

            IntegrityOutcome out = runParallelIntegrityCheck(conveyorId, INTEGRITY_TIMEOUT_MS, report);

            if (out.timedOut) {
                report.append("Result: TIMEOUT (auto reset and retry)\n");
                if (attempt == 3) {
                    String reason = "Integrity check timeout in 3 consecutive attempts.";
                    dao.setLastFailureReason(conveyorId, reason);
                    dao.updateState(conveyorId, "Paused");
                    alertService.notifyAdmin("Conveyor " + conveyorId + " entered PAUSED due to: " + reason);
                    report.append("FINAL: PAUSED. Alert sent.\n");
                    return report.toString();
                }
                continue;
            }

            if (!out.ok) {
                dao.setLastFailureReason(conveyorId, out.reason);
                dao.updateState(conveyorId, "Paused");
                alertService.notifyAdmin("Conveyor " + conveyorId + " entered PAUSED due to: " + out.reason);
                report.append("FINAL: FAILED -> PAUSED. Reason: ").append(out.reason).append("\n");
                report.append("Alert sent.\n");
                return report.toString();
            }

            // Success
            dao.updateAttemptCount(conveyorId, 0);
            dao.setLastFailureReason(conveyorId, null);
            dao.updateState(conveyorId, "Operational_Idle");

            report.append("FINAL: PASSED -> Operational_Idle\n");
            return report.toString();
        }

        // Should never happen
        String reason = "Unexpected integrity-check flow error.";
        dao.setLastFailureReason(conveyorId, reason);
        dao.updateState(conveyorId, "Paused");
        alertService.notifyAdmin("Conveyor " + conveyorId + " entered PAUSED due to: " + reason);
        report.append("FINAL: PAUSED. Reason: ").append(reason);
        return report.toString();
    }

    private IntegrityOutcome runParallelIntegrityCheck(int conveyorId, long timeoutMs, StringBuilder report) {
        ExecutorService exec = Executors.newFixedThreadPool(2);

        Callable<CheckResult> mechanical = () -> mechanicalCheck(conveyorId, report);
        Callable<CheckResult> electronic = () -> electronicCheck(conveyorId, report);

        try {
            // Run both checks in parallel, total time capped by timeout
            List<Future<CheckResult>> futures =
                    exec.invokeAll(List.of(mechanical, electronic), timeoutMs, TimeUnit.MILLISECONDS);

            // Timeout means tasks were cancelled
            for (Future<CheckResult> f : futures) {
                if (f.isCancelled()) {
                    return IntegrityOutcome.timeout();
                }
            }

            CheckResult mech = futures.get(0).get();
            CheckResult elec = futures.get(1).get();

            if (!mech.ok) return IntegrityOutcome.fail("Mechanical: " + mech.reason);
            if (!elec.ok) return IntegrityOutcome.fail("Electronic: " + elec.reason);

            return IntegrityOutcome.ok();

        } catch (Exception e) {
            return IntegrityOutcome.fail("Integrity error: " + e.getMessage());
        } finally {
            exec.shutdownNow();
        }
    }

    // ========= Mechanical check (sequential steps) =========
    private CheckResult mechanicalCheck(int conveyorId, StringBuilder report) throws Exception {
        report.append("Mechanical check:\n");

        Conveyor c = dao.getById(conveyorId);
        if (c == null) return CheckResult.fail("Conveyor not found during mechanical check.");

        // 1) Belt tension OK
        stepDelay();
        if (c.getMaxWeightKg() < 200) {
            report.append(" - belt tension: FAIL (maxWeightKg too low)\n");
            return CheckResult.fail("belt tension out of range");
        }
        report.append(" - belt tension: OK\n");

        // 2) No physical obstructions
        stepDelay();
        if (c.getPositionX() < 0 || c.getPositionY() < 0) {
            report.append(" - obstructions: FAIL (invalid position)\n");
            return CheckResult.fail("physical obstruction detected (invalid position)");
        }
        report.append(" - obstructions: OK\n");

        // 3) Rollers/drive rotate freely (basic proxy check)
        stepDelay();
        if (c.isBusy()) {
            report.append(" - rollers/drive: FAIL (busy)\n");
            return CheckResult.fail("rollers/drive resistance detected (busy)");
        }
        report.append(" - rollers/drive: OK\n");

        return CheckResult.ok();
    }

    // ========= Electronic check (sequential steps) =========
    private CheckResult electronicCheck(int conveyorId, StringBuilder report) throws Exception {
        report.append("Electronic check:\n");

        Conveyor c = dao.getById(conveyorId);
        if (c == null) return CheckResult.fail("Conveyor not found during electronic check.");

        // 1) Sensors report correctly (proxy checks)
        stepDelay();
        if (c.getLotNumber() <= 0) {
            report.append(" - sensors: FAIL (lotNumber invalid)\n");
            return CheckResult.fail("sensor report invalid (lotNumber <= 0)");
        }
        report.append(" - sensors: OK\n");

        // 2) Communication stable with central controller (proxy check)
        stepDelay();
        // We treat "DB read ok" as communication OK (since DAO is the controller link)
        report.append(" - communication: OK\n");

        return CheckResult.ok();
    }

    private void stepDelay() {
        if (STEP_DELAY_MS <= 0) return;
        try { Thread.sleep(STEP_DELAY_MS); } catch (InterruptedException ignored) {}
    }

    // ========= Small result types =========
    private static class CheckResult {
        final boolean ok;
        final String reason;

        private CheckResult(boolean ok, String reason) {
            this.ok = ok;
            this.reason = reason;
        }

        static CheckResult ok() { return new CheckResult(true, null); }
        static CheckResult fail(String reason) { return new CheckResult(false, reason); }
    }

    private static class IntegrityOutcome {
        final boolean ok;
        final boolean timedOut;
        final String reason;

        private IntegrityOutcome(boolean ok, boolean timedOut, String reason) {
            this.ok = ok;
            this.timedOut = timedOut;
            this.reason = reason;
        }

        static IntegrityOutcome ok() { return new IntegrityOutcome(true, false, null); }
        static IntegrityOutcome timeout() { return new IntegrityOutcome(false, true, "Timed out"); }
        static IntegrityOutcome fail(String reason) { return new IntegrityOutcome(false, false, reason); }
    }
}
