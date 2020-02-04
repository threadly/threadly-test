package org.threadly.test.concurrent;

import java.util.List;
import java.util.concurrent.Callable;

import org.threadly.concurrent.NoThreadScheduler;
import org.threadly.concurrent.PrioritySchedulerService;
import org.threadly.concurrent.TaskPriority;
import org.threadly.concurrent.future.ListenableFuture;
import org.threadly.util.Clock;
import org.threadly.util.ExceptionHandler;

/**
 * This differs from {@link org.threadly.concurrent.NoThreadScheduler} in that time is ONLY 
 * advanced via the tick calls.  That means that if you schedule a task, it will be scheduled off 
 * of either the creation time, or the last tick time, what ever the most recent point is.  This 
 * allows you to progress time forward faster than it could in real time, having tasks execute 
 * faster, etc, etc.
 * <p>
 * The tasks in this scheduler are only progressed forward with calls to {@link #tick()}.  Since 
 * it is running on the calling thread, calls to {@code Object.wait()} and {@code Thread.sleep()} 
 * from sub tasks will block (possibly forever).  The call to {@link #tick()} will not unblock 
 * till there is no more work for the scheduler to currently handle.
 * 
 * @since 1.0 (since 2.0.0 in threadly artifact)
 */
public class TestableScheduler implements PrioritySchedulerService {
  private final InternalScheduler scheduler;
  private long nowInMillis;
  
  /**
   * Constructs a new {@link TestableScheduler} scheduler.
   */
  public TestableScheduler() {
    this(null, 100);
  }
  
  /**
   * Constructs a new {@link TestableScheduler} scheduler.
   * 
   * @param defaultPriority Default priority for tasks which are submitted without any specified priority
   * @param maxWaitForLowPriorityInMs time low priority tasks to wait if there are high priority tasks ready to run
   */
  public TestableScheduler(TaskPriority defaultPriority, long maxWaitForLowPriorityInMs) {
    this.scheduler = new InternalScheduler(defaultPriority, maxWaitForLowPriorityInMs);
    this.nowInMillis = Clock.lastKnownTimeMillis();
  }

  @Override
  public boolean isShutdown() {
    return scheduler.isShutdown();
  }

  @Override
  public int getActiveTaskCount() {
    return scheduler.getActiveTaskCount();
  }

  @Override
  public int getQueuedTaskCount() {
    return scheduler.getQueuedTaskCount();
  }

  @Override
  public int getWaitingForExecutionTaskCount() {
    return scheduler.getWaitingForExecutionTaskCount();
  }

  @Override
  public TaskPriority getDefaultPriority() {
    return scheduler.getDefaultPriority();
  }

  @Override
  public long getMaxWaitForLowPriority() {
    return scheduler.getMaxWaitForLowPriority();
  }

  @Override
  public int getQueuedTaskCount(TaskPriority priority) {
    return scheduler.getQueuedTaskCount(priority);
  }

  @Override
  public int getWaitingForExecutionTaskCount(TaskPriority priority) {
    return scheduler.getWaitingForExecutionTaskCount(priority);
  }

  @Override
  public boolean remove(Runnable task) {
    return scheduler.remove(task);
  }

  @Override
  public boolean remove(Callable<?> task) {
    return scheduler.remove(task);
  }

  @Override
  public void execute(Runnable task) {
    scheduler.execute(task);
  }

  @Override
  public <T> ListenableFuture<T> submit(Callable<T> task) {
    return scheduler.submit(task);
  }

  @Override
  public <T> ListenableFuture<T> submit(Runnable task, T result) {
    return scheduler.submit(task, result);
  }

  @Override
  public void schedule(Runnable task, long delayInMs) {
    scheduler.schedule(task, delayInMs);
  }

  @Override
  public <T> ListenableFuture<T> submitScheduled(Callable<T> task, long delayInMs) {
    return scheduler.submitScheduled(task, delayInMs);
  }

  @Override
  public <T> ListenableFuture<T> submitScheduled(Runnable task, T result, long delayInMs) {
    return scheduler.submitScheduled(task, result, delayInMs);
  }

  @Override
  public void execute(Runnable task, TaskPriority priority) {
    scheduler.execute(task, priority);
  }

  @Override
  public <T> ListenableFuture<T> submit(Callable<T> task, TaskPriority priority) {
    return scheduler.submit(task, priority);
  }

  @Override
  public <T> ListenableFuture<T> submit(Runnable task, T result, TaskPriority priority) {
    return scheduler.submit(task, result, priority);
  }

  @Override
  public void schedule(Runnable task, long delayInMs, TaskPriority priority) {
    scheduler.schedule(task, delayInMs, priority);
  }

  @Override
  public <T> ListenableFuture<T> submitScheduled(Callable<T> task, long delayInMs, 
                                                 TaskPriority priority) {
    return scheduler.submitScheduled(task, delayInMs, priority);
  }

