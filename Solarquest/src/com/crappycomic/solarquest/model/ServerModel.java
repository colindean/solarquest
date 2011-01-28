// Solarquest
// Copyright (C) 2011 Colin Bartolome
// Licensed under the GPL. See LICENSE.txt for details.

package com.crappycomic.solarquest.model;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;

import com.crappycomic.solarquest.net.*;
import com.crappycomic.solarquest.view.*;
import com.crappycomic.solarquest.view.ViewMessage.Type;

public class ServerModel extends Model implements Runnable
{
   private static final long serialVersionUID = 3L; // version number, I guess
   
   private static enum State
   {
      PRE_ROLL,
      PRE_LAND,
      POST_ROLL,
      CHOOSING_ALLOWED_MOVE,
      SETTLING_DEBT,
      GAME_OVER,
      TRADING,
      CHOOSING_PROPERTY_LOST_TO_LEAGUE,
      CHOOSING_PROPERTY_WON_FROM_LEAGUE,
      CHOOSING_PROPERTY_WON_FROM_PLAYER
   }
   
   private String id;
   
   private transient Server server;
   
   private String defaultRuleSet;
   
   // as loaded by ModelXMLLoader
   private List<Card> cards = new ArrayList<Card>();
   
   // shuffled draw pile of cards
   private List<Card> currentDeck;
   
   private Random random = new Random();
   
   private int currentPlayer;
   
   private int playersRemaining;
   
   private BlockingQueue<Pair<ServerSideConnection, ModelMessage>> messages
      = new LinkedBlockingQueue<Pair<ServerSideConnection, ModelMessage>>();
   
   private State state;
   
   private ViewMessage lastSentMessage;
   
   private State postTradeState;
   
   private ViewMessage postTradeMessage;
   
   private List<Node> allowedMovesList;
   
   private Pair<Player, Integer> debt;
   
   private Trade currentTrade;
   
   private boolean purchasedFuelDuringPreRoll;
   
   public void initialize()
   {
      int initialCash = ruleSet.getValue(RuleSet.INITIAL_CASH);
      int initialFuel = ruleSet.getValue(RuleSet.INITIAL_FUEL);
      int initialFuelStations = ruleSet.getValue(RuleSet.INITIAL_FUEL_STATIONS);
      
      fuelStationsRemaining = ruleSet.getValue(RuleSet.TOTAL_FUEL_STATIONS);
      
      for (Player player : players)
      {
         player.changeCash(initialCash);
         player.changeFuel(initialFuel);
         player.changeFuelStations(initialFuelStations);
         player.setCurrentNode(board.getStartNode());
         
         fuelStationsRemaining -= initialFuelStations;
      }
      
      playersRemaining = players.size();
      currentDeck = new ArrayList<Card>();
   }
   
   public void start()
   {
      new Thread(this).start();
   }
   
