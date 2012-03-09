package pin.core;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to run a message loop for a thread. Threads by default do not have
 * a message loop associated with them; to create one, call {@link #prepare} in
 * the thread that is to run the loop, and then {@link #loop} to have it process
 * messages until the loop is stopped.
 * 
 * <p>
 * Most interaction with a message loop is through the {@link Handler} class.
 * 
 * <p>
 * This is a typical example of the implementation of a Looper thread, using the
 * separation of {@link #prepare} and {@link #loop} to create an initial Handler
 * to communicate with the Looper.
 * 
 * <pre>
 * class LooperThread extends Thread {
 * 	public Handler mHandler;
 * 
 * 	public void run() {
 * 		Looper.prepare();
 * 
 * 		mHandler = new Handler() {
 * 			public void handleMessage(Message msg) {
 * 				// process incoming messages here
 * 			}
 * 		};
 * 
 * 		Looper.loop();
 * 	}
 * }
 * </pre>
 */
public class Looper {
	private static final String TAG = "Looper";
	private static final Logger logger = LoggerFactory.getLogger(Looper.class);

	// sThreadLocal.get() will return null unless you've called prepare().
	static final ThreadLocal<Looper> sThreadLocal = new ThreadLocal<Looper>();
	final BlockingQueue<Message> mQueue;
	final Thread mThread;
	volatile boolean mRun;

	/**
	 * Initialize the current thread as a looper. This gives you a chance to
	 * create handlers that then reference this looper, before actually starting
	 * the loop. Be sure to call {@link #loop()} after calling this method, and
	 * end it by calling {@link #quit()}.
	 */
	public static void prepare() {
		if (sThreadLocal.get() != null) {
			throw new RuntimeException("Only one Looper may be created per thread");
		}
		sThreadLocal.set(new Looper());
	}

	/**
	 * Run the message queue in this thread. Be sure to call {@link #quit()} to
	 * end the loop.
	 */
	public static void loop() {
		Looper me = myLooper();
		if (me == null) {
			throw new RuntimeException("No Looper; Looper.prepare() wasn't called on this thread.");
		}
		BlockingQueue<Message> queue = me.mQueue;

		while (true) {
			Message msg = queue.poll();
			if (msg != null) {
				if (msg.target == null) {
					// No target is a magic identifier for the quit message.
					return;
				}

				long threadStart = 0;
				threadStart = System.currentTimeMillis();

				msg.target.dispatchMessage(msg);

				long threadTime = System.currentTimeMillis() - threadStart;
				logger.debug(TAG, "dispatchMessage cost time: " + threadTime);

				msg.recycle();
			}
		}
	}

	/**
	 * Return the Looper object associated with the current thread. Returns null
	 * if the calling thread is not associated with a Looper.
	 */
	public static Looper myLooper() {
		return sThreadLocal.get();
	}

	private Looper() {
		mQueue = new LinkedBlockingQueue<Message>();
		mRun = true;
		mThread = Thread.currentThread();
	}

	/**
	 * Return the Thread associated with this Looper.
	 */
	public Thread getThread() {
		return mThread;
	}

	public void dump(String prefix) {
		logger.info(prefix, this.toString());
		logger.info(prefix, "mRun=" + mRun);
		logger.info(prefix, "mThread=" + mThread);
		logger.info(prefix, "mQueue=" + ((mQueue != null) ? mQueue : "(null"));
		if (mQueue != null) {
			synchronized (mQueue) {
				Iterator<Message> it = mQueue.iterator();
				while (it.hasNext()) {
					logger.info(prefix, it.next().toString());
				}
				logger.info("(Total messages: " + mQueue.size() + ")");
			}
		}
	}

    public void quit() {
        Message msg = Message.obtain();
        // NOTE: By enqueueing directly into the message queue, the
        // message is left with a null target.  This is how we know it is
        // a quit message.
        mQueue.offer(msg);
    }
    
	public String toString() {
		return "Looper{" + Integer.toHexString(System.identityHashCode(this)) + "}";
	}

}
