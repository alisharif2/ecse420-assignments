package ca.mcgill.ecse420.a2;

public class LockOne implements Lock {

  private volatile boolean[] flag = new boolean[2];
  
  public static void main(String[] args) {
    Tester t = new Tester(new LockOne(), 2);
    t.execute_test();
  }

  @Override
  public void lock(int id) {
    flag[id] = true;
    while(flag[1 - id]) {}
  }

  @Override
  public void unlock(int id) {
    flag[id] = false;
  }

}
