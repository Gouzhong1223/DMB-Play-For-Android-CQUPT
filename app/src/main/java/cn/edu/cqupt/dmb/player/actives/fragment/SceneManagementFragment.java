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

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.common.CustomSettingByKey;
import cn.edu.cqupt.dmb.player.domain.CustomSetting;
import cn.edu.cqupt.dmb.player.domain.SceneInfo;
import cn.edu.cqupt.dmb.player.utils.DialogUtil;


/**
 * @author qingsong
 */
public class SceneManagementFragment extends DmbBaseFragment {

    private static final String TAG = "SceneManagementFragment";
    /**
     * 显示场景设置的sceneListView
     */
    private ListView sceneListView;
    /**
     * Fragment 的 View
     */
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_scene_management, container, false);
        }
        // 初始化组件
        initView(rootView);
        // 配置组件
        configView();
        // Inflate the layout for this fragment
        return rootView;
    }


    /**
     * 获取屏幕的像素,动态设置组件宽高
     */
    public void getAndroidScreenProperty() {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        // 屏幕高度（像素）
        int height = displayMetrics.heightPixels;
        // 屏幕密度（0.75 / 1.0 / 1.5）
        float density = displayMetrics.density;
        // 屏幕高度(dp)
        int screenHeight = (int) (height / density);

        ViewGroup.LayoutParams layoutParams = sceneListView.getLayoutParams();
        // 转换像素
        layoutParams.height = convertDpToPixel(context, screenHeight);
        // 重新设置高度
        sceneListView.setLayoutParams(layoutParams);
    }

    private int convertDpToPixel(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    /**
     * 配置组件
     */
    private void configView() {
        // 查询所有的预设场景
        List<String> sceneInfos = sceneMapper.selectAllSceneNames();
        // 设置长按监听器
        configItemLongClickListener(sceneInfos);
        // 设置单击监听器
        configItemClickListener(sceneInfos);
        // 设置焦点选择监听器
        configItemItemSelectedListener(sceneInfos);
        // 设置列表数据源
        sceneListView.setAdapter(new ArrayAdapter<>(context, R.layout.dmb_list_item, sceneInfos));
        // 动态设置宽高
        getAndroidScreenProperty();
    }

    /**
     * 配置列表元素焦点选择监听器
     *
     * @param sceneInfos 预设场景信息
     */
    private void configItemItemSelectedListener(List<String> sceneInfos) {
        sceneListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i(TAG, "onItemSelected: " + sceneInfos.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.i(TAG, "onNothingSelected: ");
            }
        });
    }

    /**
     * 配置列表元素单击监听器
     *
     * @param sceneInfos 预设场景信息
     */
    private void configItemClickListener(List<String> sceneInfos) {
        sceneListView.setOnItemClickListener((adapterView, view, i, l) -> {
            SceneInfo sceneInfo = sceneMapper.selectSceneByScreenName(sceneInfos.get(i));
            DialogUtil.generateDialog(context, "场景信息", sceneInfo.toString(), new DialogUtil.DialogButton(DialogUtil.DialogButtonEnum.POSITIVE, (dialog, which) -> dialog.cancel(), "确定")).show();
        });
    }

    /**
     * 配置列表元素长按监听器
     *
     * @param sceneInfos 预设场景信息
     */
    private void configItemLongClickListener(List<String> sceneInfos) {
        sceneListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            DialogUtil.generateDialog(context, "删除场景设置", "是否要删除名为:" + sceneInfos.get(i) + "的预设？", new DialogUtil.DialogButton(DialogUtil.DialogButtonEnum.NEGATIVE, (dialog, which) -> dialog.cancel(), "取消"), new DialogUtil.DialogButton(DialogUtil.DialogButtonEnum.POSITIVE, (dialog, which) -> {
                SceneInfo sceneInfo = sceneMapper.selectSceneByScreenName(sceneInfos.get(i));
                sceneMapper.deleteSceneBySceneName(sceneInfos.get(i));
                sceneInfos.remove(i);
                CustomSetting customSetting = customSettingMapper.selectCustomSettingByKey(CustomSettingByKey.DEFAULT_SENSE.getKey());
                if (customSetting != null) {
                    Integer settingValue = Math.toIntExact(customSetting.getSettingValue());
                    if (settingValue.equals(sceneInfo.getId())) {
                        customSettingMapper.deleteCustomSettingById(customSetting.getId());
                        Toast.makeText(context, "默认设置也被删除了...", Toast.LENGTH_SHORT).show();
                    }
                }
                sceneListView.setAdapter(new ArrayAdapter<>(context, R.layout.dmb_list_item, sceneInfos));
                dialog.cancel();
            }, "确定")).show();
            return true;
        });
    }

    /**
     * 初始化组件
     *
     * @param rootView Fragment View
     */
    private void initView(View rootView) {
        sceneListView = rootView.findViewById(R.id.sceneList);
    }
}
