package com.deyneka.tools;

import org.testng.Assert;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

class TestUtils {
    static void awaitFinish(ExecutorService executorService, int seconds) {
        executorService.shutdown();
        try {
            executorService.awaitTermination(seconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Assert.fail();
        }
    }
}
