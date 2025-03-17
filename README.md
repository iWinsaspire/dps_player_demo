# dps://play_demo

海豚星空投屏大屏接收端示例

### (0) 注意: gradle\wrapper\gradle-wrapper.properties 
demo采用本地目录使用 gradle-7.4-all.zip, **根据自己的的情况修改**。(过高版本可能不兼容，不建议gradle版本太新)
```bash 
#distributionUrl=https\://services.gradle.org/distributions/gradle-7.4-all.zip
# 下载地址 https://services.gradle.org/distributions
distributionUrl=file:///D:/Android/gradle-7.4-all.zip
```

##（1）跟目录的build.gradle添加私有mevan仓库
```groovy
maven {
    allowInsecureProtocol true  //比较高的 gradle 要允许 http，低版本可不要
    url 'http://nexus.dolphinstar.cn/repo/openmavenx'
}
```
另外注意！！！更高版本的 gradle，allprojects 移到settings.gradle 配置 [可参考 https://www.jianshu.com/p/11ce712d902d](https://www.jianshu.com/p/11ce712d902d)


## （2）app/build.gradle 文件

### 2.1 添加依赖
```groovy
//海豚星空投屏核心库 建议使用后台显示的最新本
implementation 'cn.dolphinstar:playerCore:x.x.x'
implementation 'cn.dolphinstar:dRender:x.x.x'
```

### 2.2 其他配置

```groovy
compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
}
```

## (3) APP权限
```xml
 <!-- 网络访问全系 必须权限-->
<uses-permission android:name="android.permission.INTERNET" />

<!--可选-->
<!--允许程序访问Wi-Fi网络状态信息-->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
```


## (4) 网络
注意 android 9后强制https，为了支持sdk使用http。应在AndroidManifest.xml的Application节点添加
```groovy
android:networkSecurityConfig="@xml/network_security_config"
```
app\src\main\res\xml 中添加文件 network_security_config.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="true" />
</network-security-config>
```

## (5) 创建海豚星空投屏SDK APPID

前往 海豚星空平台 [控制中心-dps://sdk](https://client.dolphinstar.cn/) 创建应用获取appId，appSecret。

## (6) SDK 接口

### 启动服务
```java
 //启动海豚星空SDK投屏服务 要确保设备连接外网情况下启动
@SuppressLint("CheckResult")
private void dpsSdkStartUp(){
        /启动配置
        StartUpCfg cfg=new StartUpCfg();
        
        /*
        应用APPID  必须        
        */
        cfg.AppId="";

        /*
        应用的Secret  必须
        */
        cfg.AppSecret=""; 

        /*
        如果PlayerName为空
        SDK v4.0.0之后版本将显示后台配置的"别名-xxx"，如果没有设置别名将显示为"海豚星空TV-xxx" 
        可以在投屏服务启动成功后通过MYOUPlayer.of(MainActivity.this).getMediaRenderName()获取
        */
        cfg.PlayerName="海豚星空TV-"+(int)(Math.random()*900+100);


        /*
        使用网卡名称 
        可选 
        多网卡同时联网指定投屏使用指定网卡的IP，
        如果指定，必须得有网络，否则ip为NULL 
        不指定自动获取任意可用IP
        参考阅读：https://dolphinstar.cn/doc/#other/network
        */
        cfg.useNetwork="wlan0";

        /*是否要支持AirPlay 默认支持
        AirPlay 采用 mDNS 来搜索发现设备，网络环境必须支持组播，而且目前还无法做到像 DLNA 那样扫码后的设备一对一发现，
        目前只做到 AirPlay 服务启动，所有同一网络内手机都可发 现电视，但仍需扫码认证才可投屏。 
        所以可根据自身需求来决定是否启动 AirPlay 服务。 SDK 默认启动。
         */
        
        //cfg.IsSupportAirplay = true

        /*
        指定设备标识符。重要，重要，重要
        参考阅读 https://dolphinstar.cn/doc/#other/deviceid

        我们产品设计以智能设备为目标对象。为了区分设备，使用设备id作为标识。根据产品需要，我们目前支持多种形式的设备标识。

        免费版推荐使用: AUTH_BY_ANDROID_ID(推荐)，AUTH_BY_RANDOM_ID(推荐)，AUTH_BY_AUTO(默认)
        
        付费版需要根据后台配置对应:
        设备标识 : MAC地址。应该填入 AUTH_BY_ETH0 或 AUTH_BY_WLAN。后台授权录入有线网卡MAC则指定为 AUTH_BY_ETH0，无线网卡MAC则指定为AUTH_BY_WLAN。
        
        设备标识 : Android ID。应该填入AUTH_BY_ANDROID_ID

        public class StartUpAuthType {
            public static final String AUTH_BY_AUTO = "auto";
            public static final String AUTH_BY_ETH0 = "eth0";
            public static final String AUTH_BY_WLAN = "wlan";
            public static final String AUTH_BY_ANDROID_ID = "androidid";
            public static final String AUTH_BY_RANDOM_ID = "randomid";
        }
        */
        cfg.AuthType = StartUpAuthType.AUTH_BY_ANDROID_ID;
        
        //启动服务
        MYOUPlayer.of(MainActivity.this)
        .useDRender()
        .StartService(cfg)
        .observeOn(AndroidSchedulers.mainThread()) //切主线程
        .subscribe(s->{
            //投屏服务启动成功
            Log.e("MainActivity","投屏服务启动成功");
            },e->{
            //投屏服务启动失败
            Log.e("MainActivity","投屏服务启动失败:"+e.getMessage());
            });
}


//注意APP关闭时 要关闭服务
protected void onDestroy() {
        MYOUPlayer.of(MainActivity.this).Close();
        super.onDestroy();
}
```

## 其他接口
```java
//获取完整的连接授权 URL，用于认证二维码显示 ,返回String
MYOUPlayer.of(MainActivity.this).GetQrUrl();
  
//获取投屏码
MYOUPlayer.of(MainActivity.this) .GetScreenCode()
MYOUPlayer.of(MainActivity.this).GetPresentationUrl();
```

# Demo下载
[github下载: https://github.com/iWinsaspire/dps_player_demo](https://github.com/iWinsaspire/dps_player_demo) （可获得最新的，推荐）

[dps_player_demo.zip 下载](https://dolphinstar.cn/fs/demo/dps_player_demo.zip)

[百度网盘下载 dps_player_demo.zip](https://pan.baidu.com/s/1QAIQtLu394F-xc6BYTty8g?pwd=idps)

        .observeOn(AndroidSchedulers.mainThread()) //要操作UI要切换主线程
        .subscribe(code -> {
            //"投屏码:"+code 
        });

//电脑浏览器 safari firefox 可以输入网站完成设备认证后使用投屏