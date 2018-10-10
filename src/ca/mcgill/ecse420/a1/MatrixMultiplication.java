package ca.mcgill.ecse420.a1;


import java.util.*;


public class MatrixMultiplication {
	
	private static  int NUMBER_THREADS = 1;
	private static final int MATRIX_SIZE = 1000;
	private static final int MINIMUM_WORK_LOAD = 10_000;

        public static void main(String[] args) {
		
		// Generate two random matrices, same size
    		double[][] a = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
    		double[][] b = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
    		sequentialMultiplyMatrix(a, b);
    		parallelMultiplyMatrix(a, b);	
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
           for(int j = 0; j < a[i].length; j++){
               double digit = a[i][j];
               if(digit == 0) continue;
               for(int k = 0; k < b[0].length; k++){
                   if(b[j][k] != 0)
                        sol[i][k] += b[j][k] * digit;
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
       // to test it on multiprocessor computers   
           // valid input
//        	NUMBER_THREADS = Runtime.getRuntime().availableProcessors();
//        	NUMBER_THREADS = Math.min(NUMBER_THREADS, (a.length * b.length * b[0].length)/MINIMUM_WORK_LOAD);
//    		NUMBER_THREADS = Math.min(NUMBER_THREADS, a.length);
//        	NUMBER_THREADS = Math.max(NUMBER_THREADS, 1);
//    		
//        	
//       	  if(NUMBER_THREADS == 1 ) {
//        		return sequentialMultiplyMatrix(a,b);
//        	}
        	
        	// more than one threads: multiprogramming
        	
        	double[][] sol = new double[a.length][b[0].length];  
        	ParallelMultiply[] threads = new ParallelMultiply[NUMBER_THREADS]; // maybe off by one
        	int rowsPerThread;
        	int startRow = 0;
        	
        	for(int i = 0; i < threads.length; ++i) {
        	    rowsPerThread = i < NUMBER_THREADS - 1 ? (a.length / NUMBER_THREADS) : (a.length / NUMBER_THREADS) + (a.length % NUMBER_THREADS);
        		threads[i] = new ParallelMultiply( a , b ,sol,startRow,rowsPerThread);
        		threads[i].start(); // maybe run or start: note explore in details
        		startRow += rowsPerThread;
        	}
        
        	
        	
        	for(ParallelMultiply th: threads) {
        		try {
        			th.join();
        		} catch (InterruptedException e){
        			 // give more explicit error message
        		}
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
                matrix[row][col] = (double) ((int) (Math.random() * 10.0));
            }
        }
        return matrix;
    }
     
        
    private static final class ParallelMultiply extends Thread {
    	private final double[][] a;
    	private final double[][] b;
    	private final double[][] result;
    	private final int startRow;
    	private final int row;
    	
    	public ParallelMultiply(double[][] a, double[][] b, double[][] result, int startRow, int row) {
    		super();
    		this.a = a;
    		this.b = b;
    		this.result = result;
    		this.startRow = startRow;
    		this.row = row;
    	}
    	
    	@Override
    	public  void run() {
    		for(int y = startRow; y < startRow + row; ++y) {
    			for(int x = 0; x < b[0].length; ++x) {
    				double sum = 0.0;
    				for(int i = 0; i < a[0].length; ++i) {
    					sum += a[y][i] * b[i][x];
    				}		
    				result[y][x] = sum;
    			}
    		}
    	}
    }
	
}
