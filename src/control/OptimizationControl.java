package control;

import entity.Conveyor;
import entity.ParkingSpace;

import java.util.List;

public class OptimizationControl {

    /**
     * Finds nearest space by:
     * - prioritize same floor first (smaller floor diff)
     * - then Euclidean distance on (x,y)
     */
    public ParkingSpace findNearest(Conveyor conveyor, List<ParkingSpace> candidates) {
        if (candidates == null || candidates.isEmpty()) return null;

        ParkingSpace best = null;
        double bestScore = Double.MAX_VALUE;

        for (ParkingSpace s : candidates) {
            double floorPenalty = Math.abs(conveyor.getFloorNumber() - s.getFloorNumber()) * 1000000.0;
            double dx = conveyor.getPositionX() - s.getPositionX();
            double dy = conveyor.getPositionY() - s.getPositionY();
            double dist = Math.sqrt(dx * dx + dy * dy);

            double score = floorPenalty + dist;
            if (score < bestScore) {
                bestScore = score;
                best = s;
            }
        }
        return best;
    }
}
