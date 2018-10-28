package ca.mcgill.ecse420.a2;

import java.lang.Thread;

public class FilterLock {

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

  public void lock(int id) {
    for(int i = 1;i < N; ++i) {
      level[id] = i; // set target level to i
      victim[i] = id; // wait for another thread to set its target level to i
      while(victim [i] == id && lvl_chk(id, i)) {}
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

  public void unlock(int id) {
    level[id] = 0;
  }

  public static FilterLock f;

  public static class TestThread implements Runnable {
    public static volatile boolean alive; // Used to end the run() method
    private int id; // Thread id
    public TestThread(int id) {
      alive = true;
      this.id = id;
    }

    @Override
    public void run() {
      while(alive) {
        try {
          f.lock(this.id);
          System.out.println(String.format("Thread %d has entered critical section", id));
          Thread.sleep(10); // Do some processing
        } catch (InterruptedException e) {
          e.printStackTrace();
        } finally {
          f.unlock(this.id);
        }

      }

    }

  }

  public static void main(String[] args) {
    final int n = 3; // problem size

    f = new FilterLock(n);

    Thread[] t = new Thread[n];
    // Create thread objects to execute runnable
    for(int i = 0;i < n;++i) {
      t[i] = new Thread(new TestThread(i));
      t[i].start();
    }

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    TestThread.alive = false;

  }

}
