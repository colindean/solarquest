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

   public boolean isFuelStationSalable()
   {
      return isFuelStationSalable(getCurrentPlayer());
   }
   
   private boolean isFuelStationSalable(Player player)
   {
      return player.getFuelStations() > 0;
   }

   public boolean isNodeSalable()
   {
      return isNodeSalable(getCurrentPlayer());
   }
   
   protected boolean isNodeSalable(Player player)
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
   
   public boolean isFuelStationPlaceable()
   {
      Player player = getCurrentPlayer();
      Node node = player.getCurrentNode();
      
      return isFuelStationPlaceable(player, node);
   }
   
   protected boolean isFuelStationPlaceable(Player player, Node node)
   {
      return !node.hasFuelStation() && node.canHaveFuelStation() && player.equals(node.getOwner()) && player.getFuelStations() > 0;
   }
   
   public boolean isFuelStationPurchaseable()
   {
      Player player = getCurrentPlayer();
      Node node = player.getCurrentNode();
      
      return isFuelStationPurchaseable(player, node);
   }
   
   protected boolean isFuelStationPurchaseable(Player player, Node node)
   {
      return (ruleSet.getValue(RuleSet.FUEL_STATIONS_AVAILABLE_EVERYWHERE) || node.getType() == Node.Type.STATION)
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

   public void setRuleSet(RuleSet ruleSet)
   {
      this.ruleSet = ruleSet;
   }

   public void setBoard(Board board)
   {
      this.board = board;
   }
}
