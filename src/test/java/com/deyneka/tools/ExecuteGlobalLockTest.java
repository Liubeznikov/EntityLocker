package com.deyneka.tools;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.deyneka.tools.TestUtils.awaitFinish;

public class ExecuteGlobalLockTest {
    private EntityLocker<String> entityLocker;
    private ExecutorService executorService;

    @BeforeMethod
    public void initEntityLocker() {
        entityLocker = new ReentrantEntityLocker<>();
        executorService = Executors.newCachedThreadPool();
    }

    @Test
    public void checkNullArguments(){
        Assert.assertThrows(IllegalArgumentException.class, () -> entityLocker.execute(null));
    }

    @Test
    public void checkGlobalLock(){
        executorService.submit(() -> entityLocker.execute(() -> {
            try {
                Thread.sleep(50_000);
            } catch (InterruptedException e) {
                Assert.fail();
            }
        }));
        executorService.submit(() -> entityLocker.execute("entity1", Assert::fail));
        executorService.submit(() -> entityLocker.execute("entity2", Assert::fail));
        executorService.submit(() -> entityLocker.execute("entity3", Assert::fail));
        awaitFinish(executorService, 5);
    }

    @Test
    public void checkProtectedCodeExecutionWithGlobalLocks(){
        final long[] unProtectedLong = {0};
        for (int i = 0; i < 100_000; i++) {
            executorService.submit(() -> {
                Runnable runnable = () -> unProtectedLong[0]++;
                entityLocker.execute(runnable);
            });
        }
        awaitFinish(executorService, 10);
        Assert.assertEquals(unProtectedLong[0], 100_000);
    }

    @Test
    public void checkMaxConcurrentRunnables(){
        AtomicInteger maxConcurrent = new AtomicInteger(0);
        AtomicInteger curConcurrent = new AtomicInteger(0);
        Runnable runnable = () -> {
            curConcurrent.incrementAndGet();
            if (curConcurrent.get() > maxConcurrent.get()){
                maxConcurrent.set(curConcurrent.get());
            }
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Assert.fail();
            }
            curConcurrent.decrementAndGet();
        };
        for (int i = 0; i < 100; i++) {
            executorService.submit(() -> entityLocker.execute(runnable));
        }
        awaitFinish(executorService, 10);
        Assert.assertEquals(maxConcurrent.get(), 1);
    }

    @Test
    public void checkReentrantGlobalLock(){
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        executorService.submit(
                () -> entityLocker.execute(() -> entityLocker.execute(
                        () -> entityLocker.execute(() -> atomicBoolean.set(true)))));
        awaitFinish(executorService, 1);
        Assert.assertTrue(atomicBoolean.get());
    }
}
