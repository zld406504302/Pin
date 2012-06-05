package com.pin.event;

import org.junit.Assert;
import org.junit.Test;

import pin.event.EventArgs;
import pin.event.EventListener;
import pin.event.EventSource;

public class TestEvent {

	class TestSender {
		private String hello;
		private String world;

		/**
		 * @return the hello
		 */
		public String getHello() {
			return hello;
		}

		/**
		 * @param hello
		 *            the hello to set
		 */
		public void setHello(String hello) {
			this.hello = hello;
		}

		/**
		 * @return the world
		 */
		public String getWorld() {
			return world;
		}

		/**
		 * @param world
		 *            the world to set
		 */
		public void setWorld(String world) {
			this.world = world;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return hello + " " + world;
		}

	}

	class TestEvetnArgs extends EventArgs {
		private String arg1 = "Hello";
		private String arg2 = "World!";

		/**
		 * @return the arg1
		 */
		public String getArg1() {
			return arg1;
		}

		/**
		 * @return the arg2
		 */
		public String getArg2() {
			return arg2;
		}

	}

	class TestEventListener implements EventListener {

		@Override
		public void fireEvent(Object sender, EventArgs event) {
			TestSender testSender = (TestSender) sender;
			TestEvetnArgs testEventArgs = (TestEvetnArgs) event;
			testSender.setHello(testEventArgs.getArg1());
			testSender.setWorld(testEventArgs.getArg2());
		}

	}

	@Test
	public void test() {
		TestSender testSender = new TestSender();
		EventSource testSource = new EventSource(testSender);
		
		testSource.addListener(new TestEventListener());
		
		testSource.fireEvent(new TestEvetnArgs());
		
		Assert.assertEquals("Hello World!", testSender.toString());
	}

}
