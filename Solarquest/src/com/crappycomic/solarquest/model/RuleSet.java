// Solarquest
// Copyright (C) 2011 Colin Bartolome
// Licensed under the GPL. See LICENSE.txt for details.

package com.crappycomic.solarquest.model;

import java.io.Serializable;
import java.util.*;

// This is kind of annoying
public class RuleSet implements Serializable
{
   private static final long serialVersionUID = 8303657986331898582L;

   public static class Rule<T> implements Serializable
   {
      private static final long serialVersionUID = -656194308203540151L;

      private String name;
      
      private Class<?> type;
      
      private Rule(String name, Class<?> type)
      {
         this.name = name;
         this.type = type;
      }
      
      @Override
      public int hashCode()
      {
         return name.hashCode();
      }
      
      @Override
      public boolean equals(Object other)
      {
         return (other instanceof Rule<?>) && name.equals(((Rule<?>)other).name);
      }
   }
   
   public static enum RedShiftRoll
   {
      DOUBLES,
      MAX_ROLL,
      THIRTEEN
   }
   
   public static enum TransactionAvailability
   {
      NOWHERE,
      STATIONS,
      EVERYWHERE
   }

   public static final Rule<Integer> INITIAL_CASH = new Rule<Integer>("initial_cash", Integer.class);
   public static final Rule<Integer> INITIAL_FUEL = new Rule<Integer>("initial_fuel", Integer.class);
   public static final Rule<Integer> INITIAL_FUEL_STATIONS = new Rule<Integer>("initial_fuel_stations", Integer.class);
   public static final Rule<Integer> TOTAL_FUEL_STATIONS = new Rule<Integer>("total_fuel_stations", Integer.class);
   public static final Rule<Integer> PASS_START_CASH = new Rule<Integer>("pass_start_cash", Integer.class);
   public static final Rule<Integer> LAND_ON_START_CASH = new Rule<Integer>("land_on_start_cash", Integer.class);
   public static final Rule<TransactionAvailability> FUEL_STATION_PURCHASE_AVAILABILITY = new Rule<TransactionAvailability>("fuel_station_purchase_availability", TransactionAvailability.class);
   public static final Rule<TransactionAvailability> FUEL_STATION_BUYBACK_AVAILABILITY = new Rule<TransactionAvailability>("fuel_station_buyback_availability", TransactionAvailability.class);
   public static final Rule<TransactionAvailability> NODE_BUYBACK_AVAILABILITY = new Rule<TransactionAvailability>("node_buyback_availability", TransactionAvailability.class);
   public static final Rule<Integer> FUEL_STATION_PRICE = new Rule<Integer>("fuel_station_price", Integer.class);
   public static final Rule<Integer> FUEL_PRICE_ON_START = new Rule<Integer>("fuel_price_on_start", Integer.class);
   public static final Rule<Integer> MAXIMUM_FUEL = new Rule<Integer>("maximum_fuel", Integer.class);
   public static final Rule<Integer> DIE_PIPS = new Rule<Integer>("die_pips", Integer.class);
   public static final Rule<Integer> LOW_FUEL = new Rule<Integer>("low_fuel", Integer.class);
   public static final Rule<Integer> MINIMUM_FUEL = new Rule<Integer>("minimum_fuel", Integer.class);
   public static final Rule<Boolean> LASER_BATTLES_ALLOWED = new Rule<Boolean>("laser_battles_allowed", Boolean.class);
   public static final Rule<Integer> LASER_BATTLE_FUEL_COST = new Rule<Integer>("laser_battle_fuel_cost", Integer.class);
   public static final Rule<Integer> LASER_BATTLE_MAXIMUM_DISTANCE = new Rule<Integer>("laser_battle_maximum_distance", Integer.class);
   public static final Rule<RedShiftRoll> RED_SHIFT_ROLL = new Rule<RedShiftRoll>("red_shift_roll", RedShiftRoll.class);
   public static final Rule<Boolean> BYPASS_ALLOWED = new Rule<Boolean>("bypass_allowed", Boolean.class);
   public static final Rule<Boolean> FUEL_AVAILABLE_ON_UNOWNED_NODE = new Rule<Boolean>("fuel_available_on_unowned_node", Boolean.class); // false for Apollo 13
   public static final Rule<Boolean> MUST_REFUEL_AT_DEAD_END = new Rule<Boolean>("must_refuel_at_dead_end", Boolean.class); // maybe true for 1985 but not 1988? (Sinope Rule)

