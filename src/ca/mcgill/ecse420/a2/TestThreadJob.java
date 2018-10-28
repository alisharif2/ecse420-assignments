package ca.mcgill.ecse420.a2;

import java.util.Random;

public class TestThreadJob implements Runnable {
  public static volatile boolean alive; // Used to end the run() method
  private static final int MAX_WAIT = 40;
  private int id; // Thread id
  private short penalty; // a higher value means sleeping for longer
  private Lock f;
  private Random r;
  private int csec_count; // how many times the thead entered its critical section
  
  public TestThreadJob(int id, Lock f) {
    alive = true;
    this.id = id;
    this.f = f;
    this.r = new Random();
    this.penalty = 1;
    this.csec_count = 0;
  }
  
  public TestThreadJob(int id, Lock f, short penalty) {
    this(id, f);
    this.penalty = penalty;
  }
  
  public int get_csec_count()
  {
    return csec_count;
  }

  @Override
  public void run() {
    while(alive) {
      try {
        f.lock(this.id);
        System.out.println(String.format("Thread %d has entered critical section", id));
        ++csec_count;
        Thread.sleep(r.nextInt(MAX_WAIT) * penalty + 1); // Do some processing inside critical section
      } catch (InterruptedException e) {
        e.printStackTrace();
      } finally {
        f.unlock(this.id);
      }
      
      try {
        Thread.sleep(r.nextInt(MAX_WAIT) * penalty + 1); // Do something outside critical section
      } catch (InterruptedException e) {
        e.printStackTrace();
      } 
      
    }
  }
}