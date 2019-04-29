package com.deyneka.tools;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.deyneka.tools.TestUtils.awaitFinish;

public class ExecuteListMethodTest {
    private EntityLocker<Integer> entityLocker;
    private ExecutorService executorService;

    @BeforeMethod
    public void initEntityLocker() {
        entityLocker = new ReentrantEntityLocker<>();
        executorService = Executors.newCachedThreadPool();
    }

    @Test
    public void checkNullArguments(){
        Assert.assertThrows(IllegalArgumentException.class, () -> entityLocker.execute(null, null, null));
        Assert.assertThrows(IllegalArgumentException.class, () -> entityLocker.execute(null , Comparator.naturalOrder(),() -> {}));
        Assert.assertThrows(IllegalArgumentException.class, () -> entityLocker.execute(Collections.singletonList(1), null, () -> {}));
        Assert.assertThrows(IllegalArgumentException.class, () -> entityLocker.execute(Collections.singletonList(1), Comparator.naturalOrder(), null));
    }

    @Test
    public void checkExecuteEmptyList(){
        executorService.submit(() -> entityLocker.execute(Collections.emptyList(), Comparator.naturalOrder(), () -> {}));
        awaitFinish(executorService, 10);
    }

    @Test
    public void checkDeadLockSafetyReversed(){
        List<Integer> entityIds = IntStream.rangeClosed(0, 1000).boxed().collect(Collectors.toList());
        executorService.submit(() -> entityLocker.execute(entityIds, Comparator.naturalOrder(), () -> {}));
        Collections.reverse(entityIds);
        executorService.submit(() -> entityLocker.execute(entityIds, Comparator.naturalOrder(), () -> {}));
        awaitFinish(executorService, 10);
    }

    @Test
    public void checkDeadLockSafetyReversedComplex(){
        for (int i = 0; i < 1000; i++) {
            List<Integer> entityIds = IntStream.rangeClosed(i, 1000).boxed().collect(Collectors.toList());
            executorService.submit(() -> entityLocker.execute(entityIds, Comparator.naturalOrder(), () -> {}));
            Collections.reverse(entityIds);
            executorService.submit(() -> entityLocker.execute(entityIds, Comparator.naturalOrder(), () -> {}));
        }
        awaitFinish(executorService, 10);
    }

    @Test
    public void checkDeadLockSafetyIntersectedRanges(){
        List<Integer> entityIds1 = IntStream.rangeClosed(0, 1000).boxed().collect(Collectors.toList());
        executorService.submit(() -> entityLocker.execute(entityIds1, Comparator.naturalOrder(), () -> {}));
        List<Integer> entityIds2 = IntStream.rangeClosed(500, 1500).boxed().collect(Collectors.toList());
        executorService.submit(() -> entityLocker.execute(entityIds2, Comparator.naturalOrder(), () -> {}));
        awaitFinish(executorService, 10);
    }

    @Test
    public void checkAllLocksGetting(){
        executorService.submit(() -> entityLocker.execute(999,() -> {
            try {
                Thread.sleep(60_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
        List<Integer> entityIds = IntStream.rangeClosed(0, 1000).boxed().collect(Collectors.toList());
        executorService.submit(() -> entityLocker.execute(entityIds, Comparator.naturalOrder(), Assert::fail));
        awaitFinish(executorService, 5);
    }
}
