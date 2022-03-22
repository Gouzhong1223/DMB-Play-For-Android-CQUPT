package cn.edu.cqupt.dmb.player.processor.dmb;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 获取 DMB 数据处理器的静态工厂类,返回的数据处理器均采用单例模式
 * @Date : create by QingSong in 2022-03-20 23:53
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.task
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class DataProcessingFactory {

    private final static DataProcessing dmbDataProcessor = new DmbDataProcessor();
    private final static DataProcessing ficDataProcessor = new FicDataProcessor();
    private final static DataProcessing frequencySetSuccessProcessor = new FrequencySetSuccessProcessor();
    private final static DataProcessing pseudoBitErrorRateProcessor = new PseudoBitErrorRateProcessor();
    private final static DataProcessing defaultDataProcessor = new DefaultDataProcessor();

    /**
     * 根据接收到的 USB 数据类型获取对应的数据处理器
     *
     * @param dataType USB 数据类型
     * @return 对应的数据处理器
     */
    public static DataProcessing getDataProcessor(int dataType) {
        switch (dataType) {
            case 0x00:
                return pseudoBitErrorRateProcessor;
            case 0x03:
                return dmbDataProcessor;
            case 0x04:
            case 0x05:
            case 0x06:
            case 0x07:
                return ficDataProcessor;
            case 0x09:
                return frequencySetSuccessProcessor;
            default:
                return defaultDataProcessor;
        }
    }
}
