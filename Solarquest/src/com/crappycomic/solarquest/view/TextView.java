package com.crappycomic.solarquest.view;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import com.crappycomic.solarquest.model.*;
import com.crappycomic.solarquest.model.ModelMessage.Type;

public class TextView extends View implements Runnable
{
   private BlockingQueue<ViewMessage> messages = new LinkedBlockingQueue<ViewMessage>();
   
   private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

   @Override
   protected void initialize()
   {
      new Thread(this).start();
   }
   
   @Override
   public void run()
   {
      while (looping)
      {
         try
         {
            processMessage(messages.take());
         }
         catch (InterruptedException ie)
         {
            looping = false;
            break;
         }
      }
   }
   
   @Override
   public void receiveMessage(ViewMessage message)
   {
      messages.add(message);      
   }
   
   @Override
   public void playerChangedCash(Player player, int amount)
   {
      System.out.print(player.getName());
      System.out.print("'s cash changed by $");
      System.out.print(amount);
      System.out.print(", totaling $");
      System.out.println(player.getCash());
   }

   @Override
   public void playerAdvancedToNode(Player player, Node node)
   {
      System.out.print(player.getName());
      System.out.print(" advanced to ");
      System.out.println(node.getID());
   }

   @Override
   public void playerChangedFuel(Player player, int amount)
   {
      System.out.print(player.getName());
      System.out.print("'s fuel changed by ");
      System.out.print(amount);
      System.out.print(", totaling ");
      System.out.println(player.getFuel());
   }

   @Override
   public void playerChangedFuelStations(Player player, int amount)
   {
      System.out.print(player.getName());
      System.out.print("'s fuel stations changed by ");
      System.out.print(amount);
      System.out.print(", totaling ");
      System.out.println(player.getFuelStations());
   }

   @Override
   public void playerLandedOnStartNode(Player player)
   {
      System.out.print(player.getName());
      System.out.println(" landed on the start node");
   }

   @Override
   public void playerPassedStartNode(Player player)
   {
      System.out.print(player.getName());
      System.out.println(" passed the start node");
   }

   @Override
   public void playerLostDueToInsufficientFuel(Player player)
   {
      System.out.print(player.getName());
      System.out.println(" was removed for having insufficient fuel to move");
   }

   @Override
   public void playerWonDisputeWithLeague(Player player)
   {
      System.out.print(player.getName());
      System.out.println(" won a dispute with the Federation");
   }

   @Override
   public void playerLostDisputeWithLeague(Player player)
   {
      System.out.print(player.getName());
      System.out.println(" lost a dispute with the Federation");
   }

   @Override
   public void playerWonDisputeWithPlayer(Player player)
   {
      System.out.print(player.getName());
      System.out.println(" won a dispute with a player");
   }

   @Override
   protected void promptForPreRollActions()
   {
      Player player = model.getCurrentPlayer();
      boolean fuelIsCritical = model.isFuelCritical();
      boolean fuelIsPurchaseable = model.isFuelPurchaseable();
      boolean fuelStationIsPlaceable = model.isFuelStationPlaceable();
      boolean fuelStationIsPurchaseable = model.isFuelStationPurchaseable();
      boolean tradeIsAllowed = model.isTradeAllowed();
      
      if (fuelIsCritical)
      {
         System.out.println("**** WARNING: Fuel level is critical. If you attempt to move without refueling to " + model.getMinimumFuel() + " or more hyrdons, you will lose! ****");
      }
      
      System.out.println(player.getName() + "'s pre-roll actions:");
      System.out.println("Q) Quit");
      if (fuelIsPurchaseable)
         System.out.println("R) Refuel for $" + model.getFuelPrice() + " per hydron");
      if (fuelStationIsPlaceable)
         System.out.println("P) Place fuel station");
      if (fuelStationIsPurchaseable)
         System.out.println("F) Buy fuel station");
      if (tradeIsAllowed)
         System.out.println("T) Trade properties or cash with another player");
      if (fuelIsCritical)
         System.out.println("Enter) Declare spaceship stranded");
         /*
          * Suggested alternate actions:
          *  THIS IS CETI ALPHA V!
          *  Take off helmet outside airlock
          *  Peperony and chease
          */
      else
         System.out.println("Enter) Roll");

      String command = prompt();

      if (command.equalsIgnoreCase("Q"))
      {
         sendMessage(Type.QUIT);
      }
      else if (fuelIsPurchaseable && command.equalsIgnoreCase("R"))
      {
         sendMessage(Type.PURCHASE_FUEL, promptWithDefault(0));
      }
      else if (fuelStationIsPlaceable && command.equalsIgnoreCase("P"))
      {
         sendMessage(Type.PLACE_FUEL_STATION);
      }
      else if (fuelStationIsPurchaseable && command.equalsIgnoreCase("F"))
      {
         sendMessage(Type.PURCHASE_FUEL_STATION);
      }
      else if (tradeIsAllowed && command.equalsIgnoreCase("T"))
      {
         promptForTrade(player);
      }
      else
      {      
         sendMessage(Type.NO_PRE_ROLL);
      }
   }
   
