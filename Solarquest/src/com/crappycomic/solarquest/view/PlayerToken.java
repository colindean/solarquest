// Solarquest
// Copyright (C) 2011 Colin Bartolome
// Licensed under the GPL. See LICENSE.txt for details.

package com.crappycomic.solarquest.view;

import java.awt.*;

import javax.swing.JComponent;

public class PlayerToken extends JComponent
{
   private static final long serialVersionUID = 0;
   
   public static final Color[] PLAYER_COLORS =
   {
      Color.RED,
      Color.CYAN,
      Color.YELLOW,
      Color.MAGENTA,
      Color.BLUE,
      Color.GREEN
   };
   
   public static final int MAX_PLAYER_COUNT = PLAYER_COLORS.length;
   
   public static final int SIZE = 20;
   
   private Color color;
   
   public PlayerToken(int player)
   {
      this.color = PLAYER_COLORS[player];
      setOpaque(false);
      setPreferredSize(new Dimension(SIZE, SIZE));
   }
   
   @Override
   protected void paintComponent(Graphics g)
   {
      super.paintComponent(g);
      
      Graphics2D g2 = (Graphics2D)g.create();
      
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(color);
      g2.fillOval(0, 0, getWidth(), getHeight());
      g2.dispose();
   }
}
