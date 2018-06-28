package io.guaong.gesturemusic.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import io.guaong.gesturemusic.config.ColorConfig;
import io.guaong.gesturemusic.util.WindowUtil;

/**
 * 播放按钮外边框
 */
public class CircleBorderView extends ViewGroup {

    private Paint mPaint;
    private DrawFilter mDrawFilter;
    private float rX, rY, R;
    private float mPaintWidth;

    public CircleBorderView(Context context) {
        super(context);
    }

    public CircleBorderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 初始绘制波纹的画笔
        mPaint = new Paint();
        // 去除画笔锯齿
        mPaint.setAntiAlias(true);
        mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        l = (int)(rX - R / 2);
        t = (int)(rY - R / 2);
        r = (int)(rX + R / 2);
        b = (int)(rY + R / 2);
        View child = getChildAt(0);
        child.layout(l,t,r,b);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        rX = w / 2;
        rY = h / 2;
        //由于分辨率均为整十整百的数值，因此除以5
        //得出宽，高的最小值，半径占1/3，即留下1/6的内边距
        R = Math.min(w, h) / 3;
        //画笔粗细根据设备宽：240得到
        mPaintWidth = WindowUtil.getWindowWidth(getContext()) / 240f;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec) / 2;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(mDrawFilter);
        mPaint.setColor(ColorConfig.PAINT_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mPaintWidth);
        canvas.drawCircle(rX, rY, R, mPaint);
    }
}
