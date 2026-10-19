package control;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ParkingEntryControlAdapter {

    public static class ArrivalResult {
        public boolean barrierOpened;
        public boolean success;
        public String message;
    }

    public ArrivalResult simulateArrival(int lotNumber, String vehicleNumber) throws Exception {
        ArrivalResult out = new ArrivalResult();
        out.success = false;
        out.barrierOpened = false;
        out.message = "";

        Object ctrl = createParkingEntryControlInstance();
        if (ctrl == null) {
            out.message = "Could not load ParkingEntryControl (check package name).";
            return out;
        }

        // try multiple method names (you may have one of these)
        String[] names = new String[]{
                "simulateArrivalInYourExistingCode",
                "simulateArrival",
                "onArrival",
                "arrive",
                "handleArrival",
                "processArrival",
                "gateArrival",
                "vehicleArrived"
        };

        // try common signatures: (int, String) and (String, int)
        for (String name : names) {
            Method m = findMethod(ctrl.getClass(), name, int.class, String.class);
            if (m != null) {
                return invokeAndParse(ctrl, m, lotNumber, vehicleNumber);
            }
            m = findMethod(ctrl.getClass(), name, String.class, int.class);
            if (m != null) {
                return invokeAndParse(ctrl, m, vehicleNumber, lotNumber);
            }
        }

        out.message = "No suitable arrival method found in ParkingEntryControl. "
                + "Expected something like arrive(int lot, String vehicle) or onArrival(int lot, String vehicle).";
        return out;
    }

    private ArrivalResult invokeAndParse(Object ctrl, Method m, Object p1, Object p2) throws Exception {
        ArrivalResult out = new ArrivalResult();
        try {
            m.setAccessible(true);
            Object ret = m.invoke(ctrl, p1, p2);

            // If method returns void -> assume success (unless exception)
            if (m.getReturnType().equals(void.class)) {
                out.success = true;
                out.barrierOpened = true;
                out.message = "Arrival processed (void return).";
                return out;
            }

            if (ret == null) {
                out.success = true;
                out.barrierOpened = true;
                out.message = "Arrival processed (null return).";
                return out;
            }

            // Boolean return
            if (ret instanceof Boolean) {
                out.barrierOpened = (Boolean) ret;
                out.success = out.barrierOpened;
                out.message = out.barrierOpened ? "Barrier opened ✅" : "Barrier did NOT open ❌";
                return out;
            }

            // String return
            if (ret instanceof String) {
                String msg = ((String) ret);
                out.message = msg;
                String low = msg.toLowerCase();
                out.barrierOpened = !(low.contains("fail") || low.contains("error") || low.contains("not"));
                out.success = out.barrierOpened;
                return out;
            }

            // Result object: try common getters/fields
            Boolean bOpen = tryGetBoolean(ret, "isBarrierOpened", "getBarrierOpened", "barrierOpened");
            Boolean success = tryGetBoolean(ret, "isSuccess", "getSuccess", "success");

            out.barrierOpened = (bOpen != null) ? bOpen : false;
            out.success = (success != null) ? success : out.barrierOpened;

            String msg = tryGetString(ret, "getMessage", "message", "msg");
            out.message = (msg != null) ? msg : (out.success ? "Arrival OK ✅" : "Arrival failed ❌");

            return out;

        } catch (Exception ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            throw new Exception(cause.getMessage(), cause);
        }
    }

    private Method findMethod(Class<?> cls, String name, Class<?> p1, Class<?> p2) {
        try {
            return cls.getDeclaredMethod(name, p1, p2);
        } catch (Exception ignored) {
        }
        try {
            return cls.getMethod(name, p1, p2);
        } catch (Exception ignored) {
        }
        return null;
    }

    private Object createParkingEntryControlInstance() {
        String[] classNames = {
                "control.ParkingEntryControl",
                "Control.ParkingEntryControl",
                "controllers.ParkingEntryControl"
        };

        for (String cn : classNames) {
            try {
                Class<?> c = Class.forName(cn);
                Constructor<?> ctor = c.getDeclaredConstructor();
                ctor.setAccessible(true);
                return ctor.newInstance();
            } catch (Exception ignored) {}
        }
        return null;
    }

    private Boolean tryGetBoolean(Object obj, String... names) {
        for (String n : names) {
            // try method
            try {
                Method m = obj.getClass().getMethod(n);
                Object r = m.invoke(obj);
                if (r instanceof Boolean) return (Boolean) r;
            } catch (Exception ignored) {}
            // try field
            try {
                var f = obj.getClass().getDeclaredField(n);
                f.setAccessible(true);
                Object r = f.get(obj);
                if (r instanceof Boolean) return (Boolean) r;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String tryGetString(Object obj, String... names) {
        for (String n : names) {
            // try method
            try {
                Method m = obj.getClass().getMethod(n);
                Object r = m.invoke(obj);
                if (r != null) return String.valueOf(r);
            } catch (Exception ignored) {}
            // try field
            try {
                var f = obj.getClass().getDeclaredField(n);
                f.setAccessible(true);
                Object r = f.get(obj);
                if (r != null) return String.valueOf(r);
            } catch (Exception ignored) {}
        }
        return null;
    }
}
