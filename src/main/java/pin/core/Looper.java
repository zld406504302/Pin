package pin.core;

import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

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
public final class Looper {
	private static final Logger LOGGER = LoggerFactory.getLogger(Looper.class);

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
			Message msg;
			try {
				msg = queue.take();

				if (msg.target == null) {
					// No target is a magic identifier for the quit message.
					return;
				}

				long threadStart = System.currentTimeMillis();

				msg.target.dispatchMessage(msg);

				long threadTime = System.currentTimeMillis() - threadStart;
				LOGGER.debug("dispatchMessage cost time: " + threadTime);
				
				if(!msg.isPeriodic()) {
					msg.recycle();
				} else {
					setNextRunTime(msg);
					queue.offer(msg);
				}
				
			} catch (Exception e) {
				LOGGER.error("error on loop", e);
			}

		}
	}
	
	/**
	 * Sets the next time to run for a periodic task.
	 */
	private static void setNextRunTime(Message msg) {
		long p = msg.period;
		if (p > 0) {
			msg.time += p;
		} else {
			msg.time = msg.target.triggerTime(-p);
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
		mQueue = new DelayedWorkQueue();
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
		LOGGER.info(prefix, this.toString());
		LOGGER.info(prefix, "mRun=" + mRun);
		LOGGER.info(prefix, "mThread=" + mThread);
		LOGGER.info(prefix, "mQueue=" + ((mQueue != null) ? mQueue : "(null"));
		if (mQueue != null) {
			synchronized (mQueue) {
				Iterator<Message> it = mQueue.iterator();
				while (it.hasNext()) {
					LOGGER.info(prefix, it.next().toString());
				}
				LOGGER.info("(Total messages: " + mQueue.size() + ")");
			}
		}
	}

	public void quit() {
		Message msg = Message.obtain();
		// NOTE: By enqueueing directly into the message queue, the
		// message is left with a null target. This is how we know it is
		// a quit message.
		mQueue.offer(msg);
	}

	public String toString() {
		return "Looper{" + Integer.toHexString(System.identityHashCode(this)) + "}";
	}

	static class DelayedWorkQueue extends AbstractQueue<Message> implements BlockingQueue<Message> {

		/*
		 * A DelayedWorkQueue is based on a heap-based data structure like those
		 * in DelayQueue and PriorityQueue, except that every
		 * ScheduledFutureTask also records its index into the heap array. This
		 * eliminates the need to find a task upon cancellation, greatly
		 * speeding up removal (down from O(n) to O(log n)), and reducing
		 * garbage retention that would otherwise occur by waiting for the
		 * element to rise to top before clearing. But because the queue may
		 * also hold RunnableScheduledFutures that are not ScheduledFutureTasks,
		 * we are not guaranteed to have such indices available, in which case
		 * we fall back to linear search. (We expect that most tasks will not be
		 * decorated, and that the faster cases will be much more common.)
		 * 
		 * All heap operations must record index changes -- mainly within siftUp
		 * and siftDown. Upon removal, a task's heapIndex is set to -1. Note
		 * that ScheduledFutureTasks can appear at most once in the queue (this
		 * need not be true for other kinds of tasks or work queues), so are
		 * uniquely identified by heapIndex.
		 */

		private static final int INITIAL_CAPACITY = 64;
		private Message[] queue = new Message[INITIAL_CAPACITY];
		private final ReentrantLock lock = new ReentrantLock();
		private int size = 0;

		/**
		 * Thread designated to wait for the task at the head of the queue. This
		 * variant of the Leader-Follower pattern
		 * (http://www.cs.wustl.edu/~schmidt/POSA/POSA2/) serves to minimize
		 * unnecessary timed waiting. When a thread becomes the leader, it waits
		 * only for the next delay to elapse, but other threads await
		 * indefinitely. The leader thread must signal some other thread before
		 * returning from take() or poll(...), unless some other thread becomes
		 * leader in the interim. Whenever the head of the queue is replaced
		 * with a task with an earlier expiration time, the leader field is
		 * invalidated by being reset to null, and some waiting thread, but not
		 * necessarily the current leader, is signalled. So waiting threads must
		 * be prepared to acquire and lose leadership while waiting.
		 */
		private Thread leader = null;

		/**
		 * Condition signalled when a newer task becomes available at the head
		 * of the queue or a new thread may need to become leader.
		 */
		private final Condition available = lock.newCondition();

		/**
		 * Set f's heapIndex
		 */
		private void setIndex(Message f, int idx) {
			f.heapIndex = idx;
		}

		/**
		 * Sift element added at bottom up to its heap-ordered spot. Call only
		 * when holding lock.
		 */
		private void siftUp(int k, Message key) {
			while (k > 0) {
				int parent = (k - 1) >>> 1;
				Message e = queue[parent];
				if (key.compareTo(e) >= 0) {
					break;
				}
				queue[k] = e;
				setIndex(e, k);
				k = parent;
			}
			queue[k] = key;
			setIndex(key, k);
		}

		/**
		 * Sift element added at top down to its heap-ordered spot. Call only
		 * when holding lock.
		 */
		private void siftDown(int k, Message key) {
			int half = size >>> 1;
			while (k < half) {
				int child = (k << 1) + 1;
				Message c = queue[child];
				int right = child + 1;
				if (right < size && c.compareTo(queue[right]) > 0) {
					c = queue[child = right];
				}
				if (key.compareTo(c) <= 0) {
					break;
				}
				queue[k] = c;
				setIndex(c, k);
				k = child;
			}
			queue[k] = key;
			setIndex(key, k);
		}

		/**
		 * Resize the heap array. Call only when holding lock.
		 */
		private void grow() {
			int oldCapacity = queue.length;
			int newCapacity = oldCapacity + (oldCapacity >> 1); // grow 50%
			if (newCapacity < 0) {
				newCapacity = Integer.MAX_VALUE;
			}
			queue = Arrays.copyOf(queue, newCapacity);
		}

		/**
		 * Find index of given object, or -1 if absent
		 */
		private int indexOf(Object x) {
			if (x != null && x instanceof Message) {
				int i = ((Message) x).heapIndex;
				// Sanity check;
				if (i >= 0 && i < size && queue[i] == x)
					return i;
			}
			return -1;
		}

		public boolean contains(Object x) {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				return indexOf(x) != -1;
			} finally {
				lock.unlock();
			}
		}

		public boolean remove(Object x) {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				int i = indexOf(x);
				if (i < 0)
					return false;

				setIndex(queue[i], -1);
				int s = --size;
				Message replacement = queue[s];
				queue[s] = null;
				if (s != i) {
					siftDown(i, replacement);
					if (queue[i] == replacement)
						siftUp(i, replacement);
				}
				return true;
			} finally {
				lock.unlock();
			}
		}

		public int size() {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				return size;
			} finally {
				lock.unlock();
			}
		}

		public boolean isEmpty() {
			return size() == 0;
		}

		public int remainingCapacity() {
			return Integer.MAX_VALUE;
		}

		@Override
		public Message peek() {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				return queue[0];
			} finally {
				lock.unlock();
			}
		}

		@Override
		public boolean offer(Message x) {
			if (x == null)
				throw new NullPointerException();
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				int i = size;
				if (i >= queue.length)
					grow();
				size = i + 1;
				if (i == 0) {
					queue[0] = x;
					setIndex(x, 0);
				} else {
					siftUp(i, x);
				}
				if (queue[0] == x) {
					leader = null;
					available.signal();
				}
			} finally {
				lock.unlock();
			}
			return true;
		}

		@Override
		public void put(Message e) {
			offer(e);
		}

		@Override
		public boolean add(Message e) {
			return offer(e);
		}

		@Override
		public boolean offer(Message e, long timeout, TimeUnit unit) {
			return offer(e);
		}

		/**
		 * Performs common bookkeeping for poll and take: Replaces first element
		 * with last and sifts it down. Call only when holding lock.
		 * 
		 * @param f
		 *            the task to remove and return
		 */
		private Message finishPoll(Message f) {
			int s = --size;
			Message x = queue[s];
			queue[s] = null;
			if (s != 0)
				siftDown(0, x);
			setIndex(f, -1);
			return f;
		}

		@Override
		public Message poll() {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				Message first = queue[0];
				if (first == null || first.getDelay(TimeUnit.NANOSECONDS) > 0)
					return null;
				else
					return finishPoll(first);
			} finally {
				lock.unlock();
			}
		}

		@Override
		public Message take() throws InterruptedException {
			final ReentrantLock lock = this.lock;
			lock.lockInterruptibly();
			try {
				for (;;) {
					Message first = queue[0];
					if (first == null)
						available.await();
					else {
						long delay = first.getDelay(TimeUnit.NANOSECONDS);
						if (delay <= 0)
							return finishPoll(first);
						else if (leader != null)
							available.await();
						else {
							Thread thisThread = Thread.currentThread();
							leader = thisThread;
							try {
								available.awaitNanos(delay);
							} finally {
								if (leader == thisThread)
									leader = null;
							}
						}
					}
				}
			} finally {
				if (leader == null && queue[0] != null)
					available.signal();
				lock.unlock();
			}
		}

		@Override
		public Message poll(long timeout, TimeUnit unit) throws InterruptedException {
			long nanos = unit.toNanos(timeout);
			final ReentrantLock lock = this.lock;
			lock.lockInterruptibly();
			try {
				for (;;) {
					Message first = queue[0];
					if (first == null) {
						if (nanos <= 0)
							return null;
						else
							nanos = available.awaitNanos(nanos);
					} else {
						long delay = first.getDelay(TimeUnit.NANOSECONDS);
						if (delay <= 0)
							return finishPoll(first);
						if (nanos <= 0)
							return null;
						if (nanos < delay || leader != null)
							nanos = available.awaitNanos(nanos);
						else {
							Thread thisThread = Thread.currentThread();
							leader = thisThread;
							try {
								long timeLeft = available.awaitNanos(delay);
								nanos -= delay - timeLeft;
							} finally {
								if (leader == thisThread)
									leader = null;
							}
						}
					}
				}
			} finally {
				if (leader == null && queue[0] != null)
					available.signal();
				lock.unlock();
			}
		}

		@Override
		public void clear() {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				for (int i = 0; i < size; i++) {
					Message t = queue[i];
					if (t != null) {
						queue[i] = null;
						setIndex(t, -1);
					}
				}
				size = 0;
			} finally {
				lock.unlock();
			}
		}

		/**
		 * Return and remove first element only if it is expired. Used only by
		 * drainTo. Call only when holding lock.
		 */
		private Message pollExpired() {
			Message first = queue[0];
			if (first == null || first.getDelay(TimeUnit.NANOSECONDS) > 0)
				return null;
			return finishPoll(first);
		}

		@Override
		public int drainTo(Collection<? super Message> c) {
			if (c == null)
				throw new NullPointerException();
			if (c == this)
				throw new IllegalArgumentException();
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				Message first;
				int n = 0;
				while ((first = pollExpired()) != null) {
					c.add(first);
					++n;
				}
				return n;
			} finally {
				lock.unlock();
			}
		}

		@Override
		public int drainTo(Collection<? super Message> c, int maxElements) {
			if (c == null)
				throw new NullPointerException();
			if (c == this)
				throw new IllegalArgumentException();
			if (maxElements <= 0)
				return 0;
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				Message first;
				int n = 0;
				while (n < maxElements && (first = pollExpired()) != null) {
					c.add(first);
					++n;
				}
				return n;
			} finally {
				lock.unlock();
			}
		}

		@Override
		public Object[] toArray() {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				return Arrays.copyOf(queue, size, Object[].class);
			} finally {
				lock.unlock();
			}
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T[] toArray(T[] a) {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				if (a.length < size)
					return (T[]) Arrays.copyOf(queue, size, a.getClass());
				System.arraycopy(queue, 0, a, 0, size);
				if (a.length > size)
					a[size] = null;
				return a;
			} finally {
				lock.unlock();
			}
		}

		@Override
		public Iterator<Message> iterator() {
			return new Itr(Arrays.copyOf(queue, size));
		}

		/**
		 * Snapshot iterator that works off copy of underlying q array.
		 */
		private class Itr implements Iterator<Message> {
			final Message[] array;
			int cursor = 0; // index of next element to return
			int lastRet = -1; // index of last element, or -1 if no such

			Itr(Message[] array) {
				this.array = array;
			}

			public boolean hasNext() {
				return cursor < array.length;
			}

			public Message next() {
				if (cursor >= array.length)
					throw new NoSuchElementException();
				lastRet = cursor;
				return array[cursor++];
			}

			public void remove() {
				if (lastRet < 0)
					throw new IllegalStateException();
				DelayedWorkQueue.this.remove(array[lastRet]);
				lastRet = -1;
			}
		}
	}

}
