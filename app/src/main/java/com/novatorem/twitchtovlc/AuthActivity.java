package com.novatorem.twitchtovlc;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

import com.novatorem.twitchtovlc.service.Services;

public class AuthActivity extends AppCompatActivity
{
    private final String loginURL = "https://id.twitch.tv/oauth2/authorize?client_id=" + Services.getApplicationClientID() + "&redirect_uri=https://novac.dev/x/ttv/&response_type=code&scope=user_read+user_follows_edit+user_subscriptions";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        WebView web = (WebView)findViewById(R.id.webview);
        WebSettings webSettings = web.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportMultipleWindows(true);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("https://novac.dev/x/ttv/?code=") && url.contains("&scope=user_read+user_follows_edit+user_subscriptions")) {

                    SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preferences), Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("userToken", url.substring(30, 60));
                    editor.commit();

                    finish();
                }
                view.loadUrl(url);
                return false;
            }
        });

        web.loadUrl(loginURL);
    }
}
