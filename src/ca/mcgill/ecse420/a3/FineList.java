package ca.mcgill.ecse420.a3;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FineList<T> {

  private Node head;
  
  FineList() {
    head = new Node(Integer.MIN_VALUE);
    head.next = new Node(Integer.MAX_VALUE);
  }
  
  public boolean add(T item) {
    int key = item.hashCode();
    head.lock.lock();
    Node pred = head;
    try {
      Node curr = pred.next;
      curr.lock.lock();
      try {
        while(curr.key < key) {
          pred.lock.unlock();
          pred = curr;
          curr = curr.next;
          curr.lock.lock();
        }
        if(curr.key == key) {
          return false;
        }
        Node newNode = new Node(item);
        newNode.next = curr;
        pred.next = newNode;
        return true;
      } finally {
        curr.lock.unlock();
      }
    } finally {
      pred.lock.unlock();
    }
  }
  
  public boolean remove(T item) {
    Node pred = null, curr = null;
    int key = item.hashCode();
    head.lock.lock();
    try {
      pred = head;
      curr = pred.next;
      curr.lock.lock();
      try {
        while(curr.key < key) {
          pred.lock.unlock();
          pred = curr;
          curr = curr.next;
          curr.lock.lock();
        }
        if(curr.key == key) {
          pred.next = curr.next;
          return true;
        }
        return false;
      } finally {
        curr.lock.unlock();
      }
    } finally {
      pred.lock.unlock();
    }
  }
  
  public boolean contains(T item) {
    int key = item.hashCode();
    head.lock.lock();
    Node pred = head;
    try {
      Node curr = pred.next;
      curr.lock.lock();
      try {
        while(curr.key < key) {
          pred.lock.unlock();
          pred = curr;
          curr = curr.next;
          curr.lock.lock();
        }
        if(curr.key == key) {
          return true;
        }
        return false;
      } finally {
        curr.lock.unlock();
      }
    } finally {
      pred.lock.unlock();
    }  }
  
  @Override
  public String toString() {
    Node t = head;
    String str = "[H]->";
    while(t.next.next.next != null) {
      t = t.next;
      str = str + t.toString() + "->";
    }
    return str + t.next.toString();
  }  
  
  public static void main(String[] args) {
    final int problem_size = 4, test_time = 1000;
    ExecutorService thread_pool = Executors.newFixedThreadPool(problem_size);
    FineList<Integer> ls = new FineList<Integer>();
    
    for(int i = 0;i < problem_size;++i) {
      thread_pool.execute(new FineListTest(ls, i != 0));
    }
    
    // Let the test run for a while
    try {
      Thread.sleep(test_time);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    
    FineListTest.is_running = false;
    thread_pool.shutdown();
    try {
      if(!thread_pool.awaitTermination(1000, TimeUnit.MILLISECONDS))
      {
        thread_pool.shutdownNow();
        if(!thread_pool.awaitTermination(1000, TimeUnit.MILLISECONDS))
        {
          System.err.println("Could not shtudown threads");
        }
      }
    } catch (InterruptedException e) {
      thread_pool.shutdownNow();
      e.printStackTrace();
    }
    
    System.out.println(ls.toString());
    
  }
   
  
  private static class FineListTest implements Runnable {
    
    private FineList<Integer> ls;
    public static volatile boolean is_running = true;
    public boolean is_producer = true;
    
    FineListTest(FineList<Integer> ls, boolean is_producer) {
      this.ls = ls;
      this.is_producer = is_producer;
    }
    
    @Override
    public void run() {
      int i = 0;
      while(is_running) {
        if(is_producer) {
          ls.add(i);
        }
        else {
          if(ls.contains(i))
          {
            if(!ls.remove(i))
            {
              System.out.println("Tried to remove element that wasn't present");
            }
          }
        }
        i = (i+1) % 20;
      }
    }
  }
  
  private class Node {
    public T item;
    public int key;
    public Node next = null;
    public Lock lock = new ReentrantLock();
    
    public Node(T item) {
      this.item = item;
      this.key = item.hashCode();
    }
    
    public Node(int key) {
      this.key = key;
    }

    @Override
    public String toString() {
      return "[" + item.toString() + "]";
    }
  }

}
