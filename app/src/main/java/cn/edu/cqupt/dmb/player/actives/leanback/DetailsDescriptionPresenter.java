package cn.edu.cqupt.dmb.player.actives.leanback;

import androidx.leanback.widget.AbstractDetailsDescriptionPresenter;

public class DetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {

    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
        SceneVO sceneVO = (SceneVO) item;

        if (sceneVO != null) {
            viewHolder.getTitle().setText(sceneVO.getTitle());
            viewHolder.getSubtitle().setText(sceneVO.getSubTitle());
            viewHolder.getBody().setText(sceneVO.getDescription());
        }
    }
}
