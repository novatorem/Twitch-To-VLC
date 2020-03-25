package com.novatorem.ttvlc.adapters;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.net.Uri;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.novatorem.ttvlc.R;
import com.novatorem.ttvlc.model.Panel;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

public class PanelAdapter extends RecyclerView.Adapter<PanelAdapter.PanelViewHolder> {
	private List<Panel> mPanels;
	private List<Target> mTargets;
	private Activity mActivity;

	public PanelAdapter(Activity mActivity) {
		this.mActivity = mActivity;
		this.mPanels = new ArrayList<>();
		this.mTargets = new ArrayList<>();
	}

	public void addPanels(List<Panel> panels) {
		mPanels.addAll(panels);
		notifyDataSetChanged();
	}

	@Override
	public PanelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater
				.from(parent.getContext())
				.inflate(R.layout.cell_panel, parent, false);

		return new PanelViewHolder(itemView);
	}

	@Override
	public int getItemCount() {
		return mPanels.size();
	}

	@Override
	public void onBindViewHolder(final PanelViewHolder holder, int position) {
		Panel mPanel = mPanels.get(position);

		String imageUrl = mPanel.getmImageUrl();
		if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.equals("null")) {
			Picasso.with(mActivity).load(mPanel.getmImageUrl()).into(holder.mImageView);
		}

		final String link = mPanel.getmLinkUrl();
		if (link != null && !link.isEmpty() && !link.equals("null")) {
			holder.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					CustomTabsIntent.Builder mTabs = new CustomTabsIntent.Builder();
					mTabs.setStartAnimations(mActivity, R.anim.slide_in_bottom_anim, R.anim.fade_out_semi_anim);
					mTabs.setExitAnimations(mActivity, R.anim.fade_in_semi_anim, R.anim.slide_out_bottom_anim);

					try {
						mTabs.build().launchUrl(mActivity, Uri.parse(link));

					} catch (ActivityNotFoundException e) {
						e.printStackTrace();
					}
				}
			});
		}


		if (mPanel.getmHtml().isEmpty() || mPanel.getmHtml().equals("null")) {
			holder.mHtmlText.setVisibility(View.GONE);
		} else {
			holder.mHtmlText.setVisibility(View.VISIBLE);
			holder.mHtmlText.setText(Html.fromHtml(mPanel.getmHtml()));
			holder.mHtmlText.setMovementMethod(LinkMovementMethod.getInstance());
		}
	}

	protected class PanelViewHolder extends RecyclerView.ViewHolder {
		protected ImageView mImageView;
		protected TextView mHtmlText;

		PanelViewHolder(View itemView) {
			super(itemView);
			mImageView = (ImageView) itemView.findViewById(R.id.panel_image);
			mHtmlText = (TextView) itemView.findViewById(R.id.panel_html);
		}
	}
}
