package com.crappycomic.solarquest.main;

import static com.crappycomic.solarquest.main.CreateGameDialog.*;

import java.awt.Component;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import com.crappycomic.solarquest.view.PlayerToken;

/** Allows a player to be selected from the list sent by the server. */
@SuppressWarnings("serial")
public class AvailablePlayersDialog extends JDialog
{
   private int player;
   
   private String playerName;
   
   public AvailablePlayersDialog(JDialog owner, Set<Integer> availablePlayers)
   {
      super(owner, "Available Players", true);
      
      player = -1;
      
      JPanel panel = new JPanel();
      final JComboBox playerBox;
      final JTextField nameField;
      JButton button;
      
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      
      Box hBox;
      
      hBox = Box.createHorizontalBox();
      hBox.add(Box.createHorizontalGlue());
      hBox.add(new JLabel("Close this dialog if you are done adding players."));
      hBox.add(Box.createHorizontalGlue());

      hBox = Box.createHorizontalBox();
      hBox.add(new JLabel("Color:"));
      hBox.add(Box.createHorizontalStrut(GAP));
      hBox.add(playerBox = new JComboBox(availablePlayers.toArray()));
      hBox.add(Box.createHorizontalStrut(GAP));
      hBox.add(new JLabel("Name:"));
      hBox.add(Box.createHorizontalStrut(GAP));
      hBox.add(nameField = new JTextField(TEXT_FIELD_WIDE));
      panel.add(hBox);
      
      playerBox.setRenderer(new ListCellRenderer()
      {
         @Override
         public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus)
         {
            JPanel cell = new JPanel();
            
            if (value != null)
               cell.add(new PlayerToken((Integer)value));
            
            if (isSelected)
               cell.setBackground(list.getSelectionBackground());
            
            return cell;
         }
      });
      
      panel.add(Box.createVerticalStrut(GAP));
      
      hBox = Box.createHorizontalBox();
      hBox.add(Box.createHorizontalGlue());
      hBox.add(button = new JButton("Add Player"));
      hBox.add(Box.createHorizontalGlue());
      
      button.addActionListener(new ActionListener()
      {
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            if (nameField.getText().trim().isEmpty())
            {
               JOptionPane.showMessageDialog(AvailablePlayersDialog.this, "Please enter a name.", "Error", JOptionPane.ERROR_MESSAGE);
               return;
            }
            
            player = (Integer)playerBox.getSelectedItem();
            playerName = nameField.getText();
            setVisible(false);
         }
      });
      
      panel.add(hBox);
      
      panel.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
      add(panel);
      pack();
      setLocationRelativeTo(owner);
      setVisible(true);
   }
   
   int getPlayer()
   {
      return player;
   }

   String getPlayerName()
   {
      return playerName;
   }
}