   @Override
   public void run()
   {
      if (lastSentMessage == null)
         setState(State.PRE_ROLL);
      else
         sendMessage(lastSentMessage);
      
      while (state != State.GAME_OVER)
      {
         Pair<ServerSideConnection, ModelMessage> pair;
         ServerSideConnection connection = null;
         ModelMessage message = null;
         Player player = getCurrentPlayer();
         Node node = player.getCurrentNode();
         
         try
         {
            pair = messages.take();
            connection = pair.getFirst();
            message = pair.getSecond();
         }
         catch (InterruptedException ie)
         {
            break;
         }
         
         // TODO: does "no lasers may be fired on Earth" mean "from Earth," "at Earth," or both?
         switch (message.getType())
         {
            case NO_PRE_ROLL:
               if (state == State.PRE_ROLL)
               {
                  if (isFuelCritical(player, node))
                  {
                     sendMessage(Type.PLAYER_LOST_DUE_TO_STRANDING, player);
                     removePlayer(player);
                     nextTurn();
                  }
                  else
                  {
                     roll();
                  
                     if (state == State.PRE_ROLL)
                     {
                        setState(State.POST_ROLL);
                     }
                  }
               }
               else
               {
                  sendInvalidModelState(connection);
               }
               break;
            case NO_PRE_LAND:
               if (state == State.PRE_LAND)
               {
                  playerAdvancedToOrRemainedOnNode(player, node);
                  
                  if (state == State.PRE_LAND)
                  {
                     setState(State.POST_ROLL);
                  }
               }
               else
               {
                  sendInvalidModelState(connection);
               }
               break;
            case NO_POST_ROLL:
               if (state == State.POST_ROLL)
               {
                  nextTurn();
               }
               else
               {
                  sendInvalidModelState(connection);
               }
               break;
            case QUIT:
               removePlayer(player);
               nextTurn();
               break;
            case PURCHASE_NODE:
               if (state == State.POST_ROLL && isNodePurchaseable(player, node))
               {
                  purchaseNode(player, node);
                  sendMessage(Type.MODEL_POST_ROLL);
               }
               else
               {
                  sendInvalidModelState(connection);
               }
               break;
            case PURCHASE_FUEL:
               if ((state == State.PRE_ROLL || state == State.POST_ROLL) && isFuelPurchaseable(player, node))
               {
                  purchaseFuel(player, node, (Integer)message.getValue());
                  setState(state);
               }
               else
               {
                  sendInvalidModelState(connection);
               }
               break;
            case PURCHASE_FUEL_STATION:
               if ((state == State.PRE_ROLL || state == State.POST_ROLL) && isFuelStationPurchaseable(player, node))
               {
                  purchaseFuelStation(player);
                  setState(state);
               }
               else
               {
                  sendInvalidModelState(connection);
               }
               break;
            case PLACE_FUEL_STATION:
               if ((state == State.PRE_ROLL || state == State.POST_ROLL) && isFuelStationPlaceable(player, node))
               {
                  placeFuelStation(player, node);
                  setState(state);
               }
               else
               {
                  sendInvalidModelState(connection);
               }
               break;
            case CHOOSE_ALLOWED_MOVE:
            {
               String chosenMove = (String)message.getValue();
               
               if (state == State.CHOOSING_ALLOWED_MOVE && allowedMovesList.contains(board.getNode(chosenMove)))
               {
                  advancePlayerToNode(player, chosenMove);
                  
                  if (state == State.CHOOSING_ALLOWED_MOVE)
                  {
                     setState(State.POST_ROLL);
                  }
               }
               else
               {
                  sendInvalidModelState(connection);
               }
               break;
            }
            case SELL_FUEL_STATION_NORMALLY:
               if ((state == State.PRE_ROLL || state == State.POST_ROLL) && isFuelStationSalableNormally(player, node))
               {
                  sellFuelStation(player);
                  setState(state);
               }
               else
               {
                  sendInvalidModelState(connection);
               }
               break;
            case SELL_FUEL_STATION_FOR_DEBT_SETTLEMENT:
               if (state == State.SETTLING_DEBT && isFuelStationSalableForDebtSettlement(player))
               {
                  sellFuelStation(player);
                  attemptToSettleDebt(player, debt.getFirst(), debt.getSecond());
               }
               else
               {
                  sendInvalidModelState(connection);
               }
               break;
            case DECLARE_BANKRUPTCY:
               if (state == State.SETTLING_DEBT)
               {
                  sendMessage(Type.PLAYER_LOST_DUE_TO_BANKRUPTCY, player, debt.getFirst());
                  removePlayer(player, debt.getFirst());
                  nextTurn();
               }
               else
               {
                  sendInvalidModelState(connection);
               }
               break;
            case SELL_NODE:
            {
               Node chosenNode = board.getNode((String)message.getValue());
               
               if (state == State.SETTLING_DEBT && isNodeSalable(player) && chosenNode != null && player.equals(chosenNode.getOwner()))
               {
                  sellNode(player, chosenNode);
                  attemptToSettleDebt(player, debt.getFirst(), debt.getSecond());
               }
               else
               {
                  sendInvalidModelState(connection);
               }
               break;
            }
            case TRADE:
               currentTrade = (Trade)message.getValue();
               postTradeState = state;
               postTradeMessage = lastSentMessage;
               sendMessage(Type.PLAYER_STARTED_TRADE, player, currentTrade);
               setState(State.TRADING);
               break;
            case TRADE_COMPLETED:
               if (state == State.TRADING && currentTrade != null) // both or neither should ever be true
               {
                  completeTrade(player, (Boolean)message.getValue());
               }
               else
               {
                  sendInvalidModelState(connection);
               }
               break;
            case NEGLIGENCE_TAKEOVER:
               if (state == State.PRE_LAND && isNegligenceTakeoverAllowed(player, node))
               {
                  Player owner = node.getOwner();
                  int price = getNodePrice(node);
                  
                  changePlayerCash(player, -price);
                  changePlayerCash(owner, price);
                  relinquishNode(owner, node);
                  obtainNode(player, node);
                  
                  if (isPreLandRequired(player, node))
                  {
                     sendMessage(Type.MODEL_PRE_LAND);
                  }
                  else
                  {
                     playerAdvancedToOrRemainedOnNode(player, node);
                     setState(State.POST_ROLL);
                  }
               }
               else
               {
                  sendInvalidModelState(connection);
               }
               break;
            case CHOOSE_PROPERTY_LOST_TO_LEAGUE:
            {
               Node chosenNode = board.getNode((String)message.getValue());
               
               if (state == State.CHOOSING_PROPERTY_LOST_TO_LEAGUE && chosenNode != null && player.equals(chosenNode.getOwner()))
               {
                  relinquishNode(player, chosenNode);
                  setState(State.POST_ROLL);
               }
               else
               {
                  sendInvalidModelState(connection);
               }
               break;
            }
            case CHOOSE_PROPERTY_WON_FROM_LEAGUE:
            {
               Node chosenNode = board.getNode((String)message.getValue());
               
               if (state == State.CHOOSING_PROPERTY_WON_FROM_LEAGUE && chosenNode != null && chosenNode.getOwner() == null)
               {
                  obtainNode(player, chosenNode);
                  setState(State.POST_ROLL);
               }
               else
               {
                  sendInvalidModelState(connection);
               }
               break;
            }
            case CHOOSE_PROPERTY_WON_FROM_PLAYER:
            {
               Node chosenNode = board.getNode((String)message.getValue());
               
               if (state == State.CHOOSING_PROPERTY_WON_FROM_PLAYER && chosenNode != null && chosenNode.getOwner() != null
                  && !player.equals(chosenNode.getOwner()))
               {
                  relinquishNode(chosenNode.getOwner(), chosenNode);
                  obtainNode(player, chosenNode);
                  setState(State.POST_ROLL);
               }
               else
               {
                  sendInvalidModelState(connection);
               }
               break;
            }
         }
      }
   }
   
