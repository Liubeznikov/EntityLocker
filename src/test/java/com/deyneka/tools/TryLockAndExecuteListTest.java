package com.deyneka.tools;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.deyneka.tools.TestUtils.awaitFinish;

public class TryLockAndExecuteListTest {
    private EntityLocker<Integer> entityLocker;
    private ExecutorService executorService;

    @BeforeMethod
    public void initEntityLocker() {
        entityLocker = new ReentrantEntityLocker<>();
        executorService = Executors.newCachedThreadPool();
    }

    @Test
    public void checkNullArguments() {
        Assert.assertThrows(IllegalArgumentException.class, () -> entityLocker.tryLockAndExecute(null, null, null, 0, null));
        Assert.assertThrows(IllegalArgumentException.class, () -> entityLocker.tryLockAndExecute(null, Comparator.naturalOrder(), () -> {},10, TimeUnit.SECONDS));
        Assert.assertThrows(IllegalArgumentException.class, () -> entityLocker.tryLockAndExecute(Collections.emptyList(), null, () -> {},10, TimeUnit.SECONDS));
        Assert.assertThrows(IllegalArgumentException.class, () -> entityLocker.tryLockAndExecute(Collections.emptyList(), Comparator.naturalOrder(), null,10, TimeUnit.SECONDS));
        Assert.assertThrows(IllegalArgumentException.class, () -> entityLocker.tryLockAndExecute(Collections.emptyList(), Comparator.naturalOrder(), () -> {},10, null));
    }

    @Test
    public void checkExecuteEmptyList(){
        executorService.submit(() -> entityLocker.tryLockAndExecute(Collections.emptyList(), Comparator.naturalOrder(), () -> {}, 5, TimeUnit.SECONDS));
        awaitFinish(executorService, 10);
    }

    @Test
    public void tryLockAndExecuteSimpleTest(){
        executorService.submit(() -> entityLocker.tryLockAndExecute(Arrays.asList(1,2,3,4,5), Comparator.naturalOrder(), () -> {}, 5, TimeUnit.SECONDS));
        awaitFinish(executorService, 10);
    }

    @Test
    public void checkDeadLockSafetyIntersectedRanges(){
        List<Integer> entityIds1 = IntStream.rangeClosed(0, 1000).boxed().collect(Collectors.toList());
        executorService.submit(() -> entityLocker.tryLockAndExecute(entityIds1, Comparator.naturalOrder(), () -> {}, 5, TimeUnit.SECONDS));
        List<Integer> entityIds2 = IntStream.rangeClosed(500, 1500).boxed().collect(Collectors.toList());
        executorService.submit(() -> entityLocker.tryLockAndExecute(entityIds2, Comparator.naturalOrder(), () -> {}, 5, TimeUnit.SECONDS));
        awaitFinish(executorService, 10);
    }
}
