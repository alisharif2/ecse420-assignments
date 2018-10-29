package ca.mcgill.ecse420.a2;

import java.util.Random;
import java.util.Stack;

public class TestThreadJob implements Runnable {
  public static volatile boolean alive; // Used to end the run() method
  private static final int MAX_WAIT = 20;
  private static Stack<Integer> s = new Stack<>();
  private int id; // Thread id
  private short penalty; // a higher value means sleeping for longer
  private Lock f;
  private Random r;
  private int csec_count; // how many times the thead entered its critical section
  private static boolean mutex_test = false;
  
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
  
  public static void run_mutex_test() {
    s.push(0);
    mutex_test = true;
  }
  
  public static void print_mutex_test_results() {
    if(!mutex_test && !alive) return;
    
    int j = 0;
    boolean fail = false;
    for(Integer i : s) {
      if(i != j++) {
        fail = true;
        break;
      }
    }
    if(fail) {
      System.out.println("Mutex Test Failed");
      System.out.println(s);
    }
    else {
      System.out.println("Mutex Test Passed");
    }
  }
  
  @Override
  public void run() {
    while(alive) {
      try {
        f.lock(this.id);
        //System.out.println(String.format("Thread %d has entered critical section", id));
        ++csec_count;
        int top = -1;
        if(mutex_test) top = s.peek();
        Thread.sleep(r.nextInt(MAX_WAIT) * penalty + 1); // Do some processing inside critical section
        if(mutex_test) s.push(top + 1);
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