   private void setState(State state)
   {
      this.state = state;
      
      Type type = null;
      
      switch (state)
      {
         case GAME_OVER:
            type = Type.MODEL_GAME_OVER;
            break;
         case POST_ROLL:
            type = Type.MODEL_POST_ROLL;
            break;
         case PRE_LAND:
            type = Type.MODEL_PRE_LAND;
            break;
         case PRE_ROLL:
            type = Type.MODEL_PRE_ROLL;
            break;
         case CHOOSING_PROPERTY_LOST_TO_LEAGUE:
            type = Type.MODEL_CHOOSING_PROPERTY_LOST_TO_LEAGUE;
            break;
         case CHOOSING_PROPERTY_WON_FROM_PLAYER:
            type = Type.MODEL_CHOOSING_PROPERTY_WON_FROM_PLAYER;
            break;
         case CHOOSING_PROPERTY_WON_FROM_LEAGUE:
            type = Type.MODEL_CHOOSING_PROPERTY_WON_FROM_LEAGUE;
            break;
         // Have already sent vital paramaters, so don't confuse things by sending a MODEL message.
         case CHOOSING_ALLOWED_MOVE:
            break;
         case SETTLING_DEBT:
            break;
         case TRADING:
            break;
      }

      if (type != null)
         sendMessage(type, state == State.PRE_ROLL ? getCurrentPlayer() : null);
   }
   
   private void sendInvalidModelState(ServerSideConnection connection)
   {
      server.sendObject(new ViewMessage(Type.MODEL_INVALID_STATE, null, null), connection);
   }

   void setID(String id)
   {
      this.id = id;
   }
   
   public String getID()
   {
      return id;
   }
   
   public RuleSet getRuleSet()
   {
      return ruleSet;
   }

   void setDefaultRuleSet(String defaultRuleSet)
   {
      this.defaultRuleSet = defaultRuleSet;
   }
   
