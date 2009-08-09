package com.crappycomic.solarquest.model;

import java.io.Serializable;
import java.util.*;

import com.crappycomic.solarquest.view.View;

/** Represents the {@link Node nodes} and cash one {@link Player player} is trading with another. */
public class Trade implements Serializable
{
   private static final long serialVersionUID = 123874703458889187L;

   private int from;
   
   private int to;
   
   private Collection<String> offered;
   
   private Collection<String> requested;
   
   private int cash;
   
   /**
    * Creates a new trade which, if accepted, will result in the {@link Node nodes} listed in
    * <code>offered</code> being transferred from <code>from</code> to <code>to</code>,
    * the opposite for the nodes listed in <code>requested</code>, and <code>cash</code>
    * being subtracted from <code>from</code> and added to <code>to</code>. (This means a
    * positive value for <code>cash</code> results in <code>to</code> gaining cash, while a
    * negative value results in <code>from</code> gaining cash.
    * 
    * Note that the actual data stored is a {@link Collection} of {@link String}s, because a
    * {@link View} is not allowed to send a {@link Node} in a {@link ModelMessage}.
    * 
    * Instances of this class are immutable.
    */
   public Trade(Player from, Player to, Iterable<Node> offered, Iterable<Node> requested, int cash)
   {
      this.from = from.getNumber();
      this.to = to.getNumber();
      
      this.offered = new ArrayList<String>();
      for (Node node : offered)
         this.offered.add(node.getID());
      this.offered = Collections.unmodifiableCollection(this.offered);
      
      this.requested = new ArrayList<String>();
      for (Node node : requested)
         this.requested.add(node.getID());
      this.requested = Collections.unmodifiableCollection(this.requested);
      
      this.cash = cash;
   }
   
   public int getFrom()
   {
      return from;
   }
   
   public int getTo()
   {
      return to;
   }
   
   public Collection<String> getOffered()
   {
      return offered;
   }

   public Collection<String> getRequested()
   {
      return requested;
   }
   
   public int getCash()
   {
      return cash;
   }

   public String toString()
   {
      StringBuilder rtn = new StringBuilder();

      rtn.append(from);
      rtn.append(" <-> ");
      rtn.append(to);
      rtn.append('\n');
      
      if (!offered.isEmpty())
      {
         rtn.append("Offered:");
         for (String offer : offered)
         {
            rtn.append(' ');
            rtn.append(offer);
         }
         rtn.append('\n');
      }
      
      if (!requested.isEmpty())
      {
         rtn.append("Requested:");
         for (String request : requested)
         {
            rtn.append(' ');
            rtn.append(request);
         }
         rtn.append('\n');
      }

      if (cash != 0)
      {
         rtn.append("Cash: ");
         rtn.append(cash);
         rtn.append('\n');
      }
      
      return rtn.toString();
   }
}
