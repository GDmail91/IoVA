package org.multibluetooth.multibluetooth.Driving;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.v4.content.ContextCompat;
import android.view.View;

/**
 * Created by YS on 2016-11-11.
 */
public class HalfCircleView extends View {
    private static final float SWEEP_INC = 180;

    private boolean isAlive = false;

    private float width ;
    private float height ;
    private int heading ;

    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int TOP = 3;
    public static final int BOTTOM = 4;

    private Paint mPaint;
    private RectF mHalfCircle;
    // 시작위치
    private float startPoint = 180;
    private float endPoint = 180;
    // 시간
    private float mSweep ;
    //선 굵기
    private int strokeWidth = 10;
    //색
    private int[] colors = {Color.BLACK, Color.BLACK, Color.BLACK};
    private int color = Color.BLACK;
    //색 범위
    float[] colorPos = {0.0f, 0.3f, 1.0f};
    //채움
    private boolean styleFill = false;


    public float[] getColorPos() {
        return colorPos;
    }

    public int[] getColors() {
        return colors;
    }

    public int getColor() {
        return color;
    }

    public boolean getFill() {
        return styleFill;
    }

    public void setFill(boolean styleFill) {
        this.styleFill = styleFill;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public float getStartPoint() {
        return startPoint;
    }

    public float getEndPoint() {
        return endPoint;
    }

    public void setColorByResource(int resId) {

        //this.color = ContextCompat.getColor(this.getContext(),resId);
        this.colors[0] = ContextCompat.getColor(this.getContext(),resId);
        this.colors[1] = ContextCompat.getColor(this.getContext(),resId);
        this.colors[2] = Color.rgb(10,10,10);
    }

    public void setAllColorByResource(int resId) {

        //this.color = ContextCompat.getColor(this.getContext(),resId);
        this.colors[0] = ContextCompat.getColor(this.getContext(),resId);
        this.colors[1] = ContextCompat.getColor(this.getContext(),resId);
        this.colors[2] = ContextCompat.getColor(this.getContext(),resId);
    }


    public HalfCircleView(Context context , float width , float height, int heading) {
        super(context);

        this.width = width;
        this.height = height;
        this.heading = heading;

        float left, top, right, bottom;

        switch (heading) {
            case LEFT:
                startPoint = 90;
                left = (float)(width * 0.2);
                top = (float)(height * 0.1);
                right = (float)(width * 1.0);
                bottom = (float)(height * 0.9);
                break;
            case RIGHT:
                startPoint = 270;
                left = -(float)(width * 0.6);
                top = (float)(height * 0.1);
                right = (float)(width * 0.5);
                bottom = (float)(height * 0.9);
                break;
            case BOTTOM:
                startPoint = 360;
                left = (float)(width * 0.1);
                top = -(float)(height * 0.7);
                right = (float)(width * 0.9);
                bottom = (float)(height * 0.7);
                break;
            default:
                startPoint = 180;
                left = (float)(width * 0.1);
                top = (float)(height * 0.3);
                right = (float)(width * 0.9);
                bottom = (float)(height * 1.7);
                break;
        }

        //반원 좌표
        mHalfCircle = new RectF(left, top, right, bottom);
    }


    /**
     * Draw 시작
     */
    public void startDraw(){

        initData();

        isAlive = true;
        invalidate();
    }

    /**
     * 데이터 초기화
     */
    private void initData(){

        startPoint = getStartPoint();
        endPoint   = getEndPoint();

        //페인트
        mPaint = new Paint();
        //mPaint.setColor(getColor());
        if (styleFill)
            mPaint.setStyle(Paint.Style.FILL);
        else
            mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(getStrokeWidth());

        int[] colors = getColors();
        float[] color_pos = getColorPos();
        LinearGradient shader;
        switch (heading) {
            case BOTTOM:
                shader = new LinearGradient(0, 0, width, height, colors, color_pos, Shader.TileMode.CLAMP);
                break;
            default:
                shader = new LinearGradient(0, height, width, 0, colors, color_pos, Shader.TileMode.CLAMP);
                break;
        }
        mPaint.setShader(shader);
        mPaint.setAntiAlias(true);
    }

    /**
     * draw 원
     * @param canvas
     * @param oval
     * @param useCenter
     * @param startPoint
     * @param paint
     */
    private void drawArcs(Canvas canvas, RectF oval, boolean useCenter, float startPoint , Paint paint) {
        //Path circle;
        //circle = new Path();
        //circle.addCircle(230, 350, 150, Path.Direction.CW);
        //canvas.drawPath(circle, paint);
        canvas.drawArc(oval, startPoint, mSweep, useCenter, paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        //배경색
        //canvas.drawColor(Color.WHITE);
        //선
        drawArcs(canvas, mHalfCircle, false, getStartPoint(), mPaint);
        //반원만
        mSweep += SWEEP_INC;
        if (mSweep > 180) {
            isAlive = false;
        }
        if (mSweep > endPoint) {
            isAlive = false;
        }

        if(isAlive){
            invalidate();
        }
    }

}