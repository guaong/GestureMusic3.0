package io.guaong.gesturemusic.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import io.guaong.gesturemusic.config.ColorConfig;
import io.guaong.gesturemusic.util.WindowUtil;

/**
 * Created by 关桐 on 2017/9/3.
 * 圆形的按钮
 */
public class CircleButton extends View {

    private Paint mPaint;
    private DrawFilter mDrawFilter;
    private float rX, rY, r;
    private float mPaintWidth;
    private float mFontWidth;
    private float mTextSize;
    private String text = "";
    private float leftTopX, leftTopY, rightBottomX, rightBottomY;

    public CircleButton(Context context) {
        super(context);
    }

    public CircleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        // 初始绘制波纹的画笔
        mPaint = new Paint();
        // 去除画笔锯齿
        mPaint.setAntiAlias(true);
        mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        rX = w / 2;
        rY = h / 2;
        //由于分辨率均为整十整百的数值，因此除以5
        //得出宽，高的最小值，半径占1/3，即留下1/6的内边距
        r = Math.min(w, h) / 3;
        //画笔粗细根据设备宽：240得到
        mPaintWidth = WindowUtil.getWindowWidth(getContext()) / 240f;
        mFontWidth = mPaintWidth / 3;
        mTextSize = WindowUtil.pxToSp(getContext(), WindowUtil.dipToPx(getContext(), r / 2));
        leftTopX = w / 2 - r;
        leftTopY = h / 2 - r;
        rightBottomX = leftTopX + 2 * r;
        rightBottomY = leftTopY + 2 * r;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthSize / 4, widthSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(mDrawFilter);
        mPaint.setColor(ColorConfig.PAINT_COLOR);
        drawCircleBorder(canvas);
        drawText(canvas, text);
    }

    /**
     * 绘制按钮外边框
     */
    private void drawCircleBorder(Canvas canvas){
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mPaintWidth);
        canvas.drawCircle(rX, rY, r, mPaint);
    }

    /**
     * 绘制按钮中的文字
     */
    private void drawText(Canvas canvas, String text){
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(mFontWidth);
        mPaint.setTextSize(mTextSize);
        final Rect targetRect =
                new Rect((int)leftTopX, (int)leftTopY, (int)rightBottomX, (int)rightBottomY);
        final Paint.FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
        int baseline =
                (targetRect.bottom + targetRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
        mPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(text, targetRect.centerX(), baseline, mPaint);
    }

    /**
     * 设置按钮中文字内容
     */
    public void setText(String text){
        this.text = text;
        postInvalidate();
    }

    /**
     * 得到按钮中文字
     */
    public String getText(){
        return text;
    }
}
