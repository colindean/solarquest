// Solarquest
// Copyright (C) 2011 Colin Bartolome
// Licensed under the GPL. See LICENSE.txt for details.

package com.crappycomic.solarquest.view;

import javax.swing.*;

import com.crappycomic.solarquest.model.*;

// TODO: gray out on game over, improve layout
public class PlayerPanel extends JPanel
{
   private static final long serialVersionUID = 0;
   
   private GraphicView view;
   
   private JLabel locationLabel;
   
   private JLabel cashLabel;
   
   private JLabel fuelLabel;
   
   private JLabel fuelStationLabel;
   
   private JLabel totalWorthLabel;
   
   PlayerPanel(GraphicView view, Model model, Player player)
   {
      this.view = view;
      
      add(new PlayerToken(player.getNumber()));
      add(new JLabel(player.getName()));
      add(locationLabel = new JLabel(getLocationLabelText(player)));
      add(cashLabel = new JLabel(getCashLabelText(player.getCash())));
      add(fuelLabel = new JLabel(getFuelLabelText(player.getFuel())));
      add(fuelStationLabel = new JLabel(getFuelStationsLabelText(player.getFuelStations())));
      add(totalWorthLabel = new JLabel(getTotalWorthLabelText(model.getTotalWorth(player))));
   }
   
   private String getLocationLabelText(Player player)
   {
      if (player.isGameOver())
      {
         return "Game Over";
      }
      else
      {
         Node node = player.getCurrentNode();
         
         return "Location: " + (node == null ? "Unknown" : view.getNodeDisplayName(node.getID()));
      }
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

   void updateNode(Player player)
   {
      locationLabel.setText(getLocationLabelText(player));
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
