package io.guaong.gesturemusic.util;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by 关桐 on 2018/6/22.
 */

public class WindowUtil {

    public static int dipToPx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static float pxToSp(Context context, float pxValue){
        return (pxValue / context.getResources().getDisplayMetrics().scaledDensity);
    }

    public static int getWindowWidth(Context context){
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    public static int getWindowHeight(Context context){
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }

}
