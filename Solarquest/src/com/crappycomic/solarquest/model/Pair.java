package com.crappycomic.solarquest.model;

import java.io.Serializable;

/** A simple, two-element tuple. */
public class Pair<S, T> implements Serializable
{
   // This value will probably not need to change any time soon.
   private static final long serialVersionUID = 1643915154460468860L;

   private S first;

   private T second;

   public Pair(S first, T second)
   {
      this.first = first;
      this.second = second;
   }

   public S getFirst()
   {
      return first;
   }

   public T getSecond()
   {
      return second;
   }
}
