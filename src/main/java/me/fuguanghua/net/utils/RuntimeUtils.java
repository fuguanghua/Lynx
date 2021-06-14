package me.fuguanghua.net.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class RuntimeUtils {
	
	public static Long pid() {
		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		String name = runtimeMXBean.getName();
		return Long.valueOf(StringUtils.split(name, "@")[0]);
	}
}
