package ca.mcgill.ecse420.a1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class DiningPhilosophers {
  public static int numberOfPhilosophers = 5;
  public static Chopstick[] chopsticks = new Chopstick[numberOfPhilosophers];


  public static void main(String[] args) {
    Philosopher[] philosophers = new Philosopher[numberOfPhilosophers];

    // create all objects
    for (int i = 0; i < numberOfPhilosophers; ++i) {
      philosophers[i] = new Philosopher(i);
      chopsticks[i] = new Chopstick(i);
    }

    // Start all philosphers
    ExecutorService executor = Executors.newFixedThreadPool(numberOfPhilosophers);
    for (int i = 0; i < numberOfPhilosophers; ++i) {
      executor.execute(philosophers[i]);
    }
  }

  public static class Philosopher implements Runnable {

    private Chopstick[] _chopsticks = {null, null};
    private int _nChopsticks = 0;
    private int _id;
    private static int waitTime = 10;

    private static enum State {
      EATING, THINKING
    }

    private State _currentState;

    public Philosopher(int id) {
      _id = id;
    }

    @Override
    public void run() {
      _currentState = State.THINKING;
      while (true) {
        try {
          changeState(State.EATING);
          Thread.sleep(waitTime); // enjoy the meal

          System.out.println(String.format("P%d EATEN", _id));

          changeState(State.THINKING);
          Thread.sleep(waitTime); // enjoy the mindscape

        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

    public void changeState(State s) throws InterruptedException {
      if (_currentState != s) {
        _currentState = s;
        if (s == State.EATING) {
          getChopsticks();
        } else {
          returnChopsticks();
        }
      }
    }

    public void getChopsticks() throws InterruptedException {
      int tmp = 0;
      
      System.out.println(String.format("P%d attempting to acquire chopsticks", _id));
      
      // attempt to reserve two chopsticks
      while (_nChopsticks < 2) {
        if (chopsticks[tmp].take(this)) {
          _chopsticks[_nChopsticks] = chopsticks[tmp];
          ++_nChopsticks;
          
          // Cannot grab chopstick instantly, so wait
          Thread.sleep(waitTime/10);
          
        }
        tmp = (++tmp) % numberOfPhilosophers;
      }
      
      System.out.println(String.format("P%d acquired chopsticks", _id));

    }

    public void returnChopsticks() {
      // Return both at once, so this can happen instantly
      for (int i = 0; i < 2; ++i) {
        if (_chopsticks[i] != null) {
          _chopsticks[i].leave(this);
          _chopsticks[i] = null;
          --_nChopsticks;
        }
      }
      System.out.println(String.format("P%d released chopsticks", _id));
    }

  }

  public static class Chopstick {
    private boolean reserved;
    private int _id;
    private Philosopher owner;

    public Chopstick(int id) {
      owner = null;
      reserved = false;
      _id = id;
    }

    public synchronized boolean take(Philosopher p) {
      boolean retVal;
      String err;
      String op = "TAKE";

      if (reserved) {
        err = "FAIL";
        retVal = false;
      } else {
        err = "GOOD";
        owner = p;
        reserved = true;
        retVal = true;
      }

      //System.out.println(String.format("P%d %s C%d %s, OWNER P%d", p._id, op, _id, err, owner._id));
      return retVal;
    }

    public synchronized boolean leave(Philosopher p) {
      String err;
      String op = "RETURN";
      boolean retVal;

      if (reserved) {
        err = "GOOD";
        reserved = false;
        owner = null;
        retVal = true;
      } else {
        err = "FAIL";
        retVal = false;
      }

      //System.out.println(String.format("P%d %s C%d %s", p._id, op, _id, err));
      return retVal;

    }

  }
}
