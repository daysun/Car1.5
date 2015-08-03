package com.car15.car;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.car15.R;
import com.car15.comm.CarInfo;
import com.car15.view.ImgAdapter;
import com.car15.view.MyGallery;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 思宁 on 2015/7/28.
 *本地小车
 */
public class LocalCarMenu extends Activity {
    private Timer timer;//用于室温和电量
    private static String TAG = "LocalCarMenu";
    //图片轮播
    private MyGallery gallery = null;
    private ArrayList<Drawable> imgList;
    private ArrayList<ImageView> portImg;
    private static ArrayList<String> paths = new ArrayList<String>();
    private int pic_num = 0;
    private int preSelImgIndex = 0;
    private LinearLayout ll_focus_indicator_container = null;
    String carid,carname;


    /*@Override
    protected void onRestart() {
        super.onRestart();
        getFileImg();
        gallery.setAdapter(new ImgAdapter(LocalCarMenu.this, imgList));
    }*/

    @Override
    protected void onStart() {
        super.onStart();
        ll_focus_indicator_container = (LinearLayout) findViewById(R.id.ll_focus_indicator_container);
        imgList = new ArrayList<Drawable>();
        getFileImg();
        InitFocusIndicatorContainer();
        gallery = (MyGallery) findViewById(R.id.gallery);
        gallery.setAdapter(new ImgAdapter(LocalCarMenu.this, imgList));
        gallery.setFocusable(true);
        gallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int selIndex, long arg3) {
                selIndex = selIndex % imgList.size();
                // 修改上一次选中项的背景
                portImg.get(preSelImgIndex).setImageResource(R.drawable.ic_focus);
                // 修改当前选中项的背景
                portImg.get(selIndex).setImageResource(R.drawable.ic_focus_select);
                preSelImgIndex = selIndex;
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_car_menu);

        carid = getIntent().getStringExtra("id");
        carname = getIntent().getStringExtra("name");

