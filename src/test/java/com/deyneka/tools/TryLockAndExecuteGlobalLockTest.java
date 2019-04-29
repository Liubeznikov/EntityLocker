package com.deyneka.tools;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.deyneka.tools.TestUtils.awaitFinish;

public class TryLockAndExecuteGlobalLockTest {
    private EntityLocker<String> entityLocker;
    private ExecutorService executorService;

    @BeforeMethod
    public void initEntityLocker() {
        entityLocker = new ReentrantEntityLocker<>();
        executorService = Executors.newCachedThreadPool();
    }

    @Test
    public void checkNullArguments() {
        Assert.assertThrows(IllegalArgumentException.class, () -> entityLocker.tryLockAndExecute(null, 0, null));
        Assert.assertThrows(IllegalArgumentException.class, () -> entityLocker.tryLockAndExecute(null, 0, TimeUnit.SECONDS));
        Assert.assertThrows(IllegalArgumentException.class, () -> entityLocker.tryLockAndExecute(() -> {}, 0, null));
    }

    @Test
    public void tryGlobalLockSimpleTest(){
        executorService.submit(() -> {
            try {
                Assert.assertEquals(entityLocker.tryLockAndExecute(() -> {}, 1, TimeUnit.SECONDS), true);
            } catch (InterruptedException e) {
                Assert.fail();
            }
        });
        awaitFinish(executorService, 5);
    }

    @Test
    public void tryLockGlobalUnreachableEntity() {
        executorService.submit(() -> entityLocker.execute( () -> {
            try {
                Thread.sleep(60_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
        executorService.submit(() -> entityLocker.tryLockAndExecute(Assert::fail, 1, TimeUnit.SECONDS));
        awaitFinish(executorService, 3);
    }
}