   @Override
   protected void promptForPreLandActions()
   {
      boolean negligenceTakeoverIsAllowed = model.isNegligenceTakeoverAllowed();

      if (negligenceTakeoverIsAllowed)
         System.out.println("N) Perform takeover due to fuel negligence for $" + model.getNodePrice());
      System.out.println("Enter) Land normally");

      String command = prompt();
      
      if (negligenceTakeoverIsAllowed && command.equalsIgnoreCase("N"))
      {
         sendMessage(Type.NEGLIGENCE_TAKEOVER);
      }
      else
      {
         sendMessage(Type.NO_PRE_LAND);
      }
   }

   @Override
   protected void promptForPostRollActions()
   {
      boolean fuelIsCritical = model.isFuelCritical();
      boolean nodeIsPurchaseable = model.isNodePurchaseable();
      boolean fuelIsPurchaseable = model.isFuelPurchaseable();
      boolean fuelStationIsPlaceable = model.isFuelStationPlaceable();
      boolean fuelStationIsPurchaseable = model.isFuelStationPurchaseable();

      if (fuelIsCritical)
      {
         System.out.println("**** WARNING: Fuel level is critical. If you attempt to move during the next turn without refueling to " + model.getMinimumFuel() + " or more hyrdons, you will lose! ****");
         if (nodeIsPurchaseable)
            System.out.println("(Note that this will be your only chance to purchase this property.)");
      }
      
      System.out.println(model.getCurrentPlayer().getName() + "'s post-roll actions:");

      System.out.println("Q) Quit");
      if (nodeIsPurchaseable)
         System.out.println("B) Buy property for $" + model.getNodePrice());
      if (fuelIsPurchaseable)
         System.out.println("R) Refuel for $" + model.getFuelPrice() + " per hydron");
      if (fuelStationIsPlaceable)
         System.out.println("P) Place fuel station");
      if (fuelStationIsPurchaseable)
         System.out.println("F) Buy fuel station");
      System.out.println("Enter) End turn");

      String command = prompt();

      if (command.equalsIgnoreCase("Q"))
      {
         sendMessage(Type.QUIT);
      }
      else if (nodeIsPurchaseable && command.equalsIgnoreCase("B"))
      {
         sendMessage(Type.PURCHASE_NODE);
      }
      else if (fuelIsPurchaseable && command.equalsIgnoreCase("R"))
      {
         sendMessage(Type.PURCHASE_FUEL, promptWithDefault(0));
      }
      else if (fuelStationIsPlaceable && command.equalsIgnoreCase("P"))
      {
         sendMessage(Type.PLACE_FUEL_STATION);
      }
      else if (fuelStationIsPurchaseable && command.equalsIgnoreCase("F"))
      {
         sendMessage(Type.PURCHASE_FUEL_STATION);
      }
      else
      {
         sendMessage(Type.NO_POST_ROLL);
      }
   }

   @Override
   public void playerHasMultipleAllowedMoves(Player player, List<Node> allowedMoves)
   {
      System.out.println(player.getName() + " has multiple moves possible and must choose:");

      for (int ndx = 0; ndx < allowedMoves.size(); ndx++)
      {
         System.out.println(ndx + ") " + allowedMoves.get(ndx).getID());
      }

      int choice = promptWithDefault(0);

      if (choice < 0 || choice >= allowedMoves.size())
         choice = 0;

      sendMessage(Type.CHOOSE_ALLOWED_MOVE, allowedMoves.get(choice).getID());
   }

