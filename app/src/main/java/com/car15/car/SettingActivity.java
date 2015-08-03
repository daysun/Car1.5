package com.car15.car;

import android.animation.AnimatorSet;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.car15.R;
import com.car15.comm.CarInfo;

import java.io.DataInputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Calendar;

/**
 * Created by 思宁 on 2015/7/30.
 * 设置
 */
public class SettingActivity extends Activity {
    private Calendar c = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_activity_car);

        //返回
        (findViewById(R.id.help_img)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingActivity.this.finish();
            }
        });
        //时间设置-开始
        (findViewById(R.id.start_time)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //弹出选择时间对话框，然后显示
                //并未上传到服务器
                Dialog dialog_time = null;
                c=Calendar.getInstance();
                dialog_time=new TimePickerDialog(SettingActivity.this,
                        new TimePickerDialog.OnTimeSetListener(){
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                ((TextView)findViewById(R.id.start_text)).setText(hourOfDay+"时"+minute+"分");
                            }
                        },c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),true);
                dialog_time.show();
            }
        });
        //时间设置-结束
        (findViewById(R.id.end_time)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog_time = null;
                c=Calendar.getInstance();
                dialog_time=new TimePickerDialog(SettingActivity.this,
                        new TimePickerDialog.OnTimeSetListener(){
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                ((TextView)findViewById(R.id.end_text)).setText(hourOfDay+"时"+minute+"分");
                            }
                        },c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),true);
                dialog_time.show();
            }
        });

        //大小设置
        (findViewById(R.id.photo_size)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //弹出对话框然后发送给服务器
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("请选择大小");
                builder.setSingleChoiceItems(R.array.pic_size, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String size = getResources().getStringArray(R.array.pic_size)[which];
                        Log.i("123", "choose---" + size);//选中的大小
                        //发给服务器
                        new Thread() {
                            @Override
                            public void run() {
                                OutputStreamWriter output = null;//发送消息
                                Socket sc = null;
                                try {
                                    sc = new Socket(CarInfo.ServerIp, CarInfo.ServerPort);
                                    output = new OutputStreamWriter(sc.getOutputStream());
                                    //发消息
                                    output.write("6666");
                                    output.write(size);
                                    output.flush();
                                } catch (Exception e) {
                                    Looper.prepare();
                                    Toast.makeText(SettingActivity.this, "连接错误", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                    Looper.loop();
                                } finally {
                                    if (!sc.isClosed()) {
                                        try {
                                            sc.close();
                                        } catch (Exception e) {
                                            Log.i("123", "关闭socket出错");
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }.start();
                        //更改文字
                        ((TextView) findViewById(R.id.size)).setText(size);
                        dialog.dismiss();
                    }
                });
                Dialog dialog = builder.create();
                dialog.show();
            }
        });
    }
}