   public String getDefaultRuleSet()
   {
      return defaultRuleSet;
   }
   
   void setDefaultView(String defaultView)
   {
      this.defaultView = defaultView;
   }
   
   public Board getBoard()
   {
      return board;
   }
   
   void addCard(Card card)
   {
      cards.add(card);
   }

   private void sendMessage(Type type)
   {
      sendMessage(type, null);
   }
   
   private void sendMessage(Type type, Player player)
   {
      sendMessage(type, player, null);
   }
   
   private void sendMessage(Type type, Player player, Serializable value)
   {
      sendMessage(new ViewMessage(type, player, value));
   }
   
   private void sendMessage(ViewMessage message)
   {
      lastSentMessage = message;
      server.sendObject(message);
   }
   
   void changePlayerCash(Player player, int amount)
   {
      if (amount == 0)
         return;
      
      player.changeCash(amount);
      
      sendMessage(Type.PLAYER_CHANGED_CASH, player, amount);
   }
   
   void changePlayerFuel(Player player, int amount)
   {
      if (amount == 0)
         return;
      
      if (player.getFuel() + amount < 0)
      {
         sendMessage(Type.PLAYER_LOST_DUE_TO_INSUFFICIENT_FUEL, player);
         removePlayer(player);
      }
      else
      {      
         player.changeFuel(amount);
         sendMessage(Type.PLAYER_CHANGED_FUEL, player, amount);
      }
   }
   
   private void removePlayer(Player player)
   {
      removePlayer(player, null);
   }
   
   private void removePlayer(Player player, Player beneficiary)
   {
      Collection<Node> nodes = new ArrayList<Node>(player.getOwnedNodes());
      
      player.setGameOver(true);
      playersRemaining--;

      if (beneficiary == null)
      {
         for (Node node : nodes)
         {
            relinquishNode(player, node);
         }
         
         changePlayerCash(player, -player.getCash());
         fuelStationsRemaining += player.getFuelStations();
         sendMessage(Type.PLAYER_RELINQUISHED_FUEL_STATIONS, player, player.getFuelStations());
         changePlayerFuelStations(player, -player.getFuelStations());
      }
      else
      {
         int cash = player.getCash();
         int fuelStations = player.getFuelStations();
         
         for (Node node : nodes)
         {
            relinquishNode(player, node);
            obtainNode(beneficiary, node);
         }
         
         changePlayerCash(player, -cash);
         changePlayerCash(beneficiary, cash);
         
         changePlayerFuelStations(player, -fuelStations);
         changePlayerFuelStations(beneficiary, fuelStations);
      }
      
      changePlayerFuel(player, -player.getFuel());

      if (playersRemaining == 1)
      {
         for (Player winner : players)
         {
            if (!winner.isGameOver())
            {
               sendMessage(Type.PLAYER_WON, winner);
               setState(State.GAME_OVER);
               
               break;
            }
         }
      }
   }
   
   void advancePlayerToNode(Player player, String nodeID)
   {
      advancePlayerToNode(player, board.getNode(nodeID));
   }
   
   private void advancePlayerToNode(Player player, Node node)
   {
      boolean passesStartNode = board.passesStart(player.getCurrentNode(), node);
      boolean landsOnStartNode = node.isStartNode();
      
      player.setCurrentNode(node);
      
      sendMessage(Type.PLAYER_ADVANCED_TO_NODE, player, node);
      
      if (passesStartNode)
      {
         sendMessage(Type.PLAYER_PASSED_START_NODE, player);
         changePlayerCash(player, ruleSet.getValue(RuleSet.PASS_START_CASH));
      }
      
      if (landsOnStartNode)
      {
         sendMessage(Type.PLAYER_LANDED_ON_START_NODE, player);
         changePlayerCash(player, ruleSet.getValue(RuleSet.LAND_ON_START_CASH));
      }

      if (node.getActions() != null)
         for (Action action : node.getActions())
            action.perform(this, player);
      
      if (isPreLandRequired(player, node))
      {
         setState(State.PRE_LAND);
      }
      else
      {
         playerAdvancedToOrRemainedOnNode(player, node);
      }
   }
      
   private void playerAdvancedToOrRemainedOnNode(Player player, Node node)
   {
      int rent = node.getRent(player);
      
      if (rent > 0)
      {
         attemptToSettleDebt(player, node.getOwner(), rent);
      }
   }
   
