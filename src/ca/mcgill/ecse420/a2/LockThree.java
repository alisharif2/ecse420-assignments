package ca.mcgill.ecse420.a2;

public class LockThree implements Lock {

  private int turn;
  private boolean busy;
  
  public static void main(String[] args) {
    System.out.println("LockThree test executing....");
    Tester t = new Tester(new LockThree(), 6);
    t.execute_test();
  }
  
  public LockThree() {
    busy = false;
  }

  @Override
  public void lock(int id) {
    turn = id;
    do
    {
      busy = true;
    } while(turn == id && busy);
  }

  @Override
  public void unlock(int id) {
    busy = false;
  }

}
