package ca.mcgill.ecse420.a3;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LockBasedArrayQueue<T> {

  private ReentrantLock enq_lock, deq_lock;
  private Condition not_empty, not_full;
  private AtomicInteger size, head, tail;
  private T[] store;
  private int capacity;

  @SuppressWarnings("unchecked")
  public LockBasedArrayQueue(int capacity) {
    this.capacity = capacity;
    head = new AtomicInteger(0);
    tail = new AtomicInteger(0);
    size = new AtomicInteger(0);
    enq_lock = new ReentrantLock();
    not_full = enq_lock.newCondition();
    deq_lock = new ReentrantLock();
    not_empty = deq_lock.newCondition();

    store = (T[]) new Object[capacity];
  }

  public void enq(T x) {
    boolean must_wake_dequeuers = false;
    enq_lock.lock();
    try {
      while (size.get() == capacity)
        not_full.await();

      store[tail.getAndIncrement() % capacity] = x;

      if (size.getAndIncrement() == 0) {
        must_wake_dequeuers = true;
      }
    } catch (InterruptedException e) {
      //e.printStackTrace();
    } finally {
      enq_lock.unlock();
    }

    if (must_wake_dequeuers) {
      deq_lock.lock();
      try {
        not_empty.signalAll();
      } finally {
        deq_lock.unlock();
      }
    }
  }

  public T deq() {
    T result = null;

    boolean must_wake_enqueuers = false;
    deq_lock.lock();
    try {
      while (size.get() == 0)
        not_empty.await();

      result = store[head.getAndIncrement() % capacity];

      if (size.getAndDecrement() == capacity) {
        must_wake_enqueuers = true;
      }

    } catch (InterruptedException e) {
      //e.printStackTrace();
    } finally {
      deq_lock.unlock();
    }

    if (must_wake_enqueuers) {
      enq_lock.lock();
      try {
        not_full.signalAll();
      } finally {
        enq_lock.unlock();
      }
    }

    return result;
  }

  public static void main(String[] args) {
    final int N_CONSUMERS = 2;
    final int N_PRODUCERS = 2;
    final int N_THREADS = N_CONSUMERS + N_PRODUCERS;
    final int SIZE = 100;
    final int N_TRANSFERS = 100;

    ExecutorService exec = Executors.newFixedThreadPool(N_THREADS);
    LockBasedArrayQueue<Integer> q = new LockBasedArrayQueue<>(SIZE);

    long start_time = System.currentTimeMillis();
    
    Future<?>[] producer_futures = new Future<?>[N_PRODUCERS];
    for (int i = 0; i < N_PRODUCERS; ++i) {
      producer_futures[i] = exec.submit(new ProducerTestThread(q, N_TRANSFERS));
    }

    ConsumerTestThread[] consumers = new ConsumerTestThread[N_CONSUMERS];
    for (int i = 0; i < N_CONSUMERS; ++i) {
      consumers[i] = new ConsumerTestThread(q);
      exec.submit(consumers[i]);
    }
    
    for(int i = 0;i < N_PRODUCERS;++i) {
      try {
        producer_futures[i].get();
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    }
    
    // Shutdown executor safely
    ConsumerTestThread.is_running = false;
    ProducerTestThread.is_running = false;
    exec.shutdown();
    try {
      if(!exec.awaitTermination(1000, TimeUnit.MILLISECONDS))
      {
        exec.shutdownNow();
        if(!exec.awaitTermination(1000, TimeUnit.MILLISECONDS))
        {
          System.err.println("Could not shtudown threads");
        }
      }
    } catch (InterruptedException e) {
      exec.shutdownNow();
      e.printStackTrace();
    }
    
    int total = 0;
    for(int i = 0;i < N_CONSUMERS;++i) {
      total += consumers[i].counter;
    }
    
    System.out.println("Total elements transfered: " + total);
    System.out.println("Time taken: " + (System.currentTimeMillis() - start_time) + " milliseconds");
    
  }

  public static class ProducerTestThread implements Runnable {

    public static volatile boolean is_running = true;
    private Random rand = new Random();
    private LockBasedArrayQueue<Integer> q;
    private int target;

    public ProducerTestThread(LockBasedArrayQueue<Integer> q, int target) {
      this.q = q;
      this.target = target;
    }

    @Override
    public void run() {
      while (is_running && target > 0) {
        q.enq(rand.nextInt());
        --target;
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }

  }

  private static class ConsumerTestThread implements Runnable {

    public static volatile boolean is_running = true;
    private LockBasedArrayQueue<Integer> q;
    private int counter = 0;

    public ConsumerTestThread(LockBasedArrayQueue<Integer> q) {
      this.q = q;
    }

    @Override
    public void run() {
      while (is_running) {
        if(q.deq() != null)
          ++counter;
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }

  }

}
