package cn.edu.cqupt.dmb.player.actives.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.BackgroundManager;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.room.Room;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.actives.BrowseErrorActivity;
import cn.edu.cqupt.dmb.player.actives.DetailsActivity;
import cn.edu.cqupt.dmb.player.actives.SetupActivity;
import cn.edu.cqupt.dmb.player.actives.leanback.CardPresenter;
import cn.edu.cqupt.dmb.player.db.database.SceneDatabase;
import cn.edu.cqupt.dmb.player.db.mapper.SceneMapper;
import cn.edu.cqupt.dmb.player.domain.SceneInfo;
import cn.edu.cqupt.dmb.player.domain.SceneTypeList;
import cn.edu.cqupt.dmb.player.domain.SceneVO;

/**
 * @author qingsong
 */
public class MainFragment extends BrowseSupportFragment {
    private static final String TAG = "MainFragment";

    private static final int BACKGROUND_UPDATE_DELAY = 300;
    private static final int GRID_ITEM_WIDTH = 200;
    private static final int GRID_ITEM_HEIGHT = 200;
    private static final int NUM_ROWS = SceneTypeList.SCENE_TYPE_CATEGORY.length;

    private final Handler mHandler = new Handler(Looper.myLooper());
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private Drawable mBackgroundDrawable;
    private BackgroundManager mBackgroundManager;
    private SceneMapper sceneMapper;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);
        // 初始化数据库
        initDataBase();
        // 初始化北京管理器
        prepareBackgroundManager();
        // 初始化 UI
        setupUiElements();
        // 加载预设场景信息
        loadRows();
        // 给按键设置监听器
        setupEventListeners();
    }

    /**
     * 初始化数据库
     */
    private void initDataBase() {
        //new a database
        sceneMapper = Room.databaseBuilder(requireContext(), SceneDatabase.class, "scene_database")
                .allowMainThreadQueries().build().getSceneMapper();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mBackgroundTimer) {
            Log.d(TAG, "onDestroy: " + mBackgroundTimer);
            mBackgroundTimer.cancel();
        }
    }

    /**
     * 加载每一个播放场景对应的场景预设
     */
    private void loadRows() {
        // 查询所有的预设场景信息
        List<SceneInfo> sceneInfos = sceneMapper.selectAllScenes();
        // 构造装载SceneVO的容器
        ArrayList<SceneVO> sceneVoList = new ArrayList<>();
        // 根据 SceneInfo 转换 SceneVO 并装载到容器中
        generateSceneVoList(sceneInfos, sceneVoList);
        // 构造列,也就是播放类型的列表
        ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        // 构造一个场景预览组件
        CardPresenter cardPresenter = new CardPresenter();
        // 装载每一个播放类型对应的场景,每个播放类型对应多个场景
        List<List<SceneVO>> sceneRows = new ArrayList<>();
        for (int i = 0; i < NUM_ROWS; i++) {
            // 初始化容器
            sceneRows.add(new ArrayList<>());
        }
        for (SceneVO sceneVO : sceneVoList) {
            // 把每个播放类型数据容器填充对应的播放场景
            sceneRows.get(sceneVO.getSceneType()).add(sceneVO);
        }
        int i;
        for (i = 0; i < NUM_ROWS; i++) {
            // 构造每一个播放类型对应的播放场景的行(hang)展示容器
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
            for (int j = 0; j < sceneRows.get(i).size(); j++) {
                // 把场景添加进去
                listRowAdapter.add(sceneRows.get(i).get(j));
            }
            HeaderItem header = new HeaderItem(i, SceneTypeList.SCENE_TYPE_CATEGORY[i]);
            // 添加选项卡信息(每一行)
            rowsAdapter.add(new ListRow(header, listRowAdapter));
        }

        // 添加其他选项
        HeaderItem gridHeader = new HeaderItem(i, "其他");

        GridItemPresenter mGridPresenter = new GridItemPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
        gridRowAdapter.add(getResources().getString(R.string.grid_view));
        gridRowAdapter.add(getString(R.string.error_fragment));
        gridRowAdapter.add(getResources().getString(R.string.personal_settings));
        rowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));

        setAdapter(rowsAdapter);
    }

    /**
     * 根据sceneInfos数组生成sceneVoList
     *
     * @param sceneInfos  数据库原始的sceneInfos
     * @param sceneVoList 装载SceneVO的List
     */
    private void generateSceneVoList(List<SceneInfo> sceneInfos, ArrayList<SceneVO> sceneVoList) {
        for (SceneInfo sceneInfo : sceneInfos) {
            SceneVO sceneVO = getSceneVO(sceneInfo);
            sceneVoList.add(sceneVO);
        }
    }

    /**
     * 根据 SceneInfo生成 SceneVO
     *
     * @param sceneInfo 数据库原始的SceneInfo
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
        configSceneDrawable(sceneVO);
        sceneVO.setSubTitle(sceneInfo.getFrequency() + ":" + sceneInfo.getDeviceId());
        return sceneVO;
    }

    /**
     * 配置Scene的图片,包括背景图还有预览图
     *
     * @param sceneVO sceneVO
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private void configSceneDrawable(SceneVO sceneVO) {
        switch (sceneVO.getSceneType()) {
            case 0: {
                sceneVO.setCardDrawableId(R.drawable.video_icon);
                sceneVO.setBackgroundDrawableId(R.drawable.video_bg);
                sceneVO.setDescription("实时视频描述");
                break;
            }
            case 1: {
                sceneVO.setCardDrawableId(R.drawable.carousel_icon);
                sceneVO.setBackgroundDrawableId(R.drawable.carousel_bg);
                sceneVO.setDescription("轮播图描述");
                break;
            }
            case 2: {
                sceneVO.setCardDrawableId(R.drawable.audio_icon);
                sceneVO.setBackgroundDrawableId(R.drawable.audio_bg);
                sceneVO.setDescription("实时音频描述");
                break;
            }
            case 3: {
                sceneVO.setCardDrawableId(R.drawable.dormitory_icon);
                sceneVO.setBackgroundDrawableId(R.drawable.dormitory_bg);
                sceneVO.setDescription("宿舍安全信息描述");
                break;
            }
            case 4: {
                sceneVO.setCardDrawableId(R.drawable.curriculum_icon);
                sceneVO.setBackgroundDrawableId(R.drawable.curriculum_bg);
                sceneVO.setDescription("教学楼课表显示描述");
                break;
            }
            default:
        }
    }

    /**
     * 初始化背景管理器
     */
    private void prepareBackgroundManager() {

        mBackgroundManager = BackgroundManager.getInstance(requireActivity());
        mBackgroundManager.attach(requireActivity().getWindow());

        mDefaultBackground = ContextCompat.getDrawable(requireContext(), R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    /**
     * 初始化 UI
     */
    private void setupUiElements() {
        // setBadgeDrawable(getActivity().getResources().getDrawable(
        // R.drawable.videos_by_google_banner));
        // Badge, when set, takes precedent
        setTitle(getString(R.string.browse_title));
        // over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        // set fastLane (or headers) background color
        setBrandColor(ContextCompat.getColor(requireContext(), R.color.fastlane_background));
        // set search icon color
        setSearchAffordanceColor(ContextCompat.getColor(requireContext(), R.color.search_opaque));
    }

    /**
     * 设置组件监听器
     */
    private void setupEventListeners() {
        setOnSearchClickedListener(view -> Toast.makeText(getActivity(), "Implement your own in-app search", Toast.LENGTH_LONG)
                .show());

        // 设置点击监听器
        setOnItemViewClickedListener(new ItemViewClickedListener());
        // 设置焦点选择监听器
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

    /**
     * 更新背景图
     *
     * @param backgroundDrawable 背景图
     */
    private void updateBackground(Drawable backgroundDrawable) {
        int width = mMetrics.widthPixels;
        int height = mMetrics.heightPixels;
        Glide.with(requireActivity())
                .load(backgroundDrawable)
                .centerCrop()
                .error(mDefaultBackground)
                .into(new SimpleTarget<Drawable>(width, height) {
                    @Override
                    public void onResourceReady(@NonNull Drawable drawable,
                                                @Nullable Transition<? super Drawable> transition) {
                        mBackgroundManager.setDrawable(drawable);
                    }
                });
        mBackgroundTimer.cancel();
    }

    /**
     * 开始背景更换定时任务
     */
    private void startBackgroundTimer() {
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
        mBackgroundTimer = new Timer();
        mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
    }

    /**
     * 组件点击监听器
     */
    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof SceneVO) {
                // 如果点击的是场景按钮
                SceneVO sceneVO = (SceneVO) item;
                Log.d(TAG, "Item: " + item);
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(DetailsActivity.SCENE_VO, sceneVO);

                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                requireActivity(),
                                ((ImageCardView) itemViewHolder.view).getMainImageView(),
                                DetailsActivity.SHARED_ELEMENT_NAME)
                        .toBundle();
                // 跳转到场景详情界面
                requireActivity().startActivity(intent, bundle);
            } else if (item instanceof String) {
                if (((String) item).contains(getString(R.string.error_fragment))) {
                    Intent intent = new Intent(getActivity(), BrowseErrorActivity.class);
                    startActivity(intent);
                } else if (item.equals(getString(R.string.personal_settings))) {
                    Intent intent = new Intent(getActivity(), SetupActivity.class);
                    startActivity(intent);
                } else if (item.equals(getString(R.string.grid_view))) {
                    loadRows();
                    Toast.makeText(getActivity(), "刷新成功", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * 组件焦点选择监听器
     */
    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onItemSelected(
                Presenter.ViewHolder itemViewHolder,
                Object item,
                RowPresenter.ViewHolder rowViewHolder,
                Row row) {
            if (item instanceof SceneVO) {
                mBackgroundDrawable = requireContext().getDrawable(((SceneVO) item).getBackgroundDrawableId());
                startBackgroundTimer();
            }
        }
    }

    /**
     * 更新背景图的定时任务
     */
    private class UpdateBackgroundTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(() -> updateBackground(mBackgroundDrawable));
        }
    }

    /**
     * 网格布局
     */
    private class GridItemPresenter extends Presenter {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            TextView view = new TextView(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT));
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.default_background));
            view.setTextColor(Color.WHITE);
            view.setGravity(Gravity.CENTER);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            ((TextView) viewHolder.view).setText((String) item);
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {
        }
    }

}
