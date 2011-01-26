// Solarquest
// Copyright (C) 2011 Colin Bartolome
// Licensed under the GPL. See LICENSE.txt for details.

package com.crappycomic.solarquest.view;

import java.awt.*;

import javax.swing.*;

public class StatusPanel extends JPanel
{
   private static final long serialVersionUID = 0;
   
   public static final int DEFAULT_WIDTH = BoardPanel.DEFAULT_WIDTH;
   public static final int DEFAULT_HEIGHT = 150;
   
   private static final int BORDER = 4;
   
   private JTextArea textArea;
   
   public StatusPanel()
   {
      setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
      setLayout(new GridLayout(1, 1));
      
      textArea = new JTextArea();
      textArea.setEditable(false);
      textArea.setBorder(BorderFactory.createEmptyBorder(BORDER, BORDER, BORDER, BORDER));
      add(new JScrollPane(textArea));
      
      setBorder(BorderFactory.createLoweredBevelBorder());
   }

   public void appendText(String text)
   {
      textArea.append(text);
      textArea.append("\n");
      textArea.setCaretPosition(textArea.getText().length());
   }
}
