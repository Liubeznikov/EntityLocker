package com.deyneka.tools;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Reentrant implementation of the <tt>EntityLocker</tt> interface.
 * An instance of <tt>ReentrantEntityLocker</tt> has parameter: cleaning frequency.
 * The cleaning frequency is the number of executions before attempt of cleaning unused locks.
 *
 * @param <E> the type of entity ids
 * @author Aleksandr Deyneka
 */
//concurrenthashMap compute if epsen

public class ReentrantEntityLocker<E> implements EntityLocker<E> {

    Map<E,ReentrantLock> map = new ConcurrentHashMap<>();

    @Override
    public void execute(E entityId, Runnable task) {
        if(entityId == null ) throw new IllegalArgumentException();
        try {
            map.computeIfAbsent(entityId, key -> new ReentrantLock());
            map.get(entityId).lock();
            execute(task);
        }
        catch (IllegalArgumentException ex){throw ex;}
        finally {
            map.get(entityId).unlock();
        }
    }



    @Override
    public boolean tryLockAndExecute(E entityId, Runnable task, long timeout, TimeUnit unit) throws InterruptedException {
        if(entityId == null || timeout == 0 || unit == null ) throw new IllegalArgumentException();

            map.computeIfAbsent(entityId, key -> new ReentrantLock());
            if(map.get(entityId).tryLock(timeout,unit) ) {
                try {
                execute(task);
                }
                catch (IllegalArgumentException ex){throw ex;}
                finally {
                    map.get(entityId).unlock();
                }
                return true;
            }
            else{
                return false;
            }



    }

    @Override
    public void execute(List<E> entityIds, Comparator<E> entityIdsComparator, Runnable task) {
        if(entityIdsComparator == null || entityIds == null) throw new IllegalArgumentException();
        entityIds.sort(entityIdsComparator);
        try {
            for (E entityId : entityIds) {
                execute(entityId, task);
            }
        }
        catch (IllegalArgumentException ex){throw ex;}

    }

    @Override
    public boolean tryLockAndExecute(List<E> entityIds, Comparator<E> entityIdsComparator, Runnable task, long timeout, TimeUnit unit) throws InterruptedException {
        if (entityIds == null || entityIds.size() == 0) throw new IllegalArgumentException();

        entityIds.sort(entityIdsComparator);
        try {
            for (E entityId : entityIds) {
                if (!tryLockAndExecute(entityId, task,timeout,unit)) {
                    return false;
                }
                }
            }

        catch (IllegalArgumentException ex){throw ex;}
        return true;
    }



    @Override
    public void execute(Runnable task) {
        if (task == null) throw new IllegalArgumentException();
        task.run();
    }

    @Override
    public boolean tryLockAndExecute(Runnable task, long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void clear() {



    }
}

