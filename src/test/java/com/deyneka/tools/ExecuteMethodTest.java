package com.deyneka.tools;

import org.testng.Assert;
import org.testng.annotations.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.deyneka.tools.TestUtils.awaitFinish;

public class ExecuteMethodTest {
    private EntityLocker<String> entityLocker;
    private ExecutorService executorService;

    @BeforeMethod
    public void initEntityLocker() {
        entityLocker = new ReentrantEntityLocker<>();
        executorService = Executors.newCachedThreadPool();
    }

    @Test
    public void checkNullArguments(){
        Assert.assertThrows(IllegalArgumentException.class, () -> entityLocker.execute(null, null));
        Assert.assertThrows(IllegalArgumentException.class, () -> entityLocker.execute(null, () -> {}));
        Assert.assertThrows(IllegalArgumentException.class, () -> entityLocker.execute("entity1", null));
        Assert.assertThrows(IllegalArgumentException.class, () -> entityLocker.execute("entity1", () -> {throw new IllegalArgumentException();}));
    }

    @Test
    public void checkProtectedCodeExecution(){
        final long[] unProtectedLong = {0};
        for (int i = 0; i < 100_000; i++) {
            executorService.submit(() -> {
                Runnable runnable = () -> unProtectedLong[0]++;
                entityLocker.execute("entity1", runnable);
            });
        }
        awaitFinish(executorService, 10);
        Assert.assertEquals(unProtectedLong[0], 100_000);
    }

    @DataProvider
    public Object[][] getData(){
        return new Object[][]{
                {Arrays.asList("entity1", "entity2", "entity3"),3},
                {Arrays.asList("entity1", "entity1", "entity2"),2},
                {Arrays.asList("entity1", "entity2", "entity2"),2},
                {Arrays.asList("entity1", "entity1", "entity1"),1},
                {Arrays.asList("entity1", "entity2", "entity3","entity1", "entity2", "entity3"),3},
                {Arrays.asList("entity1", "entity2", "entity3","entity4", "entity5", "entity6"),6},
                {Collections.emptyList(),0}
        };
    }

    @Parameters
    @Test(dataProvider = "getData")
    public void checkMaxConcurrentRunnables(List<String> entities, long maxThreadCount){
        AtomicInteger maxConcurrent = new AtomicInteger(0);
        AtomicInteger curConcurrent = new AtomicInteger(0);
        Runnable runnable = () -> {
            curConcurrent.incrementAndGet();
            if (curConcurrent.get() > maxConcurrent.get()){
                maxConcurrent.set(curConcurrent.get());
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Assert.fail();
            }
            curConcurrent.decrementAndGet();
        };
        for (int i = 0; i < 100; i++) {
            for (String entity: entities){
                executorService.submit(() -> entityLocker.execute(entity, runnable));
            }
        }
        awaitFinish(executorService, 10);
        Assert.assertEquals(maxConcurrent.get(), maxThreadCount);
    }

    @Test
    public void checkReentrantLock(){
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        executorService.submit(() -> entityLocker.execute("entity1",
                () -> entityLocker.execute("entity1",
                        () -> atomicBoolean.set(true))));
        awaitFinish(executorService, 1);
        Assert.assertTrue(atomicBoolean.get());
    }
}