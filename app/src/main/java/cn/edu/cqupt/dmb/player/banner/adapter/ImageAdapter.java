package cn.edu.cqupt.dmb.player.banner.adapter;


import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.youth.banner.adapter.BannerAdapter;

import java.util.List;

import cn.edu.cqupt.dmb.player.banner.bean.BannerDataBean;


/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description :
 * @Date : create by QingSong in 2022-03-02 21:06
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.banner.adapter
 * @ProjectName : DMB Player For Android 
 * @Version : 1.0.0
 */
public class ImageAdapter extends BannerAdapter<BannerDataBean, ImageAdapter.BannerViewHolder> {

    public ImageAdapter(List<BannerDataBean> bannerDataBeanList) {
        //设置数据，也可以调用banner提供的方法,或者自己在adapter中实现
        super(bannerDataBeanList);
    }

    //创建ViewHolder，可以用viewType这个字段来区分不同的ViewHolder
    @Override
    public BannerViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());
        //注意，必须设置为match_parent，这个是viewpager2强制要求的
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new BannerViewHolder(imageView);
    }

    @Override
    public void onBindView(BannerViewHolder holder, BannerDataBean data, int position, int size) {
        holder.imageView.setImageResource(data.imageRes);
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public BannerViewHolder(@NonNull ImageView view) {
            super(view);
            this.imageView = view;
        }
    }
}

