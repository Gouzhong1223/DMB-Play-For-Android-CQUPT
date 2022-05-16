package cn.edu.cqupt.dmb.player.actives;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Objects;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.common.FrequencyModule;
import cn.edu.cqupt.dmb.player.utils.DmbUtil;

/**
 * 这是设置页面的 Activity
 */
@Deprecated
public class SettingActivity extends Activity {

    private static final String TAG = "SettingActivity";
    /**
     * 教学楼数据源
     */
    String[] building = new String[]{"二教", "三教", "四教", "五教", "八教"};
    /**
     * 使用场景
     */
    String[] scenes = new String[]{"户外屏播放图片", "户外屏播放视频", "教学楼课表", "宿舍安全信息", "音频"};
    /**
     * 使用场景的下拉框
     */
    private Spinner scenesSpinner;
    /**
     * 教学楼下拉框
     */
    private Spinner buildingSpinner;
    /**
     * 确定按钮
     */
    private Button sureButton;
    /**
     * 提示文本
     */
    private TextView hintTextView;
    /**
     * 包含输入教学楼的组件<br/>
     * 默认不可见
     */
    private LinearLayout editBuildingLinearLayout;
    /**
     * 显示默认设置的TextView
     */
    private TextView defaultTextView;
    /**
     * 应用场景枚举
     */
    private FrequencyModule frequencyModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_main);
        // 初始化 View
        initView();
        // 配置一下 View
        configView();
        // 加载默认的 FrequencyModule
        loadConfigFromSharedPreferences();
    }

    /**
     * 初始化 View
     */
    private void initView() {
        editBuildingLinearLayout = findViewById(R.id.edit_building);
        scenesSpinner = findViewById(R.id.scenes_spinner);
        buildingSpinner = findViewById(R.id.building_spinner);
        sureButton = findViewById(R.id.sure);
        hintTextView = findViewById(R.id.hint_text_view);
        defaultTextView = findViewById(R.id.default_text_view);
    }

    /**
     * 配置View
     */
    @SuppressLint("SetTextI18n")
    private void configView() {
        // 为使用场景下拉框设置数据源
        scenesSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, scenes));
        // 这个是教学楼下拉框数据源
        buildingSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, building));
        // 设置使用场景下拉框的监听器
        scenesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectScenes = (String) adapterView.getItemAtPosition(i);
                if (Objects.equals(selectScenes, scenes[2])) {
                    // 如果选中的使用场景是教学楼课表,就把选择教学楼的 View 打开
                    editBuildingLinearLayout.setVisibility(View.VISIBLE);
                } else {
                    // 反之就直接关闭这个 View,不让用户选择
                    editBuildingLinearLayout.setVisibility(View.INVISIBLE);
                    // 根据选择的应用场景选择 FrequencyModule
                    frequencyModule = selectFrequencyModuleByScenes(selectScenes);
                }
                hintTextView.setText("当前选择的配置是:" + frequencyModule);
                Log.i(TAG, "现在选的应用场景是:" + selectScenes);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.i(TAG, "打开了下拉框,但没选!");
            }
        });
        // 设置教学楼下拉框监听器
        buildingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (editBuildingLinearLayout.getVisibility() == View.VISIBLE) {
                    // 获取选中的教学楼
                    String selectBuilding = (String) adapterView.getItemAtPosition(i);
                    // 根据教学楼选择FrequencyModule
                    frequencyModule = selectFrequencyModuleByBuilding(selectBuilding);
                    hintTextView.setText("当前选择的配置是:" + frequencyModule);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.i(TAG, "打开了下拉框,但没选!");
            }
        });
        // 设置确定按钮的监听器
        sureButton.setOnClickListener(view -> {
            if (frequencyModule == null) {
                new AlertDialog.Builder(
                        this)
                        .setTitle("未选择任何配置")
                        .setMessage("请确认是否选择相应配置")
                        .setPositiveButton("确定", null)
                        .show();
            } else {
                DmbUtil.putInt(this, "defaultFrequencyModule", frequencyModule.getSerialNumber());
                new AlertDialog.Builder(
                        this)
                        .setTitle("成功")
                        .setMessage("保存成功,下次进入 APP 将会自动进入您选择的模块!")
                        .setPositiveButton("确定", null)
                        .show();
                defaultTextView.setText("当前选择的配置是:" + frequencyModule);
            }
        });
    }

    /**
     * 根据教学楼选择 FrequencyModule<br/>
     * {"二教", "三教", "四教", "五教", "八教"}
     *
     * @param building 教学楼
     * @return FrequencyModule枚举
     */
    private FrequencyModule selectFrequencyModuleByBuilding(String building) {
        switch (building) {
            case "二教":
                return FrequencyModule.CURRICULUM_64;
            case "三教":
                return FrequencyModule.CURRICULUM_32;
            case "四教":
                return FrequencyModule.CURRICULUM_16;
            case "五教":
                return FrequencyModule.CURRICULUM_8;
            default:
                return FrequencyModule.CURRICULUM_4;
        }
    }

    /**
     * 根据选择的应用场景选择 FrequencyModule<br/>
     * {"户外屏播放图片", "户外屏播放视频", "宿舍安全信息", "音频"}<br/>
     * 注意!教学楼使用场景不包含在这个 case 中
     *
     * @param scenes 应用场景
     * @return FrequencyModule
     */
    private FrequencyModule selectFrequencyModuleByScenes(String scenes) {
        switch (scenes) {
            case "户外屏播放图片":
                return FrequencyModule.OUTDOOR_SCREEN_TPEG;
            case "户外屏播放视频":
                return FrequencyModule.OUTDOOR_SCREEN_VIDEO;
            case "宿舍安全信息":
                return FrequencyModule.DORMITORY_SAFETY;
            default:
                return FrequencyModule.AUDIO;
        }
    }

    /**
     * 从SharedPreferences中加载默认的FrequencyModule设置
     */
    @SuppressLint("SetTextI18n")
    private void loadConfigFromSharedPreferences() {
        int serialNumber = DmbUtil.getInt(this, "defaultFrequencyModule", 20);
        FrequencyModule defaultFrequencyModule = FrequencyModule.getFrequencyModuleBySerialNumber(serialNumber);
        if (defaultFrequencyModule == null) {
            frequencyModule = null;
            defaultTextView.setText("没有默认配置");
        }
        frequencyModule = defaultFrequencyModule;
        defaultTextView.setText("现在的默认配置是:" + frequencyModule);
    }

}
