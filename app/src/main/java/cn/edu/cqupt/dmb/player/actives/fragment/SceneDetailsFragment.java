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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.DetailsSupportFragment;
import androidx.leanback.app.DetailsSupportFragmentBackgroundController;
import androidx.leanback.widget.Action;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ClassPresenterSelector;
import androidx.leanback.widget.DetailsOverviewRow;
import androidx.leanback.widget.FullWidthDetailsOverviewRowPresenter;
import androidx.leanback.widget.FullWidthDetailsOverviewSharedElementHelper;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.room.Room;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.actives.DetailsActivity;
import cn.edu.cqupt.dmb.player.actives.MainActivity;
import cn.edu.cqupt.dmb.player.actives.ui.CardPresenter;
import cn.edu.cqupt.dmb.player.actives.ui.DetailsDescriptionPresenter;
import cn.edu.cqupt.dmb.player.db.database.SceneDatabase;
import cn.edu.cqupt.dmb.player.db.mapper.SceneMapper;
import cn.edu.cqupt.dmb.player.domain.SceneInfo;
import cn.edu.cqupt.dmb.player.domain.SceneVO;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;
import cn.edu.cqupt.dmb.player.utils.DialogUtil;

/**
 * @author qingsong
 */
public class SceneDetailsFragment extends DetailsSupportFragment {
    private static final String TAG = "SceneDetailsFragment";

    private static final int ACTION_WATCH_TRAILER = 1;

    private static final int DETAIL_THUMB_WIDTH = 274;
    private static final int DETAIL_THUMB_HEIGHT = 274;

    /**
     * 被选中的场景预设信息
     */
    private SceneVO selectedSceneVO;

    /**
     * 查询场景预设的 Mapper
     */
    private SceneMapper sceneMapper;

    /**
     * ArrayObjectAdapter,装载关联的预设场景信息
     */
    private ArrayObjectAdapter adapter;

    /**
     * ClassPresenterSelector,我也不知道用来干什么的
     */
    private ClassPresenterSelector presenterSelector;

    private SceneDatabase sceneDatabase;

    /**
     * 背景控制器
     */
    private DetailsSupportFragmentBackgroundController detailsBackground;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate DetailsFragment");
        super.onCreate(savedInstanceState);
        // 初始化数据库
        initDataBase();

        // 初始化背景管理器
        detailsBackground = new DetailsSupportFragmentBackgroundController(this);

