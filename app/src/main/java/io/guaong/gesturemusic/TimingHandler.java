package io.guaong.gesturemusic;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Timer;

public class TimingHandler extends Handler {

    private Timer mTimer;
    private TimingTask mTimingTask;

    TimingHandler(TimingTask timingTask, Timer timer){
        mTimingTask = timingTask;
        mTimer = timer;
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.arg1 == CommunicationTransitStation.TIMING_START) {
            mTimingTask.cancel();
            mTimingTask = new TimingTask();
            mTimer.schedule(mTimingTask, (long)msg.obj);
        } else {
            mTimingTask.cancel();
        }
    }
}
