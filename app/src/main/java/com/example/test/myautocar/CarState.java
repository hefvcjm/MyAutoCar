package com.example.test.myautocar;

import android.content.Context;
import android.graphics.Point;
import android.widget.ImageView;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by hefvcjm on 17-3-27.
 */

public class CarState implements Observer {
    //
    //控制遥控小车方向标识字符串
    public static final int FRONT = 0;
    public static final int BACK = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;
    public static final int START = 4;
    public static final int PAUSE = 5;
    //车头方向标识
    public static final int HEAD_FRONT = 0;
    public static final int HEAD_BACK = 1;
    public static final int HEAD_LEFT = 2;
    public static final int HEAD_RIGHT = 3;

    //是否正在测量
    private static boolean isOnMeasure;
    //是否正在步进途中
    private static boolean isOnMove;
    //是否完成全部测量
    private static boolean isCompletedMeasure;
    //是否启动
    private static boolean isStarted;
    //车头方向
    private int carHeadDirection;

    //行数
    private int rowNum;
    //列数
    private int colNum;
    //定位点坐标
    private static Point mPoint;

    Context mContext;

    //车头方向
    ImageView carHead;
    MapViewPanel mMapViewPanel;

    CarStateObservable mCarStateObservable;

    public CarState(Context mContext, MapViewPanel mMapViewPanel, ImageView carHead) {

        this.mContext = mContext;
        this.mMapViewPanel = mMapViewPanel;
        this.carHead = carHead;
        rowNum = mMapViewPanel.getRowNum();
        colNum = mMapViewPanel.getColNum();
        mPoint = mMapViewPanel.getmPoint();

        carHeadDirection = HEAD_FRONT;

        isOnMeasure = false;
        isOnMove = false;
        isCompletedMeasure = false;
        isStarted = false;

        mCarStateObservable = new CarStateObservable();

        if (mCarStateObservable != null) {
            mCarStateObservable.deleteObserver(this);
        }
        mCarStateObservable.addObserver(this);

    }

    /**
     * 移动
     *
     * @param direction 小车移动方向
     */
    public void setCarMove(int direction) {
        int x = mPoint.x;
        int y = mPoint.y;
        if (isOnMove || isOnMeasure) {
            return;
        }
        if (!isStarted) {
            mCarStateObservable.setStarted(true);
            mCarStateObservable.setOnMeasure(true);
            return;
        }
//        if (getCarHeadDirection() == HEAD_FRONT) {
//            if (direction == FRONT) {
//                if (x > 0) {
//                    y = y - 1;
//                    mPoint = new Point(x, y);
//                    mMapViewPanel.setmPoint(mPoint);
//                    Log.d("FRONT", "front");
//                }
//            }
//        }
        //车头向前
        if (getCarHeadDirection() == HEAD_FRONT) {
            switch (direction) {
                case FRONT:
                    if (y > 0) {
                        y = y - 1;
                        mPoint = new Point(x, y);
                        mCarStateObservable.setOnMove(true);
                        mMapViewPanel.setmPoint(mPoint);
                        mCarStateObservable.setOnMeasure(true);
                    }
                    break;
                case BACK:
                    if (y < rowNum - 1) {
                        y = y + 1;
                        mPoint = new Point(x, y);
                        mCarStateObservable.setOnMove(true);
                        mMapViewPanel.setmPoint(mPoint);
                        mCarStateObservable.setOnMeasure(true);
                    }
                    break;
                case LEFT:
                        setHeadDirection(HEAD_LEFT);
                    break;
                case RIGHT:
                        setHeadDirection(HEAD_RIGHT);

                    break;
                default:
                    break;
            }
        }

        //车头向后
        else if (getCarHeadDirection() == HEAD_BACK) {
            switch (direction) {
                case FRONT:
                    if (y < rowNum - 1) {
                        y = y + 1;
                        mPoint = new Point(x, y);
                        mCarStateObservable.setOnMove(true);
                        mMapViewPanel.setmPoint(mPoint);
                        mCarStateObservable.setOnMeasure(true);
                    }
                    break;
                case BACK:
                    if (y > 0) {
                        y = y - 1;
                        mPoint = new Point(x, y);
                        mCarStateObservable.setOnMove(true);
                        mMapViewPanel.setmPoint(mPoint);
                        mCarStateObservable.setOnMeasure(true);
                    }
                    break;
                case LEFT:
                        setHeadDirection(HEAD_RIGHT);
                    break;
                case RIGHT:
                        setHeadDirection(HEAD_LEFT);
                    break;
                default:
                    break;
            }
        }
        //车头向左
        else if (getCarHeadDirection() == HEAD_LEFT) {
            switch (direction) {
                case FRONT:
                    if (x > 0) {
                        x = x - 1;
                        mPoint = new Point(x, y);
                        mCarStateObservable.setOnMove(true);
                        mMapViewPanel.setmPoint(mPoint);
                        mCarStateObservable.setOnMeasure(true);
                    }
                    break;
                case BACK:
                    if (x < colNum - 1) {
                        x = x + 1;
                        mPoint = new Point(x, y);
                        mCarStateObservable.setOnMove(true);
                        mMapViewPanel.setmPoint(mPoint);
                        mCarStateObservable.setOnMeasure(true);
                    }
                    break;
                case LEFT:
                        setHeadDirection(HEAD_BACK);
                    break;
                case RIGHT:
                        setHeadDirection(HEAD_FRONT);
                    break;
                default:
                    break;
            }
        }
        //车头向右
        else if (getCarHeadDirection() == HEAD_RIGHT) {
            switch (direction) {
                case FRONT:
                    if (x < colNum - 1) {
                        x = x + 1;
                        mPoint = new Point(x, y);
                        mCarStateObservable.setOnMove(true);
                        mMapViewPanel.setmPoint(mPoint);
                        mCarStateObservable.setOnMeasure(true);
                    }
                    break;
                case BACK:
                    if (x > 0) {
                        x = x - 1;
                        mPoint = new Point(x, y);
                        mCarStateObservable.setOnMove(true);
                        mMapViewPanel.setmPoint(mPoint);
                        mCarStateObservable.setOnMeasure(true);
                    }
                    break;
                case LEFT:
                        setHeadDirection(HEAD_FRONT);
                    break;
                case RIGHT:
                        setHeadDirection(HEAD_BACK);
                    break;
                default:
                    break;
            }
        }


    }

