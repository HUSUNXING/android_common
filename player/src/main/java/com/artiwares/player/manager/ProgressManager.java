package com.artiwares.player.manager;

public class ProgressManager {

    private static ProgressManager sInstance;
    private long mProgress;
    private String url = "";
    private ProgressManager() {
    }

    public static ProgressManager instance() {
        if (sInstance == null) {
            synchronized (ProgressManager.class) {
                if (sInstance == null) {
                    sInstance = new ProgressManager();
                }
            }
        }
        return sInstance;
    }

    public void saveProgress(long progress) {
        ProgressManager.instance().mProgress = progress;
    }

    public long getSavedProgress() {
        return mProgress;
    }

    //如果是ListView 播放器保存进度 带入url
    public void saveProgress(int url, long progress) {
        ProgressManager.instance().mProgress = progress;
    }

    public long getSavedProgress(String url) {
        if(ProgressManager.instance().url.equals(url)){
            return mProgress;
        }
        return 0;
    }
}
