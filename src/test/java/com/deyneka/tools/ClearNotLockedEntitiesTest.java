package com.deyneka.tools;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.deyneka.tools.TestUtils.awaitFinish;

public class ClearNotLockedEntitiesTest {
    private EntityLocker<Integer> entityLocker;
    private ExecutorService executorService;

    @BeforeMethod
    public void initEntityLocker() {
        entityLocker = new ReentrantEntityLocker<>();
        executorService = Executors.newCachedThreadPool();
    }

    @Test
    public void testFrequentCleaning(){
        AtomicLong result = new AtomicLong();
        for (int i = 0; i < 1_00_000; i++) {
            int finalI = i;
            List<Integer> list = Arrays.asList(finalI, finalI + 1, finalI + 2, finalI + 3, finalI + 4);
            executorService.submit(() -> entityLocker.execute(new Random().nextInt(finalI + 1), result::incrementAndGet));
            executorService.submit(() -> entityLocker.execute(new Random().nextInt(finalI + 1), result::incrementAndGet));
            executorService.submit(() -> entityLocker.execute(new Random().nextInt(finalI + 1), result::incrementAndGet));
            executorService.submit(() -> entityLocker.execute(new Random().nextInt(finalI + 1), result::incrementAndGet));
            executorService.submit(() -> entityLocker.execute(new Random().nextInt(finalI + 1), result::incrementAndGet));
            executorService.submit(() -> entityLocker.execute(new Random().nextInt(finalI + 1), result::incrementAndGet));
            executorService.submit(() -> entityLocker.execute(new Random().nextInt(finalI + 1), result::incrementAndGet));
            executorService.submit(() -> entityLocker.execute(list, Comparator.naturalOrder(), result::incrementAndGet));
            executorService.submit(() -> entityLocker.tryLockAndExecute(finalI, result::incrementAndGet, 10, TimeUnit.SECONDS));
            executorService.submit(() -> entityLocker.tryLockAndExecute(list, Comparator.naturalOrder(), result::incrementAndGet, 10, TimeUnit.SECONDS));
            executorService.submit(() -> entityLocker.execute(result::incrementAndGet));
            executorService.submit(() -> entityLocker.tryLockAndExecute(result::incrementAndGet, 10, TimeUnit.SECONDS));
        }
        awaitFinish(executorService, 600);
        Assert.assertEquals(result.get(), 1_000_000);
    }
}
