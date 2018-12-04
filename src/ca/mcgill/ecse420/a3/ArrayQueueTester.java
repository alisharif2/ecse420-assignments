package ca.mcgill.ecse420.a3;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ArrayQueueTester {

  public static void main(String[] args) {
    final int N_CONSUMERS = 2;
    final int N_PRODUCERS = 2;
    final int N_THREADS = N_CONSUMERS + N_PRODUCERS;
    final int SIZE = 100;
    final int N_TRANSFERS = 10;
    final int TEST_TIME = 1000;

    ExecutorService exec = Executors.newFixedThreadPool(N_THREADS);
    SimpleQueue<Integer> concurrent_queue = new LockFreeArrayQueue<>(SIZE);

    long start_time = System.currentTimeMillis();
    
    Future<?>[] producer_futures = new Future<?>[N_PRODUCERS];
    for (int i = 0; i < N_PRODUCERS; ++i) {
      producer_futures[i] = exec.submit(new ProducerTestThread(concurrent_queue, N_TRANSFERS));
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
    
    System.out.println(concurrent_queue.toString());

    ConsumerTestThread[] consumers = new ConsumerTestThread[N_CONSUMERS];
    for (int i = 0; i < N_CONSUMERS; ++i) {
      consumers[i] = new ConsumerTestThread(concurrent_queue);
      exec.submit(consumers[i]);
    }
    
    // let test run for a while
    try {
      Thread.sleep(TEST_TIME);
    } catch (InterruptedException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
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
    
    System.out.println("\nTotal elements transfered: " + total);
    System.out.println("Time taken: " + (System.currentTimeMillis() - start_time) + " milliseconds");
    
  }

  public static class ProducerTestThread implements Runnable {

    public static volatile boolean is_running = true;
    private Random rand = new Random();
    private SimpleQueue<Integer> q;
    private int target;

    public ProducerTestThread(SimpleQueue<Integer> q, int target) {
      this.q = q;
      this.target = target;
    }

    @Override
    public void run() {
      while (is_running && target > 0) {
        q.enq(rand.nextInt(10));
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
    private SimpleQueue<Integer> q;
    private int counter = 0;

    public ConsumerTestThread(SimpleQueue<Integer> q) {
      this.q = q;
    }

    @Override
    public void run() {
      Integer res = 0;
      while (is_running) {
        res = q.deq();
        if(res != null)
        {
          ++counter;
          System.out.print("" + res + ", ");
        }
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
