package ca.mcgill.ecse420.a2;

public class BakeryLock implements Lock {
  private int N;
  private volatile boolean[] flag;
  private volatile int[] label;

  public BakeryLock(int n) {
    this.N = n;
    this.flag = new boolean[n];
    this.label = new int[n];
    for (int i = 0; i < n; ++i) {
      flag[i] = false;
      label[i] = 0;
    }
  }

  private int max_label() {
    int max = label[0];
    for (int i = 0; i < N; ++i) {
      if (label[i] > max)
        max = label[i];
    }
    return max;
  }

  private boolean spin_lock_condition(int i) {
    for (int k = 0; k < N; ++k) {
      if (k != i && flag[k]) {
        if (label[k] < label[i])
          return true;
        else if (label[k] == label[i] && k < i)
          return true;
      }
    }
    return false;
  }

  @Override
  public void lock(int id) {
    flag[id] = true;
    label[id] = max_label() + 1;
    while (spin_lock_condition(id)) {
    }
  }

  @Override
  public void unlock(int id) {
    flag[id] = false;
  }


  public static void main(String[] args) {
    System.out.println("BakeryLock test executing....");
    Tester t = new Tester(new BakeryLock(6), 6);
    // Uncommentting this line makes each thread take different amounts of time
    // in their respective critical sections and outside
    t.use_penalty = true;
    t.execute_test();
  }
}
