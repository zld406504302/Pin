package pin.asyncTask;

public class AsyncTaskResult<Data> {

	public enum EAsyncTaskAction {
		/**
		 * Indicates that the task has been succeed executed.
		 */
		RESULT,
		/**
		 * Indicates that the task has been cancelled.
		 */
		CANCEL,
		/**
		 * Indicates that the task state is updated.
		 */
		UPDATE
	}

	@SuppressWarnings("rawtypes")
	final AsyncTask mTask;
	final EAsyncTaskAction action;
	final Data data;

	@SuppressWarnings("rawtypes")
	AsyncTaskResult(AsyncTask task, EAsyncTaskAction action, Data data) {
		mTask = task;
		this.action = action;
		this.data = data;
	}
}
