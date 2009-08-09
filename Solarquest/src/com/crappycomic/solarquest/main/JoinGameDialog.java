package com.crappycomic.solarquest.main;

import static com.crappycomic.solarquest.main.CreateGameDialog.*;

import java.awt.event.*;
import java.io.IOException;
import java.util.Set;

import javax.swing.*;

import com.crappycomic.solarquest.model.Pair;
import com.crappycomic.solarquest.net.*;
import com.crappycomic.solarquest.view.GraphicView;

/** Allows players to join an existing network game. */
@SuppressWarnings("serial")
public class JoinGameDialog extends JDialog implements Client.HandshakeObserver
{
   /** Simple progress bar dialog. */
   private static class WaitingDialog extends JDialog
   {
      public WaitingDialog(JDialog owner)
      {
         super((JDialog)null, "Waiting for Server");
         
         JProgressBar progressBar;
         
         add(progressBar = new JProgressBar());
         progressBar.setIndeterminate(true);
         
         pack();
         setLocationRelativeTo(owner);
         setVisible(true);
      }
   }
   
   private Client client;
   
   private JTextField hostField;
   
   private JTextField portField;
   
   private AvailablePlayersDialog availablePlayersDialog;
   
   private WaitingDialog waitingDialog;
   
   /** Creates and displays a new instance. */
   public JoinGameDialog(JFrame owner)
   {
      super(owner, "Join a Game", true);
      
      JPanel panel = new JPanel();
      JButton button;
      
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      
      Box hBox;
      
      hBox = Box.createHorizontalBox();
      hBox.add(new JLabel("Host:"));
      hBox.add(Box.createHorizontalStrut(GAP));
      hBox.add(hostField = new JTextField(TEXT_FIELD_WIDE));
      hBox.add(Box.createHorizontalStrut(GAP));
      hBox.add(new JLabel("Port:"));
      hBox.add(Box.createHorizontalStrut(GAP));
      hBox.add(portField = new JTextField(Integer.toString(Server.DEFAULT_PORT), TEXT_FIELD_NARROW));
      panel.add(hBox);
      
      panel.add(Box.createVerticalStrut(GAP));
      
      hBox = Box.createHorizontalBox();
      hBox.add(Box.createHorizontalGlue());
      hBox.add(button = new JButton("Connect"));
      hBox.add(Box.createHorizontalGlue());
      
      button.addActionListener(new ActionListener()
      {
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            connect();
         }
      });
      
      panel.add(hBox);
      
      panel.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
      add(panel);
      pack();
      setLocationRelativeTo(owner);
      setVisible(true);
   }
   
   /**
    * Connects to the specified hostname ("localhost" if none was entered) and port.
    * After connecting, begins waiting for the server to send lists of available player slots
    * and for the server to indicate the game is starting.
    */
   private void connect()
   {
      String host = hostField.getText();
      int port;
      
      if (host.isEmpty())
         host = "localhost";
      
      client = new Client(this);

      try
      {
         port = Integer.parseInt(portField.getText());
      }
      catch (NumberFormatException nfe)
      {
         JOptionPane.showMessageDialog(this, "Please enter a valid port number.", "Invalid Port Number", JOptionPane.ERROR_MESSAGE);
         return;
      }
      
      try
      {
         new GraphicView().setClient(client);
         
         client.connectDirect(host, port);
         
         waitingDialog = new WaitingDialog(this);
         
         setVisible(false);
         getOwner().setVisible(false);
      }
      catch (IOException ioe)
      {
         JOptionPane.showMessageDialog(this, "Connection failed:\n" + ioe.toString(), "Connection Failed", JOptionPane.ERROR_MESSAGE);
      }
   }

   /** Displays the given list of available player slots. */
   @Override
   public void availablePlayers(Set<Integer> availablePlayers)
   {
      if (availablePlayers.isEmpty())
         return;
      
      availablePlayersDialog = new AvailablePlayersDialog(this, availablePlayers);
      
      int player = availablePlayersDialog.getPlayer();
      
      if (player >= 0)
         client.sendObject(new Pair<Integer, String>(player, availablePlayersDialog.getPlayerName()));
   }
   
   /** Cleans up any leftover dialogs. */
   @Override
   public void starting()
   {
      if (availablePlayersDialog != null)
         availablePlayersDialog.setVisible(false);
      if (waitingDialog != null)
         waitingDialog.setVisible(false);
   }
}
