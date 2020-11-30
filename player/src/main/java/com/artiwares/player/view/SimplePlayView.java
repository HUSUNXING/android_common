package com.artiwares.player.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.artiwares.player.AbstractPlayer;
import com.artiwares.player.GestureVideoController;


/**
 * 播放器视图层
 */
public class SimplePlayView extends BasePlayView {

    public static final int SCREEN_SCALE_DEFAULT = 0;
    public static final int SCREEN_SCALE_16_9 = 1;
    public static final int SCREEN_SCALE_4_3 = 2;
    public static final int SCREEN_SCALE_MATCH_PARENT = 3;
    public static final int SCREEN_SCALE_ORIGINAL = 4;
    public static final int SCREEN_SCALE_CENTER_CROP = 5;
    protected static final int FULLSCREEN_FLAGS = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    protected ResizeSurfaceView mSurfaceView;
    protected ResizeTextureView mTextureView;
    protected SurfaceTexture mSurfaceTexture;
    protected FrameLayout mPlayerContainer;
    //通过添加和移除这个view来实现隐藏和显示系统navigation bar，可以避免出现一些奇奇怪怪的问题
    protected View mHideNavBarView;
    protected int mCurrentScreenScale = SCREEN_SCALE_DEFAULT;
    protected int[] mVideoSize = {0, 0};
    private GestureVideoController mVideoController;

    public SimplePlayView(@NonNull Context context) {
        this(context, null);
    }

    public SimplePlayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimplePlayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    /**
     * 初始化播放器视图
     */
    protected void initView() {
        mPlayerContainer = new FrameLayout(getContext());
        mPlayerContainer.setBackgroundColor(Color.parseColor("#D8D8D8"));
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mPlayerContainer, params);

        mHideNavBarView = new View(getContext());
        mHideNavBarView.setSystemUiVisibility(FULLSCREEN_FLAGS);
    }

    /**
     * 创建播放器实例，设置播放器参数，并且添加用于显示视频的View
     */

    protected void initPlayer() {
        super.initPlayer();
        addDisplay();
    }

    @Override
    protected void startPrepare(boolean needReset) {
        if (TextUtils.isEmpty(mCurrentUrl) && mAssetFileDescriptor == null) return;
        if (needReset) mMediaPlayer.reset();
        if (mAssetFileDescriptor != null) {
            mMediaPlayer.setDataSource(mAssetFileDescriptor);
        } else {
            mMediaPlayer.setDataSource(mCurrentUrl, mHeaders);
        }
        mMediaPlayer.prepareAsync();
        setPlayState(STATE_PREPARING);
       // setPlayerState(PLAYER_FULL_SCREEN);
    }

    protected void addDisplay() {
        if (mUsingSurfaceView) {
            addSurfaceView();
        } else {
            addTextureView();
        }
    }

    /**
     * @param mediaController 手势控制音量亮度等
     */
    public void setVideoController(@Nullable GestureVideoController mediaController) {
        mPlayerContainer.removeView(mVideoController);
        mVideoController = mediaController;
        if (mediaController != null) {
            LayoutParams params = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            mPlayerContainer.addView(mVideoController, params);
        }
    }

    /**
     * 添加SurfaceView
     */
    private void addSurfaceView() {
        mPlayerContainer.removeView(mSurfaceView);
        mSurfaceView = new ResizeSurfaceView(getContext());
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (mMediaPlayer != null) {
                    mMediaPlayer.setDisplay(holder);
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
        surfaceHolder.setFormat(PixelFormat.RGBA_8888);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        mPlayerContainer.addView(mSurfaceView, 0, params);
    }

    /**
     * 添加TextureView
     */
    private void addTextureView() {
        mPlayerContainer.removeView(mTextureView);
        mSurfaceTexture = null;
        mTextureView = new ResizeTextureView(getContext());
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                if (mSurfaceTexture != null) {
                    mTextureView.setSurfaceTexture(mSurfaceTexture);
                } else {
                    mSurfaceTexture = surfaceTexture;
                    mMediaPlayer.setSurface(new Surface(surfaceTexture));
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return mSurfaceTexture == null;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            }
        });
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        mPlayerContainer.addView(mTextureView, 0, params);
    }

    @Override
    public void release() {
        super.release();
        mPlayerContainer.removeView(mTextureView);
        mPlayerContainer.removeView(mSurfaceView);
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        mCurrentScreenScale = SCREEN_SCALE_DEFAULT;
    }

    @Override
    public void onInfo(int what, int extra) {
        super.onInfo(what, extra);
        if (what == AbstractPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED) {
            if (mTextureView != null)
                mTextureView.setRotation(extra);
        }
    }

    /**
     * 重新播放
     *
     * @param resetPosition 是否从头开始播放
     */
    @Override
    public void replay(boolean resetPosition) {
        if (resetPosition) {
            mCurrentPosition = 0;
        }
        addDisplay();
        mNeedPause = true;
        startPrepare(true);
    }

    /**
     * 设置视频比例
     */
    @Override
    public void setScreenScale(int screenScale) {
        this.mCurrentScreenScale = screenScale;
        if (mSurfaceView != null) {
            mSurfaceView.setScreenScale(screenScale);
        } else if (mTextureView != null) {
            mTextureView.setScreenScale(screenScale);
        }
    }

    /**
     * 设置镜像旋转，暂不支持SurfaceView
     */
    @Override
    public void setMirrorRotation(boolean enable) {
        if (mTextureView != null) {
            mTextureView.setScaleX(enable ? -1 : 1);
        }
    }

    /**
     * 截图，暂不支持SurfaceView
     */
    @Override
    public Bitmap doScreenShot() {
        if (mTextureView != null) {
            return mTextureView.getBitmap();
        }
        return null;
    }

    /**
     * 获取视频宽高,其中width: mVideoSize[0], height: mVideoSize[1]
     */
    @Override
    public int[] getVideoSize() {
        return mVideoSize;
    }

    /**
     * 旋转视频画面
     *
     * @param rotation 角度
     */
    @Override
    public void setRotation(float rotation) {
        if (mTextureView != null) {
            mTextureView.setRotation(rotation);
            mTextureView.requestLayout();
        }

        if (mSurfaceView != null) {
            mSurfaceView.setRotation(rotation);
            mSurfaceView.requestLayout();
        }
    }

}
