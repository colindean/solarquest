package com.crappycomic.solarquest.view;

import java.io.IOException;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class ViewXMLLoader extends DefaultHandler
{
   private static enum State
   {
      BOARD_IMAGE,
      NODE_VIEW,
      DISPLAY_NAME,
      COORDS,
      DEED_GROUP_VIEW,
      DISPLAY_COLOR,
      CARD_VIEW,
      DESCRIPTION,
      UNKNOWN
   }
   
   private static final String FORMAT_FILENAME = "/xml/views/%s.xml";

   private static final String ELEMENT_BOARD_IMAGE = "boardImage";
   
   private static final String ELEMENT_NODE_VIEW = "nodeView";
   
   private static final String ELEMENT_DISPLAY_NAME = "displayName";
   
   private static final String ELEMENT_COORDS = "coords";
   
   private static final String ELEMENT_DEED_GROUP_VIEW = "deedGroupView";

   private static final String ELEMENT_DISPLAY_COLOR = "displayColor";
   
   private static final String ELEMENT_CARD_VIEW = "cardView";

   private static final String ATTRIBUTE_FOR = "for";

   public static void main(String[] args) throws SAXException, IOException
   {
      ViewXMLLoader xmlLoader = new ViewXMLLoader();
      
      xmlLoader.load(args[0], null);
   }

   public void load(String filename, GraphicView view) throws SAXException, IOException
   {
      String path = String.format(FORMAT_FILENAME, filename);
      XMLReader reader = XMLReaderFactory.createXMLReader();
      InputSource source = new InputSource(getClass().getResourceAsStream(path));
      
      System.out.println("Path: " + path);
      
      source.setSystemId(getClass().getResource(path).toString());
      
      reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      reader.setContentHandler(this);
      reader.parse(source);

      if (view != null)
      {
         view.setBoardImage(boardImageBuilder.toString());
         view.setNodeViews(nodeViews);
         view.setDeedGroupViews(deedGroupViews);
         view.setCardViews(cardViews);
      }
   }
   
   private Stack<State> stateStack = new Stack<State>();

   private StringBuilder boardImageBuilder;
   
   private Map<String, NodeView> nodeViews = new HashMap<String, NodeView>();
   
   private NodeView nodeView;
   
   private StringBuilder displayNameBuilder;
   
   private StringBuilder coordsBuilder;
   
   private Map<String, DeedGroupView> deedGroupViews = new HashMap<String, DeedGroupView>();
   
   private DeedGroupView deedGroupView;
   
   private StringBuilder displayColorBuilder;
   
   private Map<String, CardView> cardViews = new HashMap<String, CardView>();
   
   private CardView cardView;
   
   @Override
   public void startElement(String uri, String localName, String name, Attributes attributes)
   {
      if (localName.equals(ELEMENT_BOARD_IMAGE))
      {
         stateStack.push(State.BOARD_IMAGE);
         
         boardImageBuilder = new StringBuilder();
      }
      else if (localName.equals(ELEMENT_NODE_VIEW))
      {
         stateStack.push(State.NODE_VIEW);
         
         nodeViews.put(attributes.getValue(ATTRIBUTE_FOR), nodeView = new NodeView());
      }
      else if (localName.equals(ELEMENT_DISPLAY_NAME))
      {
         stateStack.push(State.DISPLAY_NAME);
         
         displayNameBuilder = new StringBuilder();
      }
      else if (localName.equals(ELEMENT_COORDS))
      {
         stateStack.push(State.COORDS);
         
         coordsBuilder = new StringBuilder();
      }
      else if (localName.equals(ELEMENT_DEED_GROUP_VIEW))
      {
         stateStack.push(State.DEED_GROUP_VIEW);
         
         deedGroupViews.put(attributes.getValue(ATTRIBUTE_FOR), deedGroupView = new DeedGroupView());
      }
      else if (localName.equals(ELEMENT_DISPLAY_COLOR))
      {
         stateStack.push(State.DISPLAY_COLOR);

         displayColorBuilder = new StringBuilder();
      }
      else if (localName.equals(ELEMENT_CARD_VIEW))
      {
         stateStack.push(State.CARD_VIEW);
         
         cardViews.put(attributes.getValue(ATTRIBUTE_FOR), cardView = new CardView());
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

      if (currentState == State.BOARD_IMAGE)
      {
         builder = boardImageBuilder;
      }
      else if (currentState == State.DISPLAY_NAME)
      {
         builder = displayNameBuilder;
      }
      else if (currentState == State.COORDS)
      {
         builder = coordsBuilder;
      }         
      else if (currentState == State.DISPLAY_COLOR)
      {
         builder = displayColorBuilder;
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

      if (lastState == State.NODE_VIEW)
      {
         nodeView.setDisplayName(displayNameBuilder.toString());
         nodeView.setCoords(coordsBuilder.toString());
      }
      else if (lastState == State.DEED_GROUP_VIEW)
      {
         deedGroupView.setDisplayName(displayNameBuilder.toString());
         deedGroupView.setDisplayColor(displayColorBuilder.toString());
      }
      else if (lastState == State.CARD_VIEW)
      {
         cardView.setDisplayName(displayNameBuilder.toString());
      }
   }
}
