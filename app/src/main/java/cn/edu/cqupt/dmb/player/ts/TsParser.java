package cn.edu.cqupt.dmb.player.ts;

import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;

import cn.edu.cqupt.dmb.player.ts.data.PsiData.PatItem;
import cn.edu.cqupt.dmb.player.ts.data.PsiData.PmtItem;
import cn.edu.cqupt.dmb.player.ts.data.PsipData.EitItem;
import cn.edu.cqupt.dmb.player.ts.data.PsipData.EttItem;
import cn.edu.cqupt.dmb.player.ts.data.PsipData.MgtItem;
import cn.edu.cqupt.dmb.player.ts.data.PsipData.SdtItem;
import cn.edu.cqupt.dmb.player.ts.data.PsipData.VctItem;
import cn.edu.cqupt.dmb.player.ts.data.SectionParser;
import cn.edu.cqupt.dmb.player.ts.data.SectionParser.OutputListener;
import cn.edu.cqupt.dmb.player.ts.data.TunerChannel;
import cn.edu.cqupt.dmb.player.utils.data.ByteArrayBuffer;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : Parses MPEG-2 TS packets.
 * @Date : create by QingSong in 2022-04-18 16:56
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.ts
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.4
 */
public class TsParser {
    private static final String TAG = "TsParser";
    private static final boolean DEBUG = false;

    public static final int ATSC_SI_BASE_PID = 0x1ffb;
    public static final int PAT_PID = 0x0000;
    public static final int DVB_SDT_PID = 0x0011;
    public static final int DVB_EIT_PID = 0x0012;
    private static final int TS_PACKET_START_CODE = 0x47;
    private static final int TS_PACKET_TEI_MASK = 0x80;
    private static final int TS_PACKET_SIZE = 188;

    /*
     * Using a SparseArray removes the need to auto box the int key for mStreamMap
     * in feedTdPacket which is called 100 times a second. This greatly reduces the
     * number of objects created and the frequency of garbage collection.
     * Other maps might be suitable for a SparseArray, but the performance
     * trade offs must be considered carefully.
     * mStreamMap is the only one called at such a high rate.
     */
    private final SparseArray<Stream> streamMap = new SparseArray<>();
    private final Map<Integer, VctItem> sourceIdToVctItemMap = new HashMap<>();
    private final Map<Integer, String> sourceIdToVctItemDescriptionMap = new HashMap<>();
    private final Map<Integer, VctItem> programNumberToVctItemMap = new HashMap<>();
    private final Map<Integer, List<PmtItem>> programNumberToPMTMap = new HashMap<>();
    private final Map<Integer, List<EitItem>> sourceIdToEitMap = new HashMap<>();
    private final Map<Integer, SdtItem> programNumberToSdtItemMap = new HashMap<>();
    private final Map<EventSourceEntry, List<EitItem>> eitMap = new HashMap<>();
    private final Map<EventSourceEntry, List<EttItem>> eTTMap = new HashMap<>();
    private final TreeSet<Integer> eITPids = new TreeSet<>();
    private final TreeSet<Integer> eTTPids = new TreeSet<>();
    private final SparseBooleanArray programNumberHandledStatus = new SparseBooleanArray();
    private final SparseBooleanArray vctItemHandledStatus = new SparseBooleanArray();
    private final TsOutputListener listener;
    private final boolean isDvbSignal;

    private int vctItemCount;
    private int handledVctItemCount;
    private int vctSectionParsedCount;
    private boolean[] vctSectionParsed;

    public interface TsOutputListener {
        void onPatDetected(List<PatItem> items);

        void onEitPidDetected(int pid);

        void onVctItemParsed(VctItem channel, List<PmtItem> pmtItems);

        void onEitItemParsed(VctItem channel, List<EitItem> items);

        void onEttPidDetected(int pid);

        void onAllVctItemsParsed();

        void onSdtItemParsed(SdtItem channel, List<PmtItem> pmtItems);
    }

