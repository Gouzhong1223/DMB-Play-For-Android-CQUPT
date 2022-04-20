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

import dagger.Module;
import dagger.Provides;
import dagger.Reusable;

import cn.edu.cqupt.dmb.player.common.flags.BackendKnobsFlags;
import cn.edu.cqupt.dmb.player.common.flags.CloudEpgFlags;
import cn.edu.cqupt.dmb.player.common.flags.DvrFlags;
import cn.edu.cqupt.dmb.player.common.flags.LegacyFlags;
import cn.edu.cqupt.dmb.player.common.flags.SetupFlags;
import cn.edu.cqupt.dmb.player.common.flags.StartupFlags;
import cn.edu.cqupt.dmb.player.common.flags.TunerFlags;
import cn.edu.cqupt.dmb.player.common.flags.UiFlags;

/**
 * Provides public fields for each flag so they can be changed before injection.
 */
@Module
public class SettableFlagsModule {

    public DefaultBackendKnobsFlags backendKnobsFlags = new DefaultBackendKnobsFlags();
    public DefaultCloudEpgFlags cloudEpgFlags = new DefaultCloudEpgFlags();
    public DefaultDvrFlags dvrFlags = new DefaultDvrFlags();
    public DefaultLegacyFlags legacyFlags = DefaultLegacyFlags.DEFAULT;
    public DefaultSetupFlags setupFlags = new DefaultSetupFlags();
    public DefaultStartupFlags startupFlags = new DefaultStartupFlags();
    public DefaultTunerFlags tunerFlags = new DefaultTunerFlags();
    public DefaultUiFlags uiFlags = new DefaultUiFlags();

    @Provides
    @Reusable
    BackendKnobsFlags provideBackendKnobsFlags() {
        return backendKnobsFlags;
    }

    @Provides
    @Reusable
    CloudEpgFlags provideCloudEpgFlags() {
        return cloudEpgFlags;
    }

    @Provides
    @Reusable
    DvrFlags provideDvrFlags() {
        return dvrFlags;
    }

    @Provides
    @Reusable
    LegacyFlags provideLegacyFlags() {
        return legacyFlags;
    }

    @Provides
    @Reusable
    SetupFlags provideSetupFlags() {
        return setupFlags;
    }

    @Provides
    @Reusable
    StartupFlags provideStartupFlags() {
        return startupFlags;
    }

    @Provides
    @Reusable
    TunerFlags provideTunerFlags() {
        return tunerFlags;
    }

    @Provides
    @Reusable
    UiFlags provideUiFlags() {
        return uiFlags;
    }
}
