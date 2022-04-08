package cn.edu.cqupt.dmb.player.actives;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.broadcast.DmbBroadcastReceiver;
import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.common.FrequencyModule;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;
import cn.edu.cqupt.dmb.player.utils.DmbUtil;

public class MainActivity extends Activity {

    private static final String ACTION_USB_PERMISSION = DmbPlayerConstant.ACTION_USB_PERMISSION.getDmbConstantDescribe();
    private static final String TAG = "MainActivity";
    private static final int WRITE_STORAGE_REQUEST_CODE = 100;

    public static int id;
    public static boolean isEncrypted;
    public static int building;

    private DmbBroadcastReceiver dmbBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 请求存储设备的读写权限
        requestPermissions(this);
        // 初始化组件
        initView();
        // 初始化 DMB 的常量,设备号还有频点
        initDmbConstants();
        firstInitMainActivity();
    }

    private void firstInitMainActivity() {
        // 如果已经注册过一遍 USB 广播接收器就直接跳过了
        if (!DataReadWriteUtil.isFirstInitMainActivity) {
            return;
        }
        // 初始化USB设备过滤器
        IntentFilter intentFilter = initIntentFilter();
        // 注册广播
        dmbBroadcastReceiver = DmbBroadcastReceiver.getInstance(this);
        registerReceiver(dmbBroadcastReceiver, intentFilter);
        if (!DataReadWriteUtil.USB_READY) {
            dmbBroadcastReceiver.tryConnectUsbDeviceAndLoadUsbData();
        }
        DataReadWriteUtil.isFirstInitMainActivity = false;
    }

    private void initDmbConstants() {
        // id 801 是重邮教学楼课表,820 是重邮室外屏
        // 这里设置的是默认的设备 ID ,后面再选择不同 Activity 的时候，会自动切换对应的终端 ID
        id = DmbUtil.getInt(this, DmbUtil.RECEIVER_ID, 1);
        isEncrypted = DmbUtil.getBoolean(this, DmbUtil.ENCRYPTION, true);
        building = DmbUtil.getInt(this, DmbUtil.BUILDING, 64);
        int serialNumber = DmbUtil.getInt(this, "defaultFrequencyModule", 20);
        if (serialNumber == 20) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, SettingMainActivity.class);
            startActivity(intent);
        }
        FrequencyModule defaultFrequencyModule = FrequencyModule.getFrequencyModuleBySerialNumber(serialNumber);
        DataReadWriteUtil.setActiveFrequencyModule(defaultFrequencyModule);
    }

    /**
     * 初始化USB设备过滤器
     *
     * @return 初始化完成的IntentFilter
     */
    private IntentFilter initIntentFilter() {
        IntentFilter filter = new IntentFilter();
        // 添加USB设备需要的权限
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        return filter;
    }

    /**
     * 初始化View
     */
    private void initView() {
        View.OnFocusChangeListener onFocusChangeListener = (view, b) -> {
            if (b) {
                view.setScaleX(1.4f);
                view.setScaleY(1.4f);
                // 此属性是将view添加到最上层
                view.bringToFront();
            } else {
                view.setScaleX(1.0f);
                view.setScaleY(1.0f);
            }
        };
        FrameLayout curriculumFrameLayout = findViewById(R.id.curriculum);
        FrameLayout dormitoryFrameLayout = findViewById(R.id.dormitory);
        FrameLayout carouselFrameLayout = findViewById(R.id.carousel);
        FrameLayout videoFrameLayout = findViewById(R.id.video);
        FrameLayout audioFrameLayout = findViewById(R.id.audio);
        FrameLayout settingFrameLayout = findViewById(R.id.setting);
        curriculumFrameLayout.setOnFocusChangeListener(onFocusChangeListener);
        dormitoryFrameLayout.setOnFocusChangeListener(onFocusChangeListener);
        carouselFrameLayout.setOnFocusChangeListener(onFocusChangeListener);
        videoFrameLayout.setOnFocusChangeListener(onFocusChangeListener);
        audioFrameLayout.setOnFocusChangeListener(onFocusChangeListener);
        settingFrameLayout.setOnFocusChangeListener(onFocusChangeListener);
    }

    @Override
    protected void onDestroy() {
        DataReadWriteUtil.USB_READY = false;
        super.onDestroy();
        // 解注册
        unregisterReceiver(dmbBroadcastReceiver);
    }

    /**
     * 组件的点击绑定事件,主要作用在主页的两个切换模式的按钮上面
     *
     * @param view 切换模式的按钮
     */
    @SuppressLint("NonConstantResourceId")
    public void onclick(View view) {
        // 设置按钮不收到 USB 影响
        if (!DataReadWriteUtil.USB_READY && view.getId() != R.id.setting) {
            // 如果当前USB设备没有准备好是不允许点击按钮的
            new AlertDialog.Builder(
                    this)
                    .setTitle("缺少DMB设备")
                    .setMessage("当前没有读取到任何的DMB设备信息,请插上DMB设备!")
                    .setPositiveButton("确定", null)
                    .show();
            return;
        }
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.curriculum:
                intent.setClass(this, CurriculumActivity.class);
                startActivity(intent);
                break;
            case R.id.setting:
                intent.setClass(this, SettingMainActivity.class);
                startActivity(intent);
                break;
            case R.id.dormitory:
                intent.setClass(this, DormitorySafetyActivity.class);
                startActivity(intent);
                break;
            case R.id.carousel:
                intent.setClass(this, CarouselActivity.class);
                startActivity(intent);
                break;
            case R.id.video:
                intent.setClass(this, VideoActivity.class);
                startActivity(intent);
                break;
        }
    }

    /**
     * 请求获取存储设备的读写权限
     * 如果有权限就直接跳过
     * 如果没有权限就请求用户授予
     *
     * @param context Context
     */
    private void requestPermissions(@NonNull Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "没有授权，申请权限");
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, WRITE_STORAGE_REQUEST_CODE);
        } else {
            Log.i(TAG, "有权限，打开文件");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WRITE_STORAGE_REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "申请权限成功，打开");
            } else {
                Log.i(TAG, "申请权限失败");
            }
        }
    }
}
