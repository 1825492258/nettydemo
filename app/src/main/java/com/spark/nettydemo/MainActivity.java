package com.spark.nettydemo;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.spark.netty_library.NettyService;
import com.spark.netty_library.listener.SendMsgListener;
import com.spark.netty_library.message.SocketMessage;
import com.spark.netty_library.message.SocketResponse;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private ServiceConnection sc;
    private NettyService nettyService;
    private int count = 0;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gson = new Gson();
        initData();
        bindSocketService();
        TextView tvClick = findViewById(R.id.tvClick);
        tvClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (count == 0) {
                    byte[] bytes = gson.toJson(getMAP("CFD")).getBytes();
                    nettyService.sendData(new SocketMessage(0, (short) 23013, bytes), new SendMsgListener() {
                        @Override
                        public void onMessageResponse(SocketResponse socketResponse) {
                            Log.i("tag", "CFD成功" + "channel==" + socketResponse.getChannel());
                        }

                        @Override
                        public void error() {

                        }
                    });
                } else if (count == 1) {
                    byte[] bytes = gson.toJson(getMAP("SPOT")).getBytes();
                    nettyService.sendData(new SocketMessage(1, (short) 23013, bytes), sendMsgListener2);
                }
                count++;
            }
        });
    }

    private void initData() {
        Intent intent1 = new Intent();
        intent1.setClass(this, NettyService.class);
        intent1.putExtra("ip", "47.75.255.12");
        intent1.putExtra("port", 28903);
        intent1.putExtra("channel", 0);
        startService(intent1);

        Intent intent2 = new Intent();
        intent2.setClass(this, NettyService.class);
        intent2.putExtra("ip", "192.168.2.222");
        intent2.putExtra("port", 28903);
        intent2.putExtra("channel", 1);
        startService(intent2);
    }

    private void bindSocketService() {
        /*通过binder拿到service*/
        sc = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                NettyService.SocketBinder binder = (NettyService.SocketBinder) iBinder;
                nettyService = binder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        Intent intent = new Intent(getApplicationContext(), NettyService.class);
        bindService(intent, sc, BIND_AUTO_CREATE);
    }

    public static HashMap<String, String> getMAP(String service) {
        HashMap<String, String> map = new HashMap<>();
        map.put("service", service);
        return map;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        byte[] bytes1 = gson.toJson(getMAP("CFD")).getBytes();
        nettyService.sendData(new SocketMessage(0, (short) 23014, bytes1), sendMsgListener1);
        byte[] bytes2 = gson.toJson(getMAP("SPOT")).getBytes();
        nettyService.sendData(new SocketMessage(1, (short) 23014, bytes2), sendMsgListener2);
        unbindService(sc);
    }

    SendMsgListener sendMsgListener1 = new SendMsgListener() {
        @Override
        public void onMessageResponse(SocketResponse socketResponse) {
            Log.i("tag", "CFD成功" + "channel==" + socketResponse.getChannel());
        }

        @Override
        public void error() {

        }
    };

    SendMsgListener sendMsgListener2 = new SendMsgListener() {
        @Override
        public void onMessageResponse(SocketResponse socketResponse) {
            Log.i("tag", "SPOT成功" + "channel==" + socketResponse.getChannel());
        }

        @Override
        public void error() {

        }
    };
}