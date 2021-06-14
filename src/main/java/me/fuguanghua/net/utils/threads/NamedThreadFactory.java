package me.fuguanghua.net.utils.threads;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程工厂封装扩展类
 */
public class NamedThreadFactory implements ThreadFactory {
	final ThreadGroup group;
	final AtomicInteger threadNumber = new AtomicInteger(1);
	final String namePrefix;

	public NamedThreadFactory(String name) {
		this.group = new ThreadGroup(name);
		this.namePrefix = (group.getName() + "-");
	}

	public Thread newThread(Runnable r) {
		return new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
	}

	public Thread newThread(Runnable r, String title) {
		return new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement() + title, 0L);
	}
}