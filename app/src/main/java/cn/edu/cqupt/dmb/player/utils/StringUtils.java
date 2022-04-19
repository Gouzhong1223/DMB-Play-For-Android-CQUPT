package cn.edu.cqupt.dmb.player.utils;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : Utility class for handling {@link String}.
 * @Date : create by QingSong in 2022-04-18 18:48
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.utils
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.4
 */
public final class StringUtils {

    private StringUtils() {
    }

    /**
     * Returns compares two strings lexicographically and handles null values quietly.
     */
    public static int compare(String a, String b) {
        if (a == null) {
            return b == null ? 0 : -1;
        }
        if (b == null) {
            return 1;
        }
        return a.compareTo(b);
    }

    /**
     * Returns {@code s} or {@code ""} if {@code s} is {@code null}
     */
    public static final String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
