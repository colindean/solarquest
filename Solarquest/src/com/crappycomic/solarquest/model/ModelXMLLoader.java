// Solarquest
// Copyright (C) 2011 Colin Bartolome
// Licensed under the GPL. See LICENSE.txt for details.

package com.crappycomic.solarquest.model;

import java.io.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

// TODO
public class ModelXMLLoader extends DefaultHandler
{
   /**
    * A {@link Pair} representing the friendly name and ID of an XML file that can be loaded.
    */
   public static class XMLOption extends Pair<String, String>
   {
      private static final long serialVersionUID = 0;

      private XMLOption(String id, String name)
      {
         super(id, name);
      }
      
      public String getID()
      {
         return getFirst();
      }
      
      public String getName()
      {
         return getSecond();
      }
      
      @Override
      public String toString()
      {
         return getName();
      }
   }
   
   /** Enumerates the possible states the loader can be in over the course of loading a file. */
   private enum State
   {
      RULE_SET,
      RULE,
      RULE_NAME,
      RULE_VALUE,
      GAME,
      BOARD,
      DEFAULT_RULE_SET,
      DEFAULT_VIEW,
      NODE,
      NODE_TYPE,
      NODE_DEST,
      START_NODE,
      DEED_PRICE,
      DEED_GROUP,
      DEED_RENT,
      DEED_FUEL,
      ACTION,
      ACTION_TYPE,
      ACTION_VALUE,
      CARD,
      UNKNOWN
   }
   
   private static final String FORMAT_RULESET_FILENAME = "/xml/ruleSets/%s.xml";
   
   private static final String FORMAT_GAME_FILENAME = "/xml/games/%s.xml";

   private static final String ELEMENT_RULE_SET = "ruleSet";
   
   private static final String ELEMENT_RULE = "rule";
   
   private static final String ELEMENT_RULE_NAME = "ruleName";
   
   private static final String ELEMENT_RULE_VALUE = "ruleValue";
   
   private static final String ELEMENT_GAME = "game";
   
   private static final String ELEMENT_BOARD = "board";

   private static final String ELEMENT_DEFAULT_RULE_SET = "defaultRuleSet";
   
   private static final String ELEMENT_DEFAULT_VIEW = "defaultView";
   
   private static final String ELEMENT_NODE = "node";

   private static final String ELEMENT_NODE_TYPE = "nodeType";

   private static final String ELEMENT_NODE_DEST = "nodeDest";

   private static final String ELEMENT_START_NODE = "startNode";
   
   private static final String ELEMENT_DEED_PRICE = "deedPrice";
   
   private static final String ELEMENT_DEED_GROUP = "deedGroup";
   
   private static final String ELEMENT_DEED_RENT = "deedRent";
   
   private static final String ELEMENT_DEED_FUEL = "deedFuel";
   
   private static final String ELEMENT_ACTION = "action";
   
   private static final String ELEMENT_ACTION_TYPE = "actionType";
   
   private static final String ELEMENT_ACTION_VALUE = "actionValue";
   
   private static final String ELEMENT_CARD = "card";

   private static final String ATTRIBUTE_ID = "id";
   
   private static final String ATTRIBUTE_COUNT = "count";
   
   private static final String BUILT_IN_DELIMITER = "=";
   
   private static final String PATH_BUILT_IN_GAMES = "/xml/games/builtIn";
   
   private static final String PATH_BUILT_IN_RULE_SETS = "/xml/ruleSets/builtIn";

   public static void main(String[] args) throws SAXException, IOException
   {
      ModelXMLLoader xmlLoader = new ModelXMLLoader();
      ServerModel model = xmlLoader.loadGame(args[0]);

      System.out.println(model.getBoard());

      xmlLoader.validate();
   }
   
   private static XMLOption[] getAvailableOptions(String path)
   {
      List<XMLOption> options = new ArrayList<XMLOption>();
      
      try
      {
         BufferedReader reader = new BufferedReader(new InputStreamReader(ModelXMLLoader.class.getResourceAsStream(path)));
         String line;
         
         while ((line = reader.readLine()) != null)
         {
            if (line.startsWith("#"))
               continue;
            
            String[] split = line.split(BUILT_IN_DELIMITER);
            
            options.add(new XMLOption(split[0], split[1]));
         }
      }
      catch (Exception e)
      {
         System.err.println("Error loading list of available games: " + e.toString());
      }
      
      return options.toArray(new XMLOption[0]);
   }
   
