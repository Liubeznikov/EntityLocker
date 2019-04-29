package com.deyneka.tools;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.deyneka.tools.TestUtils.awaitFinish;

public class TryLockAndExecuteTest {
    private EntityLocker<Integer> entityLocker;
    private ExecutorService executorService;

    @BeforeMethod
    public void initEntityLocker() {
        entityLocker = new ReentrantEntityLocker<>();
        executorService = Executors.newCachedThreadPool();
    }

    @Test
    public void checkNullArguments() {
        Assert.assertThrows(IllegalArgumentException.class, () -> entityLocker.tryLockAndExecute(null, null, 0, null));
        Assert.assertThrows(IllegalArgumentException.class, () -> entityLocker.tryLockAndExecute(null, () -> {
        }, 10, TimeUnit.SECONDS));
        Assert.assertThrows(IllegalArgumentException.class, () -> entityLocker.tryLockAndExecute(1, null, 10, TimeUnit.SECONDS));
        Assert.assertThrows(IllegalArgumentException.class, () -> entityLocker.tryLockAndExecute(1, () -> {
        }, 10, null));
    }

    @Test
    public void tryLockSimpleTest(){
        executorService.submit(() -> {
            try {
                Assert.assertEquals(entityLocker.tryLockAndExecute(1, () -> {}, 1, TimeUnit.SECONDS), true);
            } catch (InterruptedException e) {
                Assert.fail();
            }
        });
        awaitFinish(executorService, 5);
    }

    @Test
    public void tryLockUnreachableEntity() {
        executorService.submit(() -> entityLocker.execute(1, () -> {
            try {
                Thread.sleep(60_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
        AtomicBoolean tryLockResult = new AtomicBoolean(true);
        executorService.submit(() -> {
                try {
                    tryLockResult.set(entityLocker.tryLockAndExecute(1, () -> {}, 1, TimeUnit.SECONDS));
                } catch (InterruptedException e) {
                    Assert.fail();
                }
            }
        );
        awaitFinish(executorService, 5);
        Assert.assertEquals(tryLockResult.get(), false);
    }
}
