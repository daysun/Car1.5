package com.car15.car;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.car15.comm.CarInfo;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by 思宁 on 2015/7/30.
 * 专门用于通信的线程
 */
public class OrderThread extends Thread{
    Context context;
    String order;
    DataInputStream in =null;
    byte[] result;
    Handler handler;

    OrderThread(Context context,String order){
        this.context = context;
        this.order = order;
    }

    //需要回执消息的
    OrderThread(Context context,String order,Handler handler){
        this.context = context;
        this.order = order;
        this.handler = handler;
    }

    /*public float bytesToFloat(byte[] b) {
        // 4 bytes
        int accum = 0;
        for ( int shiftBy = 0; shiftBy < 4; shiftBy++ ) {
            accum |= (b[shiftBy] & 0xff) << shiftBy * 8;
        }
        return Float.intBitsToFloat(accum);
    }*/

    @Override
    public void run() {
        OutputStreamWriter output = null;//发送消息
        Socket sc  =null;
        try {
            sc = new Socket(CarInfo.ServerIp, CarInfo.ServerPort);
            output = new OutputStreamWriter(sc.getOutputStream());
            //发消息
            output.write(order);
            output.flush();
            Log.i("123","---sending---"+order);
            //接受结果
            in = new DataInputStream(sc.getInputStream());
            //result = new byte[4];
            if(handler != null) {
                //in.read(result,0,result.length);
                int a = in.readInt();
                Message msg = Message.obtain();
                //msg.obj = result;
                msg.obj = a;
                handler.sendMessage(msg);
            }
        }catch (Exception e){
            Looper.prepare();
            Toast.makeText(context, "连接错误", Toast.LENGTH_SHORT).show();
            //e.printStackTrace();
            Looper.loop();
        }finally {
            if(sc != null) {
                if (!sc.isClosed()) {
                    try {
                        sc.close();
                    } catch (Exception e) {
                        Log.i("123", "关闭socket出错");
                        //e.printStackTrace();
                    }
                }
            }
        }
    }
}
