package me.fuguanghua.net.event;

/**
 * 应用相关事件
 */
public abstract class AppEvent implements Cloneable {

    public int threadId;

    /**
     * 事件的key {@code EventKey}
     */
    public String name;

    public AppEvent() {
    }

    public AppEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    /**
     * DispatchType用于hash线程
     */
    public abstract long dispatchHash();

    @SuppressWarnings("unchecked")
    public <T> T convert() {
        return (T) this;
    }


    @Override
    public String toString() {
        return "Event [name=" + name + ", UniqueId=" + dispatchHash() + "]";
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
