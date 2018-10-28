package ca.mcgill.ecse420.a2;

import java.lang.Thread;

public class FilterLock implements Lock {

  private int N; // # of levels
  private volatile int[] level;
  private volatile int[] victim;

  public FilterLock(int n) {
    N = n;
    level = new int[n];
    victim = new int[n];
    for(int i = 0;i < n;++i) {
      level[i] = 0; 
    }
  }

  private boolean lvl_chk(int i, int j) {
    // check all threads
    for(int k = 0; k < N;++k) {
      // does a thread k want to enter level j?
      if(k != i && level[k] >= j) return true;
    }
    return false;
  }
  
  @Override
  public void lock(int id) {
    for(int i = 1;i < N; ++i) {
      level[id] = i; // set target level to i
      victim[i] = id; // wait for another thread to set its target level to i
      while(victim [i] == id && lvl_chk(id, i)) {}
    }
  }

  @Override
  public void unlock(int id) {
    level[id] = 0;
  }

  public static void main(String[] args) {
    final int n = 3; // problem size
    final long test_time = 10000;

    FilterLock f = new FilterLock(n);

    TestThreadJob jobs[] = new TestThreadJob[n];
    Thread[] t = new Thread[n];
    // Create thread objects to execute runnable jobs
    for(int i = 0;i < n;++i) {
      jobs[i] = new TestThreadJob(i, f, (short)(i+1));
      t[i] = new Thread(jobs[i]);
      t[i].start();
    }

    // Let the test run for a while
    try {
      Thread.sleep(test_time);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    TestThreadJob.alive = false; // end all threads
    for(int i = 0;i < n;++i) {
      try {
        t[i].join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    
    System.out.println("How many times each job entered it's critical section");
    for(int i = 0;i < n;++i) {
      System.out.println(String.format("Thread %d: %d", i, jobs[i].get_csec_count()));
    }

  }
}
