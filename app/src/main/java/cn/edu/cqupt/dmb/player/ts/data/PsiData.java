package cn.edu.cqupt.dmb.player.ts.data;


import androidx.annotation.NonNull;

import java.util.List;
import cn.edu.cqupt.dmb.player.ts.Track.AtscAudioTrack;
import cn.edu.cqupt.dmb.player.ts.Track.AtscCaptionTrack;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : Collection of MPEG PSI table items.
 * @Date : create by QingSong in 2022-04-18 16:57
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.ts
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.4
 */
public class PsiData {

    private PsiData() {
    }

    public static class PatItem {
        private final int mProgramNo;
        private final int mPmtPid;

        public PatItem(int programNo, int pmtPid) {
            mProgramNo = programNo;
            mPmtPid = pmtPid;
        }

        public int getProgramNo() {
            return mProgramNo;
        }

        public int getPmtPid() {
            return mPmtPid;
        }

        @NonNull
        @Override
        public String toString() {
            return String.format("Program No: %x PMT Pid: %x", mProgramNo, mPmtPid);
        }
    }

    public static class PmtItem {
        public static final int ES_PID_PCR = 0x100;

        private final int mStreamType;
        private final int mEsPid;
        private final List<AtscAudioTrack> mAudioTracks;
        private final List<AtscCaptionTrack> mCaptionTracks;

        public PmtItem(
                int streamType,
                int esPid,
                List<AtscAudioTrack> audioTracks,
                List<AtscCaptionTrack> captionTracks) {
            mStreamType = streamType;
            mEsPid = esPid;
            mAudioTracks = audioTracks;
            mCaptionTracks = captionTracks;
        }

        public int getStreamType() {
            return mStreamType;
        }

        public int getEsPid() {
            return mEsPid;
        }

        public List<AtscAudioTrack> getAudioTracks() {
            return mAudioTracks;
        }

        public List<AtscCaptionTrack> getCaptionTracks() {
            return mCaptionTracks;
        }

        @NonNull
        @Override
        public String toString() {
            return String.format(
                    "Stream Type: %x ES Pid: %x AudioTracks: %s CaptionTracks: %s",
                    mStreamType, mEsPid, mAudioTracks, mCaptionTracks);
        }
    }
}

