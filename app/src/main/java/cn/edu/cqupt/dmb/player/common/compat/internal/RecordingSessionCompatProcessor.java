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

import android.util.Log;

import cn.edu.cqupt.dmb.player.common.compat.api.RecordingSessionCompatCommands;
import cn.edu.cqupt.dmb.player.common.compat.api.RecordingSessionCompatEvents;
import cn.edu.cqupt.dmb.player.common.compat.api.SessionEventNotifier;
import cn.edu.cqupt.dmb.player.common.compat.internal.Commands.PrivateRecordingCommand;
import cn.edu.cqupt.dmb.player.common.compat.internal.RecordingEvents.NotifyDevToast;
import cn.edu.cqupt.dmb.player.common.compat.internal.RecordingEvents.RecordingSessionEvent;
import cn.edu.cqupt.dmb.player.common.compat.internal.RecordingEvents.RecordingStarted;

/**
 * Sends {@link RecordingSessionCompatEvents} to the TV App via {@link SessionEventNotifier} and
 * receives Commands from TV App forwarding them to {@link RecordingSessionCompatProcessor}
 */
public final class RecordingSessionCompatProcessor
        extends SessionCompatProcessor<PrivateRecordingCommand, RecordingSessionEvent>
        implements RecordingSessionCompatEvents {

    private static final String TAG = "RecordingSessionCompatProc";

    private final RecordingSessionCompatCommands mRecordingSessionOnCompat;

    public RecordingSessionCompatProcessor(
            SessionEventNotifier sessionEventNotifier,
            RecordingSessionCompatCommands recordingSessionOnCompat) {
        super(sessionEventNotifier, PrivateRecordingCommand.parser());
        mRecordingSessionOnCompat = recordingSessionOnCompat;
    }

    @Override
    protected void onCompat(PrivateRecordingCommand privateCommand) {
        switch (privateCommand.getCommandCase()) {
            case ON_DEV_MESSAGE:
                mRecordingSessionOnCompat.onDevMessage(
                        privateCommand.getOnDevMessage().getMessage());
                break;
            case COMMAND_NOT_SET:
                Log.w(TAG, "Command not set ");
        }
    }

    @Override
    public void notifyDevToast(String message) {
        NotifyDevToast devMessage = NotifyDevToast.newBuilder().setMessage(message).build();
        RecordingSessionEvent sessionEvent =
                createSessionEvent().setNotifyDevMessage(devMessage).build();
        notifyCompat(sessionEvent);
    }

    @Override
    public void notifyRecordingStarted(String uri) {
        RecordingStarted event = RecordingStarted.newBuilder().setUri(uri).build();
        RecordingSessionEvent sessionEvent =
                createSessionEvent().setRecordingStarted(event).build();
        notifyCompat(sessionEvent);
    }

    private RecordingSessionEvent.Builder createSessionEvent() {

        return RecordingSessionEvent.newBuilder().setCompatVersion(Constants.TIF_COMPAT_VERSION);
    }
}