   private void attemptToSettleDebt(Player debtor, Player creditor, int rent)
   {
      if (debtor.getCash() >= rent)
      {
         changePlayerCash(debtor, -rent);
         changePlayerCash(creditor, rent);
         
         debt = null;
         setState(State.POST_ROLL);
      }
      else
      {
         debt = new Pair<Player, Integer>(creditor, rent);
         sendMessage(Type.PLAYER_HAS_INSUFFICIENT_CASH, debtor, debt);
         setState(State.SETTLING_DEBT);
      }
   }
   
   void changePlayerFuelStations(Player player, int amount)
   {
      if (amount == 0)
         return;
      
      player.changeFuelStations(amount);
      sendMessage(Type.PLAYER_CHANGED_FUEL_STATIONS, player, amount);
   }
   
   void roll()
   {
      roll(players.get(currentPlayer));
   }
   
   void roll(Player player)
   {
      roll(player, 1, true);
   }
   
   void rollWithMultiplier(Player player, int rollMultiplier)
   {
      roll(player, rollMultiplier, false);
   }
   
   /** if useFuel is false, a Red Shift is impossible */
   void roll(Player player, int rollMultiplier, boolean allowRedShiftAndUseFuel)
   {
      int diePips = ruleSet.getValue(RuleSet.DIE_PIPS);
      int die1 = random.nextInt(diePips) + 1;
      int die2 = random.nextInt(diePips) + 1;
      
      sendMessage(Type.PLAYER_ROLLED, player, new Pair<Integer, Integer>(die1, die2));
      
      int roll = (die1 + die2) * rollMultiplier;
      
      if (allowRedShiftAndUseFuel && RuleSet.isRedShift(ruleSet.getValue(RuleSet.RED_SHIFT_ROLL), ruleSet.getValue(RuleSet.DIE_PIPS), die1, die2))
      {
         Card card = getNextCard();
         
         sendMessage(Type.PLAYER_DREW_CARD, player, card);
         
         for (Action action : card.getActions())
         {
            action.perform(this, player);
            
            // Break out if player lost the model due to the Red Shift
            if (player.isGameOver())
               break;
         }
         
         return;
      }
      
      boolean useFuel = allowRedShiftAndUseFuel && player.getCurrentNode().usesFuel();
      
      Set<Node> allowedMoves;
      
      if (useFuel && roll > player.getFuel())
         allowedMoves = Collections.emptySet();
      else
         allowedMoves = board.getAllowedMoves(player.getCurrentNode(), roll);
      
      if (allowedMoves.isEmpty())
      {
         sendMessage(Type.PLAYER_REMAINED_STATIONARY, player);
         playerAdvancedToOrRemainedOnNode(player, player.getCurrentNode());
      }
      else if (allowedMoves.size() == 1)
      {
         if (useFuel)
            changePlayerFuel(player, -roll);
         advancePlayerToNode(player, allowedMoves.iterator().next());
      }
      else
      {
         allowedMovesList = new ArrayList<Node>(allowedMoves);
         sendMessage(Type.PLAYER_HAS_MULTIPLE_ALLOWED_MOVES, player, (Serializable)allowedMovesList);
         setState(State.CHOOSING_ALLOWED_MOVE);
      }
   }
   
   void loseDisputeWithLeague(Player player)
   {
      boolean nodeIsAvailable = !player.getOwnedNodes().isEmpty();
      
      sendMessage(Type.PLAYER_LOST_DISPUTE_WITH_LEAGUE, player);

      if (nodeIsAvailable)
      {
         setState(State.CHOOSING_PROPERTY_LOST_TO_LEAGUE);
      }
      else
      {
         sendMessage(Type.PLAYER_HAD_NO_PROPERTY_TO_LOSE, player);
      }
   }
   
   void winDisputeWithLeague(Player player)
   {
      boolean nodeIsAvailable = false;
      
      for (Node node : board.getNodes())
      {
         if (node.isPurchaseable())
         {
            nodeIsAvailable = true;
            break;
         }
      }
      
      sendMessage(Type.PLAYER_WON_DISPUTE_WITH_LEAGUE, player);

      if (nodeIsAvailable)
      {
         setState(State.CHOOSING_PROPERTY_WON_FROM_LEAGUE);
      }
      else
      {
         sendMessage(Type.PLAYER_HAD_NO_PROPERTY_TO_WIN, player);
      }
   }
   