   private static final Map<String, Rule<?>> RULE_NAME_MAP;
   static
   {
      Map<String, Rule<?>> ruleNameMap = new LinkedHashMap<String, Rule<?>>();
      
      ruleNameMap.put(INITIAL_CASH.name, INITIAL_CASH);
      ruleNameMap.put(INITIAL_FUEL.name, INITIAL_FUEL);
      ruleNameMap.put(INITIAL_FUEL_STATIONS.name, INITIAL_FUEL_STATIONS);
      ruleNameMap.put(TOTAL_FUEL_STATIONS.name, TOTAL_FUEL_STATIONS);
      ruleNameMap.put(PASS_START_CASH.name, PASS_START_CASH);
      ruleNameMap.put(LAND_ON_START_CASH.name, LAND_ON_START_CASH);
      ruleNameMap.put(FUEL_STATION_PURCHASE_AVAILABILITY.name, FUEL_STATION_PURCHASE_AVAILABILITY);
      ruleNameMap.put(FUEL_STATION_BUYBACK_AVAILABILITY.name, FUEL_STATION_BUYBACK_AVAILABILITY);
      ruleNameMap.put(NODE_BUYBACK_AVAILABILITY.name, NODE_BUYBACK_AVAILABILITY);
      ruleNameMap.put(FUEL_STATION_PRICE.name, FUEL_STATION_PRICE);
      ruleNameMap.put(FUEL_PRICE_ON_START.name, FUEL_PRICE_ON_START);
      ruleNameMap.put(MAXIMUM_FUEL.name, MAXIMUM_FUEL);
      ruleNameMap.put(DIE_PIPS.name, DIE_PIPS);
      ruleNameMap.put(LOW_FUEL.name, LOW_FUEL);
      ruleNameMap.put(MINIMUM_FUEL.name, MINIMUM_FUEL);
      ruleNameMap.put(LASER_BATTLES_ALLOWED.name, LASER_BATTLES_ALLOWED);
      ruleNameMap.put(LASER_BATTLE_FUEL_COST.name, LASER_BATTLE_FUEL_COST);
      ruleNameMap.put(LASER_BATTLE_MAXIMUM_DISTANCE.name, LASER_BATTLE_MAXIMUM_DISTANCE);
      ruleNameMap.put(RED_SHIFT_ROLL.name, RED_SHIFT_ROLL);
      ruleNameMap.put(BYPASS_ALLOWED.name, BYPASS_ALLOWED);
      ruleNameMap.put(FUEL_AVAILABLE_ON_UNOWNED_NODE.name, FUEL_AVAILABLE_ON_UNOWNED_NODE);
      ruleNameMap.put(MUST_REFUEL_AT_DEAD_END.name, MUST_REFUEL_AT_DEAD_END);
      
      RULE_NAME_MAP = Collections.unmodifiableMap(ruleNameMap);
   }
   
   public static Rule<?> getRule(String name)
   {
      return RULE_NAME_MAP.get(name);
   }
   
   private String id;
   
   private Map<Rule<?>, Object> ruleValueMap = new HashMap<Rule<?>, Object>();
   
   void setID(String id)
   {
      this.id = id;
   }
   
   String getID()
   {
      return id;
   }
   
   public void setValue(Rule<?> rule, String value)
   {
      if (rule.type == Integer.class)
         ruleValueMap.put(rule, Integer.parseInt(value));
      else if (rule.type == Boolean.class)
         ruleValueMap.put(rule, Boolean.parseBoolean(value));
      else if (rule.type == RedShiftRoll.class)
         ruleValueMap.put(rule, RedShiftRoll.valueOf(value.toUpperCase()));
      else if (rule.type == TransactionAvailability.class)
         ruleValueMap.put(rule, TransactionAvailability.valueOf(value.toUpperCase()));
   }
   
   @SuppressWarnings("unchecked")
   public <T> T getValue(Rule<T> rule)
   {
      return (T)ruleValueMap.get(rule);
   }

   public static boolean isRedShift(RedShiftRoll redShiftRoll, int diePips, int die1, int die2)
   {
      switch (redShiftRoll)
      {
         case DOUBLES:
            return die1 == die2;
         case MAX_ROLL:
            return die1 == diePips && die2 == diePips;
         case THIRTEEN:
            return (die1 == 1 && die2 == 3) || (die1 == 3 && die2 == 1);
      }
      
      return false;
   }
   
   public static boolean isTransactionAvailable(TransactionAvailability availability, Node node)
   {
      switch (availability)
      {
         case NOWHERE:
            return false;
         case STATIONS:
            return node.getType() == Node.Type.STATION;
         case EVERYWHERE:
            return true;
      }
      
      return false;
   }
   
   public int size()
   {
      return ruleValueMap.size();
   }
}
