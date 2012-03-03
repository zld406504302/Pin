package pin.asyncTask;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncTaskManager {

	private static final int CORE_POOL_SIZE = 5;
	private static final int MAXIMUM_POOL_SIZE = 128;
	private static final int KEEP_ALIVE = 1;

	private final BlockingQueue<Runnable> sWorkQueue = new LinkedBlockingQueue<Runnable>(10);

	private final ThreadFactory sThreadFactory = new ThreadFactory() {
		private final AtomicInteger mCount = new AtomicInteger(1);

		public Thread newThread(Runnable r) {
			return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
		}
	};

	private final ThreadPoolExecutor sExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, sWorkQueue,
			sThreadFactory);

	@SuppressWarnings("rawtypes")
	private final BlockingQueue<AsyncTaskResult> completionQueue = new LinkedBlockingQueue<AsyncTaskResult>();

	private static AsyncTaskManager instance = new AsyncTaskManager();
	
	private AsyncTaskManager() {
		
	}
	
	public static AsyncTaskManager instance() {
		return instance;
	}
	
	@SuppressWarnings("rawtypes")
	protected boolean addResult(AsyncTaskResult result) {
		return completionQueue.add(result);
	}

	@SuppressWarnings("unchecked")
	public void handleResult() {
		@SuppressWarnings("rawtypes")
		AsyncTaskResult result = completionQueue.poll();
		if (result != null) {
			switch (result.action) {
			case RESULT:
				result.mTask.finish(result.data);
				break;
			case UPDATE:
				result.mTask.onProgressUpdate(result.data);
				break;
			case CANCEL:
				result.mTask.onCancelled();
				break;
			}
		}
	}

	protected Executor getsExecutor() {
		return sExecutor;
	}
}
