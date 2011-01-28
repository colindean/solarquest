// Solarquest
// Copyright (C) 2011 Colin Bartolome
// Licensed under the GPL. See LICENSE.txt for details.

package com.crappycomic.solarquest.model;

import java.io.Serializable;

import com.crappycomic.solarquest.view.View;

/** Represents an action taken by a {@link Player}. Sent by a {@link View} to the {@link Model} . */
public class ModelMessage implements Serializable
{
   private static final long serialVersionUID = 672249984093234004L;

   /** Enumerates the possible actions that a {@link Player} may take. */
   public static enum Type
   {
      NO_PRE_ROLL,
      NO_PRE_LAND,
      NO_POST_ROLL,
      
      QUIT,
      
      PURCHASE_NODE,
      PURCHASE_FUEL,
      PURCHASE_FUEL_STATION,
      PLACE_FUEL_STATION,
      CHOOSE_ALLOWED_MOVE,
      SELL_FUEL_STATION_NORMALLY,
      SELL_FUEL_STATION_FOR_DEBT_SETTLEMENT,
      DECLARE_BANKRUPTCY,
      SELL_NODE,
      TRADE,
      TRADE_COMPLETED,
      NEGLIGENCE_TAKEOVER,
      CHOOSE_PROPERTY_LOST_TO_LEAGUE,
      CHOOSE_PROPERTY_WON_FROM_LEAGUE,
      CHOOSE_PROPERTY_WON_FROM_PLAYER
   }
   
   /** The specific type of action taken by the {@link Player}. */
   private Type type;
   
   /**
    * A {@link Type type}-dependant value further specifying the action taken by the {@link Player}.
    * Note: A {@link View} should not send any messages containing any objects from the
    * {@link com.crappycomic.solarquest.model} package&mdash;only their ID's and such.
    */
   private Serializable value;
   
   /** Creates an empty instance. Used only during serialization. */
   public ModelMessage()
   {
   }
   
   /** Creates an instance with the given type and value. */
   public ModelMessage(Type type, Serializable value)
   {
      this.type = type;
      this.value = value;
   }

   public Type getType()
   {
      return type;
   }
   
   public Serializable getValue()
   {
      return value;
   }
   
   @Override
   public String toString()
   {
      return type + " " + value;
   }
}
