package ca.mcgill.ecse420.a1;



import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("InfiniteLoopStatement")
  public class Runner1 { 

	private Account acc1 = new Account(); 
	private Account acc2 = new Account(); 
	private Lock lock1 = new ReentrantLock();
	private Lock lock2 = new ReentrantLock();

//firstThread runs
  public void firstThread() throws InterruptedException { Random random = new Random();
  		for (int i = 0; i < 10000; i++) {
  			
  			Account.transfer(acc1, acc2, random.nextInt(100));
  		}
}

//SecondThread runs
  public void secondThread() throws InterruptedException { 
	Random random = new Random();
			for (int i = 0; i < 10000; i++) { Account.transfer(acc2, acc1,
															random.nextInt(100));
			}
  }

//When both threads finish execution, finished runs
  public void finished() { 
	  		System.out.println("Account 1 balance: " + acc1.getBalance());
			System.out.println("Account 2 balance: " +acc2.getBalance());
			System.out.println("Total balance: " + (acc1.getBalance() + acc2.getBalance()));
  }
}
