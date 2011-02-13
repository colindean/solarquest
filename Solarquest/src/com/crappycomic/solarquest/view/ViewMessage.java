// Solarquest
// Copyright (C) 2011 Colin Bartolome
// Licensed under the GPL. See LICENSE.txt for details.

package com.crappycomic.solarquest.view;

import java.io.Serializable;

import com.crappycomic.solarquest.model.Player;

public class ViewMessage implements Serializable
{
   private static final long serialVersionUID = 4768863093404077072L;

   public static enum Type
   {
      MODEL_PRE_ROLL,
      MODEL_PRE_LAND,
      MODEL_POST_ROLL,
      MODEL_CHOOSING_NODE_LOST_TO_LEAGUE,
      MODEL_CHOOSING_NODE_WON_FROM_LEAGUE,
      MODEL_CHOOSING_NODE_WON_FROM_PLAYER,
      MODEL_GAME_OVER,
      MODEL_INVALID_STATE,

      PLAYER_CHANGED_CASH,
      PLAYER_CHANGED_FUEL_STATIONS,
      PLAYER_CHANGED_FUEL,
      PLAYER_LOST_DUE_TO_INSUFFICIENT_FUEL,
      PLAYER_LOST_DUE_TO_BANKRUPTCY,
      PLAYER_LOST_DUE_TO_STRANDING,
      PLAYER_ADVANCED_TO_NODE,
      PLAYER_PASSED_START_NODE,
      PLAYER_LANDED_ON_START_NODE,
      PLAYER_LOST_DISPUTE_WITH_LEAGUE,
      PLAYER_WON_DISPUTE_WITH_LEAGUE,
      PLAYER_WON_DISPUTE_WITH_PLAYER,
      PLAYER_ROLLED,
      PLAYER_REMAINED_STATIONARY,
      PLAYER_HAS_MULTIPLE_ALLOWED_MOVES,
      PLAYER_PURCHASED_NODE,
      PLAYER_PURCHASED_FUEL_STATION,
      PLAYER_PLACED_FUEL_STATION,
      PLAYER_DREW_CARD,
      PLAYER_HAS_INSUFFICIENT_CASH,
      PLAYER_WON,
      PLAYER_SOLD_NODE,
      PLAYER_SOLD_FUEL_STATION,
      PLAYER_RELINQUISHED_FUEL_STATIONS,
      PLAYER_OBTAINED_FREE_FUEL_STATION,
      PLAYER_STARTED_TRADE,
      PLAYER_RELINQUISHED_NODE,
      PLAYER_OBTAINED_NODE,
      PLAYER_HAD_NO_NODE_TO_LOSE,
      PLAYER_HAD_NO_NODE_TO_WIN,
      PLAYER_FIRED_LASERS,
      PLAYER_FIRED_LASERS_AND_MISSED,
      PLAYER_FIRED_LASERS_AND_CAUSED_DAMAGE,
      PLAYER_FIRED_LASERS_AND_DESTROYED_A_SHIP,
      PLAYER_CAN_BYPASS,

      TRADE_ACCEPTED,
      TRADE_REJECTED
   }

   private Type type;

   private Player player;

   private Serializable value;

   public ViewMessage(Type type, Player player, Serializable value)
   {
      this.type = type;
      this.player = player;
      this.value = value;
   }

   public Type getType()
   {
      return type;
   }

   public Player getPlayer()
   {
      return player;
   }

   public Serializable getValue()
   {
      return value;
   }
   
   @Override
   public String toString()
   {
      return type + " " + player + " " + value;
   }
}
