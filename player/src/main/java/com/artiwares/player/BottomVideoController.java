package com.artiwares.player;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BottomVideoController extends FrameLayout {

    public BottomVideoController(@NonNull Context context) {
        this(context, null);
    }

    public BottomVideoController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomVideoController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
      //  View view = LayoutInflater.from(context).inflate(R.layout.bottom_video_contrller_view, this, true);

    }
}
