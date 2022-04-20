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
package cn.edu.cqupt.dmb.player.common.compat.internal;

/**
 * Static constants use by the TIF compat library
 */
final class Constants {
    static final String ACTION_GET_VERSION = "cn.edu.cqupt.dmb.player.common.compat.action.GET_VERSION";
    static final String EVENT_GET_VERSION = "cn.edu.cqupt.dmb.player.common.compat.event.GET_VERSION";
    static final String ACTION_COMPAT_ON = "cn.edu.cqupt.dmb.player.common.compat.action.COMPAT_ON";
    static final String EVENT_COMPAT_NOTIFY = "cn.edu.cqupt.dmb.player.common.compat.event.COMPAT_NOTIFY";
    static final String EVENT_COMPAT_NOTIFY_ERROR =
            "cn.edu.cqupt.dmb.player.common.compat.event.COMPAT_NOTIFY_ERROR";
    static final int TIF_COMPAT_VERSION = 1;

    private Constants() {
    }
}
