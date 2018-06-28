package io.guaong.gesturemusic.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TimePicker;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import io.guaong.gesturemusic.CommunicationTransitStation;
import io.guaong.gesturemusic.MusicPlayerBinder;
import io.guaong.gesturemusic.R;
import io.guaong.gesturemusic.TimingTask;
import io.guaong.gesturemusic.config.ColorConfig;
import io.guaong.gesturemusic.config.StringConfig;
import io.guaong.gesturemusic.config.TimingConfig;
import io.guaong.gesturemusic.model.Color;
import io.guaong.gesturemusic.service.MusicPlayerService;
import io.guaong.gesturemusic.ui.PlayButton;
import io.guaong.gesturemusic.util.SharedPreferencesUtil;

public class SettingsActivity extends AppCompatActivity {

    private Switch mFullScreenSwitch;
    private Switch mTimingSwitch;
    private RelativeLayout mSettingsLayout;
    private AlertDialog mAlertDialog;
    private RadioGroup mTimingRadioGroup;
    private Switch mDiyBgSwitch;
    private Switch mDiyWaterSwitch;

    public static TimingStopHandler timingStopHandler;

    private Color mColor;

    private boolean isBgChanged = false;
    private boolean isWaterChanged = false;

    public static long time = TimingConfig.HALF_HOUR;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TimingTask.currentActivity = 2;
        initWindow();
        findAllId();
        initView();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        TimingTask.currentActivity = 2;
    }

    private void initWindow(){
        if (SharedPreferencesUtil.readFullScreenParameter(this, StringConfig.IS_FULL)){
            setFullscreen();
        }
        setContentView(R.layout.activity_settings);
        cancelActionBar();
    }

    private void cancelActionBar(){
        final ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.hide();
        }
    }

    private void setFullscreen(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void findAllId(){
        mFullScreenSwitch = findViewById(R.id.switch_full_screen);
        mTimingSwitch = findViewById(R.id.switch_timing);
        mSettingsLayout = findViewById(R.id.layout_settings);
        mTimingRadioGroup = findViewById(R.id.group_timing);
        mDiyBgSwitch = findViewById(R.id.switch_diy_bg);
        mDiyWaterSwitch = findViewById(R.id.switch_diy_water);
    }

    private void initView(){
        readAppColors();
        mFullScreenSwitch.setChecked(SharedPreferencesUtil
                .readFullScreenParameter(this, StringConfig.IS_FULL));
        mSettingsLayout.setBackgroundColor(mColor.getBgColor());
        if (MusicPlayerService.isTiming){
            mTimingSwitch.setChecked(true);
            mTimingRadioGroup.setVisibility(View.VISIBLE);
            if (time == TimingConfig.HALF_HOUR){
                mTimingRadioGroup.check(R.id.radio_half_hour);
            }else if (time == TimingConfig.ONE_HOUR){
                mTimingRadioGroup.check(R.id.radio_one_hour);
            }else if(time == TimingConfig.ONE_AND_A_HALF_HOUR){
                mTimingRadioGroup.check(R.id.radio_one_and_a_half_hour);
            }else{
                mTimingRadioGroup.check(R.id.radio_two_hour);
            }
        }
        if (SharedPreferencesUtil.readColorParameter(this, StringConfig.COLOR_BACKGROUND) != -1){
            mDiyBgSwitch.setChecked(true);
        }
        if (SharedPreferencesUtil.readColorParameter(this, StringConfig.COLOR_WATER) != -1){
            mDiyWaterSwitch.setChecked(true);
        }
        timingStopHandler = new TimingStopHandler(this);
        initListener();
    }

    private void initListener(){
        mFullScreenSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    SharedPreferencesUtil.writeFullScreenParameter(getApplicationContext(),
                            StringConfig.FULL_SCREEN, StringConfig.IS_FULL, true);
                }else {
                    SharedPreferencesUtil.writeFullScreenParameter(getApplicationContext(),
                            StringConfig.FULL_SCREEN, StringConfig.IS_FULL, false);
                }
                alert();
            }
        });

        mTimingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    mTimingRadioGroup.setVisibility(View.VISIBLE);
                    mTimingRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup group, int checkedId) {
                            switch (checkedId){
                                case R.id.radio_half_hour:time = TimingConfig.HALF_HOUR; break;
                                case R.id.radio_one_hour:time = TimingConfig.ONE_HOUR; break;
                                case R.id.radio_one_and_a_half_hour:time = TimingConfig.ONE_AND_A_HALF_HOUR; break;
                                case R.id.radio_two_hour:time = TimingConfig.TWO_HOUR; break;
                            }
                        }
                    });
                    CommunicationTransitStation.sendTimingStartToService(time);
                    MusicPlayerService.isTiming = true;
                }else {
                    mTimingRadioGroup.setVisibility(View.GONE);
                    CommunicationTransitStation.sendTimingStopToMainActivity(MusicPlayerBinder.timingHandler);
                    MusicPlayerService.isTiming = false;
                }
            }
        });
        mDiyWaterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isWaterChanged) {
                    if (isChecked) {
                        mDiyWaterSwitch.setChecked(false);
                        showColorPickerDialogForWater();
                    } else {
                        SharedPreferencesUtil.cancelColorParameter(getApplicationContext(),
                                StringConfig.COLOR, StringConfig.COLOR_WATER);
                    }
                }else {
                    isWaterChanged = false;
                }
            }
        });
        mDiyBgSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isBgChanged) {
                    if (isChecked) {
                        mDiyBgSwitch.setChecked(false);
                        showColorPickerDialogForBg();
                    } else {
                        SharedPreferencesUtil.cancelColorParameter(getApplicationContext(),
                                StringConfig.COLOR, StringConfig.COLOR_BACKGROUND);
                        mSettingsLayout.setBackgroundColor(ColorConfig.BACKGROUND_COLOR);
                    }
                }else {
                    isBgChanged = false;
                }
            }
        });
    }

    private void readAppColors(){
        int bgColor = SharedPreferencesUtil.readColorParameter(this, "bg");
        int waterColor = SharedPreferencesUtil.readColorParameter(this, "water");
        if (bgColor == -1){
            bgColor = ColorConfig.BACKGROUND_COLOR;
        }
        if (waterColor == -1){
            waterColor = ColorConfig.WATER_COLOR;
        }
        mColor = new Color(bgColor, waterColor);
    }

    private void alert(){
        mAlertDialog = new AlertDialog.Builder(this)
                .setTitle(StringConfig.FULL_SCREEN_ALERT_TITLE)
                .setMessage(StringConfig.FULL_SCREEN_ALERT_MESSAGE)
                .setNegativeButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAlertDialog.dismiss();
                    }
                })
                .show();
    }

    private void showColorPickerDialogForBg(){
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("选择颜色")
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) { }
                })
                .setPositiveButton("确定", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        SharedPreferencesUtil.
                                writeColorParameter(getApplicationContext(),
                                        StringConfig.COLOR, StringConfig.COLOR_BACKGROUND, selectedColor);
                        mSettingsLayout.setBackgroundColor(SharedPreferencesUtil.
                                readColorParameter(getApplicationContext(), StringConfig.COLOR_BACKGROUND));
                        isBgChanged = true;
                        mDiyBgSwitch.setChecked(true);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isBgChanged = false;
                        mDiyBgSwitch.setChecked(false);
                    }
                })
                .build()
                .show();
    }

    private void showColorPickerDialogForWater(){
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("选择颜色")
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) { }
                })
                .setPositiveButton("确定", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        SharedPreferencesUtil.
                                writeColorParameter(getApplicationContext(),
                                        StringConfig.COLOR, StringConfig.COLOR_WATER, selectedColor);
                        isWaterChanged = true;
                        mDiyWaterSwitch.setChecked(true);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isWaterChanged = false;
                        mDiyWaterSwitch.setChecked(false);
                    }
                })
                .build()
                .show();
    }

    public static class TimingStopHandler extends Handler {

        private WeakReference<SettingsActivity> mWeakReference;

        TimingStopHandler(SettingsActivity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.arg1 == CommunicationTransitStation.TIMING_STOP){
                mWeakReference.get().mTimingSwitch.setChecked(false);
                mWeakReference.get().mTimingRadioGroup.setVisibility(View.GONE);
            }
        }
    }

}
