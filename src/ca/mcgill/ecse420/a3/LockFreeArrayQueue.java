package ca.mcgill.ecse420.a3;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class LockFreeArrayQueue<T> implements SimpleQueue<T> {
  private AtomicInteger size, head, tail;
  private AtomicReferenceArray<T> store;
  private int capacity;

  @SuppressWarnings("unchecked")
  public LockFreeArrayQueue(int capacity) {
    this.capacity = capacity;
    head = new AtomicInteger(0);
    tail = new AtomicInteger(0);
    size = new AtomicInteger(0);

    store = new AtomicReferenceArray<>(capacity);
  }

  public void enq(T x) {
    while(true) {
      int sz = size.get();
      int last = tail.get();
      if(last == tail.get()) {
        if(sz >= size.get()) {
          if(store.get((last+1) % capacity) == null) {
            if(sz < capacity) {
              if(store.compareAndSet(last % capacity, null, x)) {
                size.compareAndSet(sz, sz + 1);
                tail.compareAndSet(last, last + 1);
                return;
              }
            }
          }
          else {
            size.compareAndSet(sz, sz + 1);
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
          if(store.get(first % capacity) != null) {
            result = store.getAndSet(first % capacity, null);
            size.decrementAndGet();
            head.incrementAndGet();
            return result;
          }
          else {
            size.compareAndSet(sz, sz - 1);
          }
        }
        else {
          head.compareAndSet(first, first + 1);
        }
      }
    }
  }
  
  @Override
  public String toString() {
    String str = "";
    for(int i = 0;i < capacity;++i) {
      if(store.get(i) != null)
        str += store.get(i) + ", ";
    }
    return str;
  }
}
