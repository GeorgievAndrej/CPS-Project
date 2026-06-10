package com.cps.teacherapp.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {

    private static final String PREF_NAME = "cps_prefs";
    private static final String KEY_TOKEN    = "auth_token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FULLNAME = "full_name";

    private static TokenManager instance;
    private final SharedPreferences prefs;

    // Приватен конструктор — Singleton
    private TokenManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // getInstance() — ова го бараше новиот MainActivity
    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context);
        }
        return instance;
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void saveUserInfo(String username, String fullName) {
        prefs.edit()
                .putString(KEY_USERNAME, username)
                .putString(KEY_FULLNAME, fullName)
                .apply();
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String getFullName() {
        return prefs.getString(KEY_FULLNAME, "");
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}