// Solarquest
// Copyright (C) 2011 Colin Bartolome
// Licensed under the GPL. See LICENSE.txt for details.

package com.crappycomic.solarquest.net;

import java.io.*;
import java.net.Socket;

import com.crappycomic.solarquest.model.*;

/** Represents the server half of a direct connection via TCP/IP. */
public class DirectServerSideConnection implements ServerSideConnection
{
   private Server server;
   
   private ObjectInputStream in;
   
   private ObjectOutputStream out;
   
   private boolean notifyServer;
   
   private Thread readThread;
   
   DirectServerSideConnection(Server server, Socket socket) throws IOException
   {
      this.server = server;
      in = new ObjectInputStream(socket.getInputStream());
      out = new ObjectOutputStream(socket.getOutputStream());
   }
   
   @Override
   public void sendServerObject(Object object) throws IOException
   {
      try
      {
         out.writeObject(object);
      }
      catch (IOException ioe)
      {
         System.err.println("Error sending object: " + ioe.toString());
         notifyServer = false;
         readThread.interrupt();
         throw ioe;
      }
   }
   
   @Override
   public void run()
   {
      readThread = Thread.currentThread();
      
      while (true)
      {
         try
         {
            Object object = in.readObject();
            
            if (object instanceof ModelMessage)
               server.receiveMessage(this, (ModelMessage)object);
            else if (object instanceof Pair<?, ?>)
               server.receivePlayerChoice(this, (Pair<?, ?>)object);
            
            System.out.println("Server received: " + object);
         }
         catch (Exception e)
         {
            System.err.println("Error receiving object: " + e.toString());
            break;
         }
         
         if (Thread.interrupted())
            break;
      }
      
      if (notifyServer)
         server.playersDropped(this);
   }
}
