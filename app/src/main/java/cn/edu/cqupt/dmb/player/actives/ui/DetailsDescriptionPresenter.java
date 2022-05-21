package cn.edu.cqupt.dmb.player.actives.ui;

import androidx.leanback.widget.AbstractDetailsDescriptionPresenter;

import cn.edu.cqupt.dmb.player.domain.SceneVO;

/**
 * @author qingsong
 */
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
