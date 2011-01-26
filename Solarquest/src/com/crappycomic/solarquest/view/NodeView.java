// Solarquest
// Copyright (C) 2011 Colin Bartolome
// Licensed under the GPL. See LICENSE.txt for details.

package com.crappycomic.solarquest.view;

import java.awt.Point;
import java.util.StringTokenizer;

class NodeView
{
   private String displayName;
   
   private Point coords;
   
   NodeView()
   {
   }
   
   void setDisplayName(String displayName)
   {
      this.displayName = displayName;
   }
      
   String getDisplayName()
   {
      return displayName;
   }

   void setCoords(String coords)
   {
      StringTokenizer token = new StringTokenizer(coords);
      
      this.coords = new Point(Integer.parseInt(token.nextToken()), Integer.parseInt(token.nextToken()));
   }
   
   Point getCoords()
   {
      return coords;
   }
}
