package ca.mcgill.ecse420.a1;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("InfiniteLoopStatement")
	public class Runner3 {

		private Account acc1 = new Account(); 
		private Account acc2 = new Account(); 
		private Lock lock1 = new ReentrantLock(); 
		private Lock lock2 = new ReentrantLock();

//firstThread runs
	public void firstThread() throws InterruptedException { Random random = new Random();
		for (int i = 0; i < 10000; i++) {
			lock1.lock(); 
			lock2.lock(); 
		try {
				Account.transfer(acc1, acc2,random.nextInt(100)); 
		} finally { 
			lock1.unlock(); 
			lock2.unlock();
			}
		}
}

//SecondThread runs
	public void secondThread() throws InterruptedException { 
		Random random = new Random();
		
		for (int i = 0; i < 10000; i++) {
				lock2.lock(); 
				lock1.lock(); 
		try {
			Account.transfer(acc2, acc1, random.nextInt(100));
		} finally {
				lock1.unlock();
				lock2.unlock();
			}
		}
}

//When both threads finish execution, finished runs
	  public void finished() { 
	  		System.out.println("Account 1 balance: " + acc1.getBalance());
			System.out.println("Account 2 balance: " +acc2.getBalance());
			System.out.println("Total balance: " + (acc1.getBalance() + acc2.getBalance()));
			}
}
