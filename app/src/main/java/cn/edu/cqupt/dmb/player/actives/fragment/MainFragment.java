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
import cn.edu.cqupt.dmb.player.actives.ui.CardPresenter;
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
        // ??????????????????
        initDataBase();
        // ????????????????????????
        prepareBackgroundManager();
        // ????????? UI
        setupUiElements();
        // ????????????????????????
        loadRows();
        // ????????????????????????
        setupEventListeners();
    }

    /**
     * ??????????????????
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
     * ????????????????????????????????????????????????
     */
    private void loadRows() {
        // ?????????????????????????????????
        List<SceneInfo> sceneInfos = sceneMapper.selectAllScenes();
        // ????????????SceneVO?????????
        ArrayList<SceneVO> sceneVoList = new ArrayList<>();
        // ?????? SceneInfo ?????? SceneVO ?????????????????????
        generateSceneVoList(sceneInfos, sceneVoList);
        // ?????????,??????????????????????????????
        ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        // ??????????????????????????????
        CardPresenter cardPresenter = new CardPresenter();
        // ??????????????????????????????????????????,????????????????????????????????????
        List<List<SceneVO>> sceneRows = new ArrayList<>();
        for (int i = 0; i < NUM_ROWS; i++) {
            // ???????????????
            sceneRows.add(new ArrayList<>());
        }
        for (SceneVO sceneVO : sceneVoList) {
            // ????????????????????????????????????????????????????????????
            sceneRows.get(sceneVO.getSceneType()).add(sceneVO);
        }
        int i;
        for (i = 0; i < NUM_ROWS; i++) {
            // ??????????????????????????????????????????????????????(hang)????????????
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
            for (int j = 0; j < sceneRows.get(i).size(); j++) {
                // ?????????????????????
                listRowAdapter.add(sceneRows.get(i).get(j));
            }
            HeaderItem header = new HeaderItem(i, SceneTypeList.SCENE_TYPE_CATEGORY[i]);
            // ?????????????????????(?????????)
            rowsAdapter.add(new ListRow(header, listRowAdapter));
        }

        // ??????????????????
        HeaderItem gridHeader = new HeaderItem(i, "??????");

        GridItemPresenter mGridPresenter = new GridItemPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
        gridRowAdapter.add(getResources().getString(R.string.grid_view));
        gridRowAdapter.add(getString(R.string.crash_log));
        gridRowAdapter.add(getResources().getString(R.string.personal_settings));
        rowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));

        setAdapter(rowsAdapter);
    }

    /**
     * ??????sceneInfos????????????sceneVoList
     *
     * @param sceneInfos  ??????????????????sceneInfos
     * @param sceneVoList ??????SceneVO???List
     */
    private void generateSceneVoList(List<SceneInfo> sceneInfos, ArrayList<SceneVO> sceneVoList) {
        for (SceneInfo sceneInfo : sceneInfos) {
            SceneVO sceneVO = getSceneVO(sceneInfo);
            sceneVoList.add(sceneVO);
        }
    }

    /**
     * ?????? SceneInfo?????? SceneVO
     *
     * @param sceneInfo ??????????????????SceneInfo
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
     * ??????Scene?????????,??????????????????????????????
     *
     * @param sceneVO sceneVO
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private void configSceneDrawable(SceneVO sceneVO) {
        switch (sceneVO.getSceneType()) {
            case 0: {
                sceneVO.setCardDrawableId(R.drawable.video_icon);
                sceneVO.setBackgroundDrawableId(R.drawable.video_bg);
                sceneVO.setDescription("??????????????????");
                break;
            }
            case 1: {
                sceneVO.setCardDrawableId(R.drawable.carousel_icon);
                sceneVO.setBackgroundDrawableId(R.drawable.carousel_bg);
                sceneVO.setDescription("???????????????");
                break;
            }
            case 2: {
                sceneVO.setCardDrawableId(R.drawable.audio_icon);
                sceneVO.setBackgroundDrawableId(R.drawable.audio_bg);
                sceneVO.setDescription("??????????????????");
                break;
            }
            case 3: {
                sceneVO.setCardDrawableId(R.drawable.dormitory_icon);
                sceneVO.setBackgroundDrawableId(R.drawable.dormitory_bg);
                sceneVO.setDescription("????????????????????????");
                break;
            }
            case 4: {
                sceneVO.setCardDrawableId(R.drawable.curriculum_icon);
                sceneVO.setBackgroundDrawableId(R.drawable.curriculum_bg);
                sceneVO.setDescription("???????????????????????????");
                break;
            }
            default:
        }
    }

    /**
     * ????????????????????????
     */
    private void prepareBackgroundManager() {

        mBackgroundManager = BackgroundManager.getInstance(requireActivity());
        mBackgroundManager.attach(requireActivity().getWindow());

        mDefaultBackground = ContextCompat.getDrawable(requireContext(), R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    /**
     * ????????? UI
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
     * ?????????????????????
     */
    private void setupEventListeners() {
        setOnSearchClickedListener(view -> Toast.makeText(getActivity(), "Implement your own in-app search", Toast.LENGTH_LONG)
                .show());

        // ?????????????????????
        setOnItemViewClickedListener(new ItemViewClickedListener());
        // ???????????????????????????
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

    /**
     * ???????????????
     *
     * @param backgroundDrawable ?????????
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
     * ??????????????????????????????
     */
    private void startBackgroundTimer() {
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
        mBackgroundTimer = new Timer();
        mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
    }

    /**
     * ?????????????????????
     */
    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof SceneVO) {
                // ??????????????????????????????
                SceneVO sceneVO = (SceneVO) item;
                Log.d(TAG, "Item: " + item);
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(DetailsActivity.SCENE_VO, sceneVO);

                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                requireActivity(),
                                ((ImageCardView) itemViewHolder.view).getMainImageView(),
                                DetailsActivity.SHARED_ELEMENT_NAME)
                        .toBundle();
                // ???????????????????????????
                requireActivity().startActivity(intent, bundle);
            } else if (item instanceof String) {
                if (((String) item).contains(getString(R.string.crash_log))) {
                    Intent intent = new Intent(getActivity(), BrowseErrorActivity.class);
                    startActivity(intent);
                } else if (item.equals(getString(R.string.personal_settings))) {
                    Intent intent = new Intent(getActivity(), SetupActivity.class);
                    startActivity(intent);
                } else if (item.equals(getString(R.string.grid_view))) {
                    loadRows();
                    Toast.makeText(getActivity(), "????????????", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * ???????????????????????????
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
     * ??????????????????????????????
     */
    private class UpdateBackgroundTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(() -> updateBackground(mBackgroundDrawable));
        }
    }

    /**
     * ????????????
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
