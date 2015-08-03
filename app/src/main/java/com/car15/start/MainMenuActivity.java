package com.car15.start;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.car15.R;

/**
 *  main nemu
 */
public class MainMenuActivity extends Activity{
    private static int CAR_WIFI = 0x123;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        // 添加本地小车
        ((RelativeLayout)findViewById(R.id.add_car)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // dialog
                LayoutInflater inflaterDl = LayoutInflater.from(MainMenuActivity.this);
                RelativeLayout layout = (RelativeLayout)inflaterDl.inflate(R.layout.car_is_ok_dialog, null );
                final Dialog dialog = new AlertDialog.Builder(MainMenuActivity.this).create();
                dialog.show();
                dialog.getWindow().setContentView(layout);

                RelativeLayout btnOK = (RelativeLayout) layout.findViewById(R.id.ok);
                btnOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
                        intent.putExtra("extra_prefs_show_button_bar", true);
                        intent.putExtra("wifi_enable_next_on_connect", true);
                        startActivityForResult(intent,CAR_WIFI);
                        dialog.dismiss();
                    }
                });
                /*ImageButton btnClose = (ImageButton) layout.findViewById(R.id.dialog_close);
                btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });*/
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK){
            return;
        }
        if(requestCode == CAR_WIFI){
            //如果连上WiFi则跳到addlocal界面
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWifi.isConnected()) {
                Intent intent = new Intent(MainMenuActivity.this, AddCarActivity.class);
                startActivity(intent);
            }else {
                Toast.makeText(MainMenuActivity.this, "WiFi未连接", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
