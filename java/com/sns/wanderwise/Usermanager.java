package com.sns.wanderwise;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONObject;
import org.json.JSONArray;

public class Usermanager {
    private static final String USERS_KEY = "users";
    private static SharedPreferences prefs;
    
    private static void initSharedPreferences(Context context) {
        if (prefs == null) {
            // Using the modern way to get SharedPreferences
            prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        }
    }
    
    public static void registerUser(Context context, String ign, String username, String password) {
        initSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        String json = prefs.getString(USERS_KEY, "{}");
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray usersArray = jsonObject.optJSONArray("users");
            if (usersArray == null) {
                usersArray = new JSONArray();
            }
            JSONObject newUser = new JSONObject();
            newUser.put("ign", ign);  // In-game name
            newUser.put("username", username);  // Email or login username
            newUser.put("password", password);
            usersArray.put(newUser);
            jsonObject.put("users", usersArray);
            editor.putString(USERS_KEY, jsonObject.toString());
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static boolean validateUser(Context context, String username, String password) {
        initSharedPreferences(context);
        String json = prefs.getString(USERS_KEY, "{}");
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray usersArray = jsonObject.optJSONArray("users");
            if (usersArray != null) {
                for (int i = 0; i < usersArray.length(); i++) {
                    JSONObject user = usersArray.getJSONObject(i);
                    if (user.getString("username").equals(username) &&
                        user.getString("password").equals(password)) {
                        return true; // Valid user
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false; // Invalid user
    }
    public static String getUserIGN(Context context, String username) {
    initSharedPreferences(context);
    String json = prefs.getString(USERS_KEY, "{}");
    try {
        JSONObject jsonObject = new JSONObject(json);
        JSONArray usersArray = jsonObject.optJSONArray("users");
        if (usersArray != null) {
            for (int i = 0; i < usersArray.length(); i++) {
                JSONObject user = usersArray.getJSONObject(i);
                if (user.getString("username").equals(username)) {
                    return user.getString("ign"); // Return the user's IGN
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null; // Return null if no user found
}
}
