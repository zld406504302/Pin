package pin.asyncTask;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pin.asyncTask.AsyncTaskResult.EAsyncTaskAction;

/**
 * <p>
 * AsyncTask enables proper and easy use of the UI thread. This class allows to
 * perform background operations and publish results on the UI thread without
 * having to manipulate threads and/or handlers.
 * </p>
 * 
 * <p>
 * An asynchronous task is defined by a computation that runs on a background
 * thread and whose result is published on the UI thread. An asynchronous task
 * is defined by 3 generic types, called <code>Params</code>,
 * <code>Progress</code> and <code>Result</code>, and 4 steps, called
 * <code>begin</code>, <code>doInBackground</code>, <code>processProgress</code>
 * and <code>end</code>.
 * </p>
 * 
 * <h2>Usage</h2>
 * <p>
 * AsyncTask must be subclassed to be used. The subclass will override at least
 * one method ({@link #doInBackground}), and most often will override a second
 * one ({@link #onPostExecute}.)
 * </p>
 * 
 * <p>
 * Here is an example of subclassing:
 * </p>
 * 
 * <pre class="prettyprint">
 * private class DownloadFilesTask extends AsyncTask&lt;URL, Integer, Long&gt; {
 * 	protected Long doInBackground(URL... urls) {
 * 		int count = urls.length;
 * 		long totalSize = 0;
 * 		for (int i = 0; i &lt; count; i++) {
 * 			totalSize += Downloader.downloadFile(urls[i]);
 * 			publishProgress((int) ((i / (float) count) * 100));
 * 		}
 * 		return totalSize;
 * 	}
 * 
 * 	protected void onProgressUpdate(Integer... progress) {
 * 		setProgressPercent(progress[0]);
 * 	}
 * 
 * 	protected void onPostExecute(Long result) {
 * 		showDialog(&quot;Downloaded &quot; + result + &quot; bytes&quot;);
 * 	}
 * }
 * </pre>
 * 
 * <p>
 * Once created, a task is executed very simply:
 * </p>
 * 
 * <pre class="prettyprint">
 * new DownloadFilesTask().execute(url1, url2, url3);
 * </pre>
 * 
 * <h2>AsyncTask's generic types</h2>
 * <p>
 * The three types used by an asynchronous task are the following:
 * </p>
 * <ol>
 * <li><code>Params</code>, the type of the parameters sent to the task upon
 * execution.</li>
 * <li><code>Progress</code>, the type of the progress units published during
 * the background computation.</li>
 * <li><code>Result</code>, the type of the result of the background
 * computation.</li>
 * </ol>
 * <p>
 * Not all types are always used by an asynchronous task. To mark a type as
 * unused, simply use the type {@link Void}:
 * </p>
 * 
 * <pre>
 * private class MyTask extends AsyncTask&lt;Void, Void, Void&gt; { ... }
 * </pre>
 * 
 * <h2>The 4 steps</h2>
 * <p>
 * When an asynchronous task is executed, the task goes through 4 steps:
 * </p>
 * <ol>
 * <li>{@link #onPreExecute()}, invoked on the UI thread immediately after the
 * task is executed. This step is normally used to setup the task, for instance
 * by showing a progress bar in the user interface.</li>
 * <li>{@link #doInBackground}, invoked on the background thread immediately
 * after {@link #onPreExecute()} finishes executing. This step is used to
 * perform background computation that can take a long time. The parameters of
 * the asynchronous task are passed to this step. The result of the computation
 * must be returned by this step and will be passed back to the last step. This
 * step can also use {@link #publishProgress} to publish one or more units of
 * progress. These values are published on the UI thread, in the
 * {@link #onProgressUpdate} step.</li>
 * <li>{@link #onProgressUpdate}, invoked on the UI thread after a call to
 * {@link #publishProgress}. The timing of the execution is undefined. This
 * method is used to display any form of progress in the user interface while
 * the background computation is still executing. For instance, it can be used
 * to animate a progress bar or show logs in a text field.</li>
 * <li>{@link #onPostExecute}, invoked on the UI thread after the background
 * computation finishes. The result of the background computation is passed to
 * this step as a parameter.</li>
 * </ol>
 * 
 * <h2>Threading rules</h2>
 * <p>
 * There are a few threading rules that must be followed for this class to work
 * properly:
 * </p>
 * <ul>
 * <li>The task instance must be created on the UI thread.</li>
 * <li>{@link #execute} must be invoked on the UI thread.</li>
 * <li>Do not call {@link #onPreExecute()}, {@link #onPostExecute},
 * {@link #doInBackground}, {@link #onProgressUpdate} manually.</li>
 * <li>The task can be executed only once (an exception will be thrown if a
 * second execution is attempted.)</li>
 * </ul>
 */
