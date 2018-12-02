package ca.mcgill.ecse420.a3;

public class ColumnVector extends Matrix {

  public ColumnVector(int row_size) {
    super(row_size, 1);
  }

  public ColumnVector(double[][] matrix, int row_offset, int row_size) {
    super(matrix, row_offset, 0, row_size, 1);
  }
  
  public static ColumnVector rand_gen(int dim) {
    ColumnVector sm = new ColumnVector(dim);
    sm.data = Matrix.rand_gen(dim, 1).data;
    return sm;
  }
  
  @Override
  public ColumnVector[][] split() throws Exception {
    ColumnVector[][] result = new ColumnVector[2][1];
    if(row_size % 2 != 0) throw new Exception();
    int new_row_size = row_size/2;
    
    result[0][0] = new ColumnVector(data, row_offset, new_row_size);
    result[1][0] = new ColumnVector(data, row_offset + new_row_size, new_row_size);
    
    return result;
  }

}
