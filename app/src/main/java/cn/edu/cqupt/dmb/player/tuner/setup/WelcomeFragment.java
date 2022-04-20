/*
 * Copyright (C) 2015 The Android Open Source Project
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

package cn.edu.cqupt.dmb.player.tuner.setup;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.leanback.widget.GuidanceStylist.Guidance;
import androidx.leanback.widget.GuidedAction;

import java.util.List;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.common.ui.setup.SetupGuidedStepFragment;
import cn.edu.cqupt.dmb.player.common.ui.setup.SetupMultiPaneFragment;
import cn.edu.cqupt.dmb.player.tuner.api.Tuner;
import cn.edu.cqupt.dmb.player.tuner.prefs.TunerPreferences;

/**
 * A fragment for initial screen.
 */
public class WelcomeFragment extends SetupMultiPaneFragment {
    public static final String ACTION_CATEGORY = "cn.edu.cqupt.dmb.player.tuner.setup.WelcomeFragment";

    @Override
    protected SetupGuidedStepFragment onCreateContentFragment() {
        ContentFragment fragment = new ContentFragment();
        fragment.setArguments(getArguments());
        return fragment;
    }

    @Override
    protected String getActionCategory() {
        return ACTION_CATEGORY;
    }

    @Override
    protected boolean needsDoneButton() {
        return false;
    }

    /**
     * The content fragment of {@link WelcomeFragment}.
     */
    public static class ContentFragment extends SetupGuidedStepFragment {
        private int mChannelCountOnPreference;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            mChannelCountOnPreference =
                    TunerPreferences.getScannedChannelCount(getActivity().getApplicationContext());
            super.onCreate(savedInstanceState);
        }

        @NonNull
        @Override
        public Guidance onCreateGuidance(Bundle savedInstanceState) {
            String title;
            String description;
            int tunerType =
                    getArguments()
                            .getInt(
                                    BaseTunerSetupActivity.KEY_TUNER_TYPE,
                                    Tuner.TUNER_TYPE_BUILT_IN);
            if (mChannelCountOnPreference == 0) {
                switch (tunerType) {
                    case Tuner.TUNER_TYPE_USB:
                        title = getString(R.string.ut_setup_new_title);
                        description = getString(R.string.ut_setup_new_description);
                        break;
                    case Tuner.TUNER_TYPE_NETWORK:
                        title = getString(R.string.nt_setup_new_title);
                        description = getString(R.string.nt_setup_new_description);
                        break;
                    default:
                        title = getString(R.string.bt_setup_new_title);
                        description = getString(R.string.bt_setup_new_description);
                }
            } else {
                title = getString(R.string.bt_setup_again_title);
                switch (tunerType) {
                    case Tuner.TUNER_TYPE_USB:
                        description = getString(R.string.ut_setup_again_description);
                        break;
                    case Tuner.TUNER_TYPE_NETWORK:
                        description = getString(R.string.nt_setup_again_description);
                        break;
                    default:
                        description = getString(R.string.bt_setup_again_description);
                }
            }
            return new Guidance(title, description, null, null);
        }

        @Override
        public void onCreateActions(
                @NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
            String[] choices =
                    getResources()
                            .getStringArray(
                                    mChannelCountOnPreference == 0
                                            ? R.array.ut_setup_new_choices
                                            : R.array.ut_setup_again_choices);
            for (int i = 0; i < choices.length - 1; ++i) {
                actions.add(
                        new GuidedAction.Builder(getActivity()).id(i).title(choices[i]).build());
            }
            actions.add(
                    new GuidedAction.Builder(getActivity())
                            .id(ACTION_DONE)
                            .title(choices[choices.length - 1])
                            .build());
        }

        @Override
        protected String getActionCategory() {
            return ACTION_CATEGORY;
        }
    }
}
