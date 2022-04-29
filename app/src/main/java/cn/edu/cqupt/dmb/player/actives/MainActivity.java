package cn.edu.cqupt.dmb.player.actives;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.broadcast.DmbBroadcastReceiver;
import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.common.FrequencyModule;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;
import cn.edu.cqupt.dmb.player.utils.DialogUtil;
import cn.edu.cqupt.dmb.player.utils.DmbUtil;

public class MainActivity extends Activity {

    private static final String ACTION_USB_PERMISSION = DmbPlayerConstant.ACTION_USB_PERMISSION.getDmbConstantDescribe();
    private static final String TAG = "MainActivity";
    private static final int WRITE_STORAGE_REQUEST_CODE = 100;

    private static final String DEFAULT_ID_KEY = "id_key";
    private static final String DEFAULT_FREQUENCY_KEY = "frequency_key";
    /**
     * 跳转到默认场景的消息
     */
    private static final int MESSAGE_JUMP_DEFAULT_ACTIVITY = DmbPlayerConstant.MESSAGE_JUMP_DEFAULT_ACTIVITY.getDmbConstantValue();
    /**
     * 设备的 ID 号
     */
    public static volatile int id = 0;
    public static volatile int frequency = 0;
    /**
     * 装载 FrameLayout 的容器
     */
    private final ArrayList<FrameLayout> frameLayouts = new ArrayList<>();
    /**
     * USB广播接收器
     */
    private DmbBroadcastReceiver dmbBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 强制全屏,全的不能再全的那种了
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        // 请求存储设备的读写权限
        requestPermissions(this);
        // 初始化组件
        initView();
        // 初始化 DMB 的常量,设备号还有频点
        initDefaultFrequencyModule();
        MainHandler mainHandler = new MainHandler(Looper.getMainLooper());
        // 之所以在这里传进去是因为尽量吧跳转 Activity 的工作留在主线程
        firstInitMainActivity(this, mainHandler);
    }

    /**
     * 尝试初始化 USB 设备
     *
     * @param context ctx
     */
    private void firstInitMainActivity(Context context, Handler handler) {
        new Thread(() -> {
            // 如果已经注册过一遍 USB 广播接收器就直接跳过了
            if (!DataReadWriteUtil.isFirstInitMainActivity) {
                return;
            }
            // 初始化USB设备过滤器
            IntentFilter intentFilter = initIntentFilter();
            // 注册广播
            dmbBroadcastReceiver = DmbBroadcastReceiver.getInstance(context, handler);
            registerReceiver(dmbBroadcastReceiver, intentFilter);
            if (!DataReadWriteUtil.USB_READY) {
                dmbBroadcastReceiver.tryConnectUsbDeviceAndLoadUsbData();
            }
            DataReadWriteUtil.isFirstInitMainActivity = false;
        }).start();
    }

    /**
     * 初始化默认的使用场景
     */
    private void initDefaultFrequencyModule() {
        int defaultId = DmbUtil.getInt(this, DEFAULT_ID_KEY, 9999);
        int defaultFrequency = DmbUtil.getInt(this, DEFAULT_FREQUENCY_KEY, 9999);
        if (defaultId == 9999 || defaultFrequency == 9999) {
            Intent intent = new Intent();
            intent.setClass(this, SetUpActivity.class);
            startActivity(intent);
        }
        id = defaultId;
        frequency = defaultFrequency;
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
        FrameLayout carouselFrameLayout = findViewById(R.id.carousel);
        FrameLayout settingFrameLayout = findViewById(R.id.setting);

        // 装载 FrameLayout
        frameLayouts.add(carouselFrameLayout);
        frameLayouts.add(settingFrameLayout);
        // 绑定 FrameLayout 的监听器
        for (FrameLayout frameLayout : frameLayouts) {
            frameLayout.setOnFocusChangeListener(onFocusChangeListener);
        }
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
        Intent intent = new Intent();
        // 设置按钮不收到 USB 影响
        if (!DataReadWriteUtil.USB_READY && view.getId() != R.id.setting) {
            // 如果当前USB设备没有准备好是不允许点击按钮的
            DialogUtil.generateDialog(this,
                            "缺少DMB设备",
                            "当前没有读取到任何的DMB设备信息,请插上DMB设备!",
                            new DialogUtil.PositiveButton(null, "确定"))
                    .show();
            return;
        }
        switch (view.getId()) {
            case R.id.setting:
                intent.setClass(this, SetUpActivity.class);
                startActivity(intent);
                break;
            case R.id.carousel:
                if (id == 0 || frequency == 0) {
                    List<DialogUtil.PositiveButton> positiveButtons = DialogUtil.getPositiveButtonList(new DialogUtil.PositiveButton(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            intent.setClass(MainActivity.this, SetUpActivity.class);
                            startActivity(intent);
                        }
                    }, "设置"), new DialogUtil.PositiveButton(null, "确定"));
                    DialogUtil.generateDialog(this,
                                    "缺少设置项",
                                    "请先设置设备 ID 以及工作频点",
                                    positiveButtons)
                            .show();
                }
                intent.setClass(this, CarouselActivity.class);
                startActivity(intent);
                break;
        }
    }

    /**
     * 请求获取存储设备的读写权限<br/>
     * 如果有权限就直接跳过<br/>
     * 如果没有权限就请求用户授予<br/>
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

    /**
     * 根据默认的工作场景获取对应的 Activity
     *
     * @param defaultFrequencyModule 工作场景(默认的)
     * @return 对应的 Activity
     */
    private Class<?> getActivityByDefaultFrequencyModule(FrequencyModule defaultFrequencyModule) {
        if (defaultFrequencyModule.getModuleName().startsWith("CURRICULUM")) {
            return CurriculumActivity.class;
        }
        if (defaultFrequencyModule.getModuleName().equals("OUTDOOR_SCREEN_TPEG")) {
            return CarouselActivity.class;
        }
        if (defaultFrequencyModule.getModuleName().equals("DORMITORY_SAFETY")) {
            return DormitorySafetyActivity.class;
        }
        return null;
    }

    private class MainHandler extends Handler {

        public MainHandler(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MESSAGE_JUMP_DEFAULT_ACTIVITY) {
                // 双重检查USB是否连接
                if (!DataReadWriteUtil.USB_READY) {
                    new AlertDialog.Builder(
                            MainActivity.this)
                            .setTitle("缺少DMB设备")
                            .setMessage("当前没有读取到任何的DMB设备信息,请插上DMB设备!")
                            .setPositiveButton("确定", null)
                            .show();
                    return;
                }
                Intent intent = new Intent();
                if (MainActivity.id == 9999 || MainActivity.frequency == 9999) {
                    intent.setClass(MainActivity.this, SetUpActivity.class);
                } else {
                    // 获取对应的工作场景
                    intent.setClass(MainActivity.this, CarouselActivity.class);
                }
                // 跳转
                startActivity(intent);
            }
        }
    }
}