public abstract class AsyncTask<Params, Progress, Result> {
	private static final int CORE_POOL_SIZE = 5;
	private static final int MAXIMUM_POOL_SIZE = 128;
	private static final int KEEP_ALIVE = 1;
	private static final Logger logger = LoggerFactory
			.getLogger(AsyncTask.class);

	private static final BlockingQueue<Runnable> sWorkQueue = new LinkedBlockingQueue<Runnable>(
			10);

	private static final ThreadFactory sThreadFactory = new ThreadFactory() {
		private final AtomicInteger mCount = new AtomicInteger(1);

		public Thread newThread(Runnable r) {
			return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
		}
	};

	private static final ThreadPoolExecutor sExecutor = new ThreadPoolExecutor(
			CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS,
			sWorkQueue, sThreadFactory);

	private volatile Status mStatus = Status.PENDING;

	private CallBackHandler cbHandler;
	private final WorkerRunnable<Params, Result> mWorker;
	private final FutureTask<Result> mFuture;

	/**
	 * Indicates the current status of the task. Each status will be set only
	 * once during the lifetime of a task.
	 */
	public enum Status {
		/**
		 * Indicates that the task has not been executed yet.
		 */
		PENDING,
		/**
		 * Indicates that the task is running.
		 */
		RUNNING,
		/**
		 * Indicates that the task is cancelled.
		 */
		CANCEL,
		/**
		 * Indicates that {@link AsyncTask#onPostExecute} has finished.
		 */
		FINISHED,
	}

	/**
	 * Creates a new asynchronous task. This constructor must be invoked on the
	 * UI thread.
	 */
	public AsyncTask() {

		mWorker = new WorkerRunnable<Params, Result>() {
			public Result call() throws Exception {
				// Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
				return doInBackground(mParams);
			}
		};

		mFuture = new FutureTask<Result>(mWorker) {
			@Override
			protected void done() {
				Result result = null;
				AsyncTaskResult<Result> asyncResult = null;
				try {
					result = get();
				} catch (InterruptedException e) {
					logger.error("async task interrupted!", e);
				} catch (ExecutionException e) {
					throw new RuntimeException(
							"An error occured while executing doInBackground()",
							e.getCause());
				} catch (CancellationException e) {
					if (cbHandler == null) {
						onCancelled();
					} else {
						asyncResult = new AsyncTaskResult<Result>(
								AsyncTask.this, EAsyncTaskAction.CANCEL,
								(Result) null);
						cbHandler.addResult(asyncResult);
					}
					AsyncTask.this.mStatus = Status.CANCEL;
					return;
				} catch (Throwable t) {
					throw new RuntimeException(
							"An error occured while executing doInBackground()",
							t);
				}
				if (cbHandler == null) {
					finish(result);
				} else {
					asyncResult = new AsyncTaskResult<Result>(AsyncTask.this, EAsyncTaskAction.RESULT, result);
					cbHandler.addResult(asyncResult);
				}
			}
		};
	}

