package pin.core;

/**
 * Handy class for starting a new thread that has a looper. The looper can then
 * be used to create handler classes. Note that start() must still be called.
 */
public class HandlerThread extends Thread {
	private int mPriority;
	private long mTid = -1;
	private Looper mLooper;

	public HandlerThread(String name) {
		super(name);
		mPriority = currentThread().getPriority();
		setPriority(mPriority);
	}

	/**
	 * Constructs a HandlerThread.
	 * 
	 * @param name
	 * @param priority
	 *            The priority to run the thread at.
	 */
	public HandlerThread(String name, int priority) {
		super(name);
		mPriority = priority;
	}

	/**
	 * Call back method that can be explicitly over ridden if needed to execute
	 * some setup before Looper loops.
	 */
	protected void onLooperPrepared() {
	}

	public void run() {
		mTid = currentThread().getId();
		Looper.prepare();
		synchronized (this) {
			mLooper = Looper.myLooper();
			notifyAll();
		}
		onLooperPrepared();
		Looper.loop();
		mTid = -1;
	}

	/**
	 * This method returns the Looper associated with this thread. If this
	 * thread not been started or for any reason is isAlive() returns false,
	 * this method will return null. If this thread has been started, this
	 * method will block until the looper has been initialized.
	 * 
	 * @return The looper.
	 */
	public Looper getLooper() {
		if (!isAlive()) {
			return null;
		}

		// If the thread has been started, wait until the looper has been
		// created.
		synchronized (this) {
			while (isAlive() && mLooper == null) {
				try {
					wait();
				} catch (InterruptedException e) {
				}
			}
		}
		return mLooper;
	}

	/**
	 * Ask the currently running looper to quit. If the thread has not been
	 * started or has finished (that is if {@link #getLooper} returns null),
	 * then false is returned. Otherwise the looper is asked to quit and true is
	 * returned.
	 */
	public boolean quit() {
		Looper looper = getLooper();
		if (looper != null) {
			looper.quit();
			return true;
		}
		return false;
	}

	/**
	 * Returns the identifier of this thread.
	 */
	public long getThreadId() {
		return mTid;
	}
}
