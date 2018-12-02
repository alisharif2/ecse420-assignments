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
  
  @Override
  public String toString() {
    String str = "";
    
    for(int i = 0;i < row_size;++i) {
      str += "[ ";
      for(int j = 0;j < col_size - 1;++j) {
        str = str +  String.format("%.2e", this.get(i, j))  + ", ";
      }
      str = str + String.format("%.2e", this.get(i, this.col_size -1)) + " ]\n";
    }
    
    return str;
  }
}
