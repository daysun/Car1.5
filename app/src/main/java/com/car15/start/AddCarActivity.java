package com.car15.start;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.car15.R;
import com.car15.car.LocalCarMenu;
import com.car15.comm.CarInfo;

import java.io.DataInputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * 添加本地小车
 */
public class AddCarActivity extends Activity{
    boolean isLink = false;
    String id;
    String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_car);

        //返回
        (findViewById(R.id.help_img)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddCarActivity.this.finish();
            }
        });

        (findViewById(R.id.btn_next)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                id =( (EditText)findViewById(R.id.car_id)).getText().toString();//获取小车id
                name =( (EditText)findViewById(R.id.car_name)).getText().toString();//获取小车名称
                if(id.equals("") || name.equals("")){
                    Toast.makeText(AddCarActivity.this,"输入不可为空",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(AddCarActivity.this,"连接小车中...请稍等...",Toast.LENGTH_SHORT).show();
                    func();
                   /* final Handler handler = new Handler(){
                        @Override
                        public void handleMessage(Message msg) {
                            isLink = (boolean)msg.obj;
                            if(isLink){
                                func();
                            }else{
                                Toast.makeText(AddCarActivity.this,"连接错误，请确保小车开启",Toast.LENGTH_SHORT).show();
                            }
                        }
                    };
                    new Thread(){
                        public void run() {
                            Socket sc  =null;
                            OutputStreamWriter output = null;
                            try {
                                sc = new Socket(CarInfo.ServerIp, CarInfo.ServerPort);
                                output = new OutputStreamWriter(sc.getOutputStream());
                                //发消息
                                output.write("cccc");
                                output.flush();
                                Message msg = Message.obtain();
                                msg.obj = true;
                                handler.sendMessage(msg);
                            }catch (Exception e){
                                Message msg = Message.obtain();
                                msg.obj = false;
                                handler.sendMessage(msg);
                                e.printStackTrace();
                            }finally {
                                if(sc != null) {
                                    if (!sc.isClosed()) {
                                        try {
                                            sc.close();
                                            Log.i("addcaractivity","---连接断开---");
                                        } catch (Exception e) {
                                            Log.i("123", "关闭socket出错");
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }.start();*/
                }
            }
        });
    }

    public  void func(){
        //信息存起来
        SharedPreferences sharedPreferences = getSharedPreferences("car", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
        editor.putString("carid", id);
        editor.putString("carname", name);
        editor.commit();//提交修改
        //若登录成功
        Intent intent = new Intent(AddCarActivity.this,LocalCarMenu.class);
        intent.putExtra("id",id);
        intent.putExtra("name",name);
        startActivity(intent);
        AddCarActivity.this.finish();
    }
}
