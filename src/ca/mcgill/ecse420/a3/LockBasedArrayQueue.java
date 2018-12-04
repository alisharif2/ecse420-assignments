package ca.mcgill.ecse420.a3;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LockBasedArrayQueue<T> implements SimpleQueue<T> {

  private ReentrantLock enq_lock, deq_lock;
  private Condition not_empty, not_full;
  private AtomicInteger size, head, tail;
  private T[] store;
  private int capacity;

  @SuppressWarnings("unchecked")
  public LockBasedArrayQueue(int capacity) {
    this.capacity = capacity;
    head = new AtomicInteger(0);
    tail = new AtomicInteger(0);
    size = new AtomicInteger(0);
    enq_lock = new ReentrantLock();
    not_full = enq_lock.newCondition();
    deq_lock = new ReentrantLock();
    not_empty = deq_lock.newCondition();

    store = (T[]) new Object[capacity];
  }

  public void enq(T x) {
    boolean must_wake_dequeuers = false;
    enq_lock.lock();
    try {
      while (size.get() == capacity)
        not_full.await();

      store[tail.getAndIncrement() % capacity] = x;

      if (size.getAndIncrement() == 0) {
        must_wake_dequeuers = true;
      }
    } catch (InterruptedException e) {
      //e.printStackTrace();
    } finally {
      enq_lock.unlock();
    }

    if (must_wake_dequeuers) {
      deq_lock.lock();
      try {
        not_empty.signalAll();
      } finally {
        deq_lock.unlock();
      }
    }
  }

  public T deq() {
    T result = null;

    boolean must_wake_enqueuers = false;
    deq_lock.lock();
    try {
      while (size.get() == 0)
        not_empty.await();

      result = store[head.getAndIncrement() % capacity];

      if (size.getAndDecrement() == capacity) {
        must_wake_enqueuers = true;
      }

    } catch (InterruptedException e) {
      //e.printStackTrace();
    } finally {
      deq_lock.unlock();
    }

    if (must_wake_enqueuers) {
      enq_lock.lock();
      try {
        not_full.signalAll();
      } finally {
        enq_lock.unlock();
      }
    }

    return result;
  }
  
  @Override
  public String toString() {
    String str = "";
    for(int i = 0;i < capacity;++i) {
      if(store[i] != null)
        str += store[i] + ", ";
    }
    return str;
  }
}
