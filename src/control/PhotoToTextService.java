package control;

import java.io.File;

public class PhotoToTextService {

    public VehicleExtractionResult extract(String scannedVehicleNumber, File front, File rear, File side,
                                           String typeText, String rawColor, String rawSize, String rawWeight) {

        VehicleExtractionResult r = new VehicleExtractionResult();

        // Vehicle number: prefer scanned number
        r.setVehicleNumber(safeText(scannedVehicleNumber));

        // Type: user provides text (mock)
        r.setType(safeText(typeText));

        // Color normalization (only 4 known colors else "other")
        String color = safeText(rawColor).toLowerCase();
        if (color.equals("white") || color.equals("black") || color.equals("silver") || color.equals("blue")) {
            r.setColor(color);
        } else {
            r.setColor("other");
        }

        // Size normalization
        String size = safeText(rawSize).toLowerCase();
        if (!size.equals("small") && !size.equals("medium") && !size.equals("large")) {
            size = "medium";
        }
        r.setSize(size);

        // Weight parsing
        int weight = 0;
        try {
            weight = Integer.parseInt(safeText(rawWeight));
        } catch (Exception ignored) {}
        if (weight <= 0) weight = 1200; // default
        r.setWeightKg(weight);

        return r;
    }

    private String safeText(String s) {
        return s == null ? "" : s.trim();
    }
}
