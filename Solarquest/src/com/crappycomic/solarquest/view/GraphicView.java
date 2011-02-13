// Solarquest
// Copyright (C) 2011 Colin Bartolome
// Licensed under the GPL. See LICENSE.txt for details.

package com.crappycomic.solarquest.view;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.Action;

import com.crappycomic.solarquest.model.*;
import com.crappycomic.solarquest.model.ModelMessage.Type;

public class GraphicView extends View
{
   private static final int GAP = 4;
   
   private String boardImage;
   
   private Map<String, NodeView> nodeViews;
   
   private Map<String, DeedGroupView> deedGroupViews;
   
   private Map<String, CardView> cardViews;
   
   private JFrame frame;
   
   private JFrame manualFrame;
   
   private BoardPanel boardPanel;
   
   private PlayersPanel playersPanel;
   
   private StatusPanel statusPanel;
   
   private ActionsPanel actionsPanel;
   
   private NodePanel nodePanel;
   
   @Override
   protected void initialize()
   {
      SwingUtilities.invokeLater(new Runnable()
      {
         @Override
         public void run()
         {
            try
            {
               new ViewXMLLoader().load(model.getDefaultView(), GraphicView.this);
            }
            catch (Exception e)
            {
               System.err.println("Failed to load view XML: " + e.toString());
               System.exit(1);
            }

            frame = new JFrame("Solarquest");
            
            JMenuBar menuBar;
            JMenu menu;
            Action action;
            
            frame.setJMenuBar(menuBar = new JMenuBar());
            menuBar.add(menu = new JMenu("File"));
            menu.setMnemonic('F');
            
            menu.add(new JMenuItem(action = new AbstractAction("Save and Quit")
            {
               private static final long serialVersionUID = 0;
               
               @Override
               public void actionPerformed(ActionEvent evt)
               {
                  saveAndQuit();
               }
            }));
            action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
            action.setEnabled(canSave());
            
            menu.add(new JMenuItem(action = new AbstractAction("Quit")
            {
               private static final long serialVersionUID = 0;
               
               @Override
               public void actionPerformed(ActionEvent evt)
               {
                  quit();
               }
            }));
            action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_Q);
            
            menuBar.add(Box.createHorizontalGlue());
            
            menuBar.add(menu = new JMenu("Help"));
            menu.setMnemonic('H');
            
            menu.add(new JMenuItem(action = new AbstractAction("Instruction Manual")
            {
               private static final long serialVersionUID = 0;
               
               @Override
               public void actionPerformed(ActionEvent e)
               {
                  if (manualFrame == null)
                     manualFrame = new ManualFrame(getNodeDisplayName(model.getStartNode().getID()), model.getRuleSet());
                  
                  manualFrame.setVisible(true);
               }
            }
            ));
            action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_M);
            
            BorderLayout layout = new BorderLayout();
            
            layout.setHgap(GAP);
            layout.setVgap(GAP);
            frame.setLayout(layout);
            
            JPanel centerPanel = new JPanel();
            
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
            centerPanel.add(boardPanel = new BoardPanel(GraphicView.this, model, boardImage));
            centerPanel.add(statusPanel = new StatusPanel());
            frame.add(centerPanel);
            
            frame.add(playersPanel = new PlayersPanel(GraphicView.this, model), BorderLayout.WEST);
            
            JPanel eastPanel = new JPanel();
            
            eastPanel.setLayout(new BoxLayout(eastPanel, BoxLayout.Y_AXIS));
            eastPanel.add(actionsPanel = new ActionsPanel(GraphicView.this, model));
            eastPanel.add(nodePanel = new NodePanel(GraphicView.this, model));
            frame.add(eastPanel, BorderLayout.EAST);
      
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
            
            frame.addWindowListener(new WindowAdapter()
            {
               @Override
               public void windowClosing(WindowEvent evt)
               {
                  quit();
               }
            });
      
            boardPanel.adjustScales();
            
            Node currentNode = model.getCurrentPlayer().getCurrentNode();
            
            boardPanel.updateBoard(getNodeCoords(currentNode.getID()), false);
            nodePanel.updateNode(currentNode);
         }
      });
   }
   
   @Override
   protected void gameOver()
   {
      super.gameOver();
      
      actionsPanel.showGameOver();
   }

   @Override
   protected void playerAdvancedToNode(Player player, Node node)
   {
      statusPanel.appendText(player.getName() + " advanced to " + getNodeDisplayName(node.getID()) + ".");
      boardPanel.updateBoard(getNodeCoords(node.getID()));
      playersPanel.updateNode(player);
      nodePanel.updateNode(node);
   }

   @Override
   protected void playerChangedCash(Player player, int amount)
   {
      statusPanel.appendText(player.getName() + (amount > 0 ? " gained " : " spent ") + "$" + Math.abs(amount) + ".");
      playersPanel.updateCash(player, player.getCash());
      playersPanel.updateTotalWorth(player);
   }

   @Override
   protected void playerChangedFuel(Player player, int amount)
   {
      statusPanel.appendText(player.getName() + (amount > 0 ? " added " : " used ") + Math.abs(amount)
         + " hydron" + (amount == 1 || amount == -1 ? "" : "s") + " of fuel.");
      playersPanel.updateFuel(player, player.getFuel());      
   }

   @Override
   protected void playerChangedFuelStations(Player player, int amount)
   {
      statusPanel.appendText(player.getName() + (amount > 0 ? " gained " : " lost ") + Math.abs(amount)
         + " fuel station" + (amount == 1 || amount == -1 ? "" : "s") + ".");
      playersPanel.updateFuelStations(player, player.getFuelStations());
      playersPanel.updateTotalWorth(player);
   }

   @Override
   protected void playerDrewCard(Player player, Card card)
   {
      statusPanel.appendText(player.getName() + " drew a Red Shift card: " + getCardDisplayName(card.getID()) + ".");
   }

   @Override
   protected void playerHadNoNodeToLose(Player player)
   {
      statusPanel.appendText(player.getName() + " did not have to relinquish a property because none was owned.");      
   }

   @Override
   protected void playerHadNoNodeToWin(Player player)
   {
      statusPanel.appendText(player.getName() + " was unable to obtain control of a property because none was available.");      
   }

   @Override
   protected void playerHasMultipleAllowedMoves(Player player, List<Node> allowedMoves)
   {
      actionsPanel.showChooseAllowedMoveActions(player, allowedMoves);
   }

   @Override
   protected void playerLandedOnStartNode(Player player)
   {
      statusPanel.appendText(player.getName() + " landed on Start.");      
   }

   @Override
   protected void playerLostDisputeWithLeague(Player player)
   {
      statusPanel.appendText(player.getName() + " lost a dispute with the Federation League and must relinquish control of a property.");      
   }

   @Override
   protected void playerLostDueToBankruptcy(Player player)
   {
      statusPanel.appendText(player.getName() + " has declared bankruptcy and has lost.");
      boardPanel.updateBoard();
      playersPanel.gameOver(player);
   }

   @Override
   protected void playerLostDueToInsufficientFuel(Player player)
   {
      statusPanel.appendText(player.getName() + " was forced to move with insufficient fuel and has lost.");
      boardPanel.updateBoard();
      playersPanel.gameOver(player);
   }

   @Override
   protected void playerLostDueToStranding(Player player)
   {
      statusPanel.appendText(player.getName() + " was stranded due to critical fuel and has lost.");
      boardPanel.updateBoard();
      playersPanel.gameOver(player);
   }

   @Override
   protected void playerObtainedNode(Player player, Node node)
   {
      statusPanel.appendText(player.getName() + " obtained control of " + getNodeDisplayName(node.getID()) + ".");
      boardPanel.updateBoard();
      playersPanel.updateTotalWorth(player);
      nodePanel.updateNode(node);
   }

   @Override
   protected void playerPassedStartNode(Player player)
   {
      statusPanel.appendText(player.getName() + " passed Start.");      
   }

   @Override
   protected void playerPlacedFuelStation(Player player, Node node)
   {
      statusPanel.appendText(player.getName() + " placed a fuel station on " + getNodeDisplayName(node.getID()) + ".");
      boardPanel.updateBoard();
      nodePanel.updateNode(node);
   }

   @Override
   protected void playerPurchasedFuelStation(Player player)
   {
      int remaining = model.getFuelStationsRemaining();
      
      statusPanel.appendText(player.getName() + " purchased a fuel station (" + remaining + " remain" + (remaining == 1 ? "s" : "") + ").");      
   }

   @Override
   protected void playerPurchasedNode(Player player, Node node)
   {
      statusPanel.appendText(player.getName() + " purchased " + getNodeDisplayName(node.getID()) + " from the Federation League.");
      boardPanel.updateBoard();
      playersPanel.updateTotalWorth(player);
      nodePanel.updateNode(node);
   }

   @Override
   protected void playerRelinquishedNode(Player player, Node node)
   {
      statusPanel.appendText(player.getName() + " relinquished control of " + getNodeDisplayName(node.getID()) + ".");
      boardPanel.updateBoard();
      playersPanel.updateTotalWorth(player);
      nodePanel.updateNode(node);
   }

   @Override
   protected void playerRemainedStationary(Player player)
   {
      statusPanel.appendText(player.getName() + " was unable to move.");      
   }

   @Override
   protected void playerRolled(Player player, Pair<Integer, Integer> roll)
   {
      statusPanel.appendText(player.getName() + " rolled " + roll.getFirst() + " and " + roll.getSecond() + ".");
   }

   @Override
   protected void playerSoldFuelStation(Player player)
   {
      int remaining = model.getFuelStationsRemaining();
      
      statusPanel.appendText(player.getName() + " sold a fuel station to the Federation League (" + remaining + " remain" + (remaining == 1 ? "s" : "") + ").");      
   }

   @Override
   protected void playerSoldNode(Player player, Node node)
   {
      statusPanel.appendText(player.getName() + " sold " + getNodeDisplayName(node.getID()) + " to the Federation League.");
      boardPanel.updateBoard();
      nodePanel.updateNode(node);
   }

   @Override
   protected void playerWon(Player player)
   {
      statusPanel.appendText(player.getName() + " won the game!");
   }

   @Override
   protected void playerWonDisputeWithLeague(Player player)
   {
      statusPanel.appendText(player.getName() + " won a dispute with the Federation League and may take ownership of an unowned property.");      
   }

   @Override
   protected void playerWonDisputeWithPlayer(Player player)
   {
      statusPanel.appendText(player.getName() + " won a dispute with another player and may take ownership of an owned property.");      
   }
   
   @Override
   protected void playerFiredLasers(Player player, Pair<Integer, Integer> roll)
   {
      statusPanel.appendText(player.getName() + " fired lasers and rolled " + roll.getFirst() + " and " + roll.getSecond() + ".");
   }
   
   @Override
   protected void playerFiredLasersAndMissed(Player player, Player target)
   {
      statusPanel.appendText(player.getName() + " fired lasers and missed " + target.getName() + ".");
   }
   
   @Override
   protected void playerFiredLasersAndCausedDamage(Player player, Player target)
   {
      statusPanel.appendText(player.getName() + " fired lasers and damaged " + target.getName() + ".");
   }
   
   @Override
   protected void playerFiredLasersAndDestroyedAShip(Player player, Player target)
   {
      statusPanel.appendText(player.getName() + " fired lasers and DESTROYED " + target.getName() + "!");
      boardPanel.updateBoard();
      playersPanel.gameOver(target);
   }
   
   @Override
   protected void playerCanBypass(Player player)
   {
      statusPanel.appendText(player.getName() + " can bypass and roll again.");
   }
   
   @Override
   protected void promptForDebtSettlement(Player debtor, Pair<Player, Integer> debt)
   {
      actionsPanel.showDebtSettlementActions(debtor, debt);
   }

   @Override
   protected void promptForPostRollActions()
   {
      actionsPanel.showPostRollActions();
   }

   @Override
   protected void promptForPreLandActions()
   {
      actionsPanel.showPreLandActions();
   }

   @Override
   protected void promptForPreRollActions()
   {
      Node node = model.getCurrentPlayer().getCurrentNode();
      
      centerOnNode(node);
      actionsPanel.showPreRollActions();
      nodePanel.updateNode(node);
   }

   @Override
   protected void promptForNodeLostToLeague()
   {
      actionsPanel.showChooseNodeActions("Choose a property to relinquish to the Federation League:",
         model.getCurrentPlayer().getOwnedNodes(),
         Type.CHOOSE_NODE_LOST_TO_LEAGUE, model.getCurrentPlayer(), false, false);
   }

   @Override
   protected void promptForNodeWonFromLeague()
   {
      actionsPanel.showChooseNodeActions("Choose a property to obtain from the Federation League:",
         model.getUnownedNodes(),
         Type.CHOOSE_NODE_WON_FROM_LEAGUE, model.getCurrentPlayer(), false, false);
   }

   @Override
   protected void promptForNodeWonFromPlayer()
   {
      actionsPanel.showChooseNodeActions("Choose a property to obtain from another player:",
         model.getOwnedNodes(model.getCurrentPlayer()),
         Type.CHOOSE_NODE_WON_FROM_PLAYER, model.getCurrentPlayer(), false, false);
   }

   @Override
   protected void promptForTradeDecision(Trade trade)
   {
      // TODO
      boolean accept = JOptionPane.showConfirmDialog(frame, trade.toString(), "Accept this trade?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;

      statusPanel.appendText(accept ? "Trade accepted:" : "Trade rejected:");
      statusPanel.appendText(trade.toString());
      sendMessage(Type.TRADE_COMPLETED, trade.getTo(), accept);
   }

   @Override
   protected void tradeAccepted(Trade trade)
   {
      statusPanel.appendText(trade.getTo() + " accepted " + trade.getFrom() + "'s trade.");
   }

   @Override
   protected void tradeRejected(Trade trade)
   {
      statusPanel.appendText(trade.getTo() + " rejected " + trade.getFrom() + "'s trade.");
   }
   
   @Override
   public void receiveMessage(final ViewMessage message)
   {
      SwingUtilities.invokeLater(new Runnable()
      {
         @Override
         public void run()
         {
            processMessage(message);
         }
      });
   }
   
   @Override
   protected void processMessage(ViewMessage message)
   {
      super.processMessage(message);
   }

   JFrame getFrame()
   {
      return frame;
   }

   void setBoardImage(String boardImage)
   {
      this.boardImage = boardImage;
   }
   
   void setNodeViews(Map<String, NodeView> nodeViews)
   {
      this.nodeViews = nodeViews;
   }

   void setDeedGroupViews(Map<String, DeedGroupView> deedGroupViews)
   {
      this.deedGroupViews = deedGroupViews;      
   }

   void setCardViews(Map<String, CardView> cardViews)
   {
      this.cardViews = cardViews;      
   }
   
   String getNodeDisplayName(String id)
   {
      NodeView nodeView = nodeViews.get(id);
      
      return nodeView == null ? id : nodeView.getDisplayName();
   }
   
   Point getNodeCoords(String id)
   {
      NodeView nodeView = nodeViews.get(id);
      
      return nodeView == null ? new Point(0, 0) : nodeView.getCoords();
   }
   
   void centerOnNode(Node node)
   {
      if (node != null)
         boardPanel.center(getNodeCoords(node.getID()), true);
   }
   
   Color getDeedGroupDisplayColor(String id)
   {
      DeedGroupView deedGroupView = deedGroupViews.get(id);
      
      return deedGroupView == null ? null : deedGroupView.getDisplayColor();
   }
   
   String getCardDisplayName(String id)
   {
      CardView cardView = cardViews.get(id);
      
      return cardView == null ? id : cardView.getDisplayName();
   }

   void updateNodePanel(Node node)
   {
      nodePanel.updateNode(node);
   }
   
   void highlightNodes(Collection<Node> nodes)
   {
      boardPanel.setHighlightedNodes(nodes);
   }
   
   void updateChosenNode(Node node)
   {
      actionsPanel.setChosenNode(node);
   }
   
   @Override
   protected void playerObtainedFreeFuelStation(Player player)
   {
      statusPanel.appendText(player.getName() + " obtained a free fuel station.");
   }
   
   private void saveAndQuit()
   {
      JFileChooser fileChooser = new JFileChooser();
      
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      fileChooser.setMultiSelectionEnabled(false);
      fileChooser.showSaveDialog(frame);
      
      File file = fileChooser.getSelectedFile();
      
      if (file == null)
         return;
      
      if (file.exists())
         if (JOptionPane.showConfirmDialog(frame, "The file " + file.getName() + " exists. Overwrite?",
               "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;
      
      try
      {
         save(file);
         quit();
      }
      catch (IOException ioe)
      {
         JOptionPane.showMessageDialog(frame, "Failed to save: " + ioe.toString(), "Error", JOptionPane.ERROR_MESSAGE);
      }
   }
   
   private void quit()
   {
      System.exit(0); // TODO: be nicer?
   }
}
