package io.guaong.gesturemusic.util;

import android.content.Context;
import android.content.SharedPreferences;

import io.guaong.gesturemusic.config.StringConfig;

import static android.content.Context.MODE_PRIVATE;

public class SharedPreferencesUtil {

    public static boolean writeColorParameter(Context context, String name, String key, int value){
        final SharedPreferences sharedPreferences = context.getSharedPreferences(name, MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    public static int readColorParameter(Context context, String key){
        final SharedPreferences sharedPreferences = context.getSharedPreferences(StringConfig.COLOR, MODE_PRIVATE);
        return sharedPreferences.getInt(key, -1);
    }

    public static boolean readFullScreenParameter(Context context, String key){
        final SharedPreferences sharedPreferences = context.getSharedPreferences(StringConfig.FULL_SCREEN, MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, false);
    }

    public static boolean writeFullScreenParameter(Context context, String name, String key, boolean value){
        final SharedPreferences sharedPreferences = context.getSharedPreferences(name, MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    public static boolean cancelColorParameter(Context context, String name, String key){
        final SharedPreferences sharedPreferences = context.getSharedPreferences(name, MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        return editor.commit();
    }

}
