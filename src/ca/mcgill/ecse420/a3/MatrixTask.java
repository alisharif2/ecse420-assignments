package ca.mcgill.ecse420.a3;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MatrixTask {
  static ExecutorService exec = Executors.newCachedThreadPool();
  
  static Matrix add(Matrix a, Matrix b) throws InterruptedException, ExecutionException {
    if(a.col_size != b.col_size || a.row_size != b.row_size) return null;
    
    Matrix sum = null;
    if(a.col_size == a.row_size)
      sum = new SquareMatrix(a.row_size);
    else if(a.col_size == 1)
      sum = new ColumnVector(a.row_size);
    else
      sum = new Matrix(a.row_size, a.col_size);
    
    Future<?> future = exec.submit(new AddTask(a, b, sum));
    future.get();
    
    return sum;
  }
  
  private static class AddTask implements Runnable {
    Matrix a, b, c;

    public AddTask(Matrix a, Matrix b, Matrix c) {
      this.a = a;
      this.b = b;
      this.c = c;
    }
    
    @Override
    public void run() {
      try {
        // a, b, and c have the same dimensions
        int row_size = a.row_size;
        int col_size = a.col_size;
        
        if(row_size == 1 && col_size == 1) {
          c.set(0, 0, a.get(0, 0) + b.get(0, 0));
        } else {
          Matrix[][] aa = a.split(), bb = b.split(), cc = c.split();

          int futures_x = 1, futures_y = 1;
          if(c.row_size >= 2) futures_x = 2;
          if(c.col_size >= 2) futures_y = 2;
          
          Future<?>[][] futures = (Future<?>[][]) new Future[futures_x][futures_y];
          
          for(int i = 0;i < futures_x;++i) {
            for(int j = 0;j < futures_y;++j) {
              futures[i][j] = exec.submit(new AddTask(aa[i][j], bb[i][j], cc[i][j]));
            }
          }
          
          for(int i = 0;i < futures_x;++i) {
            for(int j = 0;j < futures_y;++j) {
              futures[i][j].get();
            }
          }
          
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

}
