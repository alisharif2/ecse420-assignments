package ca.mcgill.ecse420.a2;

public class Tester {
  
  private Lock f;
  private int n;
  public boolean use_penalty;
  
  public Tester(Lock f, int n) {
    this.f = f;
    this.n = n;
    this.use_penalty = false;
  }
  
  public void execute_test() {
    final long test_time = 1000;

    TestThreadJob.run_mutex_test();
    
    TestThreadJob jobs[] = new TestThreadJob[n];
    Thread[] t = new Thread[n];
    // Create thread objects to execute runnable jobs
    for(int i = 0;i < n;++i) {
      // Create each job with an execution penalty proportional to id
      if(use_penalty) jobs[i] = new TestThreadJob(i, f, (short)(i+1));
      else jobs[i] = new TestThreadJob(i, f);
      
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
    
    TestThreadJob.print_mutex_test_results();
    
  }
}
