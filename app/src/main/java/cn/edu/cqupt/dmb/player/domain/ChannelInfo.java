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
    private int serviceId;
    private String label;
    /**
     * channel name
     */
    private boolean isSelect = false;
    private int transmissionMode = 0;
    private int type;
    private int subCh;

    @Override
    public String toString() {
        return "channel = " + subCh + "  type = " + DATA_TYPE[type] + "  label = " + label + "  " + "Bitrate = " +
                subChOrganization[6];
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public int getTransmissionMode() {
        return transmissionMode;
    }

    public void setTransmissionMode(int transmissionMode) {
        this.transmissionMode = transmissionMode;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSubCh() {
        return subCh;
    }

    public void setSubCh(int subCh) {
        this.subCh = subCh;
    }

    public ChannelInfo(int[] subChOrganization, int serviceId, String label, boolean isSelect, int transmissionMode, int type, int subCh) {
        this.subChOrganization = subChOrganization;
        this.serviceId = serviceId;
        this.label = label;
        this.isSelect = isSelect;
        this.transmissionMode = transmissionMode;
        this.type = type;
        this.subCh = subCh;
    }

    public ChannelInfo() {
    }
}
