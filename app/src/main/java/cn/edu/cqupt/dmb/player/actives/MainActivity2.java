package cn.edu.cqupt.dmb.player.actives;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.edu.cqupt.dmb.player.R;
import cn.edu.cqupt.dmb.player.frame.DmbMediaDataSource;
import cn.edu.cqupt.dmb.player.frame.VideoPlayerFrame;
import cn.edu.cqupt.dmb.player.listener.VideoPlayerListenerImpl;
import cn.edu.cqupt.dmb.player.tuner.api.ChannelScanListener;
import cn.edu.cqupt.dmb.player.tuner.api.Tuner;
import cn.edu.cqupt.dmb.player.tuner.data.Channel.DeliverySystemType;
import cn.edu.cqupt.dmb.player.tuner.data.PsiData;
import cn.edu.cqupt.dmb.player.tuner.data.PsipData;
import cn.edu.cqupt.dmb.player.tuner.data.PsipData.EitItem;
import cn.edu.cqupt.dmb.player.tuner.data.Track.AtscAudioTrack;
import cn.edu.cqupt.dmb.player.tuner.data.Track.AtscCaptionTrack;
import cn.edu.cqupt.dmb.player.tuner.data.TunerChannel;
import cn.edu.cqupt.dmb.player.tuner.ts.TsParser;

public class MainActivity2 extends Activity {

    private static final String TAG = "MainActivity2";

    private VideoPlayerFrame videoPlayerFrame = null;

    private static final boolean DEBUG = false;
    public static final int ALL_PROGRAM_NUMBERS = -1;
    private TsParser mTsParser;
    private final Set<Integer> mPidSet = new HashSet<>();

    // To prevent channel duplication
    private final Set<Integer> mVctProgramNumberSet = new HashSet<>();
    private final Set<Integer> mSdtProgramNumberSet = new HashSet<>();
    private final SparseArray<TunerChannel> mChannelMap = new SparseArray<>();
    private final SparseBooleanArray mVctCaptionTracksFound = new SparseBooleanArray();
    private final SparseBooleanArray mEitCaptionTracksFound = new SparseBooleanArray();
    private final List<EventListener> mEventListeners = new ArrayList<>();
    private DeliverySystemType mDeliverySystemType = DeliverySystemType.DELIVERY_SYSTEM_UNDEFINED;
    private int mProgramNumber = ALL_PROGRAM_NUMBERS;

