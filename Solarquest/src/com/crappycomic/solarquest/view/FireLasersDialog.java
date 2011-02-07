package com.crappycomic.solarquest.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;

import com.crappycomic.solarquest.model.Player;

public class FireLasersDialog extends JDialog
{
   private static final long serialVersionUID = 0;
   
   private static final int GAP = 5;
   
   private List<JCheckBox> checkBoxes;
   
   private List<Player> targetablePlayers;
   
   private boolean fireClicked;
   
   FireLasersDialog(JFrame owner, List<Player> targetablePlayers, int fuelCost)
   {
      super(owner, "Choose players to target", true);
      
      String labelText = "Fuel cost: " + fuelCost + " hydron";
      
      if (fuelCost != 1)
         labelText += "s";
      
      add(new JLabel(labelText), BorderLayout.NORTH);
      
      Box vBox = Box.createVerticalBox();
      Box hBox;
      
      vBox.add(Box.createVerticalStrut(GAP));
      
      checkBoxes = new ArrayList<JCheckBox>();
      this.targetablePlayers = targetablePlayers;
      
      for (Player player : targetablePlayers)
      {
         JCheckBox checkBox = new JCheckBox();
         
         checkBoxes.add(checkBox);
         
         hBox = Box.createHorizontalBox();
         hBox.add(checkBox);
         hBox.add(Box.createHorizontalStrut(GAP));
         hBox.add(new JLabel(player.getName()));
         vBox.add(hBox);
         
         vBox.add(Box.createVerticalStrut(GAP));
      }
      
      add(vBox, BorderLayout.CENTER);
      
      JButton button = new JButton("Fire!");
      
      button.addActionListener(new ActionListener()
      {
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            fireClicked = true;
            setVisible(false);
         }
      });
      
      hBox = Box.createHorizontalBox();
      hBox.add(Box.createHorizontalGlue());
      hBox.add(button);
      hBox.add(Box.createHorizontalGlue());
      
      add(hBox, BorderLayout.SOUTH);
      
      pack();
      setLocationRelativeTo(owner);
      setVisible(true);
   }
   
   List<Integer> getValue()
   {
      List<Integer> targetedPlayers = new ArrayList<Integer>();
      
      if (fireClicked)
      {
         for (int index = 0; index < checkBoxes.size(); index++)
         {
            if (checkBoxes.get(index).isSelected())
            {
               targetedPlayers.add(targetablePlayers.get(index).getNumber());
            }
         }
      }
      
      return targetedPlayers;
   }
}
