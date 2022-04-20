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
 * limitations under the License.
 */
package cn.edu.cqupt.dmb.player.tuner.modules;

import cn.edu.cqupt.dmb.player.common.flags.TunerFlags;
import cn.edu.cqupt.dmb.player.tuner.cc.CaptionTrackRenderer;
import cn.edu.cqupt.dmb.player.tuner.cc.CaptionTrackRendererFactory;
import cn.edu.cqupt.dmb.player.tuner.cc.CaptionWindowLayout;
import cn.edu.cqupt.dmb.player.tuner.cc.CaptionWindowLayoutFactory;
import cn.edu.cqupt.dmb.player.tuner.exoplayer.ExoPlayerSampleExtractor;
import cn.edu.cqupt.dmb.player.tuner.exoplayer.ExoPlayerSampleExtractorFactory;
import cn.edu.cqupt.dmb.player.tuner.exoplayer.FileSampleExtractor;
import cn.edu.cqupt.dmb.player.tuner.exoplayer.FileSampleExtractorFactory;
import cn.edu.cqupt.dmb.player.tuner.exoplayer.MpegTsRendererBuilder;
import cn.edu.cqupt.dmb.player.tuner.exoplayer.MpegTsRendererBuilderFactory;
import cn.edu.cqupt.dmb.player.tuner.exoplayer.MpegTsSampleExtractor;
import cn.edu.cqupt.dmb.player.tuner.exoplayer.MpegTsSampleExtractorFactory;
import cn.edu.cqupt.dmb.player.tuner.exoplayer.buffer.RecordingSampleBuffer;
import cn.edu.cqupt.dmb.player.tuner.exoplayer.buffer.RecordingSampleBufferFactory;
import cn.edu.cqupt.dmb.player.tuner.exoplayer.buffer.SampleChunkIoHelper;
import cn.edu.cqupt.dmb.player.tuner.exoplayer.buffer.SampleChunkIoHelperFactory;
import cn.edu.cqupt.dmb.player.tuner.source.TunerSourceModule;
import cn.edu.cqupt.dmb.player.tuner.tvinput.TunerRecordingSessionExoV2FactoryImpl;
import cn.edu.cqupt.dmb.player.tuner.tvinput.TunerRecordingSessionFactoryImpl;
import cn.edu.cqupt.dmb.player.tuner.tvinput.TunerRecordingSessionWorker;
import cn.edu.cqupt.dmb.player.tuner.tvinput.TunerRecordingSessionWorkerExoV2;
import cn.edu.cqupt.dmb.player.tuner.tvinput.TunerRecordingSessionWorkerExoV2Factory;
import cn.edu.cqupt.dmb.player.tuner.tvinput.TunerRecordingSessionWorkerFactory;
import cn.edu.cqupt.dmb.player.tuner.tvinput.TunerSessionExoV2Factory;
import cn.edu.cqupt.dmb.player.tuner.tvinput.TunerSessionOverlay;
import cn.edu.cqupt.dmb.player.tuner.tvinput.TunerSessionOverlayFactory;
import cn.edu.cqupt.dmb.player.tuner.tvinput.TunerSessionV1Factory;
import cn.edu.cqupt.dmb.player.tuner.tvinput.TunerSessionWorker;
import cn.edu.cqupt.dmb.player.tuner.tvinput.TunerSessionWorkerExoV2;
import cn.edu.cqupt.dmb.player.tuner.tvinput.TunerSessionWorkerExoV2Factory;
import cn.edu.cqupt.dmb.player.tuner.tvinput.TunerSessionWorkerFactory;
import cn.edu.cqupt.dmb.player.tuner.tvinput.factory.TunerRecordingSessionFactory;
import cn.edu.cqupt.dmb.player.tuner.tvinput.factory.TunerSessionFactory;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for TV Tuners.
 */
@Module(includes = {TunerSingletonsModule.class, TunerSourceModule.class})
public abstract class TunerModule {

    @Provides
    static TunerSessionFactory tunerSessionFactory(
            TunerFlags tunerFlags,
            TunerSessionV1Factory tunerSessionFactory,
            TunerSessionExoV2Factory tunerSessionExoV2Factory) {
        return tunerFlags.useExoplayerV2() ? tunerSessionExoV2Factory : tunerSessionFactory;
    }

