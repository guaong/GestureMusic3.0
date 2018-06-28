package io.guaong.gesturemusic.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.guaong.gesturemusic.CommunicationTransitStation;
import io.guaong.gesturemusic.MusicPlayerBinder;
import io.guaong.gesturemusic.R;
import io.guaong.gesturemusic.TimingHandler;
import io.guaong.gesturemusic.TimingTask;
import io.guaong.gesturemusic.config.ColorConfig;
import io.guaong.gesturemusic.config.StringConfig;
import io.guaong.gesturemusic.model.Color;
import io.guaong.gesturemusic.model.Music;
import io.guaong.gesturemusic.service.MusicPlayerService;
import io.guaong.gesturemusic.ui.CircleButton;
import io.guaong.gesturemusic.ui.PlayButton;
import io.guaong.gesturemusic.ui.WaterWaveView;
import io.guaong.gesturemusic.util.MusicUtil;
import io.guaong.gesturemusic.util.SharedPreferencesUtil;

public class MainActivity extends AppCompatActivity {

    private CircleButton mListBtn;
    private RelativeLayout mMainLayout;
    private RelativeLayout mListLayout;
    private RelativeLayout mBodyLayout;
    private TextView mBackText;
    private ImageButton mSettingsBtn;
    private ImageButton mOrderBtn;
    private PlayButton mPlayBtn;
    private TextView mNameText;
    private TextView mAuthorText;
    private RecyclerView mRecycler;
    private WaterWaveView mWaterWaveView;
    private AlertDialog mAlertDialog;

    private MusicListAdapter mAdapter;
    // 意图用于启动播放音乐的服务
    private Intent mIntent;
    // 播放音乐的绑定
    private MusicPlayerBinder mPlayerBinder;
    // 连接服务和活动
    private ServiceConnection mConnection;
    private Typeface mTypeface;
    private Color mColor;

    // 音乐列表
    private ArrayList<Music> mMusicList;

    // 播放状态
    private boolean isStopped = true;
    private boolean haveMusic = true;
    private boolean isFirstBack = true;
    /* 点击屏幕是按下和抬起时的位置 */
    private float downX = 0, downY = 0;
    private float upX = 0, upY = 0;
    private int clickCount = 1;

    final String LIST_BTN_TEXT = "列表";

