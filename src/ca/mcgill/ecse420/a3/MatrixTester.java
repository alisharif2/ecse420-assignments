package ca.mcgill.ecse420.a3;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MatrixTester {

  public static final int N_THREADS_DEFAULT = 4;
  private final static int TEST_RUNS = 10;
  
  public static void main(String[] args) {
    
    if(args.length == 0) {
      System.out.println("The arguments should be in the following form:");
      System.out.println("MatrixTester problem_size [num_threads]");
      return;
    }
    
    // Matrix dimensions
    int problem_size = Integer.parseInt(args[0]);
    
    int nThreads = N_THREADS_DEFAULT;
    if(args.length >= 2) {
      nThreads = Integer.parseInt(args[1]);
    }
    
    // Use the same executor for each multiplier
    nThreads = Math.max(0, nThreads);
    ExecutorService exec = Executors.newFixedThreadPool(nThreads);
    MatrixTask.exec = exec;
    SimpleParallelizedMultiplier.exec = exec;
    
    System.out.println("Problem Size: " + problem_size);
    System.out.println("Number of threads: " + nThreads);
    
    // Warm up JVM
    single_test(problem_size);
    
    int successful_runs = 0;
    double[][] results_table = new double[TEST_RUNS][];
    for(int i = 0;i < TEST_RUNS;++i) {
      results_table[i] = single_test(problem_size);
      // don't count run if no useful data generated
      if(results_table[i][0] > 0) {
        ++successful_runs;
      }
    }
    
    double par_avg = 0, seq_avg = 0;
    for(int i = 0;i < TEST_RUNS;++i) {
      par_avg += results_table[i][0];
      seq_avg += results_table[i][1];
    }    
    
    par_avg /= successful_runs;
    seq_avg /= successful_runs;
    
    System.out.println(String.format(
        "Average of %d parallel multiplication runs: %1.2f ns", successful_runs, par_avg));
    
    System.out.println(String.format(
        "Average of %d sequential multiplication runs: %1.2f ns", successful_runs, seq_avg));
        
    // Shutdown executor safely
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
    
  }
  
  // problem_size gives the matrix dimensions
  public static double[] single_test(int problem_size) {
    // Can easily subsitute any kind of matrix here
    SquareMatrix a = SquareMatrix.rand_gen(problem_size);
    Matrix b = ColumnVector.rand_gen(problem_size);

    Matrix parallel_result = null, sequential_result = null;
    
    // Sequential algorithm
    double start_time = System.nanoTime();
    sequential_result = Matrix.seq_mult(a, b);
    double end_time = System.nanoTime();
    double seq_benchmark = end_time - start_time;
    
    // Parallel Algorithm
    start_time = System.nanoTime();
    try {
      // can change which multiplier to use here
      parallel_result = SimpleParallelizedMultiplier.mult(a, b);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    end_time = System.nanoTime();
    double par_benchmark = end_time - start_time;
    
    double[] results;
    // Validate results using each other
    if(parallel_result.compare(sequential_result)) {
      results = new double[] {par_benchmark, seq_benchmark};
    }
    else {
      results = new double[] {0, 0}; // won't affect avg
      // so basically discards results
    }
    return results;
  }

}
