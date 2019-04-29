package com.deyneka.tools;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.deyneka.tools.TestUtils.awaitFinish;

public class InteractiveTests {
    private EntityLocker<String> entityLocker;
    private ExecutorService executorService;

    @BeforeMethod
    public void initEntityLocker() {
        entityLocker = new ReentrantEntityLocker<>();
        executorService = Executors.newCachedThreadPool();
    }

    @Test
    public void testExecuteMethod(){
        final int[] runnableCounter = {0};
        for (int i = 0; i < 10; i++) {
            executorService.submit(() -> {
                Runnable runnable = () -> {
                    System.out.println("Entity1 runnable started " + (runnableCounter[0]) + " Thread = " + Thread.currentThread().getName());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println("Runnable task " + (runnableCounter[0]) + " interrupted");
                    }
                    System.out.println("Entity1 runnable finished " + (runnableCounter[0]));
                    runnableCounter[0]++;
                };
                entityLocker.execute("Entity1", runnable);
            });
        }
        awaitFinish(executorService, 20);
    }

    @Test
    public void testExecuteMethodComplex(){
        AtomicInteger accum = new AtomicInteger();
        List<String> log = new CopyOnWriteArrayList<>();
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 3; j++) {
                int finalJ = j;
                executorService.submit(() -> {
                    Runnable runnable = () -> {
                        int curAccum = accum.incrementAndGet();
                        log.add("Entity" + finalJ + " runnable started " + curAccum + " Thread = " + Thread.currentThread().getName());
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            log.add("Runnable task " + " interrupted");
                        }
                        log.add("Entity" + finalJ + " runnable finished " + curAccum);
                    };
                    entityLocker.execute("Entity" + finalJ, runnable);
                });
            }
        }
        awaitFinish(executorService, 100);
        log.forEach(System.out::println);
    }
}
