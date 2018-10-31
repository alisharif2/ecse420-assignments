package ca.mcgill.ecse420.a2;

public class LockTwo implements Lock {

  private volatile int victim;
  
  public static void main(String[] args) {
    Tester t = new Tester(new LockTwo(), 2);
    t.execute_test();
  }

  @Override
  public void lock(int id) {
    // TODO Auto-generated method stub
    victim = id;
    while(victim == id) {};
  }

  @Override
  public void unlock(int id) {
    // TODO Auto-generated method stub
    // Does nothing
  }

}
