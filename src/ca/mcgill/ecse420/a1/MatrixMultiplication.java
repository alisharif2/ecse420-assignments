package ca.mcgill.ecse420.a1;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MatrixMultiplication {
  private static final int NUMBER_THREADS = 2;
  private static final int MATRIX_SIZE = 1000;
  static double[][] a = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
  static double[][] b = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
  
  

      public static void main(String[] args) {
      
      // Generate two random matrices, same size
          
          long startTime = System.currentTimeMillis();
              double[][]  res1 = sequentialMultiplyMatrix(a, b);
          long endTime   = System.currentTimeMillis();
          
          long seqTime = (endTime-startTime)/1000;
          System.out.println("sequential time is " + (endTime-startTime));
          
           startTime = System.currentTimeMillis();
           double[][]  res2 =  parallelMultiplyMatrix(a, b);  
           endTime   = System.currentTimeMillis();
       System.out.println("parallel time is " + (endTime-startTime));
       
       System.out.println( seqTime < ((endTime-startTime)/1000));
       
       
       
       System.out.println("the same resumt = " + Arrays.deepToString(res1).equals(Arrays.deepToString(res2)));
          
  }
  
  /**
   * Returns the result of a sequential matrix multiplication
   * The two matrices are randomly generated
   * @param a is the first matrix
   * @param b is the second matrix
   * @return the result of the multiplication
   * */
  public static double[][] sequentialMultiplyMatrix(double[][] a, double[][] b) {
      if( !(a.length > 0 && b.length > 0) ){
          return null;       
     }     
     if( a[0].length != b.length) return null;
     
     double[][] sol = new double[a.length][b[0].length];  
     for(int i=0; i < a.length; i++){
         for(int j = 0; j < b[0].length; j++){
             for(int k = 0; k < a.length; k++){
                      sol[i][j] += a[i][k] * b[k][j];
             }
         }
     }

     return sol;
  }
  
  /**
   * Returns the result of a concurrent matrix multiplication
   * The two matrices are randomly generated
   * @param a is the first matrix
   * @param b is the second matrix
   * @return the result of the multiplication
   * */
      public static double[][] parallelMultiplyMatrix(double[][] a, double[][] b) {
          
         if( !(a.length > 0 && b.length > 0) ){
              return null;       
         }     
         if( a[0].length != b.length) return null;
          
          // more than one threads: multiprogramming
          
          double[][] sol = new double[a.length][b[0].length];  
          
          ExecutorService executor = Executors.newFixedThreadPool(NUMBER_THREADS);         
           for (int r = 0 ; r < a.length ; r++) {
                        executor.submit(new ParallelMultiply(a , b , sol , r ));                    
                }
                executor.shutdown();

                while (!executor.isTerminated()) {
                } 
          return sol;
  }
      
      /**
       * Populates a matrix of given size with randomly generated integers between 0-10.
       * @param numRows number of rows
       * @param numCols number of cols
       * @return matrix
       */
      private static double[][] generateRandomMatrix (int numRows, int numCols) {
           double matrix[][] = new double[numRows][numCols];
      for (int row = 0 ; row < numRows ; row++ ) {
          for (int col = 0 ; col < numCols ; col++ ) {
              matrix[row][col] = (double) ((int) (1+Math.random() * 2));
          }
      }
      return matrix;
  }
   
      
  private static final class ParallelMultiply extends Thread {
      private final double[][] a;
      private final double[][] b;
      private final double[][] result;
      private final int startRow;
      
      public ParallelMultiply(double[][] a, double[][] b, double[][] result, int startRow) {
          super();
          this.a = a;
          this.b = b;
          this.result = result;
          this.startRow = startRow;
      }
      
      @Override
      public  void run() {
              for(int x = 0; x < b[0].length; ++x) {
                  for(int i = 0; i < a[0].length; ++i) {
                    result[startRow][x]+= a[startRow][i] * b[i][x];
                  }       
              }
          }
  }  
	
}
