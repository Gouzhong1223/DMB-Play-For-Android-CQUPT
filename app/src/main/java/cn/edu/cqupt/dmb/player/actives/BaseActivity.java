package cn.edu.cqupt.dmb.player.actives;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import cn.edu.cqupt.dmb.player.common.CustomSettingByKey;
import cn.edu.cqupt.dmb.player.db.database.CustomSettingDatabase;
import cn.edu.cqupt.dmb.player.db.mapper.CustomSettingMapper;
import cn.edu.cqupt.dmb.player.domain.CustomSetting;
import cn.edu.cqupt.dmb.player.domain.SceneVO;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;

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
public class BaseActivity extends FragmentActivity {

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
     * PIP 输出流
     */
    protected PipedOutputStream pipedOutputStream;
    /**
     * 输入缓冲流
     */
    protected BufferedInputStream bufferedInputStream;
    /**
     * 数据库
     */
    private CustomSettingDatabase customSettingDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 获取父传递过来的参数
        selectedSceneVO = (SceneVO) this.getIntent().getSerializableExtra(DetailsActivity.SCENE_VO);
        // 初始化数据库 Mapper
        initDataBase();
        // 加载默认设置
        loadCustomSetting();
        // 初始化 PIP 管道
        initPip();
        Toast.makeText(this, "正在初始化...", Toast.LENGTH_SHORT).show();
    }

    /**
     * 初始化管道流
     */
    private void initPip() {
        PipedInputStream pipedInputStream = new PipedInputStream(1024 * 1024 * 10);
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
        customSettingDatabase = Room.databaseBuilder(this, CustomSettingDatabase.class, "custom_setting_database")
                .allowMainThreadQueries().build();
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
    }

    @Override
    protected void onDestroy() {
        DataReadWriteUtil.inMainActivity = true;
        try {
            pipedOutputStream.close();
            bufferedInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        customSettingDatabase.close();
        super.onDestroy();
        Toast.makeText(this, "已退出...", Toast.LENGTH_SHORT).show();
    }
}
