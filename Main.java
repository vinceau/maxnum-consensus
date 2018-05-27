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
import java.util.concurrent.*;


class Node implements Runnable {
   static final boolean debug = true;
   static final double reception = 0.1;

   private Thread t;
   private Integer idNum;              // our own node id
   private List<Integer> nearbyNodes;  // the nodes which we can send messages to
   private Node[] nodeList;
   public Long[] allValues;
   public int totalNodes;
   public BlockingQueue<Long[]> messageList;
   
   Node(Integer name, Long randNum, Node[] nodeList) {
      this.idNum = name;
      this.nodeList = nodeList;
      this.messageList = new LinkedBlockingQueue<Long[]>();
      // start keeping track of the node values
      this.totalNodes = nodeList.length;
      this.allValues = new Long[nodeList.length];
      this.allValues[name] = randNum;
      this.nearbyNodes = getNearbyNodes();
      if (debug) {
         System.out.printf("Node %d (%d) can talk to %s\n", this.idNum, randNum, this.nearbyNodes.toString());
      }
   }

   void updateDict(Long[] newValues) {
      if (debug) {
         System.out.printf("Node %d previous values: %s\n", this.idNum, Arrays.toString(this.allValues));
      }
      long currentMax = Long.MIN_VALUE;
      for (int i = 0; i < totalNodes; i++) {
         if (i == idNum) continue;  // we can update our own value tyvm
         if (newValues[i] == null) continue;
         // update our own dict if the new value is greater or our value is empty
         if (allValues[i] == null || allValues[i] < newValues[i]) {
            allValues[i] = newValues[i];
            if (currentMax < newValues[i]) {
               currentMax = newValues[i];
            }
         }
      }
      // should we update our own max value?
      if (allValues[idNum] < currentMax) {
         allValues[idNum] = currentMax;
      }
      if (debug) {
         System.out.printf("Node %d new values: %s\n", this.idNum, Arrays.toString(this.allValues));
      }
   }
   
   public void run() {
      try {
         while (true) {
            // for each of the nodes in nearbyNodes, send them our dictionary
            for (Integer n: nearbyNodes) {
               if (allValues[n] == null || allValues[n] < allValues[idNum]) {
                  this.nodeList[n].messageList.put(allValues);
                  if (debug) {
                     System.out.printf("Node %d -> %d: %s\n", this.idNum, n, Arrays.toString(this.allValues));
                  }
               }
            }
            // for each of the messages we receive update our local dictionary
            Long[] newValues = this.messageList.poll(4, TimeUnit.SECONDS);
            if (newValues == null) break; // the timeout was reached
            updateDict(newValues);
         }
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
      }
      System.out.printf("Node %d: %d\n", idNum, allValues[idNum]);
   }
   
   public void start () {
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
//       System.out.println(idNumList);
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

      // first declare and initialize them
      for (int i = 0; i < numNodes; i++) {
         long n = rand.nextLong();
//       System.out.println(n);
         nodesList[i] = new Node(i, n, nodesList);
         if (n > actualMax) {
            actualMax = n;
         }
      }

      // now start them all
      for (int i = 0; i < numNodes; i++) {
         nodesList[i].start();
      }

      System.out.printf("Generated %d nodes, with max value %d\n", numNodes, actualMax);

   }
}
