// Solarquest
// Copyright (C) 2011 Colin Bartolome
// Licensed under the GPL. See LICENSE.txt for details.

package com.crappycomic.solarquest.view;

import java.awt.*;
import java.awt.event.*;
import java.util.Collection;

import javax.swing.*;
import javax.swing.Action;
import javax.swing.event.*;

import com.crappycomic.solarquest.model.*;
import com.crappycomic.solarquest.model.ModelMessage.Type;

public class ActionsPanel extends JPanel
{
   private static final long serialVersionUID = 0;
   
   private class FuelSpinnerChangeListener implements ChangeListener
   {
      private int fuelPrice;
      
      private void setFuelPrice(int fuelPrice)
      {
         this.fuelPrice = fuelPrice;
      }
      
      @Override
      public void stateChanged(ChangeEvent evt)
      {
         totalFuelPriceLabel.setText(getTotalFuelPriceLabelText(fuelPrice * (Integer)fuelSpinnerModel.getValue()));
      }
   }
   
   public static final int DEFAULT_WIDTH = 200;
   
   private static final int FUEL_SPINNER_HEIGHT = 24;
   
   private static final int GAP = 5;
   
   private static final Color CRITICAL_FUEL_WARNING = Color.YELLOW;
   
   private static final Color CRITICAL_FUEL_EMERGENCY = Color.RED;
   
   private GraphicView view;
   
   private Model model;
   
   private JPanel waitingForPlayerPanel;
   
   private JPanel preRollPanel;
   
   private JPanel preLandPanel;
   
   private JPanel postRollPanel;
   
   private JPanel debtSettlementPanel;
   
   private JPanel chooseAllowedMovePanel;
   
   private JPanel allowedMovesPanel;
   
   private JPanel purchaseFuelPanel;
   
   private JPanel chooseNodePanel;
   
   private JPanel previousPanel;
   
   private JPanel currentPanel;
   
   private JLabel waitingForPlayerLabel;
   
   private JLabel waitingForActionLabel;
   
   private Action tradeAction;
   
   private JLabel preRollLabel;
   
   private Action purchaseFuelAction;

   private Action placeFuelStationAction;

   private Action purchaseFuelStationAction;
   
   private JButton rollDiceButton;
   
   private JLabel preLandLabel;
   
   private Action negligenceTakeoverAction;
   
   private JLabel postRollLabel;
   
   private Action purchaseNodeAction;
   
   private JButton endTurnButton;
   
   private JLabel debtSettlementPlayersLabel;
   
   private JLabel debtSettlementAmountLabel;
   
   private Action sellFuelStationNormallyAction;
   
   private Action sellFuelStationForDebtSettlementAction;
   
   private Action sellNodeNormallyAction;
   
   private Action sellNodeForDebtSettlementAction;
   
   private JLabel chooseAllowedMoveLabel;
   
   private JLabel purchaseFuelLabel;
   
   private Action refillToLowFuelAction;
   
   private Action finalizeFuelPurchaseAction;
   
   private SpinnerNumberModel fuelSpinnerModel;
   
   private FuelSpinnerChangeListener fuelSpinnerChangeListener;
   
   private JLabel totalFuelPriceLabel;
   
   private Collection<Node> choosableNodes;
   
   private Type chooseNodeMessageType;
   
   private Node chosenNode;

   private JLabel chooseNodePlayerLabel;
   
   private JLabel chooseNodeLabel;
   
   private Action chooseNodeAction;
   
   private JButton chooseNodeCancelButton;
   
   ActionsPanel(final GraphicView view, final Model model)
   {
      this.view = view;
      this.model = model;
      
      setPreferredSize(new Dimension(DEFAULT_WIDTH, 0));
      setLayout(new GridLayout(1, 1));
      setBorder(BorderFactory.createRaisedBevelBorder());
      
      tradeAction = new AbstractAction("Trade")
      {
         private static final long serialVersionUID = 0;
         
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            Player from = new ChoosePlayerDialog(view, "Who is starting the trade?", model.getTradeablePlayers(null)).getValue();
            
            if (from != null)
            {
               Player to = new ChoosePlayerDialog(view, "With whom will you trade?", model.getTradeablePlayers(from)).getValue();
               
               if (to != null)
               {
                  new TradeDialog(view, from, to).getValue();
               }
            }
         }
      };
      
