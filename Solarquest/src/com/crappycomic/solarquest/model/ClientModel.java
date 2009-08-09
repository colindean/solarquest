package com.crappycomic.solarquest.model;

import java.io.Serializable;

import com.crappycomic.solarquest.view.*;
import com.crappycomic.solarquest.view.ViewMessage.Type;

/**
 * A client-side version of the {@link Model} class. Implements no game logic, but merely applies
 * the results of game logic sent by the {@link ServerModel} using {@link ViewMessage}s. Maintains
 * a local, up-to-date copy of the state of the game. Used by {@link View}s to determine what
 * actions are possible at any given point.
 */
public class ClientModel extends Model implements Serializable
{
   private static final long serialVersionUID = -1572719766492701764L;

   private View view;
   
   private Player currentPlayer;

   public String getDefaultView()
   {
      return defaultView;
   }
   
   public void setView(View view)
   {
      this.view = view;
   }

   /**
    * Applies the given {@link ViewMessage} to the local game state and forwards the message to
    * the {@link View}. Replaces the message's {@link Player} with the local copy. Does the same to
    * the message's {@link Node}, if passed.
    */
   public void forwardMessage(ViewMessage message)
   {
      Type type = message.getType();
      Player player = message.getPlayer() == null ? null : playerMap.get(message.getPlayer().getNumber());
      Serializable value = message.getValue();
      Node node = (value instanceof Node) ? board.getNode(((Node)value).getID()) : null;
      
      if (node != null)
         value = node;
      
      switch (type)
      {
         case MODEL_PRE_ROLL:
            currentPlayer = player;
            break;
         case PLAYER_ADVANCED_TO_NODE:
            player.setCurrentNode(node);
            break;
         case PLAYER_CHANGED_CASH:
            player.changeCash((Integer)value);
            break;
         case PLAYER_CHANGED_FUEL:
            player.changeFuel((Integer)value);
            break;
         case PLAYER_CHANGED_FUEL_STATIONS:
            player.changeFuelStations((Integer)value);
            break;
         case PLAYER_LOST_DUE_TO_BANKRUPTCY:
         case PLAYER_LOST_DUE_TO_INSUFFICIENT_FUEL:
         case PLAYER_LOST_DUE_TO_STRANDING:
            player.setGameOver(true);
            break;
         case PLAYER_OBTAINED_FREE_FUEL_STATION:
         case PLAYER_PURCHASED_FUEL_STATION:
            fuelStationsRemaining--;
            break;
         case PLAYER_OBTAINED_NODE:
         case PLAYER_PURCHASED_NODE:
            player.addNode(node);
            node.setOwner(player);
            break;
         case PLAYER_PLACED_FUEL_STATION:
            node.setFuelStation(true);
            break;
         case PLAYER_RELINQUISHED_FUEL_STATIONS:
            fuelStationsRemaining += (Integer)value;
            break;
         case PLAYER_RELINQUISHED_NODE:
         case PLAYER_SOLD_NODE:
            player.removeNode(node);
            node.setOwner(null);
            break;
         case PLAYER_SOLD_FUEL_STATION:
            fuelStationsRemaining++;
            break;
         case MODEL_CHOOSING_PROPERTY_LOST_TO_LEAGUE:
         case MODEL_CHOOSING_PROPERTY_WON_FROM_LEAGUE:
         case MODEL_CHOOSING_PROPERTY_WON_FROM_PLAYER:
         case MODEL_GAME_OVER:
         case MODEL_INVALID_STATE:
         case MODEL_POST_ROLL:
         case MODEL_PRE_LAND:
         case PLAYER_DREW_CARD:
         case PLAYER_HAD_NO_PROPERTY_TO_LOSE:
         case PLAYER_HAD_NO_PROPERTY_TO_WIN:
         case PLAYER_HAS_INSUFFICIENT_CASH:
         case PLAYER_HAS_MULTIPLE_ALLOWED_MOVES:
         case PLAYER_LANDED_ON_START_NODE:
         case PLAYER_LOST_DISPUTE_WITH_LEAGUE:
         case PLAYER_PASSED_START_NODE:
         case PLAYER_REMAINED_STATIONARY:
         case PLAYER_ROLLED:
         case PLAYER_STARTED_TRADE:
         case PLAYER_WON:
         case PLAYER_WON_DISPUTE_WITH_LEAGUE:
         case PLAYER_WON_DISPUTE_WITH_PLAYER:
         case TRADE_ACCEPTED:
         case TRADE_REJECTED:
            // Don't care
            break;
      }
      
      // Message now refers to our local copies.
      view.receiveMessage(new ViewMessage(type, player, value));
   }
   
   void setCurrentPlayer(Player currentPlayer)
   {
      this.currentPlayer = playerMap.get(currentPlayer.getNumber());
   }
   
   @Override
   public Player getCurrentPlayer()
   {
      return currentPlayer;
   }
   
   @Override
   public String toString()
   {
      return defaultView + '\n' + players + '\n' + playerMap + '\n' + fuelStationsRemaining + '\n' + ruleSet.size() + '\n';
   }
}
