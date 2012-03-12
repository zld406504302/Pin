package pin.message;

import org.slf4j.impl.StaticLoggerBinder;

import pin.core.Handler;
import pin.core.HandlerThread;
import pin.core.Looper;
import pin.core.Message;

public class SingleThreadHandlerTest {
	public static void main(String[] args) {
		StaticLoggerBinder.getSingleton().getLoggerFactory();
		HandlerThread hThread = new HandlerThread("testHandlerThread");
		hThread.start();
		MainHandler mHandler = new MainHandler(hThread.getLooper());
		Message m = mHandler.obtainMessage(1, "aaaa");
		m.sendToTarget();
	}
}

class MyHandler extends Handler {
	public MyHandler(Looper looper) {
		super(looper);
	}

	@Override
	public void handleMessage(Message msg) { // 处理消息
		System.out.println(msg.obj.toString());
	}
}