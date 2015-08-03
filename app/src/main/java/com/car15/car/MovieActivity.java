package com.car15.car;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.car15.R;
import com.car15.comm.CarInfo;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Calendar;
import java.util.Objects;

/**
 * Created by 思宁 on 2015/7/29.
 * 视频控制页面
 */
public class MovieActivity extends Activity {
    private static final String TAG = "MovieActivity";
    SurfaceView surfaceView = null;
    SurfaceHolder mSurfaceHolder = null;
    SurfaceViewCallback surfaceViewCallback = new SurfaceViewCallback();
    boolean savePic = false;
    String carid,carname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_port); //竖屏
        Log.i(TAG,"---starting---");

        carid = getIntent().getStringExtra("id");
        carname = getIntent().getStringExtra("name");

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.movie_port);
        surfaceView =  (SurfaceView) findViewById(R.id.surfaceView);
        mSurfaceHolder = surfaceView.getHolder();
        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.getHolder().addCallback(surfaceViewCallback);

        //返回
        (findViewById(R.id.help_img)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MovieActivity.this.finish();
            }
        });

        //←
        (findViewById(R.id.left)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OrderThread orderThread = new OrderThread(MovieActivity.this, "1111");
                orderThread.start();
            }
        });
        //→
        (findViewById(R.id.right)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OrderThread orderThread = new OrderThread(MovieActivity.this,"2222");
                orderThread.start();
            }
        });
        //↑
        (findViewById(R.id.up)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OrderThread orderThread = new OrderThread(MovieActivity.this,"3333");
                orderThread.start();
            }
        });
        //↓
        (findViewById(R.id.down)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OrderThread orderThread = new OrderThread(MovieActivity.this,"4444");
                orderThread.start();
            }
        });
        //停
        (findViewById(R.id.stop)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OrderThread orderThread = new OrderThread(MovieActivity.this,"5555");
                orderThread.start();
            }
        });
        //视频截图
        (findViewById(R.id.photo)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"---pic clicked!---");
                savePic = true;
            }
        });
        //切换模式
        (findViewById(R.id.mode)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dialog
                LayoutInflater inflaterDl = LayoutInflater.from(MovieActivity.this);
                RelativeLayout layout = (RelativeLayout)inflaterDl.inflate(R.layout.select_mode_dialog, null );
                final Dialog dialog = new AlertDialog.Builder(MovieActivity.this).create();
                dialog.show();
                dialog.getWindow().setContentView(layout);

               /* (layout.findViewById(R.id.free)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        OrderThread orderThread = new OrderThread(MovieActivity.this,"6666");
                        orderThread.start();
                        ((TextView)findViewById(R.id.mode_text)).setText("自");
                        Toast.makeText(MovieActivity.this, "当前切换到自由避障模式", Toast.LENGTH_SHORT).show();
                    }
                });*/
                (layout.findViewById(R.id.man_op)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        OrderThread orderThread = new OrderThread(MovieActivity.this,"7777");
                        orderThread.start();
                        //((TextView)findViewById(R.id.mode_text)).setText("手");
                        ((TextView)findViewById(R.id.current_mode)).setText("当前状态：手动模式");
                        Toast.makeText(MovieActivity.this,"当前切换到手动模式",Toast.LENGTH_SHORT).show();
                        (findViewById(R.id.left)).setClickable(true);
                        (findViewById(R.id.up)).setClickable(true);
                        (findViewById(R.id.down)).setClickable(true);
                        (findViewById(R.id.right)).setClickable(true);
                        (findViewById(R.id.stop)).setClickable(true);
                        (findViewById(R.id.left)).setBackground(getResources().getDrawable(R.drawable.my_account));
                        (findViewById(R.id.up)).setBackground(getResources().getDrawable(R.drawable.my_account));
                        (findViewById(R.id.down)).setBackground(getResources().getDrawable(R.drawable.my_account));
                        (findViewById(R.id.right)).setBackground(getResources().getDrawable(R.drawable.my_account));
                        (findViewById(R.id.stop)).setBackground(getResources().getDrawable(R.drawable.my_account));
                        (findViewById(R.id.stop_big)).setBackground(getResources().getDrawable(R.drawable.bg_circlr));
                    }
                });
                //若未设置巡逻路线，则不可用巡逻模式
                (layout.findViewById(R.id.xunluo)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        SharedPreferences preferences = getSharedPreferences("xunluo",
                                Activity.MODE_PRIVATE);
                        boolean isSet = preferences.getBoolean("isSet",false);
                        if(isSet){
                            OrderThread orderThread = new OrderThread(MovieActivity.this,"8888");
                            orderThread.start();
                            //((TextView)findViewById(R.id.mode_text)).setText("巡");
                            ((TextView)findViewById(R.id.current_mode)).setText("当前状态：巡逻模式");
                            Toast.makeText(MovieActivity.this,"当前切换到巡逻模式",Toast.LENGTH_SHORT).show();
                            //巡逻模式下按钮不可用
                            //颜色变灰色
                            (findViewById(R.id.left)).setClickable(false);
                            (findViewById(R.id.up)).setClickable(false);
                            (findViewById(R.id.down)).setClickable(false);
                            (findViewById(R.id.right)).setClickable(false);
                            (findViewById(R.id.stop)).setClickable(false);
                            (findViewById(R.id.left)).setBackground(getResources().getDrawable(R.drawable.gray_round));
                            (findViewById(R.id.up)).setBackground(getResources().getDrawable(R.drawable.gray_round));
                            (findViewById(R.id.down)).setBackground(getResources().getDrawable(R.drawable.gray_round));
                            (findViewById(R.id.right)).setBackground(getResources().getDrawable(R.drawable.gray_round));
                            (findViewById(R.id.stop)).setBackground(getResources().getDrawable(R.drawable.gray_round));
                            (findViewById(R.id.stop_big)).setBackground(getResources().getDrawable(R.drawable.gray_bg_circlr));
                        }else{
                            Toast.makeText(MovieActivity.this,"请先设置巡逻路线再选择该模式",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
    public class SurfaceViewCallback implements SurfaceHolder.Callback,Runnable {
        Socket movie_sc  =null;
        OutputStreamWriter output = null;//发送消息
        //运行状态
        public boolean mLoop = false;
        private Matrix matrix  = null;

        @Override
        public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        }

        //surfaceView创建成功之后
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.d(TAG,"---surfaceCreated---");
            new Thread(this).start();//开启线程
            mLoop = true;//开始画图
        }

        //画图方法
        private void drawImg(Bitmap bitmap){
            //Log.i(TAG,"---drawImg---");
            try{
                Canvas canvas = mSurfaceHolder.lockCanvas();
                if(canvas == null || mSurfaceHolder == null){
                    return;
                }
                if(bitmap!=null){
                    //画布宽和高
                    int height = surfaceView.getHeight();
                    int width  = surfaceView.getWidth();
                    //生成合适的图像
                    bitmap = getReduceBitmap(bitmap,width,height);

                    Paint paint = new Paint();
                    paint.setAntiAlias(true);
                    paint.setStyle(Paint.Style.FILL);
                    //清屏
                    paint.setColor(Color.BLACK);
                    canvas.drawRect(new Rect(0, 0, width,height), paint);
                    //画图
                    //canvas.drawBitmap(bitmap, matrix, paint);
                    canvas.drawBitmap(bitmap,0,0,paint);
                }
                //解锁显示
                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }catch(Exception ex){
                Log.e("ImageSurfaceView",ex.getMessage());
                return;
            }finally{
                //资源回收
                if(bitmap!=null){
                    bitmap.recycle();
                }
            }
        }

        //缩放图片
        private Bitmap getReduceBitmap(Bitmap bitmap ,int w,int h){
            int     width     =     bitmap.getWidth();
            int     hight     =     bitmap.getHeight();
            matrix     =     new Matrix();
           float     wScake     =    ((float) w) / width;
            float     hScake     =    ((float) h) / hight ;
             matrix.postScale(wScake, hScake);
            return Bitmap.createBitmap(bitmap, 0,0,width,hight,matrix,true);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder arg0) {
            // TODO Auto-generated method stub
            mLoop = false;
            if(movie_sc != null) {
                if (!movie_sc.isClosed()) {
                    try {
                        if(output != null) {
                            output.write("eeee");
                            output.flush();
                        }
                        movie_sc.close();
                        Log.i(TAG,"---连接断开---");
                    } catch (Exception e) {
                        Log.i("123", "关闭socket出错");
                        e.printStackTrace();
                    }
                }
            }
        }

        /*public byte[] toByteArray(InputStream input) throws IOException {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int n = 0;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }
            return output.toByteArray();
        }*/

        public long bytesToLong(byte[] value) {
            long L1 = (value[0] & 0xFF) << 56;
            long L2 = (value[1] & 0xFF) << 48;
            long L3 = (value[2] & 0xFF) << 40;
            long L4 = (value[3] & 0xFF) << 32;
            long L5 = (value[4] & 0xFF) << 24;
            long L6 = (value[5] & 0xFF) << 16;
            long L7 = (value[6] & 0xFF) << 8;
            long L8 = (value[7] & 0xFF) << 0;
            return (L1 | L2 | L3 | L4 | L5 | L6 | L7 | L8);
        }

        //保存图片
        public void saveMyBitmap(Bitmap mBitmap,String bitName)  {
            String mScreenshotPath =
                    Environment.getExternalStorageDirectory() + "/car_pic";
            String path = mScreenshotPath + "/" + bitName + ".jpg";
            //文件夹不存在就创建
            File file = new File(mScreenshotPath);
            if(!file.exists()){
                file.mkdir();
            }
            //文件
            File f = new File(path);
            FileOutputStream fOut = null;
            try {
                fOut = new FileOutputStream(f);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            try {
                fOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            Log.i(TAG,"----------thread---------"+Thread.currentThread().getName());
            //包含包头包尾的解析数据
            movie_sc  =null;
            DataInputStream in =null;
            try{
                movie_sc = new Socket(CarInfo.ServerIp, CarInfo.ServerPort);
                output = new OutputStreamWriter(movie_sc.getOutputStream());
                //发消息
                output.write("9999");
                output.flush();
                //接受照片
                in = new DataInputStream(movie_sc.getInputStream());
                Log.i(TAG,"---connect started---");
                while(true){
                    //头
                    byte[] head = new byte[8];
                    in.read(head,0,head.length);
                    if("11111111".equals(new String(head))){
                        Log.i("123","---enter head---");
                        //文件
                        byte [] imgSize = new byte[8]; //long字节数
                        in.read(imgSize,0,imgSize.length);
                        long size = bytesToLong(imgSize);
                        Log.i("123","---value of size is---"+size);
                        int len = -1;
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        byte[] buffer = new byte[4096];
                        while (size > 0 && (len = in.read(buffer, 0, size < buffer.length ? (int) size : buffer.length)) != -1) {
                            out.write(buffer, 0, len);
                            size -= len;
                        }
                        byte[] img = out.toByteArray();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);//获取bitmap
                        if(mLoop){
                            synchronized (mSurfaceHolder) {
                                drawImg(bitmap);
                                if(savePic){
                                    //保存图片
                                    Calendar c = Calendar.getInstance();
                                    String picName =carid+carname+"_"+ c.get(Calendar.YEAR)+"_"+c.get(Calendar.MONTH)+"_"+c.get(Calendar.DAY_OF_MONTH)+"_"+
                                            c.get(Calendar.HOUR_OF_DAY)+"_"+c.get(Calendar.MINUTE)+"_"+c.get(Calendar.SECOND);
                                    saveMyBitmap(bitmap,picName);
                                    Log.i(TAG,"---pic saved---");
                                    runOnUiThread(new Runnable(){
                                        @Override
                                        public void run() {
                                            Toast.makeText(MovieActivity.this,"图片已保存",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    savePic = false;
                                }
                            }
                            Thread.sleep(17);
                        }
                    }else{
                        Log.i("123", "---head error---");
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                Toast.makeText(MovieActivity.this,"连接停止",Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    }
                }
            }catch(Exception e){
                Log.i(TAG,"---connect error---");
                /*runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        Toast.makeText(MovieActivity.this,"连接错误！请确保连接上小车WiFi",Toast.LENGTH_SHORT).show();
                    }
                });*/
            }finally {
                if(movie_sc != null) {
                    if (!movie_sc.isClosed()) {
                        try {
                            if(output != null) {
                                output.write("eeee");
                                output.flush();
                            }
                            movie_sc.close();
                            Log.i(TAG,"---连接断开---");
                        } catch (Exception e) {
                            Log.i("123", "关闭socket出错");
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

   /* @Override
    protected void onPause() {
        if(movie_sc != null) {
            if (!movie_sc.isClosed()) {
                try {
                    if(output != null) {
                        output.write("eeee");
                        output.flush();
                    }else{
                        output = new OutputStreamWriter(movie_sc.getOutputStream());
                        output.write("eeee");
                        output.flush();
                    }
                    movie_sc.close();
                    Log.i(TAG,"---连接断开---");
                } catch (Exception e) {
                    Log.i("123", "关闭socket出错");
                    e.printStackTrace();
                }
            }
        }
        super.onPause();
    }*/
}
