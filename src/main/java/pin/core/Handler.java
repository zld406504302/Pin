package pin.core;

import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Handler {

	private static final String TAG = "Handler";
	private static final Logger logger = LoggerFactory.getLogger(Handler.class);
	private final BlockingQueue<Message> mQueue;
	private final Looper mLooper;
	private final Callback mCallback;

	/**
	 * Callback interface you can use when instantiating a Handler to avoid
	 * having to implement your own subclass of Handler.
	 */
	public interface Callback {
		public boolean handleMessage(Message msg);
	}

	/**
	 * Subclasses must implement this to receive messages.
	 */
	public abstract void handleMessage(Message msg);

	/**
	 * Handle system messages here.
	 */
	public void dispatchMessage(Message msg) {
		if (msg.callback != null) {
			handleCallback(msg);
		} else {
			if (mCallback != null) {
				if (mCallback.handleMessage(msg)) {
					return;
				}
			}
			handleMessage(msg);
		}
	}

	/**
	 * Default constructor associates this handler with the queue for the
	 * current thread.
	 * 
	 * If there isn't one, this handler won't be able to receive messages.
	 */
	public Handler() {
		mLooper = Looper.myLooper();
		if (mLooper == null) {
			throw new RuntimeException("Can't create handler inside thread that has not called Looper.prepare()");
		}
		mQueue = mLooper.mQueue;
		mCallback = null;
	}

	/**
	 * Constructor associates this handler with the queue for the current thread
	 * and takes a callback interface in which you can handle messages.
	 */
	public Handler(Callback callback) {
		mLooper = Looper.myLooper();
		if (mLooper == null) {
			throw new RuntimeException("Can't create handler inside thread that has not called Looper.prepare()");
		}
		mQueue = mLooper.mQueue;
		mCallback = callback;
	}

	/**
	 * Use the provided queue instead of the default one.
	 */
	public Handler(Looper looper) {
		mLooper = looper;
		mQueue = looper.mQueue;
		mCallback = null;
	}

	/**
	 * Use the provided queue instead of the default one and take a callback
	 * interface in which to handle messages.
	 */
	public Handler(Looper looper, Callback callback) {
		mLooper = looper;
		mQueue = looper.mQueue;
		mCallback = callback;
	}

	/**
	 * Returns a new {@link android.os.Message Message} from the global message
	 * pool. More efficient than creating and allocating new instances. The
	 * retrieved message has its handler set to this instance (Message.target ==
	 * this). If you don't want that facility, just call Message.obtain()
	 * instead.
	 */
	public final Message obtainMessage() {
		return Message.obtain(this);
	}

	/**
	 * Same as {@link #obtainMessage()}, except that it also sets the what
	 * member of the returned Message.
	 * 
	 * @param what
	 *            Value to assign to the returned Message.what field.
	 * @return A Message from the global message pool.
	 */
	public final Message obtainMessage(int what) {
		return Message.obtain(this, what);
	}

	/**
	 * 
	 * Same as {@link #obtainMessage()}, except that it also sets the what and
	 * obj members of the returned Message.
	 * 
	 * @param what
	 *            Value to assign to the returned Message.what field.
	 * @param obj
	 *            Value to assign to the returned Message.obj field.
	 * @return A Message from the global message pool.
	 */
	public final Message obtainMessage(int what, Object obj) {
		return Message.obtain(this, what, obj);
	}

	/**
	 * 
	 * Same as {@link #obtainMessage()}, except that it also sets the what, arg1
	 * and arg2 members of the returned Message.
	 * 
	 * @param what
	 *            Value to assign to the returned Message.what field.
	 * @param arg1
	 *            Value to assign to the returned Message.arg1 field.
	 * @param arg2
	 *            Value to assign to the returned Message.arg2 field.
	 * @return A Message from the global message pool.
	 */
	public final Message obtainMessage(int what, int arg1, int arg2) {
		return Message.obtain(this, what, arg1, arg2);
	}

	/**
	 * 
	 * Same as {@link #obtainMessage()}, except that it also sets the what, obj,
	 * arg1,and arg2 values on the returned Message.
	 * 
	 * @param what
	 *            Value to assign to the returned Message.what field.
	 * @param arg1
	 *            Value to assign to the returned Message.arg1 field.
	 * @param arg2
	 *            Value to assign to the returned Message.arg2 field.
	 * @param obj
	 *            Value to assign to the returned Message.obj field.
	 * @return A Message from the global message pool.
	 */
	public final Message obtainMessage(int what, int arg1, int arg2, Object obj) {
		return Message.obtain(this, what, arg1, arg2, obj);
	}

	/**
	 * Pushes a message onto the end of the message queue. It will be received
	 * in {@link #handleMessage}, in the thread attached to this handler.
	 * 
	 * @return Returns true if the message was successfully placed in to the
	 *         message queue. Returns false on failure, usually because the
	 *         looper processing the message queue is exiting.
	 */
	public final boolean sendMessage(Message msg) {
		if (mQueue != null) {
			msg.target = this;
		} else {
			RuntimeException e = new RuntimeException(this + " sendMessageAtTime() called with no mQueue");
			logger.warn(TAG, e);
			return false;
		}
		return mQueue.offer(msg);
	}

	private final void handleCallback(Message message) {
		message.callback.run();
	}

	@Override
	public String toString() {
		return "Handler (" + getClass().getName() + ") {" + Integer.toHexString(System.identityHashCode(this)) + "}";
	}

	// if we can get rid of this method, the handler need not remember its loop
	// we could instead export a getMessageQueue() method...
	public final Looper getLooper() {
		return mLooper;
	}

	public final void dump(String prefix) {
		if (mLooper == null) {
			logger.info(prefix, "looper uninitialized");
		} else {
			mLooper.dump(prefix + "->mLooper ");
		}
	}
}
