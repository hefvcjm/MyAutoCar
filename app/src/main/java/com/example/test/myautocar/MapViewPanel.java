package com.example.test.myautocar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by hefvcjm on 17-3-7.
 */

public class MapViewPanel extends View implements Observer {

    //默认行列数
    private static final int DEFAULT_ROWNUM = 10;//默认行数
    private static final int DEFAULT_COLNUM = 10;//默认列数
    //默认控件宽高，用于wrap_content属性
    private static final int DEFAULT_WIDTH = 400;
    private static final int DEFUALT_HEIGHT = 400;
    //放缩功能时最大间距
    private static final float MAX_SPACE = 200.0f;

    //地图实际宽度
    private float mapWidth;
    //地图实际高度
    private float mapHeight;
    //地图宽度
    private int mapViewWidth;
    //地图高度
    private int mapViewHeight;
    //放缩后地图宽度
    private int mScaleMapViewWidth;
    //放缩后地图高度
    private int mScaleMapViewHeight;
    //行数
    private int rowNum = DEFAULT_ROWNUM;
    //列数
    private int colNum = DEFAULT_COLNUM;
    //行高
    private float rowHeight;
    //列宽
    private float colWidth;
    //放缩后的行高
    private float mScaleRowHeight;
    //放缩后的列宽
    private float mScaleColWidth;
    //定位图标大小比例
    private float locationIconRatio = 0.8f;
    //定位图标宽高
    private float locationIconSize;
    //完成测量图标宽高
    private float completedMeasureIconSize;
    //用于调整顶部距离
    private float adjust;
    //宽高比例
    private double ratioH2W;
    //放缩
    private ScaleInfo mScaleInfo;

    //画笔
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //定位图标
    private Bitmap locationIcon;
    //完成测量图标
    private Bitmap completedMeasureIcon;
    //定位点坐标
    private Point mPoint;
    //观察设置变化
    private MapViewObservable mMyObservable;
    //记录完成测量的点
    private ArrayList<Point> completedMeasure;

    private CarState.CarStateObservable mCarStateObservabl;

    public MapViewPanel(Context context) {
        super(context);
        init();
    }

    public MapViewPanel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MapViewPanel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    //初始化
    private void init() {
        //抗锯齿
        mPaint.setAntiAlias(true);
        //抗抖动
        mPaint.setDither(true);
        mPaint.setColor(0xffffffff);
        // mPaint.setColor(0x77000000);
        //  setBackgroundColor(0x44ff0000);
        if (mMyObservable != null) {
            mMyObservable.deleteObserver(this);
        }
        mMyObservable = new MapViewObservable();
        mMyObservable.addObserver(this);
        mPoint = new Point(colNum - 1, rowNum - 1);
        completedMeasure = new ArrayList<Point>();

        locationIcon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_location);
        completedMeasureIcon = BitmapFactory.decodeResource(getResources(), R.drawable.done);

