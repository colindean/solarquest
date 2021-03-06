// Solarquest
// Copyright (C) 2011 Colin Bartolome
// Licensed under the GPL. See LICENSE.txt for details.

package com.crappycomic.solarquest.model;

import java.io.Serializable;
import java.util.*;

public abstract class Model implements Serializable
{
   private static final long serialVersionUID = 0;
   
   protected Board board;
   
   protected String defaultView;
   
   protected List<Player> players;
   
   protected Map<Integer, Player> playerMap;
   
   protected int fuelStationsRemaining;
   
   protected RuleSet ruleSet;
   
   public abstract Player getCurrentPlayer();
   
   public List<Node> getUnownedNodes()
   {
      List<Node> nodes = new ArrayList<Node>();
      
      for (Node node : board.getNodes())
         if (node.isPurchaseable())
            nodes.add(node);
      
      return nodes;
   }

   public List<Node> getOwnedNodes(Player exclude)
   {
      List<Node> nodes = new ArrayList<Node>();
      
      for (Node node : board.getNodes())
         if (node.getOwner() != null && !node.getOwner().equals(exclude))
            nodes.add(node);
      
      return nodes;
   }
   
   public Node getStartNode()
   {
      return board.getStartNode();
   }
   
   public int getFuelStationsRemaining()
   {
      return fuelStationsRemaining;
   }
   
   public int getTotalWorth(Player player)
   {
      int totalWorth = player.getCash();
      
      totalWorth += getFuelStationPrice() * player.getFuelStations();
      
      for (Node node : player.getOwnedNodes())
         totalWorth += getNodePrice(node);
      
      return totalWorth;
   }

   public int getNodePrice()
   {
      return getNodePrice(getCurrentPlayer().getCurrentNode());
   }
   
   public int getNodePrice(Node node)
   {
      return node.hasFuelStation() ? node.getPrice() + getFuelStationPrice() : node.getPrice();
   }

   public int getFuelStationPrice()
   {
      return ruleSet.getValue(RuleSet.FUEL_STATION_PRICE);
   }

   public void setPlayers(List<Player> players)
   {
      this.players = players;
      
      playerMap = new HashMap<Integer, Player>();
      
      for (Player player : players)
         playerMap.put(player.getNumber(), player);
   }
   
   public List<Player> getPlayers()
   {
      return Collections.unmodifiableList(players);
   }

   public Collection<Node> getNodes()
   {
      return board.getNodes();
   }

   public List<Player> getTradeablePlayers(Player exclude)
   {
      List<Player> tradeablePlayers = new ArrayList<Player>();
      
      for (Player player : players)
         if (isTradeAllowed(player) && !player.equals(exclude))
            tradeablePlayers.add(player);
      
      return tradeablePlayers;
   }

   public boolean isTradeAllowed()
   {
      return isTradeAllowed(getCurrentPlayer());
   }
   
   public boolean isTradeAllowed(Player player)
   {
      return !player.getOwnedNodes().isEmpty() || player.getCash() > 0;
   }
   
   public int getMinimumFuel()
   {
      return ruleSet.getValue(RuleSet.MINIMUM_FUEL);
   }
   
   public int getLowFuel()
   {
      return ruleSet.getValue(RuleSet.LOW_FUEL);
   }
   
   public int getFuelPrice()
   {
      Player player = getCurrentPlayer();
      Node node = player.getCurrentNode();

      return getFuelPrice(player, node);
   }
   
   public int getFuelPrice(Player player, Node node)
   {
      return node.getFuelPrice(ruleSet, player);
   }

   public boolean isFuelStationSalableNormally()
   {
      Player player = getCurrentPlayer();
      Node node = player.getCurrentNode();
      
      return isFuelStationSalableNormally(player, node);
   }
   
   protected boolean isFuelStationSalableNormally(Player player, Node node)
   {
      return isFuelStationSalableAtAll(player)
         && RuleSet.isTransactionAvailable(ruleSet.getValue(RuleSet.FUEL_STATION_BUYBACK_AVAILABILITY), node);
   }
   
   public boolean isFuelStationSalableForDebtSettlement(Player player)
   {
      return isFuelStationSalableAtAll(player);
   }
   
   private boolean isFuelStationSalableAtAll(Player player)
   {
      return player.getFuelStations() > 0;
   }

