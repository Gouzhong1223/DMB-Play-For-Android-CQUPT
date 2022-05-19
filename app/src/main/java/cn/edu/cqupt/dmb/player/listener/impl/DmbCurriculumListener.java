package cn.edu.cqupt.dmb.player.listener.impl;

import android.os.Handler;

import cn.edu.cqupt.dmb.player.domain.SceneVO;
import cn.edu.cqupt.dmb.player.listener.DmbListener;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这个是监听课表数据的监听器
 * @Date : create by QingSong in 2022-04-07 14:52
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.listener
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DmbCurriculumListener implements DmbListener {

    public static final int MESSAGE_UPDATE_PICTURE = 0x100;
    private static final String TAG = "DmbCurriculumListener";
    private final Handler handler;
    private final SceneVO selectedSceneVO;
    /**
     * 教学楼数据源
     */
    private final String[] building = new String[]{"二教", "三教", "四教", "五教", "八教"};
    /**
     * 文件缓冲区
     */
    private final byte[] fileBuffer = new byte[1024 * 1024 * 2];
    private Integer length;

    public DmbCurriculumListener(Handler handler, SceneVO selectedSceneVO) {
        this.handler = handler;
        this.selectedSceneVO = selectedSceneVO;
    }

    @Override
    public void onSuccess(String fileName, byte[] bytes, int length) {
        Integer building = getBuilding(selectedSceneVO.getBuilding());
        if (building == -1) {
            return;
        }
        if (!fileName.contains(building.toString())) {
            // 如果不是需要的课表,就直接返回
            return;
        }
        this.length = length;
        System.arraycopy(bytes, 0, fileBuffer, 0, length);
        handler.sendEmptyMessage(MESSAGE_UPDATE_PICTURE);
    }

    private Integer getBuilding(Integer build) {
        switch (build) {
            case 0: {
                return 64;
            }
            case 1: {
                return 32;
            }
            case 2: {
                return 16;
            }
            case 3: {
                return 8;
            }
            case 4: {
                return 4;
            }
            default:
        }
        return -1;
    }

    @Override
    public void onReceiveMessage(String msg) {

    }

    public byte[] getFileBuffer() {
        return fileBuffer;
    }

    public Integer getLength() {
        return length;
    }
}
