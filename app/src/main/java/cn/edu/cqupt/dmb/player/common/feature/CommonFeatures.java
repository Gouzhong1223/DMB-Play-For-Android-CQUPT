/*
 * Copyright (C) 2016 The Android Open Source Project
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
 * limitations under the License
 */

package cn.edu.cqupt.dmb.player.common.feature;

import static cn.edu.cqupt.dmb.player.common.feature.BuildTypeFeature.ENG_ONLY_FEATURE;
import static cn.edu.cqupt.dmb.player.common.feature.FeatureUtils.and;
import static cn.edu.cqupt.dmb.player.common.feature.FeatureUtils.or;
import static cn.edu.cqupt.dmb.player.common.feature.TestableFeature.createTestableFeature;

import android.content.Context;
import android.util.Log;

import cn.edu.cqupt.dmb.player.common.flags.CloudEpgFlags;
import cn.edu.cqupt.dmb.player.common.flags.has.HasCloudEpgFlags;
import cn.edu.cqupt.dmb.player.common.util.LocationUtils;

/**
 * List of {@link Feature} that affect more than just the TV app.
 *
 * <p>Remove the {@code Feature} once it is launched.
 */
public class CommonFeatures {
    /**
     * DVR
     *
     * <p>See <a href="https://goto.google.com/atv-dvr-onepager">go/atv-dvr-onepager</a>
     *
     * <p>DVR API is introduced in N, it only works when app runs as a system app.
     */
    public static final TestableFeature DVR =
            createTestableFeature(and(Sdk.AT_LEAST_N, SystemAppFeature.SYSTEM_APP_FEATURE));
    /**
     * ENABLE_RECORDING_REGARDLESS_OF_STORAGE_STATUS
     *
     * <p>Enables dvr recording regardless of storage status.
     */
    public static final Feature FORCE_RECORDING_UNTIL_NO_SPACE =
            DeveloperPreferenceFeature.create("force_recording_until_no_space", false);
    /**
     * Show channel signal strength.
     */
    public static final Feature TUNER_SIGNAL_STRENGTH = ENG_ONLY_FEATURE;
    /**
     * Use AudioOnlyTvService for audio-only inputs.
     */
    public static final Feature ENABLE_TV_SERVICE = ENG_ONLY_FEATURE;
    private static final String TAG = "CommonFeatures";

    // TODO(b/74197177): remove when UI and API finalized.
    private static final boolean DEBUG = false;
    /**
     * Show postal code fragment before channel scan.
     */
    public static final Feature ENABLE_CLOUD_EPG_REGION =
            or(
                    FlagFeature.from(HasCloudEpgFlags::fromContext, CloudEpgFlags::supportedRegion),
                    new Feature() {
                        private final String[] supportedRegions = {
// AOSP_Comment_Out                             "US", "GB"
                        };

                        @Override
                        public boolean isEnabled(Context context) {
                            String country = LocationUtils.getCurrentCountry(context);
                            for (int i = 0; i < supportedRegions.length; i++) {
                                if (supportedRegions[i].equalsIgnoreCase(country)) {
                                    return true;
                                }
                            }
                            if (DEBUG) Log.d(TAG, "EPG flag false after country check");
                            return false;
                        }
                    });
}
