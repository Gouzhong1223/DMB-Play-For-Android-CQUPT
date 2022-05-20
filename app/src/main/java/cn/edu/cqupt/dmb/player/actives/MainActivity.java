package cn.edu.cqupt.dmb.player.actives;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.actives.fragment.MainFragment;
import cn.edu.cqupt.dmb.player.broadcast.DmbBroadcastReceiver;
import cn.edu.cqupt.dmb.player.common.CustomSettingByKey;
import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.db.database.CustomSettingDatabase;
import cn.edu.cqupt.dmb.player.db.database.SceneDatabase;
import cn.edu.cqupt.dmb.player.db.mapper.CustomSettingMapper;
import cn.edu.cqupt.dmb.player.db.mapper.SceneMapper;
import cn.edu.cqupt.dmb.player.domain.CustomSetting;
import cn.edu.cqupt.dmb.player.domain.SceneInfo;
import cn.edu.cqupt.dmb.player.domain.SceneVO;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;


/**
 * @author qingsong
 */
public class MainActivity extends FragmentActivity {

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
     * USB广播接收器
     */
    private DmbBroadcastReceiver dmbBroadcastReceiver;

    /**
     * 操作 CustomSettingMapper 的 Mapper
     */
    private CustomSettingMapper customSettingMapper;

    /**
     * 默认的启动场景
     */
    private SceneInfo defaultScene;

    /**
     * 操作 SceneMapper 的 Mapper
     */
    private SceneMapper sceneMapper;

    /**
     * 操作预设场景的 Mapper
     */
    private SceneDatabase sceneDatabase;

    /**
     * 操作自定义设置的 Mapper
     */
    private CustomSettingDatabase customSettingDatabase;

    /**
     * 根据预设场景类型获取对应的 Activity<br/>
     * {"视频", "轮播图", "音频", "安全信息", "课表"}
     *
     * @param sceneType 预设场景类型
     * @return Activity
     */
    public static Class<?> getActivityBySceneType(Integer sceneType) {
        switch (sceneType) {
            case 0: {
                return VideoActivity.class;
            }
            case 1: {
                return CarouselActivity.class;
            }
            case 2: {
                return null;
            }
            case 3: {
                return DormitorySafetyActivity.class;
            }
            case 4: {
                return CurriculumActivity.class;
            }
            default:
        }
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 强制全屏,全的不能再全的那种了
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main2);
        // 初始化数据库
        initDataBase();
        // 初始化默认的使用场景
        initDefaultScene();
        // 在 UI 线程中替换 Fragment
        runOnUiThread(() -> {
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_browse_fragment, new MainFragment())
                        .commitNow();
            }
        });
        // 请求存储设备的读写权限
        requestPermissions(this);
        // 构造Handler
        MainHandler mainHandler = new MainHandler(Looper.getMainLooper());
        // 尝试打开 USB
        firstInitMainActivity(MainActivity.this, mainHandler);
    }

    /**
     * 初始化数据库
     */
    private void initDataBase() {
        customSettingDatabase = Room.databaseBuilder(this, CustomSettingDatabase.class, "custom_setting_database")
                .allowMainThreadQueries().build();
        //new a database
        customSettingMapper = customSettingDatabase.getCustomSettingMapper();
        //new a database
        sceneDatabase = Room.databaseBuilder(this, SceneDatabase.class, "scene_database")
                .allowMainThreadQueries().build();
        sceneMapper = sceneDatabase.getSceneMapper();
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
            dmbBroadcastReceiver = DmbBroadcastReceiver.getInstance(context, handler, defaultScene);
            registerReceiver(dmbBroadcastReceiver, intentFilter);
            if (!DataReadWriteUtil.USB_READY) {
                dmbBroadcastReceiver.tryConnectUsbDeviceAndLoadUsbData();
            }
            DataReadWriteUtil.isFirstInitMainActivity = false;
        }).start();
    }

    /**
     * 加载默认的使用场景
     */
    private void initDefaultScene() {
        // 默认的使用场景
        CustomSetting defaultSceneSetting = customSettingMapper.selectCustomSettingByKey(CustomSettingByKey.DEFAULT_SENSE.getKey());
        if (defaultSceneSetting != null) {
            defaultScene = sceneMapper.selectSceneById(Math.toIntExact(defaultSceneSetting.getSettingValue()));
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
     * 请求获取存储设备的读写权限<br/>
     * 如果有权限就直接跳过<br/>
     * 如果没有权限就请求用户授予<br/>
     *
     * @param context Context
     */
    private void requestPermissions(@NonNull Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "没有授权，申请权限");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, WRITE_STORAGE_REQUEST_CODE);
        } else {
            Log.i(TAG, "有权限，打开文件");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WRITE_STORAGE_REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "申请权限成功，打开");
            } else {
                Log.i(TAG, "申请权限失败");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        customSettingDatabase.close();
        sceneDatabase.close();
        unregisterReceiver(dmbBroadcastReceiver);
    }

    /**
     * 根据 SceneInfo生成 SceneVO
     *
     * @param sceneInfo 数据库原始的SceneInfo
     * @return SceneVO
     */
    @NonNull
    private SceneVO getSceneVO(SceneInfo sceneInfo) {
        SceneVO sceneVO = new SceneVO();
        sceneVO.setId(Long.valueOf(sceneInfo.getId()));
        sceneVO.setBuilding(sceneInfo.getBuilding());
        sceneVO.setDeviceId(sceneInfo.getDeviceId());
        sceneVO.setTitle(sceneInfo.getSceneName());
        sceneVO.setFrequency(sceneInfo.getFrequency());
        sceneVO.setSceneType(sceneInfo.getSceneType());
        sceneVO.setSubTitle(sceneInfo.getFrequency() + ":" + sceneInfo.getDeviceId());
        return sceneVO;
    }

    /**
     * 主要作用于当 USB 就绪并拥有默认使用场景时,自动跳转到对应的使用场景中
     */
    private class MainHandler extends Handler {

        public MainHandler(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MESSAGE_JUMP_DEFAULT_ACTIVITY) {
                // 双重检查USB是否连接
                if (!DataReadWriteUtil.USB_READY) {
                    new AlertDialog.Builder(MainActivity.this,
                            com.xuexiang.xui.R.style.Base_Theme_MaterialComponents_Light_Dialog_MinWidth)
                            .setTitle("缺少DMB设备")
                            .setMessage("当前没有读取到任何的DMB设备信息,请插上DMB设备!")
                            .setPositiveButton("确定", null).show();
                    return;
                }
                if (defaultScene == null) {
                    // 如果没有设置默认的使用场景就直接忽略广播了
                    return;
                }
                Intent intent = new Intent();
                // 把默认使用场景放置在跳转参数中
                intent.putExtra(DetailsActivity.SCENE_VO, getSceneVO(defaultScene));
                // 获取对应的工作场景
                intent.setClass(MainActivity.this, getActivityBySceneType(defaultScene.getSceneType()));
                Toast.makeText(MainActivity.this, "正在跳转...", Toast.LENGTH_SHORT).show();
                // 跳转
                startActivity(intent);
            }
        }
    }
}
