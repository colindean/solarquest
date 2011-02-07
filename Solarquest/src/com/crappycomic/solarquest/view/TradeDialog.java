// Solarquest
// Copyright (C) 2011 Colin Bartolome
// Licensed under the GPL. See LICENSE.txt for details.

package com.crappycomic.solarquest.view;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import com.crappycomic.solarquest.model.*;
import com.crappycomic.solarquest.model.ModelMessage.Type;

public class TradeDialog extends JDialog
{
   private static final long serialVersionUID = 0;
   
   private static class NodeListCellRenderer extends DefaultListCellRenderer
   {
      private static final long serialVersionUID = 0;
      
      private GraphicView view;
      
      private NodeListCellRenderer(GraphicView view)
      {
         this.view = view;
      }
      
      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index,
         boolean isSelected, boolean cellHasFocus)
      {
         Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
         
         ((JLabel)component).setText(view.getNodeDisplayName(((Node)value).getID()));
         
         return component;
      }
   }
   
   private static final int GAP = 5;
   
   private Player from;
   
   private Player to;
   
   private JSpinner cashSpinner;
   
   private JList fromList;
   
   private JList toList;
   
   TradeDialog(final GraphicView view, Player from, Player to)
   {
      super(view.getFrame(), "Choose items to trade:", false);
      
      this.from = from;
      this.to = to;
      
      JPanel panel = new JPanel();
      Box box = Box.createHorizontalBox();
      JButton button;
      
      panel.setLayout(new BorderLayout(GAP, GAP));
      
      box.add(Box.createHorizontalGlue());
      box.add(new JLabel("Cash (positive is offered, negative is requested):"));
      box.add(Box.createHorizontalStrut(GAP));
      box.add(cashSpinner = new JSpinner(new SpinnerNumberModel(0, -to.getCash(), from.getCash(), 1)));
      box.add(Box.createHorizontalGlue());
      panel.add(box, BorderLayout.NORTH);
      
      if (!from.getOwnedNodes().isEmpty() || !to.getOwnedNodes().isEmpty())
      {
         Box vBox = Box.createVerticalBox();
         
         box = Box.createHorizontalBox();
         box.add(Box.createHorizontalGlue());
         box.add(new JLabel(from.getName()));
         box.add(Box.createHorizontalGlue());
         box.add(new JLabel(to.getName()));
         box.add(Box.createHorizontalGlue());
         vBox.add(box);
         
         vBox.add(Box.createVerticalStrut(GAP));      
         
         box = Box.createHorizontalBox();
         box.add(Box.createHorizontalGlue());
         box.add(button = new JButton("Clear"));
         
         button.addActionListener(new ActionListener()
         {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
               fromList.clearSelection();
            }
         });
         
         box.add(Box.createHorizontalStrut(GAP));
         box.add(fromList = new JList(from.getOwnedNodes().toArray()));
         box.add(Box.createHorizontalGlue());
         box.add(toList = new JList(to.getOwnedNodes().toArray()));
         box.add(Box.createHorizontalStrut(GAP));
         box.add(button = new JButton("Clear"));
         
         fromList.setCellRenderer(new NodeListCellRenderer(view));
         toList.setCellRenderer(new NodeListCellRenderer(view));
         
         button.addActionListener(new ActionListener()
         {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
               toList.clearSelection();
            }
         });

         box.add(Box.createHorizontalGlue());
         vBox.add(box);
         
         panel.add(vBox, BorderLayout.CENTER);
      }
      
      box = Box.createHorizontalBox();
      box.add(Box.createHorizontalGlue());
      box.add(button = new JButton("Trade"), BorderLayout.SOUTH);
      
      button.addActionListener(new ActionListener()
      {
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            setVisible(false);
            
            Trade trade = getValue();
            
            if (trade != null)
               view.sendMessage(Type.TRADE, trade.getFrom(), trade);
         }
      });
      
      box.add(Box.createHorizontalGlue());
      panel.add(box, BorderLayout.SOUTH);
      
      panel.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
      add(panel);
      
      pack();
      setLocationRelativeTo(view.getFrame());
      setVisible(true);
   }
   
   Trade getValue()
   {
      List<Node> offered = new ArrayList<Node>();
      List<Node> requested = new ArrayList<Node>();
      int cash = (Integer)cashSpinner.getValue();
      
      if (fromList != null)
         for (Object node : fromList.getSelectedValues())
            offered.add((Node)node);
      if (toList != null)
         for (Object node : toList.getSelectedValues())
            requested.add((Node)node);
      
      if (offered.isEmpty() && requested.isEmpty() && cash == 0)
         return null;
      else
         return new Trade(from, to, offered, requested, cash);
   }
}
