// Solarquest
// Copyright (C) 2011 Colin Bartolome
// Licensed under the GPL. See LICENSE.txt for details.

package com.crappycomic.solarquest.view;

import java.awt.Color;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.Border;

import com.crappycomic.solarquest.model.*;

// TODO: gray out on game over, improve layout
public class PlayerPanel extends JPanel
{
   private static final long serialVersionUID = 0;
   
   private static final int BORDER_WIDTH = 3;
   
   private GraphicView view;
   
   private Player player;
   
   private JLabel cashLabel;
   
   private JLabel fuelLabel;
   
   private JLabel fuelStationLabel;
   
   private JLabel totalWorthLabel;
   
   private MouseListener mouseListener;
   
   PlayerPanel(GraphicView view, Model model, Player player)
   {
      this.view = view;
      this.player = player;
      
      Color borderColor = PlayerToken.PLAYER_COLORS[player.getNumber()].darker();
      
      Border outsideBorder = BorderFactory.createLineBorder(borderColor, BORDER_WIDTH);
      Border insideBorder = BorderFactory.createEmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH);
      
      outsideBorder = BorderFactory.createTitledBorder(outsideBorder, player.getName());
      
      setBorder(BorderFactory.createCompoundBorder(outsideBorder, insideBorder));
      
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      
      add(cashLabel = new JLabel(getCashLabelText(player.getCash())));
      add(fuelLabel = new JLabel(getFuelLabelText(player.getFuel())));
      add(fuelStationLabel = new JLabel(getFuelStationsLabelText(player.getFuelStations())));
      add(totalWorthLabel = new JLabel(getTotalWorthLabelText(model.getTotalWorth(player))));
      
      addMouseListener(mouseListener = new MouseAdapter()
      {
         @Override
         public void mouseClicked(MouseEvent evt)
         {
            PlayerPanel.this.view.centerOnNode(PlayerPanel.this.player.getCurrentNode());
         }
      });
   }
   
   private static String getCashLabelText(int cash)
   {
      return "Cash: $" + cash;
   }
   
   private static String getFuelLabelText(int fuel)
   {
      return "Fuel: " + fuel + " hydron" + (fuel == 1 ? "" : "s");
   }
   
   private static String getFuelStationsLabelText(int fuelStations)
   {
      return "Fuel Stations: " + fuelStations;
   }
   
   private static String getTotalWorthLabelText(int totalWorth)
   {
      return "Total Worth: $" + totalWorth;
   }
   
   private String getToolTipText(Node node)
   {
      if (node == null)
         return "Out of the Solar System";
      
      switch (node.getType())
      {
         case SPACE:
         case WELL_ORBIT:
         case WELL_PULL:
            return "In " + view.getNodeDisplayName(node.getID());
         case SOLID:
            return "On " + view.getNodeDisplayName(node.getID());
         case DOCK:
         case LAB:
         case STATION:
            return "At " + view.getNodeDisplayName(node.getID());
      }
      
      return "Somewhere Out There";
   }
   
   void updateNode(Node node)
   {
      setToolTipText(getToolTipText(node));
   }

   void gameOver()
   {
      removeAll();
      removeMouseListener(mouseListener);
      setToolTipText(null);
      
      add(new JLabel("Game Over"));
      
      repaint();
   }

   void updateCash(int cash)
   {
      cashLabel.setText(getCashLabelText(cash));
   }
   
   void updateFuel(int fuel)
   {
      fuelLabel.setText(getFuelLabelText(fuel));
   }
   
   void updateFuelStations(int fuelStations)
   {
      fuelStationLabel.setText(getFuelStationsLabelText(fuelStations));
   }
   
   void updateTotalWorth(int totalWorth)
   {
      totalWorthLabel.setText(getTotalWorthLabelText(totalWorth));
   }
}