   public boolean isNodeSalableNormally()
   {
      Player player = getCurrentPlayer();
      Node node = player.getCurrentNode();
      
      return isNodeSalableAtAll(player)
         && isNodeSalableNormally(player, node);
   }
   
   protected boolean isNodeSalableNormally(Player player, Node node)
   {
      return isNodeSalableAtAll(player)
         && RuleSet.isTransactionAvailable(ruleSet.getValue(RuleSet.NODE_BUYBACK_AVAILABILITY), node);
   }

   public boolean isNodeSalableForDebtSettlement(Player player)
   {
      return isNodeSalableAtAll(player);
   }
   
   private boolean isNodeSalableAtAll(Player player)
   {
      return !player.getOwnedNodes().isEmpty();
   }

   public boolean isFuelCritical()
   {
      Player player = getCurrentPlayer();
      
      return isFuelCritical(player, player.getCurrentNode());
   }
   
   protected boolean isFuelCritical(Player player, Node node)
   {
      return player.getFuel() < getMinimumFuel() && node.usesFuel();
   }

   // TODO: Should be false if the player did not just advance to this node (e.g., insufficient fuel)
   // or, given negligence TODO below, should this be okay?
   // (A player relinquished the node he was on, so I started to wonder.)
   public boolean isNodePurchaseable()
   {
      Player player = getCurrentPlayer();
      Node node = player.getCurrentNode();
      
      return isNodePurchaseable(player, node);
   }
   
   protected boolean isNodePurchaseable(Player player, Node node)
   {
      return node.isPurchaseable() && player.getCash() >= getNodePrice(node);
   }
   
   public boolean isFuelPurchaseable()
   {
      Player player = getCurrentPlayer();
      Node node = player.getCurrentNode();

      return isFuelPurchaseable(player, node);
   }
   
   protected boolean isFuelPurchaseable(Player player, Node node)
   {
      return node.hasFuel(ruleSet.getValue(RuleSet.FUEL_AVAILABLE_ON_UNOWNED_NODE))
         && player.getCash() >= node.getFuelPrice(ruleSet, player)
         && player.getFuel() < getMaximumFuel();
   }
   
   public List<Node> getFuelStationPlaceableNodes(Player player)
   {
      boolean canPlaceOnAnyNode = ruleSet.getValue(RuleSet.CAN_PLACE_FUEL_STATIONS_ON_ANY_NODE);
      List<Node> nodes = new ArrayList<Node>();
      
      for (Node node : player.getOwnedNodes())
      {
         if (!node.hasFuelStation() && node.canHaveFuelStation())
         {
            if (canPlaceOnAnyNode || player.getCurrentNode().equals(node))
               nodes.add(node);
         }
      }
      
      return Collections.unmodifiableList(nodes);
   }
   
   public boolean isFuelStationPlaceable()
   {
      Player player = getCurrentPlayer();
      
      return isFuelStationPlaceable(player);
   }
   
   protected boolean isFuelStationPlaceable(Player player)
   {
      if (player.getFuelStations() <= 0)
         return false;
      
      return !getFuelStationPlaceableNodes(player).isEmpty();
   }
   
   public boolean isFuelStationPurchaseable()
   {
      Player player = getCurrentPlayer();
      Node node = player.getCurrentNode();
      
      return isFuelStationPurchaseable(player, node);
   }
   
   protected boolean isFuelStationPurchaseable(Player player, Node node)
   {
      return RuleSet.isTransactionAvailable(ruleSet.getValue(RuleSet.FUEL_STATION_PURCHASE_AVAILABILITY), node)
         && hasUnpurchasedFuelStation() && player.getCash() >= getFuelStationPrice();
   }

   protected boolean hasUnpurchasedFuelStation()
   {
      return fuelStationsRemaining > 0;
   }

   private int getMaximumFuel()
   {
      return ruleSet.getValue(RuleSet.MAXIMUM_FUEL);
   }
   
   public int getMaximumPurchaseableFuel()
   {
      Player player = getCurrentPlayer();
      
      return getMaximumPurchaseableFuel(player, player.getCurrentNode());
   }
   
