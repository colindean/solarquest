// Solarquest
// Copyright (C) 2011 Colin Bartolome
// Licensed under the GPL. See LICENSE.txt for details.

package com.crappycomic.solarquest.model;

import java.io.*;
import java.util.*;

import org.xml.sax.SAXException;

/** Driver class that can be helpful for exploring a {@link Board} and testing its pathfinding. */
public class BoardTest implements Runnable
{
   private Board board;
   
   private Node currentNode;
   
   private List<Node> currentNodeDestinations;
   
   private boolean looping;
   
   public static void main(String[] args) throws SAXException, IOException
   {
      new BoardTest(args[0]).run();
   }
   
   BoardTest(String filename) throws SAXException, IOException
   {
      board = new ModelXMLLoader().loadGame(filename).getBoard();
      currentNode = board.getStartNode();
      currentNodeDestinations = new ArrayList<Node>();
   }
   
   @Override
   public void run()
   {
      looping = true;
      
      while (looping)
      {
         printStatus();
         printMenu();
         try
         {
            doInput();
         }
         catch (IOException ioe)
         {
            System.err.println("Error reading input: " + ioe.toString());
            looping = false;
         }
         catch (NumberFormatException nfe)
         {
            System.out.println("Invalid input: " + nfe.toString());
         }
      }
   }
   
   public void printStatus()
   {
      System.out.println("Current Node: " + currentNode.getID());
      
      System.out.print("Destinations:");
      
      currentNodeDestinations.clear();
      
      for (Node destination : currentNode.getDestinations())
      {
         currentNodeDestinations.add(destination);
         System.out.print(" -> " + destination.getID());
      }
      
      System.out.println();
      
      System.out.println();
   }
   
   public void printMenu()
   {
      System.out.println("A) Advance [destination #]");
      System.out.println("M) Show Allowed Moves <distance>");
      System.out.println("P) Does a move to <node> from here pass Start?");
      System.out.println("T) Teleport <node>");
      System.out.println("Q) Quit");
      System.out.println();
   }
   
   public void doInput() throws IOException
   {
      System.out.print("> ");

      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      StringTokenizer tokenizer = new StringTokenizer(reader.readLine());
      
      System.out.println();
      
      if (!tokenizer.hasMoreTokens())
         return;
      
      String command = tokenizer.nextToken();
      
      if (command.equalsIgnoreCase("A"))
      {
         int ndx = tokenizer.hasMoreTokens() ? Integer.parseInt(tokenizer.nextToken()) : 0;
         
         if (ndx < 0 || ndx >= currentNodeDestinations.size())
            ndx = 0;
         
         currentNode = currentNodeDestinations.get(ndx);
      }
      else if (command.equalsIgnoreCase("M"))
      {
         int distance = tokenizer.hasMoreTokens() ? Integer.parseInt(tokenizer.nextToken()) : 0;
         
         if (distance < 0)
            distance = 0;
         
         System.out.print("Allowed moves:");
         
         for (Node destination : board.getAllowedMoves(currentNode, distance))
            System.out.print("  " + destination.getID());
         
         System.out.println();
      }
      else if (command.equalsIgnoreCase("P"))
      {
         String nodeID = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "";
         
         if (board.hasNode(nodeID))
         {
            System.out.println(currentNode.getID() + " -> " + nodeID
               + (board.passesStart(currentNode, board.getNode(nodeID)) ? " Passes Start" : " Does Not Pass Start"));
         }
      }
      else if (command.equalsIgnoreCase("T"))
      {
         String nodeID = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "";
         
         if (board.hasNode(nodeID))
            currentNode = board.getNode(nodeID);
      }
      else if (command.equalsIgnoreCase("Q"))
      {
         looping = false;
      }
      
      System.out.println();
   }
}
