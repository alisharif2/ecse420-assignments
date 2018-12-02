package ca.mcgill.ecse420.a3;

import java.util.concurrent.ExecutionException;

public class MatrixTester {

  public static void main(String[] args) {
    SquareMatrix a = SquareMatrix.rand_gen(6);
    SquareMatrix b = SquareMatrix.rand_gen(6);

    Matrix c = null;

    try {
      c = MatrixTask.add(a, b);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ExecutionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    System.out.println(a);
    System.out.println("+");
    System.out.println(b);
    System.out.println("=");
    System.out.println(c);

    
  }

}
