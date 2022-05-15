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
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import androidx.room.Room;

import java.util.ArrayList;
import java.util.HashMap;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.common.CustomSettingByKey;
import cn.edu.cqupt.dmb.player.db.database.CustomSettingDatabase;
import cn.edu.cqupt.dmb.player.db.database.SceneDatabase;
import cn.edu.cqupt.dmb.player.db.mapper.CustomSettingMapper;
import cn.edu.cqupt.dmb.player.db.mapper.SceneMapper;
import cn.edu.cqupt.dmb.player.domain.CustomSetting;
import cn.edu.cqupt.dmb.player.domain.SceneInfo;
import cn.edu.cqupt.dmb.player.utils.DialogUtil;


@SuppressLint("ValidFragment")
public class PresetFragment extends Fragment {

    private static final String TAG = "PresetFragment";
    /**
     * 教学楼数据源
     */
    private final String[] building = new String[]{"二教", "三教", "四教", "五教", "八教"};
    /**
     * 播放类型
     */
    private final String[] playType = new String[]{"视频", "轮播图", "音频", "安全信息", "课表"};
    /**
     * 装载View的List
     */
    private final ArrayList<View> viewArrayList = new ArrayList<>();
    /**
     * 装载View的上一个背景
     */
    private final HashMap<View, Drawable> preBackground = new HashMap<>();
    /**
     * 父Context
     */
    private Context context;
    /**
     * Fragment的View
     */
    private View rootView;
    /**
     * 操作预设的Mapper
     */
    private SceneMapper sceneMapper;
    /**
     * 操作自定义设置的Mapper
     */
    private CustomSettingMapper customSettingMapper;
    /**
     * 场景名字输入文本框
     */
    private EditText sceneNameEditText;
    /**
     * 频点输入文本框
     */
    private EditText frequencyEditText;
    /**
     * 设备ID输入文本框
     */
    private EditText idEditText;
    /**
     * 播放类型下拉框
     */
    private Spinner playTypeSpinner;
    /**
     * 教学楼下拉框
     */
    private Spinner buildingSpinner;
    /**
     * 提交按钮
     */
    private Button button;
    /**
     * RelativeLayout 布局
     */
    private RelativeLayout buildingRelativeLayout;