    /**
     * 获取当前测量完成状态
     *
     * @return true表示已经完成全部测量
     */
    public boolean isCompletedMeasure() {
        return isCompletedMeasure;
    }

    /**
     * 获取当前小车状态
     *
     * @return true表示正在移动
     */
    public boolean isOnMove() {
        return isOnMove;
    }

    /**
     * 获取当前测量状态
     *
     * @return true表示正在测量
     */
    public boolean isOnMeasure() {
        return isOnMeasure;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public Point getmPoint(){
        return mPoint;
    }

    public int getCarHeadDirection() {
        return carHeadDirection;
    }

    private void setHeadDirection(int direction) {
        switch (direction) {
            case HEAD_FRONT:
                carHead.setImageDrawable(mContext.getResources().
                        getDrawable(R.drawable.carhead_direction_front));
                carHeadDirection = HEAD_FRONT;
                break;
            case HEAD_BACK:
                carHead.setImageDrawable(mContext.getResources().
                        getDrawable(R.drawable.carhead_direction_back));
                carHeadDirection = HEAD_BACK;
                break;
            case HEAD_LEFT:
                carHead.setImageDrawable(mContext.getResources().
                        getDrawable(R.drawable.carhead_direction_left));
                carHeadDirection = HEAD_LEFT;
                break;
            case HEAD_RIGHT:
                carHead.setImageDrawable(mContext.getResources().
                        getDrawable(R.drawable.carhead_direction_right));
                carHeadDirection = HEAD_RIGHT;
                break;
            default:
                break;
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (isOnMove()) {
            isOnMove = false;
        }
        if (isOnMeasure()) {
            mMapViewPanel.addCompletedMeasurePoint(mPoint);
            isOnMeasure = false;
        }
    }

    public static class CarStateObservable extends Observable {
        /**
         * 设置测量状态
         *
         * @param b true表示正在测量
         */

        public void setOnMeasure(boolean b) {
            isOnMeasure = b;
            setChanged();
            notifyObservers();
        }

        /**
         * 设置小车状态
         *
         * @param b true表示正在移动
         */
        public void setOnMove(boolean b) {
            isOnMove = b;
            setChanged();
            notifyObservers();
        }

        /**
         * 设置是否完成测量
         * @param b true表示已经完成测量
         */
        public void setCompletedMeasure(boolean b) {
            isCompletedMeasure = b;
        }

        public void setStarted(boolean b) {
            isStarted = b;
        }

        public void setPoint(Point point){
            if(!mPoint.equals(point)){
                mPoint = point;
                setChanged();
                notifyObservers();
            }
        }
    }
}
