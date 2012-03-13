package pin.core;

import org.junit.Assert;
import org.junit.Test;

import pin.core.Handler;
import pin.core.HandlerThread;
import pin.core.Looper;
import pin.core.Message;

public class TestTwoTheadHandler {

	@Test
	public void twoTheadHandler() {
		HandlerThread mainThread = new HandlerThread("MainThread");
		mainThread.start(); //主线程开始工作
		
		HandlerThread workerThread = new HandlerThread("WorkerThread");
		workerThread.start(); //工作线程开始工作
		
		MainHandler mainHandler = new MainHandler(mainThread.getLooper());
		
		WorkerHandler mHandler = new WorkerHandler(workerThread.getLooper(), mainHandler);
		Message m = mHandler.obtainMessage(1, "aaaa");
		m.sendToTarget();
	}

	class WorkerHandler extends Handler {
		private Handler mainHandler;
		public WorkerHandler(Looper looper, Handler mainHandler) {
			super(looper);
			this.mainHandler = mainHandler;
		}

		@Override
		public void handleMessage(Message msg) { // 处理消息
			Assert.assertEquals("aaaa", msg.obj.toString());
			Assert.assertEquals("WorkerThread", Thread.currentThread().getName());
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
			Assert.assertEquals("main handler excuted in mainThread! ", msg.obj.toString());
			Assert.assertEquals("MainThread", Thread.currentThread().getName());
		}
	}
}