   public static XMLOption[] getAvailableGames()
   {
      return getAvailableOptions(PATH_BUILT_IN_GAMES);
   }
   
   public static XMLOption[] getAvailableRuleSets()
   {
      return getAvailableOptions(PATH_BUILT_IN_RULE_SETS);
   }

   public RuleSet loadRuleSet(String filename) throws SAXException, IOException
   {
      String path = String.format(FORMAT_RULESET_FILENAME, filename);
      XMLReader reader = XMLReaderFactory.createXMLReader();
      InputSource source = new InputSource(getClass().getResourceAsStream(path));
      
      source.setSystemId(getClass().getResource(path).toString());
      
      reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      reader.setContentHandler(this);
      reader.parse(source);

      return ruleSet;
   }
   
   public ServerModel loadGame(String filename) throws SAXException, IOException
   {
      String path = String.format(FORMAT_GAME_FILENAME, filename);
      XMLReader reader = XMLReaderFactory.createXMLReader();
      InputSource source = new InputSource(getClass().getResourceAsStream(path));
      
      source.setSystemId(getClass().getResource(path).toString());
      
      reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      reader.setContentHandler(this);
      reader.parse(source);

      return model;
   }

   private Stack<State> stateStack = new Stack<State>();

   private RuleSet ruleSet;
   
   private StringBuilder ruleNameBuilder;
   
   private StringBuilder ruleValueBuilder;
   
   private ServerModel model;
   
   private Board board;

   private StringBuilder defaultRuleSetBuilder;
   
   private StringBuilder defaultViewBuilder;
   
   private Node node;

   private StringBuilder nodeTypeBuilder;
   
   private StringBuilder nodeDestinationIDBuilder;

   private Map<Node, List<String>> nodeDestinationIDs = new HashMap<Node, List<String>>();
   
   private StringBuilder deedPriceBuilder;
   
   private StringBuilder deedGroupBuilder;
   
   private StringBuilder deedRentBuilder;
   
   private StringBuilder deedFuelBuilder;
   
   private StringBuilder actionTypeBuilder;
   
   private StringBuilder actionValueBuilder;
   
   private Card card;
   
   private Map<String, Integer> deedGroupCounts = new HashMap<String, Integer>();
   
   @Override
   public void startElement(String uri, String localName, String name, Attributes attributes)
   {
      if (localName.equals(ELEMENT_RULE_SET))
      {
         stateStack.push(State.RULE_SET);
         
         ruleSet = new RuleSet();
         ruleSet.setID(attributes.getValue(ATTRIBUTE_ID));
      }
      else if (localName.equals(ELEMENT_RULE))
      {
         stateStack.push(State.RULE);
      }
      else if (localName.equals(ELEMENT_RULE_NAME))
      {
         stateStack.push(State.RULE_NAME);
         
         ruleNameBuilder = new StringBuilder();
      }
      else if (localName.equals(ELEMENT_RULE_VALUE))
      {
         stateStack.push(State.RULE_VALUE);
         
         ruleValueBuilder = new StringBuilder();
      }
      else if (localName.equals(ELEMENT_GAME))
      {
         stateStack.push(State.GAME);
         
         model = new ServerModel();
         model.setID(attributes.getValue(ATTRIBUTE_ID));
      }
      else if (localName.equals(ELEMENT_BOARD))
      {
         stateStack.push(State.BOARD);

         model.setBoard(board = new Board());
      }
      else if (localName.equals(ELEMENT_DEFAULT_RULE_SET))
      {
         stateStack.push(State.DEFAULT_RULE_SET);
         
         defaultRuleSetBuilder = new StringBuilder();
      }
      else if (localName.equals(ELEMENT_DEFAULT_VIEW))
      {
         stateStack.push(State.DEFAULT_VIEW);
         
         defaultViewBuilder = new StringBuilder();
      }
      else if (localName.equals(ELEMENT_NODE))
      {
         stateStack.push(State.NODE);

         node = new Node();
         node.setID(attributes.getValue(ATTRIBUTE_ID));
      }
      else if (localName.equals(ELEMENT_NODE_TYPE))
      {
         stateStack.push(State.NODE_TYPE);

         nodeTypeBuilder = new StringBuilder();
      }
      else if (localName.equals(ELEMENT_NODE_DEST))
      {
         stateStack.push(State.NODE_DEST);

         nodeDestinationIDBuilder = new StringBuilder();
      }
      else if (localName.equals(ELEMENT_START_NODE))
      {
         stateStack.push(State.START_NODE);

         node.setStartNode(true);
         board.setStartNode(node);
      }
      else if (localName.equals(ELEMENT_DEED_PRICE))
      {
         stateStack.push(State.DEED_PRICE);
         
         deedPriceBuilder = new StringBuilder();
      }
      else if (localName.equals(ELEMENT_DEED_GROUP))
      {
         stateStack.push(State.DEED_GROUP);
         
         deedGroupBuilder = new StringBuilder();
      }
      else if (localName.equals(ELEMENT_DEED_RENT))
      {
         stateStack.push(State.DEED_RENT);
         
         deedRentBuilder = new StringBuilder();
      }
      else if (localName.equals(ELEMENT_DEED_FUEL))
      {
         stateStack.push(State.DEED_FUEL);
         
         deedFuelBuilder = new StringBuilder();
      }
      else if (localName.equals(ELEMENT_ACTION))
      {
         stateStack.push(State.ACTION);
      }
      else if (localName.equals(ELEMENT_ACTION_TYPE))
      {
         stateStack.push(State.ACTION_TYPE);
         
         actionTypeBuilder = new StringBuilder();
      }
      else if (localName.equals(ELEMENT_ACTION_VALUE))
      {
         stateStack.push(State.ACTION_VALUE);
         
         actionValueBuilder = new StringBuilder();
      }
      else if (localName.equals(ELEMENT_CARD))
      {
         stateStack.push(State.CARD);
         
         card = new Card();
         card.setID(attributes.getValue(ATTRIBUTE_ID));
         
         int count;
         
         if (attributes.getValue(ATTRIBUTE_COUNT) != null)
            count = Integer.parseInt(attributes.getValue(ATTRIBUTE_COUNT));
         else
            count = 1;
         
         for (int i = 0; i < count; i++)
            model.addCard(card);
      }
      else
      {
         stateStack.push(State.UNKNOWN);
      }
   }

