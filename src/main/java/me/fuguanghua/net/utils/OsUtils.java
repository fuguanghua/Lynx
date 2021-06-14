package me.fuguanghua.net.utils;

import java.util.Properties;

public class OsUtils {

    private static String getOsName() {
        Properties prop = System.getProperties();
        String os = prop.getProperty("os.name");
        return os.toLowerCase();
    }

    public static boolean isWindows() {
        String osName = getOsName();
        if (StringUtils.isBlank(osName)) {
            return false;
        }
        osName = osName.toLowerCase();
        return osName.indexOf("windows") > -1;
    }

    public static boolean isLinux() {
        String osName = getOsName();
        if (StringUtils.isBlank(osName)) {
            return false;
        }
        osName = osName.toLowerCase();
        return osName.indexOf("linux") > -1;
    }

    public static OS getOs() {
        String osName = getOsName();
        if (StringUtils.isBlank(osName)) {
            return OS.NOT_SUPPORT;
        }
        if (osName.indexOf("windows") > -1) {
            return OS.WINDOWS;
        }
        if (osName.indexOf("linux") > -1) {
            return OS.LINUX;
        }
        return OS.NOT_SUPPORT;
    }

    public enum OS {
        NOT_SUPPORT, WINDOWS, LINUX
    }
}
