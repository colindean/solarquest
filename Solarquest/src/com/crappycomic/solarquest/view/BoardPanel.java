// Solarquest
// Copyright (C) 2011 Colin Bartolome
// Licensed under the GPL. See LICENSE.txt for details.

package com.crappycomic.solarquest.view;

import static java.lang.Math.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;

import com.crappycomic.solarquest.model.*;

public class BoardPanel extends JPanel
{
   private static final long serialVersionUID = 0;
   
   public static final int DEFAULT_WIDTH = 640;

   public static final int DEFAULT_HEIGHT = 640;

   private static final int NODE_SIZE = 40;
   
   private static final int NODE_BORDER_WIDTH = 4;
   
   private static final Stroke NODE_BORDER_STROKE = new BasicStroke(NODE_BORDER_WIDTH);
   
   private static final int NODE_ALPHA = 128;
   
   private static final Color COLOR_FUELED = new Color(192, 192, 192);
   
   private static final int PLAYER_BORDER_WIDTH = 2;
   
   private static final Stroke PLAYER_BORDER_STROKE = new BasicStroke(PLAYER_BORDER_WIDTH);
   
   private static final Color PLAYER_BORDER_COLOR = Color.BLACK;
   
   private static final int IMAGE_TYPE = BufferedImage.TYPE_INT_RGB;
   
   private static final double THETA_PHASE = PI / 2;
   
   private static final int PAN_FPS = 40;
   
   private static final int PAN_DELAY = 1000 / PAN_FPS;
   
   private static final int PAN_DURATION = 500;
   
   private static final Color COLOR_HIGHLIGHT = Color.WHITE;
   
   private static final int HIGHLIGHT_SIZE = 46;
   
   private static final int HIGHLIGHT_BORDER_WIDTH = 7;
   
   private static final Stroke HIGHLIGHT_BORDER_STROKE = new BasicStroke(HIGHLIGHT_BORDER_WIDTH);

   private GraphicView view;
   
   private Model model;
   
   private JButton zoomButton;

   private BufferedImage boardImage;
   
   private BufferedImage displayImage;

   private int imageWidth;
   
   private int imageHeight;
   
   private int panelWidth;
   
   private int panelHeight;
   
   private double scaleFit;

   private double scaleZoomed;

   private boolean zoomed;
   
   private int dragX;
   
   private int dragY;
   
   private int translateX;
   
   private int translateY;

   private long panStartTime;
   
   private long panEndTime;
   
   private int panStartX;
   
   private int panStartY;
   
   private int panEndX;
   
   private int panEndY;
   
   private Timer panTimer;
   
   private boolean dragged;
   
   private Collection<Node> highlightedNodes;

