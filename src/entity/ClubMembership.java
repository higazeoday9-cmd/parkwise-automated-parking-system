package entity;

import java.sql.Timestamp;

public class ClubMembership {
    private final int memberId;           // == clientID
    private final Timestamp registrationD; // join date
    private final int discountAmo;        // percent (e.g., 10)

    public ClubMembership(int memberId, Timestamp registrationD, int discountAmo) {
        this.memberId = memberId;
        this.registrationD = registrationD;
        this.discountAmo = discountAmo;
    }

    public int getMemberId() { return memberId; }
    public Timestamp getRegistrationD() { return registrationD; }
    public int getDiscountAmo() { return discountAmo; }
}
