package control;

public enum UserRole {
    ADMIN("System Administrator"),
    CLIENT("Customer / Subscriber");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}