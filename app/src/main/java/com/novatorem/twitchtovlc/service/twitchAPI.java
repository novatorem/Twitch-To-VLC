package com.novatorem.twitchtovlc.service;

import android.content.Context;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.jacksonandroidnetworking.JacksonParserFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class twitchAPI {
    public static final String QUALITY_MOBILE 		= "mobile";
    public static final String QUALITY_LOW 			= "low";
    public static final String QUALITY_MEDIUM 		= "medium";
    public static final String QUALITY_HIGH			= "high";
    public static final String QUALITY_SOURCE 		= "chunked";
    public static final String QUALITY_AUTO 		= "auto";
    public static final String QUALITY_AUDIO_ONLY 	= "audio_only";

    public static final String QUALITY_720p60 = "720p60";
    public static final String QUALITY_720p30 = "720p30";
    public static final String QUALITY_540p30 = "540p30";
    public static final String QUALITY_480p30 = "480p30";
    public static final String QUALITY_360p30 = "360p30";
    public static final String QUALITY_240p30 = "240p30";
    public static final String QUALITY_144p30 = "144p30";

    private final String[] NEW_QUALITIES = {
            QUALITY_240p30,
            QUALITY_480p30,
            QUALITY_720p30,
            QUALITY_720p60
    };

    public static final String[] QUALITIES 			= {
            QUALITY_MOBILE,
            QUALITY_LOW,
            QUALITY_MEDIUM,
            QUALITY_HIGH,
            QUALITY_SOURCE,
            QUALITY_AUTO,
            QUALITY_AUDIO_ONLY
    };

    public static void getStreams(Context applicationContext) {
        AndroidNetworking.initialize(applicationContext);
        AndroidNetworking.setParserFactory(new JacksonParserFactory());

        AndroidNetworking.get("https://api.twitch.tv/helix/{search}")
                .addPathParameter("search", "streams")
                .addQueryParameter("game_id", "513143")
                .addQueryParameter("first", "1")
                .addHeaders("Client-ID", Services.getApplicationClientID())
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("JSON Success", String.valueOf(response));
                    }

                    @Override
                    public void onError(ANError error) {
                        Log.d("JSON Failed", String.valueOf(error.getMessage()));
                    }
                });
    }

    public static void getGame(Context applicationContext) {
        AndroidNetworking.initialize(applicationContext);
        AndroidNetworking.setParserFactory(new JacksonParserFactory());

        AndroidNetworking.get("https://api.twitch.tv/helix/{search}")
                .addPathParameter("search", "games")
                .addQueryParameter("name", "teamfight tactics")
                .addHeaders("Client-ID", Services.getApplicationClientID())
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("JSON Success", String.valueOf(response));
                    }

                    @Override
                    public void onError(ANError error) {
                        Log.d("JSON Failed", String.valueOf(error.getResponse()));
                    }
                });
    }

    private HashMap<String, String> parseM3U8(String urlToRead) {

        URL url;
        HttpURLConnection conn = null;
        Scanner in = null;
        String line;
        StringBuilder result = new StringBuilder();

        try {
            url = new URL(urlToRead.replace(" ","%20"));

            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(3000);
            conn.setConnectTimeout(3000);
            conn.setRequestMethod("GET");

            InputStream inputs;
            if (conn.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                inputs = conn.getInputStream();
            } else {
                // Error
                inputs = conn.getErrorStream();
            }

            in = new Scanner(new InputStreamReader(inputs));

            while(in.hasNextLine()) {
                line = in.nextLine();
                result.append(line).append("\n");
            }

            in.close();
            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(in != null)
                in.close();
            if(conn != null)
                conn.disconnect();
        }

        HashMap<String, String> resultList = new HashMap<>();

        ArrayList<String> qualitiesAvailable = new ArrayList<>(Arrays.asList(QUALITIES));
        qualitiesAvailable.addAll(new ArrayList<>(Arrays.asList(NEW_QUALITIES)));
        for(String quality : qualitiesAvailable) {
            Pattern p = Pattern.compile("VIDEO=\"" + quality + "\"\\n(http:\\/\\/\\S+)");
            Matcher m = p.matcher(result.toString());

            if(m.find()) {
                String resultQualityName = quality;
                switch (quality) {
                    case QUALITY_720p60: resultQualityName = QUALITY_HIGH; break;
                    case QUALITY_720p30: resultQualityName = QUALITY_MEDIUM; break;
                    case QUALITY_480p30: resultQualityName = QUALITY_LOW; break;
                    case QUALITY_240p30: resultQualityName = QUALITY_MOBILE; break;
                }

                resultList.put(resultQualityName, m.group(1));
            }
        }
        resultList.put(QUALITY_AUTO, urlToRead);

        return resultList;
    }

    //public static HashMap<String, String> getURL(String streamerName) {
    public static void getURL(String streamerName) {
        String sig = "";
        String tokenString = "";

        String resultString = Services.urlToJSONString("https://api.twitch.tv/api/channels/" + streamerName + "/access_token");
        try {
            JSONObject resultJSON = new JSONObject(resultString);
            tokenString = URLEncoder.encode(resultJSON.getString("token")); // URL Encode token
            sig = resultJSON.getString("sig");

            Log.d("ACCESS_TOKEN_STRING", tokenString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String streamUrl = String.format("http://usher.twitch.tv/api/channel/hls/%s.m3u8" +
                "?player=twitchweb&" +
                "&token=%s" +
                "&sig=%s" +
                "&allow_audio_only=true" +
                "&allow_source=true" +
                "&type=any" +
                "&p=%s", streamerName, tokenString, sig, "" + new Random().nextInt(6));

        Log.d("URL", "HSL Playlist URL: " + streamUrl);
        //return parseM3U8(streamUrl);
    }


}