        //返回
        (findViewById(R.id.help_img)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalCarMenu.this.finish();
            }
        });
        //图片轮播


        //设置小车名称
        String id = getIntent().getStringExtra("id");
        SharedPreferences preferences = getSharedPreferences("car",Activity.MODE_PRIVATE);
        final String carName = preferences.getString("carname", "");
        ((TextView)findViewById(R.id.title)).setText(carName);

        //传感器自检
        (findViewById(R.id.check)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dialog
                LayoutInflater inflaterDl = LayoutInflater.from(LocalCarMenu.this);
                RelativeLayout layout = (RelativeLayout)inflaterDl.inflate(R.layout.check_dialog, null );
                final Dialog dialog = new AlertDialog.Builder(LocalCarMenu.this).create();

                //动画部分
                Animation animation = AnimationUtils.loadAnimation(LocalCarMenu.this, R.anim.refresh);
                final ImageView img_wendu = (ImageView)layout.findViewById(R.id.img_wendu);
                final ImageView img_hongwai = (ImageView)layout.findViewById(R.id.img_hongwai);
                final ImageView img_dj = (ImageView)layout.findViewById(R.id.img_dj);
                img_wendu.setAnimation(animation);
                img_hongwai.setAnimation(animation);
                img_dj.setAnimation(animation);

                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        img_wendu.setImageResource(R.drawable.check_ok);
                        img_dj.setImageResource(R.drawable.check_ok);
                        img_hongwai.setImageResource(R.drawable.check_ok);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                dialog.show();
                dialog.getWindow().setContentView(layout);

                //确定
                RelativeLayout btnOK = (RelativeLayout) layout.findViewById(R.id.ok);
                btnOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });

        //当前模式
       /* (findViewById(R.id.model)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dialog
                LayoutInflater inflaterDl = LayoutInflater.from(LocalCarMenu.this);
                RelativeLayout layout = (RelativeLayout)inflaterDl.inflate(R.layout.select_mode_dialog, null );
                final Dialog dialog = new AlertDialog.Builder(LocalCarMenu.this).create();
                dialog.show();
                dialog.getWindow().setContentView(layout);

                (layout.findViewById(R.id.free)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        OrderThread orderThread = new OrderThread(LocalCarMenu.this,"6666");
                        orderThread.start();
                        ((TextView)findViewById(R.id.mode_text)).setText("自");
                        Toast.makeText(LocalCarMenu.this, "当前切换到自由避障模式", Toast.LENGTH_SHORT).show();
                    }
                });
                (layout.findViewById(R.id.man_op)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        OrderThread orderThread = new OrderThread(LocalCarMenu.this,"7777");
                        orderThread.start();
                        ((TextView)findViewById(R.id.mode_text)).setText("手");
                        Toast.makeText(LocalCarMenu.this,"当前切换到手动模式",Toast.LENGTH_SHORT).show();
                    }
                });
                (layout.findViewById(R.id.xunluo)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        OrderThread orderThread = new OrderThread(LocalCarMenu.this,"8888");
                        orderThread.start();
                        ((TextView)findViewById(R.id.mode_text)).setText("巡");
                        Toast.makeText(LocalCarMenu.this,"当前切换到巡逻模式",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });*/
        //巡逻路线
        (findViewById(R.id.path)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LocalCarMenu.this,StrollActivity.class);
                startActivity(intent);
            }
        });
        //视频通话
        (findViewById(R.id.sp)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LocalCarMenu.this, MovieActivity.class);
                intent.putExtra("id",carid);
                intent.putExtra("name",carname);
                startActivity(intent);
            }
        });
        //图片管理
        findViewById(R.id.mng_pic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LocalCarMenu.this,PictureMng.class);
                intent.putExtra("id",carid);
                intent.putExtra("name",carname);
                startActivity(intent);
            }
        });
        //基本设置
        findViewById(R.id.set_basic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LocalCarMenu.this,SettingActivity.class);
                startActivity(intent);
            }
        });

        //每隔五分钟发一次消息更新
        //剩余电量
        //室温
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                doActionHandler.sendMessage(message);
            }
        },1000,300000);
    }



    /*public float bytesToFloat(byte[] b) {
        int i = 0;
        Float F = new Float(0.0);
        i = ((((b[0] & 0xff) << 8 | (b[1] & 0xff)) << 8) | (b[2] & 0xff)) << 8
                | (b[3] & 0xff);
        return F.intBitsToFloat(i);
    }*/

    public int bytesToInt(byte[] b) {
        return (((int)b[0]) << 24) + (((int)b[1]) << 16) + (((int)b[2]) << 8) + b[3];
    }

    private Handler doActionHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int msgId = msg.what;
            switch (msgId) {
                case 1:
                    //电量
                    Handler handler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            /*byte[] dianl = new byte[4];
                            dianl = (byte[])msg.obj;*/
                            int wendu= (int)msg.obj;
                            ((TextView)findViewById(R.id.battery)).setText(wendu+"%");
                        }
                    };
                    OrderThread orderThread = new OrderThread(LocalCarMenu.this,"aaaa",handler);
                    orderThread.start();

                    //室温
                    Handler handler2 = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                           // int shiwen= bytesToInt((byte[])msg.obj);
                            int shiwen = (int)msg.obj;
                            ((TextView)findViewById(R.id.temperature)).setText(shiwen+"℃");
                        }
                    };
                    OrderThread orderThread2 = new OrderThread(LocalCarMenu.this,"bbbb",handler2);
                    orderThread2.start();
                    break;
                default:
                    break;
            }
        }
    };

    //图片轮播
    //获取图片路径
    private ArrayList<String> imagePath(File file) {
        ArrayList<String> list = new ArrayList<String>();
        File[] files = file.listFiles();
        for (File f : files) {
            if(f.getName().toString().split("_")[0].equals(carid+carname)){
                list.add(f.getAbsolutePath());
            }
        }
        Collections.sort(list);
        return list;
    }

    //若Environment.getExternalStorageDirectory() + "/car_pic/"中有超过图片，则用其中任意3张
    // 若不满3张，差几张补几张本地的
    public void getFileImg() {
        File baseFile = new File(Environment.getExternalStorageDirectory() + "/car_pic/");
        if (baseFile != null && baseFile.exists()) {
            paths = imagePath(baseFile);

            if (!paths.isEmpty()) { //文件夹不为空
                pic_num = paths.size();
                try {
                    if (pic_num >= 3) {//大于3张,选其中三张任意显示即可
                        for (int i = 0; i < 3; i++) {
                            FileInputStream fis = new FileInputStream(new File(paths.get(i)));//文件输入流
                            Bitmap bitmap = BitmapFactory.decodeStream(fis);
                            Drawable drawable =new BitmapDrawable(bitmap);
                            imgList.add(drawable);
                        }
                    }else{
                        for (int i = 0; i < pic_num; i++) {
                            FileInputStream fis = new FileInputStream(new File(paths.get(i)));//文件输入流
                            Bitmap bitmap = BitmapFactory.decodeStream(fis);
                            Drawable drawable =new BitmapDrawable(bitmap);
                            imgList.add(drawable);
                        }
                        if(pic_num == 1)
                        {
                            imgList.add(getResources().getDrawable(R.drawable.appmain_subject_1));
                            imgList.add(getResources().getDrawable(R.drawable.img1));
                        }else if(pic_num == 2){
                            imgList.add(getResources().getDrawable(R.drawable.img1));
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }else{ //文件夹为空
                imgList.add(getResources().getDrawable(R.drawable.lamp));
                imgList.add(getResources().getDrawable(R.drawable.appmain_subject_1));
                imgList.add(getResources().getDrawable(R.drawable.img1));
            }
        }
    }

    private void InitFocusIndicatorContainer() {
        portImg = new ArrayList<ImageView>();
        this.ll_focus_indicator_container.removeAllViews();
        for (int i = 0; i < imgList.size(); i++) {
            ImageView localImageView = new ImageView(LocalCarMenu.this);
            localImageView.setId(i);
            ImageView.ScaleType localScaleType = ImageView.ScaleType.FIT_XY;
            localImageView.setScaleType(localScaleType);
            LinearLayout.LayoutParams localLayoutParams = new LinearLayout.LayoutParams(
                    24, 24);
            localImageView.setLayoutParams(localLayoutParams);
            localImageView.setPadding(5, 5, 5, 5);
            localImageView.setImageResource(R.drawable.ic_focus);
            portImg.add(localImageView);
            this.ll_focus_indicator_container.addView(localImageView);
        }
    }
}
