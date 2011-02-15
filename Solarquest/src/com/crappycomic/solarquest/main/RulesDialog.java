package com.crappycomic.solarquest.main;

import static com.crappycomic.solarquest.main.CreateGameDialog.GAP;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import com.crappycomic.solarquest.model.RuleSet;
import com.crappycomic.solarquest.model.RuleSet.*;

@SuppressWarnings("serial")
class RulesDialog extends JDialog
{
   private static class IntegerRule extends JSpinner
   {
      private IntegerRule(Integer value)
      {
         super(new SpinnerNumberModel(value.intValue(), 0, Integer.MAX_VALUE, 1));
      }
   }
   
   private static class BooleanRule extends JComboBox
   {
      private BooleanRule(Boolean value)
      {
         super(new Object[] {false, true});
         
         setSelectedItem(value);
      }
   }
   
   private static class TransactionAvailabilityRule extends JComboBox
   {
      private TransactionAvailabilityRule(RuleSet.TransactionAvailability value)
      {
         super(RuleSet.TransactionAvailability.values());
         
         setSelectedItem(value);
      }
   }
   
   private static class RedShiftRollTypeRule extends JComboBox
   {
      private RedShiftRollTypeRule(RuleSet.RedShiftRoll value)
      {
         super(RuleSet.RedShiftRoll.values());
         
         setSelectedItem(value);
      }
   }
   
   private Map<Rule<?>, Object> rules;
   
   private boolean ok;
   
   RulesDialog(JFrame owner, RuleSet ruleSet)
   {
      super(owner, "Edit Rules", true);

      rules = new HashMap<Rule<?>, Object>();
      
      JPanel panel = new JPanel();
      Box box;
      JButton button;
      
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      
      for (Rule<?> rule : ruleSet.getRules())
      {
         box = Box.createHorizontalBox();
         
         box.add(Box.createHorizontalGlue());
         box.add(new JLabel(rule.getName()));
         box.add(Box.createHorizontalStrut(GAP));
         
         JComponent component = null;
         
         switch (rule.getType())
         {
            case INTEGER:
               box.add(component = new IntegerRule((Integer)ruleSet.getValue(rule)));
               break;
            case BOOLEAN:
               box.add(component = new BooleanRule((Boolean)ruleSet.getValue(rule)));
               break;
            case TRANSACTION_AVAILABILITY:
               box.add(component = new TransactionAvailabilityRule((TransactionAvailability)ruleSet.getValue(rule)));
               break;
            case RED_SHIFT_ROLL_TYPE:
               box.add(component = new RedShiftRollTypeRule((RedShiftRoll)ruleSet.getValue(rule)));
               break;
         }
         
         rules.put(rule, component);
         
         panel.add(box);
         panel.add(Box.createVerticalStrut(GAP));
      }
      
      box = Box.createHorizontalBox();
      box.add(Box.createHorizontalGlue());
      box.add(button = new JButton("Use Default Values"));
      
      button.addActionListener(new ActionListener()
      {
         @Override
         public void actionPerformed(ActionEvent e)
         {
            setVisible(false);
         }
      });
      
      box.add(Box.createHorizontalStrut(GAP));
      box.add(button = new JButton("Use These Values"));
      
      button.addActionListener(new ActionListener()
      {
         @Override
         public void actionPerformed(ActionEvent e)
         {
            ok = true;
            setVisible(false);
         }
      });
      
      box.add(Box.createHorizontalGlue());
      
      panel.add(box);
      
      panel.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
      add(panel);
      pack();
      setLocationRelativeTo(owner);
      setVisible(true);
   }
   
   boolean isOk()
   {
      return ok;
   }
   
   RuleSet getRuleSet()
   {
      if (!ok)
         return null;
      
      RuleSet ruleSet = new RuleSet();
      
      for (Map.Entry<Rule<?>, Object> entry : rules.entrySet())
      {
         switch (entry.getKey().getType())
         {
            case INTEGER:
               ruleSet.setValue(entry.getKey(), ((JSpinner)entry.getValue()).getValue().toString());
               break;
            case BOOLEAN:
            case TRANSACTION_AVAILABILITY:
            case RED_SHIFT_ROLL_TYPE:
               ruleSet.setValue(entry.getKey(), ((JComboBox)entry.getValue()).getSelectedItem().toString());
               break;
         }
      }
      
      return ruleSet;
   }
   
   @Override
   public void setVisible(boolean visible)
   {
      if (visible)
      {
         ok = false;
      }
      
      super.setVisible(visible);
   }
}
