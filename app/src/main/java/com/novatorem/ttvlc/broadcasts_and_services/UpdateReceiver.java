package com.novatorem.ttvlc.broadcasts_and_services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.novatorem.ttvlc.service.Settings;

public class UpdateReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED))
			new Settings(context).setIsUpdated(true);
	}
}
