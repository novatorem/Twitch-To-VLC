package com.novatorem.ttvlc.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.novatorem.ttvlc.R;
import com.novatorem.ttvlc.activities.main.MainActivity;
import com.novatorem.ttvlc.service.Settings;

public class ThemeActivity extends UsageTrackingAppCompatActivity {
	private String theme;

	@Override
	protected void onCreate(Bundle savedInstance) {
		loadTheme();
		super.onCreate(savedInstance);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!applyTheme()) {
			return;
		}

		String currentTheme = new Settings(this).getTheme();
		if (!currentTheme.equals(theme)) {
			recreate();
		}
	}

	private void loadTheme() {
		if (!applyTheme()) {
			return;
		}

		int themeRes = R.style.BlueTheme;
		theme = new Settings(this).getTheme();
		if (theme.equals(getString(R.string.purple_theme_name))) {
			themeRes = R.style.PurpleTheme;
		} else if (theme.equals(getString(R.string.black_theme_name))) {
			themeRes = R.style.BlackTheme;
		} else if (theme.equals(getString(R.string.night_theme_name))) {
			themeRes = R.style.NightTheme;
		} else if (theme.equals(getString(R.string.true_night_theme_name))) {
			themeRes = R.style.TrueNightTheme;
		}
		setTheme(themeRes);
	}

	@Override
	public void recreate() {
		if (this instanceof MainActivity) {
			((MainActivity) this).getRecyclerView().scrollToPosition(0);
		}
		super.recreate();
	}

	public boolean applyTheme() {
		return true;
	}
}
