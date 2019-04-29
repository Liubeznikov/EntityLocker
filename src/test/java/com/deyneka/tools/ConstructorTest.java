package com.deyneka.tools;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ConstructorTest {
    @Test
    public void checkNullArguments(){
        Assert.assertThrows(IllegalArgumentException.class, () -> new ReentrantEntityLocker<>());
        Assert.assertThrows(IllegalArgumentException.class, () -> new ReentrantEntityLocker<>());
        new ReentrantEntityLocker<>();
    }
}
