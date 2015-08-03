package com.car15.car;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.car15.R;
import com.car15.comm.CarInfo;
import com.car15.view.PathView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by 思宁 on 2015/7/30.
 * 巡逻及其巡逻的设置
 */
public class StrollActivity extends Activity {
    private SharedPreferences sp;
    LinearLayout layout;
    LinearLayout.LayoutParams params;
    private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stroll_activity);
        sp = this.getSharedPreferences("houseInfo", Context.MODE_WORLD_READABLE); //获取长宽数据
        explainDialog();
        init();
        /*Toast.makeText(StrollActivity.this,"请在示意图上拖动已完成障碍物摆放", Toast.LENGTH_LONG).show();
        mp = MediaPlayer.create(this,R.raw.cityofsky);
        mp.start();*/

        //返回
        (findViewById(R.id.help_img)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StrollActivity.this.finish();
            }
        });
    }

    private void init(){
        layout=(LinearLayout) findViewById(R.id.lujing);
        params = (LinearLayout.LayoutParams)layout.getLayoutParams();
        params.height = 700;
        params.width = 700;
//        params.height = sp.getInt("y", 0);
//        params.width = sp.getInt("x",0);

        //初始化为0
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("b", 0);
        editor.commit();

        final PathView view=new PathView(this);
        view.setWindowsizeX(sp.getInt("x", 7));
        view.setWindowsizeY(sp.getInt("y", 7));
        view.setPositionee(sp.getInt("z", 15));
        view.setdeviationee(sp.getInt("a", 5));
        view.setgridnum(sp.getInt("b", 0));
        if(sp.getInt("b", 0)>0)
        {
            view.setgridmethod(1);
        }
        else
            view.setgridmethod(0);



        layout.addView(view);

        //巡逻时间-调到设置里面
        findViewById(R.id.time_xunluo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StrollActivity.this,SettingActivity.class);
                startActivity(intent);
            }
        });

        //参数设置-100倍
        ((RelativeLayout)findViewById(R.id.house_set)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //弹出对话框
                LayoutInflater inflaterDl = LayoutInflater.from(StrollActivity.this);
                final RelativeLayout view_alert = (RelativeLayout) inflaterDl.inflate(R.layout.house_dialog, null);
                final Dialog dialog1 = new AlertDialog.Builder(StrollActivity.this).create();
                dialog1.show();
                dialog1.getWindow().setContentView(view_alert);
                //不然弹不出对话框
                dialog1.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

                ((EditText) view_alert.findViewById(R.id.house_x)).setText(sp.getInt("x", 700) / 100 + "");
                ((EditText) view_alert.findViewById(R.id.house_y)).setText(sp.getInt("y", 700) / 100 + "");
                ((EditText) view_alert.findViewById(R.id.house_z)).setText(sp.getInt("z", 15) + "");
                ((EditText) view_alert.findViewById(R.id.house_a)).setText(sp.getInt("a", 5) + "");
                ((EditText) view_alert.findViewById(R.id.house_b)).setText(sp.getInt("b", 0) + "");


                //默认
                view_alert.findViewById(R.id.moren).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            String x = "7";
                            String y = "7";
                            String z = "15";
                            String a = "5";
                            String b = "0";

                            ((EditText) view_alert.findViewById(R.id.house_x)).setText("7");
                            ((EditText) view_alert.findViewById(R.id.house_y)).setText("7");
                            ((EditText) view_alert.findViewById(R.id.house_z)).setText("15");
                            ((EditText) view_alert.findViewById(R.id.house_a)).setText("5");
                            ((EditText) view_alert.findViewById(R.id.house_b)).setText("0");

                            SharedPreferences.Editor editor = sp.edit();
                            editor.putInt("x", Integer.parseInt(x) * 100);
                            editor.putInt("y", Integer.parseInt(y) * 100);
                            editor.putInt("z", Integer.parseInt(z));
                            editor.putInt("a", Integer.parseInt(a));
                            editor.putInt("b", Integer.parseInt(b));

                            editor.commit();

                            params.height = sp.getInt("y", 0);
                            params.width = sp.getInt("x", 0);

                            view.setWindowsizeX(Double.parseDouble(x));
                            view.setWindowsizeY(Double.parseDouble(y));
                            view.setPositionee(Integer.parseInt(z));
                            view.setdeviationee(Integer.parseInt(a));
                            view.setgridnum(0);
                            view.setgridmethod(0);

                        } catch (Exception e) {
                        }
                        dialog1.dismiss();
                    }
                });
                //确定
                RelativeLayout btnOK = (RelativeLayout) view_alert.findViewById(R.id.ok);
                btnOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            String x = ((EditText) view_alert.findViewById(R.id.house_x)).getText().toString();
                            String y = ((EditText) view_alert.findViewById(R.id.house_y)).getText().toString();
                            String z = ((EditText) view_alert.findViewById(R.id.house_z)).getText().toString();
                            String a = ((EditText) view_alert.findViewById(R.id.house_a)).getText().toString();
                            String b = ((EditText) view_alert.findViewById(R.id.house_b)).getText().toString();
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putInt("x", Integer.parseInt(x) * 100);
                            editor.putInt("y", Integer.parseInt(y) * 100);
                            editor.putInt("z", Integer.parseInt(z));
                            editor.putInt("a", Integer.parseInt(a));
                            if (Integer.parseInt(b) > 0) {
                                editor.putInt("b", Integer.parseInt(b));
                                view.setgridmethod(1);
                                view.setgridnum(Integer.parseInt(b));
                            } else {
                                view.setgridmethod(0);
                                editor.putInt("b", 0);
                            }

                            editor.commit();

                            params.height = sp.getInt("y", 0);
                            params.width = sp.getInt("x", 0);

                            view.setWindowsizeX(Double.parseDouble(x));
                            view.setWindowsizeY(Double.parseDouble(y));
                            view.setPositionee(Integer.parseInt(z));
                            view.setdeviationee(Integer.parseInt(a));
                        } catch (Exception e) {
                            Toast.makeText(StrollActivity.this, "提示：输入错误或没有输入完整！", Toast.LENGTH_SHORT).show();
                        }
                        dialog1.dismiss();
                    }
                });
            }
        });

        //说明
        ((RelativeLayout)findViewById(R.id.explain)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                explainDialog();
            }
        });


        //障碍物摆放完成
        ((RelativeLayout)findViewById(R.id.obstacle)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.setObstacle();
                view.invalidate();
                findViewById(R.id.path).setEnabled(true);
            }
        });
        //生成路径
        ((RelativeLayout)findViewById(R.id.path)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.plan_path();
                view.invalidate();
                //Toast.makeText(StrollActivity.this,"finished！",Toast.LENGTH_SHORT).show();
                findViewById(R.id.path).setEnabled(false);
            }
        });
        //重置
        ((RelativeLayout)findViewById(R.id.reset)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.reset();
                view.invalidate();
            }
        });
        //撤销
        ((RelativeLayout)findViewById(R.id.undo)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.undo();
                view.invalidate();
            }
        });
        //设置完成
        // 将数据发给服务器
        //保存信息-
        ((RelativeLayout)findViewById(R.id.ok_send)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //将数据发送给小车-下面是要发送的数据
                /*1、windowsizeX、windowsizeY为窗口大小
                2、sendpoint[i][0] 为拐点x坐标
                sendpoint[i][1] 为拐点y坐标
                一直搜索直到sendpoint[i][0]=-1为止*/
                final double windowsizeX = view.getWindowsizeX();
                final double windowsizeY = view.getWindowsizeY();
                final int [][] sendPoint = view.getSendpoint();
                new Thread(){
                    public void run() {
                        OutputStreamWriter output = null;//发送消息
                        DataOutputStream out = null;
                        Socket sc  =null;
                        try {
                            sc = new Socket(CarInfo.ServerIp, CarInfo.ServerPort);
                            out =new DataOutputStream( sc.getOutputStream());
                            output = new OutputStreamWriter(sc.getOutputStream());
                            //发消息-命令
                            output.write("dddd");
                            //output.flush();
                            //x-y-拐点-(-1结束)
                            out.writeDouble(windowsizeX);
                            //out.flush();
                            out.writeDouble(windowsizeY);
                            //out.flush();
                            for(int i = 0;i< sendPoint.length;i++){
                                if(sendPoint[i][0]==-1){
                                    out.writeInt(-1);
                                    //out.flush();
                                    Log.i("123",-1+"------结束-----");
                                    break;
                                }
                                out.writeInt(sendPoint[i][0]);
                                //out.flush();
                                Log.i("123",sendPoint[i][0]+"-----x------");
                                out.writeInt(sendPoint[i][1]);
                               // out.flush();
                                Log.i("123",sendPoint[i][1]+"------y-----");
                            }
                            out.flush();
                            out.close();
                        }catch (Exception e){
                            Looper.prepare();
                            Toast.makeText(StrollActivity.this, "连接错误", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                            Looper.loop();
                        }finally {
                            if(sc != null) {
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
                    }
                }.start();
                Toast.makeText(StrollActivity.this,"设置完成！",Toast.LENGTH_SHORT).show();
                //保存到手机
                SharedPreferences sharedPreferences = getSharedPreferences("xunluo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
                editor.putBoolean("isSet",true);
                editor.commit();//提交修改
                //其他信息保存到手机

                //StrollActivity.this.finish();
            }
        });

        //手动生成（一样的，只不过基于栅格法多选几个点把坐标直接发过去罢了，手拖动选择路径比较难实现，因为为了好定位所以选择的栅格
        //法在方向上有局限性只能上下左右。如果是斜着拖动生成路径可能会是锯齿形线路小车会多次拐弯）
        /*((RelativeLayout)findViewById(R.id.byhand)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.setObstacle();
            }
        });*/
    }

    static byte[] DoubleToArray(double Value)
    {
        long accum = Double.doubleToRawLongBits(Value);
        byte[] byteRet = new byte[8];
        byteRet[0] = (byte)(accum & 0xFF);
        byteRet[1] = (byte)((accum>>8) & 0xFF);
        byteRet[2] = (byte)((accum>>16) & 0xFF);
        byteRet[3] = (byte)((accum>>24) & 0xFF);
        byteRet[4] = (byte)((accum>>32) & 0xFF);
        byteRet[5] = (byte)((accum>>40) & 0xFF);
        byteRet[6] = (byte)((accum>>48) & 0xFF);
        byteRet[7] = (byte)((accum>>56) & 0xFF);
        return byteRet;
    }

    public static byte[] intToBytes(int n){
        byte[] b = new byte[4];
        for(int i = 0;i < 4;i++){
            b[3-i] = (byte)(n >> (24 - i * 8));
        }
        return b;
    }
    /*public byte[] intToBytes(int n){
        byte[] b = new byte[4];
        for(int i = 0;i < 4;i++){
            b[i] = (byte)(n >> (24 - i * 8));
        }
        return b;
    }*/

    public void explainDialog(){
        LayoutInflater inflaterDl = LayoutInflater.from(StrollActivity.this);
        RelativeLayout layout = (RelativeLayout)inflaterDl.inflate(R.layout.explain_dialog, null );
        final Dialog dialog = new AlertDialog.Builder(StrollActivity.this).create();
        dialog.show();
        dialog.getWindow().setContentView(layout);

        RelativeLayout btnOK = (RelativeLayout) layout.findViewById(R.id.ok);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
}
