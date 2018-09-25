package ca.mcgill.ecse420.a1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class DiningPhilosophers {
  public static final int numberOfPhilosophers = 5;
  public static final int numberOfChopsticks = 5;
  public static final Chopstick[] chopsticks = new Chopstick[numberOfChopsticks];


  public static void main(String[] args) {
    Philosopher[] philosophers = new Philosopher[numberOfPhilosophers];

    // create all objects
    for (int i = 0; i < numberOfPhilosophers; ++i) {
      philosophers[i] = new Philosopher(i);
    }
    for (int i = 0; i < numberOfChopsticks; ++i) {
      chopsticks[i] = new Chopstick(i);
    }

    // Start all philosphers
    ExecutorService executor = Executors.newFixedThreadPool(numberOfPhilosophers);
    for (int i = 0; i < numberOfPhilosophers; ++i) {
      executor.execute(philosophers[i]);
    }

    executor.shutdown();

  }


  public static class Philosopher implements Runnable {

    // global constants

    // controls how fast they move and how quickly they starve
    private static final int waitTime = 10; // milliseconds
    private static final int _max_starvation = 100; // when they die
    private static final int _hunger = 80; // point they get hungry

    private static enum t_state {
      EATING, THINKING, ACQUIRING
    }

    // mutable members
    private Chopstick[] _chopsticks = {null, null};
    private int _nChopsticks;
    private int _id;
    private t_state _currentState;
    private int _starvation;
    
    private static final ReentrantLock mutex = new ReentrantLock();

    public Philosopher(int id) {
      _currentState = t_state.THINKING;
      _nChopsticks = 0;
      _starvation = 0;
      _id = id;
    }

    @Override
    public void run() {
      while (true) {
        try {
          Thread.sleep(waitTime);

        } catch (InterruptedException e) {
          e.printStackTrace();
          System.err.println(String.format("P%d thread could not sleep\nTerminating thread", _id));
          return;
        }

        if (_starvation >= _max_starvation) {
          System.out.println(String.format("P%d has died", _id));
          return;
        }

        switch (_currentState) {
          case THINKING:
            if (_starvation > _hunger) {
              _currentState = t_state.ACQUIRING;
            }
            ++_starvation;
            break;

          case ACQUIRING:
            // retrieve chopsticks slowly
            // 2 update ticks minimum
            if (_nChopsticks == 2) {
              mutex.unlock();
              _currentState = t_state.EATING;
            }
            else {
              if(mutex.tryLock()) {
                get_single_chopstick();
              }
            }
            ++_starvation;
            break;

          case EATING:
            // can't eat instantly
            if (_starvation <= 0) {
              // done eating
              _currentState = t_state.THINKING;
              returnChopsticks();
            } else {
              // keep eating
              --_starvation;
            }
            break;

          default:
            System.out.println("Invalid state");
            break;
        }


      }
    }

    public boolean get_single_chopstick() {
      System.out.println(String.format("P%d attempting to acquire a chopstick", _id));

      boolean ret = false;

      // only try to acquire a chopstick once
      // loop through all chopsticks and take any free one
      for (int i = 0; i < numberOfChopsticks; ++i) {
        if (chopsticks[i].take(this)) {
          _chopsticks[_nChopsticks] = chopsticks[i];
          ++_nChopsticks;

          System.out.println(String.format("P%d acquired C%d", _id, chopsticks[i]._id));
          ret = true;
          break;
        }
      }

      return ret;
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
    private Philosopher owner; // for debugging

    public Chopstick(int id) {
      owner = null;
      reserved = false;
      _id = id;
    }

    // prevents philosopher from reaching for a chopstick that is
    // in the middle of being locked by another
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
        // you can't return a chopstick you don't have
        err = "FAIL";
        retVal = false;
      }

      //System.out.println(String.format("P%d %s C%d %s", p._id, op, _id, err));
      return retVal;

    }

  }
}
