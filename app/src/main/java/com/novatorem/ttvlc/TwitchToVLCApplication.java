package com.novatorem.ttvlc;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;

import io.fabric.sdk.android.Fabric;


@SuppressLint("StaticFieldLeak") // It is alright to store application context statically
public class TwitchToVLCApplication extends MultiDexApplication {
    private static Context mContext;

    public static boolean isCrawlerUpdate = false; //ToDo remember to disable for crawler updates

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this.getApplicationContext();

        initNotificationChannels();

        if (!BuildConfig.DEBUG) {
            try {
                Fabric.with(this, new Crashlytics());

                final Fabric fabric = new Fabric.Builder(this)
                        .kits(new Crashlytics())
                        .debuggable(true)
                        .build();
                Fabric.with(fabric);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    static synchronized public FirebaseAnalytics getDefaultTracker() {
        return FirebaseAnalytics.getInstance(mContext);
    }

    public static void trackEvent(@StringRes int category, @StringRes int action, @Nullable String label) {
        TwitchToVLCApplication.trackEvent(mContext.getString(category), mContext.getString(action), label, null);
    }

    public static void trackEvent(String category, String action, @Nullable String label, @Nullable Long value) {
        // No more tracking.
    }

    private void initNotificationChannels() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || notificationManager == null) {
            return;
        }

        notificationManager.createNotificationChannel(
                new NotificationChannel(getString(R.string.live_streamer_notification_id), "New Streamer is live", NotificationManager.IMPORTANCE_LOW)
        );

        notificationManager.createNotificationChannel(
                new NotificationChannel(getString(R.string.stream_cast_notification_id), "Stream Playback Control", NotificationManager.IMPORTANCE_DEFAULT)
        );
    }
}
