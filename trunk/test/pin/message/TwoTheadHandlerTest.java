package pin.message;

import org.slf4j.impl.StaticLoggerBinder;

import pin.core.Handler;
import pin.core.HandlerThread;
import pin.core.Looper;
import pin.core.Message;

public class TwoTheadHandlerTest {
	public static void main(String[] args) {
		StaticLoggerBinder.getSingleton().getLoggerFactory();
		HandlerThread mainThread = new HandlerThread("MainThread");
		mainThread.start(); //主线程开始工作
		
		HandlerThread workerThread = new HandlerThread("WorkerThread");
		workerThread.start(); //工作线程开始工作
		
		MainHandler mainHandler = new MainHandler(mainThread.getLooper());
		
		WorkerHandler mHandler = new WorkerHandler(workerThread.getLooper(), mainHandler);
		Message m = mHandler.obtainMessage(1, "aaaa");
		m.sendToTarget();
	}
}

class WorkerHandler extends Handler {
	private Handler mainHandler;
	public WorkerHandler(Looper looper, Handler mainHandler) {
		super(looper);
		this.mainHandler = mainHandler;
	}

	@Override
	public void handleMessage(Message msg) { // 处理消息
		System.out.println("msg received! " + msg.obj.toString()  + Thread.currentThread().getName());
		Message m = mainHandler.obtainMessage(1, "main handler excuted in mainThread! ");
		m.sendToTarget();
	}
}

class MainHandler extends Handler {
	public MainHandler(Looper looper) {
		super(looper);
	}

	@Override
	public void handleMessage(Message msg) { // 处理消息
		System.out.println(msg.obj.toString() + Thread.currentThread().getName());
	}
}