    @SuppressLint("ValidFragment")
    public PresetFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化数据库Mapper
        initDataBase();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
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
            rootView = inflater.inflate(R.layout.fragment_preset, container, false);
        }
        // 初始化组件
        initView(rootView);
        // 装配组件
        configView();
        return rootView;
    }

    /**
     * 初始化View
     *
     * @param rootView Fragment中的View
     */
    private void initView(View rootView) {
        sceneNameEditText = rootView.findViewById(R.id.sceneNameEdit);
        frequencyEditText = rootView.findViewById(R.id.frequencyEdit);
        idEditText = rootView.findViewById(R.id.idEdit);
        playTypeSpinner = rootView.findViewById(R.id.playType);
        buildingSpinner = rootView.findViewById(R.id.playBuilding);
        button = rootView.findViewById(R.id.submit_btn);
        buildingRelativeLayout = rootView.findViewById(R.id.buildingRelativeLayout);
        viewArrayList.add(sceneNameEditText);
        viewArrayList.add(frequencyEditText);
        viewArrayList.add(idEditText);
        viewArrayList.add(playTypeSpinner);
        viewArrayList.add(buildingSpinner);
        viewArrayList.add(button);
    }

    /**
     * 配置各个View
     */
    @SuppressLint({"UseCompatLoadingForDrawables", "PrivateResource"})
    private void configView() {
        // 设置各个组件的焦点监听器
        for (View view : viewArrayList) {
            view.setOnFocusChangeListener((view1, b) -> {
                Drawable background;
                if (!preBackground.containsKey(view)) {
                    // 如果Map中没有上一个背景，就将当前的背景放入Map
                    background = view.getBackground();
                    preBackground.put(view, background);
                } else {
                    // 有背景就直接取出
                    background = preBackground.get(view);
                }
                if (b) {
                    // 有焦点就设置一下背景
                    view.setBackground(context.getDrawable(android.R.drawable.list_selector_background));
                } else {
                    // 没有焦点就设置为上一个背景
                    view1.setBackground(background);
                    // 移除这一个Key-Value
                    preBackground.remove(view);
                }
            });
        }
        // 设置播放类型下拉框数据源
        playTypeSpinner.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, playType));
        // 设置教学楼下拉框数据源
        buildingSpinner.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, building));
        // 设置播放类型下拉选择监听器
        playTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (playType[i].equals("课表")) {
                    // 如果选择的是课表，就把buildingRelativeLayout设置为可见
                    buildingRelativeLayout.setVisibility(View.VISIBLE);
                } else {
                    // 如果不是就把buildingRelativeLayout设置为不可见
                    buildingRelativeLayout.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.i(TAG, "onNothingSelected: ");
            }
        });

        // 提交按钮设置监听器
        button.setOnClickListener(view -> {
            // 获取输入的预设名字
            String sceneName = sceneNameEditText.getText().toString();
            // 获取输入的频点
            String frequency = frequencyEditText.getText().toString();
            // 获取输入的设备ID
            String id = idEditText.getText().toString();
            // 获取输入的播放类型
            String playType = playTypeSpinner.getSelectedItem().toString();
            String building;
            // 校验参数
            if (!validationParameters(sceneName, frequency, id)) {
                return;
            }
            // 构造预设实体类
            SceneInfo sceneInfo = new SceneInfo();
            sceneInfo.setSceneName(sceneName);
            sceneInfo.setFrequency(Integer.parseInt(frequency));
            sceneInfo.setSceneId(Integer.parseInt(id));
            sceneInfo.setSceneType(getSceneTypeBySceneStr(playType));
            if (playType.equals("课表")) {
                // 获取输入的教学楼
                building = buildingSpinner.getSelectedItem().toString();
                sceneInfo.setBuilding(getBuildingNumByBuildingStr(building));
            }
            // 在数据库中插入预设
            sceneMapper.insertScene(sceneInfo);
            // 弹出对话框提示成功
            DialogUtil.generateDialog(context, "保存成功！", "预设已经成功保存啦！添加成功的预设都会在主页面显示哦！同时还可以设置一个默认的预设作为APP启动时载入的场景！", new DialogUtil.PositiveButton(DialogUtil.DialogButtonEnum.POSITIVE, (dialog, which) -> {
                dialog.cancel();
                // 查询现有的默认使用场景
                CustomSetting customSetting = customSettingMapper.selectCustomSettingByKey(CustomSettingByKey.DEFAULT_SENSE.getKey());
                if (customSetting == null) {
                    // 如果没有默认的使用场景就提示:是否将当前的预设信息设置为默认的使用场景
                    DialogUtil.generateDialog(context, "设置自启动场景", "检测到您还没有设置默认的使用场景,是否将" + sceneName + "设置为APP默认的使用场景?", new DialogUtil.PositiveButton(DialogUtil.DialogButtonEnum.NEGATIVE, (dialog1, which1) -> dialog1.cancel(), "取消"), new DialogUtil.PositiveButton(DialogUtil.DialogButtonEnum.POSITIVE, (dialog12, which12) -> {
                        // 构造一个关于默认使用场景的自定义设置实体
                        CustomSetting customSettingRecord = new CustomSetting();
                        SceneInfo sceneInfoRecord = sceneMapper.selectSceneByScreenName(sceneName);
                        customSettingRecord.setSettingKey(CustomSettingByKey.DEFAULT_SENSE.getKey());
                        customSettingRecord.setSettingValue(Long.valueOf(sceneInfoRecord.getId()));
                        // 插入默认使用场景的设置
                        customSettingMapper.insertCustomSetting(customSettingRecord);
                    }, "确定")).show();
                }
            }, "确定")).show();
        });
    }

    /**
     * 根据选择的播放类型获取播放类型的ID
     *
     * @param sceneStr 播放类型字符串
     * @return 播放类型ID
     */
    private Integer getSceneTypeBySceneStr(String sceneStr) {
        int type = -1;
        for (int i = 0; i < playType.length; i++) {
            if (playType[i].equals(sceneStr)) {
                type = i;
            }
        }
        return type;
    }

    /**
     * 根据教学楼字符串获取教学楼的ID
     *
     * @param buildingStr 教学楼字符串
     * @return 教学楼ID
     */
    private Integer getBuildingNumByBuildingStr(String buildingStr) {
        int type = -1;
        for (int i = 0; i < building.length; i++) {
            if (building[i].equals(buildingStr)) {
                type = i;
            }
        }
        return type;
    }

    /**
     * 校验输入参数
     *
     * @param sceneName 预设名称
     * @param frequency 频点
     * @param id        设备ID
     * @return true->校验通过
     */
    private boolean validationParameters(String sceneName, String frequency, String id) {
        if (sceneName.equals("")) {
            DialogUtil.generateDialog(context, "预设名字还没填呢！", "快去给你的自定义预设取个名字吧！", new DialogUtil.PositiveButton(DialogUtil.DialogButtonEnum.POSITIVE, (dialog, index) -> dialog.cancel(), "确定")).show();
            return false;
        }

        // 根据预设名称查询场景信息
        SceneInfo sceneInfo = sceneMapper.selectSceneByScreenName(sceneName);
        if (sceneInfo != null) {
            // 如果场景信息不为空提示预设名称重复
            DialogUtil.generateDialog(context, "预设名字重复啦！", "这个预设名字已经被占用啦！重新给预设取个名字吧！", new DialogUtil.PositiveButton(DialogUtil.DialogButtonEnum.POSITIVE, (dialog, index) -> dialog.cancel(), "确定")).show();
            return false;
        }
        if (frequency.equals("")) {
            DialogUtil.generateDialog(context, "工作频点名字还没填呢!", "快去填一下工作频点吧！", new DialogUtil.PositiveButton(DialogUtil.DialogButtonEnum.POSITIVE, (dialog, index) -> dialog.cancel(), "确定")).show();
            return false;
        }
        if (id.equals("")) {
            DialogUtil.generateDialog(context, "终端编号还没填呢！", "快去填一下终端编号吧！", new DialogUtil.PositiveButton(DialogUtil.DialogButtonEnum.POSITIVE, (dialog, index) -> dialog.cancel(), "确定")).show();
            return false;
        }

        // 根据频点和ID查询场景信息
        SceneInfo sceneInfo1 = sceneMapper.selectSceneBySceneIdAndFrequency(Integer.parseInt(id), Integer.parseInt(frequency));
        if (sceneInfo1 != null) {
            // 如果已经有相同的组合则提示预设信息重复
            DialogUtil.generateDialog(context, "这个预设已经有啦！", "有一个叫：" + sceneInfo1.getSceneName() + "的预设里面的终端编号和工作频点和你现在的设置是一样的，不用重复提交啦！", new DialogUtil.PositiveButton(DialogUtil.DialogButtonEnum.POSITIVE, (dialog, index) -> dialog.cancel(), "确定")).show();
            return false;
        }
        return true;
    }
}
