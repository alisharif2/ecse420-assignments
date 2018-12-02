package ca.mcgill.ecse420.a3;

public class SquareMatrix extends Matrix {

  private int dim;
  
  public SquareMatrix(int dim) {
    super(dim, dim);
    this.dim = dim;
  }
  
  public SquareMatrix(double[][] data, int row_offset, int col_offset, int dim) {
    super(data, row_offset, col_offset, dim, dim);
    this.dim = dim;
  }
  
  public static SquareMatrix rand_gen(int dim) {
    SquareMatrix sm = new SquareMatrix(dim);
    sm.data = Matrix.rand_gen(dim, dim).data;
    return sm;
  }
  
  @Override
  public SquareMatrix[][] split() throws Exception {
    SquareMatrix[][] result = new SquareMatrix[2][2];
    if(dim % 2 != 0) throw new Exception();
    int new_dim = dim/2;
    
    result[0][0] = new SquareMatrix(data, row_offset, col_offset, new_dim);
    result[0][1] = new SquareMatrix(data, row_offset, col_offset + new_dim, new_dim);
    result[1][0] = new SquareMatrix(data, row_offset + new_dim, col_offset, new_dim);
    result[1][1] = new SquareMatrix(data, row_offset + new_dim, col_offset + new_dim, new_dim);
    
    return result;
  }

}
