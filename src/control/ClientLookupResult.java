package control;

import java.util.ArrayList;
import java.util.List;

public class ClientLookupResult {
    public boolean success;
    public String message;

    public Integer clientId;
    public String clientFullName;
    public String phone;

    public List<ActiveSessionView> activeSessions = new ArrayList<>();

    public static ClientLookupResult ok(String msg) {
        ClientLookupResult r = new ClientLookupResult();
        r.success = true;
        r.message = msg;
        return r;
    }

    public static ClientLookupResult fail(String msg) {
        ClientLookupResult r = new ClientLookupResult();
        r.success = false;
        r.message = msg;
        return r;
    }
}

