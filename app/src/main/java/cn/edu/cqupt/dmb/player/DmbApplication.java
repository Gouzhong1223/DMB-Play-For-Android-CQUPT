/*
 *
 *              Copyright 2022 By Gouzhong1223
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package cn.edu.cqupt.dmb.player;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.media.MediaRouter.ROUTE_TYPE_LIVE_AUDIO;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.media.MediaRouter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.room.Room;

import com.xuexiang.xui.XUI;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import cn.edu.cqupt.dmb.player.actives.DetailsActivity;
import cn.edu.cqupt.dmb.player.actives.MainActivity;
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
import cn.edu.cqupt.dmb.player.handler.CrashHandler;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description :
 * @Date : create by QingSong in 2022-05-13 22:17
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.actives
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DmbApplication extends Application implements DefaultLifecycleObserver {
    /**
     * 自定义USB权限
     */
    private static final String ACTION_USB_PERMISSION = DmbPlayerConstant.ACTION_USB_PERMISSION.getDmbConstantDescribe();
    /**
     * 跳转到默认场景的消息
     */
    private static final int MESSAGE_JUMP_DEFAULT_ACTIVITY = DmbPlayerConstant.MESSAGE_JUMP_DEFAULT_ACTIVITY.getDmbConstantValue();
    private static final String TAG = "DmbApplication";
    /**
     * USB广播接收器
     */
    private DmbBroadcastReceiver dmbBroadcastReceiver;
    /**
     * 操作 CustomSettingMapper 的 Mapper
     */
    private CustomSettingMapper customSettingMapper;
    /**
     * 操作 SceneMapper 的 Mapper
     */
    private SceneMapper sceneMapper;
    /**
     * 默认的启动场景
     */
    private SceneInfo defaultScene;
    /**
     * 操作自定义设置的 Mapper
     */
    private CustomSettingDatabase customSettingDatabase;
    /**
     * 操作预设场景的 Mapper
     */
    private SceneDatabase sceneDatabase;

    @Override
    public void onCreate() {
        String curProcessName = getCurrentProcessName(this);
        if (!curProcessName.equals(getPackageName())) {
            return;
        }
        super.onCreate();
        Log.i(TAG, "onCreate: 初始化 XUI");
        XUI.init(this);
        XUI.debug(false);
        CrashHandler crashHandler = CrashHandler.getInstance();
        Log.i(TAG, "onCreate: 初始化全局异常拦截器");
        crashHandler.init(getApplicationContext());
        // 初始化数据库
        initDataBase();
        // 初始化默认使用场景
        initDefaultScene();
        // 初始化音频设备
        initAudioManager();
        // 构造Handler
        MainHandler mainHandler = new MainHandler(Looper.getMainLooper());
        // 尝试打开 USB
        Log.i(TAG, "onCreate: 尝试打开 USB");
        firstInitMainActivity(this, mainHandler);
    }

    /**
     * 初始化数据库
     */
    private void initDataBase() {
        customSettingDatabase = Room.databaseBuilder(this, CustomSettingDatabase.class, "custom_setting_database").allowMainThreadQueries().build();
        //new a database
        customSettingMapper = customSettingDatabase.getCustomSettingMapper();
        //new a database
        sceneDatabase = Room.databaseBuilder(this, SceneDatabase.class, "scene_database").allowMainThreadQueries().build();
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
                Log.i(TAG, "firstInitMainActivity: 已经打开过 USB,现在跳过");
                return;
            }
            // 初始化USB设备过滤器
            IntentFilter intentFilter = initIntentFilter();
            // 注册广播
            Log.i(TAG, "firstInitMainActivity: 获取DmbBroadcastReceiver实例");
            dmbBroadcastReceiver = DmbBroadcastReceiver.getInstance(context, handler, defaultScene);
            registerReceiver(dmbBroadcastReceiver, intentFilter);
            if (!DataReadWriteUtil.USB_READY) {
                Log.i(TAG, "firstInitMainActivity: 尝试第一次打开 USB链接");
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
     * 初始化音频设备
     */
    private void initAudioManager() {
        CustomSetting audioModeSetting = customSettingMapper.selectCustomSettingByKey(CustomSettingByKey.AUDIO_OUTPUT_MODE.getKey());
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) {
            Log.e(TAG, "initAudioManager: audioManager is null");
            return;
        }
        if (audioModeSetting == null) {
            audioManager.setParameters("audio_devices_out_active=AUDIO_CODEC");
            Log.i(TAG, "initAudioManager: audioModeSetting is null,set audio_devices_out_active=AUDIO_CODEC");
        } else {
            if (audioModeSetting.getSettingValue() == 0L) {
                audioManager.setParameters("audio_devices_out_active=AUDIO_CODEC");
                Log.i(TAG, "initAudioManager: audioModeSetting.getSettingValue() == 0L,set audio_devices_out_active=AUDIO_CODEC");
            } else {
                audioManager.setParameters("audio_devices_out_active=AUDIO_HDMI,AUDIO_CODEC");
                Log.i(TAG, "initAudioManager: audioModeSetting.getSettingValue() == 1L,set audio_devices_out_active=AUDIO_HDMI,AUDIO_CODEC");
            }
        }
        Log.i(TAG, "initAudioManager: " + audioManager.getParameters("audio_devices_out_active"));
        Log.i(TAG, "initAudioManager: " + Arrays.toString(audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)));
        MediaRouter mediaRouter = (MediaRouter) getSystemService(Context.MEDIA_ROUTER_SERVICE);
        if (mediaRouter == null) {
            Log.e(TAG, "initAudioManager: mediaRouter is null");
            return;
        }
        MediaRouter.RouteInfo selectedRoute = mediaRouter.getSelectedRoute(ROUTE_TYPE_LIVE_AUDIO);
        Log.d(TAG, "initAudioManager: " + selectedRoute.getName());
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

    @Override
    public void onDestroy(@NonNull @NotNull LifecycleOwner owner) {
        customSettingDatabase.close();
        sceneDatabase.close();
        unregisterReceiver(dmbBroadcastReceiver);
    }

    /**
     * 获取当前的进程名
     *
     * @param context:上下文
     * @return :返回值
     */
    public String getCurrentProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
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
            if (DataReadWriteUtil.selectSceneVO != null) {
                // 如果已经有了选中播放的场景,就直接跳过了
                Log.w(TAG, "handleMessage: 已经有选中的播放场景,跳过 USB 自动跳转...");
                return;
            }
            if (msg.what == MESSAGE_JUMP_DEFAULT_ACTIVITY) {
                // 双重检查USB是否连接
                if (!DataReadWriteUtil.USB_READY) {
                    new AlertDialog.Builder(getApplicationContext(), com.xuexiang.xui.R.style.Base_Theme_MaterialComponents_Light_Dialog_MinWidth).setTitle("缺少DMB设备").setMessage("当前没有读取到任何的DMB设备信息,请插上DMB设备!").setPositiveButton("确定", null).show();
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
                if (DataReadWriteUtil.selectSceneVO != null) {
                    intent.setClass(getApplicationContext(), MainActivity.getActivityBySceneType(DataReadWriteUtil.selectSceneVO.getSceneType()));
                } else {
                    intent.setClass(getApplicationContext(), MainActivity.getActivityBySceneType(defaultScene.getSceneType()));
                }
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                Toast.makeText(getApplicationContext(), "正在跳转...", Toast.LENGTH_SHORT).show();
                // 跳转
                startActivity(intent);
            }
        }
    }
}
