package cn.edu.cqupt.dmb.player.actives;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.broadcast.DmbBroadcastReceiver;
import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;
import cn.edu.cqupt.dmb.player.utils.DmbUtil;

public class MainActivity extends Activity {

    private static final String ACTION_USB_PERMISSION = DmbPlayerConstant.ACTION_USB_PERMISSION.getDmbConstantDescribe();

    public static int id;
    public static boolean isEncrypted;
    public static int building;

    private DmbBroadcastReceiver dmbBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 初始化组件
        initView();
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
        id = DmbUtil.getInt(this, DmbUtil.RECEIVER_ID, 801);
        isEncrypted = DmbUtil.getBoolean(this, DmbUtil.ENCRYPTION, true);
        building = DmbUtil.getInt(this, DmbUtil.BUILDING, 64);
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
        Button button1 = findViewById(R.id.button1);
        button1.setOnFocusChangeListener(onFocusChangeListener);
        Button button2 = findViewById(R.id.button2);
        button2.setOnFocusChangeListener(onFocusChangeListener);
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
        if (!DataReadWriteUtil.USB_READY) {
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
            case R.id.button1:
                intent.setClass(MainActivity.this, DormitorySafetyActivity.class);
                startActivity(intent);
                break;
            case R.id.button2:
                intent.setClass(MainActivity.this, CarouselActivity.class);
                startActivity(intent);
                break;
        }
    }
}
