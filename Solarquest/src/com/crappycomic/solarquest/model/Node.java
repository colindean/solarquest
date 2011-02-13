// Solarquest
// Copyright (C) 2011 Colin Bartolome
// Licensed under the GPL. See LICENSE.txt for details.

package com.crappycomic.solarquest.model;

import java.io.Serializable;
import java.util.*;

/** A position on a {@link Board}. Nodes describe the various paths a {@link Player} can take. */
public class Node implements Comparable<Node>, Serializable
{
   private static final long serialVersionUID = -6994474778709552608L;

   /** Enumerates the possible types of nodes. */
   public static enum Type
   {
      SPACE,
      WELL_ORBIT,
      WELL_PULL,
      SOLID,
      DOCK,
      LAB,
      STATION
   }

   /** Unique identifier for this node. */
   private String id;
   
   /** Node index, as determined by the order in which this node was created. */
   private int index;

   /** The type of this node. */
   private Type type;

   /** True if this node is where players start on the board. Only one of these should exist per board. */
   private boolean startNode;

   /** True if this node has a fuel station on it. */
   private boolean fuelStation;

   /** List of nodes adjacent to this node in the normal direction of travel. */
   private List<Node> destinations = new ArrayList<Node>();

   /** Base price of this node (not including a fuel station, if present), or zero if this node may not be purchased. */
   private int price;

   /** Deed group to which this node belongs. */
   private String group;

   /** List of rents this node charges when landed upon, in ascending order, or null if this node may not be purchased. */
   private List<Integer> rents;

   /** List of fuel prices for this node, or null if fuel may never be purchased on this node. Length must equal that of {@link #rents}. */
   private List<Integer> fuels;
   
   /** Actions that take place upon landing on this node. */
   private List<Action> actions;

   /** The player that owns this node, or null if this node is unowned. */
   private Player owner;

   void setID(String id)
   {
      this.id = id;
   }

   public String getID()
   {
      return id;
   }
   
   void setIndex(int index)
   {
      this.index = index;
   }
   
   int getIndex()
   {
      return index;
   }

   void setType(Type type)
   {
      this.type = type;
   }
   
   public Type getType()
   {
      return type;
   }

   void setStartNode(boolean startNode)
   {
      this.startNode = startNode;
   }

   public boolean isStartNode()
   {
      return startNode;
   }

   void addDestination(Node destination)
   {
      destinations.add(destination);
   }

   public List<Node> getDestinations()
   {
      return Collections.unmodifiableList(destinations);
   }

   void setPrice(int price)
   {
      this.price = price;
   }
   
   public int getPrice()
   {
      return price;
   }

   void setGroup(String group)
   {
      this.group = group;
   }

   public String getGroup()
   {
      return group;
   }

   void setOwner(Player owner)
   {
      this.owner = owner;
   }
   
   public Player getOwner()
   {
      return owner;
   }

   void addRent(int rent)
   {
      if (rents == null)
         rents = new ArrayList<Integer>();

      rents.add(rent);
   }
   
   public List<Integer> getRents()
   {
      return rents == null ? null : Collections.unmodifiableList(rents);
   }

   void addFuel(int fuel)
   {
      if (fuels == null)
         fuels = new ArrayList<Integer>();

      fuels.add(fuel);
   }
   
   public List<Integer> getFuels()
   {
      return fuels == null ? null : Collections.unmodifiableList(fuels);
   }

   void addAction(Action action)
   {
      if (actions == null)
         actions = new ArrayList<Action>();
      
      actions.add(action);
   }

   public List<Action> getActions()
   {
      return actions == null ? null : Collections.unmodifiableList(actions);
   }
   
   @Override
   public String toString()
   {
      return id;
   }
   
