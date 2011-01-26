// Solarquest
// Copyright (C) 2011 Colin Bartolome
// Licensed under the GPL. See LICENSE.txt for details.

package com.crappycomic.solarquest.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

import com.crappycomic.solarquest.model.*;
import com.crappycomic.solarquest.net.Client;

public abstract class View
{
   private Client client;
   
   protected ClientModel model;
   
   private ServerModel serverModel;

   protected boolean looping = true;

   private ViewMessage lastMessage;
   
   private Set<Integer> localPlayers;

   protected View()
   {
   }

   @SuppressWarnings("unchecked")
   protected void processMessage(ViewMessage message)
   {
      switch (message.getType())
      {
         case MODEL_PRE_ROLL:
            promptForPreRollActions();
            break;
         case MODEL_PRE_LAND:
            promptForPreLandActions();
            break;
         case MODEL_POST_ROLL:
            promptForPostRollActions();
            break;
         case MODEL_CHOOSING_PROPERTY_LOST_TO_LEAGUE:
            promptForPropertyLostToLeague();
            break;
         case MODEL_CHOOSING_PROPERTY_WON_FROM_LEAGUE:
            promptForPropertyWonFromLeague();
            break;
         case MODEL_CHOOSING_PROPERTY_WON_FROM_PLAYER:
            promptForPropertyWonFromPlayer();
            break;
         case TRADE_ACCEPTED:
            tradeAccepted((Trade)message.getValue());
            break;
         case TRADE_REJECTED:
            tradeRejected((Trade)message.getValue());
            break;
         case MODEL_GAME_OVER:
            gameOver();
            break;
         case MODEL_INVALID_STATE:
            invalidModelState();
            break;
         case PLAYER_CHANGED_CASH:
            playerChangedCash(message.getPlayer(), (Integer)message.getValue());
            break;
         case PLAYER_CHANGED_FUEL_STATIONS:
            playerChangedFuelStations(message.getPlayer(), (Integer)message.getValue());
            break;
         case PLAYER_CHANGED_FUEL:
            playerChangedFuel(message.getPlayer(), (Integer)message.getValue());
            break;
         case PLAYER_LOST_DUE_TO_INSUFFICIENT_FUEL:
            playerLostDueToInsufficientFuel(message.getPlayer());
            break;
         case PLAYER_ADVANCED_TO_NODE:
            playerAdvancedToNode(message.getPlayer(), (Node)message.getValue());
            break;
         case PLAYER_PASSED_START_NODE:
            playerPassedStartNode(message.getPlayer());
            break;
         case PLAYER_LANDED_ON_START_NODE:
            playerLandedOnStartNode(message.getPlayer());
            break;
         case PLAYER_LOST_DISPUTE_WITH_LEAGUE:
            playerLostDisputeWithLeague(message.getPlayer());
            break;
         case PLAYER_WON_DISPUTE_WITH_LEAGUE:
            playerWonDisputeWithLeague(message.getPlayer());
            break;
         case PLAYER_WON_DISPUTE_WITH_PLAYER:
            playerWonDisputeWithPlayer(message.getPlayer());
            break;
         case PLAYER_ROLLED:
            playerRolled(message.getPlayer(), (Pair<Integer, Integer>)message.getValue());
            break;
         case PLAYER_REMAINED_STATIONARY:
            playerRemainedStationary(message.getPlayer());
            break;
         case PLAYER_HAS_MULTIPLE_ALLOWED_MOVES:
            playerHasMultipleAllowedMoves(message.getPlayer(), (List<Node>)message.getValue());
            break;
         case PLAYER_PURCHASED_NODE:
            playerPurchasedNode(message.getPlayer(), (Node)message.getValue());
            break;
         case PLAYER_PURCHASED_FUEL_STATION:
            playerPurchasedFuelStation(message.getPlayer());
            break;
         case PLAYER_PLACED_FUEL_STATION:
            playerPlacedFuelStation(message.getPlayer(), (Node)message.getValue());
            break;
         case PLAYER_RELINQUISHED_FUEL_STATIONS:
            // Ignore. This message is for ClientModel only.
            break;
         case PLAYER_OBTAINED_FREE_FUEL_STATION:
            playerObtainedFreeFuelStation(message.getPlayer());
            break;
         case PLAYER_DREW_CARD:
            playerDrewCard(message.getPlayer(), (Card)message.getValue());
            break;
         case PLAYER_HAS_INSUFFICIENT_CASH:
            promptForDebtSettlement((Pair<Player, Integer>)message.getValue());
            break;
         case PLAYER_WON:
            playerWon(message.getPlayer());
            break;
         case PLAYER_LOST_DUE_TO_BANKRUPTCY:
            playerLostDueToBankruptcy(message.getPlayer());
            break;
         case PLAYER_SOLD_NODE:
            playerSoldNode(message.getPlayer(), (Node)message.getValue());
            break;
         case PLAYER_SOLD_FUEL_STATION:
            playerSoldFuelStation(message.getPlayer());
            break;
         case PLAYER_STARTED_TRADE:
            promptForTradeDecision((Trade)message.getValue());
            break;
         case PLAYER_LOST_DUE_TO_STRANDING:
            playerLostDueToStranding(message.getPlayer());
            break;
         case PLAYER_RELINQUISHED_NODE:
            playerRelinquishedNode(message.getPlayer(), (Node)message.getValue());
            break;
         case PLAYER_OBTAINED_NODE:
            playerObtainedNode(message.getPlayer(), (Node)message.getValue());
            break;
         case PLAYER_HAD_NO_PROPERTY_TO_LOSE:
            playerHadNoPropertyToLose(message.getPlayer());
            break;
         case PLAYER_HAD_NO_PROPERTY_TO_WIN:
            playerHadNoPropertyToWin(message.getPlayer());
            break;
      }
      
      lastMessage = message;
   }

