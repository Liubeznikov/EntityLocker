package com.deyneka.tools;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Interface for executing tasks with synchronization
 * @param <E> the type of entity ids
 * @author Aleksandr Deyneka
 */
public interface EntityLocker<E> {
    /**
     * Executes task after acquiring lock on entityId.
     * Task will be executed in isolation to other tasks with the same entityId.
     * @param entityId id of entity
     * @param task the task to execute
     */
    void execute(E entityId, Runnable task);

    /**
     * Tries to acquire the lock on entityId if lock is free within the given waiting time and executes task.
     * @param entityId id of entity
     * @param task the task to execute
     * @param timeout the maximum time to wait for the lock
     * @param unit the time unit of the {@code time} argument
     * @return {@code true} if the lock was acquired and {@code false}
     *         if the waiting time elapsed before the lock was acquired
     * @throws InterruptedException if the current thread is interrupted
     *         while acquiring the lock
     */
    boolean tryLockAndExecute(E entityId, Runnable task, long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * Executes task after acquiring lock on entityIds.
     * Needs to pass the same entityIdsComparator to prevent deadlocks.
     * @param entityIds ids of entities
     * @param entityIdsComparator comparator for sorting entityIds
     * @param task the task to execute
     */
    void execute(List<E> entityIds, Comparator<E> entityIdsComparator, Runnable task);

    /**
     * Tries to acquire the lock on entityIds if locks is free within the given waiting time and executes task.
     * Needs to pass the same entityIdsComparator to prevent deadlocks.
     * @param entityIds ids of entities
     * @param entityIdsComparator comparator for sorting entityIds
     * @param task the task to execute
     * @param timeout the maximum time to wait for the lock
     * @param unit the time unit of the {@code time} argument
     * @return {@code true} if the lock was acquired and {@code false}
     *         if the waiting time elapsed before the lock was acquired
     * @throws InterruptedException if the current thread is interrupted
     *         while acquiring the lock
     */
    boolean tryLockAndExecute(List<E> entityIds, Comparator<E> entityIdsComparator, Runnable task, long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * Executes task after acquiring global lock.
     * Task will be executed in isolation to other tasks.
     * @param task the task to execute
     */
    void execute(Runnable task);

    /**
     * Tries to acquire the global lock if lock is free within the given waiting time and executes task.
     * @param task the task to execute
     * @param timeout the maximum time to wait for the lock
     * @param unit the time unit of the {@code time} argument
     * @return {@code true} if the global lock was acquired and {@code false}
     *         if the waiting time elapsed before the global lock was acquired
     * @throws InterruptedException if the current thread is interrupted
     *         while acquiring the global lock
     */
    boolean tryLockAndExecute(Runnable task, long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * Removes inner data of EntityLocker
     * @throws UnsupportedOperationException if the <tt>clear</tt> operation
     *         is not supported by this EntityLocker
     */
    void clear();
}
