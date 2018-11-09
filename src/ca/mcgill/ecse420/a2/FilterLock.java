package ca.mcgill.ecse420.a2;

public class FilterLock implements Lock {

  private int N; // # of levels
  private volatile int[] level;
  private volatile int[] victim;

  public FilterLock(int n) {
    N = n;
    level = new int[n];
    victim = new int[n];
    for (int i = 0; i < n; ++i) {
      level[i] = 0;
    }
  }

  private boolean lvl_chk(int i, int j) {
    // check all threads
    for (int k = 0; k < N; ++k) {
      // does a thread k want to enter level j?
      if (k != i && level[k] >= j)
        return true;
    }
    return false;
  }

  @Override
  public void lock(int id) {
    for (int i = 1; i < N; ++i) {
      level[id] = i; // set target level to i
      victim[i] = id; // wait for another thread to set its target level to i
      while (victim[i] == id && lvl_chk(id, i)) {
      }
    }
  }

  @Override
  public void unlock(int id) {
    level[id] = 0;
  }

  public static void main(String[] args) {
    System.out.println("FilterLock test executing....");
    Tester t = new Tester(new FilterLock(6), 6);
    // Uncommentting this line makes each thread take different amounts of time
    // in their respective critical sections and outside
    t.use_penalty = true;
    t.execute_test();
  }
}
