package cn.edu.cqupt.dmb.player.domain;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 这个是自定义设置的数据库实体
 * @Date : create by QingSong in 2022-05-14 15:40
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.domain
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.0
 */
@Entity(tableName = "custom_setting")
public class CustomSetting {
    @PrimaryKey
    @ColumnInfo(name = "id")
    private Integer id;

    @ColumnInfo(name = "settingKey")
    private String settingKey;

    @ColumnInfo(name = "settingValue")
    private Long settingValue;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public Long getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(Long settingValue) {
        this.settingValue = settingValue;
    }

    @NonNull
    @Override
    public String toString() {
        return "CustomSetting{" +
                "id=" + id +
                ", settingKey='" + settingKey + '\'' +
                ", settingValue=" + settingValue +
                '}';
    }
}
