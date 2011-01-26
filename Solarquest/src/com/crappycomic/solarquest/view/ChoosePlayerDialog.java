// Solarquest
// Copyright (C) 2011 Colin Bartolome
// Licensed under the GPL. See LICENSE.txt for details.

package com.crappycomic.solarquest.view;

import java.awt.FlowLayout;
import java.awt.event.*;
import java.util.List;

import javax.swing.*;

import com.crappycomic.solarquest.model.Player;

public class ChoosePlayerDialog extends JDialog
{
   private static final long serialVersionUID = 0;
   
   private Player player;
   
   ChoosePlayerDialog(GraphicView view, String title, List<Player> players)
   {
      super(view.getFrame(), title, true);
      
      setLayout(new FlowLayout());
      
      for (final Player buttonPlayer : players)
      {
         JButton button = new JButton(buttonPlayer.getName());
         
         button.addActionListener(new ActionListener()
         {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
               player = buttonPlayer;
               setVisible(false);
            }
         });
         
         add(button);
      }
      
      pack();
      setLocationRelativeTo(view.getFrame());      
      setVisible(true);
   }
   
   Player getValue()
   {
      return player;
   }
}