        // 获取父传递过来的参数
        selectedSceneVO =
                (SceneVO) requireActivity().getIntent().getSerializableExtra(DetailsActivity.SCENE_VO);
        if (selectedSceneVO != null) {
            presenterSelector = new ClassPresenterSelector();
            adapter = new ArrayObjectAdapter(presenterSelector);
            setupDetailsOverviewRow();
            setupDetailsOverviewRowPresenter();
            setupRelatedSceneListRow();
            setAdapter(adapter);
            initializeBackground(selectedSceneVO);
            setOnItemViewClickedListener(new ItemViewClickedListener());
        } else {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        }
    }

    /**
     * 初始化数据库
     */
    private void initDataBase() {
        sceneDatabase = Room.databaseBuilder(requireContext(), SceneDatabase.class, "scene_database")
                .allowMainThreadQueries().build();
        //new a database
        sceneMapper = sceneDatabase.getSceneMapper();
    }

    /**
     * 根据选择的 VO,初始化北京图
     *
     * @param data 选中的 VO
     */
    private void initializeBackground(SceneVO data) {
        detailsBackground.enableParallax();
        Glide.with(requireActivity())
                .asBitmap()
                .centerCrop()
                .error(R.drawable.default_background)
                .load(data.getBackgroundDrawableId())
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap,
                                                @Nullable Transition<? super Bitmap> transition) {
                        detailsBackground.setCoverBitmap(bitmap);
                        adapter.notifyArrayItemRangeChanged(0, adapter.size());
                    }
                });
    }

    /**
     * 初始化概览
     */
    private void setupDetailsOverviewRow() {
        Log.d(TAG, "doInBackground: " + selectedSceneVO.toString());
        final DetailsOverviewRow row = new DetailsOverviewRow(selectedSceneVO);
        row.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.default_background));
        int width = convertDpToPixel(requireActivity().getApplicationContext(), DETAIL_THUMB_WIDTH);
        int height = convertDpToPixel(requireActivity().getApplicationContext(), DETAIL_THUMB_HEIGHT);
        Glide.with(requireActivity())
                .load(selectedSceneVO.getCardDrawableId())
                .centerCrop()
                .error(R.drawable.default_background)
                .into(new SimpleTarget<Drawable>(width, height) {
                    @Override
                    public void onResourceReady(@NonNull Drawable drawable,
                                                @Nullable Transition<? super Drawable> transition) {
                        Log.d(TAG, "details overview card image url ready: " + drawable);
                        row.setImageDrawable(drawable);
                        adapter.notifyArrayItemRangeChanged(0, adapter.size());
                    }
                });

        ArrayObjectAdapter actionAdapter = new ArrayObjectAdapter();

        actionAdapter.add(
                new Action(
                        ACTION_WATCH_TRAILER,
                        getResources().getString(R.string.start_play),
                        getResources().getString(R.string.watch_trailer_2)));
        row.setActionsAdapter(actionAdapter);

        adapter.add(row);
    }


    @RequiresApi(api = Build.VERSION_CODES.R)
    private void setupDetailsOverviewRowPresenter() {
        // Set detail background.
        FullWidthDetailsOverviewRowPresenter detailsPresenter =
                new FullWidthDetailsOverviewRowPresenter(new DetailsDescriptionPresenter());
        detailsPresenter.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.selected_background));

        // Hook up transition element.
        FullWidthDetailsOverviewSharedElementHelper sharedElementHelper =
                new FullWidthDetailsOverviewSharedElementHelper();
        sharedElementHelper.setSharedElementEnterTransition(
                getActivity(), DetailsActivity.SHARED_ELEMENT_NAME);
        detailsPresenter.setListener(sharedElementHelper);
        detailsPresenter.setParticipatingEntranceTransition(true);

        detailsPresenter.setOnActionClickedListener(action -> {
            if (action.getId() == ACTION_WATCH_TRAILER) {
                if (!DataReadWriteUtil.USB_READY) {
                    DialogUtil.generateDialog(requireContext(),
                            com.xuexiang.xui.R.style.Base_Theme_MaterialComponents_Light_Dialog_MinWidth,
                            "缺少DMB设备",
                            "当前没有读取到任何的DMB设备信息,请插上DMB设备!",
                            new DialogUtil.AlertDialogButton(DialogUtil.DialogButtonEnum.POSITIVE,
                                    null, "确定")).show();
                    return;
                }
                // 设置被选中的播放模块
                Log.i(TAG, "setupDetailsOverviewRowPresenter: 设置选中场景为" + selectedSceneVO);
                DataReadWriteUtil.selectSceneVO = selectedSceneVO;
                Intent intent = new Intent(getActivity(), MainActivity.getActivityBySceneType(selectedSceneVO.getSceneType()));
                intent.putExtra(DetailsActivity.SCENE_VO, selectedSceneVO);
                Toast.makeText(requireContext(), "正在跳转...", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            } else {
                Toast.makeText(getActivity(), action.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        presenterSelector.addClassPresenter(DetailsOverviewRow.class, detailsPresenter);
    }

    private void setupRelatedSceneListRow() {
        String[] subcategories = {getString(R.string.related_movies)};
        List<SceneInfo> sceneInfos = sceneMapper.selectAllScenes();
        ArrayList<SceneVO> list = new ArrayList<>();
        generateSceneVoList(sceneInfos, list);
        Collections.shuffle(list);
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
        for (int j = 0; j < list.size(); j++) {
            listRowAdapter.add(list.get(j % 5));
        }

        HeaderItem header = new HeaderItem(0, subcategories[0]);
        adapter.add(new ListRow(header, listRowAdapter));
        presenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());
    }

    private void generateSceneVoList(List<SceneInfo> sceneInfos, ArrayList<SceneVO> sceneVoList) {
        for (SceneInfo sceneInfo : sceneInfos) {
            SceneVO sceneVO = getSceneVO(sceneInfo);
            sceneVoList.add(sceneVO);
        }
    }

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

    private int convertDpToPixel(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    @Override
    public void onDestroyView() {
        sceneDatabase.close();
        super.onDestroyView();
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(
                Presenter.ViewHolder itemViewHolder,
                Object item,
                RowPresenter.ViewHolder rowViewHolder,
                Row row) {

            if (item instanceof SceneVO) {
                Log.d(TAG, "Item: " + item);
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(getResources().getString(R.string.movie), selectedSceneVO);

                Bundle bundle =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                                        requireActivity(),
                                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
                                        DetailsActivity.SHARED_ELEMENT_NAME)
                                .toBundle();
                requireActivity().startActivity(intent, bundle);
            }
        }
    }
}
