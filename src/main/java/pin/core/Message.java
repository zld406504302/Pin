package pin.core;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public final class Message implements Delayed {
	/**
	 * User-defined message code so that the recipient can identify what this
	 * message is about. Each {@link Handler} has its own name-space for message
	 * codes, so you do not need to worry about yours conflicting with other
	 * handlers.
	 */
	public int what;
	
	/**
	 * An arbitrary integer to send to the recipient. 
	 */
	public int arg1;

	/**
	 * An arbitrary integer to send to the recipient. 
	 */
	public int arg2;

	/**
	 * An arbitrary object to send to the recipient. 
	 */
	public Object obj;
	
	/** The time the task is added to message queue */
	long when;
	
	/** The time the task is enabled to execute in nanoTime units */
	public long time;
	
	/**
	 * Period in nanoseconds for repeating messages. A positive value indicates
	 * fixed-rate execution. A negative value indicates fixed-delay
	 * execution. A value of 0 indicates a non-repeating task.
	 */
	public long period;

	Handler target;

	Runnable callback;

	Message next;
	
	/**
	 * Index into delay queue, to support faster cancellation.
	 */
	int heapIndex;

	/**
	 * 用来加锁的变量
	 */
	private static final byte[] SPOOLSYNC = new byte[0];
	private static Message sPool;
	private static int sPoolSize = 0;

	private static final int MAX_POOL_SIZE = 50;

	/**
	 * 私有构造函数
	 */
	private Message() {

	}

	/**
	 * Return a new Message instance from the global pool. Allows us to avoid
	 * allocating new objects in many cases.
	 * 
	 * @return {@link Message}
	 */
	public static Message obtain() {
		synchronized (SPOOLSYNC) {
			if (sPool != null) {
				Message m = sPool;
				sPool = m.next;
				m.next = null;
				sPoolSize--;
				return m;
			}
		}
		return new Message();
	}

	/**
	 * Same as {@link #obtain()}, but copies the values of an existing message
	 * (including its target) into the new one.
	 * 
	 * @param orig
	 *            Original message to copy.
	 * @return A Message object from the global pool.
	 */
	public static Message obtain(Message orig) {
		Message m = obtain();
		m.what = orig.what;
		m.arg1 = orig.arg1;
		m.arg2 = orig.arg2;
		m.obj = orig.obj;
		m.target = orig.target;
		m.callback = orig.callback;

		return m;
	}

	/**
	 * Same as {@link #obtain()}, but sets the value for the <em>target</em>
	 * member on the Message returned.
	 * 
	 * @param h
	 *            Handler to assign to the returned Message object's
	 *            <em>target</em> member.
	 * @return A Message object from the global pool.
	 */
	public static Message obtain(Handler h) {
		Message m = obtain();
		m.target = h;

		return m;
	}

	/**
	 * Same as {@link #obtain(Handler)}, but assigns a callback Runnable on the
	 * Message that is returned.
	 * 
	 * @param h
	 *            Handler to assign to the returned Message object's
	 *            <em>target</em> member.
	 * @param callback
	 *            Runnable that will execute when the message is handled.
	 * @return A Message object from the global pool.
	 */
	public static Message obtain(Handler h, Runnable callback) {
		Message m = obtain();
		m.target = h;
		m.callback = callback;

		return m;
	}

	/**
	 * Same as {@link #obtain()}, but sets the values for both <em>target</em>
	 * and <em>what</em> members on the Message.
	 * 
	 * @param h
	 *            Value to assign to the <em>target</em> member.
	 * @param what
	 *            Value to assign to the <em>what</em> member.
	 * @return A Message object from the global pool.
	 */
	public static Message obtain(Handler h, int what) {
		Message m = obtain();
		m.target = h;
		m.what = what;

		return m;
	}

	/**
	 * Same as {@link #obtain()}, but sets the values of the <em>target</em>,
	 * <em>what</em>, and <em>obj</em> members.
	 * 
	 * @param h
	 *            The <em>target</em> value to set.
	 * @param what
	 *            The <em>what</em> value to set.
	 * @param obj
	 *            The <em>object</em> method to set.
	 * @return A Message object from the global pool.
	 */
	public static Message obtain(Handler h, int what, Object obj) {
		Message m = obtain();
		m.target = h;
		m.what = what;
		m.obj = obj;

		return m;
	}

	/**
	 * Same as {@link #obtain()}, but sets the values of the <em>target</em>,
	 * <em>what</em>, <em>arg1</em>, and <em>arg2</em> members.
	 * 
	 * @param h
	 *            The <em>target</em> value to set.
	 * @param what
	 *            The <em>what</em> value to set.
	 * @param arg1
	 *            The <em>arg1</em> value to set.
	 * @param arg2
	 *            The <em>arg2</em> value to set.
	 * @return A Message object from the global pool.
	 */
	public static Message obtain(Handler h, int what, int arg1, int arg2) {
		Message m = obtain();
		m.target = h;
		m.what = what;
		m.arg1 = arg1;
		m.arg2 = arg2;

		return m;
	}

	/**
	 * Same as {@link #obtain()}, but sets the values of the <em>target</em>,
	 * <em>what</em>, <em>arg1</em>, <em>arg2</em>, and <em>obj</em> members.
	 * 
	 * @param h
	 *            The <em>target</em> value to set.
	 * @param what
	 *            The <em>what</em> value to set.
	 * @param arg1
	 *            The <em>arg1</em> value to set.
	 * @param arg2
	 *            The <em>arg2</em> value to set.
	 * @param obj
	 *            The <em>obj</em> value to set.
	 * @return A Message object from the global pool.
	 */
	public static Message obtain(Handler h, int what, int arg1, int arg2, Object obj) {
		Message m = obtain();
		m.target = h;
		m.what = what;
		m.arg1 = arg1;
		m.arg2 = arg2;
		m.obj = obj;

		return m;
	}

	/**
	 * Return a Message instance to the global pool. You MUST NOT touch the
	 * Message after calling this function -- it has effectively been freed.
	 */
	public void recycle() {
		clearForRecycle();

		synchronized (SPOOLSYNC) {
			if (sPoolSize < MAX_POOL_SIZE) {
				next = sPool;
				sPool = this;
				sPoolSize++;
			}
		}
	}

	/**
	 * Make this message like o. Performs a shallow copy of the data field. Does
	 * not copy the linked list fields, nor the timestamp or target/callback of
	 * the original message.
	 * 
	 * @param o
	 *            {@link Message}
	 */
	public void copyFrom(Message o) {
		this.what = o.what;
		this.arg1 = o.arg1;
		this.arg2 = o.arg2;
		this.obj = o.obj;
	}

	/**
	 * Return the targeted delivery time of this message, in milliseconds.
	 * 
	 * @return the targeted delivery time of this message
	 */
	public long getWhen() {
		return when;
	}

	/**
	 * 设置要发送到的{@link Handler}
	 * 
	 * @param target
	 *            {@link Handler}
	 */
	public void setTarget(Handler target) {
		this.target = target;
	}

	/**
	 * Retrieve the a {@link pin.core.Handler Handler} implementation that will
	 * receive this message. The object must implement
	 * {@link pin.core.Handler#handleMessage(pin.core.Message)
	 * Handler.handleMessage()}. Each Handler has its own name-space for message
	 * codes, so you do not need to worry about yours conflicting with other
	 * handlers.
	 * 
	 * @return {@link Handler}
	 */
	public Handler getTarget() {
		return target;
	}

	/**
	 * Retrieve callback object that will execute when this message is handled.
	 * This object must implement Runnable. This is called by the
	 * <em>target</em> {@link Handler} that is receiving this Message to
	 * dispatch it. If not set, the message will be dispatched to the receiving
	 * Handler's {@link Handler#handleMessage(Message Handler.handleMessage())}.
	 * 
	 * @return callback object
	 */
	public Runnable getCallback() {
		return callback;
	}

	/**
	 * Sends this Message to the Handler specified by {@link #getTarget}. Throws
	 * a null pointer exception if this field has not been set.
	 */
	public void sendToTarget() {
		target.sendMessage(this);
	}

	/**
	 * 回收之前对消息进行清理
	 */
	void clearForRecycle() {
		what = 0;
		arg1 = 0;
		arg2 = 0;
		obj = null;
		when = 0;
		time = 0;
		period = 0;
		target = null;
		callback = null;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();

		b.append("{ what=");
		b.append(what);

		if (arg1 != 0) {
			b.append(" arg1=");
			b.append(arg1);
		}

		if (arg2 != 0) {
			b.append(" arg2=");
			b.append(arg2);
		}

		if (obj != null) {
			b.append(" obj=");
			b.append(obj);
		}
		
		b.append(" when=");
		b.append(when);
		
		b.append(" time=");
		b.append(time);
		
		b.append(" period=");
		b.append(period);

		b.append(" now=");
		b.append(System.nanoTime());

		b.append(" }");

		return b.toString();
	}

	@Override
	public int compareTo(Delayed other) {
		if (other == this) {
			return 0;
		}
		if (other instanceof Message) {
			Message x = (Message) other;
			long diff = time - x.time;
			if (diff < 0) {
				return -1;
			} else if (diff > 0) {
				return 1;
//			} else if (sequenceNumber < x.sequenceNumber) {
//				return -1;
			} else {
				return 1;
			}
		}
		long d = (getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS));
		return (d == 0) ? 0 : ((d < 0) ? -1 : 1);
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(time - System.nanoTime(), TimeUnit.NANOSECONDS);
	}
	
	/**
	 * Returns true if this is a periodic (not a one-shot) action.
	 * 
	 * @return true if periodic
	 */
	public boolean isPeriodic() {
		return period != 0;
	}
	
}
