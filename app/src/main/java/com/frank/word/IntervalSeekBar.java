package com.frank.word;

//https://blog.csdn.net/u012127961/article/details/121688095

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * 间距拖动
 */
public class IntervalSeekBar extends View {

    //圆圈颜色
    private int circleColor = Color.WHITE;
    //圆圈半径
    private int radius = 50;
    //背景颜色
    private int backgroundColor = Color.parseColor("#929292");
    //进度颜色
    private int progressColor = Color.parseColor("#003AFD");
    //线条宽度
    private int lineWidth = 8;
    //中心位置
    private int centerX, centerY;
    //宽度、高度
    private int width, height;
    //左边进度
    private int leftProgress = 20;
    //右边进度
    private int rightProgress = 80;
    //总长度
    private int totalProgress = 100;
    //水平间距
    private int marginHorizontal = 40;
    //垂直间距
    private int marginVertical = 40;
    //左边坐标
    private int[] leftCoordinate;
    //右边坐标
    private int[] rightCoordinate;

    private Paint paint;

    public IntervalSeekBar(Context context) {
        super(context);
    }

    public IntervalSeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public IntervalSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        centerX = getMeasuredWidth() / 2;
        centerY = getMeasuredHeight() / 2;
        int requiredWidth = width, requiredHeight = radius * 2 + marginVertical * 2;
        int measureSpecWidth = MeasureSpec.getSize(widthMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int measureSpecHeight = MeasureSpec.getSize(heightMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int measureWidth = measureSpecWidth;
        int measureHeight = measureSpecHeight;
        if ((widthSpecMode == MeasureSpec.AT_MOST || widthSpecMode == MeasureSpec.UNSPECIFIED) && heightSpecMode == MeasureSpec.AT_MOST || heightSpecMode == MeasureSpec.UNSPECIFIED) {
            measureWidth = requiredWidth;
            measureHeight = requiredHeight;
        } else if (widthSpecMode == MeasureSpec.AT_MOST || widthSpecMode == MeasureSpec.UNSPECIFIED) {
            measureWidth = requiredWidth;
            measureHeight = measureSpecHeight;
        } else if (heightSpecMode == MeasureSpec.AT_MOST || heightSpecMode == MeasureSpec.UNSPECIFIED) {
            measureWidth = measureSpecWidth;
            measureHeight = requiredHeight;
        }
        setMeasuredDimension(measureWidth, measureHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        drawProgress(canvas, true);
        drawProgress(canvas, false);
        drawCircle(canvas, true);
        drawCircle(canvas, false);
    }

    /**
     * 绘制背景
     *
     * @param canvas
     */
    private void drawBackground(Canvas canvas) {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(backgroundColor);
        paint.setStrokeWidth(lineWidth);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        int lineWidth = width - 2 * marginHorizontal;
        int lineStartX = marginHorizontal;
        int lineStopX = marginHorizontal + lineWidth;
        paint.setColor(backgroundColor);
        canvas.drawLine(lineStartX, centerY, lineStopX, centerY, paint);
    }

    /**
     * 绘制进度
     *
     * @param canvas
     * @param left
     */
    private void drawProgress(Canvas canvas, boolean left) {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(progressColor);
        paint.setStrokeWidth(lineWidth);
        int lineWidth = width - 2 * marginHorizontal;
        int lineLeftStartX = marginHorizontal;
        int lineLeftStopX = marginHorizontal + lineWidth * leftProgress / totalProgress;
        if (left) {
            paint.setColor(backgroundColor);
            canvas.drawLine(lineLeftStartX, centerY, lineLeftStopX, centerY, paint);
        }
        int lineRightStartX = marginHorizontal + lineWidth * leftProgress / totalProgress;
        int lineSRightStopX = marginHorizontal + lineWidth * rightProgress / totalProgress;
        if (!left) {
            paint.setColor(progressColor);
            canvas.drawLine(lineRightStartX, centerY, lineSRightStopX, centerY, paint);
        }
    }

    /**
     * 绘制圆圈
     *
     * @param canvas
     * @param left
     */
    private void drawCircle(Canvas canvas, boolean left) {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(circleColor);
        paint.setStrokeWidth(lineWidth);
        int lineWidth = width - 2 * marginHorizontal;
        int lineLeftStopX = marginHorizontal + lineWidth * leftProgress / totalProgress;
        if (left) {
            Shader shader = new RadialGradient(lineLeftStopX, centerY, radius, new int[]{Color.WHITE, Color.GRAY}, new float[]{0.9f, 1.0f}, Shader.TileMode.MIRROR);
            paint.setShader(shader);
            canvas.drawCircle(lineLeftStopX, centerY, radius, paint);
            leftCoordinate = new int[]{lineLeftStopX, centerY};
        }
        int lineRightStopX = marginHorizontal + lineWidth * rightProgress / totalProgress;
        if (!left) {
            Shader shader = new RadialGradient(lineRightStopX, centerY, radius, new int[]{Color.WHITE, Color.GRAY}, new float[]{0.9f, 1.0f}, Shader.TileMode.MIRROR);
            paint.setShader(shader);
            canvas.drawCircle(lineRightStopX, centerY, radius, paint);
            rightCoordinate = new int[]{lineRightStopX, centerY};
        }
    }

    /**
     * 是否左边
     *
     * @param x
     * @param y
     * @return
     */
    private boolean isLeft(float x, float y) {
        if (x <= leftCoordinate[0] + radius
                && y >= leftCoordinate[1] - radius * 2
                && y <= leftCoordinate[1] + radius * 2) {
            return true;
        }
        return false;
    }

    /**
     * 是否右边
     *
     * @param x
     * @param y
     * @return
     */
    private boolean isRight(float x, float y) {
        if (x >= rightCoordinate[0] - radius * 2
                && y >= rightCoordinate[1] - radius * 2
                && y <= rightCoordinate[1] + radius * 2) {
            return true;
        }
        return false;
    }

    private boolean isLeft;
    private boolean isRight;
    private float downX, downY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                isLeft = isLeft(downX, downY);
                isRight = isRight(downX, downY);
                break;
            case MotionEvent.ACTION_MOVE:
                if (isLeft) {
                    int leftMoveX = (int) (event.getX() - leftCoordinate[0]);
                    if (leftMoveX != 0) {
                        leftProgress = (leftCoordinate[0] + leftMoveX) * totalProgress / width;
                    }
                    if (leftProgress > totalProgress) {
                        leftProgress = totalProgress;
                    }
                    if (leftProgress < 0) {
                        leftProgress = 0;
                    }
                }
                if (isRight) {
                    int rightMoveX = (int) (event.getX() - rightCoordinate[0]);
                    if (rightMoveX != 0) {
                        rightProgress = (rightCoordinate[0] + rightMoveX) * totalProgress / width;
                    }
                    if (rightProgress > totalProgress) {
                        rightProgress = totalProgress;
                    }
                    if (rightProgress < 0) {
                        rightProgress = 0;
                    }
                }
                int spaceProgress = radius * 3 * totalProgress / (width - 2 * marginHorizontal);
                if (leftProgress < rightProgress - spaceProgress) {
                    invalidate();
                    if (onSeekBarChangeListener != null) {
                        onSeekBarChangeListener.onProgressChanged(this, leftProgress, rightProgress);
                    }
                }
                break;
        }
        return true;
    }

    private OnSeekBarChangeListener onSeekBarChangeListener;

    /**
     * 设置监听
     *
     * @param onSeekBarChangeListener
     */
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener onSeekBarChangeListener) {
        this.onSeekBarChangeListener = onSeekBarChangeListener;
    }

    public interface OnSeekBarChangeListener {
        void onProgressChanged(IntervalSeekBar seekBar, int leftProgress, int rightProgress);
    }

    public void setValue(int left, int right, int total) {
        leftProgress = left;
        rightProgress = right;
        totalProgress = total;
    }

}

