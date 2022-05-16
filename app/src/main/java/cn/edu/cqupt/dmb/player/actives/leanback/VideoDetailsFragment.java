package cn.edu.cqupt.dmb.player.actives.leanback;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import cn.edu.cqupt.dmb.player.db.database.SceneDatabase;
import cn.edu.cqupt.dmb.player.db.mapper.SceneMapper;
import cn.edu.cqupt.dmb.player.domain.SceneInfo;

/**
 * @author qingsong
 */
public class VideoDetailsFragment extends DetailsSupportFragment {
    private static final String TAG = "VideoDetailsFragment";

    private static final int ACTION_WATCH_TRAILER = 1;
    private static final int ACTION_RENT = 2;
    private static final int ACTION_BUY = 3;

    private static final int DETAIL_THUMB_WIDTH = 274;
    private static final int DETAIL_THUMB_HEIGHT = 274;

    private static final int NUM_COLS = 10;

    private SceneVO mSelectedSceneVO;

    private SceneMapper sceneMapper;

    private ArrayObjectAdapter mAdapter;
    private ClassPresenterSelector mPresenterSelector;

    private DetailsSupportFragmentBackgroundController mDetailsBackground;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate DetailsFragment");
        super.onCreate(savedInstanceState);
        initDataBase();

        mDetailsBackground = new DetailsSupportFragmentBackgroundController(this);

        mSelectedSceneVO =
                (SceneVO) requireActivity().getIntent().getSerializableExtra(DetailsActivity.MOVIE);
        if (mSelectedSceneVO != null) {
            mPresenterSelector = new ClassPresenterSelector();
            mAdapter = new ArrayObjectAdapter(mPresenterSelector);
            setupDetailsOverviewRow();
            setupDetailsOverviewRowPresenter();
            setupRelatedMovieListRow();
            setAdapter(mAdapter);
            initializeBackground(mSelectedSceneVO);
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
        //new a database
        sceneMapper = Room.databaseBuilder(requireContext(), SceneDatabase.class, "scene_database")
                .allowMainThreadQueries().build().getSceneMapper();
    }

    private void initializeBackground(SceneVO data) {
        mDetailsBackground.enableParallax();
        Glide.with(requireActivity())
                .asBitmap()
                .centerCrop()
                .error(R.drawable.default_background)
                .load(data.getBackgroundDrawableId())
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap,
                                                @Nullable Transition<? super Bitmap> transition) {
                        mDetailsBackground.setCoverBitmap(bitmap);
                        mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size());
                    }
                });
    }

    private void setupDetailsOverviewRow() {
        Log.d(TAG, "doInBackground: " + mSelectedSceneVO.toString());
        final DetailsOverviewRow row = new DetailsOverviewRow(mSelectedSceneVO);
        row.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.default_background));
        int width = convertDpToPixel(requireActivity().getApplicationContext(), DETAIL_THUMB_WIDTH);
        int height = convertDpToPixel(requireActivity().getApplicationContext(), DETAIL_THUMB_HEIGHT);
        Glide.with(requireActivity())
                .load(mSelectedSceneVO.getCardDrawableId())
                .centerCrop()
                .error(R.drawable.default_background)
                .into(new SimpleTarget<Drawable>(width, height) {
                    @Override
                    public void onResourceReady(@NonNull Drawable drawable,
                                                @Nullable Transition<? super Drawable> transition) {
                        Log.d(TAG, "details overview card image url ready: " + drawable);
                        row.setImageDrawable(drawable);
                        mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size());
                    }
                });

        ArrayObjectAdapter actionAdapter = new ArrayObjectAdapter();

        actionAdapter.add(
                new Action(
                        ACTION_WATCH_TRAILER,
                        getResources().getString(R.string.watch_trailer_1),
                        getResources().getString(R.string.watch_trailer_2)));
        actionAdapter.add(
                new Action(
                        ACTION_RENT,
                        getResources().getString(R.string.rent_1),
                        getResources().getString(R.string.rent_2)));
        actionAdapter.add(
                new Action(
                        ACTION_BUY,
                        getResources().getString(R.string.buy_1),
                        getResources().getString(R.string.buy_2)));
        row.setActionsAdapter(actionAdapter);

        mAdapter.add(row);
    }

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
                Intent intent = new Intent(getActivity(), PlaybackActivity.class);
                intent.putExtra(DetailsActivity.MOVIE, mSelectedSceneVO);
                startActivity(intent);
            } else {
                Toast.makeText(getActivity(), action.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        mPresenterSelector.addClassPresenter(DetailsOverviewRow.class, detailsPresenter);
    }

    private void setupRelatedMovieListRow() {
        String[] subcategories = {getString(R.string.related_movies)};
        List<SceneInfo> sceneInfos = sceneMapper.selectAllScenes();
        ArrayList<SceneVO> list = new ArrayList<>();
        generateSceneVoList(sceneInfos, list);
        Collections.shuffle(list);
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
        for (int j = 0; j < NUM_COLS; j++) {
            listRowAdapter.add(list.get(j % 5));
        }

        HeaderItem header = new HeaderItem(0, subcategories[0]);
        mAdapter.add(new ListRow(header, listRowAdapter));
        mPresenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());
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
        sceneVO.setDeviceId(sceneInfo.getSceneId());
        sceneVO.setTitle(sceneInfo.getSceneName());
        sceneVO.setFrequency(sceneInfo.getFrequency());
        sceneVO.setSceneType(sceneInfo.getSceneType());
        configSceneDrawable(sceneVO);
        sceneVO.setSubTitle(sceneInfo.getFrequency() + ":" + sceneInfo.getSceneId());
        sceneVO.setVideoUrl("https://commondatastorage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202010_%20Year%20in%20Review.mp4");
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
                sceneVO.setCardDrawableId(R.drawable.video);
                sceneVO.setBackgroundDrawableId(R.drawable.video);
                sceneVO.setDescription("实时视频描述");
                break;
            }
            case 1: {
                sceneVO.setCardDrawableId(R.drawable.carousel);
                sceneVO.setBackgroundDrawableId(R.drawable.carousel);
                sceneVO.setDescription("轮播图描述");
                break;
            }
            case 2: {
                sceneVO.setCardDrawableId(R.drawable.audio);
                sceneVO.setBackgroundDrawableId(R.drawable.audio);
                sceneVO.setDescription("实时音频描述");
                break;
            }
            case 3: {
                sceneVO.setCardDrawableId(R.drawable.dormitory);
                sceneVO.setBackgroundDrawableId(R.drawable.dormitory);
                sceneVO.setDescription("宿舍安全信息描述");
                break;
            }
            case 4: {
                sceneVO.setCardDrawableId(R.drawable.curriculum);
                sceneVO.setBackgroundDrawableId(R.drawable.curriculum);
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
                intent.putExtra(getResources().getString(R.string.movie), mSelectedSceneVO);

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
