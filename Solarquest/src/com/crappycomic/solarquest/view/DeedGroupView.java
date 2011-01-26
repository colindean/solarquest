// Solarquest
// Copyright (C) 2011 Colin Bartolome
// Licensed under the GPL. See LICENSE.txt for details.

package com.crappycomic.solarquest.view;

import java.awt.*;
import java.util.StringTokenizer;

class DeedGroupView
{
   private String displayName;
   
   private Color displayColor;
   
   void setDisplayName(String displayName)
   {
      this.displayName = displayName;
   }
   
   String getDisplayName()
   {
      return displayName;
   }
   
   void setDisplayColor(String displayColor)
   {
      StringTokenizer token = new StringTokenizer(displayColor);
      
      this.displayColor = new Color(Integer.parseInt(token.nextToken()), Integer.parseInt(token.nextToken()), Integer.parseInt(token.nextToken()));
   }

   Color getDisplayColor()
   {
      return displayColor;
   }
}
