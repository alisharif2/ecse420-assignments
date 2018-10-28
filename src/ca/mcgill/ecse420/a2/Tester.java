package ca.mcgill.ecse420.a2;

public class Tester {
  
  private Lock f;
  private int n;
  
  public Tester(Lock f, int n) {
    this.f = f;
    this.n = n;
  }
  
  public void run() {
    final long test_time = 1000;

    TestThreadJob jobs[] = new TestThreadJob[n];
    Thread[] t = new Thread[n];
    // Create thread objects to execute runnable jobs
    for(int i = 0;i < n;++i) {
      // Create each job with an execution penalty proportional to id
      jobs[i] = new TestThreadJob(i, f, (short)(i+1));
      t[i] = new Thread(jobs[i]); // create thread objects to execute the jobs
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
        t[i].join(); // make sure all jobs have ended
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