   @Override
   public void playerRemainedStationary(Player player)
   {
      System.out.print(player.getName());
      System.out.println(" did not advance");
   }

   @Override
   public void playerRolled(Player player, Pair<Integer, Integer> dice)
   {
      System.out.print(player.getName());
      System.out.print(" rolled ");
      System.out.println(dice.getFirst() + ", " + dice.getSecond());
   }

   private int promptWithDefault(int defaultValue)
   {
      System.out.print("[" + defaultValue + "] ");
      
      try
      {
         return Integer.parseInt(prompt());
      }
      catch (NumberFormatException nfe)
      {
         return defaultValue;
      }
   }
   
   private String prompt()
   {
      System.out.print("> ");

      try
      {
         return reader.readLine();
      }
      catch (IOException ioe)
      {
         return "";
      }
   }
   
   @Override
   public void playerPurchasedNode(Player player, Node node)
   {
      System.out.print(player.getName());
      System.out.print(" purchased ");
      System.out.println(node.getID());
   }

   @Override
   public void playerPlacedFuelStation(Player player, Node node)
   {
      System.out.print(player.getName());
      System.out.print(" placed a fuel station on ");
      System.out.println(node.getID());
   }

   @Override
   public void playerPurchasedFuelStation(Player player)
   {
      System.out.print(player.getName());
      System.out.println(" purchased a fuel station from the Federation");
   }
   
   @Override
   public void playerDrewCard(Player player, Card card)
   {
      System.out.print(player.getName());
      System.out.print(" drew a card: ");
      System.out.println(card.getID());
   }
   
   @Override
   public void playerWon(Player player)
   {
      System.out.print(player.getName());
      System.out.println(" won the game!");
   }
   
   @Override
   protected void promptForDebtSettlement(Pair<Player, Integer> debt)
   {
      Player player = model.getCurrentPlayer();
      boolean fuelStationIsSalable = model.isFuelStationSalable();
      boolean nodeIsSalable = model.isNodeSalable();
      boolean tradeIsAllowed = model.isTradeAllowed();
      
      System.out.println(player.getName() + " owes " + debt.getFirst().getName() + " a debt of $" + debt.getSecond() + " but has too little cash to pay it.");
      System.out.println("With $" + player.getCash() + " on hand, " + player.getName() + " must raise $" + (debt.getSecond() - player.getCash()));

      if (fuelStationIsSalable)
         System.out.println("F) Sell a fuel station back to Federation for $" + model.getFuelStationPrice());
      if (nodeIsSalable)
         System.out.println("P) Sell a property back to Federation for deed price (plus $" + model.getFuelStationPrice() + " if it has a fuel station)");
      if (tradeIsAllowed)
         System.out.println("T) Trade properties or cash with another player");
      System.out.println("Enter) Declare bankruptcy (all assets will go to " + debt.getFirst().getName() + ")");
      
      String command = prompt();
      
      if (fuelStationIsSalable && command.equalsIgnoreCase("F"))
      {
         sendMessage(Type.SELL_FUEL_STATION);
      }
      else if (nodeIsSalable && command.equalsIgnoreCase("P"))
      {
         List<Node> nodes = new ArrayList<Node>(player.getOwnedNodes());
         
         System.out.println("Owned properties:");
         
         for (int ndx = 0; ndx < nodes.size(); ndx++)
         {
            Node node = nodes.get(ndx);
            
            System.out.print(ndx + ") " + node.getID());
            if (node.hasFuelStation())
               System.out.print(" and fuel station");
            System.out.println(": " + model.getNodePrice(node));
         }
         
         int choice = promptWithDefault(0);
         
         if (choice < 0 || choice >= nodes.size())
            choice = 0;
         
         sendMessage(Type.SELL_NODE, nodes.get(choice).getID());
      }
      else if (command.equalsIgnoreCase("T"))
      {
         promptForTrade(player);
      }
      else
      {
         sendMessage(Type.DECLARE_BANKRUPTCY);
      }
   }
   
