package com.artiwares.player.view;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.artiwares.player.AbstractPlayer;
import com.artiwares.player.R;
import com.artiwares.player.listener.OnVideoViewStateChangeListener;
import com.artiwares.player.listener.PlayerEventListener;
import com.artiwares.player.manager.ExoPlayerFactory;
import com.artiwares.player.manager.MediaPlayerControl;
import com.artiwares.player.manager.ProgressManager;

import java.util.Map;


public abstract class BasePlayView extends FrameLayout implements MediaPlayerControl, PlayerEventListener {

    public static final int STATE_ERROR = -1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_PREPARING = 1;
    public static final int STATE_PREPARED = 2;
    public static final int STATE_PLAYING = 3;
    public static final int STATE_PAUSED = 4;
    public static final int STATE_PLAYBACK_COMPLETED = 5;
    public static final int STATE_BUFFERING = 6;
    public static final int STATE_BUFFERED = 7;
   // public static final int PLAYER_NORMAL = 10;        // 普通播放器
    public static final int PLAYER_FULL_SCREEN = 11;   // 全屏播放器
    protected AbstractPlayer mMediaPlayer;//播放器
    protected boolean mIsMute;//是否静音
    protected String mCurrentUrl;//当前播放视频的地址
    protected Map<String, String> mHeaders;//当前视频地址的请求头
    protected AssetFileDescriptor mAssetFileDescriptor;//assets文件
    protected long mCurrentPosition;//当前正在播放视频的位置
    protected int mCurrentPlayState = STATE_IDLE;//当前播放器的状态
    protected int mCurrentPlayerState = PLAYER_FULL_SCREEN;

    protected AudioManager mAudioManager;//系统音频管理器
    @Nullable
    protected AudioFocusHelper mAudioFocusHelper;
    protected boolean mIsLockFullScreen;//是否锁定屏幕
    protected OnVideoViewStateChangeListener mOnVideoViewStateChangeListeners;
    @Nullable
    protected ProgressManager mProgressManager = ProgressManager.instance();
    protected boolean mAutoRotate;
    protected boolean mUsingSurfaceView;
    protected boolean mIsLooping;
    protected boolean mEnableAudioFocus;
    protected boolean mEnableMediaCodec;
    public boolean mNeedPause = false;
    protected String mErrorMessage;

    public BasePlayView(@NonNull Context context) {
        this(context, null);
    }