    private abstract static class Stream {
        private static final int INVALID_CONTINUITY_COUNTER = -1;
        private static final int NUM_CONTINUITY_COUNTER = 16;

        protected int continuityCounter = INVALID_CONTINUITY_COUNTER;
        protected final ByteArrayBuffer packet = new ByteArrayBuffer(TS_PACKET_SIZE);

        public void feedData(byte[] data, int continuityCounter, boolean startIndicator) {
            if ((this.continuityCounter + 1) % NUM_CONTINUITY_COUNTER != continuityCounter) {
                packet.setLength(0);
            }
            this.continuityCounter = continuityCounter;
            handleData(data, startIndicator);
        }

        protected abstract void handleData(byte[] data, boolean startIndicator);

        protected abstract void resetDataVersions();
    }

    private class SectionStream extends Stream {
        private final SectionParser sectionParser;
        private final int mPid;

        public SectionStream(int pid) {
            mPid = pid;
            // When PMT is parsed later than VCT.
            // When PMT is parsed later than SDT.
            // The current section was handled before.
            // Channel description
            // Event Information description
            OutputListener sectionListener = new OutputListener() {
                @Override
                public void onPatParsed(List<PatItem> items) {
                    for (PatItem i : items) {
                        startListening(i.getPmtPid());
                    }
                    if (listener != null) {
                        listener.onPatDetected(items);
                    }
                }

                @Override
                public void onPmtParsed(int programNumber, List<PmtItem> items) {
                    programNumberToPMTMap.put(programNumber, items);
                    if (DEBUG) {
                        Log.d(
                                TAG,
                                "onPMTParsed, programNo "
                                        + programNumber
                                        + " handledStatus is "
                                        + programNumberHandledStatus.get(
                                        programNumber, false));
                    }
                    int statusIndex = programNumberHandledStatus.indexOfKey(programNumber);
                    if (statusIndex < 0) {
                        programNumberHandledStatus.put(programNumber, false);
                    }
                    if (!programNumberHandledStatus.get(programNumber)) {
                        VctItem vctItem = programNumberToVctItemMap.get(programNumber);
                        if (vctItem != null) {
                            // When PMT is parsed later than VCT.
                            programNumberHandledStatus.put(programNumber, true);
                            handleVctItem(vctItem, items);
                            handledVctItemCount++;
                            if (handledVctItemCount >= vctItemCount
                                    && vctSectionParsedCount >= vctSectionParsed.length
                                    && listener != null) {
                                listener.onAllVctItemsParsed();
                            }
                        }
                        SdtItem sdtItem = programNumberToSdtItemMap.get(programNumber);
                        if (sdtItem != null) {
                            // When PMT is parsed later than SDT.
                            programNumberHandledStatus.put(programNumber, true);
                            handleSdtItem(sdtItem, items);
                        }
                    }
                }

                @Override
                public void onMgtParsed(List<MgtItem> items) {
                    for (MgtItem i : items) {
                        if (streamMap.get(i.getTableTypePid()) != null) {
                            continue;
                        }
                        if (i.getTableType() >= MgtItem.TABLE_TYPE_EIT_RANGE_START
                                && i.getTableType() <= MgtItem.TABLE_TYPE_EIT_RANGE_END) {
                            startListening(i.getTableTypePid());
                            eITPids.add(i.getTableTypePid());
                            if (listener != null) {
                                listener.onEitPidDetected(i.getTableTypePid());
                            }
                        } else if (i.getTableType() == MgtItem.TABLE_TYPE_CHANNEL_ETT
                                || (i.getTableType() >= MgtItem.TABLE_TYPE_ETT_RANGE_START
                                && i.getTableType()
                                <= MgtItem.TABLE_TYPE_ETT_RANGE_END)) {
                            startListening(i.getTableTypePid());
                            eTTPids.add(i.getTableTypePid());
                            if (listener != null) {
                                listener.onEttPidDetected(i.getTableTypePid());
                            }
                        }
                    }
                }

                @Override
                public void onVctParsed(
                        List<VctItem> items, int sectionNumber, int lastSectionNumber) {
                    if (vctSectionParsed == null) {
                        vctSectionParsed = new boolean[lastSectionNumber + 1];
                    } else if (vctSectionParsed[sectionNumber]) {
                        // The current section was handled before.
                        if (DEBUG) {
                            Log.d(TAG, "Duplicate VCT section found.");
                        }
                        return;
                    }
                    vctSectionParsed[sectionNumber] = true;
                    vctSectionParsedCount++;
                    vctItemCount += items.size();
                    for (VctItem i : items) {
                        if (DEBUG) Log.d(TAG, "onVCTParsed " + i);
                        if (i.getSourceId() != 0) {
                            sourceIdToVctItemMap.put(i.getSourceId(), i);
                            i.setDescription(
                                    sourceIdToVctItemDescriptionMap.get(i.getSourceId()));
                        }
                        int programNumber = i.getProgramNumber();
                        programNumberToVctItemMap.put(programNumber, i);
                        List<PmtItem> pmtList = programNumberToPMTMap.get(programNumber);
                        if (pmtList != null) {
                            programNumberHandledStatus.put(programNumber, true);
                            handleVctItem(i, pmtList);
                            handledVctItemCount++;
                            if (handledVctItemCount >= vctItemCount
                                    && vctSectionParsedCount >= vctSectionParsed.length
                                    && listener != null) {
                                listener.onAllVctItemsParsed();
                            }
                        } else {
                            programNumberHandledStatus.put(programNumber, false);
                            Log.i(
                                    TAG,
                                    "onVCTParsed, but PMT for programNo "
                                            + programNumber
                                            + " is not found yet.");
                        }
                    }
                }

                @Override
                public void onEitParsed(int sourceId, List<EitItem> items) {
                    if (DEBUG) Log.d(TAG, "onEITParsed " + sourceId);
                    EventSourceEntry entry = new EventSourceEntry(mPid, sourceId);
                    eitMap.put(entry, items);
                    handleEvents(sourceId);
                }

                @Override
                public void onEttParsed(int sourceId, List<EttItem> descriptions) {
                    if (DEBUG) {
                        Log.d(
                                TAG,
                                String.format(
                                        "onETTParsed sourceId: %d, descriptions.size(): %d",
                                        sourceId, descriptions.size()));
                    }
                    for (EttItem item : descriptions) {
                        if (item.eventId == 0) {
                            // Channel description
                            sourceIdToVctItemDescriptionMap.put(sourceId, item.text);
                            VctItem vctItem = sourceIdToVctItemMap.get(sourceId);
                            if (vctItem != null) {
                                vctItem.setDescription(item.text);
                                List<PmtItem> pmtItems =
                                        programNumberToPMTMap.get(vctItem.getProgramNumber());
                                if (pmtItems != null) {
                                    handleVctItem(vctItem, pmtItems);
                                }
                            }
                        }
                    }

                    // Event Information description
                    EventSourceEntry entry = new EventSourceEntry(mPid, sourceId);
                    eTTMap.put(entry, descriptions);
                    handleEvents(sourceId);
                }

                @Override
                public void onSdtParsed(List<SdtItem> sdtItems) {
                    for (SdtItem sdtItem : sdtItems) {
                        if (DEBUG) Log.d(TAG, "onSdtParsed " + sdtItem);
                        int programNumber = sdtItem.getServiceId();
                        programNumberToSdtItemMap.put(programNumber, sdtItem);
                        List<PmtItem> pmtList = programNumberToPMTMap.get(programNumber);
                        if (pmtList != null) {
                            programNumberHandledStatus.put(programNumber, true);
                            handleSdtItem(sdtItem, pmtList);
                        } else {
                            programNumberHandledStatus.put(programNumber, false);
                            Log.i(
                                    TAG,
                                    "onSdtParsed, but PMT for programNo "
                                            + programNumber
                                            + " is not found yet.");
                        }
                    }
                }
            };
            sectionParser = new SectionParser(sectionListener);
        }

