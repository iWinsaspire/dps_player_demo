package cn.dolphinstar.demo;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.dolphinstar.demo.toolkit.QRHelper;
import cn.dolphinstar.lib.player.core.MYOUPlayer;
import cn.dolphinstar.lib.player.core.StartUpCfg;
import cn.dolphinstar.lib.wozkit.NetHelper;
import cn.dolphinstar.player.base.StartUpAuthType;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class MainActivity extends Activity {

    //二维码图片
    private ImageView qrImageView; 

    //wifi名称
    private String wifiName;
    //名称
    private String playerName;

    @SuppressLint({"CheckResult", "ShowToast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 设置屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        qrImageView = findViewById(R.id.iv_qrcode); 

        if (Build.VERSION.SDK_INT >= 23) {
            checkAndRequestPermission();
        } else {
            dpsSdkStartUp();
        }
    }

    @SuppressLint({"CheckResult", "ShowToast", "SetTextI18n"})
    private void dpsSdkStartUp() {

        NetHelper netHelper = new NetHelper(getApplicationContext());
        int netType = netHelper.getConnectedType();
        //必须确保有网络的情况下在启动sdk
        if (netType == -1) {
            wifiName = "未连接网络!";
            Toast.makeText(getApplicationContext(), "未连接网络，投屏服务未启动!", Toast.LENGTH_LONG);
        } else {
            if (netType == 1) {
                wifiName = netHelper.WifiName();
            } else {
                wifiName = "有线网络";
            }
            ((TextView)findViewById(R.id.tv_wifi)).setText("网络:"+wifiName);
            playerName="海豚星空TV-" +(int) (Math.random() * 900 + 100);

            //sdk启动配置
            StartUpCfg cfg = new StartUpCfg();
            cfg.PlayerName = playerName;
            cfg.IsShowLogger = BuildConfig.DEBUG;
            cfg.AppSecret = "xxxxxxx"; //这里填入你的秘钥

            //demo 特殊配置信息 ，非必要。按自己想要的方式给 AppId AppSecret赋值就好
            if(!BuildConfig.dpsAppId.isEmpty()){
                cfg.AppId = BuildConfig.dpsAppId;
            }
            if(!BuildConfig.dpsAppSecret.isEmpty()){
                cfg.AppSecret = BuildConfig.dpsAppSecret;
            }

            //仅允许最后一台认证过的设备有效模式
            //MYOUPlayer.of(MainActivity.this).setAuthMode(1);
            //启动服务
            MYOUPlayer.of(MainActivity.this)
                    .useDRender()
                    .StartService(cfg)
                    .observeOn(AndroidSchedulers.mainThread()) //切主线程
                    .subscribe(s -> {
                        //投屏服务启动成功
                        Log.e("MainActivity","投屏服务启动成功");
                        onSuccess();
                        //如果需要替换音乐播放器的背景图，必须在 .useDRender()之后
                        //MYOUPlayer.of(MainActivity.this).setMusicBackground(R.drawable.connect);
                    }, e -> {
                        //投屏服务启动失败
                        String msg = "投屏服务启动失败: "+ e.getMessage();
                        ((TextView)findViewById(R.id.tv_cast_code)).setText(msg);
                        Log.e("MainActivity",msg);
                    });
        }
    }

    @SuppressLint("CheckResult")
    private void onSuccess() {
        //获取二维码链接并显示
        buildQRcode(MYOUPlayer.of(MainActivity.this).GetQrUrl());

        //获取投屏码并显示
        MYOUPlayer.of(MainActivity.this)
                .GetScreenCode()
                .observeOn(AndroidSchedulers.mainThread()) //切主线程
                .subscribe(code -> {
                    ((TextView)findViewById(R.id.tv_cast_code)).setText("投屏码:"+code);
                });

        ((TextView)findViewById(R.id.tv_name)).setText(playerName);

    }

    protected void onDestroy() {
        //关闭SDK 重要
        if (MYOUPlayer.of(MainActivity.this).IsStartUp()){
            MYOUPlayer.of(MainActivity.this).Close();
        }
        super.onDestroy();
    }


    //region 处理二维码部分
    //创建二维码把并显示到图片上
    private void buildQRcode(String url) {
        final String text = url;

        LinearLayout line = findViewById(R.id.layout_linear);
        int ih = line.getHeight() * 2 / 3;

        Bitmap qrBitmap = new QRHelper().BuildQRCode(text, ih, ih);
        if (qrBitmap!=null) {
            runOnUiThread(() -> qrImageView.setImageBitmap(qrBitmap));
        }
    }

 

    //endregion

    //region 动态权限申请

    @TargetApi(Build.VERSION_CODES.M)
    private void checkAndRequestPermission() {
        //假设需要动态权限申请完成在启动sdk
        List<String> lackedPermission = new ArrayList<>();

        // 如果需要的权限都已经有了，那么直接调用SDK
        if (lackedPermission.size() == 0) {
            dpsSdkStartUp();
        } else {
            String[] requestPermissions = new String[lackedPermission.size()];
            lackedPermission.toArray(requestPermissions);
            requestPermissions(requestPermissions, 1024);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1024) {
            dpsSdkStartUp();
        }
    }

    //endregion



}
