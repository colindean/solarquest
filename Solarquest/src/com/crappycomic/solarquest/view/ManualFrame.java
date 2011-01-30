package com.crappycomic.solarquest.view;

import java.io.*;

import javax.swing.*;

import com.crappycomic.solarquest.model.RuleSet;
import com.crappycomic.solarquest.model.RuleSet.Rule;

public class ManualFrame extends JFrame
{
   private static final long serialVersionUID = 0;
   
   /** Entry point for testing manual text changes. */
   public static void main(String[] args)
   {
      ManualFrame frame = new ManualFrame(null, null);
      
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setVisible(true);
   }
   
   ManualFrame(String startNode, RuleSet ruleSet)
   {
      super("Instruction Manual");
      
      try
      {
         InputStream in = getClass().getResourceAsStream("manual.html");
         BufferedReader reader = new BufferedReader(new InputStreamReader(in));
         StringBuilder manual = new StringBuilder();
         String line;
         
         while ((line = reader.readLine()) != null)
         {
            manual.append(line);
            manual.append('\n');
         }
         
         String parsedManual = parseManual(manual.toString(), startNode, ruleSet);
         
         JTextPane textPane = new JTextPane();
         
         textPane.setEditable(false);
         textPane.setContentType("text/html");
         textPane.setText(parsedManual);
         textPane.setCaretPosition(0);
         
         JScrollPane scrollPane = new JScrollPane(textPane);
         
         setContentPane(scrollPane);
      }
      catch (Exception e)
      {
         JOptionPane.showMessageDialog(null, "Unable to load instruction manual: " + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
      }
      
      setSize(640, 480);
      setLocationRelativeTo(null);
   }
   
   private String parseManual(String manual, String startNode, RuleSet ruleSet)
   {
      String parsedManual = manual;
      
      parsedManual = parsedManual.replaceAll("\\{start_node\\}", startNode == null ? "Start" : startNode);
      
      if (ruleSet != null)
      {
         for (Rule<?> rule : ruleSet.getRules())
         {
            String ruleValue = ruleSet.getValueForManual(rule);
            
            parsedManual = parsedManual.replaceAll("\\{" + rule.getName() + "\\}", ruleValue);
         }
      }
      
      return parsedManual;
   }
}
