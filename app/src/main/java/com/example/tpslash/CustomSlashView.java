package com.example.tpslash;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CustomSlashView extends View {
    private final static String TAG = "CustomSlashView";

    private int crossCount = 21;

    private float movX;
    private float movY;
    private float currentX;
    private float currentY;

    boolean[] slashStates;
    boolean[] backslashStates;

    private Canvas canvas;
    private Bitmap bitmap;
    private Paint paintMove;
    private Paint paint;

    //
    private float bandHeight;
    private float slope;
    private float offset;

    private int width;
    private int height;

    /**两个集合--》记录手指的轨迹的点---》一个线段*/
    private ArrayList<Float> xs;
    private ArrayList<Float> ys;
    //装所有线段的集合
    private List<ArrayList<Float>> linesX;
    private List<ArrayList<Float>> linesY;

    private ArrayList<Float> slashStateX;
    private ArrayList<Float> slashStateY;

    private ArrayList<Float> backslashStateX;
    private ArrayList<Float> backslashStateY;

    public CustomSlashView(Context context) {
        this(context, null);
    }

    public CustomSlashView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomSlashView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);

        paintMove = new Paint(Paint.DITHER_FLAG);//创建一个画笔
        paintMove.setStyle(Paint.Style.STROKE);//设置非填充
        paintMove.setStrokeWidth(5);//笔宽5像素
        paintMove.setColor(Color.GREEN);//设置为红笔
        paintMove.setAntiAlias(true);//锯齿不显示

        init();
    }

    public void init() {
        linesX = new ArrayList<ArrayList<Float>>();
        linesY = new ArrayList<ArrayList<Float>>();

        slashStateX = new ArrayList<>();
        slashStateY = new ArrayList<>();

        backslashStateX = new ArrayList<>();
        backslashStateY = new ArrayList<>();

        slashStates = new boolean[crossCount];
        backslashStates = new boolean[crossCount];
        for(int i = 0; i < crossCount; i++){
            slashStates[i] = false;
            backslashStates[i] = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        width = getWidth();
        height = getHeight();

        for(int i = 0; i < slashStateX.size(); i++) {
            canvas.drawCircle(slashStateX.get(i), slashStateY.get(i), 10, paintMove);
        }

        for(int i = 0; i < backslashStateX.size(); i++) {
            canvas.drawCircle(backslashStateX.get(i), backslashStateY.get(i), 10, paintMove);
        }

        for(int j=0; j<linesX.size(); j++) {
            for(int i=0; i<linesX.get(j).size()-1; i++) {
                canvas.drawLine(linesX.get(j).get(i), linesY.get(j).get(i),linesX.get(j).get(i+1), linesY.get(j).get(i+1), paintMove);
            }
        }

        // y = slope * x + offset
        slope = (float) height / (float) width;
        offset = (float) height / 20;

        float leftSlashStartX = 0;
        float leftSlashStartY = leftSlashStartX * slope + offset;
        float leftSlashEndY = height;
        float leftSlashEndX = (leftSlashEndY - offset ) / slope;
        canvas.drawLine(leftSlashStartX, leftSlashStartY, leftSlashEndX, leftSlashEndY, paint);

        // y = slope * x - offset
        float rightSlashStartY = 0;
        float rightSlashStartX = (rightSlashStartY + offset) / slope;
        float rightSlashEndX = width;
        float rightSlashEndY = rightSlashEndX * slope - offset;
        canvas.drawLine(rightSlashStartX, rightSlashStartY, rightSlashEndX, rightSlashEndY, paint);

        // y = - slope * x + height - offset
        float leftBackSlashStartY = 0;
        float leftBackSlashStartX = (leftBackSlashStartY + offset - height) / slope * (-1);
        float leftBackSlashEndX = 0;
        float leftBackSlashEndY = (-1) * leftBackSlashEndX * slope + height - offset;
        canvas.drawLine(leftBackSlashStartX, leftBackSlashStartY, leftBackSlashEndX, leftBackSlashEndY, paint);

        // y = - slope * x + height + offset
        float rightBackSlashStartX = width;
        float rightBackSlashStartY = (-1) * rightBackSlashStartX * slope + height + offset;
        float rightBackSlashEndY = height;
        float rightBackSlashEndX = (rightBackSlashEndY - offset - height) / slope * (-1);
        Log.e(TAG, "x:" + rightBackSlashEndX + " y:" + rightBackSlashEndY + " x1:" + rightBackSlashStartX + " y1:" + rightBackSlashStartY);
        canvas.drawLine(rightBackSlashStartX, rightBackSlashStartY, rightBackSlashEndX, rightBackSlashEndY, paint);

        bandHeight = (float)height / crossCount;

        float startX;
        float endX;
        float horizontalY;
        for (int i = 1; i < crossCount; i++) {
            horizontalY = bandHeight * i;

            startX = (horizontalY - offset ) / slope;
            endX = (horizontalY + offset) / slope;
            canvas.drawLine(startX, horizontalY, endX, horizontalY, paint);

            startX = (horizontalY + offset - height) / slope * (-1);;
            endX = (horizontalY - offset - height) / slope * (-1);
            canvas.drawLine(startX, horizontalY, endX, horizontalY, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float x = event.getX();
            float y = event.getY();

            xs.add(x);
            ys.add(y);

            if (bandHeight != 0) {
                int index = (int) (y / bandHeight);
                //y值在边界时会出现 相除等于crossCount的情况，导致数组越界
                if (index == crossCount) {
                    index = index - 1;
                }

                float circleY = index * bandHeight + bandHeight / 2;

                if (((y - offset ) / slope)  < x &&  x < ((y + offset) / slope)) {
                    slashStates[index] = true;

                    slashStateY.add(circleY);
                    slashStateX.add(circleY  * width / height);
                }

                if (((y + offset - height) / slope * (-1))  < x
                        &&  x < ((y - offset - height) / slope * (-1))) {
                    backslashStates[index] = true;

                    backslashStateY.add(circleY);
                    backslashStateX.add((height - circleY)  / slope);
                }
            }

            invalidate();
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            xs=new ArrayList<Float>();
            ys=new ArrayList<Float>();

            linesX.add(xs);
            linesY.add(ys);
        }
        return true;
    }
}