   protected void promptForTrade(Player player)
   {
      List<Player> players = new ArrayList<Player>(model.getPlayers());
      Player trader;
      
      players.remove(model.getCurrentPlayer());
      
      if (players.size() == 1)
      {
         trader = players.get(0);
      }
      else
      {
         System.out.println("Players:");
         
         for (int ndx = 0; ndx < players.size(); ndx++)
         {
            System.out.println(ndx + ") " + players.get(ndx).getName());
         }
         
         trader = players.get(promptWithDefault(0));
      }
      
      List<Node> ownedByPlayer = new ArrayList<Node>(player.getOwnedNodes());
      List<Node> ownedByTrader = new ArrayList<Node>(trader.getOwnedNodes());
      List<Node> offered = new ArrayList<Node>();
      List<Node> requested = new ArrayList<Node>();
      int cash = 0;
      
      while (true)
      {
         boolean canOfferProperty = !ownedByPlayer.isEmpty();
         boolean canWithdrawProperty = !offered.isEmpty();
         boolean canRequestProperty = !ownedByTrader.isEmpty();
         boolean canForgoProperty = !requested.isEmpty();
         
         System.out.println("Choose an action:");
         if (canOfferProperty)
            System.out.println("O) Offer property");
         if (canWithdrawProperty)
            System.out.println("W) Withdraw property from offer");
         if (canRequestProperty)
            System.out.println("R) Request property");
         if (canForgoProperty)
            System.out.println("F) Forgo requested property");
         System.out.println("C) Change cash offer/request");
         System.out.println("Enter) Complete trade");
         
         String action = prompt();
         
         if (action.equalsIgnoreCase("O"))
         {
            System.out.println("Choose a property to offer:");
            
            for (int ndx = 0; ndx < ownedByPlayer.size(); ndx++)
               System.out.println(ndx + ") " + ownedByPlayer.get(ndx).getID());
            
            int choice = promptWithDefault(0);
            
            if (choice < 0 || choice >= ownedByPlayer.size())
               choice = 0;
            
            offered.add(ownedByPlayer.remove(choice));
         }
         else if (action.equalsIgnoreCase("W"))
         {
            System.out.println("Choose a property to withdraw:");
            
            for (int ndx = 0; ndx < offered.size(); ndx++)
               System.out.println(ndx + ") " + offered.get(ndx).getID());
            
            int choice = promptWithDefault(0);
            
            if (choice < 0 || choice >= offered.size())
               choice = 0;
            
            ownedByPlayer.add(offered.remove(choice));
         }
         else if (action.equalsIgnoreCase("R"))
         {
            System.out.println("Choose a property to request:");
            
            for (int ndx = 0; ndx < ownedByTrader.size(); ndx++)
               System.out.println(ndx + ") " + ownedByTrader.get(ndx).getID());
            
            int choice = promptWithDefault(0);
            
            if (choice < 0 || choice >= ownedByTrader.size())
               choice = 0;
            
            requested.add(ownedByTrader.remove(choice));
         }
         else if (action.equalsIgnoreCase("F"))
         {
            System.out.println("Choose a property to forgo:");
            
            for (int ndx = 0; ndx < requested.size(); ndx++)
               System.out.println(ndx + ") " + requested.get(ndx).getID());
            
            int choice = promptWithDefault(0);
            
            if (choice < 0 || choice >= requested.size())
               choice = 0;
            
            ownedByTrader.add(requested.remove(choice));
         }
         else if (action.equalsIgnoreCase("C"))
         {
            System.out.println("Enter amount of cash to trade (positive is offered, negative is requested)");
            System.out.println("You may offer a maximum of $" + player.getCash());
            System.out.println("You may request a maximum of $" + trader.getCash());
            // TODO: since 1988 rules allow sale of fuel stations at any time, the implication here is that
            // the player could add the value of any unplaced fuel stations to the request, at which point the
            // trader would have to sell stations to accept the trade
            
            int choice = promptWithDefault(0);
            
            if (choice > player.getCash())
               choice = player.getCash();
            else if (choice < -trader.getCash())
               choice = -trader.getCash();
            
            cash = choice;
         }
         else
         {
            break;
         }
         
         if (!offered.isEmpty())
         {
            System.out.println("Offered:");
            for (Node node : offered)
               System.out.println("\t" + node.getID());
         }
         
         if (!requested.isEmpty())
         {
            System.out.println("Requested:");
            for (Node node : requested)
               System.out.println("\t" + node.getID());
         }
         
         if (cash != 0)
            System.out.println("Cash: " + cash);
      }
      
      if (!offered.isEmpty() || !requested.isEmpty() || cash != 0)
         sendMessage(Type.TRADE, new Trade(player, trader, offered, requested, cash));
   }
   
