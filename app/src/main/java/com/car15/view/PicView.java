package com.car15.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.car15.R;
import com.car15.car.PictureMng;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by 思宁 on 2015/8/1.
 * 显示pictures的view
 */
public class PicView extends TableLayout{
    private static final String TAG = "PicView";
    public Context mContext;
    private static ArrayList<String> paths = new ArrayList<String>();
    int pic_num=0;

    public PicView(Context context) {
        super(context);
        mContext = context;
        setupViews();
    }

    public PicView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setupViews();
    }

    //界面设置
    public void setupViews() {
        Map<String,Bitmap> maps = new TreeMap<String, Bitmap>();
        try {
            maps = buildThum();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Iterator<String> it = maps.keySet().iterator();
        int i = 0;

        int columnNum = 3;
        int rowNum = (pic_num%3==0)? pic_num/3:pic_num/3+1;
        for(int row = 0;row<rowNum;row++){
            TableRow tableRow = new TableRow(getContext());
            tableRow.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            tableRow.setPadding(30,0,0,0);
            //tableRow.setGravity(Gravity.CENTER);
            for(int column =0;column<columnNum; column++){
                if(it.hasNext()){
                    String path = (String) it.next();
                    Bitmap bm = maps.get(path);
                    ImageView image = new ImageView(mContext);
                    image.setPadding(10,5,10,10);
                    image.setImageBitmap(bm);
                    /* ViewGroup.LayoutParams params = image.getLayoutParams();
                    params.height = 40;
                    params.width = 40;
                    image.setLayoutParams(params);*/
                    image.setId(i++);
                    tableRow.addView(image);
                    image.setOnLongClickListener(longlistener);
                    image.setOnClickListener(listener);

                }
            }
            addView(tableRow);
        }
    }

    //清空界面
    public void clearViews(){
        this.removeAllViews();
    }
    /**
     * 长按删除
     */
    OnLongClickListener longlistener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            //弹出对话框-删除
            LayoutInflater inflaterDl = LayoutInflater.from(getContext());
            RelativeLayout layout = (RelativeLayout)inflaterDl.inflate(R.layout.is_delete_dialog, null );
            final Dialog dialog = new AlertDialog.Builder(getContext()).create();
            dialog.show();
            dialog.getWindow().setContentView(layout);
            final String path = paths.get(v.getId());

            //确定
            layout.findViewById(R.id.ok).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    //删除该图片
                    try {
                        File f = new File(path);
                        f.delete();
                        Toast.makeText(getContext(),"删除成功",Toast.LENGTH_SHORT).show();
                        //清空画面
                        clearViews();
                        setupViews();
                    }catch (Exception e){
                        e.printStackTrace();
                        Log.i(TAG,"删除错误");
                    }
                    dialog.dismiss();
                }
            });
            //取消
            layout.findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            return true; //必须为true
        }
    };

    /**
     * 短按
     */
    OnClickListener listener  = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String path = paths.get(v.getId());
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            //imageView.setImageBitmap(bitmap);
            LayoutInflater inflaterDl = LayoutInflater.from(getContext());
            RelativeLayout layout = (RelativeLayout)inflaterDl.inflate(R.layout.original_photo, null );
            final Dialog dialog = new AlertDialog.Builder(getContext()).create();
            dialog.show();
            dialog.getWindow().setContentView(layout);
            ((ImageView)layout.findViewById(R.id.original)).setImageBitmap(bitmap);
        }
    };
   /* OnTouchListener listener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_UP){
                String path = paths.get(v.getId());
                InputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(path);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                //imageView.setImageBitmap(bitmap);
                LayoutInflater inflaterDl = LayoutInflater.from(getContext());
                RelativeLayout layout = (RelativeLayout)inflaterDl.inflate(R.layout.original_photo, null );
                final Dialog dialog = new AlertDialog.Builder(getContext()).create();
                dialog.show();
                dialog.getWindow().setContentView(layout);
                ((ImageView)layout.findViewById(R.id.original)).setImageBitmap(bitmap);
            }
            return false;
        }
    };*/

    /**
     * 获取图片地址列表
     * @param file
     * @return
     */
    private ArrayList<String> imagePath(File file) {
        SharedPreferences preferences =  mContext.getSharedPreferences("car", Activity.MODE_PRIVATE);
        String carName = preferences.getString("carname", "");
        String carId = preferences.getString("carid", "");
        ArrayList<String> list = new ArrayList<String>();
        File[] files = file.listFiles();
        for (File f : files) {
            if(f.getName().toString().split("_")[0].equals(carId+carName)){
                list.add(f.getAbsolutePath());
            }
            //list.add(f.getAbsolutePath());
        }
        Collections.sort(list);
        return list;
    }

    /**
     * 读取sdcard文件夹中的图片，并生成略缩图
     * @return
     * @throws FileNotFoundException
     */
    private Map<String,Bitmap> buildThum() throws FileNotFoundException {
        File baseFile = new File(Environment.getExternalStorageDirectory() + "/car_pic/");
        Map<String,Bitmap> maps = new TreeMap<String, Bitmap>();
        if (baseFile != null && baseFile.exists()) {
            paths = imagePath(baseFile);

            if (!paths.isEmpty()) {
                pic_num = paths.size();
                for (int i = 0; i < paths.size(); i++) {
                    /*BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true; // 设置了此属性一定要记得将值设置为false
                    Bitmap bitmap =BitmapFactory.decodeFile(paths.get(i),options);
                    options.inJustDecodeBounds = false;
                    int be = options.outHeight/140;  *//*//*40*//*  *//*控制大小*//*
                    if (be <= 0) {
                        be = 10;
                    }
                    options.inSampleSize = be;
                    bitmap = BitmapFactory.decodeFile(paths.get(i),options);
                    maps.put(paths.get(i), bitmap);*/
                    BitmapFactory.Options opts = null;
                    opts = new BitmapFactory.Options();
                    opts.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(paths.get(i), opts);
                    // 计算图片缩放比例
                    int width = 1200;
                    int height = 900;
                    final int minSideLength = Math.min(width, height);
                    opts.inSampleSize = computeSampleSize(opts, minSideLength,
                            width * height);
                    opts.inJustDecodeBounds = false;
                    opts.inInputShareable = true;
                    opts.inPurgeable = true;
                    Bitmap bitmap = BitmapFactory.decodeFile(paths.get(i),opts);
                    maps.put(paths.get(i), bitmap);
                }
            }else{
                Toast.makeText(getContext(),"啊哦，当前没有图片哦~",Toast.LENGTH_SHORT).show();
            }
        }

        return maps;
    }

    public static int computeSampleSize(BitmapFactory.Options options,
                                        int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
                .sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math
                .floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }
}
