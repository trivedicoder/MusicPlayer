package com.example.echobox.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager handles user session persistence using SharedPreferences.
 * Stores login state and user profile information so the user remains
 * logged in across app restarts.
 */
public class SessionManager {

    private final SharedPreferences pref;

    private static final String PREF_NAME = "EchoBoxPref";
    private static final String IS_LOGIN = "IsLoggedIn";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_EMAIL = "email";

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Creates a login session by storing the user's details and setting login status to true.
     */
    public void createLoginSession(String username, String email) {
        pref.edit()
                .putBoolean(IS_LOGIN, true)
                .putString(KEY_USERNAME, username)
                .putString(KEY_EMAIL, email)
                .apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, "User");
    }

    public String getEmail() {
        return pref.getString(KEY_EMAIL, "");
    }

    /**
     * Clears all session data (logout).
     */
    public void logoutUser() {
        pref.edit().clear().apply();
    }
}
