package control;

import entity.PriceRateDAO;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;

public class IRSImportControl {

    private final PriceRateDAO dao = new PriceRateDAO();

    public int importPriceRates(String jsonPath) throws Exception {
        JSONObject root = (JSONObject) new JSONParser().parse(new FileReader(jsonPath));
        int effectiveYear = ((Long) root.get("effectiveYear")).intValue();
        JSONArray rates = (JSONArray) root.get("rates");

        int inserted = 0;

        for (Object obj : rates) {
            JSONObject r = (JSONObject) obj;

            int priceListID = ((Long) r.get("priceListID")).intValue();
            int lotNumber = ((Long) r.get("lotNumber")).intValue();
            int firstHour = ((Long) r.get("priceFirstHour")).intValue();
            int addHour = ((Long) r.get("priceAdditionalHour")).intValue();
            int fullDay = ((Long) r.get("priceFullDay")).intValue();

            if (!dao.exists(priceListID, lotNumber, effectiveYear)) {
                dao.insert(priceListID, lotNumber, effectiveYear, firstHour, addHour, fullDay);
                inserted++;
            }
        }

        return inserted;
    }
}
