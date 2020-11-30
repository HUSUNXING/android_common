package com.artiwares.player.manager;

import android.graphics.Bitmap;

public interface MediaPlayerControl {

    /**
     * 开始播放
     */
    void start();

    /**
     * 暂停播放
     */
    void pause();

    /**
     * 获取视频总时长
     */
    long getDuration();

    /**
     * 获取当前播放的位置
     */
    long getCurrentPosition();

    /**
     * 调整播放进度
     */
    void seekTo(long pos);

    /**
     * 是否正在播放
     */
    boolean isPlaying();

    /**
     * 开启缓存后，返回是缓存的进度
     */
    int getBufferedPercentage();

    boolean isMute();

    /**
     * 是否静音
     */
    void setMute(boolean isMute);

    /**
     * controller是否处于锁定状态
     */
    void setLock(boolean isLocked);

    /**
     * 设置视频比例
     */
    void setScreenScale(int screenScale);

    /**
     * 设置播放速度
     */
    void setSpeed(float speed);

    /**
     * 获取缓冲速度
     */
    long getTcpSpeed();

    /**
     * 重新播放
     *
     * @param resetPosition 是否从头开始播放
     */
    void replay(boolean resetPosition);

    /**
     *
     * @param enable 是否开启旋转
     */
    void setMirrorRotation(boolean enable);

    /**
     * 截图，暂不支持SurfaceView
     */
    Bitmap doScreenShot();

    /**
     * 获取视频宽高,其中width: mVideoSize[0], height: mVideoSize[1]
     */
    int[] getVideoSize();
}