package io.guaong.gesturemusic.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import io.guaong.gesturemusic.config.ColorConfig;
import io.guaong.gesturemusic.util.WindowUtil;

public class PlayButton extends View {

    private Paint mPaint;
    private DrawFilter mDrawFilter;
    public static final int STATUS_PLAY = 1;
    public static final int STATUS_PAUSE = 2;
    public static final int PLAY_TO_PAUSE = 3;
    public static final int PAUSE_TO_PLAY = 4;
    private int mStatus;
    private float r, rX, rY;
    private float left, right, top, bottom, rightCenter;
    private int mWidth, mHeight;
    private float mPaintWidth;
    private int narrow = 0, enlarge = 10;

    public PlayButton(Context context) {
        super(context);
        mPaint = new Paint();
        // 去除画笔锯齿
        mPaint.setAntiAlias(true);
        mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mStatus = STATUS_PLAY;
    }

    public PlayButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        // 去除画笔锯齿
        mPaint.setAntiAlias(true);
        mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mStatus = STATUS_PLAY;
    }

    public PlayButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED){
            widthSize = widthSize / 4;
        }
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED){
            heightSize = widthSize;
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        // 半径取外边框圆的半径（r = Math.min(w, h) / 3）的2/3
        r = w / 2;
        rX = w / 2;
        rY = h / 2;
        top = r - (float)(Math.sin(Math.PI / 3)) * r;
        bottom = r + (float)(Math.sin(Math.PI / 3)) * r;
        left = r - (float)(Math.cos(Math.PI / 3)) * r;
        right = r + (float)(Math.cos(Math.PI / 3)) * r;
        rightCenter = 2 * r;
        mPaintWidth = WindowUtil.getWindowWidth(getContext()) / 240f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(mDrawFilter);
        mPaint.setColor(ColorConfig.PAINT_COLOR);
        mPaint.setStrokeWidth(mPaintWidth);
        switch (mStatus){
            case STATUS_PLAY:
                drawPlayStyle(canvas, left, top, bottom, rightCenter);break;
            case STATUS_PAUSE:
                drawPauseStyle(canvas, left, right, top, bottom);break;
            case PLAY_TO_PAUSE:
                playToPause(canvas);
                break;
            case PAUSE_TO_PLAY:
                pauseToPlay(canvas);
                break;
            default:break;
        }
    }

    /**
     * 绘制播放按钮
     */
    private void drawPlayStyle(Canvas canvas, float left, float top, float bottom, float rightCenter){
        //使用双缓冲，先将所画内容存放至缓冲区
        final Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        final Canvas c = new Canvas();
        mPaint.setStyle(Paint.Style.STROKE);
        c.setBitmap(bitmap);
        final Path p = new Path();
        p.moveTo(left, top);
        p.lineTo(left, bottom);
        p.lineTo(rightCenter, rY);
        p.close();
        c.drawPath(p, mPaint);
        canvas.drawBitmap(bitmap, 0, 0, mPaint);
    }

    /**
     * 绘制暂停按钮
     */
    private void drawPauseStyle(Canvas canvas, float left, float right, float top, float bottom){
        canvas.drawLine(left, top, left, bottom, mPaint);
        canvas.drawLine(right, top, right, bottom, mPaint);
    }

    /**
     * 改变按钮状态（播放/暂停）
     */
    public void setStatus(int status){
        mStatus = status;
        postInvalidate();
    }

    /**
     * 播放按钮过渡到暂停按钮
     */
    private void playToPause(Canvas canvas){
        if (narrow <= 10){ //缩小play按钮
            playNarrow(canvas, narrow);
            narrow++;
            postInvalidate();
        }else{
            if (enlarge >= 0){ //放大pause按钮
                pauseEnlarge(canvas, enlarge);
                enlarge--;
                postInvalidate();
            }else { //还原
                narrow = 0;
                enlarge = 10;
                drawPauseStyle(canvas, left, right, top, bottom);
                mStatus = STATUS_PAUSE;
            }
        }
    }

    /**
     * 暂停按钮过渡到播放按钮
     */
    private void pauseToPlay(Canvas canvas){
        if (narrow <= 10){ //缩小pause按钮
            pauseNarrow(canvas, narrow);
            narrow++;
            postInvalidate();
        }else{
            if (enlarge >= 0){ //放大play按钮
                playEnlarge(canvas, enlarge);
                enlarge--;
                postInvalidate();
            }else { //还原
                narrow = 0;
                enlarge = 10;
                drawPlayStyle(canvas, left, top, bottom, rightCenter);
                mStatus = STATUS_PLAY;
            }
        }
    }

    /**
     * 播放按钮缩小
     */
    private void playNarrow(Canvas canvas, int narrow){
        drawPlayStyle(canvas, left + narrow, top + narrow, bottom - narrow, rightCenter - narrow);
        postInvalidate();
    }

    /**
     * 播放按钮放大
     */
    private void playEnlarge(Canvas canvas, int enlarge){
        drawPlayStyle(canvas, left + enlarge, top + enlarge, bottom - enlarge, rightCenter - enlarge);
        postInvalidate();
    }

    /**
     * 暂停按钮缩小
     */
    private void pauseNarrow(Canvas canvas, int narrow){
        drawPauseStyle(canvas, left + narrow, right - narrow, top + narrow, bottom - narrow);
        postInvalidate();
    }

    /**
     * 暂停按钮放大
     */
    private void pauseEnlarge(Canvas canvas, int enlarge){
        drawPauseStyle(canvas, left + enlarge, right - enlarge, top + enlarge, bottom - enlarge);
        postInvalidate();
    }
}
