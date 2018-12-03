package ca.mcgill.ecse420.a3;

import java.util.Random;

public class Matrix {
  public double[][] data;
  public int row_size = 0, col_size = 0;
  public int row_offset = 0, col_offset = 0;
  protected static final Random rand = new Random();

  
  public Matrix(int row_size, int col_size) {
    data = new double[row_size][col_size];
    this.row_size = row_size;
    this.col_size = col_size;
  }
  
  protected Matrix(double [][] matrix, int row_offset, int col_offset, int row_size, int col_size) {
    data = matrix;
    this.row_offset = row_offset;
    this.row_size = row_size;
    this.col_offset = col_offset;
    this.col_size = col_size;
  }
  
  public void set(int row, int col, double v) {
    data[row + row_offset][col + col_offset] = v;
  }
  
  public double get(int row, int col) {
    return data[row + row_offset][col + col_offset];
  }
  
  // doesn't have a body. please override
  public Matrix[][] split() throws Exception {
    throw new Exception();
  }
  
  public static Matrix rand_gen(int row_size, int col_size) {
    Matrix result = new Matrix(row_size, col_size);
    
    for(int i = 0;i < result.row_size;++i) {
      for(int j = 0;j < result.col_size;++j) {
        result.data[i][j] = 10 * rand.nextDouble();
      }
      }
    
    return result;
  }
  
  public static Matrix seq_mult(Matrix a, Matrix b) {
    if(a.col_size != b.row_size) return null;
        
    Matrix result = new Matrix(a.row_size, b.col_size);
    
    for(int i = 0;i < result.row_size;++i) {
      for(int j = 0;j < result.col_size;++j) {
        for(int k = 0;k < b.row_size;++k) {
          result.data[i][j] += a.data[i][k] * b.data[k][j];
        }
      }
    }
    
    return result;
  }
  
  public static Matrix seq_add(Matrix a, Matrix b) {
    if(a.row_size != b.row_size && b.col_size != a.col_size) return null;
    
    Matrix result = new Matrix(a.row_size, a.col_size);
    
    for(int i = 0;i < result.row_size;++i) {
      for(int j = 0;j < result.col_size;++j) {
        result.set(i, j, a.get(i, j) + b.get(i, j));
      }
    }
    
    
    return result;
  }
  
  @Override
  public String toString() {
    String str = "{ ";
    
    for(int i = 0;i < row_size;++i) {
      if(i == 0)
        str += "{ ";
      else 
        str += "  { ";
        
      for(int j = 0;j < col_size - 1;++j) {
        str = str +  String.format("%.2f", this.get(i, j))  + ", ";
      }
      str = str + String.format("%.2f", this.get(i, this.col_size -1)) + " }";
      if(i != row_size - 1) {
        str = str + ",\n";
      }
    }
    
    return str + " }";
  }

  public boolean compare(Matrix m) {
    if(this.col_size == m.col_size && this.row_size == m.row_size) {
      for(int i = 0;i < this.row_size;++i) {
        for(int j = 0;j < this.col_size;++j) {
          if(!compare_doubles(this.get(i, j), m.get(i, j))) return false;
        }
      }
      return true;
    }
    return false;
  }
  
  private boolean compare_doubles(double d1, double d2) {
    if(d1 == d2) return true;
    final double eps = 0.001; 
    return Math.abs(d1 - d2) < (eps * Math.max(Math.abs(d1), Math.abs(d2)));
  }
}
