package cn.edu.cqupt.dmb.player.actives.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.room.Room;

import java.util.List;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.db.database.SceneDatabase;
import cn.edu.cqupt.dmb.player.db.mapper.SceneMapper;
import cn.edu.cqupt.dmb.player.domain.SceneInfo;
import cn.edu.cqupt.dmb.player.utils.DialogUtil;


public class SceneManagementFragment extends Fragment {

    private static final String TAG = "SceneManagementFragment";
    private ListView sceneListView;
    private View rootView;

    private Context context;

    private SceneMapper sceneMapper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        initDataBase();
    }

    private void initDataBase() {
        sceneMapper = Room.databaseBuilder(context, SceneDatabase.class, "scene_database") //new a database
                .allowMainThreadQueries().build().getSceneMapper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_scene_management, container, false);
        }
        initView(rootView);
        configView();
        // Inflate the layout for this fragment
        return rootView;
    }

    private void configView() {
        List<String> sceneInfos = sceneMapper.selectAllSceneNames();
        sceneListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            DialogUtil.generateDialog(context, "删除场景设置", "是否要删除名为:" + sceneInfos.get(i) + "的预设？", new DialogUtil.PositiveButton(DialogUtil.DialogButtonEnum.NEGATIVE, (dialog, which) -> dialog.cancel(), "取消"), new DialogUtil.PositiveButton(DialogUtil.DialogButtonEnum.POSITIVE, (dialog, which) -> {
                sceneMapper.deleteSceneBySceneName(sceneInfos.get(i));
                sceneInfos.remove(i);
                sceneListView.setAdapter(new ArrayAdapter<>(context, R.layout.dmb_list_item, sceneInfos));
                dialog.cancel();
            }, "确定")).show();
            return true;
        });
        sceneListView.setOnItemClickListener((adapterView, view, i, l) -> {
            SceneInfo sceneInfo = sceneMapper.selectSceneByScreenName(sceneInfos.get(i));
            DialogUtil.generateDialog(context, "场景信息", sceneInfo.toString(), new DialogUtil.PositiveButton(DialogUtil.DialogButtonEnum.POSITIVE, (dialog, which) -> dialog.cancel(), "确定")).show();
        });
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
        sceneListView.setAdapter(new ArrayAdapter<>(context, R.layout.dmb_list_item, sceneInfos));
    }

    private void initView(View rootView) {
        sceneListView = rootView.findViewById(R.id.sceneList);
    }
}
