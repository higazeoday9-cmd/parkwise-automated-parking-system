package control;

public class AuthSession {
    private static UserRole currentRole = null;
    private static String currentUserIdentifier = null; // admin username or client phone

    public static void login(UserRole role, String identifier) {
        currentRole = role;
        currentUserIdentifier = identifier;
    }

    public static void logout() {
        currentRole = null;
        currentUserIdentifier = null;
    }

    public static boolean isLoggedIn() {
        return currentRole != null;
    }

    public static UserRole getCurrentRole() {
        return currentRole;
    }

    public static String getCurrentUserIdentifier() {
        return currentUserIdentifier;
    }

    public static boolean isAdmin() {
        return currentRole == UserRole.ADMIN;
    }
}