   void winDisputeWithPlayer(Player player)
   {
      boolean nodeIsAvailable = false;
      
      for (Player owner : players)
      {
         if (owner.equals(player))
            continue;
         if (!owner.getOwnedNodes().isEmpty())
         {
            nodeIsAvailable = true;
            break;
         }
      }
      
      sendMessage(Type.PLAYER_WON_DISPUTE_WITH_PLAYER, player);
      
      if (nodeIsAvailable)
      {
         setState(State.CHOOSING_PROPERTY_WON_FROM_PLAYER);
      }
      else
      {
         sendMessage(Type.PLAYER_HAD_NO_PROPERTY_TO_WIN, player);
      }
   }
   
   private void nextTurn()
   {
      if (playersRemaining <= 1)
         return;
      
      do
      {
         currentPlayer++;
      
         if (currentPlayer >= players.size())
            currentPlayer = 0;
      }
      while (players.get(currentPlayer).isGameOver());
      
      purchasedFuelDuringPreRoll = false;
      setState(State.PRE_ROLL);
   }

   private void purchaseNode(Player player, Node node)
   {
      changePlayerCash(player, -getNodePrice(node));
      player.addNode(node);
      node.setOwner(player);
      
      sendMessage(Type.PLAYER_PURCHASED_NODE, player, node);
   }
   
   private void sellNode(Player player, Node node)
   {
      changePlayerCash(player, getNodePrice(node));
      player.removeNode(node);
      node.setOwner(null);
      
      sendMessage(Type.PLAYER_SOLD_NODE, player, node);
   }

   private void purchaseFuel(Player player, Node node, int hydrons)
   {
      int maximumPurchaseableFuel = getMaximumPurchaseableFuel(player, node);
      
      if (hydrons > maximumPurchaseableFuel)
         hydrons = maximumPurchaseableFuel;
      
      if (hydrons <= 0)
         return;

      purchasedFuelDuringPreRoll = state == State.PRE_ROLL;
      
      int fuelPrice = node.getFuelPrice(ruleSet, player);
      int totalFuelPrice = fuelPrice * hydrons;
      Player owner = node.getOwner();
      
      changePlayerCash(player, -totalFuelPrice);
      if (owner != null)
         changePlayerCash(owner, totalFuelPrice);
      changePlayerFuel(player, hydrons);
   }

   private void placeFuelStation(Player player, Node node)
   {
      if (player.getFuelStations() <= 0 || node.hasFuelStation())
         return;
      
      changePlayerFuelStations(player, -1);
      node.setFuelStation(true);
      sendMessage(Type.PLAYER_PLACED_FUEL_STATION, player, node);
   }

   private void purchaseFuelStation(Player player)
   {
      if (!hasUnpurchasedFuelStation() || player.getCash() < getFuelStationPrice())
         return;
      
      fuelStationsRemaining--;
      changePlayerCash(player, -getFuelStationPrice());
      changePlayerFuelStations(player, 1);
      
      sendMessage(Type.PLAYER_PURCHASED_FUEL_STATION, player);
   }
   
   private void sellFuelStation(Player player)
   {
      changePlayerFuelStations(player, -1);
      changePlayerCash(player, getFuelStationPrice());
      fuelStationsRemaining++;
      
      sendMessage(Type.PLAYER_SOLD_FUEL_STATION, player);
   }
   
   void givePlayerFuelStation(Player player)
   {
      if (fuelStationsRemaining > 0)
      {
         changePlayerFuelStations(player, 1);
         fuelStationsRemaining--;

         sendMessage(Type.PLAYER_OBTAINED_FREE_FUEL_STATION, player);
      }
   }
   
   private Card getNextCard()
   {
      if (currentDeck.isEmpty())
      {
         currentDeck.addAll(cards);
         Collections.shuffle(currentDeck);
      }
      
      Card card = currentDeck.remove(0);
      
      currentDeck.add(card);
      
      return card;
   }