   protected void gameOver()
   {
      looping = false;
   }

   protected abstract void playerChangedCash(Player player, int amount);

   protected abstract void playerChangedFuelStations(Player player, int amount);

   protected abstract void playerChangedFuel(Player player, int amount);

   protected abstract void playerLostDueToInsufficientFuel(Player player);

   protected abstract void playerAdvancedToNode(Player player, Node node);

   protected abstract void playerPassedStartNode(Player player);

   protected abstract void playerLandedOnStartNode(Player player);

   protected abstract void playerLostDisputeWithLeague(Player player);

   protected abstract void playerWonDisputeWithLeague(Player player);

   protected abstract void playerWonDisputeWithPlayer(Player player);

   protected abstract void playerRolled(Player player, Pair<Integer, Integer> dice);

   protected abstract void playerRemainedStationary(Player player);

   protected abstract void playerHasMultipleAllowedMoves(Player player, List<Node> allowedMoves);

   protected abstract void playerPurchasedNode(Player player, Node node);

   protected abstract void playerPlacedFuelStation(Player player, Node node);
   
   protected abstract void playerObtainedFreeFuelStation(Player player);

   protected abstract void playerPurchasedFuelStation(Player player);

   protected abstract void playerDrewCard(Player player, Card card);

   protected abstract void playerWon(Player player);
   
   protected abstract void playerLostDueToStranding(Player player);
   
   protected abstract void playerRelinquishedNode(Player player, Node node);
   
   protected abstract void playerObtainedNode(Player player, Node node);
   
   protected abstract void tradeAccepted(Trade trade);
   
   protected abstract void tradeRejected(Trade trade);
   
   protected abstract void playerLostDueToBankruptcy(Player player);
   
   protected abstract void playerSoldNode(Player player, Node node);
   
   protected abstract void playerSoldFuelStation(Player player);
   
   protected abstract void playerHadNoPropertyToWin(Player player);
   
   protected abstract void playerHadNoPropertyToLose(Player player);

   protected abstract void promptForPreRollActions();
   
   protected abstract void promptForPreLandActions();

   protected abstract void promptForPostRollActions();

   protected abstract void promptForDebtSettlement(Pair<Player, Integer> debt);
   
   protected abstract void promptForTradeDecision(Trade trade);
   
   protected abstract void promptForPropertyLostToLeague();
   
   protected abstract void promptForPropertyWonFromLeague();
   
   protected abstract void promptForPropertyWonFromPlayer();
   
   protected abstract void initialize();
   
   public abstract void receiveMessage(ViewMessage message);
   
   protected void sendMessage(ModelMessage.Type type)
   {
      sendMessage(type, null);
   }
   
   protected void sendMessage(ModelMessage.Type type, Serializable value)
   {
      client.sendObject(new ModelMessage(type, value));
   }
   
   protected void invalidModelState()
   {
      if (lastMessage != null)
      {
         System.err.println("Model reports invalid state. Repeating last received ViewMessage.");

         receiveMessage(lastMessage);
      }
   }
   
   public void setClient(Client client)
   {
      this.client = client;
      client.setView(this);
   }
   
   public void setModel(ClientModel model)
   {
      this.model = model;
      initialize();
   }
   
   public void setLocalPlayers(Set<Integer> localPlayers)
   {
      this.localPlayers = localPlayers;
   }
   
   boolean isPlayerLocal(Player player)
   {
      return localPlayers != null && localPlayers.contains(player.getNumber());
   }
   
   public void setServerModel(ServerModel serverModel)
   {
      this.serverModel = serverModel;
   }
   
   boolean canSave()
   {
      return serverModel != null;
   }
   
   void save(File file) throws IOException
   {
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
      
      out.writeObject(serverModel);
      out.flush();
      out.close();
   }
}
