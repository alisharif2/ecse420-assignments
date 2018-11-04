package ca.mcgill.ecse420.a2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Tester {
  
  private Lock f;
  private int n;
  private final ExecutorService thread_pool;
  
  public boolean use_penalty;
  
  public Tester(Lock f, int n) {
    this.f = f;
    this.n = n;
    this.use_penalty = false;
    this.thread_pool = Executors.newFixedThreadPool(n);
  }
  
  public void execute_test() {
    final long test_time = 5000;

    TestThreadJob.run_mutex_test();
    
    TestThreadJob jobs[] = new TestThreadJob[n];
    
    // Create thread objects to execute runnable jobs
    for(int i = 0;i < n;++i) {
      // Create each job with an execution penalty proportional to id
      if(use_penalty) 
        jobs[i] = new TestThreadJob(i, f, (short)(i+1));
      else 
        jobs[i] = new TestThreadJob(i, f);
      
      thread_pool.execute(jobs[i]);
    }

    // Let the test run for a while
    try {
      Thread.sleep(test_time);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    TestThreadJob.alive = false; // end all threads
    thread_pool.shutdown();
    try {
      if(!thread_pool.awaitTermination(1000, TimeUnit.MILLISECONDS))
      {
        thread_pool.shutdownNow();
        if(!thread_pool.awaitTermination(1000, TimeUnit.MILLISECONDS))
        {
          System.err.println("Could not shtudown threads");
        }
      }
    } catch (InterruptedException e) {
      thread_pool.shutdownNow();
      e.printStackTrace();
    }
    
    
    System.out.println("How many times each job entered it's critical section");
    for(int i = 0;i < n;++i) {
      System.out.println(String.format("Thread %d: %d", i, jobs[i].get_csec_count()));
    }
    
    TestThreadJob.print_mutex_test_results();
    
  }
}
