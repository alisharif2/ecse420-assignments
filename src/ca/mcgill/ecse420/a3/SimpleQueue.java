package ca.mcgill.ecse420.a3;

public interface SimpleQueue<T> {
  public void enq(T x);
  public T deq();
}
