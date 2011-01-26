// Solarquest
// Copyright (C) 2011 Colin Bartolome
// Licensed under the GPL. See LICENSE.txt for details.

package com.crappycomic.solarquest.model;

import java.io.Serializable;

/**
 * Represents a change that landing on a certain {@link Node} or drawing a certain
 * {@link Card} can impart on a {@link Player}.
 */
public class Action implements Serializable
{
   private static final long serialVersionUID = 9184933106556150564L;

   /** Enumerates the possible types of actions. */
   public enum Type
   {
      COLLECT_CASH(Integer.class),
      COLLECT_FUEL_STATION(Integer.class),
      ADVANCE(String.class),
      USE_FUEL(Integer.class),
      ROLL_WITH_MULTIPLIER(Integer.class),
      ROLL_AGAIN(null),
      LOSE_DISPUTE_LEAGUE(null),
      WIN_DISPUTE_LEAGUE(null),
      WIN_DISPUTE_PLAYER(null);
      
      private Class<?> valueType;
      
      private Type(Class<?> valueType)
      {
         this.valueType = valueType;
      }
   }
   
   /** The specific type of action to be performed. */
   private Type type;
   
   /**
    * The type-dependant modifier for this specific action. For example, the number
    * of hydrons of fuel, or the {@link Node node} to be advanced to.
    */
   private Object value;
   
   /** Creates an instance with the given type and {@link Integer integer} or {@link String string} value. */
   Action(Type type, String value)
   {
      this.type = type;
      
      if (this.type.valueType != null)
      {
         if (this.type.valueType.equals(Integer.class))
            this.value = new Integer(value);
         else if (this.type.valueType.equals(String.class))
            this.value = value;
      }
   }
   
   /**
    * Instructs the given {@link ServerModel model} to impart the change this action 
    * represents on the given {@link Player player}.
    */
   void perform(ServerModel model, Player player)
   {
      switch (type)
      {
         case COLLECT_CASH:
            model.changePlayerCash(player, (Integer)value);
            break;
         case COLLECT_FUEL_STATION:
            model.givePlayerFuelStation(player);
            break;
         case ADVANCE:
            model.advancePlayerToNode(player, (String)value);
            break;
         case USE_FUEL:
            model.changePlayerFuel(player, -(Integer)value);
            break;
         case ROLL_WITH_MULTIPLIER:
            model.rollWithMultiplier(player, (Integer)value);
            break;
         case ROLL_AGAIN:
            model.roll(player);
            break;
         case LOSE_DISPUTE_LEAGUE:
            model.loseDisputeWithLeague(player);
            break;
         case WIN_DISPUTE_LEAGUE:
            model.winDisputeWithLeague(player);
            break;
         case WIN_DISPUTE_PLAYER:
            model.winDisputeWithPlayer(player);
            break;
      }
   }
   
   @Override
   public String toString()
   {
      StringBuilder out = new StringBuilder();
      
      out.append(type.toString());
      
      if (type.valueType != null)
      {
         out.append('\t');
         out.append(value);
      }
      
      return out.toString(); 
   }
}
