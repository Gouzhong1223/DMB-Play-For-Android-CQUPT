package cn.edu.cqupt.dmb.player.banner.adapter;

import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.youth.banner.adapter.BannerAdapter;

import java.util.List;

import cn.edu.cqupt.dmb.player.banner.bean.BannerBitmapDataBean;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description :
 * @Date : create by QingSong in 2022-03-23 13:05
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.banner.adapter
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class BitmapAdapter extends BannerAdapter<BannerBitmapDataBean, BitmapAdapter.BannerViewHolder> {

    public BitmapAdapter(List<BannerBitmapDataBean> bannerBitmapDataBeans) {
        // 初始化数据
        super(bannerBitmapDataBeans);
    }

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
    public void onBindView(BannerViewHolder holder, BannerBitmapDataBean data, int position, int size) {
        holder.imageView.setImageBitmap(data.getImageRes());
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public BannerViewHolder(@NonNull ImageView view) {
            super(view);
            this.imageView = view;
        }
    }
}
