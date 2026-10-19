package boundary;

import entity.Conveyor;
import entity.ConveyorDAO;

public class DbSmokeTest {
    public static void main(String[] args) throws Exception {
        ConveyorDAO dao = new ConveyorDAO();
        Conveyor c = dao.getById(1);
        System.out.println(c);
    }
}
