package cn.edu.cqupt.dmb.player.processor.tpeg;


/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : TPEG 数据处理器静态工厂,返回的数据处理器均采用单例模式
 * @Date : create by QingSong in 2022-03-22 21:55
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.processor.tpeg
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class TpegDataProcessingFactory {

    private static final int FIRST_FRAME = 2;
    private static final int MIDDLE_FRAME = 1;
    private static final int LAST_FRAME = 3;

    private static final TpegDataProcessing firstTpegFrameProcessor = new FirstTpegFrameProcessor();
    private static final TpegDataProcessing middleTpegFrameProcessor = new MiddleTpegFrameProcessor();
    private static final TpegDataProcessing lastTpegFrameProcessor = new LastTpegFrameProcessor();
    private static final TpegDataProcessing defaultTpegDataProcessing = new DefaultTpegDataProcessing();

    /**
     * 根据 TPEG 数据类型获取 TPEG 数据处理器
     *
     * @param tpegType TPEG 数据类型
     * @return 对应的 TPEG 数据处理器
     */
    public static TpegDataProcessing getDataProcessor(int tpegType) {
        switch (tpegType) {
            case FIRST_FRAME:
                return firstTpegFrameProcessor;
            case MIDDLE_FRAME:
                return middleTpegFrameProcessor;
            case LAST_FRAME:
                return lastTpegFrameProcessor;
            default:
                return defaultTpegDataProcessing;
        }
    }
}
