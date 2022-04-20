/*
 * Copyright (C) 2018 The Android Open Source Project
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

package cn.edu.cqupt.dmb.player.common.compat;

import android.content.Context;
import android.media.tv.TvView;

import android.os.Bundle;


import android.util.ArrayMap;
import android.util.AttributeSet;

import cn.edu.cqupt.dmb.player.common.compat.api.PrivateCommandSender;
import cn.edu.cqupt.dmb.player.common.compat.api.TvInputCallbackCompatEvents;
import cn.edu.cqupt.dmb.player.common.compat.api.TvViewCompatCommands;
import cn.edu.cqupt.dmb.player.common.compat.internal.TvViewCompatProcessor;

/**
 * TIF Compatibility for {@link TvView}.
 *
 * <p>Extends {@code TvView} in a backwards compatible way.
 */
public class TvViewCompat extends TvView implements TvViewCompatCommands, PrivateCommandSender {

    private final TvViewCompatProcessor mTvViewCompatProcessor;

    public TvViewCompat(Context context) {
        this(context, null);
    }

    public TvViewCompat(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TvViewCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTvViewCompatProcessor = new TvViewCompatProcessor(this);
    }

    @Override
    public void setCallback(TvInputCallback callback) {
        super.setCallback(callback);
        if (callback instanceof TvInputCallbackCompat) {
            TvInputCallbackCompat compatEvents = (TvInputCallbackCompat) callback;
            mTvViewCompatProcessor.setCallback(compatEvents);
            compatEvents.mTvViewCompatProcessor = mTvViewCompatProcessor;
        }
    }

    @Override
    public void devMessage(String message) {
        mTvViewCompatProcessor.devMessage(message);
    }

    /**
     * TIF Compatibility for {@link TvInputCallback}.
     *
     * <p>Extends {@code TvInputCallback} in a backwards compatible way.
     */
    public static class TvInputCallbackCompat extends TvInputCallback
            implements TvInputCallbackCompatEvents {
        private final ArrayMap<String, Integer> inputCompatVersionMap = new ArrayMap<>();
        private TvViewCompatProcessor mTvViewCompatProcessor;

//        @Override
//        public void onEvent(String inputId, String eventType, Bundle eventArgs) {
//            if (mTvViewCompatProcessor != null
//                    && !mTvViewCompatProcessor.handleEvent(inputId, eventType, eventArgs)) {
//                super.onEvent(inputId, eventType, eventArgs);
//            }
//        }

        public int getTifCompatVersionForInput(String inputId) {
            return inputCompatVersionMap.containsKey(inputId)
                    ? inputCompatVersionMap.get(inputId)
                    : 0;
        }

        @Override
        public void onDevToast(String inputId, String message) {
        }

        /**
         * This is called when the signal strength is notified.
         *
         * @param inputId The ID of the TV input bound to this view.
         * @param value   The current signal strength. Should be one of the followings.
         *                <ul>
         *                  <li>{@link TvInputConstantCompat#SIGNAL_STRENGTH_NOT_USED}
         *                  <li>{@link TvInputConstantCompat#SIGNAL_STRENGTH_ERROR}
         *                  <li>{@link TvInputConstantCompat#SIGNAL_STRENGTH_UNKNOWN}
         *                  <li>{int [0, 100]}
         *                </ul>
         */
        @Override
        public void onSignalStrength(String inputId, int value) {
        }
    }
}
