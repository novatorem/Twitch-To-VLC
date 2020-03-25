package com.novatorem.ttvlc.adapters;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.novatorem.ttvlc.R;
import com.novatorem.ttvlc.model.SettingsCategory;

import net.nrask.srjneeds.SRJAdapter;
import net.nrask.srjneeds.SRJViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsCategoryAdapter extends SRJAdapter<SettingsCategory, SettingsCategoryAdapter.SettingsCategoryViewHolder> {

	@Override
	protected int getLayoutResource(int viewType) {
		return R.layout.cell_settings_category;
	}

	@Override
	protected ViewHolderFactory<SettingsCategoryViewHolder> getViewHolderCreator(int i) {
		return new ViewHolderFactory<SettingsCategoryViewHolder>() {
			@Override
			public SettingsCategoryViewHolder create(View view) {
				return new SettingsCategoryViewHolder(view);
			}
		};
	}

	public class SettingsCategoryViewHolder extends SRJViewHolder<SettingsCategory> {
		@BindView(R.id.txt_category_title)
		protected TextView mTitleView;

		@BindView(R.id.txt_category_summary)
		protected TextView mSummaryView;

		@BindView(R.id.img_category_icon)
		protected ImageView mCategoryIconView;

		public SettingsCategoryViewHolder(View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
		}

		@Override
		protected void onDataBinded(SettingsCategory settingsCategory) {
			mTitleView.setText(settingsCategory.getTitleRes());
			mSummaryView.setText(settingsCategory.getSummaryRes());
			mCategoryIconView.setImageResource(settingsCategory.getIconRes());
		}
	}
}
