package com.novatorem.ttvlc.activities.setup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.novatorem.ttvlc.R;
import com.novatorem.ttvlc.activities.UsageTrackingAppCompatActivity;
import com.novatorem.ttvlc.activities.main.MyStreamsActivity;
import com.novatorem.ttvlc.activities.settings.SettingsNotificationsActivity;
import com.novatorem.ttvlc.misc.ResizeWidthAnimation;
import com.novatorem.ttvlc.service.Service;
import com.novatorem.ttvlc.service.Settings;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

public class NotificationActivity extends UsageTrackingAppCompatActivity {
	private final String LOG_TAG 						= getClass().getSimpleName();
	private SupportAnimator transitionAnimationWhite	= null;
	private SupportAnimator transitionAnimationBlue 	= null;

	private int mFABWidth 								= 0;
	private boolean hasCustomized 						= false;
	private boolean hasTransitioned						= false;
	private boolean hasSingleTransition					= false;

	private final int EXPAND_DURATION 					= 500;
	private final int REVEAL_ANIMATION_DURATION 		= 650;
	private final int REVEAL_ANIMATION_DELAY 			= 200;
	private final int HIDE_VIEW_ANIMATION_DURATION 		= 550;

	private final int SHOW_LOGO_ANIMATION_DURATION 		= 600;
	private final int SHOW_TEXT_ANIMATION_DURATION 		= 600;
	private final int SHOW_TEXT_ANIMATION_BASE_DELAY 	= 105;
	private final int SHOW_TEXT_ANIMATION_DELAY 		= 105;

	private final int SHOW_CONTINUE_ICON_DURATION 		= 650;
	private final int SHOW_CONTINUE_ICON_DELAY 			= 600;

	private final int SHOW_CUSTOMIZE_TEXT_DURATION 		= 300;
	private final int SHOW_CUSTOMIZE_TEXT_DELAY 		= 200;


