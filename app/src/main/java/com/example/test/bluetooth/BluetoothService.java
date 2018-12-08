package com.example.test.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.os.Handler;

/**
 * Created by win10 on 2018/1/20.
 * 自定义个性化蓝牙服务
 */

public class BluetoothService {

    private final String TAG = "BluetoothService";

    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private String name = "BluetoothService";

    //定义状态常量
    public static final int STATE_NONE = 0;//未知
    public static final int STATE_CONNECTED = 1;//已连接
    public static final int STATE_LISTENING = 2;//正在监听连接
    public static final int STATE_REQUESTING = 3;//正在请求连接
    public static final int STATE_CONNECTING = 4;//正在连接
    public static final int STATE_CHANGED = 5;//状态改变

    // 接收和发送消息
    public static final int RECV_MSG = 0;
    public static final int SEND_MSG = 1;

    private int state = STATE_NONE;

    //蓝牙服务相关的类
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothServerSocket serverSocket;
    private BluetoothSocket socket;
    private ConnectionStateBroadcast mConnectionStateBroadcast;
    private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };

    //线程
    AcceptThread mAcceptThread;//监听连接线程
    RequestThread mRequestThread;//请求连接线程
    ManageSocketThread mManageSocketThread;//管理发送接收消息线程

    private Context mContext;
    private final Handler mHandler;
    private byte[] buffer;

    //构造方法
    public BluetoothService(Context mContext, Handler mHandler, BluetoothAdapter mBluetoothAdapter) {
        buffer = new byte[1024];
        this.mContext = mContext;
        this.mHandler = mHandler;
        this.mBluetoothAdapter = mBluetoothAdapter;
        mConnectionStateBroadcast = new ConnectionStateBroadcast();
        mContext.registerReceiver(mConnectionStateBroadcast, new IntentFilter(
                BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED));
    }

    public void disConnect() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (serverSocket!=null){
                serverSocket.close();
            }
            if (mBluetoothGatt != null) {
                mBluetoothGatt.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mAcceptThread != null) {
            mAcceptThread.interrupt();
            mAcceptThread = null;
        }
        if (mRequestThread != null) {
            mRequestThread.interrupt();
            mRequestThread = null;
        }
        if (mManageSocketThread != null) {
            mManageSocketThread.interrupt();
            mManageSocketThread.close();
            mManageSocketThread = null;
        }
    }

    public synchronized void accept() {
        initService();
        setState(STATE_LISTENING);
        notifyStateChanged();
        disConnect();
        mAcceptThread = new AcceptThread();
        mAcceptThread.start();
    }

    public synchronized void connect(BluetoothDevice device) {
        initService();
        setState(STATE_REQUESTING);
        notifyStateChanged();
        disConnect();
        mRequestThread = new RequestThread(device);
        mRequestThread.start();
    }

    public void closeService() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (mBluetoothGatt != null) {
                mBluetoothGatt.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mContext.unregisterReceiver(mConnectionStateBroadcast);
        if (mAcceptThread != null) {
            mAcceptThread.interrupt();
            mAcceptThread = null;
        }
        if (mRequestThread != null) {
            mRequestThread.interrupt();
            mRequestThread = null;
        }
        if (mManageSocketThread != null) {
            mManageSocketThread.interrupt();
            mManageSocketThread.close();
            mManageSocketThread = null;
        }
    }

