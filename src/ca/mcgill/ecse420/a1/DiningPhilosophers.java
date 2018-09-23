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
    private static final int waitTime = 10;

    private static enum t_state {
      EATING, THINKING, ACQUIRING
    }
    
    // mutable members
    private Chopstick[] _chopsticks = {null, null};
    private int _nChopsticks = 0;
    private int _id;
    private t_state _currentState;
    private int _starvation; // 100 = dead

    public Philosopher(int id) {
      _starvation = 0;
      _id = id;
    }

    @Override
    public void run() {
      _currentState = t_state.THINKING;
      while (true) {
        try {
          
          Thread.sleep(waitTime);
          
          if(_starvation >= 100) {
            System.out.println(String.format("P%d has died", _id));
            return;
          }
          
          switch (_currentState) {
            case THINKING:
              if(_starvation > 80) _currentState = t_state.ACQUIRING;
              ++_starvation;
              break;
              
            case ACQUIRING:
              if(_nChopsticks == 2) _currentState = t_state.EATING;
              else {
                get_single_chopstick();
              }
              ++_starvation;
              break;
              
            case EATING:
              _starvation = 0;
              returnChopsticks();
              _currentState = t_state.THINKING;
              break;
              
            default:
              System.out.println("Invalid state");
              break;
          }

        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

    public boolean get_single_chopstick() throws InterruptedException {      
      System.out.println(String.format("P%d attempting to acquire a chopstick", _id));
      
      boolean ret = false;
      
      for(int i = 0; i < numberOfChopsticks;++i) {
        if (chopsticks[i].take(this)) {
          _chopsticks[_nChopsticks] = chopsticks[i];
          ++_nChopsticks;
          
          System.out.println(String.format("P%d acquired C%d", _id, chopsticks[i]._id));
          ret = true;
          break;
        }
      }
      
      if(!ret)
      {
        System.out.println(String.format("P%d could not acquire chopstick", _id));
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
