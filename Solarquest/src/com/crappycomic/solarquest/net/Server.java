package com.crappycomic.solarquest.net;

import java.io.IOException;
import java.util.*;

import com.crappycomic.solarquest.model.*;

/** Handles communication between the connection and the {@link ServerModel}. */
public class Server
{
   public interface HandshakeObserver
   {
      void connectionAdded(ServerSideConnection connection);
      void playerAdded(ServerSideConnection connection, int player, String name);
   }
   
   public static final int DEFAULT_PORT = 1986;
   
   private LocalConnection localConnection;
   
   private Set<ServerSideConnection> connections;
   
   private DirectServer directServer;
   
   private Thread directServerThread;
   
   private Map<ServerSideConnection, Set<Integer>> playerMap;
   
   private ServerModel model;
   
   private HandshakeObserver observer;
   
   public Server(HandshakeObserver observer)
   {
      this.observer = observer;
      connections = Collections.synchronizedSet(new HashSet<ServerSideConnection>());
      playerMap = Collections.synchronizedMap(new HashMap<ServerSideConnection, Set<Integer>>());
   }
   
   public boolean listenForDirectConnections(int port)
   {
      boolean success = (directServer = new DirectServer(this, port)).createServerSocket();
      
      if (success)
      {
         directServerThread = new Thread(directServer);
         directServerThread.setDaemon(true);
         directServerThread.start();
      }
      
      return success;
   }
   
   public void stopListeningForDirectConnections()
   {
      if (directServer != null)
         directServer.setLooping(false);
      
      if (directServerThread != null)
         directServerThread.interrupt();
   }
   
   public void setLocalConnection(LocalConnection connection)
   {
      localConnection = connection;
      connections.add(connection);
   }
   
   public ServerSideConnection getLocalConnection()
   {
      return localConnection;
   }
   
   void addConnection(ServerSideConnection connection)
   {
      connections.add(connection);
      observer.connectionAdded(connection);
   }
   
   public Set<Integer> getPlayers(ServerSideConnection connection)
   {
      return playerMap.get(connection);
   }
   
   public void setPlayers(ServerSideConnection connection, Set<Integer> players)
   {
      playerMap.put(connection, players);
   }
   
   public void setModel(ServerModel model)
   {
      this.model = model;
   }
   
   public void sendObject(Object object)
   {
      synchronized (connections)
      {
         // TODO: Concurrent modification on dropped connection!
         for (ServerSideConnection connection : connections)
         {
            sendObject(object, connection);
         }
      }
   }
   
   public void sendObject(Object object, ServerSideConnection connection)
   {
      try
      {
         connection.sendServerObject(object);
         System.out.println("Server sent: " + object);
      }
      catch (IOException ioe)
      {
         System.err.println("Exception while sending object: " + ioe.toString());
         playersDropped(connection);
      }
   }
   
   void receiveMessage(ServerSideConnection connection, ModelMessage message)
   {
      model.receiveMessage(connection, message);
   }
   
   void playersDropped(ServerSideConnection connection)
   {
      connections.remove(connection);
      model.playersDropped(playerMap.get(connection));  
   }
   
   public void start()
   {
      observer = null;
      sendObject(model.createClientModel());
      
      for (ServerSideConnection connection : connections)
      {
         System.out.println("Sending " + playerMap.get(connection) + " to " + connection);
         sendObject(playerMap.get(connection), connection);
      }
      
      model.start();
   }

   public void receivePlayerChoice(ServerSideConnection connection, Pair<?, ?> choice)
   {
      observer.playerAdded(connection, (Integer)choice.getFirst(), (String)choice.getSecond());
   }
}
