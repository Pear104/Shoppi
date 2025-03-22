package com.example.shoppimobile.utils;

import android.util.Base64;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class JwtUtils {

    public static boolean isJwtExpired(String token) {
        try {
            String[] splitToken = token.split("\\.");
            if (splitToken.length < 2) {
                return true; // Invalid token
            }

            // Decode payload (second part of JWT)
            String payloadJson = new String(Base64.decode(splitToken[1], Base64.URL_SAFE), StandardCharsets.UTF_8);
            JSONObject payload = new JSONObject(payloadJson);

            // Get expiration time (exp)
            long exp = payload.optLong("exp", 0);
            long currentTime = System.currentTimeMillis() / 1000; // Convert to seconds

            return exp < currentTime;
        } catch (Exception e) {
            e.printStackTrace();
            return true; // Assume expired if there's an error
        }
    }

}
