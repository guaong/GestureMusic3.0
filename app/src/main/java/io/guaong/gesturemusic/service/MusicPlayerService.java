package io.guaong.gesturemusic.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Timer;

import io.guaong.gesturemusic.MusicPlayerBinder;
import io.guaong.gesturemusic.TimingHandler;
import io.guaong.gesturemusic.TimingTask;
import io.guaong.gesturemusic.model.Music;

public class MusicPlayerService extends Service {

    private MusicPlayerBinder mPlayerBinder;

    private MediaPlayer mMediaPlayer;

    public static boolean isTiming = false;

    private Timer mTimingTimer = new Timer();

    private Timer mAnimationTimer = new Timer();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mPlayerBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 取得音乐列表
        ArrayList<Music> mMusicList = intent.getParcelableArrayListExtra("musicList");
        mMediaPlayer = MediaPlayer.create(getBaseContext(), mMusicList.get(0).getUri());
        mPlayerBinder = new MusicPlayerBinder(getBaseContext(), mMediaPlayer, mMusicList, mTimingTimer, mAnimationTimer);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mTimingTimer.cancel();
        mAnimationTimer.cancel();
        mMediaPlayer.stop();
    }
}