    @Provides
    static TunerRecordingSessionFactory tunerRecordingSessionFactory(
            TunerFlags tunerFlags,
            TunerRecordingSessionFactoryImpl tunerRecordingSessionFactoryImpl,
            TunerRecordingSessionExoV2FactoryImpl tunerRecordingSessionExoV2FactoryImpl) {
        return tunerFlags.useExoplayerV2() ?
                tunerRecordingSessionExoV2FactoryImpl : tunerRecordingSessionFactoryImpl;
    }

    @Binds
    abstract TunerRecordingSessionWorker.Factory tunerRecordingSessionWorkerFactory(
            TunerRecordingSessionWorkerFactory tunerRecordingSessionWorkerFactory);

    @Binds
    abstract TunerRecordingSessionWorkerExoV2.Factory tunerRecordingSessionWorkerExoV2Factory(
            TunerRecordingSessionWorkerExoV2Factory tunerRecordingSessionWorkerExoV2Factory);

    @Binds
    abstract TunerSessionWorker.Factory tunerSessionWorkerFactory(
            TunerSessionWorkerFactory tunerSessionWorkerFactory);

    @Binds
    abstract TunerSessionOverlay.Factory tunerSessionOverlayFactory(
            TunerSessionOverlayFactory tunerSessionOverlayFactory);

    @Binds
    abstract CaptionTrackRenderer.Factory captionTrackRendererFactory(
            CaptionTrackRendererFactory captionTrackRendererFactory);

    @Binds
    abstract CaptionWindowLayout.Factory captionWindowLayoutFactory(
            CaptionWindowLayoutFactory captionWindowLayoutFactory);

    @Binds
    abstract TunerSessionWorkerExoV2.Factory tunerSessionWorkerExoV2Factory(
            TunerSessionWorkerExoV2Factory tunerSessionWorkerExoV2Factory);

    @Binds
    abstract MpegTsRendererBuilder.Factory mpegTsRendererBuilderFactory(
            MpegTsRendererBuilderFactory mpegTsRendererBuilderFactory);

    @Binds
    abstract MpegTsSampleExtractor.Factory mpegTsSampleExtractorFactory(
            MpegTsSampleExtractorFactory mpegTsSampleExtractorFactory);

    @Binds
    abstract FileSampleExtractor.Factory fileSampleExtractorFactory(
            FileSampleExtractorFactory fileSampleExtractorFactory);

    @Binds
    abstract RecordingSampleBuffer.Factory recordingSampleBufferFactory(
            RecordingSampleBufferFactory recordingSampleBufferFactory);

    @Binds
    abstract ExoPlayerSampleExtractor.Factory exoPlayerSampleExtractorFactory(
            ExoPlayerSampleExtractorFactory exoPlayerSampleExtractorFactory);

    @Binds
    abstract SampleChunkIoHelper.Factory sampleChunkIoHelperFactory(
            SampleChunkIoHelperFactory sampleChunkIoHelperFactory);

    @Binds
    abstract cn.edu.cqupt.dmb.player.tuner.exoplayer2.MpegTsSampleExtractor.Factory
    mpegTsSampleExtractorFactoryV2(
            cn.edu.cqupt.dmb.player.tuner.exoplayer2.MpegTsSampleExtractorFactory
                    mpegTsSampleExtractorFactory);

    @Binds
    abstract cn.edu.cqupt.dmb.player.tuner.exoplayer2.ExoPlayerSampleExtractor.Factory
    exoPlayerSampleExtractorFactoryV2(
            cn.edu.cqupt.dmb.player.tuner.exoplayer2.ExoPlayerSampleExtractorFactory
                    exoPlayerSampleExtractorFactory);

    @Binds
    abstract cn.edu.cqupt.dmb.player.tuner.exoplayer2.FileSampleExtractor.Factory
    fileSampleExtractorFactoryV2(
            cn.edu.cqupt.dmb.player.tuner.exoplayer2.FileSampleExtractorFactory fileSampleExtractorFactory);

    @Binds
    abstract cn.edu.cqupt.dmb.player.tuner.exoplayer2.buffer.RecordingSampleBuffer.Factory
    recordingSampleBufferFactoryV2(
            cn.edu.cqupt.dmb.player.tuner.exoplayer2.buffer.RecordingSampleBufferFactory
                    recordingSampleBufferFactory);

    @Binds
    abstract cn.edu.cqupt.dmb.player.tuner.exoplayer2.buffer.SampleChunkIoHelper.Factory
    sampleChunkIoHelperFactoryV2(
            cn.edu.cqupt.dmb.player.tuner.exoplayer2.buffer.SampleChunkIoHelperFactory
                    sampleChunkIoHelperFactory);
}
