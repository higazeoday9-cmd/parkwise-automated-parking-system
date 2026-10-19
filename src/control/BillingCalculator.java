package control;

public class BillingCalculator {

    public static class Bill {
        public int totalHours;   // rounded up
        public int amount;
        public String rateUsed;
    }

    /**
     * Pricing rules:
     * - hours = ceil(minutes/60)
     * - for a day: min(firstHour + additional*(hours-1), fullDay)
     * - for multi-day: days*fullDay + remainder day price
     */
    public Bill calculate(long totalMinutes, int firstHour, int addHour, int fullDay) {
        Bill b = new Bill();

        int hours = (int) Math.ceil(totalMinutes / 60.0);
        if (hours <= 0) hours = 1;
        b.totalHours = hours;

        int days = hours / 24;
        int rem = hours % 24;

        int remCost = 0;
        if (rem > 0) {
            int byHours = firstHour + addHour * Math.max(0, rem - 1);
            remCost = Math.min(byHours, fullDay);
        }

        b.amount = days * fullDay + remCost;

        if (days > 0 && rem == 0) b.rateUsed = "fullDay x " + days;
        else if (days > 0) b.rateUsed = "fullDay x " + days + " + remainder";
        else if (b.amount == fullDay && hours > 1) b.rateUsed = "fullDay";
        else if (hours == 1) b.rateUsed = "firstHour";
        else b.rateUsed = "firstHour + additionalHour";

        return b;
    }
}
