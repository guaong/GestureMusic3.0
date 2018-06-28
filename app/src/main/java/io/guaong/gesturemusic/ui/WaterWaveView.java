package io.guaong.gesturemusic.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import java.lang.ref.WeakReference;

import io.guaong.gesturemusic.CommunicationTransitStation;
import io.guaong.gesturemusic.config.ColorConfig;
import io.guaong.gesturemusic.config.StringConfig;
import io.guaong.gesturemusic.util.SharedPreferencesUtil;
import io.guaong.gesturemusic.util.WindowUtil;

/**
 * Created by 关桐 on 2018/6/22.
 *
 */
public class WaterWaveView extends View{

    // 重置动画
    public static final int RESET_ANIMATION = 1;
    // 改变动画（动画动或暂停）
    public static final int CHANGE_ANIMATION = 2;
    // 更新动画（动画高度变化）
    public static final int UPDATE_ANIMATION = 3;

    // 第一条水波移动速度
    private static final float TRANSLATE_X_SPEED_ONE = 7f;

    // 当前控件高度
    private int mHeight;
    // 当前控件宽度
    private int mWidth;
    // 第一条的偏移量
    private int mOffsetX;
    private float mOffsetY = 0;
    // 第一条偏移的大小
    private int mOffsetSpeedX;

    // Y坐标组
    private float[] mPositionsY;
    // 改变的第一组Y坐标
    private float[] mResetPositionsY;
    // 第一组线（四个为一条线）
    private float[] lines;

    // 是否改变坐标数据，即重绘内容是否改变
    private boolean isStop = true;
    // 音乐时长
    private long mMusicDuration;

    private Paint mPaint;
    private DrawFilter mDrawFilter;

    public static AnimationHandler animationHandler;

    public WaterWaveView(Context context) {
        super(context);
    }

    public WaterWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        animationHandler = new AnimationHandler(this);
        // 将dp转化为px，用于控制不同分辨率上移动速度基本一致
        mOffsetSpeedX = WindowUtil.dipToPx(context, TRANSLATE_X_SPEED_ONE);
        // 初始绘制波纹的画笔
        mPaint = new Paint();
        // 去除画笔锯齿
        mPaint.setAntiAlias(true);
        // 设置风格为实线
        mPaint.setStyle(Paint.Style.FILL);
        // 设置画笔颜色
        mPaint.setColor(ColorConfig.WATER_COLOR);
        mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(mDrawFilter);
        // 重置所有Y坐标，第一次为初始化
        resetPositionsY();
        int j = 0;
        /* 将组成线的四个坐标存入数组 */
        for (int i = 0; i < mWidth; i++){
            lines[j++] = i;
            lines[j++] = mHeight - mResetPositionsY[i] - mOffsetY;
            lines[j++] = i;
            lines[j++] = mHeight;
        }
        canvas.drawLines(lines, mPaint);
        /* cut点，将从该点将正弦函数分割成两部分 */
        mOffsetX += mOffsetSpeedX;
        if (mOffsetX >= mWidth) {
            mOffsetX = 0;
        }
        postInvalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        mPositionsY = new float[mWidth];
        mResetPositionsY = new float[mWidth];
        lines = new float[mWidth * 4];

        // 将周期定为view总宽度
        final float mCycle = (float) (2 * Math.PI / mWidth);
        final float OFFSET_Y = 0;
        final float STRETCH_FACTOR_A = 20f;
        for (int i = 0; i < mWidth; i++){
            // Asin（ωx+φ）+b
            mPositionsY[i] = (float)(STRETCH_FACTOR_A * Math.sin(mCycle * i) + OFFSET_Y);
        }
    }

    /**
     * 重置Y坐标
     * 将正弦函数分割为两部分，分别得到原始正弦数据的相应部分
     * 由于是一个完整的2π周期，因此是连续不断地
     */
    private void resetPositionsY(){
        if (!isStop){
            int firstIntervalY = mPositionsY.length - mOffsetX;
            System.arraycopy(mPositionsY, mOffsetX, mResetPositionsY, 0, firstIntervalY);
            System.arraycopy(mPositionsY, 0, mResetPositionsY, firstIntervalY, mOffsetX);
        }
    }

    public void setColor(int color){
        mPaint.setColor(color);
        postInvalidate();
    }

    /**
     * 用于接收动画状态
     * 信息来自MusicPlayService
     * 用于根据当前播放时间改变水波高度
     */
    public static class AnimationHandler extends Handler {

        private WeakReference<WaterWaveView> mWeakReference;
        private WaterWaveView mWaterWaveView;

        AnimationHandler(WaterWaveView waterWaveView){
            mWeakReference = new WeakReference<>(waterWaveView);
            mWaterWaveView = mWeakReference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.arg1){
                case RESET_ANIMATION:
                    resetAnimation(msg);
                    break;
                case CHANGE_ANIMATION:
                    changeAnimation(msg);
                    break;
                case UPDATE_ANIMATION:
                    updateAnimation(msg);
                    break;
            }
        }

        private void resetAnimation(Message msg){
            mWaterWaveView.mMusicDuration = (long)msg.obj;

        }

        private void changeAnimation(Message msg){
            mWaterWaveView.isStop = (boolean)msg.obj;
        }

        private void updateAnimation(Message msg){
            int current = (int)msg.obj;
            mWaterWaveView.mOffsetY =
                    (float) mWaterWaveView.mHeight / mWaterWaveView.mMusicDuration * current;
        }
    }

}