        @Override
        protected void handleData(byte[] data, boolean startIndicator) {
            int startPos = 0;
            if (packet.length() == 0) {
                if (startIndicator) {
                    startPos = (data[0] & 0xff) + 1;
                } else {
                    // Don't know where the section starts yet. Wait until start indicator is on.
                    return;
                }
            } else {
                if (startIndicator) {
                    startPos = 1;
                }
            }

            // When a broken packet is encountered, parsing will stop and return right away.
            if (startPos >= data.length) {
                packet.setLength(0);
                return;
            }
            packet.append(data, startPos, data.length - startPos);
            sectionParser.parseSections(packet);
        }

        @Override
        protected void resetDataVersions() {
            sectionParser.resetVersionNumbers();
        }

    }

    private static class EventSourceEntry {
        public final int pid;
        public final int sourceId;

        public EventSourceEntry(int pid, int sourceId) {
            this.pid = pid;
            this.sourceId = sourceId;
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + pid;
            result = 31 * result + sourceId;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof EventSourceEntry) {
                EventSourceEntry another = (EventSourceEntry) obj;
                return pid == another.pid && sourceId == another.sourceId;
            }
            return false;
        }
    }

    private void handleVctItem(VctItem channel, List<PmtItem> pmtItems) {
        if (DEBUG) {
            Log.d(TAG, "handleVctItem " + channel);
        }
        if (listener != null) {
            listener.onVctItemParsed(channel, pmtItems);
        }
        int sourceId = channel.getSourceId();
        int statusIndex = vctItemHandledStatus.indexOfKey(sourceId);
        if (statusIndex < 0) {
            vctItemHandledStatus.put(sourceId, false);
            return;
        }
        if (!vctItemHandledStatus.valueAt(statusIndex)) {
            List<EitItem> eitItems = sourceIdToEitMap.get(sourceId);
            if (eitItems != null) {
                // When VCT is parsed later than EIT.
                vctItemHandledStatus.put(sourceId, true);
                handleEitItems(channel, eitItems);
            }
        }
    }

    private void handleEitItems(VctItem channel, List<EitItem> items) {
        if (listener != null) {
            listener.onEitItemParsed(channel, items);
        }
    }

    private void handleSdtItem(SdtItem channel, List<PmtItem> pmtItems) {
        if (DEBUG) {
            Log.d(TAG, "handleSdtItem " + channel);
        }
        if (listener != null) {
            listener.onSdtItemParsed(channel, pmtItems);
        }
    }

    private void handleEvents(int sourceId) {
        Map<Integer, EitItem> itemSet = new HashMap<>();
        for (int pid : eITPids) {
            List<EitItem> eitItems = eitMap.get(new EventSourceEntry(pid, sourceId));
            if (eitItems != null) {
                for (EitItem item : eitItems) {
                    item.setDescription(null);
                    itemSet.put(item.getEventId(), item);
                }
            }
        }
        for (int pid : eTTPids) {
            List<EttItem> ettItems = eTTMap.get(new EventSourceEntry(pid, sourceId));
            if (ettItems != null) {
                for (EttItem ettItem : ettItems) {
                    if (ettItem.eventId != 0) {
                        EitItem item = itemSet.get(ettItem.eventId);
                        if (item != null) {
                            item.setDescription(ettItem.text);
                        }
                    }
                }
            }
        }
        List<EitItem> items = new ArrayList<>(itemSet.values());
        sourceIdToEitMap.put(sourceId, items);
        VctItem channel = sourceIdToVctItemMap.get(sourceId);
        if (channel != null && programNumberHandledStatus.get(channel.getProgramNumber())) {
            vctItemHandledStatus.put(sourceId, true);
            handleEitItems(channel, items);
        } else {
            vctItemHandledStatus.put(sourceId, false);
            if (!isDvbSignal) {
                // Log only when zapping to non-DVB channels, since there is not VCT in DVB signal.
                Log.i(TAG, "onEITParsed, but VCT for sourceId " + sourceId + " is not found yet.");
            }
        }
    }

    /**
     * Creates MPEG-2 TS parser.
     *
     * @param listener TsOutputListener
     */
    public TsParser(TsOutputListener listener, boolean isDvbSignal) {
        startListening(PAT_PID);
        startListening(ATSC_SI_BASE_PID);
        this.isDvbSignal = isDvbSignal;
        if (isDvbSignal) {
            startListening(DVB_EIT_PID);
            startListening(DVB_SDT_PID);
        }
        this.listener = listener;
    }

    private void startListening(int pid) {
        streamMap.put(pid, new SectionStream(pid));
    }

    private void feedTSPacket(byte[] tsData, int pos) {
        if (tsData.length < pos + TS_PACKET_SIZE) {
            if (DEBUG) Log.d(TAG, "Data should include a single TS packet.");
            return;
        }
        if (tsData[pos] != TS_PACKET_START_CODE) {
            if (DEBUG) Log.d(TAG, "Invalid ts packet.");
            return;
        }
        if ((tsData[pos + 1] & TS_PACKET_TEI_MASK) != 0) {
            if (DEBUG) Log.d(TAG, "Erroneous ts packet.");
            return;
        }

        // For details for the structure of TS packet, see H.222.0 Table 2-2.
        int pid = ((tsData[pos + 1] & 0x1f) << 8) | (tsData[pos + 2] & 0xff);
        boolean hasAdaptation = (tsData[pos + 3] & 0x20) != 0;
        boolean hasPayload = (tsData[pos + 3] & 0x10) != 0;
        boolean payloadStartIndicator = (tsData[pos + 1] & 0x40) != 0;
        int continuityCounter = tsData[pos + 3] & 0x0f;
        Stream stream = streamMap.get(pid);
        int payloadPos = pos;
        payloadPos += hasAdaptation ? 5 + (tsData[pos + 4] & 0xff) : 4;
        if (!hasPayload || stream == null) {
            // We are not interested in this packet.
            return;
        }
        if (payloadPos >= pos + TS_PACKET_SIZE) {
            if (DEBUG) Log.d(TAG, "Payload should be included in a single TS packet.");
            return;
        }
        stream.feedData(
                Arrays.copyOfRange(tsData, payloadPos, pos + TS_PACKET_SIZE),
                continuityCounter,
                payloadStartIndicator);
    }

    /**
     * Feeds MPEG-2 TS data to parse.
     *
     * @param tsData buffer for ATSC TS stream
     * @param pos    the offset where buffer starts
     * @param length The length of available data
     */
    public void feedTSData(byte[] tsData, int pos, int length) {
        for (; pos <= length - TS_PACKET_SIZE; pos += TS_PACKET_SIZE) {
            feedTSPacket(tsData, pos);
        }
    }

    /**
     * Retrieves the channel information regardless of being well-formed.
     *
     * @return {@link List} of {@link TunerChannel}
     */
    public List<TunerChannel> getMalFormedChannels() {
        List<TunerChannel> incompleteChannels = new ArrayList<>();
        for (int i = 0; i < programNumberHandledStatus.size(); i++) {
            if (!programNumberHandledStatus.valueAt(i)) {
                int programNumber = programNumberHandledStatus.keyAt(i);
                List<PmtItem> pmtList = programNumberToPMTMap.get(programNumber);
                if (pmtList != null) {
                    TunerChannel tunerChannel = new TunerChannel(programNumber, pmtList);
                    incompleteChannels.add(tunerChannel);
                }
            }
        }
        return incompleteChannels;
    }

    /**
     * Reset the versions so that data with old version number can be handled.
     */
    public void resetDataVersions() {
        for (int eitPid : eITPids) {
            Stream stream = streamMap.get(eitPid);
            if (stream != null) {
                stream.resetDataVersions();
            }
        }
    }
}
