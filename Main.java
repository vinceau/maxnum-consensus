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

import java.util.*;


class Node implements Runnable {
   static final double reception = 0.1;

   private Thread t;
   private Integer idNum;              // our own node id
   private Integer totalNodes;         // total number of nodes that we have
   private Long nodeNum;               // the random number we generated
   private List<Integer> nearbyNodes;  // the nodes which we can send messages to
   
   Node(Integer name, Integer totalNodesNum, Long randNum) {
      this.idNum = name;
      this.totalNodes = totalNodesNum;
      this.nodeNum = randNum;
      this.nearbyNodes = getNearbyNodes();
   }
   
   public void run() {
      System.out.println("Running " +  idNum );
      try {
         for(int i = 4; i > 0; i--) {
            System.out.println("Thread: " + idNum + ", " + i);
            // Let the thread sleep for a while.
            Thread.sleep(50);
         }
      } catch (InterruptedException e) {
         System.out.println("Thread " +  idNum + " interrupted.");
      }
      System.out.println("Thread " +  idNum + " exiting.");
   }
   
   public void start () {
      System.out.println("Starting " +  idNum );
      if (t == null) {
         t = new Thread(this, idNum.toString());
         t.start ();
      }
   }

   /**
    * Returns a list of indicies indicating which nodes a node can talk to
    * this is not efficient for really long lists
    * since we're shuffling the whole list but only taking 10% of i
    */
   List<Integer> getNearbyNodes() {
         // have a list of all the ids so we can sample and shuffle them
         List<Integer> idNumList = new ArrayList<Integer>(totalNodes);
         for (int i = 0; i < totalNodes; i++) {
            if (i == idNum) continue;  // don't include self as one of the nearby nodes
            idNumList.add(i);
         }
         Collections.shuffle(idNumList);
         // assume that each node can talk to at least one other node
         return idNumList.subList(0, Math.max(1, (int) Math.floor(totalNodes * reception)));
   }
}

public class Main {

   public static void main(String args[]) {
      int numNodes = 0;

      try {
         numNodes = Integer.parseInt(args[0]);  // raises NumberFormatException or IndexOutOfBoundsException
      } catch (NumberFormatException | IndexOutOfBoundsException e) {
         System.err.println("Number of nodes must be a number greater than 0.");
         System.exit(1);
      }

      if (numNodes <= 0) {
         System.err.println("Number of nodes must be a number greater than 0.");
         System.exit(1);
      };

      // we're gonna store all the nodes here
      Node[] nodesList = new Node[numNodes];
      // keep track of what the true max is for debug purposes
      long actualMax = Long.MIN_VALUE;
      Random rand = new Random();

      for (int i = 0; i < numNodes; i++) {
         long n = rand.nextLong();
         System.out.println(n);
         nodesList[i] = new Node(i, numNodes, n);
         nodesList[i].start();
         if (n > actualMax) {
            actualMax = n;
         }
      }

      System.out.printf("Generated %d nodes, with max value %d\n", numNodes, actualMax);

   }
}
