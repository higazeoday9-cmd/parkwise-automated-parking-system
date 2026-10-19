package control;

import entity.ParkingLot;
import entity.ParkingLotDAO;

import java.util.List;

public class ParkingLotControl {
    private final ParkingLotDAO dao = new ParkingLotDAO();

    // your existing method
    public List<ParkingLot> getAllLots() throws Exception {
        return dao.getAll();
    }

    // alias for pages that call getAll()
    public List<ParkingLot> getAll() throws Exception {
        return getAllLots();
    }
}

