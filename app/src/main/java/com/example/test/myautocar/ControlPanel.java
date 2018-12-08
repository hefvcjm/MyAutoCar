package com.example.test.myautocar;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by hefvcjm on 17-3-8.
 */

public class ControlPanel extends View {
    //默认宽度
    private static final int DEFAULT_WIDTH = 400;
    //默认高度
    private static final int DEFUALT_HEIGHT = 400;
    //方向标记常量
    public static final int CENTER = 0;
    public static final int RIGHT = 1;
    public static final int LEFT = 2;
    public static final int BACK = 3;
    public static final int FRONT = 4;
    public static final int NONE = 5;

    //控制盘尺寸
    private int size;

    //停止按钮范围，以中心为圆心，半径为下面数值乘上size
    private float R1 = 8.0f / 42;
    //方向按钮离中心距离比例（of size）
    private float R2 = 13.0f / 42;
    //最外范围半径
    private float R3 = 0.5f;
    //半径具体值
    private float r1;
    private float r2;
    private float r3;

    //判断当前是否启动
    private boolean isPause = true;
    private OnControlListener mOnControlListener;

    public ControlPanel(Context context) {
        super(context);
        init();
    }

    public ControlPanel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ControlPanel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        //  setBackgroundColor(0x44ff0000);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        //int width = Math.min(widthSize, heightSize)
        if (widthMode == MeasureSpec.AT_MOST) {
            widthSize = DEFAULT_WIDTH;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = DEFUALT_HEIGHT;
        }
        if (widthMode == MeasureSpec.UNSPECIFIED) {
            widthSize = heightSize;
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            heightSize = widthSize;
        }
        size = Math.min(widthSize, heightSize);
        Log.d("size", size + "");
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        size = Math.min(w, h);
        r1 = size * R1;
        r2 = size * R2;
        r3 = size * R3;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            switch (getControl(event)) {
                case CENTER://暂停启动
                    if (isPause) {
                        setBackground(getResources().getDrawable(R.drawable.control_panel_pause));
                    } else {
                        setBackground(getResources().getDrawable(R.drawable.control_panel_start));
                    }
                    exeControlListener(CENTER);
                    break;
                case RIGHT://right
                    setBackground(getResources().getDrawable(R.drawable.control_panel_right));
                    exeControlListener(RIGHT);
                    break;
                case LEFT://left
                    setBackground(getResources().getDrawable(R.drawable.control_panel_left));
                    exeControlListener(LEFT);
                    break;
                case BACK://back
                    setBackground(getResources().getDrawable(R.drawable.control_panel_back));
                    exeControlListener(BACK);
                    break;
                case FRONT://front
                    setBackground(getResources().getDrawable(R.drawable.control_panel_front));
                    exeControlListener(FRONT);
                    break;
                default:
                    break;
            }
        }
        invalidate();
        if (action == MotionEvent.ACTION_UP) {
            if (isPause) {
                setBackground(getResources().getDrawable(R.drawable.control_panel_pause));
            } else {
                setBackground(getResources().getDrawable(R.drawable.control_panel_start));
            }
        }
        invalidate();
        return true;
    }

    public int getControl(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            float r = getR(x, y);
            Log.d("r", "" + r + "/" + size / 2);
            float dx = x - size / 2;
            float dy = y - size / 2;
            if (r < r1) {
                isPause = !isPause;
                return CENTER;//表示启动停止按钮
            } else if (!isPause) {
                if (r > r2 && r < r3) {
                    Log.d("direction", "true");
                    if (dx > dy && dx > (0 - dy)) {
                        Log.d("direction", "right");
                        return RIGHT;//right
                    } else if (dx < dy && dx < (0 - dy)) {
                        return LEFT;//left
                    } else if (dx < dy && dx > (0 - dy)) {
                        return BACK;//back
                    } else if (dx > dy && dx < (0 - dy)) {
                        return FRONT;//front
                    }
                }
            }
        }
        return NONE;
    }

    private float getR(int x, int y) {
        float dx = x - size / 2;
        float dy = y - size / 2;
        float r = (float) Math.sqrt(dx * dx + dy * dy);
        return r;
    }

    private void exeControlListener(int operation){
        if(mOnControlListener == null){
            return;
        }
        switch (operation){
            case CENTER://暂停启动
                if (isPause) {
                    mOnControlListener.onControlPause(this);
                } else {
                    mOnControlListener.onControlStart(this);
                }
                break;
            case RIGHT://right
                mOnControlListener.onControlRight(this);
                break;
            case LEFT://left
                mOnControlListener.onControlLeft(this);
                break;
            case BACK://back
                mOnControlListener.onControlBack(this);
                break;
            case FRONT://front
                mOnControlListener.onControlFront(this);
                break;
            default:
                break;
        }
    }

    private static final String INSTANCE = "instance";
    private static final String INSTANCE_ISPAUSE = "instance_isPause";

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(INSTANCE_ISPAUSE, isPause);
        bundle.putParcelable(INSTANCE, super.onSaveInstanceState());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            isPause = bundle.getBoolean(INSTANCE_ISPAUSE);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE));
            if (isPause) {
                setBackground(getResources().getDrawable(R.drawable.control_panel_pause));
            } else {
                setBackground(getResources().getDrawable(R.drawable.control_panel_start));
            }
            return;
        }
        super.onRestoreInstanceState(state);
    }

    public boolean isPause() {
        return isPause;
    }

    public void setOnControlListener(OnControlListener ctrl) {
        mOnControlListener = ctrl;
    }

    public  void setPause(){
        isPause = true;
        setBackground(getResources().getDrawable(R.drawable.control_panel_pause));
    }

    //处理点击事件接口
    public interface OnControlListener {
        abstract void onControlPause(ControlPanel cp);
        abstract void onControlStart(ControlPanel cp);
        abstract void onControlLeft(ControlPanel cp);
        abstract void onControlRight(ControlPanel cp);
        abstract void onControlFront(ControlPanel cp);
        abstract void onControlBack(ControlPanel cp);
    }



}
