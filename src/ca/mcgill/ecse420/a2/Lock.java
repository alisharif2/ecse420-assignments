package ca.mcgill.ecse420.a2;

public interface Lock {
  public void lock(int id);
  public void unlock(int id);
}
