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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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

    /**
     * 自定义USB权限
     */
    private static final String ACTION_USB_PERMISSION = DmbPlayerConstant.ACTION_USB_PERMISSION.getDmbConstantDescribe();
    private static final String TAG = "MainActivity";
    /**
     * 设备存储权限
     */
    private static final int WRITE_STORAGE_REQUEST_CODE = 100;
    /**
     * 跳转到默认场景的消息
     */
    private static final int MESSAGE_JUMP_DEFAULT_ACTIVITY = DmbPlayerConstant.MESSAGE_JUMP_DEFAULT_ACTIVITY.getDmbConstantValue();
    /**
     * 设备的 ID 号
     */
    public static volatile int id;
    /**
     * 装载 FrameLayout 的容器
     */
    private final ArrayList<FrameLayout> frameLayouts = new ArrayList<>();
    /**
     * USB广播接收器
     */
    private DmbBroadcastReceiver dmbBroadcastReceiver;
    /**
     * 默认的工作场景
     */
    private FrequencyModule defaultFrequencyModule;

    @RequiresApi(api = Build.VERSION_CODES.R)
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
        // 构造Handler
        MainHandler mainHandler = new MainHandler(Looper.getMainLooper());
        // 之所以在这里传进去是因为尽量把跳转 Activity 的工作留在主线程
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
        // 从sharedPreferences中获取默认模块的序号,序号的范围是 1-9
        int serialNumber = DmbUtil.getInt(this, DmbPlayerConstant.DEFAULT_FREQUENCY_MODULE_KEY.getDmbConstantName(), 20);
        if (serialNumber == 20) {
            // 如果获取到的序号是 20,说明没有设置默认模块
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, SettingActivity.class);
            // 转到设置页面
            startActivity(intent);
            return;
        }
        // 根据序号获取模块信息
        defaultFrequencyModule = FrequencyModule.getFrequencyModuleBySerialNumber(serialNumber);
        assert defaultFrequencyModule != null;
        // 设置 ID
        id = defaultFrequencyModule.getDeviceID();
        // 设置活跃的模块
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
     * 动态设置 UI 布局
     */
    private void dynamicLayout() {
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        // 屏幕宽度（像素）
        int width = metric.widthPixels;
        // 屏幕高度（像素）
        int height = metric.heightPixels;
        // 屏幕密度（0.75 / 1.0 / 1.5）
        float density = metric.density;
        // 屏幕密度DPI（120 / 160 / 240）
        int densityDpi = metric.densityDpi;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            frameLayouts.forEach(e -> {

            });
        } else {
            for (FrameLayout frameLayout : frameLayouts) {

            }
        }

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

        // 装载 FrameLayout
        frameLayouts.add(curriculumFrameLayout);
        frameLayouts.add(dormitoryFrameLayout);
        frameLayouts.add(carouselFrameLayout);
        frameLayouts.add(videoFrameLayout);
        frameLayouts.add(audioFrameLayout);
        frameLayouts.add(settingFrameLayout);
        // 绑定 FrameLayout 的监听器
        frameLayouts.forEach(e -> e.setOnFocusChangeListener(onFocusChangeListener));
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
    @RequiresApi(api = Build.VERSION_CODES.R)
    @SuppressLint("NonConstantResourceId")
    public void onclick(View view) {
        Intent intent = new Intent();
        List<DialogUtil.PositiveButton> settingPositiveButtons = DialogUtil.getPositiveButtonList(
                new DialogUtil.PositiveButton(null, "确定"),
                new DialogUtil.PositiveButton((dialogInterface, i) -> {
                    intent.setClass(MainActivity.this, SettingActivity.class);
                    startActivity(intent);
                }, "设置"));
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
        if (DataReadWriteUtil.getDefaultFrequencyModule(this) == null && view.getId() != R.id.setting) {
            // 如果当前还没有设置默认的工作模块,就提醒用户进行设置
            DialogUtil.generateDialog(this, "缺少默认工作场景设置!", "您还没有设置默认的工作场景,点击右下角设置按钮进行使用场景的设置," +
                    "设置完成之后您可以进入任意一个场景,默认的工作场景设置完成之后," +
                    "并不会影响您进入其他场景,之后每次启动 APP 都会进入默认的工作场景.", settingPositiveButtons).show();
        }
        switch (view.getId()) {
            case R.id.curriculum:
                FrequencyModule activeFrequencyModule = DataReadWriteUtil.getActiveFrequencyModule();
                // 判断当前默认的设置是不是课表
                if (!activeFrequencyModule.getModuleName().startsWith("CURRICULUM")) {
                    DialogUtil.generateDialog(this,
                            "设置冲突",
                            "当前设置的使用场景不是课表," +
                                    "我不知道您想显示哪一个教学楼的课表,如果您想显示课表," +
                                    "请在设置中设置课表并选择教学楼",
                            settingPositiveButtons).show();
                    return;
                }
                intent.setClass(this, CurriculumActivity.class);
                startActivity(intent);
                break;
            case R.id.setting:
                intent.setClass(this, SetupActivity.class);
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
    @RequiresApi(api = Build.VERSION_CODES.R)
    private Class<?> getActivityByDefaultFrequencyModule(FrequencyModule defaultFrequencyModule) {
        if (defaultFrequencyModule.getModuleName().startsWith("CURRICULUM")) {
            return CurriculumActivity.class;
        }
        if (defaultFrequencyModule.getModuleName().equals("OUTDOOR_SCREEN_TPEG")) {
            return CarouselActivity.class;
        }
        if (defaultFrequencyModule.getModuleName().equals("OUTDOOR_SCREEN_VIDEO")) {
            return VideoActivity.class;
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

        @RequiresApi(api = Build.VERSION_CODES.R)
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MESSAGE_JUMP_DEFAULT_ACTIVITY) {
                if (defaultFrequencyModule == null) {
                    // 没有的话,就直接返回了
                    return;
                }
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
                // 获取对应的工作场景
                intent.setClass(MainActivity.this, getActivityByDefaultFrequencyModule(defaultFrequencyModule));
                // 跳转
                startActivity(intent);
            }
        }
    }
}
