package com.deyneka.tools;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.deyneka.tools.TestUtils.awaitFinish;

public class ClearTest {
    private EntityLocker<Integer> entityLocker;
    private ExecutorService executorService;

    @BeforeMethod
    public void initEntityLocker() {
        entityLocker = new ReentrantEntityLocker<>();
        executorService = Executors.newCachedThreadPool();
    }

    @Test
    public void simpleTest(){
        final long[] unProtectedLong = {0};
        for (int i = 0; i < 100_000; i++) {
            executorService.submit(() -> entityLocker.execute(1,() -> unProtectedLong[0]++));
            entityLocker.clear();
            executorService.submit(() -> entityLocker.execute(1,() -> unProtectedLong[0]--));
        }
        awaitFinish(executorService, 10);
        Assert.assertEquals(unProtectedLong[0], 0);
    }
}
