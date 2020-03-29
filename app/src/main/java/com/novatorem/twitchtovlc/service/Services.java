package com.novatorem.twitchtovlc.service;

import android.util.Log;

import com.novatorem.twitchtovlc.misc.SecretKeys;

import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Services {
    public static String getApplicationClientID() {
        return SecretKeys.ApplicationID;
    }

    public static String getAnonTokenID() {
        return SecretKeys.AnonTokenID;
    }

    public static String getTwitchID() {
        return SecretKeys.twitchID;
    }

    public static String urlToJSONString(String urlToRead) {
        // Alright, so sometimes Twitch decides that Pocket Plays's client ID should be blocked. Currently only happens for the hidden /api endpoints.
        // IF we are being blocked, then retry the request with Twitch web ClientID. They are typically not blocking this.
        String result = urlToJSONString(urlToRead, true); // "{\"error\":\"Gone\",\"status\":410,\"message\":\"this API has been removed.\"}";
        try {
            boolean retryWithWebClientId = false;
            if (result.isEmpty()) {
                retryWithWebClientId = true;
            } else {
                JSONObject resultJson = new JSONObject(result);
                int status = resultJson.getInt("status");
                String error = resultJson.getString("error");
                retryWithWebClientId = status == 410 || error.equals("Gone");
            }

            if (retryWithWebClientId) {
                result = urlToJSONString(urlToRead, false);
            }

        } catch (Exception exc) {

        }

        return result;
    }

    public static String urlToJSONString(String urlToRead, Boolean usePocketPlaysClientId) {

        String clientId;
        if (usePocketPlaysClientId) {
            clientId = Services.getApplicationClientID();
        } else {
            clientId = getTwitchID();
        }

        URL url;
        HttpURLConnection conn = null;
        Scanner in = null;
        String result = "";

        try {
            url = new URL(urlToRead);

            conn = (HttpURLConnection) url.openConnection();

            conn.setReadTimeout(5000);
            conn.setConnectTimeout(3000);
            conn.setRequestProperty("Client-ID", clientId);
            conn.setRequestProperty("Accept", "application/vnd.twitchtv.v5+json");
            conn.setRequestMethod("GET");
            in = new Scanner(new InputStreamReader(conn.getInputStream()));

            while (in.hasNextLine()) {
                String line = in.nextLine();
                result += line;
            }

            in.close();
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null)
                in.close();
            if (conn != null)
                conn.disconnect();
        }

        if (result.length() == 0 || (result.length() >= 1 && result.charAt(0) != '{')) {
            Log.v("URL TO JSON STRING", urlToRead + " did not successfully get read");
            Log.v("URL TO JSON STRING", "Result of reading - " + result);
        }

        return result;
    }
}
