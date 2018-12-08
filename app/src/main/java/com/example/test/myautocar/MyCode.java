package com.example.test.myautocar;

/**
 * Created by win10 on 2018/1/18.
 * 定义编码
 */

public class MyCode {
    //模式
    public static final byte MODE_REAL_TIME = 0x11;//实时模式
    public static final byte MODE_STEP = 0x12;//步进模式
    public static final byte MODE_TRACKING = 0x13;//循迹模式

    //操作
    public static final byte OPERATION_START = 0x5A;//启动
    public static final byte OPERATION_PAUSE = 0x5B;//停止
    public static final byte OPERATION_LEFT = 0x46;//左转
    public static final byte OPERATION_RIGHT = 0x42;//右转
    public static final byte OPERATION_FRONT = 0x41;//前进
    public static final byte OPERATION_BACK = 0x45;//后退

    //标记
    public static final byte FLAG_STEP_HEAD = 0x69;//
    public static final byte FLAG_STEP_END = 0x70;//

    public static final byte FLAG_ROW_HEAD = 0x71;//
    public static final byte FLAG_ROW_END = 0x72;//

    public static final byte FLAG_COL_HEAD = 0x73;//
    public static final byte FLAG_COL_END = 0x74;//

    public static final byte FLAG_WIDTH_HEAD = 0x75;//
    public static final byte FLAG_WIDTH_END = 0x76;//

    public static final byte FLAG_LENGTH_HEAD = 0x77;//
    public static final byte FLAG_LENGTH_END = 0x78;//

    //数据帧标记
    public static final byte FLAG_FRAME_HEAD = 0x20;//
    public static final byte FLAG_FRAME_END = 0x25;//

    public static final byte FLAG_FRAME_MODE_HEAD = 0x21;//
    public static final byte FLAG_FRAME_MODE_END = 0x22;//

    public static final byte FLAG_FRAME_OPERATION_HEAD = 0x23;//
    public static final byte FLAG_FRAME_OPERATION_END = 0x24;//

    public static byte[] pack(byte mode, byte[] operation) {
        if (operation == null) {
            return null;
        }
        int len = 7 + operation.length;
        byte[] dataFrame = new byte[len];
        dataFrame[0] = FLAG_FRAME_HEAD;
        dataFrame[1] = FLAG_FRAME_MODE_HEAD;
        dataFrame[2] = mode;
        dataFrame[3] = FLAG_FRAME_MODE_END;
        dataFrame[4] = FLAG_FRAME_OPERATION_HEAD;
        for (int i = 5; i < 5 + operation.length; i++) {
            dataFrame[i] = operation[i - 5];
        }
        dataFrame[len - 1] = FLAG_FRAME_END;
        dataFrame[len - 2] = FLAG_FRAME_OPERATION_END;
        return dataFrame;
    }
}
