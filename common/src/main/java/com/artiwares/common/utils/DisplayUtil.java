package com.artiwares.common.utils;

import android.content.res.Resources;
import android.util.TypedValue;

public class DisplayUtil {

    public static int dp2px(float dp, Resources resources) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }


}
