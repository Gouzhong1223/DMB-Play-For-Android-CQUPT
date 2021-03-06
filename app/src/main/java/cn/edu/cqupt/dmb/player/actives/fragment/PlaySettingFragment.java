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

package cn.edu.cqupt.dmb.player.actives.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;

import com.xuexiang.xui.widget.alpha.XUIAlphaTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.common.CustomSettingByKey;
import cn.edu.cqupt.dmb.player.domain.CustomSetting;
import cn.edu.cqupt.dmb.player.domain.SceneInfo;


/**
 * @author qingsong
 */
public class PlaySettingFragment extends DmbBaseFragment {

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
    private final Integer[] carouselNum = new Integer[]{5, 6, 7, 8, 9, 10};
    /**
     * 预设名称和ID的Map
     */
    private final HashMap<String, Long> sceneIdMap = new HashMap<>();

    /**
     * Fragment里面的View
     */
    private View rootView;

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
     * 是否开启调试日志显示
     */
    private SwitchCompat deBugLogSwitch;

    /**
     * 音频输出模式文本组件
     */
    private XUIAlphaTextView audioModeTextView;

    /**
     * 音频输出模式切换开关
     */
    private SwitchCompat audioOutputModeSwitchCompat;

    /**
     * 音频管理器
     */
    private AudioManager audioManager;

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
        deBugLogSwitch = rootView.findViewById(R.id.deBugLogSwitchCompat);
        defaultSceneSpinner = rootView.findViewById(R.id.default_sense_spinner);
        carouselNumSpinner = rootView.findViewById(R.id.carousel_num_spinner);
        audioModeTextView = rootView.findViewById(R.id.Audio_output_modeTextView);
        audioOutputModeSwitchCompat = rootView.findViewById(R.id.audioOutputModeSwitchCompat);
        viewList.add(showSignalSwitch);
        viewList.add(deBugLogSwitch);
        viewList.add(defaultSceneSpinner);
        viewList.add(carouselNumSpinner);
        viewList.add(audioOutputModeSwitchCompat);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * 设置 View 的焦点监听器
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private void configViewOnFocusChangeListener() {
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
    }