   public String toDebugString()
   {
      StringBuilder out = new StringBuilder();

      out.append(id);

      if (startNode)
         out.append(" (start)");

      out.append(":");

      for (Node destination : destinations)
      {
         out.append("\t-> ");
         out.append(destination.id);
         out.append("  ");
      }
      
      if (actions != null)
      {
         for (Action action : actions)
         {
            out.append("\n\t");
            out.append(action.toString());
         }
      }

      if (price != 0)
      {
         out.append("\n\tPrice: ");
         out.append(price);
         out.append("\tGroup: ");
         out.append(group);
         out.append('\n');

         if (rents != null)
         {
            out.append("\tRent:");

            if (fuels != null)
               out.append("\tFuel:");

            out.append('\n');

            for (int ndx = 0; ndx < rents.size(); ndx++)
            {
               out.append('\t');
               out.append(rents.get(ndx));

               if (fuels != null)
               {
                  out.append('\t');
                  out.append(fuels.get(ndx));
               }

               out.append('\n');
            }
         }

         if (owner != null)
         {
            out.append("Owner: ");
            out.append(owner.toString());
            out.append('\n');
         }
      }

      return out.toString();
   }

   @Override
   public int compareTo(Node node)
   {
      return id.compareTo(node.id);
   }

   @Override
   public boolean equals(Object other)
   {
      if (!(other instanceof Node))
         return false;
      
      Node otherNode = (Node)other;
      
      if (id == null)
         return otherNode.id == null;
      
      return id.equals(otherNode.id);
   }

   @Override
   public int hashCode()
   {
      // Doing a null check here because deserialization is acting weird.
      return id == null ? 0 : id.hashCode();
   }
   
   /** Returns true if this node may <em>ever</em> be purchased. */
   public boolean canOwn()
   {
      return getPrice() > 0;
   }

   /** Returns true if this node may <em>currently</em> be purchased. */
   boolean isPurchaseable()
   {
      return getOwner() == null && canOwn();
   }

   /** Returns true if a {@link Player} may land on this node. */
   public boolean canLand()
   {
      switch (type)
      {
         case SPACE:
         case SOLID:
         case DOCK:
         case LAB:
         case STATION:
            return true;
         case WELL_ORBIT:
         case WELL_PULL:
         default:
            return false;
      }
   }

   public boolean hasFuelStation()
   {
      return fuelStation;
   }
   
   void setFuelStation(boolean fuelStation)
   {
      this.fuelStation = fuelStation;      
   }

   public boolean canHaveFuelStation()
   {
      return type == Type.SOLID && !startNode;
   }
   
   /**
    * Returns true if this node currently has fuel available. Returns false if
    * <code>fuelAvailableOnUnownedNode</code> is false and this node is unowned.
    */
   boolean hasFuel(boolean fuelAvailableOnUnownedNode)
   {
      if (startNode)
         return true;
      
      switch (type)
      {
         case SOLID:
            return fuelStation && (fuelAvailableOnUnownedNode || getOwner() != null);
         case DOCK:
            return fuelAvailableOnUnownedNode || getOwner() != null;
         case SPACE:
         case LAB:
         case STATION:
         case WELL_ORBIT:
         case WELL_PULL:
         default:
            return false;
      }
   }

   /** Returns true if fuel should be used when leaving this node. */
   public boolean usesFuel()
   {
      return type == Type.SOLID;
   }
   
   /** Returns price of one hydron of fuel, regardless of whether refuelling is actually allowed. */
   public int getFuelPrice(RuleSet ruleSet, Player player)
   {
      if (startNode)
         return ruleSet.getValue(RuleSet.FUEL_PRICE_ON_START);
      
      if (fuels == null)
         return 0;
      
      if (owner == null) // Purchase from league at cheapest price
         return fuels.get(0);
      
      if (owner.equals(player))
         return 0;
      
      return fuels.get(owner.getGroupCount(getGroup()));
   }
   
   /**
    * Returns the cost of landing on this node, depending on how many nodes in this node's deed
    * group are owned by the player who owns this node. Returns zero if this node is unowned.
    */
   public int getRent(Player player)
   {
      if (owner == null)
         return 0;
      
      if (owner.equals(player))
         return 0;
      
      return rents.get(owner.getGroupCount(getGroup()));
   }
}
