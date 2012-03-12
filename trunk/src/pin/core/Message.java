package pin.core;

public class Message {
	/**
	 * User-defined message code so that the recipient can identify what this
	 * message is about. Each {@link Handler} has its own name-space for message
	 * codes, so you do not need to worry about yours conflicting with other
	 * handlers.
	 */
	public int what;

	/**
	 * arg1 and arg2 are lower-cost alternatives to using
	 * {@link #setData(Bundle) setData()} if you only need to store a few
	 * integer values.
	 */
	public int arg1;

	/**
	 * arg1 and arg2 are lower-cost alternatives to using
	 * {@link #setData(Bundle) setData()} if you only need to store a few
	 * integer values.
	 */
	public int arg2;

	/**
	 * An arbitrary object to send to the recipient. When using
	 * {@link Messenger} to send the message across processes this can only be
	 * non-null if it contains a Parcelable of a framework class (not one
	 * implemented by the application). For other data transfer use
	 * {@link #setData}.
	 * 
	 * <p>
	 * Note that Parcelable objects here are not supported prior to the
	 * {@link android.os.Build.VERSION_CODES#FROYO} release.
	 */
	public Object obj;

	long when;

	Handler target;

	Runnable callback;

	Message next;

	private static final byte[] sPoolSync = new byte[0];
	private static Message sPool;
	private static int sPoolSize = 0;

	private static final int MAX_POOL_SIZE = 50;

	private Message() {

	}

	/**
	 * Return a new Message instance from the global pool. Allows us to avoid
	 * allocating new objects in many cases.
	 */
	public static Message obtain() {
		synchronized (sPoolSync) {
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

		synchronized (sPoolSync) {
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
	 */
	public void copyFrom(Message o) {
		this.what = o.what;
		this.arg1 = o.arg1;
		this.arg2 = o.arg2;
		this.obj = o.obj;
	}

	/**
	 * Return the targeted delivery time of this message, in milliseconds.
	 */
	public long getWhen() {
		return when;
	}

	public void setTarget(Handler target) {
		this.target = target;
	}

	/**
	 * Retrieve the a {@link android.os.Handler Handler} implementation that
	 * will receive this message. The object must implement
	 * {@link android.os.Handler#handleMessage(android.os.Message)
	 * Handler.handleMessage()}. Each Handler has its own name-space for message
	 * codes, so you do not need to worry about yours conflicting with other
	 * handlers.
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
	 */
	public Runnable getCallback() {
		return callback;
	}
	
    /**
     * Sends this Message to the Handler specified by {@link #getTarget}.
     * Throws a null pointer exception if this field has not been set.
     */
	public void sendToTarget() {
        target.sendMessage(this);
    }

	void clearForRecycle() {
		what = 0;
		arg1 = 0;
		arg2 = 0;
		obj = null;
		when = 0;
		target = null;
		callback = null;
	}

	public String toString() {
		StringBuilder b = new StringBuilder();

		b.append("{ what=");
		b.append(what);

		b.append(" when=");
		b.append(when);

		b.append(" now=");
		b.append(System.currentTimeMillis());
		
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

		b.append(" }");

		return b.toString();
	}
}
