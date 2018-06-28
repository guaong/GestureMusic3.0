package io.guaong.gesturemusic;

import android.os.Handler;
import android.os.Message;

import io.guaong.gesturemusic.activity.MainActivity;
import io.guaong.gesturemusic.service.MusicPlayerService;
import io.guaong.gesturemusic.ui.WaterWaveView;

/**
 * Created by 关桐 on 2018/6/22.
 * 信息中转站，用于转发各种消息
 */
public class CommunicationTransitStation {

    public static int TIMING_START = 1;
    public static int TIMING_STOP = 2;
    // 音乐播放完成
    public static int MUSIC_PLAY_COMPLETE = 1;

    /**
     * 向activity发送消息
     */
    public static void sendMessageToActivity(){
        final Message message = new Message();
        message.arg1 = MUSIC_PLAY_COMPLETE;
        // 向activity发送消息，改变音乐信息
        MainActivity.musicInformationChangeHandler.sendMessage(message);
    }

    /**
     * 向MainActivity发送开始定时指令
     */
    public static void sendTimingStartToService(long time){
        final Message message = new Message();
        message.arg1 = TIMING_START;
        message.obj = time;
        // 向activity发送消息，改变音乐信息
        MusicPlayerBinder.timingHandler.sendMessage(message);
    }

    /**
     * 向MainActivity发送结束定时指令
     */
    public static void sendTimingStopToMainActivity(Handler handler){
        final Message message = new Message();
        message.arg1 = TIMING_STOP;
        // 向activity发送消息，改变音乐信息
        handler.sendMessage(message);
    }

    /**
     * 向WaterWaveView发送消息
     * @param musicDuration 时长
     */
    public static void sendDurationToAnimation(long musicDuration){
        sendMessageToAnimation(WaterWaveView.RESET_ANIMATION, musicDuration);
    }

    /**
     * 向WaterWaveView发送消息
     * @param b 是否停止动画
     */
    public static void sendStatusToAnimation(boolean b){
        sendMessageToAnimation(WaterWaveView.CHANGE_ANIMATION, b);
    }

    /**
     * 向WaterView发送消息
     * @param currentTime 当前音乐时间
     */
    public static void sendCurrentTimeToAnimation(int currentTime){
        sendMessageToAnimation(WaterWaveView.UPDATE_ANIMATION, currentTime);
    }

    /**
     * 发送消息到WaterView
     */
    private static void sendMessageToAnimation(int type, Object o){
        final Message message = new Message();
        message.arg1 = type;
        message.obj = o;
        WaterWaveView.animationHandler.sendMessage(message);
    }

}
