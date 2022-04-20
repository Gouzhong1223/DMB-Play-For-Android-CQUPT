/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.edu.cqupt.dmb.player.common.flags.impl;

import cn.edu.cqupt.dmb.player.common.flags.BackendKnobsFlags;
import cn.edu.cqupt.dmb.player.common.flags.CloudEpgFlags;
import cn.edu.cqupt.dmb.player.common.flags.DvrFlags;
import cn.edu.cqupt.dmb.player.common.flags.LegacyFlags;
import cn.edu.cqupt.dmb.player.common.flags.StartupFlags;
import cn.edu.cqupt.dmb.player.common.flags.TunerFlags;
import cn.edu.cqupt.dmb.player.common.flags.UiFlags;
import dagger.Module;
import dagger.Provides;
import dagger.Reusable;

/**
 * Provides default flags.
 */
@Module
public class DefaultFlagsModule {

    @Provides
    @Reusable
    BackendKnobsFlags provideBackendKnobsFlags() {
        return new DefaultBackendKnobsFlags();
    }

    @Provides
    @Reusable
    CloudEpgFlags provideCloudEpgFlags() {
        return new DefaultCloudEpgFlags();
    }

    @Provides
    @Reusable
    DvrFlags provideDvrFlags() {
        return new DefaultDvrFlags();
    }

    @Provides
    @Reusable
    LegacyFlags provideLegacyFlags() {
        return DefaultLegacyFlags.DEFAULT;
    }

    @Provides
    @Reusable
    StartupFlags provideStartupFlags() {
        return new DefaultStartupFlags();
    }

    @Provides
    @Reusable
    TunerFlags provideTunerFlags() {
        return new DefaultTunerFlags();
    }

    @Provides
    @Reusable
    UiFlags provideUiFlags() {
        return new DefaultUiFlags();
    }
}
