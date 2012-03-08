package pin.asyncTask;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DefaultCallBackHandler implements CallBackHandler {

	@SuppressWarnings("rawtypes")
	private final BlockingQueue<AsyncTaskResult> completionQueue = new LinkedBlockingQueue<AsyncTaskResult>();
	
	@SuppressWarnings("rawtypes")
	public boolean addResult(AsyncTaskResult result) {
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

}
