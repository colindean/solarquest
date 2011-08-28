// Solarquest
// Copyright (C) 2011 Colin Bartolome
// Licensed under the GPL. See LICENSE.txt for details.

package com.crappycomic.solarquest.model;

import java.io.Serializable;
import java.util.*;

public class Player implements Serializable
{
   private static final long serialVersionUID = 4758001876065372552L;

   private int number;
   
   private String name;
   
   private boolean gameOver;
   
   private int cash;
   
   private int fuelStations;
   
   private int fuel;
   
   private Node currentNode;
   
   private Set<Node> ownedNodes = new HashSet<Node>();
   
   private transient boolean ownedNodesRehashed;
   
   private Map<String, Integer> groupCounts = new HashMap<String, Integer>();
   
   public Player(int number)
   {
      this.number = number;
   }

   @Override
   public boolean equals(Object other)
   {
      return other instanceof Player && number == ((Player)other).number;
   }
   
   @Override
   public int hashCode()
   {
      return number;
   }
   
   public int getNumber()
   {
      return number;
   }
   
   public void setName(String name)
   {
      this.name = name;
   }
   
   public String getName()
   {
      return name;
   }
   
   void setGameOver(boolean gameOver)
   {
      this.gameOver = gameOver;
   }
   
   public boolean isGameOver()
   {
      return gameOver;
   }
   
   void changeCash(int amount)
   {
      cash += amount;
   }
   
   public int getCash()
   {
      return cash;
   }
   
   void changeFuelStations(int amount)
   {
      fuelStations += amount;
   }
   
   public int getFuelStations()
   {
      return fuelStations;
   }
   
   void changeFuel(int amount)
   {
      fuel += amount;
   }

   public int getFuel()
   {
      return fuel;
   }
   
   void setCurrentNode(Node node)
   {
      this.currentNode = node;
   }
   
   public Node getCurrentNode()
   {
      return currentNode;
   }
   
   void addNode(Node node)
   {
      if (ownedNodes.add(node))
         incrementGroupCount(node.getGroup());
   }
   
   void removeNode(Node node)
   {
      if (ownedNodes.remove(node))
         decrementGroupCount(node.getGroup());
   }
   
   public Set<Node> getOwnedNodes()
   {
      // This is pretty dumb, but it's a decent enough fix for Java's deserialization woes.
      // See bug #4957674 on Sun's site.
      if (!ownedNodesRehashed)
      {
         ownedNodes = new HashSet<Node>(ownedNodes);
         ownedNodesRehashed = true;
      }
      
      return Collections.unmodifiableSet(ownedNodes);
   }
   
   private void incrementGroupCount(String group)
   {
      if (groupCounts.containsKey(group))
         groupCounts.put(group, groupCounts.get(group) + 1);
      else
         groupCounts.put(group, 0);
   }
   
   private void decrementGroupCount(String group)
   {
      groupCounts.put(group, groupCounts.get(group) - 1);
   }
   
   public int getGroupCount(String group)
   {
      return groupCounts.get(group);
   }
   
   void fixGroupCounts()
   {
      groupCounts.clear();
      
      for (Node node : getOwnedNodes())
      {
         incrementGroupCount(node.getGroup());
      }
   }

   @Override
   public String toString()
   {
      return name;
   }
}
