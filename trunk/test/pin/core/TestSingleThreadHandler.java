package pin.core;

import junit.framework.Assert;

import org.junit.Test;

import pin.core.Handler;
import pin.core.HandlerThread;
import pin.core.Looper;
import pin.core.Message;

public class TestSingleThreadHandler {

	@Test
	public void singleThreadHandler() {
		HandlerThread hThread = new HandlerThread("testHandlerThread");
		hThread.start();
		MyHandler mHandler = new MyHandler(hThread.getLooper());
		Message m = mHandler.obtainMessage(1, "aaaa");
		m.sendToTarget();
	}

	class MyHandler extends Handler {
		public MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) { // 处理消息
			Assert.assertEquals("aaaa", msg.obj.toString());
		}
	}
}
