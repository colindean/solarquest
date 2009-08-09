package com.crappycomic.solarquest.view;

import java.awt.*;
import java.util.*;

import javax.swing.JPanel;

import com.crappycomic.solarquest.model.*;

public class PlayersPanel extends JPanel
{
   public static final int DEFAULT_WIDTH = 200;

   private Map<Player, PlayerPanel> panelMap;
   
   private Model model;
   
   PlayersPanel(GraphicView view, Model model)
   {
      this.model = model;
      
      java.util.List<Player> players = model.getPlayers();
      
      setPreferredSize(new Dimension(DEFAULT_WIDTH, 0));
      
      setLayout(new GridLayout(players.size(), 1));
      
      panelMap = new HashMap<Player, PlayerPanel>();
      
      for (Player player : players)
      {
         PlayerPanel panel = new PlayerPanel(view, model, player);
         panelMap.put(player, panel);
         add(panel);
      }
   }

   void updateNode(Player player)
   {
      panelMap.get(player).updateNode(player);
   }
   
   void updateCash(Player player, int cash)
   {
      panelMap.get(player).updateCash(cash);
   }
   
   void updateFuel(Player player, int fuel)
   {
      panelMap.get(player).updateFuel(fuel);
   }
   
   void updateFuelStations(Player player, int fuelStations)
   {
      panelMap.get(player).updateFuelStations(fuelStations);
   }
   
   void updateTotalWorth(Player player)
   {
      panelMap.get(player).updateTotalWorth(model.getTotalWorth(player));
   }
}
