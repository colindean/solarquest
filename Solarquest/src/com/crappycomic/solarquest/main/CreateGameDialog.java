// Solarquest
// Copyright (C) 2011 Colin Bartolome
// Licensed under the GPL. See LICENSE.txt for details.

package com.crappycomic.solarquest.main;

import java.awt.event.*;
import java.io.IOException;
import java.util.*;

import javax.swing.*;
import javax.swing.Action;

import org.xml.sax.SAXException;

import com.crappycomic.solarquest.model.*;
import com.crappycomic.solarquest.net.*;
import com.crappycomic.solarquest.view.*;

/** Sets up all relevant options and creates a new game. */
@SuppressWarnings("serial")
public class CreateGameDialog extends JDialog implements Server.HandshakeObserver
{
   static final int GAP = 5;
   static final int TEXT_FIELD_NARROW = 4;
   static final int TEXT_FIELD_WIDE = 10;
   
   /** Enumerates the possible states each player slot can be. */
   private static enum PlayerState
   {
      NONE,
      LOCAL,
      REMOTE;
      
      @Override
      public String toString()
      {
         return name().substring(0, 1) + name().substring(1).toLowerCase();
      }
   }
   
   /** The allowed states when creating a new game. */
   private static final PlayerState[] STATES_NORMAL = PlayerState.values();
   
   /** The allowed states when loading an existing game. */
   private static final PlayerState[] STATES_LOADED_OPEN = new PlayerState[]
   {
      PlayerState.LOCAL,
      PlayerState.REMOTE
   };
   
   /** Handles the "Enable/Disable Network Play" button functionality. */
   private class NetworkPlayAction extends AbstractAction
   {
      boolean enabled;
      
      private NetworkPlayAction()
      {
         super();
         
         putValue(Action.NAME, getName());
      }
      
      private String getName()
      {
         return enabled ? "Disable Network Play" : "Enable Network Play";
      }
      
      @Override
      public void actionPerformed(ActionEvent evt)
      {
         if (enabled)
         {
            server.stopListeningForDirectConnections();
         }
         else
         {
            int port;
            
            try
            {
               port = Integer.parseInt(portField.getText());
            }
            catch (NumberFormatException nfe)
            {
               port = 0;
            }
            
            boolean success = server.listenForDirectConnections(port);
            
            if (!success)
            {
               JOptionPane.showMessageDialog(CreateGameDialog.this,
                  "Could not create the Network Play server.\n(Is something using that port already?)",
                  "Error creating server", JOptionPane.ERROR_MESSAGE);
               return;
            }
         }
         
         enabled = !enabled;
         putValue(Action.NAME, getName());
      }
   }
   
   private class PlayerStateBox extends JComboBox
   {
      private PlayerStateBox(final int player, PlayerState[] states)
      {
         super(states);
         
         addItemListener(new ItemListener()
         {
            @Override
            public void itemStateChanged(ItemEvent evt)
            {
               playerNames[player].setEditable(getSelectedItem() == PlayerState.LOCAL);
               playerNames[player].setText("");
               
               if (getSelectedItem() != PlayerState.REMOTE)
                  playerMap.remove(player);
            }
         });
      }
   }
   
   private Server server;
   
   private Map<Integer, ServerSideConnection> playerMap;
   
   private JComboBox gameBox;
   
   private JComboBox ruleSetBox;
   
   private JTextField portField;
   
   private JTextField[] playerNames;
   
   private PlayerStateBox[] playerStateBoxes;
   