   @Override
   public void characters(char[] ch, int start, int length)
   {
      State currentState = stateStack.peek();
      StringBuilder builder;

      if (currentState == State.RULE_NAME)
      {
         builder = ruleNameBuilder;
      }
      else if (currentState == State.RULE_VALUE)
      {
         builder = ruleValueBuilder;
      }
      else if (currentState == State.DEFAULT_RULE_SET)
      {
         builder = defaultRuleSetBuilder;
      }
      else if (currentState == State.DEFAULT_VIEW)
      {
         builder = defaultViewBuilder;
      }
      else if (currentState == State.NODE_TYPE)
      {
         builder = nodeTypeBuilder;
      }
      else if (currentState == State.NODE_DEST)
      {
         builder = nodeDestinationIDBuilder;
      }
      else if (currentState == State.DEED_PRICE)
      {
         builder = deedPriceBuilder;
      }
      else if (currentState == State.DEED_GROUP)
      {
         builder = deedGroupBuilder;
      }
      else if (currentState == State.DEED_RENT)
      {
         builder = deedRentBuilder;
      }
      else if (currentState == State.DEED_FUEL)
      {
         builder = deedFuelBuilder;
      }
      else if (currentState == State.ACTION_TYPE)
      {
         builder = actionTypeBuilder;
      }
      else if (currentState == State.ACTION_VALUE)
      {
         builder = actionValueBuilder;
      }
      else
      {
         builder = null;
      }
      
      if (builder != null)
         builder.append(ch, start, length);
   }

