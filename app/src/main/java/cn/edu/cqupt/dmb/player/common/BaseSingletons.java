/*
 * Copyright (C) 2017 The Android Open Source Project
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

package cn.edu.cqupt.dmb.player.common;

import cn.edu.cqupt.dmb.player.common.buildtype.HasBuildType;
import cn.edu.cqupt.dmb.player.common.flags.has.HasCloudEpgFlags;
import cn.edu.cqupt.dmb.player.common.recording.RecordingStorageStatusManager;
import cn.edu.cqupt.dmb.player.common.util.Clock;

/**
 * Injection point for the base app
 */
public interface BaseSingletons extends HasCloudEpgFlags, HasBuildType {

    /*
     * Do not add any new methods here.
     *
     * To move a getter to Injection.
     *  1. Make a type injectable @Singleton.
     *  2. Mark the getter here as deprecated.
     *  3. Lazily inject the object in TvApplication.
     *  4. Move easy usages of getters to injection instead.
     *  5. Delete the method when all usages are migrated.
     */

    /* @deprecated use injection instead.  */
    @Deprecated
    Clock getClock();

    /* @deprecated use injection instead.  */
    @Deprecated
    RecordingStorageStatusManager getRecordingStorageStatusManager();
}