   /** Creates and displays a new instance, initialized to match the given (loaded) serverModel, if passed. */
   public CreateGameDialog(JFrame owner, final ServerModel serverModel)
   {
      super(owner, "Create a Game", true);
      
      server = new Server(this);
      playerMap = new HashMap<Integer, ServerSideConnection>();
      
      JPanel panel = new JPanel();
      JLabel label;
      JButton button;
      
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      
      Box hBox;
      Box vBox;
      
      hBox = Box.createHorizontalBox();
      
      vBox = Box.createVerticalBox();
      vBox.add(Box.createVerticalGlue());
      vBox.add(label = new JLabel("Game:"));
      label.setAlignmentX(1.0f);
      vBox.add(Box.createVerticalGlue());
      vBox.add(label = new JLabel("Rule Set:"));
      label.setAlignmentX(1.0f);
      vBox.add(Box.createVerticalGlue());
      hBox.add(vBox);
      
      hBox.add(Box.createHorizontalStrut(GAP));
      
      vBox = Box.createVerticalBox();
      vBox.add(Box.createVerticalGlue());
      vBox.add(gameBox = serverModel == null ? new JComboBox(ModelXMLLoader.getAvailableGames()) : new JComboBox());
      vBox.add(Box.createVerticalStrut(GAP));
      vBox.add(ruleSetBox = serverModel == null ? new JComboBox(ModelXMLLoader.getAvailableRuleSets()) : new JComboBox());
      vBox.add(Box.createVerticalGlue());
      hBox.add(vBox);
      
      if (serverModel != null)
      {
         // TODO: it would be nice to pre-populate these; the friendly names would have to become XML attributes
         gameBox.setEnabled(false);
         ruleSetBox.setEnabled(false);
      }
      
      panel.add(hBox);
      panel.add(Box.createVerticalStrut(GAP));
      
      hBox = Box.createHorizontalBox();
      hBox.add(new JButton(new NetworkPlayAction()));
      hBox.add(Box.createHorizontalStrut(GAP));
      hBox.add(new JLabel("Port:"));
      hBox.add(Box.createHorizontalStrut(GAP));
      hBox.add(portField = new JTextField(Integer.toString(Server.DEFAULT_PORT), TEXT_FIELD_NARROW));
      
      panel.add(hBox);
      
      playerNames = new JTextField[PlayerToken.MAX_PLAYER_COUNT];
      playerStateBoxes = new PlayerStateBox[PlayerToken.MAX_PLAYER_COUNT];
      
      for (int player = 0; player < PlayerToken.MAX_PLAYER_COUNT; player++)
      {
         JPanel tokenPanel;
         PlayerState[] states;
         
         playerNames[player] = new JTextField(TEXT_FIELD_WIDE);
         
         if (serverModel == null)
         {
            playerNames[player].setEditable(false);
            states = STATES_NORMAL;
         }
         else if (serverModel.getPlayerName(player) == null)
         {
            continue;
         }
         else
         {
            playerNames[player].setText(serverModel.getPlayerName(player));
            states = STATES_LOADED_OPEN;
         }

         playerStateBoxes[player] = new PlayerStateBox(player, states);

         hBox = Box.createHorizontalBox();
         hBox.add(tokenPanel = new JPanel());
         tokenPanel.add(new PlayerToken(player));
         hBox.add(Box.createHorizontalStrut(GAP));
         hBox.add(new JLabel("Name:"));
         hBox.add(Box.createHorizontalStrut(GAP));
         hBox.add(playerNames[player]);
         hBox.add(Box.createHorizontalStrut(GAP));
         hBox.add(playerStateBoxes[player]);
         
         panel.add(Box.createVerticalStrut(GAP));
         panel.add(hBox);
      }
      
      hBox = Box.createHorizontalBox();
      hBox.add(Box.createHorizontalGlue());
      hBox.add(button = new JButton("Start"));
      hBox.add(Box.createHorizontalGlue());
      
      button.addActionListener(new ActionListener()
      {
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            boolean success;
            
            try
            {
               success = start(serverModel);
            }
            catch (Exception e)
            {
               System.err.println("Exception while starting game: " + e.toString());
               e.printStackTrace(System.err);
               success = false;
            }
            
            if (success)
            {
               server.stopListeningForDirectConnections();
               getOwner().setVisible(false);
               setVisible(false);
            }
         }
      });
      
      panel.add(Box.createVerticalStrut(GAP));
      panel.add(hBox);

