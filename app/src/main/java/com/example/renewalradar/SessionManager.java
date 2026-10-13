package com.example.renewalradar;

import android.content.Context;
import android.content.SharedPreferences;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Remembers who is signed in (using SharedPreferences), so the user
 * stays logged in after closing the app. Also hashes passwords.
 */
public class SessionManager {

    private static final String PREFS = "renewal_radar_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void login(User user) {
        prefs.edit()
                .putLong(KEY_USER_ID, user.id)
                .putString(KEY_USER_NAME, user.name)
                .apply();
    }

    public void logout() {
        prefs.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return getUserId() != -1;
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    /** Turns a password into a SHA-256 hash, so the real password is never saved. */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