   public void receiveMessage(ServerSideConnection connection, ModelMessage modelMessage)
   {
      messages.add(new Pair<ServerSideConnection, ModelMessage>(connection, modelMessage));
   }

   private void completeTrade(Player player, boolean accepted)
   {
      Player from = playerMap.get(currentTrade.getFrom());
      Player to = playerMap.get(currentTrade.getTo());
      Collection<Node> offered = getAllNodes(currentTrade.getOffered());
      Collection<Node> requested = getAllNodes(currentTrade.getRequested());
      int cash = currentTrade.getCash();
      
      if (accepted)
      {
         sendMessage(Type.TRADE_ACCEPTED, null, currentTrade);

         for (Node node : offered)
         {
            if (from.equals(node.getOwner()))
            {
               relinquishNode(from, node);
               obtainNode(to, node);
            }
         }
         
         for (Node node : requested)
         {
            if (to.equals(node.getOwner()))
            {
               relinquishNode(to, node);
               obtainNode(from, node);
            }
         }
         
         if ((cash > 0 && from.getCash() >= cash) || (cash < 0 && to.getCash() >= -cash))
         {
            changePlayerCash(from, -cash);
            changePlayerCash(to, cash);
         }
      }
      else
      {
         sendMessage(Type.TRADE_REJECTED, null, currentTrade);
      }
      
      currentTrade = null;

      if (debt == null)
      {
         state = postTradeState;
         sendMessage(postTradeMessage);
      }
      else
      {
         // Did the property causing the debt change hands?
         Node node = player.getCurrentNode();
         
         if (player.equals(node.getOwner()) || node.getOwner() == null)
         {
            // Current player obtained node or the node was sold back to the League; cancel debt.
            debt = null;
            setState(State.POST_ROLL);
            return;
         }
         else if (!debt.getFirst().equals(node.getOwner()))
         {
            // Some other player obtained node; transfer debt.
            debt = new Pair<Player, Integer>(node.getOwner(), debt.getSecond());
         }

         attemptToSettleDebt(player, debt.getFirst(), debt.getSecond());
      }
   }
   
   private Collection<Node> getAllNodes(Collection<String> nodeIDs)
   {
      Collection<Node> nodes = new ArrayList<Node>(nodeIDs.size());
      
      for (String nodeID : nodeIDs)
         nodes.add(board.getNode(nodeID));
      
      return nodes;
   }
   
   private void relinquishNode(Player player, Node node)
   {
      player.removeNode(node);
      node.setOwner(null);
      sendMessage(Type.PLAYER_RELINQUISHED_NODE, player, node);
   }
   
   private void obtainNode(Player player, Node node)
   {
      player.addNode(node);
      node.setOwner(player);
      sendMessage(Type.PLAYER_OBTAINED_NODE, player, node);
   }

   private boolean isPreLandRequired(Player player, Node node)
   {
      return isNegligenceTakeoverAllowed(player, node) || isLaserBattleAllowed(); // TODO: add params for efficiency
   }
   
   public boolean isLaserBattleEverAllowed()
   {
      return ruleSet.getValue(RuleSet.LASER_BATTLES_ALLOWED);
   }
   
   public boolean isLaserBattleAllowed()
   {
      // TODO: add fuel and distance check
      return isLaserBattleEverAllowed() && ((state == State.PRE_ROLL && !purchasedFuelDuringPreRoll) || state == State.PRE_LAND);
   }

   public void setServer(Server server)
   {
      this.server = server;
   }
   
   @Override
   public Player getCurrentPlayer()
   {
      return players.get(currentPlayer);
   }

   public void playersDropped(Collection<Integer> droppedPlayers)
   {
      for (int player : droppedPlayers)
         removePlayer(playerMap.get(player));
   }

   public ClientModel createClientModel()
   {
      ClientModel clientModel = new ClientModel();
      
      clientModel.board = board;
      clientModel.defaultView = defaultView;
      clientModel.players = players;
      clientModel.playerMap = playerMap;
      clientModel.ruleSet = ruleSet;
      clientModel.fuelStationsRemaining = fuelStationsRemaining;
      clientModel.setCurrentPlayer(getCurrentPlayer());
      
      return clientModel;
   }

   public String getPlayerName(int number)
   {
      Player player = playerMap.get(number);
      
      return player == null ? null : player.getName();
   }
}
