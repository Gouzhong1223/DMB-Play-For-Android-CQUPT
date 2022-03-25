package cn.edu.cqupt.dmb.player.old;

import android.util.Log;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import cn.edu.cqupt.dmb.player.decoder.FicDecoder;
import cn.edu.cqupt.dmb.player.domain.ChannelInfo;
import cn.edu.cqupt.dmb.player.domain.Dangle;
import cn.edu.cqupt.dmb.player.utils.DataReadWriteUtil;

public class DangleReader extends Thread {

    private static final String TAG = "DangleReader";

    /**
     * data packet size
     */
    public static final int PACKET_SIZE = 64;
    /**
     * because the read packet is small than buffer size, in order to improve performance,
     * each time will read  a number of packet
     */
    public static final int READ_TIME = 15;

    private final Dangle mDangle;
    private final PipedOutputStream mOutputStream;
    private int mBer;
    private final FicDecoder mFicDecoder;
    private int mBitRate;
    private boolean mIsSelectId;
    private ChannelInfo mChannelInfo;


    public DangleReader(Dangle dangle, PipedInputStream pipedInputStream, int id, boolean isEncrypted) {
        mDangle = dangle;
        mOutputStream = new PipedOutputStream();
        try {
            mOutputStream.connect(pipedInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mFicDecoder = FicDecoder.getInstance(id, isEncrypted);
    }


    @Override
    public void run() {
        byte[] readBuf = new byte[PACKET_SIZE * READ_TIME];
        byte[] packetBuf = new byte[PACKET_SIZE];
        byte[] ficBuf = new byte[32];
        int dataLength;
        int len, bbReg0, bbReg3;
        Log.e(TAG, "DangleReader start");
        mIsSelectId = false;
        while (DataReadWriteUtil.USB_READY) {
            len = mDangle.read(readBuf);
            if (len != readBuf.length) {
                Log.i(TAG, "read error");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            for (int i = 0; i < READ_TIME; i++) {
                System.arraycopy(readBuf, i * PACKET_SIZE, packetBuf, 0, PACKET_SIZE);
                switch (packetBuf[3]) {
                    case 0x00: // Receive Baseband register values
                        if (packetBuf[6] == 0) {
                            bbReg0 = ((((int) packetBuf[8]) & 0x00ff) << 8) + ((int) packetBuf[9] & 0x00ff);
                            bbReg3 = (((int) packetBuf[14] & 0x00FF) << 8) | (((int) packetBuf[15]) & 0x00FF);
                            if (bbReg3 == 0) {
                                mBer = 2500;
                            } else {
                                mBer = bbReg0 * 104 / (32 + mBitRate);
                            }
                        }
                        break;
                    case 0x02:
                        continue;
                    case 0x03: /* Receive DMB or DAB data */
                        dataLength = (((int) packetBuf[7]) & 0x0FF);
                        try {
                            mOutputStream.write(packetBuf, 8, dataLength);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 0x04: /* FIC data */
                    case 0x05:
                    case 0x06:
                    case 0x07:
                        //Log.e(TAG,"receive fic data");
                        System.arraycopy(packetBuf, 8, ficBuf, 0, 32);
                        mFicDecoder.decode(ficBuf);
                        if (!mIsSelectId && (mChannelInfo = mFicDecoder.getSelectChannelInfo()) != null) {
                            new Thread(() -> {
                                mIsSelectId = mDangle.SetChannel(mChannelInfo);
                                Log.e(TAG, mChannelInfo.toString());
                            }).start();
                            mBitRate = mChannelInfo.subChOrganization[6];
                            mIsSelectId = true;
                        }
                        break;
                    case 0x09:
                        Log.e(TAG, "set frequency successful");
                        break;
                    default:
                        Log.e(TAG, "unknown fic info");
                        break;
                }
            }
        }
        Log.e(TAG, "DangleReader end");
    }

    public int getBer() {
        return mBer;
    }
}
