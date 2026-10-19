package control;

import entity.PriceRate;
import entity.PriceRateDAO;

import java.util.List;

public class PricingControl {
    private final PriceRateDAO dao = new PriceRateDAO();

    public List<PriceRate> getAllPriceRates() throws Exception {
        return dao.getAll();
    }
}
