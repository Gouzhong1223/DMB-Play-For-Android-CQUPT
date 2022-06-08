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

package cn.edu.cqupt.dmb.player.banner.bean;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description :
 * @Date : create by QingSong in 2022-03-23 13:06
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.banner.bean
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class BannerBitmapDataBean {

    private Bitmap imageRes;
    private String title;
    private int viewType;
    private long loopTime;

    public BannerBitmapDataBean(Bitmap imageRes, String title, int viewType) {
        this.imageRes = imageRes;
        this.title = title;
        this.viewType = viewType;
    }

    public BannerBitmapDataBean(Bitmap imageRes, String title, int viewType, long loopTime) {
        this.imageRes = imageRes;
        this.title = title;
        this.viewType = viewType;
        this.loopTime = loopTime;
    }

    public Bitmap getImageRes() {
        return imageRes;
    }

    public void setImageRes(Bitmap imageRes) {
        this.imageRes = imageRes;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public long getLoopTime() {
        return loopTime;
    }

    public void setLoopTime(long loopTime) {
        this.loopTime = loopTime;
    }

    @NonNull
    @Override
    public String toString() {
        return "BannerBitmapDataBean{" + "imageRes=" + imageRes + ", title='" + title + '\'' + ", viewType=" + viewType + ", loopTime=" + loopTime + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BannerBitmapDataBean)) return false;
        BannerBitmapDataBean that = (BannerBitmapDataBean) o;
        return viewType == that.viewType && imageRes.equals(that.imageRes) && title.equals(that.title) && loopTime == that.loopTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageRes, title, viewType, loopTime);
    }
}
