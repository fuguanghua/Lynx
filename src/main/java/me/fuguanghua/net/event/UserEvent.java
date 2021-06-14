package me.fuguanghua.net.event;

public abstract class UserEvent extends AppEvent {

	/**
	 * uid
	 */
	private long uid;

	//ThreadId.ACTOR
	public UserEvent(String name, long uid) {
		super(name);
		this.uid = uid;
	}

	@Override
	public long dispatchHash() {
		return uid;
	}

	public long getUid() {
		return uid;
	}

}
