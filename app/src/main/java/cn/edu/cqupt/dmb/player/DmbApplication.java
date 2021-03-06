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
     * ?????????USB??????
     */
    private static final String ACTION_USB_PERMISSION = DmbPlayerConstant.ACTION_USB_PERMISSION.getDmbConstantDescribe();
    /**
     * ??????????????????????????????
     */
    private static final int MESSAGE_JUMP_DEFAULT_ACTIVITY = DmbPlayerConstant.MESSAGE_JUMP_DEFAULT_ACTIVITY.getDmbConstantValue();
    private static final String TAG = "DmbApplication";
    /**
     * USB???????????????
     */
    private DmbBroadcastReceiver dmbBroadcastReceiver;
    /**
     * ?????? CustomSettingMapper ??? Mapper
     */
    private CustomSettingMapper customSettingMapper;
    /**
     * ?????? SceneMapper ??? Mapper
     */
    private SceneMapper sceneMapper;
    /**
     * ?????????????????????
     */
    private SceneInfo defaultScene;
    /**
     * ???????????????????????? Mapper
     */
    private CustomSettingDatabase customSettingDatabase;
    /**
     * ????????????????????? Mapper
     */
    private SceneDatabase sceneDatabase;

    @Override
    public void onCreate() {
        String curProcessName = getCurrentProcessName(this);
        if (!curProcessName.equals(getPackageName())) {
            return;
        }
        super.onCreate();
        Log.i(TAG, "onCreate: ????????? XUI");
        XUI.init(this);
        XUI.debug(false);
        CrashHandler crashHandler = CrashHandler.getInstance();
        Log.i(TAG, "onCreate: ??????????????????????????????");
        crashHandler.init(getApplicationContext());
        // ??????????????????
        initDataBase();
        // ???????????????????????????
        initDefaultScene();
        // ?????????????????????
        initAudioManager();
        // ??????Handler
        MainHandler mainHandler = new MainHandler(Looper.getMainLooper());
        // ???????????? USB
        Log.i(TAG, "onCreate: ???????????? USB");
        firstInitMainActivity(this, mainHandler);
    }

    /**
     * ??????????????????
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
     * ??????????????? USB ??????
     *
     * @param context ctx
     */
    private void firstInitMainActivity(Context context, Handler handler) {
        new Thread(() -> {
            // ??????????????????????????? USB ?????????????????????????????????
            if (!DataReadWriteUtil.isFirstInitMainActivity) {
                Log.i(TAG, "firstInitMainActivity: ??????????????? USB,????????????");
                return;
            }
            // ?????????USB???????????????
            IntentFilter intentFilter = initIntentFilter();
            // ????????????
            Log.i(TAG, "firstInitMainActivity: ??????DmbBroadcastReceiver??????");
            dmbBroadcastReceiver = DmbBroadcastReceiver.getInstance(context, handler, defaultScene);
            registerReceiver(dmbBroadcastReceiver, intentFilter);
            if (!DataReadWriteUtil.USB_READY) {
                Log.i(TAG, "firstInitMainActivity: ????????????????????? USB??????");
                dmbBroadcastReceiver.tryConnectUsbDeviceAndLoadUsbData();
            }
            DataReadWriteUtil.isFirstInitMainActivity = false;
        }).start();
    }

    /**
     * ???????????????????????????
     */
    private void initDefaultScene() {
        // ?????????????????????
        CustomSetting defaultSceneSetting = customSettingMapper.selectCustomSettingByKey(CustomSettingByKey.DEFAULT_SENSE.getKey());
        if (defaultSceneSetting != null) {
            defaultScene = sceneMapper.selectSceneById(Math.toIntExact(defaultSceneSetting.getSettingValue()));
        }
    }

    /**
     * ?????????????????????
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
     * ?????????USB???????????????
     *
     * @return ??????????????????IntentFilter
     */
    private IntentFilter initIntentFilter() {
        IntentFilter filter = new IntentFilter();
        // ??????USB?????????????????????
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        return filter;
    }

    /**
     * ?????? SceneInfo?????? SceneVO
     *
     * @param sceneInfo ??????????????????SceneInfo
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
     * ????????????????????????
     *
     * @param context:?????????
     * @return :?????????
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
     * ?????????????????? USB ????????????????????????????????????,???????????????????????????????????????
     */
    private class MainHandler extends Handler {

        public MainHandler(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (DataReadWriteUtil.selectSceneVO != null) {
                // ???????????????????????????????????????,??????????????????
                Log.w(TAG, "handleMessage: ??????????????????????????????,?????? USB ????????????...");
                return;
            }
            if (msg.what == MESSAGE_JUMP_DEFAULT_ACTIVITY) {
                // ????????????USB????????????
                if (!DataReadWriteUtil.USB_READY) {
                    new AlertDialog.Builder(getApplicationContext(), com.xuexiang.xui.R.style.Base_Theme_MaterialComponents_Light_Dialog_MinWidth).setTitle("??????DMB??????").setMessage("??????????????????????????????DMB????????????,?????????DMB??????!").setPositiveButton("??????", null).show();
                    return;
                }
                if (defaultScene == null) {
                    // ???????????????????????????????????????????????????????????????
                    return;
                }
                Intent intent = new Intent();
                // ?????????????????????????????????????????????
                intent.putExtra(DetailsActivity.SCENE_VO, getSceneVO(defaultScene));
                // ???????????????????????????
                if (DataReadWriteUtil.selectSceneVO != null) {
                    intent.setClass(getApplicationContext(), MainActivity.getActivityBySceneType(DataReadWriteUtil.selectSceneVO.getSceneType()));
                } else {
                    intent.setClass(getApplicationContext(), MainActivity.getActivityBySceneType(defaultScene.getSceneType()));
                }
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                Toast.makeText(getApplicationContext(), "????????????...", Toast.LENGTH_SHORT).show();
                // ??????
                startActivity(intent);
            }
        }
    }
}
