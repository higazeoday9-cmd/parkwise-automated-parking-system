package control;

public class AlertService {

    // Alert stub (spec says alert is sent to administrator)
    public void notifyAdmin(String message) {
        System.out.println("!!! ADMIN ALERT !!! " + message);
    }
}