    private Tuner mTunerHal;
    private int mFrequency;
    private String mModulation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 强制全屏,全的不能再全的那种了
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main2);
        videoPlayerFrame = findViewById(R.id.main_2);
        videoPlayerFrame.setVideoListener(new VideoPlayerListenerImpl(videoPlayerFrame));

        init();
    }

    private void init() {
        SimpleExoPlayer simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this);
        PipedInputStream pipedInputStream = new PipedInputStream(188 * 10);
        PipedOutputStream pipedOutputStream = new PipedOutputStream();
        try {
            pipedOutputStream.connect(pipedInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedInputStream bufferedInputStream = new BufferedInputStream(pipedInputStream);
        new Thread(() -> {
            try {
                @SuppressLint("SdCardPath") FileInputStream fileInputStream = new FileInputStream("/sdcard/video/霍元甲_720P_60P_H265-encode.ts");
                byte[] bytes = new byte[188 * 10];

                Log.i(TAG, "init: 开始从 USB中读取数据");
                TsParser tsParser = getTsParser();
                ArrayList<byte[]> arrayList = new ArrayList<>();
                int cnt = 0;
                while (fileInputStream.read(bytes) > 0) {
//                    if (cnt == 1) {
//                        for (int i = 0; i < cnt; i++) {
//                            pipedOutputStream.write(arrayList.get(i));
//                        }
//                        pipedOutputStream.flush();
//                        cnt = 0;
//                        arrayList.clear();
//                    }
//                    cnt++;
//                    arrayList.add(bytes);
                    pipedOutputStream.write(bytes);
                    pipedOutputStream.flush();
//                    tsParser.feedTSData(bytes, 0, bytes.length);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        Log.i(TAG, "init: 设置自定义数据源");
        videoPlayerFrame.setDataSource(new DmbMediaDataSource(bufferedInputStream));
        try {
            Log.i(TAG, "init: 加载数据源");
            videoPlayerFrame.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private TsParser getTsParser() {
        return new TsParser(mTsOutputListener, true);
    }


    /**
     * Listener for detecting ATSC TV channels and receiving EPG data.
     */
    public interface EventListener extends ChannelScanListener {

        /**
         * Fired when new program events of an ATSC TV channel arrived.
         *
         * @param channel an ATSC TV channel
         * @param items   a list of EIT items that were received
         */
        void onEventDetected(TunerChannel channel, List<EitItem> items);

        /**
         * Fired when information of all detectable ATSC TV channels in current frequency arrived.
         */
        void onChannelScanDone();
    }

    private void startListening(int pid) {
        if (mPidSet.contains(pid)) {
            return;
        }
        mPidSet.add(pid);
        mTunerHal.addPidFilter(pid, Tuner.FILTER_TYPE_OTHER);
    }

    private final TsParser.TsOutputListener mTsOutputListener =
            new TsParser.TsOutputListener() {
                @Override
                public void onPatDetected(List<PsiData.PatItem> items) {
                    for (PsiData.PatItem i : items) {
                        if (mProgramNumber == ALL_PROGRAM_NUMBERS
                                || mProgramNumber == i.getProgramNo()) {
                            mTunerHal.addPidFilter(i.getPmtPid(), Tuner.FILTER_TYPE_OTHER);
                        }
                    }
                }

                @Override
                public void onEitPidDetected(int pid) {
                    startListening(pid);
                }

                @Override
                public void onEitItemParsed(
                        PsipData.VctItem channel, List<PsipData.EitItem> items) {
                    TunerChannel tunerChannel = mChannelMap.get(channel.getProgramNumber());
                    if (DEBUG) {
                        Log.d(
                                TAG,
                                "onEitItemParsed tunerChannel:"
                                        + tunerChannel
                                        + " "
                                        + channel.getProgramNumber());
                    }
                    int channelSourceId = channel.getSourceId();

                    // Source id 0 is useful for cases where a cable operator wishes to define a
                    // channel for
                    // which no EPG data is currently available.
                    // We don't handle such a case.
                    if (channelSourceId == 0) {
                        return;
                    }

                    // If at least a one caption track have been found in EIT items for the given
                    // channel,
                    // we starts to interpret the zero tracks as a clearance of the caption tracks.
                    boolean captionTracksFound = mEitCaptionTracksFound.get(channelSourceId);
                    for (PsipData.EitItem item : items) {
                        if (captionTracksFound) {
                            break;
                        }
                        List<AtscCaptionTrack> captionTracks = item.getCaptionTracks();
                        if (captionTracks != null && !captionTracks.isEmpty()) {
                            captionTracksFound = true;
                        }
                    }
                    mEitCaptionTracksFound.put(channelSourceId, captionTracksFound);
                    if (captionTracksFound) {
                        for (PsipData.EitItem item : items) {
                            item.setHasCaptionTrack();
                        }
                    }
                    if (tunerChannel != null && !mEventListeners.isEmpty()) {
                        for (EventListener eventListener : mEventListeners) {
                            eventListener.onEventDetected(tunerChannel, items);
                        }
                    }
                }

                @Override
                public void onEttPidDetected(int pid) {
                    startListening(pid);
                }

                @Override
                public void onAllVctItemsParsed() {
                    if (!mEventListeners.isEmpty()) {
                        for (EventListener eventListener : mEventListeners) {
                            eventListener.onChannelScanDone();
                        }
                    }
                }

                @Override
                public void onVctItemParsed(
                        PsipData.VctItem channel, List<PsiData.PmtItem> pmtItems) {
                    if (DEBUG) {
                        Log.d(TAG, "onVctItemParsed VCT " + channel);
                        Log.d(TAG, "                PMT " + pmtItems);
                    }

                    // Merges the audio and caption tracks located in PMT items into the tracks of
                    // the given
                    // tuner channel.
                    TunerChannel tunerChannel = new TunerChannel(channel, pmtItems);
                    List<AtscAudioTrack> audioTracks = new ArrayList<>();
                    List<AtscCaptionTrack> captionTracks = new ArrayList<>();
                    for (PsiData.PmtItem pmtItem : pmtItems) {
                        if (pmtItem.getAudioTracks() != null) {
                            audioTracks.addAll(pmtItem.getAudioTracks());
                        }
                        if (pmtItem.getCaptionTracks() != null) {
                            captionTracks.addAll(pmtItem.getCaptionTracks());
                        }
                    }
                    int channelProgramNumber = channel.getProgramNumber();

                    // If at least a one caption track have been found in VCT items for the given
                    // channel,
                    // we starts to interpret the zero tracks as a clearance of the caption tracks.
                    boolean captionTracksFound =
                            mVctCaptionTracksFound.get(channelProgramNumber)
                                    || !captionTracks.isEmpty();
                    mVctCaptionTracksFound.put(channelProgramNumber, captionTracksFound);
                    if (captionTracksFound) {
                        tunerChannel.setHasCaptionTrack();
                    }
                    tunerChannel.setAudioTracks(audioTracks);
                    tunerChannel.setCaptionTracks(captionTracks);
                    tunerChannel.setDeliverySystemType(mDeliverySystemType);
                    tunerChannel.setFrequency(mFrequency);
                    tunerChannel.setModulation(mModulation);
                    mChannelMap.put(tunerChannel.getProgramNumber(), tunerChannel);
                    boolean found = mVctProgramNumberSet.contains(channelProgramNumber);
                    if (!found) {
                        mVctProgramNumberSet.add(channelProgramNumber);
                    }
                    if (!mEventListeners.isEmpty()) {
                        for (EventListener eventListener : mEventListeners) {
                            eventListener.onChannelDetected(tunerChannel, !found);
                        }
                    }
                }

                @Override
                public void onSdtItemParsed(
                        PsipData.SdtItem channel, List<PsiData.PmtItem> pmtItems) {
                    if (DEBUG) {
                        Log.d(TAG, "onSdtItemParsed SDT " + channel);
                        Log.d(TAG, "                PMT " + pmtItems);
                    }

                    // Merges the audio and caption tracks located in PMT items into the tracks of
                    // the given
                    // tuner channel.
                    TunerChannel tunerChannel = new TunerChannel(channel, pmtItems);
                    List<AtscAudioTrack> audioTracks = new ArrayList<>();
                    List<AtscCaptionTrack> captionTracks = new ArrayList<>();
                    for (PsiData.PmtItem pmtItem : pmtItems) {
                        if (pmtItem.getAudioTracks() != null) {
                            audioTracks.addAll(pmtItem.getAudioTracks());
                        }
                        if (pmtItem.getCaptionTracks() != null) {
                            captionTracks.addAll(pmtItem.getCaptionTracks());
                        }
                    }
                    int channelProgramNumber = channel.getServiceId();
                    tunerChannel.setAudioTracks(audioTracks);
                    tunerChannel.setCaptionTracks(captionTracks);
                    tunerChannel.setDeliverySystemType(mDeliverySystemType);
                    tunerChannel.setFrequency(mFrequency);
                    tunerChannel.setModulation(mModulation);
                    mChannelMap.put(tunerChannel.getProgramNumber(), tunerChannel);
                    boolean found = mSdtProgramNumberSet.contains(channelProgramNumber);
                    if (!found) {
                        mSdtProgramNumberSet.add(channelProgramNumber);
                    }
                    if (!mEventListeners.isEmpty()) {
                        for (EventListener eventListener : mEventListeners) {
                            eventListener.onChannelDetected(tunerChannel, !found);
                        }
                    }
                }
            };

}
