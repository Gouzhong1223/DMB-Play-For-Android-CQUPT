package cn.edu.cqupt.dmb.player.ts.common;

import android.content.Context;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description :
 * @Date : create by QingSong in 2022-04-18 18:53
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.ts
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.4
 */
public interface Feature {
    boolean isEnabled(Context context);
}
