package cn.edu.cqupt.dmb.player.domain;


import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.util.Log;

import cn.edu.cqupt.dmb.player.common.DmbPlayerConstant;
import cn.edu.cqupt.dmb.player.task.SendDataToUsbTask;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : Dangle 交互类
 * @Date : create by QingSong in 2022-03-15 14:08
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.domain
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class Dangle {


    private static final String TAG = "Dangle";

    /**
     * 写入USB数据的Endpoint
     */
    private final UsbEndpoint usbEndpointOut;

    /**
     * 已经打开的USB连接
     */
    private final UsbDeviceConnection usbDeviceConnection;

    public Dangle(UsbEndpoint usbEndpointOut, UsbDeviceConnection usbDeviceConnection) {
        this.usbEndpointOut = usbEndpointOut;
        this.usbDeviceConnection = usbDeviceConnection;
    }

    /**
     * 清空Dangle寄存器，初始化Dangle，在设置频点和节目之前，需要先执行初始化操作
     */
    public void clearRegister() {
        boolean ret1;
        boolean ret2;
        boolean ret3;
        boolean ret4;
        byte[] clearBBChReg = {
                (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 32,
                (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x21, (byte) 0xff, (byte) 0xff,
                (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x23, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x24, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x25, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x26, (byte) 0x00, (byte) 0x02,
                (byte) 0x00, (byte) 0x2c, (byte) 0x00, (byte) 0x10
        };
        Integer call3 = new SendDataToUsbTask(clearBBChReg, usbEndpointOut, usbDeviceConnection).call();
        ret1 = call3 == clearBBChReg.length;

        byte[] clearBBFicData1 = {
                (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 32,
                (byte) 0x40, (byte) 0x00, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x40, (byte) 0x10, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x40, (byte) 0x20, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x40, (byte) 0x30, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x40, (byte) 0x40, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x40, (byte) 0x50, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x40, (byte) 0x60, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x40, (byte) 0x70, (byte) 0xFF, (byte) 0xFF
        };
        Integer call2 = new SendDataToUsbTask(clearBBFicData1, usbEndpointOut, usbDeviceConnection).call();
        ret2 = call2 == clearBBFicData1.length;

        byte[] clearBBFicData2 = {
                (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 32,
                (byte) 0x40, (byte) 0x80, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x40, (byte) 0x90, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x40, (byte) 0xA0, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x40, (byte) 0xB0, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x40, (byte) 0xC0, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x00, (byte) 0x07, (byte) 0x00, (byte) 0x03,
                (byte) 0x71, (byte) 0x90, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x07, (byte) 0x00, (byte) 0x00
        };
        Integer call1 = new SendDataToUsbTask(clearBBFicData2, usbEndpointOut, usbDeviceConnection).call();
        ret3 = call1 == clearBBFicData2.length;

        byte[] clearMcuReg = {
                (byte) 0xFF, (byte) 0xFF, (byte) 0x08, (byte) 0x06,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        Integer call = new SendDataToUsbTask(clearMcuReg, usbEndpointOut, usbDeviceConnection).call();

        ret4 = call == clearMcuReg.length;

        if (ret1 && ret2 && ret3 && ret4) {
            Log.i(TAG, "清除 DMB 设置成功!");
            return;
        }
        Log.e(TAG, "清除 DMB 设置失败!");
    }

    /**
     * 设置频道
     *
     * @param channelInfo 可由Fic解码得到
     * @return isSelectId  如果设置成功,就直接返回true,反之就返回false
     */
    public boolean SetChannel(ChannelInfo channelInfo) {
        byte[] mcu_cmd = new byte[48];
        byte[] bb_cmd = new byte[48];
        byte temp = 0;
        int div, rem1, rem2;
        int nStartCu;
        int br = channelInfo.subChOrganization[6];
        switch (channelInfo.transmissionMode) {
            case 0:
                div = 48;
                rem1 = 4;
                rem2 = 5;
                break;
            case 1:
                div = 12;
                rem1 = 4;
                rem2 = 5;
                break;
            case 2:
                div = 6;
                rem1 = 9;
                rem2 = 10;
                break;
            case 3:
                div = 24;
                rem1 = 4;
                rem2 = 5;
                break;
            default:
                div = 48;
                rem1 = 4;
                rem2 = 5;
                break;
        }

        mcu_cmd[0] = (byte) 0xff;
        bb_cmd[0] = (byte) 0xff;
        mcu_cmd[1] = (byte) 0xff;
        bb_cmd[1] = (byte) 0xff;
        mcu_cmd[2] = 0x08;
        bb_cmd[2] = 0x00;
        mcu_cmd[3] = 0x06;
        bb_cmd[3] = 28;
        mcu_cmd[4] = 0x00;
        bb_cmd[4] = (byte) 0x80;
        mcu_cmd[5] = (byte) ((br >>> 8) & 0x00ff);
        bb_cmd[5] = 0x20;
        mcu_cmd[6] = (byte) (br & 0x00ff);
        bb_cmd[6] = (byte) ((channelInfo.subChOrganization[0] >>> 8) & 0x00ff);
        mcu_cmd[7] = 0x01;
        bb_cmd[7] = (byte) (channelInfo.subChOrganization[0] & 0x00ff);
        mcu_cmd[8] = 0x00;
        bb_cmd[8] = (byte) 0x80;
        mcu_cmd[9] = 0x00;
        bb_cmd[9] = 0x21;

        nStartCu = (channelInfo.subChOrganization[0] & 0x03ff); /* start capacity unit address */
        mcu_cmd[10] = 0x00;
        bb_cmd[10] = (byte) ((nStartCu) / div + rem1);
        temp = (byte) ((nStartCu + channelInfo.subChOrganization[1] - 1) / div + rem2);
        if (((temp == (byte) 22) && (channelInfo.transmissionMode == 0)) ||
                ((temp == (byte) 76) && (channelInfo.transmissionMode == 1)) ||
                ((temp == (byte) 153) && (channelInfo.transmissionMode == 2)) ||
                ((temp == (byte) 40) && (channelInfo.transmissionMode == 3))) {
            mcu_cmd[9] = 0x01;
        }
        mcu_cmd[11] = 0x00;
        bb_cmd[11] = temp;
        mcu_cmd[12] = 0x00;
        bb_cmd[12] = (byte) 0x80;
        mcu_cmd[13] = 0x00;
        bb_cmd[13] = 0x22;
        mcu_cmd[14] = 0x00;
        bb_cmd[14] = (byte) ((channelInfo.subChOrganization[2] >>> 8) & 0x00ff);
        mcu_cmd[15] = 0x00;
        bb_cmd[15] = (byte) (channelInfo.subChOrganization[2] & 0x00ff);
        mcu_cmd[16] = 0x00;
        bb_cmd[16] = (byte) 0x80;
        mcu_cmd[17] = 0x00;
        bb_cmd[17] = 0x23;
        mcu_cmd[18] = 0x00;
        bb_cmd[18] = (byte) ((channelInfo.subChOrganization[3] >>> 8) & 0x00ff);
        mcu_cmd[19] = 0x00;
        bb_cmd[19] = (byte) (channelInfo.subChOrganization[3] & 0x00ff);
        mcu_cmd[20] = 0x00;
        bb_cmd[20] = (byte) 0x80;
        mcu_cmd[21] = 0x00;
        bb_cmd[21] = 0x24;
        mcu_cmd[22] = 0x00;
        bb_cmd[22] = (byte) ((channelInfo.subChOrganization[4] >>> 8) & 0x00ff);
        mcu_cmd[23] = 0x00;
        bb_cmd[23] = (byte) (channelInfo.subChOrganization[4] & 0x00ff);
        mcu_cmd[24] = 0x00;
        bb_cmd[24] = (byte) 0x80;
        mcu_cmd[25] = 0x00;
        bb_cmd[25] = 0x25;
        mcu_cmd[26] = 0x00;
        bb_cmd[26] = (byte) ((channelInfo.subChOrganization[5] >>> 8) & 0x00ff);
        mcu_cmd[27] = 0x00;
        bb_cmd[27] = (byte) (channelInfo.subChOrganization[5] & 0x00ff);
        mcu_cmd[28] = 0x00;
        bb_cmd[28] = (byte) 0x80;
        mcu_cmd[29] = 0x00;
        bb_cmd[29] = 0x26;
        mcu_cmd[30] = 0x00;
        bb_cmd[30] = (byte) 0xff;
        mcu_cmd[31] = 0x00;
        bb_cmd[31] = 0x02;

        boolean ret1;
        Integer call1 = new SendDataToUsbTask(bb_cmd, usbEndpointOut, usbDeviceConnection).call();
        ret1 = call1 == bb_cmd.length;

        boolean ret2;
        Integer call = new SendDataToUsbTask(mcu_cmd, usbEndpointOut, usbDeviceConnection).call();
        ret2 = call == bb_cmd.length;
        if (ret1 && ret2) {
            return true;
        }
        Log.e(TAG, "set channel fail");
        return false;
    }

    public void setFrequency(int frequency) {
        byte[] cmd = new byte[48];
        cmd[0] = (byte) 0xff;
        cmd[1] = (byte) 0xff;
        cmd[2] = (byte) 0x01;
        cmd[3] = (byte) 0x2c;
        cmd[4] = (byte) 0xc0;
        cmd[5] = 1;
        cmd[6] = 1;
        cmd[7] = 0;
        cmd[8] = 0;
        cmd[9] = 0;
        cmd[10] = 0;
        cmd[11] = 4;
        cmd[12] = (byte) ((frequency >> 24) & 0xff);
        cmd[13] = (byte) ((frequency >> 16) & 0xff);
        cmd[14] = (byte) ((frequency >> 8) & 0xff);
        cmd[15] = (byte) ((frequency) & 0xff);
        int ret;
        ret = new SendDataToUsbTask(cmd, usbEndpointOut, usbDeviceConnection).call();
        if (ret == cmd.length) {
            Log.i(TAG, "设置频点成功:" + frequency);
            return;
        }
        Log.e(TAG, "set frequency fail");
    }
}
