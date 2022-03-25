package cn.edu.cqupt.dmb.player.domain;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : ChannelInfo 实体类
 * @Date : create by QingSong in 2022-03-15 13:31
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.domain
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
public class ChannelInfo {

    public static final String[] DATA_TYPE = {"stream audio", "stream data", "reserved", "packet data"};

    /**
     * Sub-channel organization,it can determine a channel,
     * 0: start address, 1: Sub-channel size (CU), 6: Bit rate (Kbit/s)
     */
    public int[] subChOrganization = new int[7];
    public int serviceId;
    public String label;
    /**
     * channel name
     */
    public boolean isSelect = false;
    public int transmissionMode = 0;
    public int type;
    public int subCh;

    @Override
    public String toString() {
        return "channel = " + subCh + "  type = " + DATA_TYPE[type] + "  label = " + label + "  " + "Bitrate = " +
                subChOrganization[6];
    }
}
