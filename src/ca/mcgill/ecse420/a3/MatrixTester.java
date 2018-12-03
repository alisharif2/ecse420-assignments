package ca.mcgill.ecse420.a3;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MatrixTester {

  public static final int N_THREADS_DEFAULT = 4;
  
  public static void main(String[] args) {
    
    if(args.length == 0) {
      System.out.println("The arguments should be in the following form:");
      System.out.println("MatrixTester problem_size [num_threads]");
      return;
    }
    
    int problem_size = Integer.parseInt(args[0]);
    
    int nThreads = N_THREADS_DEFAULT;
    if(args.length >= 2) {
      nThreads = Integer.parseInt(args[1]);
    }
    nThreads = Math.max(0, nThreads);
    MatrixTask.exec = Executors.newFixedThreadPool(nThreads);
    
    
    SquareMatrix a = SquareMatrix.rand_gen(problem_size);
    SquareMatrix b = SquareMatrix.rand_gen(problem_size);

    Matrix parallel_result = null, sequential_result = null;

    
    long start_time = System.currentTimeMillis();
    sequential_result = Matrix.seq_mult(a, b);
    long end_time = System.currentTimeMillis();
    System.out.println(String.format("Sequential Multiplication: %d", (end_time - start_time)));
    
    
    start_time = System.currentTimeMillis();
    try {
      parallel_result = MatrixTask.mult(a, b);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    end_time = System.currentTimeMillis();
    System.out.println(String.format("Parallel Multiplication: %d", (end_time - start_time)));
    
    System.out.println("Are they equal? " + parallel_result.compare(sequential_result));
    
    MatrixTask.exec.shutdown();
    try {
      if(!MatrixTask.exec.awaitTermination(1000, TimeUnit.MILLISECONDS))
      {
        MatrixTask.exec.shutdownNow();
        if(!MatrixTask.exec.awaitTermination(1000, TimeUnit.MILLISECONDS))
        {
          System.err.println("Could not shtudown threads");
        }
      }
    } catch (InterruptedException e) {
      MatrixTask.exec.shutdownNow();
      e.printStackTrace();
    }    
  }

}
