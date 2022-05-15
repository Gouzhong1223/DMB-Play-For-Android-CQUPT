package cn.edu.cqupt.dmb.player.actives.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.widget.SwitchCompat;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.common.CustomSettingByKey;
import cn.edu.cqupt.dmb.player.db.database.CustomSettingDatabase;
import cn.edu.cqupt.dmb.player.db.database.SceneDatabase;
import cn.edu.cqupt.dmb.player.db.mapper.CustomSettingMapper;
import cn.edu.cqupt.dmb.player.db.mapper.SceneMapper;
import cn.edu.cqupt.dmb.player.domain.CustomSetting;
import cn.edu.cqupt.dmb.player.domain.SceneInfo;


public class PlaySettingFragment extends Fragment {

    private final String TAG = "PlaySettingFragment";

    /**
     * 装载View的List
     */
    private final List<View> viewList = new ArrayList<>();
    /**
     * 装载View的前一个背景的Map
     */
    private final HashMap<View, Drawable> preBackground = new HashMap<>();
    /**
     * 轮播图数量
     */
    private final Integer[] carouselNum = new Integer[]{3, 4, 5, 6, 7, 8};
    /**
     * 预设名称和ID的Map
     */
    private final HashMap<String, Long> sceneIdMap = new HashMap<>();
    /**
     * 父Context
     */
    private Context context;
    /**
     * Fragment里面的View
     */
    private View rootView;
    /**
     * 操作场景的Mapper
     */
    private SceneMapper sceneMapper;
    /**
     * 默认使用场景下拉框
     */
    private Spinner defaultSceneSpinner;
    /**
     * 轮播图数量下拉框
     */
    private Spinner carouselNumSpinner;
    /**
     * 是否开启信号显示开关
     */
    private SwitchCompat showSignalSwitch;
    /**
     * 操作默认设置的Mapper
     */
    private CustomSettingMapper customSettingMapper;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDataBase();
    }

    /**
     * 初始化数据库
     */
    private void initDataBase() {
        sceneMapper = Room.databaseBuilder(context, SceneDatabase.class, "scene_database") //new a database
                .allowMainThreadQueries().build().getSceneMapper();

        customSettingMapper = Room.databaseBuilder(context, CustomSettingDatabase.class, "custom_setting_database") //new a database
                .allowMainThreadQueries().build().getCustomSettingMapper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_play_setting, container, false);
        }
        // Inflate the layout for this fragment
        initView(rootView);
        configView();
        return rootView;
    }

    /**
     * 初始化View
     *
     * @param rootView fragment里面的View
     */
    private void initView(View rootView) {
        showSignalSwitch = rootView.findViewById(R.id.SwitchCompat);
        defaultSceneSpinner = rootView.findViewById(R.id.default_sense_spinner);
        carouselNumSpinner = rootView.findViewById(R.id.carousel_num_spinner);
        viewList.add(showSignalSwitch);
        viewList.add(defaultSceneSpinner);
        viewList.add(carouselNumSpinner);
    }

    /**
     * 配置View
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private void configView() {
        // 给View设置背景
        for (View view : viewList) {
            view.setFocusable(true);
            view.setOnFocusChangeListener((view1, b) -> {
                Drawable background;
                // 判断缓存里面是否有当前View的上一个背景
                if (!preBackground.containsKey(view)) {
                    // 如果没有就取出当前View的背景并放入缓存
                    background = view.getBackground();
                    preBackground.put(view, background);
                } else {
                    // 如果有就取出上一个背景
                    background = preBackground.get(view);
                }
                if (b) {
                    // 如果有焦点就重新设置背景
                    view1.setBackground(context.getDrawable(android.R.drawable.list_selector_background));
                } else {
                    // 如果没有焦点就设置为View的上一个背景
                    view1.setBackground(background);
                    preBackground.remove(view);
                }
            });
        }
        // 装载使用场景名字的List
        List<String> sceneNames = new ArrayList<>();

        // 查询使用场景并将使用场景的名字和ID装配到sceneIdMap中
        for (SceneInfo selectAllScene : sceneMapper.selectAllScenes()) {
            sceneNames.add(selectAllScene.getSceneName());
            sceneIdMap.put(selectAllScene.getSceneName(), Long.valueOf(selectAllScene.getSceneId()));
        }
        // 设置轮播图下拉框的数据源
        carouselNumSpinner.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, carouselNum));
        // 设置默认使用场景下拉框的数据源
        defaultSceneSpinner.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, sceneNames));
        defaultSceneSpinner.setFocusable(true);
        defaultSceneSpinner.setClickable(true);
        // 设置默认使用场景下拉框的选择监听器
        defaultSceneSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                CustomSetting customSetting = customSettingMapper.selectCustomSettingByKey(CustomSettingByKey.DEFAULT_SENSE.getKey());
                if (customSetting == null) {
                    customSetting = new CustomSetting();
                    customSetting.setSettingKey(CustomSettingByKey.DEFAULT_SENSE.getKey());
                    customSetting.setSettingValue(sceneIdMap.get(sceneNames.get(i)));
                    customSettingMapper.insertCustomSetting(customSetting);
                } else {
                    Long sceneId = customSetting.getSettingValue();
                    if (!Objects.equals(sceneIdMap.get(sceneNames.get(i)), sceneId)) {
                        customSetting.setSettingValue(sceneId);
                        customSettingMapper.updateCustomSetting(customSetting);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.i(TAG, "onNothingSelected: ");
            }
        });
        CustomSetting setting = customSettingMapper.selectCustomSettingByKey(CustomSettingByKey.OPEN_SIGNAL.getKey());
        if (setting == null) {
            showSignalSwitch.setChecked(false);
        } else {
            boolean flag = setting.getSettingValue() == 1;
            showSignalSwitch.setChecked(flag);
        }
        showSignalSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            Log.i(TAG, "onCheckedChanged: 现在的设置是:" + b);
            CustomSetting customSetting = customSettingMapper.selectCustomSettingByKey(CustomSettingByKey.OPEN_SIGNAL.getKey());
            if (customSetting == null) {
                CustomSetting customSettingRecord = new CustomSetting();
                customSettingRecord.setSettingKey(CustomSettingByKey.OPEN_SIGNAL.getKey());
                customSettingRecord.setSettingValue((long) (b ? 1 : 0));
                customSettingMapper.insertCustomSetting(customSettingRecord);
            } else {
                customSetting.setSettingValue((long) (b ? 1 : 0));
                customSettingMapper.updateCustomSetting(customSetting);
            }
        });

    }
}
