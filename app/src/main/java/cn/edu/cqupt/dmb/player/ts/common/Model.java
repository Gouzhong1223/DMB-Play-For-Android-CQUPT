package cn.edu.cqupt.dmb.player.ts.common;

import android.content.Context;

import androidx.annotation.NonNull;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description :
 * @Date : create by QingSong in 2022-04-18 18:52
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.ts
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.4
 */
public interface Model {

    ModelFeature ARCHER = new ModelFeature("Archer");
    ModelFeature NEXUS_PLAYER = new ModelFeature("Nexus Player");

    /** True when the {@link android.os.Build#MODEL} equals the {@code model} given. */
    final class ModelFeature implements Feature {
        private final String mModel;

        private ModelFeature(String model) {
            mModel = model;
        }

        @Override
        public boolean isEnabled(Context context) {
            return isEnabled();
        }

        public boolean isEnabled() {
            return android.os.Build.MODEL.equals(mModel);
        }

        @NonNull
        @Override
        public String toString() {
            return "ModelFeature(" + mModel + ")=" + isEnabled();
        }
    }
}