  @Override
  public <T> ListenableFuture<T> submitScheduled(Runnable task, T result, long delayInMs,
                                                 TaskPriority priority) {
    return scheduler.submitScheduled(task, result, delayInMs, priority);
  }

  @Override
  public void scheduleWithFixedDelay(Runnable task, long initialDelay, long recurringDelay) {
    scheduler.scheduleWithFixedDelay(task, initialDelay, recurringDelay);
  }

  @Override
  public void scheduleAtFixedRate(Runnable task, long initialDelay, long period) {
    scheduler.scheduleAtFixedRate(task, initialDelay, period);
  }

  @Override
  public void scheduleWithFixedDelay(Runnable task, long initialDelay, long recurringDelay,
                                     TaskPriority priority) {
    scheduler.scheduleWithFixedDelay(task, initialDelay, recurringDelay, priority);
  }

  @Override
  public void scheduleAtFixedRate(Runnable task, long initialDelay, long period,
                                  TaskPriority priority) {
    scheduler.scheduleAtFixedRate(task, initialDelay, period, priority);
  }
  
  /**
   * Returns the last provided time to the tick call.  If tick has not been called yet, then this 
   * will represent the time at construction.
   * 
   * @return last time the scheduler used for reference on execution
   */
  public long getLastTickTime() {
    return nowInMillis;
  }
  
  /**
   * This is to provide a convince when advancing the scheduler forward an explicit amount of time.  
   * Where tick accepts an absolute time, this accepts an amount of time to advance forward.  That 
   * way the user does not have to track the current time.
   * 
   * @param timeInMillis amount in milliseconds to advance the scheduler forward
   * @return quantity of tasks run during this tick call
   */
  public int advance(long timeInMillis) {
    return advance(timeInMillis, null);
  }
  
  /**
   * This is to provide a convince when advancing the scheduler forward an explicit amount of time.  
   * Where tick accepts an absolute time, this accepts an amount of time to advance forward.  That 
   * way the user does not have to track the current time.  
   * <p>
   * This call allows you to specify an {@link ExceptionHandler}.  If provided, if any tasks throw 
   * an exception, this will be called to inform them of the exception.  This allows you to ensure 
   * that you get a returned task count (meaning if provided, no exceptions except a possible 
   * {@link InterruptedException} can be thrown).  If {@code null} is provided for the exception 
   * handler, than any tasks which throw a {@link RuntimeException}, will throw out of this 
   * invocation.
   * 
   * @param timeInMillis amount in milliseconds to advance the scheduler forward
   * @param exceptionHandler Exception handler implementation to call if any tasks throw an 
   *                           exception, or null to have exceptions thrown out of this call
   * @return quantity of tasks run during this tick call
   */
  public int advance(long timeInMillis, ExceptionHandler exceptionHandler) {
    return tick(nowInMillis + timeInMillis, exceptionHandler);
  }
  
  /**
   * Progresses tasks for the current time.  This will block as it runs as many scheduled or 
   * waiting tasks as possible.  This call will NOT block if no task are currently ready to run.
   * <p>
   * If any tasks throw a {@link RuntimeException}, they will be bubbled up to this tick call.  
   * Any tasks past that task will not run till the next call to tick.  So it is important that 
   * the implementor handle those exceptions.  
   * 
   * @return quantity of tasks run during this tick call
   */
  public int tick() {
    return tick(null);
  }
  
  /**
   * Progresses tasks for the current time.  This will block as it runs as many scheduled or 
   * waiting tasks as possible.  This call will NOT block if no task are currently ready to run.  
   * <p>
   * This call allows you to specify an {@link ExceptionHandler}.  If provided, if any tasks throw 
   * an exception, this will be called to inform them of the exception.  This allows you to ensure 
   * that you get a returned task count (meaning if provided, no exceptions except a possible 
   * {@link InterruptedException} can be thrown).  If {@code null} is provided for the exception 
   * handler, than any tasks which throw a {@link RuntimeException}, will throw out of this 
   * invocation.
   * 
   * @param exceptionHandler Exception handler implementation to call if any tasks throw an 
   *                           exception, or null to have exceptions thrown out of this call
   * @return quantity of tasks run during this tick call
   */
  public int tick(ExceptionHandler exceptionHandler) {
    long currentRealTime = Clock.accurateTimeMillis();
    if (nowInMillis > currentRealTime) {
      return tick(nowInMillis, exceptionHandler);
    } else {
      return tick(currentRealTime, exceptionHandler);
    }
  }
  
