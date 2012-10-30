package com.pin.util;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: zhongyuan
 * Date: 12-8-20
 * Time: 下午3:44
 */
public class TestVolatile {
    private volatile int i = 0;

    Thread t1;
    Thread t2;

    @Before
    public void setUp() throws Exception {
        t1 = new Thread(new PlusOne());
        t2 = new Thread(new PlusOne());
    }

    @Test
    public void testVolatile() {
        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(200, i);
    }

    class PlusOne implements Runnable {
        private int counter = 100;

        @Override
        public void run() {
            while (counter > 0) {
                i++;
                counter--;
            }

        }
    }
}