      purchaseFuelAction = new AbstractAction()
      {
         private static final long serialVersionUID = 0;
         
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            showPurchaseFuelActions();
         }
      };
      
      placeFuelStationAction = new AbstractAction("Place Fuel Station")
      {
         private static final long serialVersionUID = 0;
         
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            view.sendMessage(Type.PLACE_FUEL_STATION);
         }
      };
      
      purchaseFuelStationAction = new AbstractAction("Purchase Fuel Station")
      {
         private static final long serialVersionUID = 0;
         
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            view.sendMessage(Type.PURCHASE_FUEL_STATION);            
         }
      };
      
      sellFuelStationNormallyAction = new AbstractAction("Sell Fuel Station")
      {
         private static final long serialVersionUID = 0;
         
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            view.sendMessage(Type.SELL_FUEL_STATION_NORMALLY);
         }
      };
      
      sellNodeNormallyAction = new AbstractAction("Sell Property")
      {
         private static final long serialVersionUID = 0;
         
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            showChooseNodeActions("Choose a property to sell to the Federation League:",
               model.getCurrentPlayer().getOwnedNodes(), Type.SELL_NODE_NORMALLY, true);
         }
      };
      
      Action rollDiceAction = new AbstractAction("Roll Dice")
      {
         private static final long serialVersionUID = 0;
         
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            switchPanel(null);
            view.sendMessage(Type.NO_PRE_ROLL);
         }
      };
      
      waitingForPlayerPanel = createPanel();
      waitingForPlayerPanel.add(waitingForPlayerLabel = createLabel(getWaitingForPlayerLabelText(null)));
      waitingForPlayerPanel.add(Box.createVerticalStrut(GAP));
      waitingForPlayerPanel.add(waitingForActionLabel = createLabel(""));      
      waitingForPlayerPanel.add(Box.createVerticalGlue());
      
      preRollPanel = createPanel();
      preRollPanel.add(preRollLabel = createLabel(getPreRollLabelText(null)));
      preRollPanel.add(Box.createVerticalStrut(GAP));
      preRollPanel.add(createButton(tradeAction));
      preRollPanel.add(Box.createVerticalStrut(GAP));
      preRollPanel.add(createButton(purchaseFuelAction));
      preRollPanel.add(Box.createVerticalStrut(GAP));
      preRollPanel.add(createButton(placeFuelStationAction));
      preRollPanel.add(Box.createVerticalStrut(GAP));
      preRollPanel.add(createButton(purchaseFuelStationAction));
      preRollPanel.add(Box.createVerticalStrut(GAP));
      preRollPanel.add(createButton(sellFuelStationNormallyAction));
      preRollPanel.add(Box.createVerticalStrut(GAP));
      preRollPanel.add(createButton(sellNodeNormallyAction));
      preRollPanel.add(Box.createVerticalGlue());
      preRollPanel.add(rollDiceButton = createButton(rollDiceAction));

      negligenceTakeoverAction = new AbstractAction("Perform Takeover")
      {
         private static final long serialVersionUID = 0;
         
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            view.sendMessage(Type.NEGLIGENCE_TAKEOVER);
         }
      };
      
      Action preLandContinueButton = new AbstractAction("Continue")
      {
         private static final long serialVersionUID = 0;
         
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            switchPanel(null);
            view.sendMessage(Type.NO_PRE_LAND);
         }
      };
      
      preLandPanel = createPanel();
      preLandPanel.add(preLandLabel = createLabel(getPreLandLabelText(null)));
      preLandPanel.add(Box.createVerticalStrut(GAP));
      preLandPanel.add(createButton(tradeAction));
      preLandPanel.add(Box.createVerticalStrut(GAP));
      preLandPanel.add(createButton(negligenceTakeoverAction));
      preLandPanel.add(Box.createVerticalGlue());
      preLandPanel.add(createButton(preLandContinueButton));
      
      purchaseNodeAction = new AbstractAction("Purchase Property")
      {
         private static final long serialVersionUID = 0;
         
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            view.sendMessage(Type.PURCHASE_NODE);            
         }
      };
      
      Action endTurnAction = new AbstractAction("End Turn")
      {
         private static final long serialVersionUID = 0;
         
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            switchPanel(null);
            view.sendMessage(Type.NO_POST_ROLL);
         }
      };
      
      postRollPanel = createPanel();
      postRollPanel.add(postRollLabel = createLabel(getPostRollLabelText(null)));
      postRollPanel.add(Box.createVerticalStrut(GAP));
      postRollPanel.add(createButton(tradeAction));
      postRollPanel.add(Box.createVerticalStrut(GAP));
      postRollPanel.add(createButton(purchaseNodeAction));
      postRollPanel.add(Box.createVerticalStrut(GAP));
      postRollPanel.add(createButton(purchaseFuelAction));
      postRollPanel.add(Box.createVerticalStrut(GAP));
      postRollPanel.add(createButton(placeFuelStationAction));
      postRollPanel.add(Box.createVerticalStrut(GAP));
      postRollPanel.add(createButton(purchaseFuelStationAction));
      postRollPanel.add(Box.createVerticalStrut(GAP));
      postRollPanel.add(createButton(sellFuelStationNormallyAction));
      postRollPanel.add(Box.createVerticalStrut(GAP));
      postRollPanel.add(createButton(sellNodeNormallyAction));
      postRollPanel.add(Box.createVerticalGlue());
      postRollPanel.add(endTurnButton = createButton(endTurnAction));
      
      Action declareBankruptcyAction = new AbstractAction("Declare Bankruptcy")
      {
         private static final long serialVersionUID = 0;
         
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            switchPanel(null);
            view.sendMessage(Type.DECLARE_BANKRUPTCY);
         }
      };
      
      sellFuelStationForDebtSettlementAction = new AbstractAction("Sell Fuel Station")
      {
         private static final long serialVersionUID = 0;
         
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            view.sendMessage(Type.SELL_FUEL_STATION_FOR_DEBT_SETTLEMENT);
         }
      };
      
      sellNodeForDebtSettlementAction = new AbstractAction("Sell Property")
      {
         private static final long serialVersionUID = 0;
         
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            showChooseNodeActions("Choose a property to sell to the Federation League:",
               model.getCurrentPlayer().getOwnedNodes(), Type.SELL_NODE_FOR_DEBT_SETTLEMENT, true);
         }
      };
      
      debtSettlementPanel = createPanel();
      debtSettlementPanel.add(debtSettlementPlayersLabel = createLabel(getDebtSettlementPlayersLabelText(null, null)));
      debtSettlementPanel.add(Box.createVerticalStrut(GAP));
      debtSettlementPanel.add(debtSettlementAmountLabel = createLabel(getDebtSettlementAmountLabelText(0)));
      debtSettlementPanel.add(Box.createVerticalStrut(GAP));
      debtSettlementPanel.add(createButton(tradeAction));
      debtSettlementPanel.add(Box.createVerticalStrut(GAP));
      debtSettlementPanel.add(createButton(sellFuelStationForDebtSettlementAction));
      debtSettlementPanel.add(Box.createVerticalStrut(GAP));
      debtSettlementPanel.add(createButton(sellNodeForDebtSettlementAction));
      debtSettlementPanel.add(Box.createVerticalGlue());
      debtSettlementPanel.add(createButton(declareBankruptcyAction));
      
      chooseAllowedMovePanel = createPanel();
      chooseAllowedMovePanel.add(chooseAllowedMoveLabel = createLabel(getPlayerLabelText(null)));
      chooseAllowedMovePanel.add(Box.createVerticalStrut(GAP));
      chooseAllowedMovePanel.add(createLabel("Choose one of the following:"));
      chooseAllowedMovePanel.add(Box.createVerticalStrut(GAP));
      chooseAllowedMovePanel.add(allowedMovesPanel = new JPanel());
      allowedMovesPanel.setLayout(new BoxLayout(allowedMovesPanel, BoxLayout.Y_AXIS));
      
      refillToLowFuelAction = new AbstractAction("Refill to " + model.getLowFuel())
      {
         private static final long serialVersionUID = 0;
         
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            int hydrons = model.getLowFuel() - model.getCurrentPlayer().getFuel();
            
            fuelSpinnerModel.setValue(Math.min(hydrons, (Integer)fuelSpinnerModel.getMaximum()));
         }
      };
      
      finalizeFuelPurchaseAction = new AbstractAction()
      {
         private static final long serialVersionUID = 0;
         
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            view.sendMessage(Type.PURCHASE_FUEL, (Integer)fuelSpinnerModel.getValue());
         }
      };
      
      Action cancelAction = new AbstractAction("Cancel")
      {
         private static final long serialVersionUID = 0;
         
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            switchPanel(previousPanel);
         }
      };

      JSpinner fuelSpinner;
      
      purchaseFuelPanel = createPanel();
      purchaseFuelPanel.add(purchaseFuelLabel = createLabel(getPlayerLabelText(null)));
      purchaseFuelPanel.add(Box.createVerticalStrut(GAP));
      purchaseFuelPanel.add(createLabel("Choose the number of hydrons:"));
      purchaseFuelPanel.add(Box.createVerticalStrut(GAP));
      purchaseFuelPanel.add(createButton(refillToLowFuelAction));
      purchaseFuelPanel.add(Box.createVerticalStrut(GAP));
      purchaseFuelPanel.add(fuelSpinner = new JSpinner(fuelSpinnerModel = new SpinnerNumberModel(1, 1, 1, 1)));
      fuelSpinner.setMaximumSize(new Dimension(DEFAULT_WIDTH / 2, FUEL_SPINNER_HEIGHT));
      fuelSpinnerModel.addChangeListener(fuelSpinnerChangeListener = new FuelSpinnerChangeListener());
      purchaseFuelPanel.add(Box.createVerticalStrut(GAP));
      purchaseFuelPanel.add(totalFuelPriceLabel = createLabel(getTotalFuelPriceLabelText(0)));
      purchaseFuelPanel.add(Box.createVerticalStrut(GAP));
      purchaseFuelPanel.add(createButton(finalizeFuelPurchaseAction));
      purchaseFuelPanel.add(Box.createVerticalGlue());
      purchaseFuelPanel.add(createButton(cancelAction));
      
      chooseNodeAction = new AbstractAction()
      {
         private static final long serialVersionUID = 0;
         
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            if (chosenNode == null)
               return;
            
            view.highlightNodes(null);
            view.sendMessage(chooseNodeMessageType, chosenNode.getID());
         }
      };
      
      chooseNodePanel = createPanel();
      chooseNodePanel.add(chooseNodePlayerLabel = createLabel(getPlayerLabelText(null)));
      chooseNodePanel.add(Box.createVerticalStrut(GAP));
      chooseNodePanel.add(chooseNodeLabel = createLabel(""));
      chooseNodePanel.add(Box.createVerticalStrut(GAP));
      chooseNodePanel.add(createButton(chooseNodeAction));
      chooseNodePanel.add(Box.createVerticalGlue());
      chooseNodePanel.add(chooseNodeCancelButton = createButton(cancelAction));
   }
   
   public static JLabel createLabel(String text)
   {
      JLabel label = new JLabel(text);
      
      label.setAlignmentX(0.5f);
      
      return label;
   }
   
   private JPanel createPanel()
   {
      JPanel panel = new JPanel();
      
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
      
      return panel;
   }
   
   private JButton createButton(Action action)
   {
      JButton button = new JButton(action);
      
      button.setAlignmentX(0.5f);
      
      return button;
   }

   private void switchPanel(JPanel panel)
   {
      previousPanel = currentPanel;
      currentPanel = panel;
      
      removeAll();
      
      if (panel != null)
         add(panel);
      
      validate();
      repaint();
   }
   
   private String getWaitingForPlayerLabelText(Player player)
   {
      return "-- Waiting for " + (player == null ? "Nobody" : player.getName()) + " --";
   }
   
   private String getPreRollLabelText(Player player)
   {
      return "-- " + (player == null ? "Nobody" : player.getName()) + "'s Turn (pre-roll) --";
   }
   
   private String getPreLandLabelText(Player player)
   {
      return "-- " + (player == null ? "Nobody" : player.getName()) + "'s Turn (pre-land) --";
   }
   
   private String getPostRollLabelText(Player player)
   {
      return "-- " + (player == null ? "Nobody" : player.getName()) + "'s Turn (post-roll) --";
   }
   
   private String getDebtSettlementPlayersLabelText(Player debtor, Player creditor)
   {
      return "-- " + (debtor == null ? "Nobody" : debtor.getName())
         + " owes " + (creditor == null ? "Nobody" : creditor.getName())
         + " --";
   }
   
   private String getPlayerLabelText(Player player)
   {
      return "-- " + (player == null ? "Nobody" : player.getName()) + "'s Turn --";
   }

   private String getDebtSettlementAmountLabelText(int amount)
   {
      return "$" + amount;
   }
   
   private String getChooseNodeLabelText(String labelText)
   {
      return "<html><center>" + labelText + "</center></html>";
   }
   
   private String getTotalFuelPriceLabelText(int price)
   {
      return "Total: $" + price;
   }
   
   void showPreRollActions()
   {
      Player player = model.getCurrentPlayer();
      
      if (view.isPlayerLocal(player))
      {
         boolean fuelIsPurchaseable = model.isFuelPurchaseable();
         boolean fuelStationIsPlaceable = model.isFuelStationPlaceable();
         boolean fuelStationIsPurchaseable = model.isFuelStationPurchaseable();
         boolean fuelStationIsSalable = model.isFuelStationSalableNormally();
         boolean nodeIsSalable = model.isNodeSalableNormally();
         boolean fuelIsCritical = model.isFuelCritical();
         
         preRollLabel.setText(getPreRollLabelText(player));
         
         purchaseFuelAction.setEnabled(fuelIsPurchaseable);
         purchaseFuelAction.putValue(Action.NAME, fuelIsPurchaseable && model.getFuelPrice() == 0 ? "Refuel" : "Purchase Fuel");
         purchaseFuelAction.putValue(Action.SHORT_DESCRIPTION, getPurchaseFuelActionTooltip(fuelIsPurchaseable, model.getFuelPrice()));
         placeFuelStationAction.setEnabled(fuelStationIsPlaceable);
         purchaseFuelStationAction.setEnabled(fuelStationIsPurchaseable);
         purchaseFuelStationAction.putValue(Action.SHORT_DESCRIPTION, getPurchaseFuelStationActionTooltip(fuelStationIsPurchaseable, model.getFuelStationPrice()));
         sellFuelStationNormallyAction.setEnabled(fuelStationIsSalable);
         sellFuelStationNormallyAction.putValue(Action.SHORT_DESCRIPTION, getSellFuelStationActionTooltip(fuelStationIsSalable, model.getFuelStationPrice()));
         sellNodeNormallyAction.setEnabled(nodeIsSalable);
         rollDiceButton.setBackground(fuelIsCritical ? CRITICAL_FUEL_EMERGENCY : null);
         
         switchPanel(preRollPanel);
      }
      else
      {
         waitingForPlayerLabel.setText(getWaitingForPlayerLabelText(player));
         waitingForActionLabel.setText("(pre-roll)");
         
         switchPanel(waitingForPlayerPanel);
      }
   }

   void showPreLandActions()
   {
      Player player = model.getCurrentPlayer();
      
      if (view.isPlayerLocal(player))
      {
         boolean negligenceTakeoverIsAllowed = model.isNegligenceTakeoverAllowed();
         
         preLandLabel.setText(getPreLandLabelText(player));
         
         negligenceTakeoverAction.setEnabled(negligenceTakeoverIsAllowed);
         negligenceTakeoverAction.putValue(Action.SHORT_DESCRIPTION, getNegligenceTakeoverActionTooltip(negligenceTakeoverIsAllowed, model.getNodePrice()));
         
         switchPanel(preLandPanel);
      }
      else
      {
         waitingForPlayerLabel.setText(getWaitingForPlayerLabelText(player));
         waitingForActionLabel.setText("(pre-land)");
         
         switchPanel(waitingForPlayerPanel);
      }
   }

   void showPostRollActions()
   {
      Player player = model.getCurrentPlayer();
      
      if (view.isPlayerLocal(player))
      {
         boolean nodeIsPurchaseable = model.isNodePurchaseable();
         boolean fuelIsPurchaseable = model.isFuelPurchaseable();
         boolean fuelStationIsPlaceable = model.isFuelStationPlaceable();
         boolean fuelStationIsPurchaseable = model.isFuelStationPurchaseable();
         boolean fuelStationIsSalable = model.isFuelStationSalableNormally();
         boolean nodeIsSalable = model.isNodeSalableNormally();
         boolean fuelIsCritical = model.isFuelCritical();
         int fuelPrice = model.getFuelPrice();
         
         postRollLabel.setText(getPostRollLabelText(player));
   
         purchaseNodeAction.setEnabled(nodeIsPurchaseable);
         purchaseNodeAction.putValue(Action.SHORT_DESCRIPTION, getPurchaseNodeActionTooltip(nodeIsPurchaseable, model.getNodePrice()));
         purchaseFuelAction.setEnabled(fuelIsPurchaseable);
         purchaseFuelAction.putValue(Action.NAME, fuelIsPurchaseable && fuelPrice == 0 ? "Refuel" : "Purchase Fuel");
         purchaseFuelAction.putValue(Action.SHORT_DESCRIPTION, getPurchaseFuelActionTooltip(fuelIsPurchaseable, fuelPrice));
         placeFuelStationAction.setEnabled(fuelStationIsPlaceable);
         purchaseFuelStationAction.setEnabled(fuelStationIsPurchaseable);
         purchaseFuelStationAction.putValue(Action.SHORT_DESCRIPTION, getPurchaseFuelStationActionTooltip(fuelStationIsPurchaseable, model.getFuelStationPrice()));
         sellFuelStationNormallyAction.setEnabled(fuelStationIsSalable);
         sellFuelStationNormallyAction.putValue(Action.SHORT_DESCRIPTION, getSellFuelStationActionTooltip(fuelStationIsSalable, model.getFuelStationPrice()));
         sellNodeNormallyAction.setEnabled(nodeIsSalable);
         endTurnButton.setBackground(fuelIsCritical ? CRITICAL_FUEL_WARNING : null);
         
         switchPanel(postRollPanel);
      }
      else
      {
         waitingForPlayerLabel.setText(getWaitingForPlayerLabelText(player));
         waitingForActionLabel.setText("(post-roll)");
         
         switchPanel(waitingForPlayerPanel);
      }
   }
   
   void showDebtSettlementActions(Pair<Player, Integer> debt)
   {
      Player player = model.getCurrentPlayer();

      if (view.isPlayerLocal(player))
      {
         boolean fuelStationIsSalable = model.isFuelStationSalableForDebtSettlement();
         boolean nodeIsSalable = model.isNodeSalableForDebtSettlement();
         
         debtSettlementPlayersLabel.setText(getDebtSettlementPlayersLabelText(player, debt.getFirst()));
         debtSettlementAmountLabel.setText(getDebtSettlementAmountLabelText(debt.getSecond()));
         sellFuelStationForDebtSettlementAction.setEnabled(fuelStationIsSalable);
         sellFuelStationForDebtSettlementAction.putValue(Action.SHORT_DESCRIPTION, getSellFuelStationActionTooltip(fuelStationIsSalable, model.getFuelStationPrice()));
         sellNodeForDebtSettlementAction.setEnabled(nodeIsSalable);
   
         switchPanel(debtSettlementPanel);
      }
      else
      {
         waitingForPlayerLabel.setText(getWaitingForPlayerLabelText(player));
         waitingForActionLabel.setText("(settling $" + debt.getSecond() + " debt to " + debt.getFirst() + ")");
         
         switchPanel(waitingForPlayerPanel);
      }
   }
   
   void showChooseAllowedMoveActions(Player player, Iterable<Node> allowedMoves)
   {
      if (view.isPlayerLocal(player))
      {
         chooseAllowedMoveLabel.setText(getPlayerLabelText(player));
         
         allowedMovesPanel.removeAll();
         
         for (final Node node : allowedMoves)
         {
            Action action = new AbstractAction(view.getNodeDisplayName(node.getID()))
            {
               private static final long serialVersionUID = 0;
               
               @Override
               public void actionPerformed(ActionEvent evt)
               {
                  view.sendMessage(Type.CHOOSE_ALLOWED_MOVE, node.getID());
               }
            };
            
            allowedMovesPanel.add(createButton(action));
            allowedMovesPanel.add(Box.createVerticalStrut(GAP));
         }
         
         allowedMovesPanel.validate();
         
         switchPanel(chooseAllowedMovePanel);
      }
      else
      {
         waitingForPlayerLabel.setText(getWaitingForPlayerLabelText(player));
         waitingForActionLabel.setText("(choosing destination)");
         
         switchPanel(waitingForPlayerPanel);
      }
   }
   
   void showPurchaseFuelActions()
   {
      Player player = model.getCurrentPlayer();
      Node node = player.getCurrentNode();
      int maximumHydrons = model.getMaximumPurchaseableFuel(player, node);
      int fuelPrice = model.getFuelPrice(player, node);
      
      purchaseFuelLabel.setText(getPlayerLabelText(player));
      refillToLowFuelAction.setEnabled(player.getFuel() < model.getLowFuel());
      finalizeFuelPurchaseAction.putValue(Action.NAME, fuelPrice == 0 ? "Refuel" : "Purchase");

      fuelSpinnerChangeListener.setFuelPrice(fuelPrice);
      fuelSpinnerModel.setValue(maximumHydrons);
      fuelSpinnerModel.setMaximum(maximumHydrons);
      
      switchPanel(purchaseFuelPanel);
   }
   
   void showChooseNodeActions(String labelText, Collection<Node> nodes, Type type, boolean canCancel)
   {
      Player player = model.getCurrentPlayer();
      
      if (view.isPlayerLocal(player))
      {
         choosableNodes = nodes;
         chooseNodeMessageType = type;
         chosenNode = null;
         chooseNodePlayerLabel.setText(getPlayerLabelText(player));
         chooseNodeLabel.setText(getChooseNodeLabelText(labelText));
         chooseNodeAction.putValue(Action.NAME, "Choose a Property");
         chooseNodeCancelButton.setVisible(canCancel);
         
         view.highlightNodes(nodes);
         
         switchPanel(chooseNodePanel);
      }
      else
      {
         waitingForPlayerLabel.setText(getWaitingForPlayerLabelText(player));
         waitingForActionLabel.setText("(choosing property)");
         
         switchPanel(waitingForPlayerPanel);
      }
   }

   private String getPurchaseFuelActionTooltip(boolean fuelIsPurchaseable, int fuelPrice)
   {
      return fuelIsPurchaseable ? "$" + fuelPrice : "";
   }
   
   private String getPurchaseFuelStationActionTooltip(boolean fuelStationIsPurchaseable, int fuelStationPrice)
   {
      return fuelStationIsPurchaseable ? "$" + fuelStationPrice : "";
   }
   
   private String getNegligenceTakeoverActionTooltip(boolean neglicenceTakeoverIsAllowed, int nodePrice)
   {
      return neglicenceTakeoverIsAllowed ? "$" + nodePrice : "";
   }
   
   private String getPurchaseNodeActionTooltip(boolean nodeIsPurchaseable, int nodePrice)
   {
      return nodeIsPurchaseable ? "$" + nodePrice : "";
   }
   
   private String getSellFuelStationActionTooltip(boolean fuelStationIsSalable, int fuelStationPrice)
   {
      return fuelStationIsSalable ? "$" + fuelStationPrice : "";
   }

   void setChosenNode(Node node)
   {
      if (choosableNodes == null || !choosableNodes.contains(node))
         return;
      
      chosenNode = node;
      chooseNodeAction.putValue(Action.NAME, "Choose " + view.getNodeDisplayName(node.getID()));
      
      repaint();
   }
}
