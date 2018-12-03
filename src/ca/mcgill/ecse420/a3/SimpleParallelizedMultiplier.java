package ca.mcgill.ecse420.a3;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class SimpleParallelizedMultiplier {

  public static ExecutorService exec = null;
  
  public static Matrix mult(Matrix a, Matrix b) throws InterruptedException, ExecutionException {
    if (a.col_size != b.row_size)
      return null;
    Matrix c = new Matrix(a.row_size, b.col_size);

    Future<?>[][] f = new Future<?>[a.row_size][b.col_size];
    for (int i = 0; i < a.row_size; ++i) {
      for (int j = 0; j < b.col_size; ++j) {
        f[i][j] = exec.submit(new Task(a, b, c, i, j));
      }
    }

    for (int i = 0; i < a.row_size; ++i) {
      for (int j = 0; j < b.col_size; ++j) {
        f[i][j].get();
      }
    }

    return c;
  }

  private static class Task implements Runnable {

    Matrix a, b, c;
    int row, col;

    public Task(Matrix a, Matrix b, Matrix c, int row, int col) {
      this.a = a;
      this.b = b;
      this.c = c;
      this.row = row;
      this.col = col;
    }

    @Override
    public void run() {
      c.set(row, col, 0);
      for (int i = 0; i < a.col_size; ++i) {
        c.set(row, col, c.get(row, col) + a.get(row, i) * b.get(i, col));
      }
    }

  }



}
