package com.example.test.myautocar;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.test.bluetooth.BluetoothService;
import com.example.test.setting.SettingActivity;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity {

    // 调试标记字符串
    private final String TAG = "MyAutoCar";
    //requestCode
    // 蓝牙打开
    private final int ENABLE_BLUETOOTH = 0;
    // 使蓝牙可见
    private final int DISCOVERY_REQUEST = 1;
    //步进
    private final int SETTING = 2;


    // 模式
    private final int MODE_STEP = SettingActivity.CONTROL_BY_STEP;// 自动步进
    private final int MODE_REAL_TIME = SettingActivity.CONTROL_BY_AUTO;// 实时遥控
    private final int MODE_TRACKING = SettingActivity.CONTROL_BY_TRACKING;//自动循迹
    private final int MODE_CHANGED = 2;// 模式改变

    BluetoothAdapter mBluetoothAdapter = null;
    BluetoothService mBluetoothService;

    private TextView tv;
    private TextView tv_mode;
    private ImageButton ib_setting;

    private MapViewPanel mMapViewPanel;
    private ControlPanel mControlPanel;

    //模式
    int mode;
    String str_mode;
    //行数
    private int rowNum;
    //列数
    private int colNum;
    //行宽
    private float rowWidth = 10.00f;
    //列长
    private float colLength = 10.00f;
    //步进
    private float width_step = 1.00f;
    private float length_step = 1.00f;

    private CarState mCarState;

    TextView tv_bt_state;

    private BluetoothStateBroadcast mBluetoothStateBroadcast;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case BluetoothService.SEND_MSG:
//                    Toast.makeText(MainActivity.this, msg.obj.toString(),
//                            Toast.LENGTH_SHORT).show();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                    String time = dateFormat.format(new Date());
                    Log.d("time", time);
                    tv.setText(time + ")已发送" + ((byte[]) msg.obj).length + "字节：" + bytes2HexStr((byte[]) msg.obj));
                    break;
                case BluetoothService.RECV_MSG:
                    // 获得远程设备发送的消息
                    break;
                case BluetoothService.STATE_CHANGED:
                    switch (mBluetoothService.getState()) {
                        case BluetoothService.STATE_CONNECTED:
                            tv_bt_state.setText("已连接");
                            break;
                        case BluetoothService.STATE_LISTENING:
                            tv_bt_state.setText("正在监听连接...");
                            break;
                        case BluetoothService.STATE_REQUESTING:
                            tv_bt_state.setText("正在请求连接...");
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            tv_bt_state.setText("正在连接...");
                            break;
                        case BluetoothService.STATE_NONE:
                            tv_bt_state.setText(R.string.str_bt_state_default);
                            break;
                        default:
                            tv_bt_state.setText(R.string.str_bt_state_default);
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        //full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        checkSdkVersion();
        init();

        mControlPanel.setOnControlListener(new ControlPanel.OnControlListener() {

            @Override
            public void onControlPause(ControlPanel cp) {
                if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
                    mControlPanel.setPause();
                    return;
                }
                mCarState.setCarMove(CarState.PAUSE);
                byte[] operation = {MyCode.OPERATION_PAUSE};
                byte[] msg = MyCode.pack(getModeCode(), operation);
                mBluetoothService.send(msg);//stop
            }

            @Override
            public void onControlStart(ControlPanel cp) {
                if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
                    mControlPanel.setPause();

                    return;
                }
                if (getMode() == MODE_TRACKING) {
                    Log.d("mode", mode + "");
                    int WheightBits2 = (int) (rowWidth * 100) / 100;
                    int WlowBits2 = (int) (rowWidth * 100) % 100;
                    int HheightBits2 = (int) (colLength * 100) / 100;
                    int HlowBits2 = (int) (colLength * 100) % 100;
                    byte[] operation = {MyCode.FLAG_ROW_HEAD, (byte) rowNum, MyCode.FLAG_ROW_END,
                            MyCode.FLAG_COL_HEAD, (byte) colNum, MyCode.FLAG_COL_END,
                            MyCode.FLAG_WIDTH_HEAD, (byte) WheightBits2, (byte) WlowBits2, MyCode.FLAG_WIDTH_END,
                            MyCode.FLAG_LENGTH_HEAD, (byte) HheightBits2, (byte) HlowBits2, MyCode.FLAG_LENGTH_END};
                    byte[] msg = MyCode.pack(getModeCode(), operation);
                    mBluetoothService.send(msg);//
                } else {
                    mCarState.setCarMove(CarState.START);
                    byte[] operation = {MyCode.OPERATION_START};
                    byte[] msg = MyCode.pack(getModeCode(), operation);
                    mBluetoothService.send(msg);//start
                }
            }

            @Override
            public void onControlLeft(ControlPanel cp) {
                if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
                    return;
                }
                if (getMode() == MODE_TRACKING) {
                    return;
                }
                if (getMode() == MODE_STEP) {
                    //小车当前位置
                    Point currentLocation = mMapViewPanel.getmPoint();
                    int x = currentLocation.x;
                    int y = currentLocation.y;
                    if (x == 0 && mCarState.getCarHeadDirection() == CarState.HEAD_FRONT) {
                        return;
                    }
                    if (x == colNum - 1 && mCarState.getCarHeadDirection() == CarState.HEAD_BACK) {
                        return;
                    }
                    if (y == 0 && mCarState.getCarHeadDirection() == CarState.HEAD_RIGHT) {
                        return;
                    }
                    if (y == rowNum - 1 && mCarState.getCarHeadDirection() == CarState.HEAD_LEFT) {
                        return;
                    }
                }
                mCarState.setCarMove(CarState.LEFT);
                byte[] operation = {MyCode.OPERATION_LEFT};
                byte[] msg = MyCode.pack(getModeCode(), operation);
                mBluetoothService.send(msg);//left
            }

            @Override
            public void onControlRight(ControlPanel cp) {
                if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
                    return;
                }
                if (getMode() == MODE_TRACKING) {
                    return;
                }
                if (getMode() == MODE_STEP) {
                    //小车当前位置
                    Point currentLocation = mMapViewPanel.getmPoint();
                    int x = currentLocation.x;
                    int y = currentLocation.y;
                    if (x == 0 && mCarState.getCarHeadDirection() == CarState.HEAD_BACK) {
                        return;
                    }
                    if (x == colNum - 1 && mCarState.getCarHeadDirection() == CarState.HEAD_FRONT) {
                        return;
                    }
                    if (y == 0 && mCarState.getCarHeadDirection() == CarState.HEAD_LEFT) {
                        return;
                    }
                    if (y == rowNum - 1 && mCarState.getCarHeadDirection() == CarState.HEAD_RIGHT) {
                        return;
                    }
                }
                mCarState.setCarMove(CarState.RIGHT);
                byte[] operation = {MyCode.OPERATION_RIGHT};
                byte[] msg = MyCode.pack(getModeCode(), operation);
                mBluetoothService.send(msg);//right
            }

            @Override
            public void onControlFront(ControlPanel cp) {
                if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
                    return;
                }
                if (getMode() == MODE_TRACKING) {
                    return;
                }
                if (getMode() == MODE_STEP) {
                    //小车当前位置
                    Point currentLocation = mMapViewPanel.getmPoint();
                    int x = currentLocation.x;
                    int y = currentLocation.y;
                    if (x == 0 && mCarState.getCarHeadDirection() == CarState.HEAD_LEFT) {
                        return;
                    }
                    if (x == colNum - 1 && mCarState.getCarHeadDirection() == CarState.HEAD_RIGHT) {
                        return;
                    }
                    if (y == 0 && mCarState.getCarHeadDirection() == CarState.HEAD_FRONT) {
                        return;
                    }
                    if (y == rowNum - 1 && mCarState.getCarHeadDirection() == CarState.HEAD_BACK) {
                        return;
                    }
                    mCarState.setCarMove(CarState.FRONT);
                    if (mCarState.getCarHeadDirection() == CarState.HEAD_BACK || mCarState.getCarHeadDirection() == CarState.HEAD_FRONT) {
                        //WIDTH
                        int heightBits2 = (int) (width_step * 100) / 100;
                        int lowBits2 = (int) (width_step * 100) % 100;
                        byte[] operation = {MyCode.OPERATION_FRONT, MyCode.FLAG_STEP_HEAD, (byte) heightBits2, (byte) lowBits2, MyCode.FLAG_STEP_END};
                        byte[] msg = MyCode.pack(getModeCode(), operation);
                        mBluetoothService.send(msg);//
                    } else if (mCarState.getCarHeadDirection() == CarState.HEAD_LEFT || mCarState.getCarHeadDirection() == CarState.HEAD_RIGHT) {
                        //LENGTH
                        int heightBits2 = (int) (length_step * 100) / 100;
                        int lowBits2 = (int) (length_step * 100) % 100;
                        byte[] operation = {MyCode.OPERATION_FRONT, MyCode.FLAG_STEP_HEAD, (byte) heightBits2, (byte) lowBits2, MyCode.FLAG_STEP_END};
                        byte[] msg = MyCode.pack(getModeCode(), operation);
                        mBluetoothService.send(msg);//
                    }

//                    mBluetoothService.send("前进" + str_step + "米");
                } else if (getMode() == MODE_REAL_TIME) {
                    byte[] operation = {MyCode.OPERATION_FRONT};
                    byte[] msg = MyCode.pack(getModeCode(), operation);
                    mBluetoothService.send(msg);//
                }
            }

            @Override
            public void onControlBack(ControlPanel cp) {
                if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
                    return;
                }
                if (getMode() == MODE_TRACKING) {
                    return;
                }
                if (getMode() == MODE_STEP) {
                    //小车当前位置
                    Point currentLocation = mMapViewPanel.getmPoint();
                    int x = currentLocation.x;
                    int y = currentLocation.y;
                    if (x == 0 && mCarState.getCarHeadDirection() == CarState.HEAD_RIGHT) {
                        return;
                    }
                    if (x == colNum - 1 && mCarState.getCarHeadDirection() == CarState.HEAD_LEFT) {
                        return;
                    }
                    if (y == 0 && mCarState.getCarHeadDirection() == CarState.HEAD_BACK) {
                        return;
                    }
                    if (y == rowNum - 1 && mCarState.getCarHeadDirection() == CarState.HEAD_FRONT) {
                        return;
                    }
                    mCarState.setCarMove(CarState.BACK);
                    if (mCarState.getCarHeadDirection() == CarState.HEAD_BACK || mCarState.getCarHeadDirection() == CarState.HEAD_FRONT) {
                        //WIDTH
                        int heightBits2 = (int) (width_step * 100) / 100;
                        int lowBits2 = (int) (width_step * 100) % 100;
                        byte[] operation = {MyCode.OPERATION_BACK, MyCode.FLAG_STEP_HEAD, (byte) heightBits2, (byte) lowBits2, MyCode.FLAG_STEP_END};
                        byte[] msg = MyCode.pack(getModeCode(), operation);
                        mBluetoothService.send(msg);//
                    } else if (mCarState.getCarHeadDirection() == CarState.HEAD_LEFT || mCarState.getCarHeadDirection() == CarState.HEAD_RIGHT) {
                        //LENGTH
                        int heightBits2 = (int) (length_step * 100) / 100;
                        int lowBits2 = (int) (length_step * 100) % 100;
                        byte[] operation = {MyCode.OPERATION_BACK, MyCode.FLAG_STEP_HEAD, (byte) heightBits2, (byte) lowBits2, MyCode.FLAG_STEP_END};
                        byte[] msg = MyCode.pack(getModeCode(), operation);
                        mBluetoothService.send(msg);//
                    }
                    //mBluetoothService.send("后退" + str_step + "米");
                } else if (getMode() == MODE_REAL_TIME) {
                    byte[] operation = {MyCode.OPERATION_BACK};
                    byte[] msg = MyCode.pack(getModeCode(), operation);
                    mBluetoothService.send(msg);//
                }
            }
        });

        ib_setting = (ImageButton) findViewById(R.id.bt_setting);
        ib_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(MainActivity.this, ib_setting);
                popup.getMenuInflater().inflate(R.menu.menu, popup.getMenu());
                //setIconsVisible(popup.getMenu(),true);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.item_connection_request:
                                Log.d(TAG, "get bonded device");
                                Set<BluetoothDevice> bondedDevice = mBluetoothAdapter
                                        .getBondedDevices();
                                final Map<String, BluetoothDevice> map = new HashMap<String, BluetoothDevice>();
                                ArrayList<String> arrayList = new ArrayList<String>();
                                for (BluetoothDevice device : bondedDevice) {
                                    arrayList.add(device.getName());
                                    map.put(device.getName(), device);
                                }
                                Log.d(TAG, "adapter");
                                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                        MainActivity.this, android.R.layout.simple_list_item_1,
                                        arrayList);
                                Log.d(TAG, "dialog");
                                AlertDialog.Builder dialog = new AlertDialog.Builder(
                                        MainActivity.this);
                                dialog.setTitle(R.string.str_connection_selection);
                                dialog.setCancelable(false);
                                dialog.setSingleChoiceItems(adapter, 0,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                mBluetoothService.connect(map.get(adapter
                                                        .getItem(which)));
                                                dialog.dismiss();
                                            }
                                        });
                                dialog.setPositiveButton(R.string.str_check_cancel,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        });
                                dialog.show();
                                return true;
                            case R.id.item_connection_listen:
                                mBluetoothService.accept();
                                return true;
                            case R.id.item_setting_param:
                                mCarState.setCarMove(CarState.PAUSE);
                                mControlPanel.setPause();
                                byte[] operation = {MyCode.OPERATION_PAUSE};
                                byte[] msg = MyCode.pack(getModeCode(), operation);
                                if (mBluetoothService != null && mBluetoothService.getState() == BluetoothService.STATE_CONNECTED) {
                                    mBluetoothService.send(msg);//stop
                                }
                                startActivityForResult(new Intent(MainActivity.this, SettingActivity.class), SETTING);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popup.show();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        getSetting(SettingActivity.FILE_NAME);
    }

    @Override
    protected void onDestroy() {
        mBluetoothService.closeService();
        unregisterReceiver(mBluetoothStateBroadcast);
        super.onDestroy();
    }

    private void checkSdkVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return;
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(
                    MainActivity.this);
            dialog.setTitle(R.string.str_warn);
            dialog.setMessage(R.string.str_warn_message_low_sdk);
            dialog.setCancelable(false);
            dialog.setPositiveButton(R.string.str_check_ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            dialog.show();
        }
    }

    private void init() {
        tv_bt_state = (TextView) findViewById(R.id.tv_bt_state);

        tv_mode = (TextView) findViewById(R.id.tv_mode);
        tv = (TextView) findViewById(R.id.tv_info);
        tv.setMovementMethod(ScrollingMovementMethod.getInstance());

        mMapViewPanel = (MapViewPanel) findViewById(R.id.my_map);
        mControlPanel = (ControlPanel) findViewById(R.id.control_panel);

        getSetting(SettingActivity.FILE_NAME);

        mCarState = new CarState(MainActivity.this, mMapViewPanel
                , (ImageView) findViewById(R.id.img_carhead_direction));

        // 蓝牙初始化
        initBluetooth();
        startDiscovery();
        mBluetoothService.accept();

        mBluetoothStateBroadcast = new BluetoothStateBroadcast();
        registerReceiver(mBluetoothStateBroadcast, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    // 设置模式
    private synchronized void setMode(int mode) {
        this.mode = mode;
    }

    // 获得当前模式
    private synchronized int getMode() {
        return mode;
    }

    //获取当前模式编码
    private byte getModeCode() {
        switch (getMode()) {
            case MODE_REAL_TIME:
                return MyCode.MODE_REAL_TIME;
            case MODE_STEP:
                return MyCode.MODE_STEP;
            case MODE_TRACKING:
                return MyCode.MODE_TRACKING;
            default:
                return MyCode.MODE_STEP;
        }
    }

    //反射设置菜单图标可见
    private void setIconsVisible(Menu menu, boolean flag) {
        //判断menu是否为空
        if (menu != null) {
            try {
                //如果不为空,就反射拿到menu的setOptionalIconsVisible方法
                Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                //暴力访问该方法
                method.setAccessible(true);
                //调用该方法显示icon
                method.invoke(menu, flag);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    // 初始化蓝牙设备
    private void initBluetooth() {
        // 获取默认蓝牙适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // 蓝牙不可用
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "设备不支持蓝牙功能");
            finish();
        }
        Log.d(TAG, mBluetoothAdapter.getAddress());
        // 检测蓝牙是否打开
        if (!mBluetoothAdapter.isEnabled()) {
            // 提示用户打开蓝牙
            AlertDialog.Builder dialog = new AlertDialog.Builder(
                    MainActivity.this);
            dialog.setTitle(R.string.str_warn);
            dialog.setMessage(R.string.str_warn_message_bt_off);
            dialog.setCancelable(false);
            dialog.setPositiveButton(R.string.str_check_ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            dialog.show();
        }
        // 设置蓝牙可见性
        if (mBluetoothAdapter.isEnabled()
                && mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            startActivityForResult(new Intent(
                    BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE), DISCOVERY_REQUEST);
        }
        mBluetoothService = new BluetoothService(MainActivity.this,
                mHandler, mBluetoothAdapter);
        Log.d(TAG, "初始化完");
    }// 初始化完

    private Set<BluetoothDevice> devices = new HashSet();

    // 搜索设备
    private void startDiscovery() {
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String remoteDeviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                BluetoothDevice remoteDevice = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                devices.add(remoteDevice);
                Log.d(TAG, "discovered " + remoteDeviceName);
            }

        }, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        if (mBluetoothAdapter.isEnabled() && !mBluetoothAdapter.isDiscovering())
            devices.clear();
        mBluetoothAdapter.startDiscovery();
    }// 搜索完成

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCarState.setCarMove(CarState.PAUSE);
        switch (requestCode) {
            case ENABLE_BLUETOOTH:
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "蓝牙已打开");
                } else {
                    Log.d(TAG, "蓝牙未打开");
                    finish();
                }
                break;
            case DISCOVERY_REQUEST:
                if (resultCode == RESULT_CANCELED) {
                    Log.d(TAG, "用户取消设置蓝牙可见性");
                }
                break;
            case SETTING:
                if (data == null) {
                    break;
                }
                mode = data.getIntExtra("controlMode", MODE_STEP);
                str_mode = data.getStringExtra("mode_string");
                rowNum = data.getIntExtra("array_row", 10);
                colNum = data.getIntExtra("array_col", 10);
                rowWidth = data.getFloatExtra("area_width", 10.00f);
                colLength = data.getFloatExtra("area_length", 10.00f);
                width_step = data.getFloatExtra("width_step", 1.00f);
                length_step = data.getFloatExtra("length_step", 1.00f);
                setMode(data.getIntExtra("controlMode", MODE_STEP));
                if (str_mode != null) {
                    tv_mode.setText(getResources().getString(R.string.str_mode_title) + str_mode);
                }
                Log.d(TAG, "onActivityResult\n模式编码mode：" + mode + "\n模式str_mode：" + str_mode + "\n行数rowNum：" + rowNum
                        + "\n列数colNum：" + colNum + "\n行宽rowWidth：" + rowWidth + "\n列长colLength：" + colLength
                        + "\n纵向步进width_step：" + width_step + "\n行向步进length_step：" + length_step);
                break;
            default:
                break;
        }
    }//

    private void getSetting(String fileName) {
//        return;
//        if (!isFileExist(fileName)) {
//            return;
//        }

        SharedPreferences pref = getSharedPreferences(fileName, MODE_PRIVATE);

        mode = pref.getInt("controlMode", MODE_STEP);
        str_mode = pref.getString("et_select_mode", "自动步进");
        rowNum = Integer.parseInt(pref.getString("et_array_row", "10"));
        colNum = Integer.parseInt(pref.getString("et_array_col", "10"));
        rowWidth = Float.parseFloat(pref.getString("et_area_width", "10.00"));
        colLength = Float.parseFloat(pref.getString("et_area_length", "10.00"));
        width_step = Float.parseFloat(pref.getString("et_select_width_step", "1.00"));
        length_step = Float.parseFloat(pref.getString("et_select_length_step", "1.00"));
        tv_mode.setText(getResources().getString(R.string.str_mode_title) + str_mode);
        Log.d(TAG, "getSetting\n模式编码mode：" + mode + "\n模式str_mode：" + str_mode + "\n行数rowNum：" + rowNum
                + "\n列数colNum：" + colNum + "\n行宽rowWidth：" + rowWidth + "\n列长colLength：" + colLength
                + "\n纵向步进width_step：" + width_step + "\n行向步进length_step：" + length_step);
        if (rowNum == mMapViewPanel.getRowNum() && colNum == mMapViewPanel.getColNum()) {
            return;
        }
        mMapViewPanel.setRowNum(rowNum);
        mMapViewPanel.setColNum(colNum);
        mMapViewPanel.clearAllCompletedMeasurePoint();
        mMapViewPanel.setmPoint(new Point(colNum - 1, rowNum - 1));
        mCarState = new CarState(MainActivity.this, mMapViewPanel
                , (ImageView) findViewById(R.id.img_carhead_direction));
        mControlPanel.setPause();

    }

    private boolean isFileExist(String fileName) {
        SharedPreferences pref = getSharedPreferences(fileName, MODE_PRIVATE);
        if (pref.getString(fileName, "") == "") {
            return false;
        }
        return true;
    }

    private String bytes2HexStr(byte[] bytes) {
        String str = "";
        if (bytes == null || bytes.length == 0) {
            return str;
        }
        int len = bytes.length;
        for (int i = 0; i < len; i++) {
            str = str + String.format("%02x", bytes[i]).toUpperCase() + "  ";
        }
        return str;
    }

    class BluetoothStateBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE, 0);
            if (state == BluetoothAdapter.STATE_ON) {
                Log.d(TAG, "state on");
            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(
                        MainActivity.this);
                dialog.setTitle(R.string.str_warn);
                dialog.setMessage(R.string.str_warn_message_bt_off);
                dialog.setCancelable(false);
                dialog.setPositiveButton(R.string.str_check_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                dialog.show();
                Log.d(TAG, "state not on");
            }
        }
    }
}
