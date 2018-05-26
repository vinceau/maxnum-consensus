/*

Q1. In the programming language of your choice, implement a distributed algorithm for finding the maximum number between nodes.
- Nodes generate a random 64-bit number at start up, and must form a consensus about the maximum number across all nodes.
- Each node should be modeled as a thread (so pick a programming language that has threads).
- Nodes can only communicate with each other using message passing (so pick a programming language with channels, or model message passing using FIFO queues).
- Nodes can only communicate with 1/10th of the other nodes (you can decide how to configure them).
- Nodes should print out the maximum number when they have it. Each node can only print once. Not all nodes need to print the correct number, but the more, the better.
- The program should be a command-line tool, that accepts the number of nodes as an argument.
Focus on an implementation that achieves consensus consistently. Remember to check all concurrent data access for data-races.

 */

import java.util.Random;


class Node implements Runnable {
   private Thread t;
   private String threadName;
   
   Node( String name) {
      threadName = name;
      System.out.println("Creating " +  threadName );
   }
   
   public void run() {
      System.out.println("Running " +  threadName );
      try {
         for(int i = 4; i > 0; i--) {
            System.out.println("Thread: " + threadName + ", " + i);
            // Let the thread sleep for a while.
            Thread.sleep(50);
         }
      } catch (InterruptedException e) {
         System.out.println("Thread " +  threadName + " interrupted.");
      }
      System.out.println("Thread " +  threadName + " exiting.");
   }
   
   public void start () {
      System.out.println("Starting " +  threadName );
      if (t == null) {
         t = new Thread (this, threadName);
         t.start ();
      }
   }
}

public class Main {

   public static void main(String args[]) {
      int numNodes = 0;

      try {
         numNodes = Integer.parseInt(args[0]);  // raises NumberFormatException or IndexOutOfBoundsException
      } catch (NumberFormatException | IndexOutOfBoundsException | AssertionError e) {
         System.err.println("Number of nodes must be a number greater than 0.");
         System.exit(1);
      }

      if (numNodes <= 0) {
         System.err.println("Number of nodes must be a number greater than 0.");
         System.exit(1);
      };

      System.out.printf("Generating %d nodes\n", numNodes);

      Random rand = new Random();
      long n = new Random().nextLong();
      System.out.println(n);
      System.out.printf("%s arguments were passed in\n", args.length);
      for (String s: args) {
         System.out.println(s);
      }
      Node R1 = new Node( "Thread-1");
      R1.start();
      
      Node R2 = new Node( "Thread-2");
      R2.start();
   }   
}
