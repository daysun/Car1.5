package com.car15.start;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.car15.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by 思宁 on 2015/8/1.
 * 所有的小车
 */
public class MyAllCar extends Activity {
    public static final String PATH = "/data/data/com.car15/shared_prefs/";
    private static ArrayList<String> paths = new ArrayList<String>(); //所有文件的名称
    private int car_num = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_car);

        //返回
        (findViewById(R.id.help_img)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyAllCar.this.finish();
            }
        });

        //getAllFile();
    }

    //文件的路径
    /*private static ArrayList<String> imagePath(File file) {
        ArrayList<String> list = new ArrayList<String>();
        File[] files = file.listFiles();
        for (File f : files) {
            //list.add(f.getAbsolutePath());
            list.add(f.getName());
        }
        Collections.sort(list);
        return list;
    }

    public void getAllFile() {
        File baseFile = new File(PATH);  //路径有问题
        if (baseFile != null && baseFile.exists()) {
            Log.i("123","----1---");
            paths = imagePath(baseFile);

            if (!paths.isEmpty()) {
                car_num = paths.size();
                for (int i = 0; i < car_num; i++) {
                    Log.i("123",paths.get(i));
                }
            }
        }
    }*/
}
