package cn.edu.cqupt.dmb.player.actives;

import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.common.CustomSettingByKey;
import cn.edu.cqupt.dmb.player.db.database.CustomSettingDatabase;
import cn.edu.cqupt.dmb.player.db.mapper.CustomSettingMapper;
import cn.edu.cqupt.dmb.player.decoder.FicDecoder;
import cn.edu.cqupt.dmb.player.domain.CustomSetting;
import cn.edu.cqupt.dmb.player.domain.SceneVO;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;
import cn.edu.cqupt.dmb.player.utils.UsbUtil;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这个是抽象的 Activity ,里面包含了一些通用的初始化方法
 * @Date : create by QingSong in 2022-05-18 13:14
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.actives
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public abstract class BaseActivity extends FragmentActivity {

    private static final String TAG = "BaseActivity";
    /**
     * 选中的使用场景配置
     */
    protected SceneVO selectedSceneVO;
    /**
     * 操作自定义设置的 Mapper
     */
    protected CustomSettingMapper customSettingMapper;
    /**
     * 自定义的轮播图数量设置
     */
    protected CustomSetting defaultCarouselNumSetting;
    /**
     * 是否显示信号的设置
     */
    protected CustomSetting defaultSignalShowSetting;
    /**
     * 是否显示信号的设置
     */
    protected CustomSetting showDebugLogSetting;
    /**
     * PIP 输出流
     */
    protected PipedOutputStream pipedOutputStream;
    /**
     * 输入缓冲流
     */
    protected BufferedInputStream bufferedInputStream;
    /**
     * Fic解码器
     */
    protected FicDecoder ficDecoder;
    /**
     * PIP输入流
     */
    private PipedInputStream pipedInputStream;
    /**
     * 数据库
     */
    private CustomSettingDatabase customSettingDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 强制全屏,全的不能再全的那种了
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(getLayoutResIdByClassName(getLocalClassName()));
        initActivity();
    }

    /**
     * 初始化 Activity
     */
    private void initActivity() {
        DataReadWriteUtil.inMainActivity = true;
        // 获取父传递过来的参数
        selectedSceneVO = (SceneVO) this.getIntent().getSerializableExtra(DetailsActivity.SCENE_VO);
        // 初始化数据库 Mapper
        initDataBase();
        // 加载默认设置
        loadCustomSetting();
        // 初始化 PIP 管道
        initPip();
        // 初始化组件
        initView();
        // 配置组件
        configView();
        // 重置 Dongle 的状态
        resetDongle();
        // 开始进行解码
        startDecode();
        // 开始接收 DMB 数据
        UsbUtil.startReceiveDmbData(pipedOutputStream);
        Log.i(TAG, "onCreate: " + getLocalClassName());
        Toast.makeText(this, "正在初始化..." + getLocalClassName(), Toast.LENGTH_LONG).show();
    }

    /**
     * 根据类名获取 ResId
     *
     * @param className 类名
     * @return ResId
     */
    private int getLayoutResIdByClassName(String className) {
        switch (className) {
            case "actives.CarouselActivity":
                return R.layout.activity_carousel;
            case "actives.AudioActivity":
                return R.layout.activity_audio;
            case "actives.CurriculumActivity":
                return R.layout.activity_curriculum;
            case "actives.DormitorySafetyActivity":
                return R.layout.activity_dormitory;
            default:
                return R.layout.activity_video;
        }
    }

    /**
     * 初始化组件
     */
    protected abstract void initView();

    /**
     * 配置组件属性
     */
    protected abstract void configView();

    /**
     * 开始解码工作
     */
    protected abstract void startDecode();

    /**
     * 重置 Dongle 的状态
     */
    private void resetDongle() {
        // 获取Fic解码器
        ficDecoder = FicDecoder.getInstance(selectedSceneVO.getDeviceId(), true);
        // 重置一下dongle
        UsbUtil.restdongle(ficDecoder, selectedSceneVO);
    }

    /**
     * 初始化管道流
     */
    private void initPip() {
        pipedInputStream = new PipedInputStream(1024 * 1024 * 10);
        pipedOutputStream = new PipedOutputStream();
        try {
            pipedOutputStream.connect(pipedInputStream);
            bufferedInputStream = new BufferedInputStream(pipedInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化数据库
     */
    private void initDataBase() {
        customSettingDatabase = Room.databaseBuilder(this, CustomSettingDatabase.class, "custom_setting_database").allowMainThreadQueries().build();
        //new a database
        customSettingMapper = customSettingDatabase.getCustomSettingMapper();
    }

    /**
     * 加载自定义设置
     */
    private void loadCustomSetting() {
        // 查询轮播图数量设置
        defaultCarouselNumSetting = customSettingMapper.selectCustomSettingByKey(CustomSettingByKey.DEFAULT_CAROUSEL_NUM.getKey());
        if (defaultCarouselNumSetting == null) {
            defaultCarouselNumSetting = new CustomSetting();
            // 没有设置的话,默认设置成 5 张
            defaultCarouselNumSetting.setSettingValue(5L);
            defaultCarouselNumSetting.setSettingKey(CustomSettingByKey.DEFAULT_CAROUSEL_NUM.getKey());
            customSettingMapper.insertCustomSetting(defaultCarouselNumSetting);
        }
        // 查询信号显示设置
        defaultSignalShowSetting = customSettingMapper.selectCustomSettingByKey(CustomSettingByKey.OPEN_SIGNAL.getKey());
        showDebugLogSetting = customSettingMapper.selectCustomSettingByKey(CustomSettingByKey.SHOW_DEBUG_LOG.getKey());
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy: 重置 USB 标志位...");
        DataReadWriteUtil.inMainActivity = true;
        // 重置被选中的播放模块
        Log.i(TAG, "onDestroy: 重置选中播放的场景...");
        DataReadWriteUtil.selectSceneVO = null;
        try {
            if (pipedOutputStream != null) {
                pipedOutputStream.close();
            }
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
            if (pipedInputStream != null) {
                pipedInputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (customSettingDatabase != null) {
            customSettingDatabase.close();
        }
        super.onDestroy();
        Toast.makeText(this, getLocalClassName() + "已退出...", Toast.LENGTH_LONG).show();
    }
}
