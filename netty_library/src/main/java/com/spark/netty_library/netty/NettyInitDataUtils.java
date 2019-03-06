package com.spark.netty_library.netty;

import android.util.Log;

import com.spark.netty_library.DefineUtil;
import com.spark.netty_library.message.SocketMessage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * 格式化数据
 * Created by Administrator on 2019/1/21 0021.
 */

public class NettyInitDataUtils {
    /**
     * 格式化发送数据
     *
     * @param cmd
     * @param body
     * @return
     */
    public static byte[] buildRequest(int cmd, byte[] body) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            int length = body == null ? 26 : (26 + body.length);
            dos.writeInt(length);
            dos.writeLong(DefineUtil.SEQUESCEID);
            dos.writeShort(cmd);
            dos.writeInt(DefineUtil.VERSION);
            byte[] terminalBytes = DefineUtil.TERMINIAL.getBytes();
            dos.write(terminalBytes);
            dos.writeInt(DefineUtil.REQUESTID);
            if (body != null) dos.write(body);
            return bos.toByteArray();
        } catch (IOException ex) {
            try {
                dos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