//    public boolean send(int msg) {
//        return mManageSocketThread.write(msg);
//    }

    public boolean send(String msg) {
        return mManageSocketThread.write(msg);
    }

    public boolean send(byte[] bytes) {
        return mManageSocketThread.write(bytes);
    }

    public byte[] read() {
        return buffer;
    }

    public synchronized int getState() {
        return state;
    }


    // 初始化蓝牙服务
    private void initService() {
        if (mAcceptThread != null && mAcceptThread.isAlive()) {
            mAcceptThread = null;
        }
        if (mRequestThread != null && mRequestThread.isAlive()) {
            mRequestThread = null;
        }
        if (mManageSocketThread != null && mManageSocketThread.isAlive()) {
            mManageSocketThread = null;
        }
        if (socket != null) {
            try {
                socket.close();
                socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "初始化service完成");
    }

    private synchronized void setState(int state) {
        this.state = state;
        Log.d(TAG,"state = " + state);
    }

    private void handlerMsg(int what, Object obj) {
        mHandler.obtainMessage(what, obj).sendToTarget();
    }

    private void handlerMsg(int what, int arg1, Object obj) {
        mHandler.obtainMessage(what, arg1, -1, obj).sendToTarget();
    }

    private void notifyStateChanged() {
        handlerMsg(STATE_CHANGED, "");
    }

    private void startManageSocket(BluetoothSocket socket) {
        mManageSocketThread = new ManageSocketThread(socket);
        mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, mBluetoothGattCallback);
        mManageSocketThread.start();
    }

    private class AcceptThread extends Thread {
        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
                        name, uuid);
            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
            }
            serverSocket = tmp;
            Log.d(TAG, "serverSocket construction finish");
        }

        public void run() {
            Log.d(TAG, "AcceptThread run");
            setName("AcceptThread");
            if (serverSocket != null) {
                while (true) {
                    try {
                        Log.d(TAG, "waiting for accept");
                        setState(STATE_LISTENING);
                        notifyStateChanged();
                        socket = serverSocket.accept();
                        mBluetoothDevice = socket.getRemoteDevice();
                        Log.d(TAG, "accept");
                        setState(STATE_CONNECTED);
                        notifyStateChanged();
                        startManageSocket(socket);
                        serverSocket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "fail to accept");
                        try {
                            serverSocket.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        setState(STATE_NONE);
                        notifyStateChanged();
                        Log.d(TAG, "reAcceptThread");
                        return;
                    }
                    break;
                }
            }
        }
    }

    private class RequestThread extends Thread {
        public RequestThread(BluetoothDevice device) {
            mBluetoothDevice = device;
            try {
                socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                Log.d(TAG, "clientSocket construction finish");
            } catch (IOException e) {
                Log.e(TAG, "create() fail");
                e.printStackTrace();
            }
        }

        @SuppressLint("NewApi")
        public void run() {
            Log.d(TAG, "RequestThread run");
            setName("RequestThread");
            mBluetoothAdapter.cancelDiscovery();
            if (socket != null) {
                try {
                    Log.d(TAG, "waiting for connect");
                    setState(STATE_REQUESTING);
                    notifyStateChanged();
                    socket.connect();
                    setState(STATE_CONNECTED);
                    notifyStateChanged();
                    startManageSocket(socket);
                    Log.d(TAG, "connect");
                } catch (IOException e) {
                    setState(STATE_NONE);
                    notifyStateChanged();
                    Log.e(TAG, "fail to connect");
                }
            }
        }
    }

    private class ManageSocketThread extends Thread {
        BluetoothSocket socket = null;
        OutputStream output = null;
        InputStream input = null;
        int count = 0;

        public ManageSocketThread(BluetoothSocket socket) {
            this.socket = socket;
            if (socket != null) {
                try {
                    output = socket.getOutputStream();
                    Log.d(TAG, "getOutputStream()");
                } catch (IOException e) {
                    Log.e(TAG, "server getOutputStream() fail", e);
                }
                try {
                    input = socket.getInputStream();
                    Log.d(TAG, "getInputStream()");
                } catch (IOException e) {
                    Log.e(TAG, "server getInputStream() fail", e);
                }
                Log.d(TAG, "get IOStream finish");
            } else {
                Log.d(TAG, "socket is null, can not get IOStream");
            }
            Log.d(TAG, "ManageSocket construction finish");
        }//

//        public boolean write(int msg) {
//            if (msg < 0)
//                return false;
//            try {
//                output.write(msg);
//                Log.d(TAG, "Write:" + msg);
//            } catch (IOException e) {
//                Log.e(TAG, "write() Exception", e);
//                return false;
//            }
//            Log.d(TAG, "handlermsg");
//            handlerMsg(SEND_MSG, ((Integer)msg).);
//            Log.d(TAG, "handlermsg END");
//            return true;
//        }//

        public boolean write(String msg) {
            if (msg == null)
                return false;
            try {
                output.write((msg).getBytes());
                //		bw.flush();
                Log.d(TAG, "Write:" + msg);
            } catch (IOException e) {
                Log.e(TAG, "write() Exception", e);
                return false;
            }
            Log.d(TAG, "handlermsg");
            handlerMsg(SEND_MSG, msg.getBytes());
            Log.d(TAG, "handlermsg END");
            return true;
        }//

        public boolean write(byte[] bytes) {
            if (bytes == null || bytes.length == 0)
                return false;
            try {
                output.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write() Exception", e);
                return false;
            }
            Log.d(TAG, "handlermsg");
            handlerMsg(SEND_MSG, bytes);
            Log.d(TAG, "handlermsg END");
            return true;
        }//

        public void close() {
            try {
                if (input != null) {
                    input.close();
                }
                if (output != null) {
                    output.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @SuppressLint("NewApi")
        public void run() {
            Log.d(TAG, "ManageSocket run");
            //	BufferedReader br = new BufferedReader(new InputStreamReader(input));
            while (true) {
                try {
                    // 读取远程设备的一行数据
                    count = input.read(buffer);
                    Log.d(TAG, "2收到：" + buffer.toString());
                    if (buffer == null)
                        continue;
                    if (mHandler == null) {
                        Log.d(TAG, "mHandler is null");
                        return;
                    }
                    handlerMsg(RECV_MSG, count, buffer);
                } catch (IOException e) {
                    Log.e(TAG, "readline Exception", e);
                    break;
                }
            }
        }//
    }

    class ConnectionStateBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "State Broadcast");
            int state = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_CONNECTION_STATE, 0);
            if (state == BluetoothAdapter.STATE_CONNECTED) {
                setState(STATE_CHANGED);
                notifyStateChanged();
                Log.d(TAG, "state connected");
            } else if (state == BluetoothAdapter.STATE_CONNECTING) {
                setState(STATE_CONNECTING);
                notifyStateChanged();
            } else {
//                setState(STATE_NONE);
//                notifyStateChanged();
            }
        }
    }//
}
