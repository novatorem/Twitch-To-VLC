package com.novatorem.ttvlc.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.novatorem.ttvlc.BuildConfig;
import com.novatorem.ttvlc.TwitchToVLCApplication;
import com.novatorem.ttvlc.activities.main.MainActivity;
import com.novatorem.ttvlc.service.Service;
import com.novatorem.ttvlc.service.Settings;

public abstract class UsageTrackingAppCompatActivity extends AppCompatActivity {
	private String LOG_TAG = getClass().getSimpleName();
	private long startTime;

	public void trackEvent(@StringRes int category, @StringRes int action) {
		trackEvent(getString(category), getString(action));
	}

	public void trackEvent(@StringRes int category, @StringRes int action, @Nullable String label) {
		TwitchToVLCApplication.trackEvent(getString(category), getString(action), label, null);
	}

	public void trackEvent(String category, String action) {
		TwitchToVLCApplication.trackEvent(category, action, null, null);
	}
}
