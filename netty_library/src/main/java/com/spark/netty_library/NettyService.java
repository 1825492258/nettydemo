package com.spark.netty_library;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


import com.spark.netty_library.listener.SendMsgListener;
import com.spark.netty_library.message.SocketMessage;
import com.spark.netty_library.netty.ConnectListener;
import com.spark.netty_library.netty.NettyClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class NettyService extends Service implements ConnectListener {
    private HashMap<Integer, NettyClient> hashMap;
    private NettyClient nettyClient;
    public static final String TAG = NettyService.class.getName();
    private int channel; // 不同地址的socket
    private String ip;
    private int port;
    private SocketBinder sockerBinder = new SocketBinder();

    public NettyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sockerBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        hashMap = new HashMap<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            port = intent.getIntExtra("port", 0);
            ip = intent.getStringExtra("ip");
            channel = intent.getIntExtra("channel", 0);
            if (hashMap != null && hashMap.size() > 0) {
                nettyClient = hashMap.get(channel);
            }
            Log.d("tag", "ip==" + ip + ",,,port==" + port + ",,,size===" + hashMap.size());
            if (nettyClient == null) {
                nettyClient = NettyClient.init();
                nettyClient.setListener(this);
                hashMap.put(channel, nettyClient);
            }
            connect();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (hashMap == null || hashMap.size() == 0)
            return;
        for (NettyClient nettyClient : hashMap.values()) {
            nettyClient.setReconnectNum(0);
            nettyClient.disconnect();
        }
    }

    /**
     * 创建socket对象
     *
     * @param ip
     * @param port
     */
    private void connect() {
        if (!nettyClient.checkConnectStatus(ip, port)) {
            nettyClient.connect(ip, port); // 连接服务器
        }
    }

    /**
     * 发送数据
     *
     * @param socketMessage
     * @param sendMsgListener
     */
    public void sendData(SocketMessage socketMessage, SendMsgListener sendMsgListener) {
        int code = socketMessage.getChannel();
        if (hashMap != null && hashMap.get(code) != null) {
            NettyClient nettyClient = hashMap.get(code);
            nettyClient.sendMessage(socketMessage, sendMsgListener);
        } else {
            if (sendMsgListener != null) {
                sendMsgListener.error();
            }
        }
    }

    @Override
    public void onServiceStatusConnectChanged(int errorCode) {
        switch (errorCode) {
            case STATUS_CONNECT_SUCCESS:
                Log.i("NettyService", "连接成功");
                break;
            case STATUS_CONNECT_ERROR:
                Log.i("NettyService", "连接失败");
                break;
        }
    }

    /**
     * 返回NettyService 在需要的地方可以通过ServiceConnection获取到NettyService
     */
    public class SocketBinder extends Binder {
        public NettyService getService() {
            return NettyService.this;
        }
    }
}