      panel.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
      add(panel);
      pack();
      setLocationRelativeTo(owner);
      setVisible(true);
   }
   
   /** Informs all clients which players are local, sets up the {@link View}, and starts the game. */
   private boolean start(ServerModel serverModel) throws SAXException, IOException
   {
      // Do all the error checking first, before setting up any other game objects.
      int playerCount = 0;
      
      for (int ndx = 0; ndx < playerStateBoxes.length; ndx++)
      {
         if (playerStateBoxes[ndx] == null)
            continue;
         
         Object playerState = playerStateBoxes[ndx].getSelectedItem();
         
         if (playerState == PlayerState.LOCAL)
         {
            if (playerNames[ndx].getText().trim().isEmpty())
            {
               JOptionPane.showMessageDialog(this,
                  "Please enter a name for every local player.",
                  "Error", JOptionPane.ERROR_MESSAGE);
               return false;
            }
            
            playerCount++;
         }
         else if (playerState == PlayerState.REMOTE)
         {
            if (playerNames[ndx].getText().trim().isEmpty())
            {
               JOptionPane.showMessageDialog(this,
                  "One or more Remote player slots are still open.\nPlease close before continuing.",
                  "Error", JOptionPane.ERROR_MESSAGE);
               return false;
            }
            
            playerCount++;
         }
      }
      
      if (playerCount < 2)
      {
         JOptionPane.showMessageDialog(this, "You can't have a game with fewer than two players!", "Error", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      // Now set up the game objects for real.
      List<Player> players = serverModel == null ? new ArrayList<Player>() : serverModel.getPlayers();
      Client client = new Client(null);
      LocalConnection localConnection = new LocalConnection(server, client);

      server.setLocalConnection(localConnection);
      
      for (int ndx = 0; ndx < playerStateBoxes.length; ndx++)
      {
         if (playerStateBoxes[ndx] == null)
            continue;
         
         Object playerState = playerStateBoxes[ndx].getSelectedItem();
         
         if (playerState == PlayerState.LOCAL)
         {
            addPlayer(serverModel, players, ndx, true);
         }
         else if (playerState == PlayerState.REMOTE)
         {
            addPlayer(serverModel, players, ndx, false);
         }
      }

      if (serverModel == null)
      {
         ModelXMLLoader xmlLoader = new ModelXMLLoader();

         serverModel = xmlLoader.loadGame(((ModelXMLLoader.XMLOption)gameBox.getSelectedItem()).getID());
         serverModel.setRuleSet(xmlLoader.loadRuleSet(((ModelXMLLoader.XMLOption)ruleSetBox.getSelectedItem()).getID()));

         Collections.shuffle(players);
         serverModel.setPlayers(players);
         
         serverModel.initialize();
      }

      serverModel.setServer(server);
      server.setModel(serverModel);
      
      View view = new GraphicView();
      
      view.setClient(client);
      view.setServerModel(serverModel);

      client.setConnection(localConnection);

      server.start();
      
      return true;
   }
   
   /** Sets up the given player with the appropriate client, creating the player, if necessary. */
   private void addPlayer(ServerModel serverModel, List<Player> players, int ndx, boolean local)
   {
      ServerSideConnection connection = local ? server.getLocalConnection() : playerMap.get(ndx);
      String name = playerNames[ndx].getText();
      Set<Integer> localPlayers = server.getPlayers(connection);
      Player player = serverModel == null ? new Player(ndx) : findPlayer(players, ndx);
      
      player.setName(name);
      
      if (serverModel == null)
         players.add(player);
      
      if (localPlayers == null)
      {
         server.setPlayers(connection, localPlayers = new HashSet<Integer>());
      }
      
      localPlayers.add(ndx);
   }
   
   /** Returns the player with the given number, or null if that player does not exist. */
   private static Player findPlayer(Iterable<Player> players, int number)
   {
      for (Player player : players)
         if (player.getNumber() == number)
            return player;
      
      return null;
   }

   /** Returns a set of Remote player slots that have not yet been filled. */
   private synchronized Set<Integer> getAvailablePlayers()
   {
      Set<Integer> availablePlayers = new TreeSet<Integer>();
      
      for (int ndx = 0; ndx < playerStateBoxes.length; ndx++)
         if (playerStateBoxes[ndx] != null && playerStateBoxes[ndx].getSelectedItem() == PlayerState.REMOTE
               && playerNames[ndx].getText().isEmpty())
            availablePlayers.add(ndx);
      
      return availablePlayers;
   }
   
   /** Sends the list of available players to the given connection. */
   @Override
   public synchronized void connectionAdded(ServerSideConnection connection)
   {
      server.sendObject(getAvailablePlayers(), connection);
   }

   /** Adds the given player, or sends an error if the slot or all slots are full. */
   @Override
   public synchronized void playerAdded(ServerSideConnection connection, int player, String name)
   {
      Set<Integer> availablePlayers = getAvailablePlayers();
      
      if (availablePlayers.isEmpty())
      {
         server.sendObject("This server is full.", connection);
      }
      else if (!availablePlayers.contains(player))
      {
         server.sendObject("That player is unavailable. Please select another.", connection);
         server.sendObject(getAvailablePlayers(), connection);
      }
      else
      {
         playerMap.put(player, connection);
         playerNames[player].setText(name);
         
         availablePlayers = getAvailablePlayers();
         
         if (!availablePlayers.isEmpty())
            server.sendObject(availablePlayers, connection);
      }
   }
}
