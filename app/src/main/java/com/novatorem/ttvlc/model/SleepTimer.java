package com.novatorem.ttvlc.model;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.novatorem.ttvlc.R;
import com.novatorem.ttvlc.service.DialogService;
import com.novatorem.ttvlc.service.Settings;

import biz.kasual.materialnumberpicker.MaterialNumberPicker;

/**
 * Created by Sebastian Rask Jepsen on 22/07/16.
 */
public class SleepTimer {
	private String LOG_TAG = getClass().getSimpleName();
	private int sleepTimerProgressMinutes;
	private Handler sleepTimerHandler;
	private Runnable sleepTimerRunnable;
	private SleepTimerDelegate delegate;
	private Settings settings;
	private Context context;
	private boolean isRunning;

	public SleepTimer(final SleepTimerDelegate delegate, Context context) {
		this.settings = new Settings(context);
		this.context = context;
		this.delegate = delegate;
		sleepTimerProgressMinutes = Integer.MIN_VALUE;
		sleepTimerHandler = new Handler();
		sleepTimerRunnable = new Runnable() {
			@Override
			public void run() {
				try {
					if (sleepTimerProgressMinutes == 0) {
						isRunning = false;
						delegate.onTimesUp();
					} else {
						sleepTimerProgressMinutes--;
						sleepTimerHandler.postDelayed(this, 1000 * 60);
					}
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(LOG_TAG, "Sleep Timer runnable failed");
				}
			}
		};
	}

	public void show(Activity activity) {
		int hourToShow = settings.getStreamSleepTimerHour();
		int minuteToShow = settings.getStreamSleepTimerMinute();

		if (isRunning && sleepTimerProgressMinutes > 0) {
			hourToShow = sleepTimerProgressMinutes / 60;
			minuteToShow = sleepTimerProgressMinutes % 60;
		}

		DialogService.getSleepTimerDialog(activity, isRunning, new MaterialDialog.SingleButtonCallback() {
				@Override
				public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
					View customView = dialog.getCustomView();
					MaterialNumberPicker hourPicker = (MaterialNumberPicker) customView.findViewById(R.id.hourPicker);
					MaterialNumberPicker minPicker = (MaterialNumberPicker) customView.findViewById(R.id.minutePicker);

					int hour = hourPicker.getValue(), minute = minPicker.getValue();

					if (isRunning) {
						sleepTimerProgressMinutes = hour * 60 + minute;
					} else {
						start(hour, minute);
					}
				}},
				new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						if (isRunning) {
							stop();
						}
					}
				},
				hourToShow,
				minuteToShow)
				.show();
	}

	private void start(int hour, int minute) {
		isRunning = true;
		sleepTimerProgressMinutes = hour * 60 + minute;
		sleepTimerHandler.removeCallbacks(sleepTimerRunnable);
		sleepTimerHandler.postDelayed(sleepTimerRunnable, 0);
		settings.setStreamSleepTimerHour(hour);
		settings.setStreamSleepTimerMinute(minute);
		if (hour > 0) {
			delegate.onStart(context.getString(R.string.stream_sleep_timer_started, hour, minute));
		} else {
			delegate.onStart(context.getString(R.string.stream_sleep_timer_started_minutes_only, minute));
		}
	}

	private void stop() {
		isRunning = false;
		sleepTimerHandler.removeCallbacks(sleepTimerRunnable);
		delegate.onStop(context.getString(R.string.stream_sleep_timer_stopped));
	}

	public interface SleepTimerDelegate {
		void onTimesUp();
		void onStart(String message);
		void onStop(String message);
	}
}
