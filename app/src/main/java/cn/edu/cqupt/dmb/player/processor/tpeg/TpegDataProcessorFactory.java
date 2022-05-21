package cn.edu.cqupt.dmb.player.processor.tpeg;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : TPEG数据处理器静态工厂
 * @Date : create by QingSong in 2022-05-21 13:29
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.processor.tpeg
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class TpegDataProcessorFactory {
    /**
     * 头帧
     */
    private final static TpegDataProcessor firstFrameTpegDataProcessor = new FirstFrameDataProcessor();
    /**
     * 中间帧
     */
    private final static TpegDataProcessor middleFrameTpegDataProcessor = new MiddleFrameDataProcessor();
    /**
     * 尾帧
     */
    private final static TpegDataProcessor lastFrameTpegDataProcessor = new LastFrameDataProcessor();
    /**
     * 默认的
     */
    private final static TpegDataProcessor defaultTpegDataProcessor = new DefaultTpegDataProcessor();

    /**
     * 根据 TPEG 数据类型获取 TPEG 数据处理器
     *
     * @param tpegType TPEG 数据类型
     * @return TpegDataProcessor
     */
    public static TpegDataProcessor getDataProcessor(int tpegType) {
        switch (tpegType) {
            case 2: {
                return firstFrameTpegDataProcessor;
            }
            case 1: {
                return middleFrameTpegDataProcessor;
            }
            case 3: {
                return lastFrameTpegDataProcessor;
            }
            default: {
                return defaultTpegDataProcessor;
            }
        }
    }
}
