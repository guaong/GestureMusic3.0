package io.guaong.gesturemusic;

import android.util.Log;

import java.util.TimerTask;

import io.guaong.gesturemusic.activity.MainActivity;
import io.guaong.gesturemusic.activity.SettingsActivity;

public class TimingTask extends TimerTask {

    public static int currentActivity = 1;

    @Override
    public void run() {
        if (currentActivity == 1){
            CommunicationTransitStation.sendTimingStopToMainActivity(MainActivity.timingStopHandler);
        }else{
            CommunicationTransitStation.sendTimingStopToMainActivity(SettingsActivity.timingStopHandler);
        }
        CommunicationTransitStation.sendTimingStopToMainActivity(MusicPlayerBinder.timingHandler);
    }
}
