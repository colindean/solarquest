package com.crappycomic.solarquest.net;

import java.io.IOException;
import java.net.*;

/** A simple server that uses direct connections over TCP/IP. */
public class DirectServer implements Runnable
{
   private static int ACCEPT_TIMEOUT = 1000;
   
   private Server server;
   
   private int port;
   
   private ServerSocket serverSocket;
   
   private boolean looping;
   
   DirectServer(Server server, int port)
   {
      this.server = server;
      this.port = port;
      looping = true;
   }
   
   public boolean createServerSocket()
   {
      try
      {
         serverSocket = new ServerSocket(port);
         serverSocket.setSoTimeout(ACCEPT_TIMEOUT);
      }
      catch (Exception e)
      {
         System.err.println("Error opening server socket: " + e.toString());
         return false;
      }
      
      return true;
   }
   
   @Override
   public void run()
   {
      System.out.println("Waiting for direct connections...");
      
      try
      {
         while (looping)
         {
            Socket socket = null;
            
            while (true)
            {
               try
               {
                  socket = serverSocket.accept();
               }
               catch (SocketTimeoutException ste)
               {
                  if (!looping || Thread.interrupted())
                     break;
               }
               
               if (socket != null)
                  break;
            }
            
            if (!looping || socket == null)
               break;
            
            DirectServerSideConnection connection = new DirectServerSideConnection(server, socket);
            
            server.addConnection(connection);
            new Thread(connection).start();
            System.out.println("Accepted connection from: " + socket.getRemoteSocketAddress());
         }
      }
      catch (IOException ioe)
      {
         System.err.println("Error accepting connection: " + ioe.toString());
      }
      
      try
      {
         serverSocket.close();
      }
      catch (IOException ioe)
      {
         System.err.println("Error closing server socket: " + ioe.toString());
      }
      
      System.out.println("Server no longer listening");
   }

   void setLooping(boolean looping)
   {
      this.looping = looping;
   }
}
