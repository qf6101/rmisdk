package io.github.qf6101.rmisdk.util;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * User: qfeng
 * Date: 15-12-7 下午3:40
 * Usage: 测试循环队列
 */
public class CircularListTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CircularListTest.class);
    private CircularList<String> cl = null;
    private ExecutorService servicePool = null;

    @Before
    public void setUp() {
        servicePool = Executors.newFixedThreadPool(10);
        cl = new CircularList<String>();
        cl.add("1");
        cl.add("2");
        cl.add("3");
        cl.add("4");
        cl.add("5");
    }

    @Test
    public void testMoveNext() throws InterruptedException {
        for (int i = 0; i < 1000; ++i) {
            final int finalI = i;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    testMoveNextImp(finalI);
                }
            });
            thread.start();
        }
        servicePool.awaitTermination(1000, TimeUnit.HOURS);
    }

    private void testMoveNextImp(final int i) {
        Future<Integer> future = servicePool.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                LOGGER.info(i + " :" + cl.next());
                Thread.sleep(2000);
                return 0;
            }
        });
        try {
            future.get(1000, TimeUnit.SECONDS);
        } catch (Exception ex) {
            LOGGER.error("", ex);
            future.cancel(true);
        }
    }

}
