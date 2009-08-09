package com.crappycomic.solarquest.model;

import java.io.Serializable;
import java.util.*;

public class Board implements Serializable
{
   private Map<String, Node> nodes = new TreeMap<String, Node>();

   private Node startNode;
   
   Collection<Node> getNodes()
   {
      return Collections.unmodifiableCollection(nodes.values());
   }
   
   void addNode(Node node)
   {
      node.setIndex(nodes.size());
      nodes.put(node.getID(), node);
   }

   void setStartNode(Node startNode)
   {
      this.startNode = startNode;
   }

   public Node getStartNode()
   {
      return startNode;
   }

   public String toString()
   {
      StringBuilder out = new StringBuilder();

      out.append("Nodes: ");
      out.append(nodes.size());
      out.append("\n\n");

      for (Map.Entry<String, Node> entry : nodes.entrySet())
      {
         out.append(entry.getValue());
         out.append("\n");
      }

      return out.toString();
   }

   public Node getNode(String nodeID)
   {
      return nodes.get(nodeID);
   }
   
   public boolean hasNode(String nodeID)
   {
      return nodes.containsKey(nodeID);
   }
   
   public Set<Node> getAllowedMoves(Node node, int distance)
   {
      Set<Node> allowedMoves = new TreeSet<Node>();
      Node lastBranch = null;
      int distanceRemainingAtLastBranch = 0;
      Node currentNode = node;
      List<Node> currentDestinations;
      int distanceRemaining = distance;
      
      while (true)
      {
         currentDestinations = currentNode.getDestinations();
         
         // Add currentNode if we're at our limit, or back up to lastBranch and pick other path.
         if (distanceRemaining <= 0)
         {
            // Can land here, so add currentNode.
            // Example: Any non-well node
            if (currentNode.canLand())
            {
               allowedMoves.add(currentNode);
               break;
            }
            // Can't land here and we didn't encounter any branches along our path. No move is allowed.
            // Example: Mars -> Well_Venus_1
            else if (lastBranch == null)
            {
               break;
            }
            
            // Can't land here, so back up to lastBranch and try the other path (i.e., orbit).
            // Example: Lab_Uranus, distance of 5
            currentNode = lastBranch.getDestinations().get(1);
            distanceRemaining = distanceRemainingAtLastBranch - 1;
            continue;
         }
         // Add all valid landing nodes if a branch is possible and we have one node left.
         // Example: Jupiter_Ganymede, distanceRemaining of 1
         else if (distanceRemaining == 1 && currentDestinations.size() > 1)
         {
            for (Node destination : currentDestinations)
               if (destination.canLand())
                  allowedMoves.add(destination);
            
            break;
         }
         // Add current node if next node will pull us back a space and we have one node left.
         // Example: Venus, distanceRemaining of 1
         else if (distanceRemaining == 1 && currentDestinations.get(0).getType() == Node.Type.WELL_PULL && currentNode.canLand())
         {
            allowedMoves.add(currentNode);
            
            break;
         }
         
         // Record the last branch, if any.
         if (currentDestinations.size() > 1)
         {
            lastBranch = currentNode;
            distanceRemainingAtLastBranch = distanceRemaining;
         }
         
         // Advance along the current path.
         currentNode = currentDestinations.get(0);
         distanceRemaining--;
      }

      return allowedMoves;
   }
   
   // Breadth-first, shortest path to destination wins
   public boolean passesStart(Node fromNode, Node toNode)
   {
      if (fromNode.isStartNode() || toNode.isStartNode())
         return false;
      
      List<List<Node>> paths = new ArrayList<List<Node>>();
      Set<Node> visitedNodes = new HashSet<Node>();
      List<Node> initialPath = new ArrayList<Node>();
      
      initialPath.add(fromNode);
      paths.add(initialPath);
      
      while (!paths.isEmpty())
      {
         List<Node> currentPath = paths.remove(0);
         Node currentNode = currentPath.get(currentPath.size() - 1);
         Collection<Node> destinations = currentNode.getDestinations();
         
         if (currentNode.equals(toNode))
            return currentPath.contains(startNode);
         
         visitedNodes.add(currentNode);

         for (Node destination : destinations)
         {
            if (!visitedNodes.contains(destination))
            {
               List<Node> newPath = new ArrayList<Node>(currentPath);
               
               newPath.add(destination);
               paths.add(newPath);
            }
         }
      }
      
      return false;
   }
   
   void initializeAdjacencyMatrix()
   {
      int[][] matrix = new int[nodes.size()][];
      
      for (int row = 0; row < matrix.length; row++)
      {
         matrix[row] = new int[matrix.length];
         Arrays.fill(matrix[row], -1);
         
         // Each node is 0 away from itself
         matrix[row][row] = 0;
      }
      
      for (Node from : nodes.values())
      {
         for (Node to : from.getDestinations())
         {
            matrix[from.getIndex()][to.getIndex()] = matrix[to.getIndex()][from.getIndex()] = 1;
         }
      }
      
      for (int dist = 1; dist < matrix.length; dist++)
      {
         for (int row = 0; row < matrix.length; row++)
         {
            for (int col = 0; col < matrix.length; col++)
            {
               if (matrix[row][col] == dist)
               {
                  for (int col2 = 0; col2 < matrix.length; col2++)
                  {
                     if (matrix[col][col2] == 1 && matrix[row][col2] == -1)
                        matrix[row][col2] = dist + 1;
                  }
               }
            }
         }
      }
      
//      printAdjacencyMatrix(matrix);
   }

   @SuppressWarnings("unused")
   private void printAdjacencyMatrix(int[][] matrix)
   {
      for (int row = 0; row < matrix.length; row++)
      {
         for (int col = 0; col < matrix.length; col++)
            System.out.print(matrix[row][col] + "\t");
         
         System.out.println();
      }
   }
}
