package ca.mcgill.ecse420.a3;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class LockFreeArrayQueue<T> implements SimpleQueue<T> {
  private AtomicInteger size, head, tail;
  private AtomicReferenceArray<T> store;
  private int capacity;

  public LockFreeArrayQueue(int capacity) {
    this.capacity = capacity;
    head = new AtomicInteger(0);
    tail = new AtomicInteger(0);
    size = new AtomicInteger(0);
    store = new AtomicReferenceArray<>(capacity);
  }

  public void enq(T x) {
    while (true) {
      int sz = size.get();
      int last = tail.get();
      if (last == tail.get()) {
        if (sz >= size.get()) {
          if (store.get((last) % capacity) == null) {
            if (sz < capacity) {
              // each of these lines can be interrupted so need special case to take care of
              // where each interrupt might happen
              if (store.compareAndSet(last % capacity, null, x)) {
                size.compareAndSet(sz, sz + 1);
                tail.compareAndSet(last, last + 1);
                return;
              }
            }
          } else {
            // the spot at tail isn't free but tail and size haven't been updated
            // help the other thread
            size.compareAndSet(sz, sz + 1);
            tail.compareAndSet(last, last + 1);
          }
        } else {
          // if size has changed since last seen then update tail if not already done so
          tail.compareAndSet(last, last + 1);
        }
      }

    }
  }

  // similar logic to enq
  public T deq() {
    T result = null;
    while (true) {
      int sz = size.get();
      int first = head.get();
      if (first == head.get()) {
        if (sz <= size.get()) {
          if (store.get(first % capacity) != null) {
            result = store.getAndSet(first % capacity, null);
            if (result != null) {
              size.compareAndSet(sz, sz - 1);
              head.compareAndSet(first, first + 1);
              return result;
            }
          } else {
            size.compareAndSet(sz, sz - 1);
            head.compareAndSet(first, first + 1);
          }
        } else {
          head.compareAndSet(first, first + 1);
        }
      }
    }
  }

  @Override
  public String toString() {
    String str = "";
    for (int i = 0; i < capacity; ++i) {
      if (store.get(i) != null)
        str += store.get(i) + ", ";
    }
    return str;
  }
}
