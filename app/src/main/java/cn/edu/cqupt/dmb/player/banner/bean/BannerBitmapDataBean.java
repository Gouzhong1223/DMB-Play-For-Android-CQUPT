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

    public BannerBitmapDataBean(Bitmap imageRes, String title, int viewType) {
        this.imageRes = imageRes;
        this.title = title;
        this.viewType = viewType;
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

    @NonNull
    @Override
    public String toString() {
        return "BannerBitmapDataBean{" +
                "imageRes=" + imageRes +
                ", title='" + title + '\'' +
                ", viewType=" + viewType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BannerBitmapDataBean)) return false;
        BannerBitmapDataBean that = (BannerBitmapDataBean) o;
        return viewType == that.viewType && imageRes.equals(that.imageRes) && title.equals(that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageRes, title, viewType);
    }
}
