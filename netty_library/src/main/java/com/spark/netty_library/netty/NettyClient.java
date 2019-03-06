package com.spark.netty_library.netty;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.spark.netty_library.listener.SendMsgListener;
import com.spark.netty_library.message.SocketMessage;
import com.spark.netty_library.message.SocketResponse;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.HashMap;

import static com.spark.netty_library.netty.ConnectListener.STATUS_CONNECT_ERROR;
import static com.spark.netty_library.netty.ConnectListener.STATUS_CONNECT_SUCCESS;


/**
 * Created by daiyy on 20190121.
 */

public class NettyClient {
    private static final String TAG = "netty";
    private ConnectListener listener;
    private SendMsgListener sendMsgListener;
    private int reconnectNum = 0;
    private long reconnectIntervalTime = 5000;
    private final Gson gson;
    private Socket socket;
    private String ip;
    private int port;
    private DataInputStream dis;
    private DataOutputStream dos;
    private SocketThread socketThread;
    private boolean isConnect;
    private int channel;

    public NettyClient() {
        gson = new Gson();
    }

    public static NettyClient init() {
        return new NettyClient();
    }

    /**
     * 连接tcp
     *
     * @return
     */
    public synchronized NettyClient connect(final String ip, final int port) {
        this.ip = ip;
        this.port = port;
        if (socket == null || !socket.isConnected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket = new Socket(ip, port);
                        dis = new DataInputStream(socket.getInputStream());
                        dos = new DataOutputStream(socket.getOutputStream());
                        if (socket != null && socket.isConnected()) {
                            isConnect = true;
                        } else {
                            isConnect = false;
                        }
                        if (listener != null) {
                            listener.onServiceStatusConnectChanged(isConnect ? STATUS_CONNECT_SUCCESS : STATUS_CONNECT_ERROR);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        reconnect();
                    }
                    while (isConnect) {
                        startRecTask(dis);
                    }
                }
            }).start();

        }
        return this;
    }

    class SocketThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (isConnect) {
                startRecTask(dis);
            }
        }
    }


    /**
     * 断开连接
     */
    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
                socket = null;
                dis.close();
                dis = null;
                dos.close();
                dos = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 重连
     */
    public void reconnect() {
        if (reconnectNum > 0) {
            reconnectNum--;
            try {
                Thread.sleep(reconnectIntervalTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            disconnect();
            connect(ip, port);
        } else {
            disconnect();
            if (listener != null) {
                listener.onServiceStatusConnectChanged(STATUS_CONNECT_ERROR); // 连接失败
            }
        }
    }

    /**
     * 发送消息
     *
     * @param vo
     * @param futureListener
     */
    public void sendMessage(SocketMessage socketMessage, SendMsgListener sendMsgListener) {
        this.sendMsgListener = sendMsgListener;
        this.channel = socketMessage.getChannel();
        if (socket == null || !socket.isConnected()) {
            if (this.sendMsgListener != null) {
                this.sendMsgListener.error(); // 连接断开
            }
            return;
        }
        try {
            byte[] requestBytes = NettyInitDataUtils.buildRequest(socketMessage.getCmd(), socketMessage.getBody());
            dos.write(requestBytes);
            dos.flush();
        } catch (Exception e) {
            e.printStackTrace();
            if (this.sendMsgListener != null)
                this.sendMsgListener.error();
        }
    }

    /**
     * 打印数据
     *
     * @param socketMessage
     */
    private static void printLn(SocketMessage socketMessage) {
        String json = null;
        try {
            json = new String(socketMessage.getBody(), "utf-8");
            Log.d("tag", "发送的服务==" + socketMessage.getChannel() + ",,,,,cmd===" + socketMessage.getCmd() + ",,,内容===" + json);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始接收返回数据
     *
     * @param dis
     */
    private void startRecTask(DataInputStream dis) {
        try {
            int length = dis.readInt();
            long sequenceId = dis.readLong();
            short cmd = dis.readShort();
            final int responseCode = dis.readInt();
            int requestId = dis.readInt();
            byte[] buffer = new byte[length - 22];
            int nIdx = 0;
            int nReadLen = 0;
            while (nIdx < buffer.length) {
                nReadLen = dis.read(buffer, nIdx, buffer.length - nIdx);
                if (nReadLen > 0) {
                    nIdx += nReadLen;
                } else {
                    break;
                }
            }
            String str = new String(buffer);
            if (sendMsgListener != null) {
                Log.i("tag", "返回数据指令==" + cmd + ",,,,返回数据==" + str);
                SocketResponse socketResponse = new SocketResponse(cmd, str, this.channel);
                sendMsgListener.onMessageResponse(socketResponse);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 设置重连次数
     *
     * @param reconnectNum 重连次数
     */
    public void setReconnectNum(int reconnectNum) {
        this.reconnectNum = reconnectNum;
    }

    /**
     * 设置重连时间间隔
     *
     * @param reconnectIntervalTime 时间间隔
     */
    public void setReconnectIntervalTime(long reconnectIntervalTime) {
        this.reconnectIntervalTime = reconnectIntervalTime;
    }

    /**
     * 检查连接状态
     *
     * @return
     */
    public boolean checkConnectStatus(String ip, int port) {
        return socket != null && socket.isConnected();
    }


    public void setListener(ConnectListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener == null ");
        }
        this.listener = listener;
    }


}
