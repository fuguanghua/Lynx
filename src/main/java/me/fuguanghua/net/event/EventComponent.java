package me.fuguanghua.net.event;

import me.fuguanghua.net.Lynx;
import me.fuguanghua.net.components.Component;
import me.fuguanghua.net.dispatch.executor.AppEventExecutor;
import me.fuguanghua.net.event.annotation.EventReceive;
import me.fuguanghua.net.exception.CoreException;
import me.fuguanghua.net.router.annotation.Route;
import me.fuguanghua.net.utils.Constants;
import me.fuguanghua.net.utils.StringUtils;
import me.fuguanghua.net.utils.extend.ASMMethod;
import me.fuguanghua.net.utils.threads.NamedScheduleExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class EventComponent implements Component {
    private static Logger LOGGER = LoggerFactory.getLogger(EventComponent.class);

    Lynx lynx;

    private ConcurrentLinkedQueue<AppEvent> eventQueue = new ConcurrentLinkedQueue<>();

    private NamedScheduleExecutor eventExecutor;

    /**
     * key:eventName, value:{key:threadId, value:List<ASMMethod>}
     */
    private Map<String, Map<Integer, List<ASMMethod>>> eventInfoMaps = new LinkedHashMap<>();

    public EventComponent(Lynx lynx) {
        this.lynx = lynx;
    }

    @Override
    public String name() {
        return Constants.Component.EVENT;
    }

    @Override
    public void start() {
        this.eventExecutor = new NamedScheduleExecutor(1, "event-queue-thread");
        this.eventExecutor.scheduleWithFixedDelay(() -> {
            try {
                for (; ; ) {
                    AppEvent e = eventQueue.poll();
                    if (e == null) {
                        return;
                    }
                    publish(e);
                }
            } catch (Exception e) {
                LOGGER.error("scene queue thread error:{}", e);
            }
        }, 10, 1, TimeUnit.MILLISECONDS);
    }

    @Override
    public void afterStart() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void beforeStop() {
    }

    public void post(AppEvent event) {
        if (event != null) {
            this.eventQueue.add(event);
        }
    }

    private void publish(AppEvent event) {
        Map<Integer, List<ASMMethod>> maps = this.eventInfoMaps.get(event.name);
        if (maps == null) {
            LOGGER.warn("event={}, not found target", event.name);
            return;
        }

        try {
            int i = 0;
            for (int threadId : maps.keySet()) {
                if (i == 0) {
                    event.threadId = threadId;
                    lynx.dispatch().publish(new AppEventExecutor(lynx, event));
                } else {
                    AppEvent e = (AppEvent) event.clone();
                    e.threadId = threadId;
                    lynx.dispatch().publish(new AppEventExecutor(lynx, e));
                }
                i++;
            }
        } catch (Exception ex) {
            LOGGER.error("{}", ex);
        }
    }

    /**
     * event execute
     *
     * @param event
     */
    public void execute(AppEvent event) {
        Map<Integer, List<ASMMethod>> maps = this.eventInfoMaps.get(event.name);
        List<ASMMethod> list = maps.get(event.threadId);
        for (ASMMethod method : list) {
            try {
                method.invoke(event);
            } catch (Exception e) {
                LOGGER.error("event execute error. event={}", event.name);
                LOGGER.error("", e);
            }
        }
    }

    /**
     * load @EventReceive
     *
     * @param listener
     * @return
     */
    public void register(Object listener) {
        Class<?> clazz = listener.getClass();
        Method[] methods = clazz.getMethods();
        for (Method m : methods) {
            m.setAccessible(true);
            // 普通的事件注解
            EventReceive receive = m.getAnnotation(EventReceive.class);
            if (receive != null) {
                registerEvent(listener, clazz, m, receive);
            }
        }
    }

    private void registerEvent(Object listener, Class<?> clazz, Method method, EventReceive receive) {
        int threadId = receive.threadId();
        if (threadId < 1) {
            Route route = listener.getClass().getAnnotation(Route.class);
            if (route != null) {
                threadId = route.defaultThreadId();
            }
        }

        if (threadId < 1) {
            throw new CoreException(" class = {}, method = {}, no set threadId", listener.getClass(), method.getName());
        }

        for (String eventName : receive.name()) {
            if (StringUtils.isBlank(eventName)) {
                LOGGER.warn("event name is null! class = {}, method = {}", listener.getClass(), method.getName());
                continue;
            }
            addEventInfo(eventName, threadId, ASMMethod.valueOf(method, listener));
        }
    }

    private void addEventInfo(String eventName, int threadId, ASMMethod method) {
        Map<Integer, List<ASMMethod>> valueMaps = eventInfoMaps.getOrDefault(eventName, new HashMap<>());
        if (!eventInfoMaps.containsKey(eventName)) {
            eventInfoMaps.put(eventName, valueMaps);
        }

        List<ASMMethod> eventInfoList = valueMaps.getOrDefault(threadId, new ArrayList<>());
        if (!valueMaps.containsKey(threadId)) {
            valueMaps.put(threadId, eventInfoList);
        }
        eventInfoList.add(method);
    }

}