   @Override
   public void endElement(String uri, String localName, String name)
   {
      State lastState = stateStack.pop();

      if (lastState == State.RULE)
      {
         ruleSet.setValue(RuleSet.getRule(ruleNameBuilder.toString()), ruleValueBuilder.toString());
      }
      else if (lastState == State.BOARD)
      {
         for (Map.Entry<Node, List<String>> entry : nodeDestinationIDs.entrySet())
            for (String destinationID : entry.getValue())
               entry.getKey().addDestination(board.getNode(destinationID));
         
         board.initializeAdjacencyMatrix();
      }
      else if (lastState == State.DEFAULT_RULE_SET)
      {
         model.setDefaultRuleSet(defaultRuleSetBuilder.toString());
      }
      else if (lastState == State.DEFAULT_VIEW)
      {
         model.setDefaultView(defaultViewBuilder.toString());
      }
      else if (lastState == State.NODE)
      {
         board.addNode(node);
      }
      else if (lastState == State.NODE_TYPE)
      {
         node.setType(Node.Type.valueOf(nodeTypeBuilder.toString().toUpperCase()));
      }
      else if (lastState == State.NODE_DEST)
      {
         List<String> destinations = nodeDestinationIDs.get(node);
         
         if (destinations == null)
         {
            nodeDestinationIDs.put(node, destinations = new ArrayList<String>());
         }
         
         destinations.add(nodeDestinationIDBuilder.toString());
      }
      else if (lastState == State.DEED_PRICE)
      {
         node.setPrice(Integer.parseInt(deedPriceBuilder.toString()));
      }
      else if (lastState == State.DEED_GROUP)
      {
         node.setGroup(deedGroupBuilder.toString());
         
         Integer deedGroupCount = deedGroupCounts.get(deedGroupBuilder.toString());
         
         if (deedGroupCount == null)
            deedGroupCount = new Integer(0);
         
         deedGroupCounts.put(deedGroupBuilder.toString(), deedGroupCount + 1);         
      }
      else if (lastState == State.DEED_RENT)
      {
         node.addRent(Integer.parseInt(deedRentBuilder.toString()));
      }
      else if (lastState == State.DEED_FUEL)
      {
         node.addFuel(Integer.parseInt(deedFuelBuilder.toString()));
      }
      else if (lastState == State.ACTION)
      {
         Action action = new Action(Action.Type.valueOf(actionTypeBuilder.toString().toUpperCase()), actionValueBuilder.toString());
         
         if (stateStack.peek() == State.NODE)
         {
            node.addAction(action);
         }
         else if (stateStack.peek() == State.CARD)
         {
            card.addAction(action);
         }
      }
   }

   private void validate()
   {
      validateDestinations();
      validateDeeds();
      validateConnectivity();
   }

   private void validateDestinations()
   {
      boolean noDeadEnds = true;
      boolean noNullDestinations = true;

      for (Node currentNode : board.getNodes())
      {
         List<Node> destinations = currentNode.getDestinations();

         if (destinations.isEmpty())
            noDeadEnds = false;

         for (Node destination : destinations)
         {
            if (destination == null)
            {
               System.out.println("Null destination: " + currentNode.getID());
               noNullDestinations = false;
            }
         }
      }

      System.out
         .println("Every node has at least one destination: " + (noDeadEnds ? "PASS" : "FAIL"));
      System.out.println("Every destination ID is a valid node: " + (noNullDestinations ? "PASS" : "FAIL"));
   }

   private void validateDeeds()
   {
      boolean allDeedsValid = true;
      
      for (Node currentNode : board.getNodes())
      {
         Integer deedGroupCount = deedGroupCounts.get(currentNode.getGroup());

         if (deedGroupCount == null)
            continue;
         
         List<Integer> rents = currentNode.getRents();
         List<Integer> fuels = currentNode.getFuels();
         
         if (deedGroupCount != rents.size())
         {
            System.out.println(currentNode.getID() + " rents size of " + rents.size() + " does not match group size of " + deedGroupCount);
            allDeedsValid = false;
         }
         
         if (fuels != null && deedGroupCount != fuels.size())
         {
            System.out.println(currentNode.getID() + " fuels size of " + fuels.size() + " does not match group size of " + deedGroupCount);
            allDeedsValid = false;
         }
      }
      
      System.out.println("All deed rent/fuel counts match group sizes: " + (allDeedsValid ? "PASS" : "FAIL"));
   }

   private void validateConnectivity()
   {
      boolean startNodeExists = board.getStartNode() != null;

      System.out.println("Start node exists: " + (startNodeExists ? "PASS" : "FAIL"));

      if (!startNodeExists)
         return;

      boolean startNodeReachable = false;

      for (Node currentNode : board.getNodes())
      {
         for (Node destination : currentNode.getDestinations())
         {
            if (destination.isStartNode())
            {
               startNodeReachable = true;
               break;
            }
         }
      }

      System.out.println("Start node is reachable: " + (startNodeReachable ? "PASS" : "FAIL"));

      Set<Node> unvisitedNodes = new HashSet<Node>(board.getNodes());

      visitNodes(board.getStartNode(), unvisitedNodes);

      System.out.println("All nodes are reachable from the start: "
         + (unvisitedNodes.isEmpty() ? "PASS" : "FAIL (" + unvisitedNodes.size() + ")"));
   }
   
   private static void visitNodes(Node node, Set<Node> unvisitedNodes)
   {
      Collection<Node> destinations = node.getDestinations();

      unvisitedNodes.remove(node);

      for (Node destination : destinations)
      {
         if (unvisitedNodes.contains(destination))
            visitNodes(destination, unvisitedNodes);
      }
   }
}