   BoardPanel(final GraphicView view, final Model model, String boardImageFilename)
   {
      this.view = view;
      this.model = model;
      
      setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
      setBackground(Color.BLACK);
      setLayout(new BorderLayout());

      loadBoardImage(boardImageFilename);

      Box box = Box.createHorizontalBox();

      box.add(Box.createHorizontalGlue());
      box.add(zoomButton = new JButton("Zoom"));

      zoomButton.addMouseListener(new MouseAdapter()
      {
         @Override
         public void mouseClicked(MouseEvent evt)
         {
            zoomed = !zoomed;
            repaint();
         }
      });

      add(box, BorderLayout.SOUTH);

      addComponentListener(new ComponentAdapter()
      {
         @Override
         public void componentResized(ComponentEvent evt)
         {
            adjustScales();
            center(getPanPoint(), false);
            repaint();
         }
      });
      
      addMouseListener(new MouseAdapter()
      {
         @Override
         public void mousePressed(MouseEvent evt)
         {
            if (zoomed && boardImage != null)
            {
               dragX = evt.getX();
               dragY = evt.getY();
            }
            
            dragged = false;
         }
         
         @Override
         public void mouseReleased(MouseEvent evt)
         {
            if (dragged)
               return;
            
            double x = evt.getX() / getScale() - (zoomed ? translateX : 0);
            double y = evt.getY() / getScale() - (zoomed ? translateY : 0);
            double minDistance = Double.MAX_VALUE;
            Node closestNode = null;
            
            for (Node node : model.getNodes())
            {
               Point nodeCenter = view.getNodeCoords(node.getID());
               double distance = Math.pow(x - nodeCenter.x, 2) + Math.pow(y - nodeCenter.y, 2); // distance squared, actually
               
               if (distance < minDistance)
               {
                  minDistance = distance;
                  closestNode = node;
               }
            }
            
            view.updateNodePanel(closestNode != null && closestNode.canOwn() ? closestNode : null);
            
            if (highlightedNodes != null)
               view.updateChosenNode(closestNode);
         }
      });
      
      addMouseMotionListener(new MouseAdapter()
      {
         @Override
         public void mouseDragged(MouseEvent evt)
         {
            if (zoomed && boardImage != null)
            {
               int deltaX = evt.getX() - dragX;
               int deltaY = evt.getY() - dragY;
               Point point = clampTranslates(translateX + deltaX, translateY + deltaY);
               
               translateX = point.x;
               translateY = point.y;
               dragX = evt.getX();
               dragY = evt.getY();
   
               repaint();
            }
            
            dragged = true;
         }
      });
      
      adjustScales();
      
      zoomed = true;
      
      panTimer = new Timer(PAN_DELAY, new ActionListener()
      {
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            if (System.currentTimeMillis() >= panEndTime)
            {
               translateX = panEndX;
               translateY = panEndY;
               panTimer.stop();
            }
            else
            {
               Point point = getPanPoint();
               
               translateX = point.x;
               translateY = point.y;               
            }
            
            repaint();
         }
      });
   }

   private void loadBoardImage(String filename)
   {
      try
      {
         BufferedImage image = ImageIO.read(getClass().getResourceAsStream(filename));
         
         boardImage = new BufferedImage(image.getWidth(), image.getHeight(), IMAGE_TYPE);
         
         for (int x = 0; x < image.getWidth(); x++)
            for (int y = 0; y < image.getHeight(); y++)
               boardImage.setRGB(x, y, image.getRGB(x, y));
      }
      catch (Exception e)
      {
         System.err.println("Exception loading board image: " + e.toString());
      }
   }

   void adjustScales()
   {
      if (boardImage != null)
      {
         imageWidth = boardImage.getWidth(this);
         imageHeight = boardImage.getHeight(this);
         panelWidth = getWidth();
         panelHeight = getHeight();
         
         double scaleFitX = (double)panelWidth / imageWidth;
         double scaleFitY = (double)panelHeight / imageHeight;
         
         scaleFit = Math.min(scaleFitX, scaleFitY);
         scaleZoomed = 1;
      }
   }
   
   private double getScale()
   {
      return zoomed ? scaleZoomed : scaleFit;
   }
   
   @Override
   protected void paintComponent(Graphics g)
   {
      if (displayImage == null)
      {
         super.paintComponent(g);
      }
      else
      {
         Graphics2D g2 = (Graphics2D)g;
         AffineTransform transform = new AffineTransform();
         
         if (zoomed)
         {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            
            transform.translate(translateX, translateY);
         }
         else
         {
            double scale = getScale();
            
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, panelWidth, panelHeight);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
            int centerX = (int)((panelWidth - imageWidth * scale) / 2);
            int centerY = (int)((panelHeight - imageHeight * scale) / 2);
            
            transform.translate(centerX, centerY);
            transform.scale(scale, scale);
         }
   
         g2.drawImage(displayImage, transform, this);
      }
   }
   
   private Point clampTranslates(int x, int y)
   {
      return new Point((int)Math.min(Math.max(x, panelWidth - (imageWidth * scaleZoomed)), 0),
         (int)Math.min(Math.max(y, panelHeight - (imageHeight * scaleZoomed)), 0));
   }
   
   void updateBoard()
   {
      updateBoard(null, false);
   }

   void updateBoard(Point center)
   {
      updateBoard(center, true);
   }
   
   void updateBoard(Point center, boolean panSmoothly)
   {
      int width = boardImage == null ? getWidth() : imageWidth;
      int height = boardImage == null ? getHeight() : imageHeight;
      
      displayImage = new BufferedImage(width, height, IMAGE_TYPE);
      
      Graphics2D g2 = (Graphics2D)displayImage.getGraphics();
      
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      if (boardImage == null)
      {
         g2.setColor(Color.BLACK);
         g2.fillRect(0, 0, width, height);
      }
      else
      {
         boardImage.copyData(displayImage.getRaster());
      }
      
      for (Node node : model.getNodes())
      {
         Point nodeCenter = view.getNodeCoords(node.getID());
         
         if (node.getOwner() != null)
         {
            Color color = PlayerToken.PLAYER_COLORS[node.getOwner().getNumber()];
            
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), NODE_ALPHA));
            g2.fillOval(nodeCenter.x - (NODE_SIZE / 2), nodeCenter.y - (NODE_SIZE / 2), NODE_SIZE, NODE_SIZE);
         }
         
         if (highlightedNodes != null && highlightedNodes.contains(node))
         {
            g2.setStroke(HIGHLIGHT_BORDER_STROKE);
            g2.setColor(COLOR_HIGHLIGHT);
            g2.drawOval(nodeCenter.x - (HIGHLIGHT_SIZE / 2), nodeCenter.y - (HIGHLIGHT_SIZE / 2), HIGHLIGHT_SIZE, HIGHLIGHT_SIZE);
         }
         
         if (node.hasFuelStation())
         {
            g2.setStroke(NODE_BORDER_STROKE);
            g2.setColor(COLOR_FUELED);         
            g2.drawOval(nodeCenter.x - (NODE_SIZE / 2), nodeCenter.y - (NODE_SIZE / 2), NODE_SIZE, NODE_SIZE);
         }
      }
      
      Map<Node, java.util.List<Player>> playersAtNodes = new HashMap<Node, java.util.List<Player>>();
      
      for (Player player : model.getPlayers())
      {
         if (player.isGameOver())
            continue;
         
         java.util.List<Player> playersAtNode = playersAtNodes.get(player.getCurrentNode());
         
         if (playersAtNode == null)
            playersAtNodes.put(player.getCurrentNode(), playersAtNode = new ArrayList<Player>());
         
         playersAtNode.add(player);
      }
      
      for (Map.Entry<Node, java.util.List<Player>> playersAtNode : playersAtNodes.entrySet())
      {
         Point nodeCenter = view.getNodeCoords(playersAtNode.getKey().getID());
         int players = playersAtNode.getValue().size();
         
         for (int player = 0; player < playersAtNode.getValue().size(); player++)
         {
            double theta = getTheta(player, players);
            Point2D.Double playerCenter = playersAtNode.getValue().size() == 1
               ? new Point2D.Double(nodeCenter.x, nodeCenter.y)
               : new Point2D.Double(nodeCenter.x + PlayerToken.SIZE * cos(theta), nodeCenter.y + PlayerToken.SIZE * -sin(theta));
            int x = (int)(playerCenter.x - PlayerToken.SIZE / 2);
            int y = (int)(playerCenter.y - PlayerToken.SIZE / 2);
            
            g2.setColor(PlayerToken.PLAYER_COLORS[playersAtNode.getValue().get(player).getNumber()]);
            g2.fillOval(x, y, PlayerToken.SIZE, PlayerToken.SIZE);
            g2.setStroke(PLAYER_BORDER_STROKE);
            g2.setColor(PLAYER_BORDER_COLOR);
            g2.drawOval(x, y, PlayerToken.SIZE, PlayerToken.SIZE);
         }
      }
      
      center(center, panSmoothly);
   }
   
   private double getTheta(int player, int players)
   {
      return 2 * PI * player / players + THETA_PHASE;
   }
   
   // Attempt to make the given point the center of the view
   void center(Point point, boolean smoothly)
   {
      if (point != null)
      {
         int x = -(point.x - panelWidth / 2);
         int y = -(point.y - panelHeight / 2);
         
         Point panPoint = clampTranslates(x, y);
         
         if (smoothly)
         {
            panStartTime = System.currentTimeMillis();
            panEndTime = panStartTime + PAN_DURATION;
            panStartX = translateX;
            panStartY = translateY;
            panEndX = panPoint.x;
            panEndY = panPoint.y;
            panTimer.start();
         }
         else
         {
            translateX = panPoint.x;
            translateY = panPoint.y;
         }
      }
      
      repaint();
   }
   
   private Point getPanPoint()
   {
      double t = (double)(System.currentTimeMillis() - panStartTime) / PAN_DURATION;
//      double factor = t; // linear
      double factor = 0.5 * (1 - cos(PI * t));
      double x = panStartX + (panEndX - panStartX) * factor;
      double y = panStartY + (panEndY - panStartY) * factor;
      
      return new Point((int)x, (int)y);
   }
   
   void setHighlightedNodes(Collection<Node> highlightedNodes)
   {
      this.highlightedNodes = highlightedNodes;
      updateBoard();
   }
}
