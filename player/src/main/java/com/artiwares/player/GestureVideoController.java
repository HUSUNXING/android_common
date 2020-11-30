package com.artiwares.player;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;


import com.artiwares.player.view.CenterView;
import com.artiwares.player.view.SimplePlayView;

import java.util.Formatter;
import java.util.Locale;


public class GestureVideoController extends FrameLayout implements
        GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, View.OnTouchListener {

    protected GestureDetector mGestureDetector;
    protected AudioManager mAudioManager;
    protected int mStreamVolume;
    protected float mBrightness;
    protected boolean mNeedSeek = false;
    protected boolean mFirstTouch;
    protected boolean mChangePosition;
    protected boolean mChangeBrightness;
    protected boolean mChangeVolume;

    private GestureListener mGestureListener;
    protected SimplePlayView mMediaPlayer;
    private CenterView mCenterView;
    private Formatter mFormatter;
    private StringBuilder mFormatBuilder;
    private long mPosition;

    public void setMediaPlayer(SimplePlayView mMediaPlayer) {
        this.mMediaPlayer = mMediaPlayer;
    }

    public GestureVideoController(Context context) {
        this(context, null);
    }

    public GestureVideoController(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureVideoController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    protected void initView() {
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        setClickable(true);
        setFocusable(true);

        mCenterView = new CenterView(getContext());
        mCenterView.setVisibility(GONE);
        addView(mCenterView);

        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mGestureDetector = new GestureDetector(getContext(), this);
        setOnTouchListener(this);
    }

    public void GestureListener(GestureListener gestureListener) {
        this.mGestureListener = gestureListener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        if (isEdge(getContext(), e)) return false;
        mStreamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Activity activity = scanForActivity(getContext());
        if (activity == null) {
            mBrightness = 0;
        } else {
            mBrightness = activity.getWindow().getAttributes().screenBrightness;
        }
        mFirstTouch = true;
        mChangePosition = false;
        mChangeBrightness = false;
        mChangeVolume = false;
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (mGestureListener != null) {
            mGestureListener.centerViewOnClick();
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (isEdge(getContext(), e1))
            return false;
        float deltaX = e1.getX() - e2.getX();
        float deltaY = e1.getY() - e2.getY();
        if (mFirstTouch) {
            mChangePosition = Math.abs(distanceX) >= Math.abs(distanceY);
            if (!mChangePosition) { //半屏宽度
                int halfScreen = getScreenWidth(getContext()) / 2;
                if (e2.getX() > halfScreen) {
                    mChangeVolume = true;
                } else {
                    mChangeBrightness = true;
                }
                onStartSlide();
            }
            mFirstTouch = false;
        }
       /* if (mChangePosition) {
            slideToChangePosition(deltaX);
        } else*/
        if (mChangeBrightness) {
            slideToChangeBrightness(deltaY);
        } else if (mChangeVolume) {
            slideToChangeVolume(deltaY);
        }
        return true;
    }


    @Override
    public boolean onDoubleTap(MotionEvent e) {
        //if (!mIsLocked) doPauseResume();
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean detectedUp = event.getAction() == MotionEvent.ACTION_UP;
        if (!mGestureDetector.onTouchEvent(event) && detectedUp) {
            onStopSlide();
            if (mNeedSeek) {
                mMediaPlayer.seekTo(mPosition);
                mNeedSeek = false;
            }
        }
        return super.onTouchEvent(event);
    }

    private void onStopSlide() {
        if (mCenterView.getVisibility() == VISIBLE) {
            mCenterView.setVisibility(GONE);
        }
    }

    private void onStartSlide() {
        hide();
        mCenterView.setVisibility(VISIBLE);
    }

    private void hide() {

    }

    private void onBrightnessChange(int percent) {
        mCenterView.setProVisibility(View.VISIBLE);
        mCenterView.setIcon(R.drawable.dkplayer_ic_action_brightness);
        mCenterView.setTextView(percent + "%");
        mCenterView.setProPercent(percent);
    }

    protected void slideToChangePosition(float deltaX) {
        deltaX = -deltaX;
        int width = getMeasuredWidth();
        int duration = (int) mMediaPlayer.getDuration();
        int currentPosition = (int) mMediaPlayer.getCurrentPosition();
        int position = (int) (deltaX / width * 600000 + currentPosition);
        if (position > duration) position = duration;
        if (position < 0) position = 0;
        onPositionChange(position, currentPosition, duration);
        mPosition = position;
        mNeedSeek = true;
    }

    public void onPositionChange(int slidePosition, int currentPosition, int duration) {
        mCenterView.setProVisibility(View.GONE);
        if (slidePosition > currentPosition) {
            mCenterView.setIcon(R.drawable.dkplayer_ic_action_fast_forward);
        } else {
            mCenterView.setIcon(R.drawable.dkplayer_ic_action_fast_rewind);
        }
        mCenterView.setTextView(stringForTime(slidePosition) + "/" + stringForTime(duration));
    }

    protected void slideToChangeBrightness(float deltaY) {
        Activity activity = scanForActivity(getContext());
        if (activity == null) return;
        Window window = activity.getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        int height = getMeasuredHeight();
        if (mBrightness == -1.0f) mBrightness = 0.5f;
        float brightness = deltaY * 2 / height * 1.0f + mBrightness;
        if (brightness < 0) {
            brightness = 0f;
        }
        if (brightness > 1.0f) brightness = 1.0f;
        int percent = (int) (brightness * 100);
        attributes.screenBrightness = brightness;
        window.setAttributes(attributes);
        onBrightnessChange(percent);
    }

    protected void slideToChangeVolume(float deltaY) {
        int streamMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int height = getMeasuredHeight();
        float deltaV = deltaY * 2 / height * streamMaxVolume;
        float index = mStreamVolume + deltaV;
        if (index > streamMaxVolume) index = streamMaxVolume;
        if (index < 0) index = 0;
        int percent = (int) (index / streamMaxVolume * 100);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) index, 0);
        onVolumeChange(percent);
    }

    private void onVolumeChange(int percent) {
        mCenterView.setProVisibility(View.VISIBLE);
        if (percent <= 0) {
            mCenterView.setIcon(R.drawable.dkplayer_ic_action_volume_off);
        } else {
            mCenterView.setIcon(R.drawable.dkplayer_ic_action_volume_up);
        }
        mCenterView.setTextView(percent + "%");
        mCenterView.setProPercent(percent);
    }

    protected String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static boolean isEdge(Context context, MotionEvent e) {
        float edgeSize = dip2px(context, 40);
        return e.getRawX() < edgeSize
                || e.getRawX() > getScreenWidth(context) - edgeSize
                || e.getRawY() < edgeSize
                || e.getRawY() > getScreenHeight(context) - edgeSize;
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    public static Activity scanForActivity(Context context) {
        return context == null ? null : (context instanceof Activity ? (Activity) context : (context instanceof ContextWrapper ? scanForActivity(((ContextWrapper) context).getBaseContext()) : null));
    }

    public interface GestureListener {

        void centerViewOnClick();
    }
}