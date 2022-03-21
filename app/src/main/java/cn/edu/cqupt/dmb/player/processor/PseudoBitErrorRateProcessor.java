package cn.edu.cqupt.dmb.player.processor;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 伪误码率类型的数据处理器
 * @Date : create by QingSong in 2022-03-20 23:40
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.task
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class PseudoBitErrorRateProcessor implements DataProcessing {

    private volatile int ber;
    private volatile int bitRate;
    int bbReg0, bbReg3;

    @Override
    public void processData(byte[] usbData) {
        if (usbData[6] == 0) {
            bbReg0 = ((((int) usbData[8]) & 0x00ff) << 8) + ((int) usbData[9] & 0x00ff);
            bbReg3 = (((int) usbData[14] & 0x00FF) << 8) | (((int) usbData[15]) & 0x00FF);
            if (bbReg3 == 0) {
                ber = 2500;
            } else {
                ber = bbReg0 * 104 / (32 + bitRate);
            }
        }
    }
}
