package io.guaong.gesturemusic.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.guaong.gesturemusic.model.Music;

/**
 * Created by 关桐 on 2018/6/22.
 */
public class MusicUtil {

    public static ArrayList<Music> getMusicList(Context context) {
        final ArrayList<Music> musicList = new ArrayList<>();
        final ContentResolver contentResolver = context.getContentResolver();
        final Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor != null && cursor.moveToFirst()) {
            String title;
            String artist;
            long size;
            String uri;
            int id;
            long duration;
            String time;
            while (!cursor.isAfterLast()) {
                title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                int flag = cutIndex(title);
                if (flag != -1) {
                    artist = title.substring(0, flag);
                    title = title.substring(flag + 2, title.length());
                }
                size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                uri = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                String s = (int) ((duration / 1000) % 60) + "";
                if ((int) ((duration / 1000) % 60) < 10) {
                    s = 0 + s;
                }
                time = (int) (duration / 60000) + ":" + s;
                if (isLegal(title, artist, size, duration)) {
                    Music music = new Music();
                    music.setTitle(title);
                    music.setArtist(artist);
                    music.setUri(uri);
                    music.setDuration(duration);
                    music.setId(id);
                    music.setSize(size);
                    music.setTime(time);
                    musicList.add(music);
                }
                cursor.moveToNext();


            }
            cursor.close();
        }
        return musicList;
    }

    public static boolean haveMusic(List<Music> musicList) {
        return musicList != null;
    }

    private static int cutIndex(String str) {
        return str.indexOf("-");
    }

    private static boolean isLegal(String title, String artist, long size, long duration) {
        final boolean isLegal;
        isLegal = (!"".contains(title))
                && (size > 1024)
                && (!"".contains(artist))
                && (duration > 120000);
        return isLegal;
    }

//    private static boolean isCode(String strName) {
//        Pattern p = Pattern.compile("\\s*|t*|r*|n*");
//        Matcher m = p.matcher(strName);
//        String after = m.replaceAll("");
//        String temp = after.replaceAll("\\p{P}", "");
//        char[] ch = temp.trim().toCharArray();
//        float chLength = ch.length;
//        float count = 0;
//        for (char c : ch) {
//            if (!Character.isLetterOrDigit(c)) {
//                if (!isChinese(c)) {
//                    count = count + 1;
//                }
//            }
//        }
//        float result = count / chLength;
//        return result <= 0.4;
//    }

//    private static boolean isChinese(char c) {
//        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
//        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
//                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
//                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
//                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
//                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
//                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
//    }

}
