package cn.edu.cqupt.dmb.player.actives;


import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.processor.FicDataProcessor;
import cn.edu.cqupt.dmb.player.utils.DmbUtil;
import cn.edu.cqupt.dmb.player.utils.UsbUtil;

public class MainActivity extends Activity {

    private static final int VID = DmbPlayerConstant.DMB_V_ID.getDmbConstantValue();
    private static final int PID = DmbPlayerConstant.DMB_P_ID.getDmbConstantValue();
    public volatile static boolean USB_READY = false;

    private static final String ACTION_USB_PERMISSION = DmbPlayerConstant.ACTION_USB_PERMISSION.getDmbConstantDescribe();

    private UsbManager usbManager;
    public static int id;
    public static boolean mIsEncrypted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 初始化组件
        initView();
        // 初始化USB设备过滤器
        IntentFilter intentFilter = initIntentFilter();
        // 注册广播
        registerReceiver(broadcastReceiver, intentFilter);
        initDmbConstants();
        // 先尝试一下直接读取终端的USB接口查看是否已经有USB设备已经接入,
        // 如果已经接入就直接开始从USB读取数据,如果没有就等待上面的广播通知
        if (!USB_READY) {
            tryConnectUsbDeviceAndLoadUsbData();
        }
    }

    private void initDmbConstants() {
        id = DmbUtil.getInt(this, DmbUtil.RECEIVER_ID, 802);
        mIsEncrypted = DmbUtil.getBoolean(this, DmbUtil.ENCRYPTION, true);
    }

    /**
     * 尝试直接从USB中读取数据
     */
    private void tryConnectUsbDeviceAndLoadUsbData() {
        if (usbManager == null) {
            usbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);
        }
        // 如果当前终端没有USB设备接入,就直接返回
        if (usbManager.getDeviceList().size() == 0) {
            return;
        }
        for (UsbDevice device : usbManager.getDeviceList().values()) {
            // 如果有就开始再设备列表里面查找DMB接收机
            if (device.getVendorId() == VID && device.getProductId() == PID) {
                // 发送自定义广播也就是发起权限请求的广播
                Intent intent = new Intent(ACTION_USB_PERMISSION);
                @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent =
                        PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
                // 弹出权限框，进行权限申请
                usbManager.requestPermission(device, pendingIntent);
            }
        }
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


    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // 是否接收到自定义广播
            if (ACTION_USB_PERMISSION.equals(action)) {
                // 这里之所以加这么一个判断,是因为在测试的过程中发现了一个问题
                // 就是如果在运行的过程中拔出USB,然后再插入,那么这个方法会被调用多次,次数是1+2^n次,n代表USB插拔的次数
                // 所以现在用一个状态位来控制线程池的调度,如果USB已经是就绪的状态,就不再往线程池提交任务了
                if (USB_READY) {
                    return;
                }
                // 如果收到这个广播就证明用户已经授予了USB读取的权限
                synchronized (this) {
                    UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    Log.e(TAG, System.currentTimeMillis() + "---接收到广播,action:" + action);
                    // 是否授权成功
                    if (usbManager.hasPermission(usbDevice)) {
                        if (usbDevice != null) {
                            USB_READY = true;
                            // 打开USB设备并开始读取数据
                            openDevice();
                        }
                    } else {
                        Log.e(TAG, System.currentTimeMillis() + "---USB权限已被拒绝，Permission denied for device" + usbDevice);
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                // 如果走到这里,有两种情况
                // 1.说明用户暂时没有授予USB读取的权限
                // 2.用户之前授予过USB权限,做了一次插拔动作,这是授予权限之后拔出之后再插入的分支
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                assert usbDevice != null;
                if (VID == usbDevice.getVendorId() && PID == usbDevice.getProductId()) {
                    // 所以在这里要多做一步判断,就是如果用户已经授予权限,那就直接打开USB设备
                    if (usbManager.hasPermission(usbDevice)) {
                        USB_READY = false;
                        FicDataProcessor.isSelectId = false;
                        // 打开USB设备并开始读取数据
                        closeDevice();
                    } else {
                        // 如果暂时还没有授予权限,那就发起授权申请
                        // 然后再向用户发起授予权限的请求
                        Log.e(TAG, System.currentTimeMillis() + "---USB权限已被拒绝，Permission denied for device" + usbDevice);
                        openDeviceAndRequestDevice();
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                // 这个广播是用户拔出USB的通知
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                assert device != null;
                if (VID == device.getVendorId() && PID == device.getProductId()) {
                    USB_READY = false;
                    // 由于是直接拔出,所以直接执行关闭USB的后置方法
                    Log.e(TAG, System.currentTimeMillis() + "---USB 设备已被拔出!");
                    closeDevice();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        USB_READY = false;
        super.onDestroy();
        // 解注册
        unregisterReceiver(broadcastReceiver);
    }

    /**
     * 此方法是用于发起用户USB读取权限授予请求的方法
     * 在用户同意权限授予之后,会发送{@link DmbPlayerConstant#ACTION_USB_PERMISSION}广播
     * 次广播用于唤醒读取USB数据的线程
     */
    private void openDeviceAndRequestDevice() {
        if (usbManager == null) {
            usbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);
        }
        for (UsbDevice device : usbManager.getDeviceList().values()) {
            // 判断USB设备是否合法
            if (device.getVendorId() == VID && device.getProductId() == PID) {
                // 发送自定义广播
                Intent intent = new Intent(ACTION_USB_PERMISSION);
                @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent =
                        PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
                // 弹出权限框，进行权限申请
                usbManager.requestPermission(device, pendingIntent);
            }
        }
    }

    /**
     * 插入USB设备并且通过权限申请之后执行的方法
     */
    private void openDevice() {
        UsbUtil usbUtil = new UsbUtil();
        // 开始从USB中读取数据
        usbUtil.initUsb(usbManager);
    }

    /**
     * 拔出USB设备之后执行的方法
     */
    private void closeDevice() {
        // 关闭线程池,停止从USB中读取数据
        // 注意,这里是立马关闭线程!而不是简单的让线程池暂停接受任务,如果是暂停接受任务,里面已经接收的任务会继续执行!
        UsbUtil.getScheduledExecutorService().shutdownNow();
        new AlertDialog.Builder(
                this)
                .setTitle("DMB设备异常")
                .setMessage("DMB设备已经被拔出,请重新插上DMB设备")
                .setPositiveButton("确定", null)
                .show();
    }

    /**
     * 组件的点击绑定事件,主要作用在主页的两个切换模式的按钮上面
     *
     * @param view 切换模式的按钮
     */
    @SuppressLint("NonConstantResourceId")
    public void onclick(View view) {
        if (!USB_READY) {
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
