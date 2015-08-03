package com.car15.car;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.car15.R;

/**
 * Created by 思宁 on 2015/8/1.
 * 图片管理
 */
public class PictureMng extends Activity{
    public  String carid,carname;

    public String    getCarInfo(){
        return carid+carname;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pic_mng);

        carid = getIntent().getStringExtra("id");
        carname = getIntent().getStringExtra("name");

        //返回
        (findViewById(R.id.help_img)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PictureMng.this.finish();
            }
        });
    }
}
