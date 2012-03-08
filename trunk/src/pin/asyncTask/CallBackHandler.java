package pin.asyncTask;

public interface CallBackHandler {
	@SuppressWarnings("rawtypes")
	boolean addResult(AsyncTaskResult result);
	void handleResult();
}