	/**
	 * Override this method to perform a computation on a background thread. The
	 * specified parameters are the parameters passed to {@link #execute} by the
	 * caller of this task.
	 * 
	 * 
	 * @param params
	 *            The parameters of the task.
	 * 
	 * @return A result, defined by the subclass of this task.
	 * 
	 * @see #onPreExecute()
	 * @see #onPostExecute
	 */
	protected abstract Result doInBackground(Params... params);

	/**
	 * Runs on the UI thread before {@link #doInBackground}.
	 * 
	 * @see #onPostExecute
	 * @see #doInBackground
	 */
	protected void onPreExecute() {
	}

	/**
	 * Runs on the UI thread after {@link #doInBackground}. The specified result
	 * is the value returned by {@link #doInBackground} or null if the task was
	 * cancelled or an exception occured.
	 * 
	 * @param result
	 *            The result of the operation computed by
	 *            {@link #doInBackground}.
	 * 
	 * @see #onPreExecute
	 * @see #doInBackground
	 */
	protected void onPostExecute(Result result) {
	}

	/**
	 * Runs on the UI thread after {@link #publishProgress} is invoked. The
	 * specified values are the values passed to {@link #publishProgress}.
	 * 
	 * @param values
	 *            The values indicating progress.
	 * 
	 * @see #publishProgress
	 * @see #doInBackground
	 */
	protected void onProgressUpdate(Progress value) {
	}

	/**
	 * Runs on the UI thread after {@link #cancel(boolean)} is invoked.
	 * 
	 * @see #cancel(boolean)
	 * @see #isCancelled()
	 */
	protected void onCancelled() {
	}

	/**
	 * Returns <tt>true</tt> if this task was cancelled before it completed
	 * normally.
	 * 
	 * @return <tt>true</tt> if task was cancelled before it completed
	 * 
	 * @see #cancel(boolean)
	 */
	public final boolean isCancelled() {
		return mFuture.isCancelled();
	}

	public final boolean cancel(boolean mayInterruptIfRunning) {
		return mFuture.cancel(mayInterruptIfRunning);
	}

	/**
	 * Returns the current status of this task.
	 * 
	 * @return The current status.
	 */
	public final Status getStatus() {
		return mStatus;
	}

	/**
	 * Executes the task with the specified parameters. The task returns itself
	 * (this) so that the caller can keep a reference to it.
	 * 
	 * This method must be invoked on the UI thread.
	 * 
	 * @param params
	 *            The parameters of the task.
	 * 
	 * @return This instance of AsyncTask.
	 * 
	 * @throws IllegalStateException
	 *             If {@link #getStatus()} returns either
	 *             {@link AsyncTask.Status#RUNNING} or
	 *             {@link AsyncTask.Status#FINISHED}.
	 */
	public final AsyncTask<Params, Progress, Result> execute(Params... params) {
		if (mStatus != Status.PENDING) {
			switch (mStatus) {
			case RUNNING:
				throw new IllegalStateException("Cannot execute task:"
						+ " the task is already running.");
			case FINISHED:
				throw new IllegalStateException("Cannot execute task:"
						+ " the task has already been executed "
						+ "(a task can be executed only once)");
			}
		}

		mStatus = Status.RUNNING;

		onPreExecute();

		mWorker.mParams = params;

		sExecutor.execute(mFuture);

		return this;
	}

	/**
	 * This method can be invoked from {@link #doInBackground} to publish
	 * updates on the UI thread while the background computation is still
	 * running. Each call to this method will trigger the execution of
	 * {@link #onProgressUpdate} on the UI thread.
	 * 
	 * @param values
	 *            The progress values to update the UI with.
	 * 
	 * @see #onProgressUpdate
	 * @see #doInBackground
	 */
	protected final void publishProgress(Progress value) {
		onProgressUpdate(value);
	}

	protected final void finish(Result result) {
		if (isCancelled())
			result = null;
		onPostExecute(result);
		mStatus = Status.FINISHED;
	}

	private static abstract class WorkerRunnable<Params, Result> implements
			Callable<Result> {
		Params[] mParams;
	}
}