  /**
   * This progresses tasks based off the time provided.  This is primarily used in testing by 
   * providing a possible time in the future (to execute future tasks).  This call will NOT block 
   * if no task are currently ready to run.  
   * <p>
   * If any tasks throw a {@link RuntimeException}, they will be bubbled up to this tick call.  
   * Any tasks past that task will not run till the next call to tick.  So it is important that 
   * the implementor handle those exceptions.
   * <p>
   * This call accepts the absolute time in milliseconds.  If you want to advance the scheduler a 
   * specific amount of time forward, look at the "advance" call.
   * 
   * @param currentTime Absolute time to provide for looking at task run time
   * @return quantity of tasks run in this tick call
   */
  public int tick(long currentTime) {
    return tick(currentTime, null);
  }
  
  /**
   * This progresses tasks based off the time provided.  This is primarily used in testing by 
   * providing a possible time in the future (to execute future tasks).  This call will NOT block 
   * if no task are currently ready to run.  
   * <p>
   * This call allows you to specify an {@link ExceptionHandler}.  If provided, if any tasks throw 
   * an exception, this will be called to inform them of the exception.  This allows you to ensure 
   * that you get a returned task count (meaning if provided, no exceptions except a possible 
   * {@link InterruptedException} can be thrown).  If {@code null} is provided for the exception 
   * handler, than any tasks which throw a {@link RuntimeException}, will throw out of this 
   * invocation.
   * <p>
   * This call accepts the absolute time in milliseconds.  If you want to advance the scheduler a 
   * specific amount of time forward, look at the "advance" call.
   * 
   * @param currentTime Absolute time to provide for looking at task run time
   * @param exceptionHandler Exception handler implementation to call if any tasks throw an 
   *                           exception, or null to have exceptions thrown out of this call
   * @return quantity of tasks run in this tick call
   */
  public int tick(long currentTime, ExceptionHandler exceptionHandler) {
    if (nowInMillis > currentTime) {
      throw new IllegalArgumentException("Time can not go backwards");
    }
    nowInMillis = currentTime;
    
    return scheduler.tick(exceptionHandler);
  }
  
  /**
   * Checks if there are tasks ready to be run on the scheduler.  If 
   * {@link #tick(ExceptionHandler)} is not currently being called, this call indicates if the 
   * next {@link #tick(ExceptionHandler)} will have at least one task to run.  If 
   * {@link #tick(ExceptionHandler)} is currently being invoked, this call will do a best attempt 
   * to indicate if there is at least one more task to run (not including the task which may 
   * currently be running).  It's a best attempt as it will try not to block the thread invoking 
   * {@link #tick(ExceptionHandler)} to prevent it from accepting additional work.
   *  
   * @return {@code true} if there are task waiting to run
   */
  public boolean hasTaskReadyToRun() {
    return scheduler.hasTaskReadyToRun();
  }
  
  /**
   * Checks how long till the next task will be ready to execute.  If there are no tasks in this 
   * scheduler currently then {@link Long#MAX_VALUE} will be returned.  If there is a task ready 
   * to execute this will return a value less than or equal to zero.  If the task is past its 
   * desired point of execution the result will be a negative amount of milliseconds past that 
   * point in time.  
   * <p>
   * Generally this is called from the same thread that would invoke 
   * {@link #tick(ExceptionHandler)} (but does not have to be).  Since this does not block or lock 
   * if being invoked in parallel with {@link #tick(ExceptionHandler)}, the results may be no 
   * longer accurate by the time this invocation has returned.
   * <p>
   * This can be useful if you want to know how long you can block on something, ASSUMING you can 
   * detect that something has been added to the scheduler, and interrupt that blocking in order 
   * to handle tasks.
   * 
   * @return Milliseconds till the next task is ready to run
   */
  public long getDelayTillNextTask() {
    return scheduler.getDelayTillNextTask();
  }
  
  /**
   * Removes any tasks waiting to be run.  Will not interrupt any tasks currently running if 
   * {@link #tick(ExceptionHandler)} is being called.  But will avoid additional tasks from being 
   * run on the current {@link #tick(ExceptionHandler)} call.  
   * <p>
   * If tasks are added concurrently during this invocation they may or may not be removed.
   * 
   * @return List of runnables which were waiting in the task queue to be executed (and were now removed)
   */
  public List<Runnable> clearTasks() {
    return scheduler.clearTasks();
  }
  
  /**
   * Small internal wrapper class so that we can control what from the "NoThreadScheduler" api's 
   * we want to expose from this implementation.
   * 
   * @since 1.0 (since 2.4.0 in threadly artifact)
   */
  protected class InternalScheduler extends NoThreadScheduler {
    public InternalScheduler(TaskPriority defaultPriority, long maxWaitForLowPriorityInMs) {
      super(defaultPriority, maxWaitForLowPriorityInMs);
    }

    @Override
    protected long nowInMillis(boolean accurate) {
      return nowInMillis;
    }
  }
}