        mCarStateObservabl = new CarState.CarStateObservable();
        mScaleInfo = new ScaleInfo();
        mScaleInfo.addObserver(this);

    }

    @Override
    public void update(Observable o, Object arg) {
        if (mScaleInfo == null && mMyObservable == null) {
            return;
        }
        mScaleRowHeight = mScaleInfo.getScaleRowHeight();
        mScaleColWidth = mScaleInfo.getScaleColWidth();
        colWidth = mapViewWidth * 1.0f / colNum;
        rowHeight = mapViewHeight * 1.0f / rowNum;
        Log.d("update", "mapViewWidth=" + mapViewWidth);
        locationIconSize = (int) (Math.min(mScaleColWidth, mScaleRowHeight) * locationIconRatio);
        adjust = locationIconSize;
        mScaleRowHeight = (mScaleInfo.getScaleMapHeight() - adjust) * 2.0f / (rowNum * 2 - 1);
        completedMeasureIconSize = locationIconSize / 2;
        locationIcon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_location);
        completedMeasureIcon = BitmapFactory.decodeResource(getResources(), R.drawable.done);
        Log.d("update", "completedMeasureIcon");
        Log.d("update", "");
        invalidate();
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

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mapViewHeight = h;
        mapViewWidth = w;

        mScaleMapViewHeight = h;
        mScaleMapViewWidth = w;

        mScaleInfo.setSrc(0, 0, w, h);

        colWidth = mapViewWidth * 1.0f / colNum;
        rowHeight = mapViewHeight * 1.0f / rowNum;

        mScaleColWidth = colWidth;
        mScaleRowHeight = rowHeight;

        locationIconSize = (int) (Math.min(mScaleColWidth, mScaleRowHeight) * locationIconRatio);
        adjust = locationIconSize;
        completedMeasureIconSize = locationIconSize / 2;
        locationIcon = Bitmap.createScaledBitmap(locationIcon,
                (int) locationIconSize, (int) locationIconSize, false);
        completedMeasureIcon = Bitmap.createScaledBitmap(completedMeasureIcon,
                (int) completedMeasureIconSize, (int) completedMeasureIconSize, false);
    }


    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    float firstX = -1;
    float firstY = -1;
    float secondX = -1;
    float secondY = -1;
    boolean firstScale = true;
    boolean firstMove = true;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            //单指
            case MotionEvent.ACTION_DOWN:
                firstX = event.getX();
                firstY = event.getY();
                mode = DRAG;
                break;
            //双指
            case MotionEvent.ACTION_POINTER_DOWN:
                mode = ZOOM;
                break;
            // 手指放开
            case MotionEvent.ACTION_UP:
                mode = NONE;
                firstMove = true;
                firstScale = true;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = DRAG;
                firstMove = true;
                firstScale = true;
                break;
            // 单指滑动事件
            case MotionEvent.ACTION_MOVE:
                Log.d("onTouchEvent", "pointerCount=" + event.getPointerCount());
                if (mode == DRAG) {
                    if (!firstMove) {
                        mScaleInfo.setMove(event.getX() - firstX, event.getY() - firstY);
                    }
                    firstX = event.getX();
                    firstY = event.getY();
                    firstMove = false;
                } else if (mode == ZOOM) {
                    Log.d("onTouchEvent", "firstX=" + firstX + ",firstY=" + firstY + ",secondX=" + secondX + ",secondY=" + secondY);
                    if (!firstScale) {
                        mScaleInfo.setScale(firstX, firstY, secondX, secondY,
                                event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                    }
                    firstX = event.getX(0);
                    firstY = event.getY(0);
                    secondX = event.getX(1);
                    secondY = event.getY(1);
                    firstScale = false;
                }
                break;
            default:
                firstMove = true;
                firstScale = true;
                break;
        }
