package com.pin.core;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import pin.core.Handler;
import pin.core.HandlerThread;
import pin.core.Looper;
import pin.core.Message;

public class TestMessageLooper {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		HandlerThread testHandlerThread = new HandlerThread("testHandlerThread");
		testHandlerThread.start();
		
		TestHandler handler = new TestHandler(testHandlerThread.getLooper());
		handler.sendMessage(Message.obtain(handler, 1, 2, 3));
		handler.sendMessageDelayed(Message.obtain(handler, 4, 5, 6), 3, TimeUnit.SECONDS);
		
		handler.sendMessageAtFixedRate(Message.obtain(handler, 7, 8, 9), 5, 3, TimeUnit.SECONDS);
		
		handler.sendMessageAtFixedDelay(Message.obtain(handler, 10, 11, 12), 7, 4, TimeUnit.SECONDS);
		
		while(true) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	class TestHandler extends Handler {

		public TestHandler(Looper looper) {
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			System.out.println("message handled at " + System.nanoTime());
			System.out.println(msg);
		}
		
	}
}
