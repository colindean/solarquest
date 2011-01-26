// Solarquest
// Copyright (C) 2011 Colin Bartolome
// Licensed under the GPL. See LICENSE.txt for details.

package com.crappycomic.solarquest.view;

import java.awt.*;

import javax.swing.*;

import com.crappycomic.solarquest.model.*;

public class NodePanel extends JPanel
{
   private static final long serialVersionUID = 0;
   
   private static final int DEFAULT_WIDTH = ActionsPanel.DEFAULT_WIDTH;
   
   private static final int BORDER_WIDTH = 5;
   
   private static final Color HIGHLIGHT_COLOR = new Color(32, 128, 32);
   
   private GraphicView view;
   
   private Model model;
   
   private JPanel panel;
   
   private JLabel nameLabel;
   
   private JLabel priceLabel;
   
   private JLabel ownerLabel;
   
   private JLabel fuelStationLabel;
   
   private JPanel deedPanel;
   
   NodePanel(GraphicView view, Model model)
   {
      this.view = view;
      this.model = model;
      
      setPreferredSize(new Dimension(DEFAULT_WIDTH, 0));
      setLayout(new GridLayout(1, 1));
      
      panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.add(nameLabel = ActionsPanel.createLabel(""));
      panel.add(priceLabel = ActionsPanel.createLabel(""));
      panel.add(ownerLabel = ActionsPanel.createLabel(""));
      panel.add(fuelStationLabel = ActionsPanel.createLabel("Fuel Station Present"));
      fuelStationLabel.setVisible(false);
      
      deedPanel = new JPanel();
      deedPanel.setLayout(new BoxLayout(deedPanel, BoxLayout.X_AXIS));
      panel.add(deedPanel);
      
      panel.setVisible(false);
      add(panel);
   }
   
   void updateNode(Node node)
   {
      if (node == null || !node.canOwn())
      {
         panel.setVisible(false);
      }
      else
      {
         Color displayColor = view.getDeedGroupDisplayColor(node.getGroup());
         java.util.List<Integer> rents = node.getRents();
         java.util.List<Integer> fuels = node.getFuels();
         boolean showFuels = fuels != null;
         int highlight = node.getOwner() == null ? -1 : node.getOwner().getGroupCount(node.getGroup());
         JPanel ownedPanel;
         JPanel rentPanel;
         JPanel fuelPanel;
         
         panel.setBorder(displayColor == null ? null : BorderFactory.createLineBorder(view.getDeedGroupDisplayColor(node.getGroup()), BORDER_WIDTH));
         
         nameLabel.setText(view.getNodeDisplayName(node.getID()));
         priceLabel.setText("Price: $" + model.getNodePrice(node));
         ownerLabel.setText(node.getOwner() == null ? "Unowned" : "Owned by " + node.getOwner().getName());
         fuelStationLabel.setVisible(node.hasFuelStation());
         
         deedPanel.removeAll();
         deedPanel.add(Box.createHorizontalGlue());
         deedPanel.add(ownedPanel = new JPanel());
         ownedPanel.setLayout(new BoxLayout(ownedPanel, BoxLayout.Y_AXIS));
         ownedPanel.add(new JLabel("Owned"));
         deedPanel.add(Box.createHorizontalGlue());
         deedPanel.add(rentPanel = new JPanel());
         rentPanel.setLayout(new BoxLayout(rentPanel, BoxLayout.Y_AXIS));
         rentPanel.add(new JLabel("Rent / Fee"));
         deedPanel.add(Box.createHorizontalGlue());
         deedPanel.add(fuelPanel = new JPanel());
         fuelPanel.setLayout(new BoxLayout(fuelPanel, BoxLayout.Y_AXIS));
         fuelPanel.add(new JLabel("Fuel"));
         deedPanel.add(Box.createHorizontalGlue());
         
         for (int ndx = 0; ndx < rents.size(); ndx++)
         {
            JLabel ownedLabel;
            JLabel rentLabel;
            JLabel fuelLabel = null;
            
            ownedPanel.add(ownedLabel = new JLabel(Integer.toString(ndx + 1)));
            rentPanel.add(rentLabel = new JLabel(Integer.toString(rents.get(ndx))));
            fuelPanel.add(fuelLabel = new JLabel(showFuels ? Integer.toString(fuels.get(ndx)) : "-"));
            
            if (ndx == highlight)
            {
               ownedLabel.setForeground(HIGHLIGHT_COLOR);
               rentLabel.setForeground(HIGHLIGHT_COLOR);
               fuelLabel.setForeground(HIGHLIGHT_COLOR);
            }
         }
         
         deedPanel.validate();
         
         panel.setVisible(true);
      }
   }
}
