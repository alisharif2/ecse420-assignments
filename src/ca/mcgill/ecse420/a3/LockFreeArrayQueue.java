package ca.mcgill.ecse420.a3;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class LockFreeArrayQueue<T> implements SimpleQueue<T> {
  private AtomicInteger size, head, tail;
  private T[] store;
  private int capacity;

  @SuppressWarnings("unchecked")
  public LockFreeArrayQueue(int capacity) {
    this.capacity = capacity;
    head = new AtomicInteger(0);
    tail = new AtomicInteger(0);
    size = new AtomicInteger(0);

    store = (T[]) new Object[capacity];
  }

  public void enq(T x) {
    while(true) {
      int sz = size.get();
      int last = tail.get();
      if(last == tail.get()) {
        if(sz >= size.get()) {
          if(store[(last+1) % capacity] == null) {
            if(sz < capacity) {
              store[last % capacity] = x;
              size.incrementAndGet();
              tail.incrementAndGet();
              return;
            }
          }
          else {
            size.compareAndSet(sz, sz + 1);
            tail.compareAndSet(last, last + 1);
          }
        }
        else {
          tail.compareAndSet(last, last + 1);
        }
      }

    }
  }

  public T deq() {
    T result = null;
    while(true) {
      int sz = size.get();
      int first = head.get();
      if(first == head.get()) {
        if(sz <= size.get()) {
          if(store[first % capacity] != null) {
            result = store[first % capacity];
            store[first % capacity] = null;
            size.decrementAndGet();
            head.incrementAndGet();
            return result;
          }
          else {
            size.compareAndSet(sz, sz - 1);
            head.compareAndSet(first, first + 1);
          }
        }
        else {
          head.compareAndSet(first, first + 1);
        }
      }
    }
  }
}
