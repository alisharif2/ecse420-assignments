package ca.mcgill.ecse420.a3;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SimpleParallizedMultiplier {
  
  private static ExecutorService exec = Executors.newFixedThreadPool(MatrixTester.N_THREADS_DEFAULT);
  
  public static Matrix mult(SquareMatrix a, ColumnVector b) throws InterruptedException, ExecutionException {
    if(a.col_size != b.row_size) return null;
    ColumnVector c = new ColumnVector(b.row_size);
    
    Future<?>[] f = new Future<?>[a.row_size];
    for(int i = 0;i < a.row_size;++i) {
      f[i] = exec.submit(new Task(a, b, c, i));
    }
    
    for(int i = 0;i < a.row_size;++i) {
      f[i].get();
    }
    
    return c;
  }
  
  private static class Task implements Runnable {

    SquareMatrix a;
    ColumnVector b, c;
    int row;
    
    public Task(SquareMatrix a, ColumnVector b, ColumnVector c, int row) {
      this.a = a;
      this.b = b;
      this.c = c;
      this.row = row;
    }
    
    @Override
    public void run() {
      c.set(row, 0, 0);
      for(int i = 0;i < a.col_size;++i) {
        c.set(row, 0, c.get(row, 0) + a.get(row, i) * b.get(i, 0));
      }
    }
    
  }
  
  
  

}