    public BasePlayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BasePlayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BasePlayView);
        mAutoRotate = typedArray.getBoolean(R.styleable.BasePlayView_autoRotate, false);
        mUsingSurfaceView = typedArray.getBoolean(R.styleable.BasePlayView_usingSurfaceView, false);
        mIsLooping = typedArray.getBoolean(R.styleable.BasePlayView_looping, false);
        mEnableAudioFocus = typedArray.getBoolean(R.styleable.BasePlayView_enableAudioFocus, true);
        mEnableMediaCodec = typedArray.getBoolean(R.styleable.BasePlayView_enableMediaCodec, false);
        typedArray.recycle();
    }

    /**
     * 初始化播放器
     */
    protected void initPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = ExoPlayerFactory.create().createPlayer(getContext());
        }
        mMediaPlayer.bindVideoView(this);
        mMediaPlayer.setEnableMediaCodec(mEnableMediaCodec);
        mMediaPlayer.setLooping(mIsLooping);
        mMediaPlayer.setVolume(0.0f, 0.0f);
    }

    /**
     * 向Controller设置播放状态，用于控制Controller的ui展示
     */
    protected void setPlayState(int playState) {
        mCurrentPlayState = playState;

        if (mOnVideoViewStateChangeListeners != null) {
            mOnVideoViewStateChangeListeners.onPlayStateChanged(playState);
        }
    }

    /**
     * 向Controller设置播放器状态，包含全屏状态和非全屏状态
     */
    protected void setPlayerState(int playerState) {
        mCurrentPlayerState = playerState;
    }

    /**
     * 开始准备播放（直接播放）
     */
    protected abstract void startPrepare(boolean needReset);

    /**
     * 开始播放
     */
    @Override
    public void start() {
        if (mCurrentPlayState == STATE_IDLE) {
            startPlay(false);
        } else if (isInPlaybackState()) {
            startInPlaybackState();
        }
        setKeepScreenOn(true);
        if (mAudioFocusHelper != null)
            mAudioFocusHelper.requestFocus();
    }

    /**
     * 第一次播放
     */
    public void startPlay(boolean needReset) {
        if (mEnableAudioFocus) {
            mAudioManager = (AudioManager) getContext().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            mAudioFocusHelper = new AudioFocusHelper();
        }

        mCurrentPosition = 0;
        initPlayer();
        startPrepare(needReset);
    }

    /**
     * 播放状态下开始播放
     */
    protected void startInPlaybackState() {
        mMediaPlayer.start();
        setPlayState(STATE_PLAYING);
    }

    /**
     * 暂停播放
     */
    @Override
    public void pause() {
        if (isPlaying()) {
            mMediaPlayer.pause();
            setPlayState(STATE_PAUSED);
            setKeepScreenOn(false);
            if (mAudioFocusHelper != null)
                mAudioFocusHelper.abandonFocus();
        }
    }

    /**
     * 继续播放
     */
    public void resume() {
        if (isInPlaybackState()
                && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
            setPlayState(STATE_PLAYING);
            if (mAudioFocusHelper != null)
                mAudioFocusHelper.requestFocus();
            setKeepScreenOn(true);
        }
    }

    /**
     * 停止播放
     */
    public void stopPlayback() {
        if (mProgressManager != null && isInPlaybackState())
            mProgressManager.saveProgress(mCurrentPosition);
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            setPlayState(STATE_IDLE);
            if (mAudioFocusHelper != null)
                mAudioFocusHelper.abandonFocus();
            setKeepScreenOn(false);
        }
        onPlayStopped();
    }

    /**
     * 释放播放器
     */
    public void release() {
        if (mProgressManager != null && isInPlaybackState())
            mProgressManager.saveProgress(mCurrentPosition);
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
            setPlayState(STATE_IDLE);
            if (mAudioFocusHelper != null)
                mAudioFocusHelper.abandonFocus();
            setKeepScreenOn(false);
        }
        onPlayStopped();
    }

    private void onPlayStopped() {
        mIsLockFullScreen = false;
        mCurrentPosition = 0;
    }

    /**
     * 监听播放状态变化
     */
    public void addOnVideoViewStateChangeListener(@NonNull OnVideoViewStateChangeListener listener) {
        mOnVideoViewStateChangeListeners = listener;
    }

    /**
     * 移除播放状态监听
     */
    public void removeOnVideoViewStateChangeListener() {
        if (mOnVideoViewStateChangeListeners != null) {
            mOnVideoViewStateChangeListeners = null;
        }
    }

    /**
     * 是否处于播放状态
     */
    protected boolean isInPlaybackState() {
        return (mMediaPlayer != null
                && mCurrentPlayState != STATE_ERROR
                && mCurrentPlayState != STATE_IDLE
                && mCurrentPlayState != STATE_PREPARING
                && mCurrentPlayState != STATE_PLAYBACK_COMPLETED);
    }

    /**
     * 获取视频总时长
     */
    @Override
    public long getDuration() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    /**
     * 获取当前播放的位置
     */
    @Override
    public long getCurrentPosition() {
        if (isInPlaybackState()) {
            mCurrentPosition = mMediaPlayer.getCurrentPosition();
            return mCurrentPosition;
        }
        return 0;
    }

    /**
     * 调整播放进度
     */
    @Override
    public void seekTo(long pos) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(pos);
        }
    }

    /**
     * 是否处于播放状态
     */
    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    /**
     * 获取当前缓冲百分比
     */
    @Override
    public int getBufferedPercentage() {
        return mMediaPlayer != null ? mMediaPlayer.getBufferedPercentage() : 0;
    }

    /**
     * 是否处于静音状态
     */
    @Override
    public boolean isMute() {
        return mIsMute;
    }

    /**
     * 设置静音
     */
    @Override
    public void setMute(boolean isMute) {
        if (mMediaPlayer != null) {
            this.mIsMute = isMute;
            float volume = isMute ? 0.0f : 1.0f;
            mMediaPlayer.setVolume(volume, volume);
        }
    }

    /**
     * 设置controller是否处于锁定状态
     */
    @Override
    public void setLock(boolean isLocked) {
        this.mIsLockFullScreen = isLocked;
    }

    /**
     * 视频播放出错回调
     */
    @Override
    public void onError(String error) {
        setPlayState(STATE_ERROR);
        mErrorMessage = error;
    }

    public String getErrorMessage() {
        if (TextUtils.isEmpty(mErrorMessage)) {
            return "unknown error";
        }
        return mErrorMessage;
    }

    /**
     * 视频播放完成回调
     */
    @Override
    public void onCompletion() {
        setPlayState(STATE_PLAYBACK_COMPLETED);
        setKeepScreenOn(false);
        mCurrentPosition = 0;
    }

    @Override
    public void onInfo(int what, int extra) {
        switch (what) {
            case AbstractPlayer.MEDIA_INFO_BUFFERING_START:
                setPlayState(STATE_BUFFERING);
                break;
            case AbstractPlayer.MEDIA_INFO_BUFFERING_END:
                setPlayState(STATE_BUFFERED);
                break;
            case AbstractPlayer.MEDIA_INFO_VIDEO_RENDERING_START: // 视频开始渲染
                setPlayState(STATE_PLAYING);
                if (getVisibility() != VISIBLE) pause();
                if (mNeedPause) {
                    pause();
                    mNeedPause = false;
                }
                break;
        }
    }

    /**
     * 视频缓冲完毕，准备开始播放时回调
     */
    @Override
    public void onPrepared() {
        setPlayState(STATE_PREPARED);
        if (mCurrentPosition > 0) {
            seekTo(mCurrentPosition);
        }
    }

    /**
     * 获取当前播放器的状态
     */
    public int getCurrentPlayerState() {
        return mCurrentPlayerState;
    }

    /**
     * 获取当前的播放状态
     */
    public int getCurrentPlayState() {
        return mCurrentPlayState;
    }


    /**
     * 获取缓冲速度
     */
    @Override
    public long getTcpSpeed() {
        return mMediaPlayer.getTcpSpeed();
    }

    /**
     * 设置播放速度
     */
    @Override
    public void setSpeed(float speed) {
        if (isInPlaybackState()) {
            mMediaPlayer.setSpeed(speed);
        }
    }

    /**
     * 设置视频地址
     */
    public void setUrl(String url) {
        this.mCurrentUrl = url;
    }

    /**
     * 设置包含请求头信息的视频地址
     *
     * @param url     视频地址
     * @param headers 请求头
     */
    public void setUrl(String url, Map<String, String> headers) {
        mCurrentUrl = url;
        mHeaders = headers;
    }

    /**
     * 用于播放assets里面的视频文件
     */
    public void setAssetFileDescriptor(AssetFileDescriptor fd) {
        this.mAssetFileDescriptor = fd;
    }

    /**
     * 一开始播放就seek到预先设置好的位置
     */
    public void skipPositionWhenPlay(int position) {
        this.mCurrentPosition = position;
    }

    /**
     * 设置音量 0.0f-1.0f 之间
     *
     * @param v1 左声道音量
     * @param v2 右声道音量
     */
    public void setVolume(float v1, float v2) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(v1, v2);
        }
    }

    /**
     * 设置进度管理器，用于保存播放进度
     */
    public void setProgressManager(@Nullable ProgressManager progressManager) {
        this.mProgressManager = progressManager;
    }

    /**
     * 循环播放， 默认不循环播放
     */
    public void setLooping(boolean looping) {
        mIsLooping = looping;
        if (mMediaPlayer != null) {
            mMediaPlayer.setLooping(looping);
        }
    }

    /**
     * 是否自动旋转， 默认不自动旋转
     */
    public void setAutoRotate(boolean autoRotate) {
        mAutoRotate = autoRotate;
    }

    /**
     * 是否启用SurfaceView，默认不启用
     */
    public void setUsingSurfaceView(boolean usingSurfaceView) {
        mUsingSurfaceView = usingSurfaceView;
    }

    /**
     * 是否开启AudioFocus监听， 默认开启
     */
    public void setEnableAudioFocus(boolean enableAudioFocus) {
        mEnableAudioFocus = enableAudioFocus;
    }

    /**
     * 是否使用MediaCodec进行解码（硬解码），默认不开启，使用软解
     */
    public void setEnableMediaCodec(boolean enableMediaCodec) {
        mEnableMediaCodec = enableMediaCodec;
    }

    /**
     * 音频焦点改变监听
     */
    private class AudioFocusHelper implements AudioManager.OnAudioFocusChangeListener {
        private boolean startRequested = false;
        private boolean pausedForLoss = false;
        private int currentFocus = 0;

        @Override
        public void onAudioFocusChange(int focusChange) {
            if (currentFocus == focusChange) {
                return;
            }

            currentFocus = focusChange;
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN://获得焦点
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT://暂时获得焦点
                    if (startRequested || pausedForLoss) {
                        start();
                        startRequested = false;
                        pausedForLoss = false;
                    }
                    if (mMediaPlayer != null && !mIsMute)//恢复音量
                        mMediaPlayer.setVolume(1.0f, 1.0f);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS://焦点丢失
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT://焦点暂时丢失
                    if (isPlaying()) {
                        pausedForLoss = true;
                        pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK://此时需降低音量
                    if (mMediaPlayer != null && isPlaying() && !mIsMute) {
                        mMediaPlayer.setVolume(0.1f, 0.1f);
                    }
                    break;
            }
        }

        /**
         * Requests to obtain the audio focus
         */
        void requestFocus() {
            if (currentFocus == AudioManager.AUDIOFOCUS_GAIN) {
                return;
            }

            if (mAudioManager == null) {
                return;
            }

            int status = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == status) {
                currentFocus = AudioManager.AUDIOFOCUS_GAIN;
                return;
            }

            startRequested = true;
        }

        /**
         * Requests the system to drop the audio focus
         */
        void abandonFocus() {

            if (mAudioManager == null) {
                return;
            }

            startRequested = false;
            mAudioManager.abandonAudioFocus(this);
        }
    }

}
