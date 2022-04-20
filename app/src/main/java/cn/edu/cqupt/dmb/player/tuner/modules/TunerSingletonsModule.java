package cn.edu.cqupt.dmb.player.tuner.modules;

import cn.edu.cqupt.dmb.player.tuner.singletons.TunerSingletons;
import dagger.Module;

/**
 * Provides bindings for items provided by {@link TunerSingletons}.
 *
 * <p>Use this module to inject items directly instead of using {@code TunerSingletons}.
 */
@Module
public class TunerSingletonsModule {
    private final TunerSingletons mTunerSingletons;

    public TunerSingletonsModule(TunerSingletons tunerSingletons) {
        this.mTunerSingletons = tunerSingletons;
    }
}
