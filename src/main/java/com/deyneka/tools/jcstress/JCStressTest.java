package com.deyneka.tools.jcstress;

import com.deyneka.tools.ReentrantEntityLocker;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.L_Result;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;

@org.openjdk.jcstress.annotations.JCStressTest
@Outcome(id = "2", expect = ACCEPTABLE,  desc = "Both updates.")
@State
public class JCStressTest {
    private long value;
    private ReentrantEntityLocker<Integer> entityLocker = new ReentrantEntityLocker<>();

    @Actor
    public void actor1() {
        entityLocker.execute(1, () -> value++);
    }

    @Actor
    public void actor2() {
        entityLocker.execute(1, () -> value++);
    }

    @Arbiter
    public void arbiter(L_Result r) {
        r.r1 = value;
    }
}