   public int getMaximumPurchaseableFuel(Player player, Node node)
   {
      int spaceInTank = getMaximumFuel() - player.getFuel();
      int fuelPrice = node.getFuelPrice(ruleSet, player);
      int affordableFuel = fuelPrice == 0 ? getMaximumFuel() : player.getCash() / fuelPrice;
      
      return Math.min(spaceInTank, affordableFuel);
   }

   // TODO: Should this be allowed when player is stuck on a node with low fuel?
   public boolean isNegligenceTakeoverAllowed()
   {
      Player player = getCurrentPlayer();
      
      return isNegligenceTakeoverAllowed(player, player.getCurrentNode());
   }
   
   protected boolean isNegligenceTakeoverAllowed(Player player, Node node)
   {
      return node.getOwner() != null && node.canHaveFuelStation() && !node.hasFuelStation()
         && player.getFuel() <= getLowFuel() && !player.equals(node.getOwner())
         && (player.getCash() >= node.getPrice() || isTradeAllowed(player));
      // TODO isTradeAllowed clause allowed takeover despite insufficient funds
      // need separate way to determine whether pre-land is required that includes this clause
      // while the button itself excludes it
   }

   public boolean isLaserBattleEverAllowed()
   {
      return ruleSet.getValue(RuleSet.LASER_BATTLES_ALLOWED);
   }
   
   public boolean isLaserBattleAllowed()
   {
      return isLaserBattleAllowed(getCurrentPlayer());
   }
   
   public abstract boolean isLaserBattleAllowed(Player player);

   public List<Player> getLaserTargetablePlayers()
   {
      return getLaserTargetablePlayers(getCurrentPlayer());
   }
   
   List<Player> getLaserTargetablePlayers(Player player)
   {
      List<Player> targetablePlayers = new ArrayList<Player>();
      
      // Can't fire from start node if rules don't allow it.
      if (player.getCurrentNode().isStartNode()
            && !ruleSet.getValue(RuleSet.LASERS_CAN_FIRE_FROM_START))
         return targetablePlayers;
      
      int laserBattleFuelCost = ruleSet.getValue(RuleSet.LASER_BATTLE_FUEL_COST);
      int maximumDistance;
      
      if (laserBattleFuelCost == 0)
         maximumDistance = Integer.MAX_VALUE;
      else
         maximumDistance = player.getFuel() / laserBattleFuelCost - 1;
      
      maximumDistance = Math.min(maximumDistance, ruleSet.getValue(RuleSet.LASER_BATTLE_MAXIMUM_DISTANCE));
      
      // Can't fire if we don't even have enough fuel for a zero-distance shot.
      if (maximumDistance < 0)
         return targetablePlayers;
      
      for (Player otherPlayer : getPlayers())
      {
         // Can't fire on yourself because lasers don't point that way.
         if (player.equals(otherPlayer))
            continue;
         
         // Can't beat dead horses.
         if (otherPlayer.isGameOver())
            continue;
         
         // Can't fire at start node if rules don't allow it.
         if (otherPlayer.getCurrentNode().isStartNode()
               && !ruleSet.getValue(RuleSet.LASERS_CAN_FIRE_AT_START))
            continue;
         
         // Can fire if other player is close enough.
         if (board.getDistanceBetweenNodes(player.getCurrentNode(), otherPlayer.getCurrentNode()) <= maximumDistance)
            targetablePlayers.add(otherPlayer);
      }
      
      return targetablePlayers;
   }
   
   public int getLaserBattleFuelCost(Node targetedNode)
   {
      return (board.getDistanceBetweenNodes(getCurrentPlayer().getCurrentNode(), targetedNode) + 1)
         * ruleSet.getValue(RuleSet.LASER_BATTLE_FUEL_COST);
   }
   
   public boolean isBypassEverAllowed()
   {
      return ruleSet.getValue(RuleSet.BYPASS_ALLOWED);
   }
   
   public void setRuleSet(RuleSet ruleSet)
   {
      this.ruleSet = ruleSet;
   }

   public RuleSet getRuleSet()
   {
      return ruleSet;
   }

   public void setBoard(Board board)
   {
      this.board = board;
   }

   public void fixOwnedNodes()
   {
      for (Node node : board.getNodes())
      {
         Player player = node.getOwner();
         
         if (player != null)
         {
            player.addNode(node);
         }
      }
      
      for (Player player : players)
      {
         player.fixGroupCounts();
      }
   }
}
