package org.example.demo6;

public class SessionManager {
    private static String currentUser;
    private static String currentRole;
    private static boolean loggedIn = false;

    public static void setUser(String login, String role) {
        currentUser = login;
        currentRole = role;
        loggedIn = true;
    }

    public static boolean isAuthenticated() {
        return loggedIn;
    }

    public static String getLogin() {
        return currentUser;
    }

    public static String getRole() {
        return currentRole;
    }

    public static void logout() {
        currentUser = null;
        currentRole = null;
        loggedIn = false;
    }
}