//        if (action == MotionEvent.ACTION_UP) {
//            int x = (int) event.getX();
//            int y = (int) event.getY();
//            Log.d("touch event", "x:" + x + "  y:" + y);
//            Point p = getValidPoint(x, y);
//            Log.d("p", "x:" + p.x + "  y:" + p.y);
//            if (mPoint.equals(p)) {
//                return false;
//            }
//            mPoint = p;
//            mCarStateObservabl.setPoint(mPoint);
//
//            Log.d("mPoint", "x:" + p.x + "  y:" + p.y);
//            invalidate();
//        }
        return true;
    }

    private Point getValidPoint(int x, int y) {
        Point point = mScaleInfo.getScaleViewCoordinate(x, y);
        int x1 = point.x > mScaleMapViewWidth ? (int) (mScaleMapViewWidth - mScaleColWidth / 2) : point.x;
        int y1 = (point.y - adjust) < 0 ? 0 : (int) (point.y - adjust);
        y1 = (int) ((y1 > (mScaleMapViewHeight - adjust) ?
                (mScaleMapViewHeight - adjust) : y1) + mScaleRowHeight / 2);
        return new Point((int) (x1 / mScaleColWidth), (int) (y1 / mScaleRowHeight));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawMap(canvas);
        drawCompletedMeasureIcon(canvas);
        drawLocationIcon(canvas);
    }

    private void drawLocationIcon(Canvas canvas) {
        locationIcon = Bitmap.createScaledBitmap(locationIcon,
                (int) locationIconSize, (int) locationIconSize, false);
        if (mPoint != null) {
            float rate = 16.5f / 18;
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setAntiAlias(true);
            p.setDither(true);
            p.setColor(0x44ff0000);
            canvas.drawCircle(mPoint.x * mScaleColWidth + mScaleColWidth / 2 + mScaleInfo.getxZero(),
                    mPoint.y * mScaleRowHeight + adjust + mScaleInfo.getyZero(),
                    locationIconSize / 3, p);
            canvas.drawBitmap(locationIcon,
                    mPoint.x * mScaleColWidth - (locationIconSize - mScaleColWidth) / 2 + mScaleInfo.getxZero(),
                    mPoint.y * mScaleRowHeight - adjust * rate + adjust + mScaleInfo.getyZero(), null);
            Log.d("locationIcon", "x:" + mPoint.x + "  y:" + mPoint.y);
        }
    }

    private void drawCompletedMeasureIcon(Canvas canvas) {
        completedMeasureIcon = Bitmap.createScaledBitmap(completedMeasureIcon,
                (int) completedMeasureIconSize, (int) completedMeasureIconSize, false);
        if (completedMeasure.size() != 0) {
            for (Point point : completedMeasure) {
                canvas.drawBitmap(completedMeasureIcon,
                        point.x * mScaleColWidth + mScaleColWidth / 2 - completedMeasureIconSize / 2 + mScaleInfo.getxZero(),
                        point.y * mScaleRowHeight + adjust - completedMeasureIconSize / 2 + mScaleInfo.getyZero(), null);
            }
        }
    }

    private void drawMap(Canvas canvas) {
        //重新调整行高
        mScaleRowHeight = (mScaleInfo.getScaleMapHeight() - adjust) * 2.0f / (rowNum * 2 - 1);
        rowHeight = (mapViewHeight - adjust) * 2.0f / (rowNum * 2 - 1);
        //画行
        for (int i = 0; i < rowNum; i++) {
            int startX = mScaleInfo.getxZero() + (int) (mScaleColWidth / 2);
            int endX = mScaleInfo.getxZero() + (int) (mScaleInfo.getScaleMapWidth() - (mScaleColWidth / 2));
            int y = mScaleInfo.getyZero() + (int) (i * mScaleRowHeight + adjust);
            canvas.drawLine(startX, y, endX, y, mPaint);
        }
        //画列
        for (int i = 0; i < colNum; i++) {
            int x = mScaleInfo.getxZero() + (int) ((0.5 + i) * mScaleColWidth);
            int startY = mScaleInfo.getyZero() + (int) (adjust);
            int endY = mScaleInfo.getyZero() + (int) (mScaleInfo.getScaleMapHeight() - (mScaleRowHeight / 2));
            canvas.drawLine(x, startY, x, endY, mPaint);
        }
        Log.d("map", "xZero=" + mScaleInfo.getyZero() + ",yZero=" + mScaleInfo.getyZero());
    }

    //设置行数
    public void setRowNum(int mRowNum) {
        if (rowNum != mRowNum) {
            rowNum = mRowNum;
            mMyObservable.setChange();
        }
    }

    //设置列数
    public void setColNum(int mColNum) {
        if (colNum != mColNum) {
            colNum = mColNum;
            mMyObservable.setChange();
        }
    }

    /**
     * 添加完成测量的点到completedMeasure中
     *
     * @param point 添加点
     */
    public void addCompletedMeasurePoint(Point point) {
        if (!completedMeasure.contains(point)) {
            completedMeasure.add(point);
            mMyObservable.setChange();
        }
    }

    /**
     * 添加完成测量的点到completedMeasure中
     *
     * @param point 删除点
     */
    public void deleteCompletedMeasurePoint(Point point) {
        if (completedMeasure.contains(point)) {
            completedMeasure.remove(point);
            mMyObservable.setChange();
        }
    }

    public void setmPoint(Point point) {
        if (!point.equals(mPoint)) {
            mPoint = point;
            mMyObservable.setChange();
        }
    }

    public int getRowNum() {
        return rowNum;
    }

    public int getColNum() {
        return colNum;
    }

    public Point getmPoint() {
        return mPoint;
    }


    /**
     * 清空所有完成测量的点
     */
    public void clearAllCompletedMeasurePoint() {
        completedMeasure.clear();
    }

    private static final String INSTANCE = "instance";
    private static final String INSTANCE_ROWNUM = "instance_rownum";
    private static final String INSTANCE_COLNUM = "instance_colnum";
    private static final String INSTANCE_MPOINT_X = "instance_mpoint_x";
    private static final String INSTANCE_MPOINT_Y = "instance_mpoint_y";

    //记得为自定义控件添加id，不然下面方法将会失效
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE, super.onSaveInstanceState());
        bundle.putInt(INSTANCE_COLNUM, colNum);
        bundle.putInt(INSTANCE_ROWNUM, rowNum);
        bundle.putInt(INSTANCE_MPOINT_X, mPoint.x);
        bundle.putInt(INSTANCE_MPOINT_Y, mPoint.y);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            rowNum = bundle.getInt(INSTANCE_ROWNUM, DEFAULT_ROWNUM);
            colNum = bundle.getInt(INSTANCE_COLNUM, DEFAULT_COLNUM);
            int x = bundle.getInt(INSTANCE_MPOINT_X, 0);
            int y = bundle.getInt(INSTANCE_MPOINT_Y, 0);
            mPoint = new Point(x, y);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    private class MapViewObservable extends Observable {

        private void setChange() {
            this.setChanged();
            this.notifyObservers();
        }
    }

    private class ScaleInfo extends Observable {
        //放缩后原点相对于view的坐标
        private int xZero;
        private int yZero;
        //原view对应的矩形
        private RectF src;
        //放缩后的矩形
        private RectF dst;
        //放缩后行高
        private float scaleRowHeight;
        //放缩后列宽
        private float scaleColWidth;
        //视点中心,相对不动的点
        private Point viewPoint;

        public ScaleInfo() {
            xZero = 0;
            yZero = 0;
            src = new RectF();
            dst = new RectF();
            viewPoint = new Point(mapViewWidth / 2, mapViewHeight / 2);
            Log.d("ScaleInfo", "dst.left=" + dst.left + ",dst.top=" + dst.top + ",dst.right=" + dst.right + ",dst.bottom=" + dst.bottom);
        }

        public void setSrc(float left, float top, float right, float bottom) {
            src.left = left;
            src.top = top;
            src.right = right;
            src.bottom = bottom;
            dst = src;
            Log.d("setSrc", "dst.left=" + dst.left + ",dst.top=" + dst.top + ",dst.right=" + dst.right + ",dst.bottom=" + dst.bottom);
        }

        public int getxZero() {
            return xZero;
        }

        public int getyZero() {
            return yZero;
        }

        public float getScaleRowHeight() {
            return getScaleMapHeight() / rowNum;
        }

        public float getScaleColWidth() {
            return getScaleMapWidth() / colNum;
        }

        public void setViewPoint(float x1, float y1, float x2, float y2) {
            viewPoint = new Point((int) ((x1 + x2) / 2), (int) ((y1 + y2) / 2));
            Log.d("setViewPoint", "x=" + viewPoint.x + ",y=" + viewPoint.y);

        }

        public Point getScaleViewCoordinate(int x, int y) {
            return new Point(x - xZero, y - yZero);
        }

        public float getScaleMapWidth() {
            return dst.right - dst.left;
        }

        public float getScaleMapHeight() {
            return dst.bottom - dst.top;
        }

        private void setScaleColWidth(float dx, float dy, float ndx, float ndy) {
            double distance = Math.sqrt(dx * dx + dy * dy);
            double nDistance = Math.sqrt(ndx * ndx + ndy * ndy);
            Log.d("setScaleColWidth", "distance=" + distance + ",nDistance=" + nDistance);
            double d = nDistance - distance;
            if (Math.abs(nDistance - distance) > 10.0f) {
                Log.d("setScaleColWidth", "true");
                if (colWidth < rowHeight) {
                    scaleColWidth = (float) (scaleColWidth + d / 3) > Math.max(colWidth, MAX_SPACE) ?
                            Math.max(colWidth, MAX_SPACE) : (float) (scaleColWidth + d / 3);
                    scaleColWidth = scaleColWidth < colWidth ? colWidth : scaleColWidth;
                } else {
                    scaleRowHeight = (float) (scaleRowHeight + d / 3) > Math.max(rowHeight, MAX_SPACE) ?
                            Math.max(rowHeight, MAX_SPACE) : (float) (scaleRowHeight + d / 3);
                    scaleRowHeight = scaleRowHeight < rowHeight ? rowHeight : scaleRowHeight;
                }
            }
            scaleColWidth = scaleColWidth < colWidth ? colWidth : scaleColWidth;
            scaleRowHeight = scaleRowHeight < rowHeight ? rowHeight : scaleRowHeight;
        }

        private void setDstRectF() {
            float dstWidth = scaleColWidth * colNum;
            float dstHeight = mapViewHeight * dstWidth / mapViewWidth;
            if (colWidth >= rowHeight) {
                dstHeight = scaleRowHeight * rowNum;
                dstWidth = mapViewWidth * dstHeight / mapViewHeight;
            }
            Log.d("setDstRectF", "dstWidth=" + dstWidth + ",dstHeight=" + dstHeight);
            float srcWidth = src.right - src.left;
            float srcHeight = src.bottom - src.top;
            Log.d("setDstRectF", "srcWidth=" + srcWidth + ",srcHeight=" + srcHeight + ",src.top=" + src.top);
            float l, t;
            l = (viewPoint.x - src.left) / srcWidth;
            t = (viewPoint.y - src.top) / srcHeight;
            dst.left = viewPoint.x - l * dstWidth;
            dst.top = viewPoint.y - t * dstHeight;
            if (dst.left > 0) {
                dst.left = 0;
            }
            if (dst.top > 0) {
                dst.top = 0;
            }
            Log.d("setDstRectF", "viewPoint.y - t * dstHeight=" + (viewPoint.y - t * dstHeight));
            Log.d("setDstRectF", "dst.top=" + dst.top);
            dst.right = dst.left + dstWidth;
            dst.bottom = dst.top + dstHeight;
            if (dst.right < mapViewWidth) {
                dst.right = mapViewWidth;
                dst.left = dst.right - dstWidth;
            }
            if (dst.bottom < mapViewHeight) {
                dst.bottom = mapViewHeight;
                dst.top = dst.bottom - dstHeight;
            }
            src = dst;
            Log.d("setDstRectF", "src.top=" + src.top);
        }

        private void setxZero() {
            xZero = (int) dst.left;
        }

        private void setyZero() {
            yZero = (int) dst.top;
        }

        public void setScale(float x1, float y1, float x2, float y2, float nx1, float ny1, float nx2, float ny2) {
            setViewPoint(nx1, ny1, nx2, ny2);
            Log.d("setScale", "setViewPoint");
            setScaleColWidth(x2 - x1, y2 - y1, nx2 - nx1, ny2 - ny1);
            Log.d("setScale", "setScaleColWidth");
            setDstRectF();
            Log.d("setScale", "setDstRectF");
            setxZero();
            Log.d("setScale", "setxZero");
            setyZero();
            Log.d("setScale", "setyZero");
            setChanged();
            notifyObservers();
            Log.d("setScale", "over");
        }

        public void setMove(float dx, float dy) {
//            dx = -dx;
//            dy = -dy;
//            Log.d("setMove", "dst.left=" + dst.left + ",dst.top=" + dst.top + ",dst.right=" + dst.right + ",dst.bottom=" + dst.bottom);
//            Log.d("setMove", "src.left=" + src.left + ",src.top=" + src.top + ",src.right=" + src.right + ",src.bottom=" + src.bottom);
            float dstWidth = src.right - src.left;
            float dstHeight = src.bottom - src.top;
            dst.left = src.left + dx > 0 ? 0 : (src.left + dx);
            dst.top = src.top + dy > 0 ? 0 : (src.top + dy);
            dst.right = dst.left + dstWidth;
            dst.bottom = dst.top + dstHeight;

            dst.right = dst.right < mapViewWidth ?
                    mapViewWidth : dst.right;
            dst.bottom = dst.bottom < mapViewHeight ?
                    mapViewHeight : dst.bottom;
            dst.left = dst.right - dstWidth;
            dst.top = dst.bottom - dstHeight;

            src = dst;
            setxZero();
            setyZero();
            Log.d("dxdy", "dx=" + dx + ",dy=" + dy);
            Log.d("setMove", "dst.left=" + dst.left + ",dst.top=" + dst.top + ",dst.right=" + dst.right + ",dst.bottom=" + dst.bottom);
            setChanged();
            notifyObservers();
        }
    }

}
