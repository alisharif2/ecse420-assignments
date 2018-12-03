package ca.mcgill.ecse420.a3;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class MatrixTask {
  public static ExecutorService exec = null;
  
  private static final int MIN_SIZE = 100;
  
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
    
    private void in_place_addition(Matrix a, Matrix b, Matrix c) throws Exception {
      if(a.row_size != b.row_size && b.col_size != a.col_size) throw new Exception();
      
      for(int i = 0;i < c.row_size;++i) {
        for(int j = 0;j < c.col_size;++j) {
          c.set(i, j, a.get(i, j) + b.get(i, j));
        }
      }
    }
    
    @Override
    public void run() {
      try {
        // a, b, and c have the same dimensions
        int row_size = a.row_size;
        int col_size = a.col_size;
        
        if(row_size <= MIN_SIZE && col_size <= MIN_SIZE) {
          in_place_addition(a, b, c);
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

  static Matrix mult(Matrix a, Matrix b) throws InterruptedException, ExecutionException {
    if(a.col_size != b.row_size) return null;
    
    Matrix product = null;
    if(b.row_size == b.col_size)
      product = new SquareMatrix(b.row_size);
    else if(b.col_size == 1) 
      product = new ColumnVector(b.row_size);
    else
      product = new Matrix(a.row_size, b.col_size);
    
    Future<?> future = exec.submit(new MultTask(a, b, product));
    future.get();
    
    return product;
  }
  
  private static class MultTask implements Runnable {
    Matrix a, b, c, lhs, rhs;
    public MultTask(Matrix a, Matrix b, Matrix c) {
      this.a = a;
      this.b = b;
      this.c = c;
      
      if(b.col_size == 1) {
        lhs = new ColumnVector(b.row_size);
        rhs = new ColumnVector(b.row_size);
      }
      else if(b.row_size == b.col_size) {
        lhs = new SquareMatrix(b.row_size);
        rhs = new SquareMatrix(b.row_size);
      }
      else {
        lhs = new Matrix(a.row_size, b.col_size);
        rhs = new Matrix(a.row_size, b.col_size);
      }
    }
    
    private void in_place_mult(Matrix a, Matrix b, Matrix c) {
      if(a.col_size != b.row_size) return;
            
      for(int i = 0;i < c.row_size;++i) {
        for(int j = 0;j < c.col_size;++j) {
          for(int k = 0;k < b.row_size;++k) {
            c.set(i, j, c.get(i, j) + a.get(i, k) * b.get(k, j));
          }
        }
      }
    }
    
    @Override
    public void run() {
      try {
        if(a.row_size <= MIN_SIZE && b.row_size <= MIN_SIZE) {
          in_place_mult(a, b, c);
        } else {
          Matrix[][] aa = a.split(), bb = b.split(), ll = lhs.split(), rr = rhs.split();
          
          int futures_x = 1, futures_y = 1;
          if(c.row_size >= 2) futures_x = 2;
          if(c.col_size >= 2) futures_y = 2;
          
          Future<?>[][][] futures = (Future<?>[][][]) new Future[futures_x][futures_y][2];
          
          for(int i = 0;i < futures_x;++i) {
            for(int j = 0;j < futures_y;++j) {
              futures[i][j][0] = exec.submit(new MultTask(aa[i][0], bb[0][j], ll[i][j]));
              futures[i][j][1] = exec.submit(new MultTask(aa[i][1], bb[1][j], rr[i][j]));
            }
          }
          
          for(int i = 0;i < futures_x;++i) {
            for(int j = 0;j < futures_y;++j) {
              for(int k = 0;k < 2;++k) {
                futures[i][j][k].get();
              }
            }
          }
          
          Future<?> done = exec.submit(new AddTask(lhs, rhs, c));
          done.get();
          
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
  }
  
}