	private ImageView 		mGearIcon,
							mContinueIcon;
	private View 			mContinueFAB,
							mContinueFABShadow,
							mTransitionViewWhite,
							mTransitionViewBlue;
	private TextView 		mLoginTextLineOne,
							mCustomizeText,
							mSkipText;
	private RelativeLayout 	mLoginTextContainer,
							mLayout;
	private FrameLayout 	mContinueFABContainer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification);

		mLayout					= (RelativeLayout) findViewById(R.id.layout);
		mLoginTextContainer		= (RelativeLayout) findViewById(R.id.notification_text_container);
		mContinueFABContainer 	= (FrameLayout) findViewById(R.id.notification_continue_circle_container);
		mGearIcon 				= (ImageView) findViewById(R.id.notification_icon);
		mContinueIcon			= (ImageView) findViewById(R.id.forward_arrow);
		mLoginTextLineOne 		= (TextView) findViewById(R.id.notification_text_line_one);
		mCustomizeText			= (TextView) findViewById(R.id.customize_text);
		mSkipText				= (TextView) findViewById(R.id.skip_text);
		mContinueFAB 			= findViewById(R.id.notification_continue_circle);
		mContinueFABShadow 		= findViewById(R.id.notification_continue_circle_shadow);
		mTransitionViewWhite 	= findViewById(R.id.transition_view);
		mTransitionViewBlue 	= findViewById(R.id.transition_view_blue);

		mContinueIcon		.setVisibility(View.INVISIBLE);
		mLoginTextLineOne	.setVisibility(View.INVISIBLE);
		mSkipText			.setVisibility(View.INVISIBLE);
		mGearIcon			.setVisibility(View.INVISIBLE);
		mTransitionViewBlue	.setVisibility(View.INVISIBLE);
		mTransitionViewWhite.setVisibility(View.INVISIBLE);
		mCustomizeText		.setVisibility(View.INVISIBLE);

		float textPosition = (int) (2.5 * (getScreenHeight() / 5));
		mLoginTextContainer	.setY(textPosition);

		mContinueFABContainer	.bringToFront();
		mLoginTextLineOne		.bringToFront();
		Service.bringToBack(mTransitionViewWhite);
		Service.bringToBack(mTransitionViewBlue);

		showLogoAnimations();
		showTextLineAnimations(mLoginTextLineOne, 1);
		showTextLineAnimations(mSkipText, 2);

		/* Wait until the view has been laid out and measured THEN expand the width of the FAB */
		ViewTreeObserver vto = mLayout.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				mFABWidth  = mContinueFABContainer.getMeasuredWidth();
				expandContinueButtonAnimations();
				ViewTreeObserver obs = mLayout.getViewTreeObserver();
				obs.removeGlobalOnLayoutListener(this);

			}
		});

		mSkipText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final SupportAnimator.AnimatorListener navigateToNextActivityListener = new SupportAnimator.AnimatorListener(){

					@Override
					public void onAnimationStart() {

					}

					@Override
					public void onAnimationEnd() {
						navigateToNextActivity();
					}

					@Override
					public void onAnimationCancel() {

					}

					@Override
					public void onAnimationRepeat() {

					}
				};
				Animation.AnimationListener transitionAnimationListener = null;

				if(LoginActivity.hasLoadedFollows()) {
					hasSingleTransition = true;
					transitionAnimationListener = new Animation.AnimationListener() {
						@Override
						public void onAnimationStart(Animation animation) {

						}

						@Override
						public void onAnimationEnd(Animation animation) {
							showSingleTransitionAnimation().addListener(navigateToNextActivityListener);
						}

						@Override
						public void onAnimationRepeat(Animation animation) {

						}
					};
				} else {
					hasSingleTransition = false;
					transitionAnimationListener = new Animation.AnimationListener() {
						@Override
						public void onAnimationStart(Animation animation) {

						}

						@Override
						public void onAnimationEnd(Animation animation) {
							showTransitionAnimation().addListener(navigateToNextActivityListener);
						}

						@Override
						public void onAnimationRepeat(Animation animation) {

						}
					};
				}

				collapseContinueButtonAnimations().setAnimationListener(transitionAnimationListener);
			}
		});

		mContinueFABContainer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(LOG_TAG, hasTransitioned + "");
				if(!hasTransitioned) {
					hasCustomized = true;
					Intent intent = new Intent(getBaseContext(), SettingsNotificationsActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

					ActivityOptionsCompat settingsAnim = ActivityOptionsCompat.makeCustomAnimation(NotificationActivity.this, R.anim.slide_in_right_anim, R.anim.fade_out_semi_anim);
					ActivityCompat.startActivity(NotificationActivity.this, intent, settingsAnim.toBundle());
				} else {
					mSkipText.performClick();
				}

			}
		});
	}
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if(hasFocus && hasCustomized){
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					mSkipText.performClick();
				}
			}, 50);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(LOG_TAG, "On Resumes Activity");

		if(transitionAnimationWhite != null && hasTransitioned) {
			if(hasSingleTransition) {
				showSingleReverseTransitionAnimation();
			} else {
				showReverseTransitionAnimation();
			}

		}
	}

	@Override
	public void onBackPressed() {
			collapseContinueButtonAnimations();
			hideAllViews().setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					NotificationActivity.super.onBackPressed();
					// We don't want a transition when going back. The activities handle the animation themselves.
					overridePendingTransition(0, 0);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}
			});

	}

	private int getScreenHeight() {
		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		final DisplayMetrics displayMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(displayMetrics);
		return displayMetrics.heightPixels;
	}

	private void navigateToNextActivity() {
		hasTransitioned = true;
		hasCustomized = false;
		Settings settings = new Settings(getBaseContext());
		settings.setSetup(true);
		if(!LoginActivity.hasLoadedFollows()) {
			this.startActivity(new Intent(getBaseContext(), ConfirmSetupActivity.class));
			this.overridePendingTransition(0, 0);
		} else {
			this.startActivity(new Intent(getBaseContext(), MyStreamsActivity.class));
			this.overridePendingTransition(0, 0);
		}

	}

	/** Animations from here and down */

	private SupportAnimator showSingleTransitionAnimation() {
		// Get the center for the FAB
		int cx = (int) mContinueFABContainer.getX() + mContinueFABContainer.getMeasuredHeight() / 2;
		int cy = (int) mContinueFABContainer.getY() + mContinueFABContainer.getMeasuredWidth() / 2;

		// get the final radius for the clipping circle
		int dx = Math.max(cx, mTransitionViewWhite.getWidth() - cx);
		int dy = Math.max(cy, mTransitionViewWhite.getHeight() - cy);
		float finalRadius = (float) Math.hypot(dx, dy);

		mTransitionViewBlue.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		mTransitionViewWhite.setLayerType(View.LAYER_TYPE_HARDWARE, null);

		final SupportAnimator whiteTransitionAnimation =
				ViewAnimationUtils.createCircularReveal(mTransitionViewWhite, cx, cy, 0, finalRadius);
		whiteTransitionAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
		whiteTransitionAnimation.setDuration(REVEAL_ANIMATION_DURATION);
		whiteTransitionAnimation.addListener(new SupportAnimator.AnimatorListener() {
			@Override
			public void onAnimationStart() {
				mTransitionViewWhite.bringToFront();
				mTransitionViewWhite.setVisibility(View.VISIBLE);
				mContinueFABContainer.setClickable(false);
				if(mContinueIcon.getVisibility() == View.VISIBLE) {
					hideContinueIconAnimations();
				}
			}

			@Override
			public void onAnimationEnd() {
				transitionAnimationWhite = whiteTransitionAnimation;
			}

			@Override
			public void onAnimationCancel() {

			}

			@Override
			public void onAnimationRepeat() {

			}
		});
		whiteTransitionAnimation.start();
		return whiteTransitionAnimation;
	}

	private SupportAnimator showSingleReverseTransitionAnimation() {
		mTransitionViewWhite.setLayerType(View.LAYER_TYPE_HARDWARE, null);

		final SupportAnimator whiteReversed = transitionAnimationWhite.reverse();
		whiteReversed.setInterpolator(new AccelerateDecelerateInterpolator());
		whiteReversed.addListener(new SupportAnimator.AnimatorListener() {
			@Override
			public void onAnimationStart() {
				mTransitionViewWhite.setVisibility(View.VISIBLE);
				mTransitionViewWhite.bringToFront();
			}

			@Override
			public void onAnimationEnd() {
				Service.bringToBack(mTransitionViewWhite);
				mTransitionViewWhite.setLayerType(View.LAYER_TYPE_NONE, null);
				mTransitionViewWhite.setVisibility(View.INVISIBLE);
				mContinueFABContainer.setClickable(true);

				mContinueIcon.bringToFront();
				mContinueIcon.setVisibility(View.VISIBLE);
				showContinueIconAnimations(360);
			}

			@Override
			public void onAnimationCancel() {

			}

			@Override
			public void onAnimationRepeat() {

			}
		});
		whiteReversed.setDuration(REVEAL_ANIMATION_DURATION);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				whiteReversed.start();
			}
		}, REVEAL_ANIMATION_DELAY);
		return whiteReversed;
	}

	private SupportAnimator showTransitionAnimation() {
		// Get the center for the FAB
		int cx = (int) mContinueFABContainer.getX() + mContinueFABContainer.getMeasuredHeight() / 2;
		int cy = (int) mContinueFABContainer.getY() + mContinueFABContainer.getMeasuredWidth() / 2;

		// get the final radius for the clipping circle
		int dx = Math.max(cx, mTransitionViewWhite.getWidth() - cx);
		int dy = Math.max(cy, mTransitionViewWhite.getHeight() - cy);
		float finalRadius = (float) Math.hypot(dx, dy);

		mTransitionViewBlue.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		mTransitionViewWhite.setLayerType(View.LAYER_TYPE_HARDWARE, null);

		final SupportAnimator whiteTransitionAnimation =
				ViewAnimationUtils.createCircularReveal(mTransitionViewWhite, cx, cy, 0, finalRadius);
		whiteTransitionAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
		whiteTransitionAnimation.setDuration(REVEAL_ANIMATION_DURATION);
		whiteTransitionAnimation.addListener(new SupportAnimator.AnimatorListener() {
			@Override
			public void onAnimationStart() {
				mTransitionViewWhite.bringToFront();
				mTransitionViewWhite.setVisibility(View.VISIBLE);
				mContinueFABContainer.setClickable(false);
				if (mContinueIcon.getVisibility() == View.VISIBLE) {
					hideContinueIconAnimations();
				}
			}

			@Override
			public void onAnimationEnd() {
				transitionAnimationWhite = whiteTransitionAnimation;
			}

			@Override
			public void onAnimationCancel() {

			}

			@Override
			public void onAnimationRepeat() {

			}
		});


		final SupportAnimator blueTransitionAnimation =
				ViewAnimationUtils.createCircularReveal(mTransitionViewBlue, cx, cy, 0, finalRadius);
		blueTransitionAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
		blueTransitionAnimation.setDuration(REVEAL_ANIMATION_DURATION);
		blueTransitionAnimation.addListener(new SupportAnimator.AnimatorListener() {
			@Override
			public void onAnimationStart() {
				mTransitionViewBlue.setVisibility(View.VISIBLE);
				mTransitionViewBlue.bringToFront();
				mContinueFABShadow.bringToFront();
				mContinueFAB.bringToFront();
			}

			@Override
			public void onAnimationEnd() {
				mTransitionViewBlue.setLayerType(View.LAYER_TYPE_NONE, null);
				mTransitionViewWhite.setLayerType(View.LAYER_TYPE_NONE, null);
				transitionAnimationBlue = blueTransitionAnimation;
			}

			@Override
			public void onAnimationCancel() {

			}

			@Override
			public void onAnimationRepeat() {

			}
		});

		whiteTransitionAnimation.start();
		blueTransitionAnimation.setStartDelay(REVEAL_ANIMATION_DELAY);
		blueTransitionAnimation.start();

		return blueTransitionAnimation;
	}

	private SupportAnimator showReverseTransitionAnimation() {
		mTransitionViewBlue.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		mTransitionViewWhite.setLayerType(View.LAYER_TYPE_HARDWARE, null);

		SupportAnimator blueReversed = transitionAnimationBlue.reverse();
		blueReversed.setInterpolator(new AccelerateDecelerateInterpolator());
		blueReversed.addListener(new SupportAnimator.AnimatorListener() {
			@Override
			public void onAnimationStart() {
				mTransitionViewBlue.setVisibility(View.VISIBLE);
				mTransitionViewBlue.bringToFront();
			}

			@Override
			public void onAnimationEnd() {
				Service.bringToBack(mTransitionViewBlue);
				mTransitionViewBlue.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationCancel() {

			}

			@Override
			public void onAnimationRepeat() {

			}
		});
		blueReversed.setDuration(REVEAL_ANIMATION_DURATION);
		blueReversed.start();

		final SupportAnimator whiteReversed = transitionAnimationWhite.reverse();
		whiteReversed.setInterpolator(new AccelerateDecelerateInterpolator());
		whiteReversed.addListener(new SupportAnimator.AnimatorListener() {
			@Override
			public void onAnimationStart() {
				mTransitionViewWhite.setVisibility(View.VISIBLE);
				mTransitionViewWhite.bringToFront();
			}

			@Override
			public void onAnimationEnd() {
				Service.bringToBack(mTransitionViewWhite);
				mTransitionViewBlue.setLayerType(View.LAYER_TYPE_NONE, null);
				mTransitionViewWhite.setLayerType(View.LAYER_TYPE_NONE, null);
				mTransitionViewWhite.setVisibility(View.INVISIBLE);
				mContinueFABContainer.setClickable(true);

				mContinueIcon.bringToFront();
				mContinueIcon.setVisibility(View.VISIBLE);
				showContinueIconAnimations(360);
			}

			@Override
			public void onAnimationCancel() {

			}

			@Override
			public void onAnimationRepeat() {

			}
		});
		whiteReversed.setDuration(REVEAL_ANIMATION_DURATION);
		whiteReversed.setStartDelay(REVEAL_ANIMATION_DELAY);
		whiteReversed.start();

		return whiteReversed;
	}

	private AnimationSet hideAllViews() {
		if(mContinueIcon.getVisibility() == View.VISIBLE) {
			hideContinueIconAnimations();
		}
		hideViewAnimation(mGearIcon, HIDE_VIEW_ANIMATION_DURATION);
		hideViewAnimation(mLoginTextLineOne, HIDE_VIEW_ANIMATION_DURATION);

		return hideViewAnimation(mSkipText, HIDE_VIEW_ANIMATION_DURATION);
	}

	private AnimationSet hideViewAnimation(final View view, final int DURATION) {
		view.setLayerType(View.LAYER_TYPE_HARDWARE, null);

		Animation mScaleAnimation = new ScaleAnimation(1, 0, 1, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		mScaleAnimation.setDuration(DURATION);
		mScaleAnimation.setInterpolator(new OvershootInterpolator(0.7f));

		Animation mAlphaAnimation = new AlphaAnimation(1f, 0f);
		mAlphaAnimation.setDuration(DURATION/2);
		mAlphaAnimation.setInterpolator(new DecelerateInterpolator());
		mAlphaAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				view.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});


		final AnimationSet mViewAnimations = new AnimationSet(false);
		mViewAnimations.setInterpolator(new AccelerateDecelerateInterpolator());
		mViewAnimations.setFillBefore(true);
		mViewAnimations.setFillAfter(true);
		mViewAnimations.addAnimation(mScaleAnimation);
		mViewAnimations.addAnimation(mAlphaAnimation);

		mViewAnimations.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				view.setLayerType(View.LAYER_TYPE_NONE, null);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});


		view.startAnimation(mViewAnimations);
		return mViewAnimations;
	}

	private AnimationSet showLogoAnimations() {
		mGearIcon.setLayerType(View.LAYER_TYPE_HARDWARE, null);

		Animation mScaleAnimation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		mScaleAnimation.setDuration(SHOW_LOGO_ANIMATION_DURATION);
		mScaleAnimation.setInterpolator(new OvershootInterpolator(0.7f));

		Animation mAlphaAnimation = new AlphaAnimation(0f, 1f);
		mAlphaAnimation.setDuration(SHOW_LOGO_ANIMATION_DURATION);
		mAlphaAnimation.setInterpolator(new DecelerateInterpolator());
		mAlphaAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				mGearIcon.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});

		final AnimationSet mLogoAnimations = new AnimationSet(false);
		mLogoAnimations.setInterpolator(new AccelerateDecelerateInterpolator());
		mLogoAnimations.setFillBefore(true);
		mLogoAnimations.setFillAfter(true);
		mLogoAnimations.addAnimation(mScaleAnimation);
		mLogoAnimations.addAnimation(mAlphaAnimation);

		mLogoAnimations.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mGearIcon.setLayerType(View.LAYER_TYPE_NONE, null);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});


		mGearIcon.startAnimation(mLogoAnimations);
		return mLogoAnimations;
	}

	private AnimationSet showContinueIconAnimations(int toDegree) {
		mContinueIcon.setVisibility(View.VISIBLE);
		Animation mScaleAnimation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		Animation mRotateAnimation = new RotateAnimation(
																toDegree - 360, toDegree,
																Animation.RELATIVE_TO_SELF, 0.5f,
																Animation.RELATIVE_TO_SELF, 0.5f
		);
		mRotateAnimation.setRepeatCount(0);
		mScaleAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				mContinueIcon.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});

		AnimationSet mAnimations = new AnimationSet(true);
		mAnimations.setDuration(SHOW_CONTINUE_ICON_DURATION);
		mAnimations.setFillAfter(true);
		mAnimations.setInterpolator(new OvershootInterpolator(1.5f));
		mAnimations.addAnimation(mScaleAnimation);
		mAnimations.addAnimation(mRotateAnimation);

		mContinueIcon.startAnimation(mAnimations);


		return mAnimations;
	}

	private AnimationSet hideContinueIconAnimations() {
		Animation mScaleAnimation = new ScaleAnimation(1, 0, 1, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		Animation mRotateAnimation = new RotateAnimation(
				mContinueIcon.getRotation(),  360 - mContinueIcon.getRotation(),
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f
		);
		mRotateAnimation.setRepeatCount(0);
		mRotateAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mContinueIcon.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
		AnimationSet mAnimations = new AnimationSet(true);
		mAnimations.setDuration(SHOW_CONTINUE_ICON_DURATION);
		mAnimations.setFillAfter(true);
		mAnimations.setInterpolator(new OvershootInterpolator(1.5f));
		mAnimations.addAnimation(mScaleAnimation);
		mAnimations.addAnimation(mRotateAnimation);

		mContinueIcon.startAnimation(mAnimations);

		return mAnimations;
	}

	private AnimationSet showTextLineAnimations(final TextView mTextLine, int lineNumber) {
		mTextLine.setLayerType(View.LAYER_TYPE_HARDWARE, null);

		int travelDistance = (lineNumber < 3)
									 ? (int) TypedValue.applyDimension(
																			  TypedValue.COMPLEX_UNIT_SP,
																			  getResources().getDimension(R.dimen.welcome_text_line_three_size),
																			  getResources().getDisplayMetrics())
									 : 0;

		float overshoot = (lineNumber == 1) ? 2f : 1f;
		final Animation mTranslationAnimation = new TranslateAnimation(0, 0, travelDistance, 0);
		mTranslationAnimation.setInterpolator(new OvershootInterpolator(overshoot));

		final Animation mAlphaAnimation = new AlphaAnimation(0f, 1f);
		mAlphaAnimation.setInterpolator(new DecelerateInterpolator());
		mAlphaAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				mTextLine.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mTextLine.setLayerType(View.LAYER_TYPE_NONE, null);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});

		final AnimationSet mWelcomeTextAnimations = new AnimationSet(false);
		mWelcomeTextAnimations.setDuration(SHOW_TEXT_ANIMATION_DURATION);
		mWelcomeTextAnimations.setInterpolator(new AccelerateDecelerateInterpolator());
		mWelcomeTextAnimations.setFillBefore(true);
		mWelcomeTextAnimations.setFillAfter(true);
		mWelcomeTextAnimations.addAnimation(mAlphaAnimation);
		mWelcomeTextAnimations.addAnimation(mTranslationAnimation);

		int delay = (lineNumber < 3)
							? SHOW_TEXT_ANIMATION_BASE_DELAY * lineNumber
							: SHOW_TEXT_ANIMATION_BASE_DELAY * (lineNumber * 2);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mTextLine.startAnimation(mWelcomeTextAnimations);

			}
		}, delay + SHOW_TEXT_ANIMATION_DELAY);

		return mWelcomeTextAnimations;
	}

	private Animation expandContinueButtonAnimations() {
		Animation resizeAnimation = new ResizeWidthAnimation(mContinueFABContainer, (int) getResources().getDimension(R.dimen.notification_customize_width));
		resizeAnimation.setDuration(EXPAND_DURATION);
		mContinueFABContainer.startAnimation(resizeAnimation);

		final Animation mAlphaAnimation = new AlphaAnimation(0f, 1f);
		mAlphaAnimation.setDuration(SHOW_CUSTOMIZE_TEXT_DURATION);
		mAlphaAnimation.setInterpolator(new DecelerateInterpolator());
		mAlphaAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				mCustomizeText.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mCustomizeText.startAnimation(mAlphaAnimation);
			}
		}, SHOW_CUSTOMIZE_TEXT_DELAY);

		return resizeAnimation;
	}

	private Animation collapseContinueButtonAnimations() {
		Animation resizeAnimation = new ResizeWidthAnimation(mContinueFABContainer, mFABWidth);
		resizeAnimation.setDuration(EXPAND_DURATION);
		resizeAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
		mContinueFABContainer.startAnimation(resizeAnimation);

		final Animation mAlphaAnimation = new AlphaAnimation(1f, 0f);
		mAlphaAnimation.setDuration(SHOW_CUSTOMIZE_TEXT_DURATION);
		mAlphaAnimation.setInterpolator(new DecelerateInterpolator());
		mAlphaAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mCustomizeText.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});

		if(mCustomizeText.getVisibility() == View.VISIBLE) {
			mCustomizeText.startAnimation(mAlphaAnimation);
		}

		return resizeAnimation;
	}
}
