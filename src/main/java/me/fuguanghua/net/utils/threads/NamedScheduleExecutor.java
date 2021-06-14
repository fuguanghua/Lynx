package me.fuguanghua.net.utils.threads;

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * 线程命名的执行器封装类
 */
public class NamedScheduleExecutor extends ScheduledThreadPoolExecutor {

	public NamedScheduleExecutor(int poolSize, final String name) {
		super(poolSize, new NamedThreadFactory(name));
	}

}
