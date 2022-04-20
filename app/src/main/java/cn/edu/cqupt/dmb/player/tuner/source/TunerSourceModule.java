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
package cn.edu.cqupt.dmb.player.tuner.source;

import javax.inject.Singleton;

import cn.edu.cqupt.dmb.player.tuner.api.TunerFactory;
import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for TV Tuners Sources.
 */
@Module()
public class TunerSourceModule {
    @Provides
    @Singleton
    TunerTsStreamerManager providesTunerTsStreamerManager(TunerFactory tunerFactory) {
        return new TunerTsStreamerManager(tunerFactory);
    }
}