    public static MusicInformationChangeHandler musicInformationChangeHandler;
    public static TimingStopHandler timingStopHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TimingTask.currentActivity = 2;
        if (havePermission()){
            showNoPermissionView();
        }
        if(!addMusicRes()){ //没有歌曲
            setContentView(R.layout.activity_no_music);
        }else{
            initWindow();
            initService();
            startService();
            findAllId();
            initView();
        }
    }

    private boolean havePermission(){
        return PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    private void showNoPermissionView(){
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1001) {
            mAlertDialog = new AlertDialog.Builder(this)
                    .setTitle("注意")
                    .setMessage("若未开启读写存储权限，则无法使用该应用")
                    .setNegativeButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mAlertDialog.dismiss();
                        }
                    })
                    .show();
        }
    }

    private void initWindow(){
        if (SharedPreferencesUtil.readFullScreenParameter(this, StringConfig.IS_FULL)){
            setFullscreen();
        }
        setContentView(R.layout.activity_main);
        cancelActionBar();
        readAppColors();
    }

    private void readAppColors(){
        int bgColor = SharedPreferencesUtil.readColorParameter(this, StringConfig.COLOR_BACKGROUND);
        int waterColor = SharedPreferencesUtil.readColorParameter(this, StringConfig.COLOR_WATER);
        if (bgColor == -1){
            bgColor = ColorConfig.BACKGROUND_COLOR;
        }
        if (waterColor == -1){
            waterColor = ColorConfig.WATER_COLOR;
        }
        mColor = new Color(bgColor, waterColor);
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

    private boolean addMusicRes(){
        mMusicList = MusicUtil.getMusicList(this);
        haveMusic = mMusicList.size() != 0;
        return haveMusic;
    }

    private void initService(){
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mPlayerBinder = (MusicPlayerBinder) service;
                // 只有等到服务启动完成才可以执行，否则有可能会优先执行，造成空指针异常
                insertMusicInformation();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };
    }

    private void startService(){
        mIntent = new Intent(this, MusicPlayerService.class);
        mIntent.putParcelableArrayListExtra("musicList", mMusicList);
        startService(mIntent);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);
    }

    private void findAllId(){
        mWaterWaveView = findViewById(R.id.view_water_wave);
        mListBtn = findViewById(R.id.btn_list);
        mBodyLayout = findViewById(R.id.layout_body);
        mMainLayout = findViewById(R.id.layout_main);
        mListLayout = findViewById(R.id.layout_list);
        mBackText = findViewById(R.id.text_back);
        mSettingsBtn = findViewById(R.id.btn_settings);
        mPlayBtn = findViewById(R.id.btn_play);
        mAuthorText = findViewById(R.id.text_author);
        mNameText = findViewById(R.id.text_name);
        mRecycler = findViewById(R.id.recycler_list);
        mOrderBtn = findViewById(R.id.btn_order);
    }

    private void initView(){
        changeColor();
        musicInformationChangeHandler = new MusicInformationChangeHandler(this);
        timingStopHandler = new TimingStopHandler(this);
        setTextViewFontStyle();
        mListBtn.setText(LIST_BTN_TEXT);
        mListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainLayout.setVisibility(View.INVISIBLE);
                mListLayout.setVisibility(View.VISIBLE);
                initRecycler();
                mRecycler.scrollToPosition(mPlayerBinder.getCurrentPosition());
            }
        });
        mBackText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainLayout.setVisibility(View.VISIBLE);
                mListLayout.setVisibility(View.INVISIBLE);
            }
        });
        mSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });
        mPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStopped){
                    mPlayBtn.setStatus(PlayButton.PLAY_TO_PAUSE);
                    isStopped = false;
                    mPlayerBinder.playCurrent();
                }else {
                    mPlayBtn.setStatus(PlayButton.PAUSE_TO_PLAY);
                    isStopped = true;
                    mPlayerBinder.pauseCurrent();
                }
            }
        });
        mOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickCount = clickCount % 3 + 1;
                switch (clickCount){
                    case 1: mOrderBtn.setImageResource(R.drawable.loop);
                            mPlayerBinder.setPlayOrder(MusicPlayerBinder.PLAY_LOOP);break;
                    case 2: mOrderBtn.setImageResource(R.drawable.random);
                            mPlayerBinder.setPlayOrder(MusicPlayerBinder.PLAY_RANDOM);break;
                    case 3: mOrderBtn.setImageResource(R.drawable.single);
                            mPlayerBinder.setPlayOrder(MusicPlayerBinder.PLAY_SINGLE);break;
                }
            }
        });
    }

    private void changeColor(){
        mBodyLayout.setBackgroundColor(mColor.getBgColor());
        mListLayout.setBackgroundColor(mColor.getWaterColor());
        mWaterWaveView.setColor(mColor.getWaterColor());
    }

    private void setTextViewFontStyle() {
        mTypeface = Typeface.createFromAsset(getAssets(), "font/nunito.ttf");
        mBackText.setTypeface(mTypeface);
        mAuthorText.setTypeface(mTypeface);
        mNameText.setTypeface(mTypeface);
    }

    private void initRecycler() {
        mRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAdapter = new MusicListAdapter(mPlayerBinder.getMusicList());
        mRecycler.setAdapter(mAdapter);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (haveMusic) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    upX = event.getRawX();
                    upY = event.getRawY();
                    // 夹角小于45度，判定为水平手势
                    boolean isHorizontal = Math.abs((upY - downY) / (upX - downX)) <= 0.5f;
                    // 滑动距离大于100，判定为有滑动手势
                    boolean isMoved = Math.abs(upX - downX) >= 100;
                    boolean isToLeft = (upX - downX) < 0;
                    if (isHorizontal && isMoved) {
                        if (isToLeft) { // play last
                            mPlayerBinder.playLast();
                        } else { // play next
                            mPlayerBinder.playNext();
                        }
                        insertMusicInformation();
                        if (isStopped){
                            mPlayBtn.setStatus(PlayButton.PLAY_TO_PAUSE);
                        }else{
                            mPlayBtn.setStatus(PlayButton.STATUS_PAUSE);
                        }
                        isStopped = false;
                    }
                    break;
                case MotionEvent.ACTION_DOWN:
                    downY = event.getRawY();
                    downX = event.getRawX();
                    break;
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (haveMusic) {
            if (mMainLayout.getVisibility() == View.INVISIBLE) {
                mListLayout.setVisibility(View.INVISIBLE);
                mMainLayout.setVisibility(View.VISIBLE);
            } else {
                exitApp();
            }
        } else {
            exitApp();
        }
    }

    private void exitApp(){
        final Timer timer = new Timer();
        if (isFirstBack){
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
            isFirstBack = false;
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    isFirstBack = true;
                }
            };
            timer.schedule(timerTask, 2000);
        }else {
            unbindService(mConnection);
            stopService(mIntent);
            finish();
            System.exit(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        stopService(mIntent);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        TimingTask.currentActivity = 1;
        readAppColors();
        changeColor();
    }

    /**
     * 设置音乐信息
     */
    public void insertMusicInformation() {
        mNameText.setText(mPlayerBinder.getCurrentMusic().getTitle());
        mAuthorText.setText(mPlayerBinder.getCurrentMusic().getArtist());
    }

    public static class MusicInformationChangeHandler extends Handler {

        private WeakReference<MainActivity> mWeakReference;

        MusicInformationChangeHandler(MainActivity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.arg1 == CommunicationTransitStation.MUSIC_PLAY_COMPLETE) {
                mWeakReference.get().insertMusicInformation();
                //mWeakReference.get().mAdapter.notifyDataSetChanged();
            }
        }
    }

    public static class TimingStopHandler extends Handler{
        private WeakReference<MainActivity> mWeakReference;

        TimingStopHandler(MainActivity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
           if (msg.arg1 == CommunicationTransitStation.TIMING_STOP){
               // 告知server停止播放音乐
               mWeakReference.get().mPlayerBinder.pauseCurrent();
               mWeakReference.get().isStopped = true;
               MusicPlayerService.isTiming = false;
               // 告知播放按钮音乐停止
               mWeakReference.get().mPlayBtn.setStatus(PlayButton.STATUS_PLAY);
           }
        }
    }

    /**
     * recycler的item点击
     */
    class ItemClickListener implements View.OnClickListener {

        int mPosition;

        ItemClickListener(int position) {
            mPosition = position;
        }

        @Override
        public void onClick(View v) {
            mPlayerBinder.setCurrentPosition(mPosition);
            mPlayerBinder.play(mPosition);
            insertMusicInformation();
            // 这真是个大bug，返回menu时才有动画效果
            mPlayBtn.setStatus(PlayButton.PLAY_TO_PAUSE);
            isStopped = false;
            mAdapter.notifyDataSetChanged();
        }
    }

    class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.MusicListHolder> {

        private List<Music> mMusicList;

        MusicListAdapter(List<Music> list) {
            mMusicList = list;
        }


        @Override
        public MusicListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_child, parent, false);
            return new MusicListHolder(view);
        }

        @Override
        public void onBindViewHolder(MusicListHolder holder, int position) {
            holder.mAuthor.setText(mMusicList.get(position).getArtist());
            holder.mName.setText(mMusicList.get(position).getTitle());
            holder.mTime.setText(mMusicList.get(position).getTime());
            holder.mLayout.setOnClickListener(new ItemClickListener(position));
            //holder.mLayout.setOnLongClickListener(new ItemLongClickListener());
            if (position == mPlayerBinder.getCurrentPosition()) {
                holder.mAuthor.setTextColor(mColor.getBgColor());
                holder.mName.setTextColor(mColor.getBgColor());
                holder.mTime.setTextColor(mColor.getBgColor());
            } else {
                holder.mAuthor.setTextColor(ColorConfig.PAINT_COLOR);
                holder.mName.setTextColor(ColorConfig.PAINT_COLOR);
                holder.mTime.setTextColor(ColorConfig.PAINT_COLOR);
            }
        }

        @Override
        public int getItemCount() {
            return mMusicList.size();
        }

        class MusicListHolder extends RecyclerView.ViewHolder {

            private TextView mName;
            private TextView mAuthor;
            private TextView mTime;
            private RelativeLayout mLayout;

            MusicListHolder(View itemView) {
                super(itemView);
                mName = itemView.findViewById(R.id.text_list_title);
                mAuthor = itemView.findViewById(R.id.text_list_author);
                mTime = itemView.findViewById(R.id.text_list_time);
                mLayout = itemView.findViewById(R.id.layout_list_child);
                mName.setTypeface(mTypeface);
                mAuthor.setTypeface(mTypeface);
                mTime.setTypeface(mTypeface);
            }
        }

    }

}
