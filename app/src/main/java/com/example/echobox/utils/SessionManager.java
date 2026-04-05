package com.example.echobox.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager handles user session persistence using SharedPreferences.
 * It stores login state and user profile information (username, email)
 * so that the user remains logged in across app restarts.
 */
public class SessionManager {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    // Preference file name and keys
    private static final String PREF_NAME = "EchoBoxPref";
    private static final String IS_LOGIN = "IsLoggedIn";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_EMAIL = "email";

    /**
     * Constructor for SessionManager.
     */
    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /**
     * Creates a login session by storing the user's details and setting login status to true.
     */
    public void createLoginSession(String username, String email) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.apply(); // Asynchronous save
    }

    /**
     * Checks if a user is currently logged in.
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }

    /**
     * Retrieves the stored username, defaulting to "User" if not found.
     */
    public String getUsername() {
        return pref.getString(KEY_USERNAME, "User");
    }

    /**
     * Retrieves the stored email address.
     */
    public String getEmail() {
        return pref.getString(KEY_EMAIL, "");
    }

    /**
     * Clears all session data (logout).
     */
    public void logoutUser() {
        editor.clear();
        editor.apply();
    }
}