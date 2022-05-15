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

import androidx.annotation.NonNull;
import androidx.room.Room;

import com.xuexiang.xui.widget.dialog.materialdialog.DialogAction;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

import org.jetbrains.annotations.NotNull;

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
    private final ArrayList<View> viewArrayList = new ArrayList<>();
    private final HashMap<View, Drawable> preBackground = new HashMap<>();
    private Context context;
    private View rootView;
    private SceneMapper sceneMapper;
    private CustomSettingMapper customSettingMapper;
    private EditText sceneNameEditText;
    private EditText frequencyEditText;
    private EditText idEditText;
    private Spinner playTypeSpinner;
    private Spinner buildingSpinner;
    private Button button;
    private RelativeLayout buildingRelativeLayout;


    @SuppressLint("ValidFragment")
    public PresetFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDataBase();
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

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
        initView(rootView);
        configView();
        return rootView;
    }

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

    @SuppressLint({"UseCompatLoadingForDrawables", "PrivateResource"})
    private void configView() {
        for (View view : viewArrayList) {
            view.setOnFocusChangeListener((view1, b) -> {
                Drawable background;
                if (!preBackground.containsKey(view)) {
                    background = view.getBackground();
                    preBackground.put(view, background);
                } else {
                    background = preBackground.get(view);
                }
                if (b) {
                    view.setBackground(context.getDrawable(android.R.drawable.list_selector_background));
                } else {
                    view1.setBackground(background);
                    preBackground.remove(view);
                }
            });
        }
        playTypeSpinner.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, playType));
        buildingSpinner.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, building));
        playTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (playType[i].equals("课表")) {
                    buildingRelativeLayout.setVisibility(View.VISIBLE);
                } else {
                    buildingRelativeLayout.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.i(TAG, "onNothingSelected: ");
            }
        });

        button.setOnClickListener(view -> {
            String sceneName = sceneNameEditText.getText().toString();
            String frequency = frequencyEditText.getText().toString();
            String id = idEditText.getText().toString();
            String playType = playTypeSpinner.getSelectedItem().toString();
            String building;
            if (!validationParameters(sceneName, frequency, id)) {
                return;
            }
            SceneInfo sceneInfo = new SceneInfo();
            sceneInfo.setSceneName(sceneName);
            sceneInfo.setFrequency(Integer.parseInt(frequency));
            sceneInfo.setSceneId(Integer.parseInt(id));
            sceneInfo.setSceneType(getSceneTypeBySceneStr(playType));
            if (playType.equals("课表")) {
                building = buildingSpinner.getSelectedItem().toString();
                sceneInfo.setBuilding(getBuildingNumByBuildingStr(building));
            }
            sceneMapper.insertScene(sceneInfo);
            DialogUtil.generateDialog(context, "保存成功！", "预设已经成功保存啦！添加成功的预设都会在主页面显示哦！同时还可以设置一个默认的预设作为APP启动时载入的场景！", new DialogUtil.PositiveButton(DialogUtil.DialogButtonEnum.POSITIVE, (dialog, which) -> {
                dialog.cancel();
                CustomSetting customSetting = customSettingMapper.selectCustomSettingByKey(CustomSettingByKey.DEFAULT_SENSE.getKey());
                if (customSetting == null) {
                    DialogUtil.generateDialog(context, "设置自启动场景", "检测到您还没有设置默认的使用场景,是否将" + sceneName + "设置为APP默认的使用场景?", new DialogUtil.PositiveButton(DialogUtil.DialogButtonEnum.NEGATIVE, (dialog1, which1) -> dialog1.cancel(), "取消"), new DialogUtil.PositiveButton(DialogUtil.DialogButtonEnum.POSITIVE, new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            CustomSetting customSettingRecord = new CustomSetting();
                            SceneInfo sceneInfoRecord = sceneMapper.selectSceneByScreenName(sceneName);
                            customSettingRecord.setSettingKey(CustomSettingByKey.DEFAULT_SENSE.getKey());
                            customSettingRecord.setSettingValue(Long.valueOf(sceneInfoRecord.getId()));
                            customSettingMapper.insertCustomSetting(customSettingRecord);
                        }
                    }, "确定")).show();
                }
            }, "确定")).show();
        });
    }

    private Integer getSceneTypeBySceneStr(String sceneStr) {
        int type = -1;
        for (int i = 0; i < playType.length; i++) {
            if (playType[i].equals(sceneStr)) {
                type = i;
            }
        }
        return type;
    }

    private Integer getBuildingNumByBuildingStr(String buildingStr) {
        int type = -1;
        for (int i = 0; i < building.length; i++) {
            if (building[i].equals(buildingStr)) {
                type = i;
            }
        }
        return type;
    }

    private boolean validationParameters(String sceneName, String frequency, String id) {
        if (sceneName.equals("")) {
            DialogUtil.generateDialog(context, "预设名字还没填呢！", "快去给你的自定义预设取个名字吧！", new DialogUtil.PositiveButton(DialogUtil.DialogButtonEnum.POSITIVE, (dialog, index) -> dialog.cancel(), "确定")).show();
            return false;
        }

        SceneInfo sceneInfo = sceneMapper.selectSceneByScreenName(sceneName);
        if (sceneInfo != null) {
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

        SceneInfo sceneInfo1 = sceneMapper.selectSceneBySceneIdAndFrequency(Integer.parseInt(id), Integer.parseInt(frequency));
        if (sceneInfo1 != null) {
            DialogUtil.generateDialog(context, "这个预设已经有啦！", "有一个叫：" + sceneInfo1.getSceneName() + "的预设里面的终端编号和工作频点和你现在的设置是一样的，不用重复提交啦！", new DialogUtil.PositiveButton(DialogUtil.DialogButtonEnum.POSITIVE, (dialog, index) -> dialog.cancel(), "确定")).show();
            return false;
        }
        return true;
    }
}