    /**
     * 配置View
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private void configView() {
        configViewOnFocusChangeListener();
        // 装载使用场景名字的List
        List<String> sceneNames = new ArrayList<>();

        // 查询使用场景并将使用场景的名字和ID装配到sceneIdMap中
        for (SceneInfo selectAllScene : sceneMapper.selectAllScenes()) {
            sceneNames.add(selectAllScene.getSceneName());
            sceneIdMap.put(selectAllScene.getSceneName(), Long.valueOf(selectAllScene.getId()));
        }
        // 装配轮播图数量下拉框组件
        configCarouselNumSpinner();
        // 装配默认使用场景下拉框组件
        configDefaultSceneSpinner(sceneNames);
        // 装配信号显示开关组件
        configSwitch();
        // 装配调试日志开关组件
        configDeBugLogSwitch();
        // 装配音频输出模式组件
        configAudioModeComponent();
    }

    /**
     * 装配音频输出模式组件
     */
    @SuppressLint("SetTextI18n")
    private void configAudioModeComponent() {
        CustomSetting audioOutputModeSetting = customSettingMapper.selectCustomSettingByKey(CustomSettingByKey.AUDIO_OUTPUT_MODE.getKey());
        if (audioOutputModeSetting != null) {
            if (audioOutputModeSetting.getSettingValue() == 0L) {
                audioModeTextView.setText("音频输出模式:扬声器");
                audioOutputModeSwitchCompat.setChecked(false);
            } else {
                audioModeTextView.setText("音频输出模式:HDMI");
                audioOutputModeSwitchCompat.setChecked(true);
            }
        } else {
            audioModeTextView.setText("音频输出模式:扬声器");
        }

        audioOutputModeSwitchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            CustomSetting audioModeRecord = customSettingMapper.selectCustomSettingByKey(CustomSettingByKey.AUDIO_OUTPUT_MODE.getKey());
            if (audioModeRecord == null) {
                audioModeRecord = new CustomSetting();
                audioModeRecord.setSettingKey(CustomSettingByKey.AUDIO_OUTPUT_MODE.getKey());
                if (isChecked) {
                    audioModeTextView.setText("音频输出模式:HDMI");
                    audioModeRecord.setSettingValue(1L);
                } else {
                    audioModeTextView.setText("音频输出模式:扬声器");
                    audioModeRecord.setSettingValue(0L);
                }
                customSettingMapper.insertCustomSetting(audioModeRecord);
            } else {
                if (isChecked) {
                    audioModeTextView.setText("音频输出模式:HDMI");
                    audioModeRecord.setSettingValue(1L);
                } else {
                    audioModeTextView.setText("音频输出模式:扬声器");
                    audioModeRecord.setSettingValue(0L);
                }
                customSettingMapper.updateCustomSetting(audioModeRecord);
            }
            // 切换一下音频输出设备
            switchAudioOutputDevice(isChecked);
        });
    }

    /**
     * 切换音频输出设备
     *
     * @param isChecked 是否选中
     */
    private void switchAudioOutputDevice(boolean isChecked) {
        if (isChecked) {
            audioManager.setParameters("audio_devices_out_active=AUDIO_HDMI");
            Log.i(TAG, "switchAudioOutputDevice: 选中HDMI");
            Toast.makeText(context, "选中HDMI输出", Toast.LENGTH_SHORT).show();
        } else {
            audioManager.setParameters("audio_devices_out_active=AUDIO_CODEC");
            Log.i(TAG, "switchAudioOutputDevice: 选中扬声器");
            Toast.makeText(context, "选中扬声器输出", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 装配轮播图数量下拉框组件
     */
    private void configCarouselNumSpinner() {
        // 设置轮播图下拉框的数据源
        carouselNumSpinner.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, carouselNum));
        // 查询现有的轮播图数量设置
        CustomSetting carouselCustomSetting = customSettingMapper.selectCustomSettingByKey(CustomSettingByKey.DEFAULT_CAROUSEL_NUM.getKey());
        if (carouselCustomSetting != null) {
            for (int i = 0; i < carouselNum.length; i++) {
                Integer carouseN = carouselNum[i];
                Long settingValue = carouselCustomSetting.getSettingValue();
                if (Objects.equals(Long.valueOf(carouseN), settingValue)) {
                    // 绑定编号
                    carouselNumSpinner.setSelection(i);
                }
            }
        }
        // 设置轮播图数量下拉框的选择监听器
        carouselNumSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // 查询现有的轮播图数量设置
                CustomSetting carouselNumSetting = customSettingMapper.selectCustomSettingByKey(CustomSettingByKey.DEFAULT_CAROUSEL_NUM.getKey());
                Integer carouselN = carouselNum[i];
                if (carouselNumSetting == null) {
                    carouselNumSetting = new CustomSetting();
                    carouselNumSetting.setSettingKey(CustomSettingByKey.DEFAULT_CAROUSEL_NUM.getKey());
                    carouselNumSetting.setSettingValue(Long.valueOf(carouselN));
                    // 插入设置
                    customSettingMapper.insertCustomSetting(carouselNumSetting);
                } else {
                    carouselNumSetting.setSettingValue(Long.valueOf(carouselN));
                    // 更新设置
                    customSettingMapper.updateCustomSetting(carouselNumSetting);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.i(TAG, "onNothingSelected: ");
            }
        });
    }

    /**
     * 装配默认使用场景的下拉框
     *
     * @param sceneNames 场景预设
     */
    private void configDefaultSceneSpinner(List<String> sceneNames) {
        // 设置默认使用场景下拉框的数据源
        defaultSceneSpinner.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, sceneNames));
        defaultSceneSpinner.setFocusable(true);
        defaultSceneSpinner.setClickable(true);
        // 查询数据库中已有的默认使用场景设置
        CustomSetting defaultSceneSetting = customSettingMapper.selectCustomSettingByKey(CustomSettingByKey.DEFAULT_SENSE.getKey());
        if (defaultSceneSetting != null) {
            // 如果默认使用场景的设置不为空
            for (int i = 0; i < sceneNames.size(); i++) {
                // 在列表中对比,寻找出sceneNames中对应的索引
                if (Objects.equals(defaultSceneSetting.getSettingValue(), sceneIdMap.get(sceneNames.get(i)))) {
                    defaultSceneSpinner.setSelection(i);
                }
            }
        }
        // 设置默认使用场景下拉框的选择监听器
        defaultSceneSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                CustomSetting customSetting = customSettingMapper.selectCustomSettingByKey(CustomSettingByKey.DEFAULT_SENSE.getKey());
                if (customSetting == null) {
                    customSetting = new CustomSetting();
                    customSetting.setSettingKey(CustomSettingByKey.DEFAULT_SENSE.getKey());
                    customSetting.setSettingValue(sceneIdMap.get(sceneNames.get(i)));
                    // 插入设置
                    customSettingMapper.insertCustomSetting(customSetting);
                } else {
                    // 获取主键
                    Long sceneId = customSetting.getSettingValue();
                    if (!Objects.equals(sceneIdMap.get(sceneNames.get(i)), sceneId)) {
                        customSetting.setSettingValue(sceneIdMap.get(sceneNames.get(i)));
                        // 更新设置
                        customSettingMapper.updateCustomSetting(customSetting);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.i(TAG, "onNothingSelected: ");
            }
        });
    }

    /**
     * 装配开关组件
     */
    private void configSwitch() {
        // 查询默认的信号显示设置
        CustomSetting defaultSignalSetting = customSettingMapper.selectCustomSettingByKey(CustomSettingByKey.OPEN_SIGNAL.getKey());
        if (defaultSignalSetting == null) {
            showSignalSwitch.setChecked(false);
        } else {
            boolean flag = defaultSignalSetting.getSettingValue() == 1;
            // 如果有信号显示设置,就初始化一下开关
            showSignalSwitch.setChecked(flag);
        }
        // 给信号显示开关设置开关监听器
        showSignalSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            Log.i(TAG, "onCheckedChanged: 现在的设置是:" + b);
            CustomSetting customSetting = customSettingMapper.selectCustomSettingByKey(CustomSettingByKey.OPEN_SIGNAL.getKey());
            if (customSetting == null) {
                CustomSetting customSettingRecord = new CustomSetting();
                customSettingRecord.setSettingKey(CustomSettingByKey.OPEN_SIGNAL.getKey());
                customSettingRecord.setSettingValue((long) (b ? 1 : 0));
                // 插入信号显示设置
                customSettingMapper.insertCustomSetting(customSettingRecord);
            } else {
                customSetting.setSettingValue((long) (b ? 1 : 0));
                // 更新信号显示设置
                customSettingMapper.updateCustomSetting(customSetting);
            }
        });
    }

    /**
     * 装配日志显示开关组件
     */
    private void configDeBugLogSwitch() {
        // 查询默认的信号显示设置
        CustomSetting defaultSignalSetting = customSettingMapper.selectCustomSettingByKey(CustomSettingByKey.SHOW_DEBUG_LOG.getKey());
        if (defaultSignalSetting == null) {
            deBugLogSwitch.setChecked(false);
        } else {
            boolean flag = defaultSignalSetting.getSettingValue() == 1;
            // 如果有信号显示设置,就初始化一下开关
            deBugLogSwitch.setChecked(flag);
        }
        // 给日志显示开关设置开关监听器
        deBugLogSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            Log.i(TAG, "onCheckedChanged: 现在的设置是:" + b);
            CustomSetting customSetting = customSettingMapper.selectCustomSettingByKey(CustomSettingByKey.SHOW_DEBUG_LOG.getKey());
            if (customSetting == null) {
                CustomSetting customSettingRecord = new CustomSetting();
                customSettingRecord.setSettingKey(CustomSettingByKey.SHOW_DEBUG_LOG.getKey());
                customSettingRecord.setSettingValue((long) (b ? 1 : 0));
                // 插入信号显示设置
                customSettingMapper.insertCustomSetting(customSettingRecord);
            } else {
                customSetting.setSettingValue((long) (b ? 1 : 0));
                // 更新信号显示设置
                customSettingMapper.updateCustomSetting(customSetting);
            }
        });
    }

}
