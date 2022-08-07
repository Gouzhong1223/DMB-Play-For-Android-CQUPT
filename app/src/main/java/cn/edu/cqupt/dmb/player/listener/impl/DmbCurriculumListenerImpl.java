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

package cn.edu.cqupt.dmb.player.listener.impl;

import android.os.Handler;
import android.util.Log;

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
public class DmbCurriculumListenerImpl implements DmbListener {

    /**
     * 更新课表的广播
     */
    public static final int MESSAGE_UPDATE_PICTURE = 0x100;
    private static final String TAG = "DmbCurriculumListenerImpl";
    /**
     * 课表显示回调
     */
    private final Handler handler;
    /**
     * 选中的播放场景
     */
    private final SceneVO selectedSceneVO;

    /**
     * 文件缓冲区
     */
    private final byte[] fileBuffer = new byte[1024 * 1024 * 2];
    /**
     * 有效数据长度
     */
    private Integer length;

    public DmbCurriculumListenerImpl(Handler handler, SceneVO selectedSceneVO) {
        this.handler = handler;
        this.selectedSceneVO = selectedSceneVO;
    }

    @Override
    public void onSuccess(String fileName, byte[] bytes, int length) {
        // 获取教学楼编号
        Integer building = getBuilding(selectedSceneVO.getBuilding());
        if (building == -1) {
            Log.e(TAG, "onSuccess: 未知的教学楼编号");
            return;
        }
        if (!fileName.contains(building.toString())) {
            // 如果不是需要的课表,就直接返回
            return;
        }
        this.length = length;
        System.arraycopy(bytes, 0, fileBuffer, 0, length);
        // 发送一条更新课表的广播
        handler.sendEmptyMessage(MESSAGE_UPDATE_PICTURE);
    }

    /**
     * 根据教学楼顺序获取教学楼编号
     *
     * @param build 教学楼顺序
     * @return 教学路编号
     */
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

    public byte[] getFileBuffer() {
        return fileBuffer;
    }

    public Integer getLength() {
        return length;
    }
}