   @Override
   protected void promptForTradeDecision(Trade trade)
   {
      System.out.println(trade.toString());
      System.out.print("Does " + trade.getTo() + " accept? (Y/N)");
      
      String decision = prompt();

      sendMessage(Type.TRADE_COMPLETED, decision.equalsIgnoreCase("Y"));
   }

   @Override
   protected void tradeAccepted(Trade trade)
   {
      System.out.println(trade.getFrom() + "'s proposed trade with " + trade.getTo() + " was accepted");      
   }
   
   @Override
   protected void tradeRejected(Trade trade)
   {
      System.out.println(trade.getFrom() + "'s proposed trade with " + trade.getTo() + " was rejected");      
   }
   
   @Override
   protected void playerLostDueToStranding(Player player)
   {
      System.out.println(player.getName() + " failed to refuel and was stranded");
   }
   
   @Override
   protected void playerRelinquishedNode(Player player, Node node)
   {
      System.out.println(player.getName() + " relinquished ownership of " + node.getID());      
   }
   
   @Override
   protected void playerObtainedNode(Player player, Node node)
   {
      System.out.println(player.getName() + " obtained ownership of " + node.getID());
   }
   
   @Override
   protected void playerLostDueToBankruptcy(Player player)
   {
      System.out.println(player.getName() + " lost due to bankruptcy");      
   }
   
   @Override
   protected void playerSoldNode(Player player, Node node)
   {
      System.out.println(player.getName() + " sold " + node.getID() + " to the Federation");
   }
   
   @Override
   protected void playerSoldFuelStation(Player player)
   {
      System.out.println(player.getName() + " sold a fuel station to the Federation");      
   }

   @Override
   protected void playerHadNoPropertyToLose(Player player)
   {
      System.out.print(player.getName());
      System.out.println(" had no property to lose");
   }

   @Override
   protected void playerHadNoPropertyToWin(Player player)
   {
      System.out.print(player.getName());
      System.out.println(" had no property to win");
   }

   @Override
   protected void promptForPropertyLostToLeague()
   {
      Player player = model.getCurrentPlayer();
      List<Node> nodes = new ArrayList<Node>(player.getOwnedNodes());
      
      System.out.println("Choose a property to relinquish to the Federation:");
      
      for (int ndx = 0; ndx < nodes.size(); ndx++)
         System.out.println(ndx + ") " + nodes.get(ndx).getID());
      
      int choice = promptWithDefault(0);
      
      if (choice < 0 || choice >= nodes.size())
         choice = 0;
      
      sendMessage(Type.CHOOSE_PROPERTY_LOST_TO_LEAGUE, nodes.get(choice).getID());
   }

   @Override
   protected void promptForPropertyWonFromLeague()
   {
      List<Node> nodes = new ArrayList<Node>(model.getUnownedNodes());
      
      System.out.println("Choose a property to obtain from the Federation:");
      
      for (int ndx = 0; ndx < nodes.size(); ndx++)
         System.out.println(ndx + ") " + nodes.get(ndx).getID());
      
      int choice = promptWithDefault(0);
      
      if (choice < 0 || choice >= nodes.size())
         choice = 0;
      
      sendMessage(Type.CHOOSE_PROPERTY_LOST_TO_LEAGUE, nodes.get(choice).getID());
   }

   @Override
   protected void promptForPropertyWonFromPlayer()
   {
      List<Node> nodes = new ArrayList<Node>();
      
      System.out.println("Choose a property to obtain from another player:");
      
      for (Player player : model.getPlayers())
      {
         if (player.equals(model.getCurrentPlayer()) || player.getOwnedNodes().isEmpty())
            continue;
         
         System.out.println(player.getName() + ":");
         
         for (Node node : player.getOwnedNodes())
         {
            System.out.println("\t" + nodes.size() + ") " + node.getID());
            nodes.add(node);
         }
      }
      
      int choice = promptWithDefault(0);
      
      if (choice < 0 || choice >= nodes.size())
         choice = 0;
      
      sendMessage(Type.CHOOSE_PROPERTY_WON_FROM_PLAYER, nodes.get(choice).getID());
   }

   @Override
   protected void playerObtainedFreeFuelStation(Player player)
   {
      System.out.println(player.getName() + " obtained a free fuel station");      
   }
}
