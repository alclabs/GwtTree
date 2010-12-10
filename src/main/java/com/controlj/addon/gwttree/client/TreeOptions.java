package com.controlj.addon.gwttree.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.*;

import java.util.*;

/**<!=========================================================================>
   This class is responsible for maintaining the set of tree options choosen by
   the user, and displays the dialog allowing those options to be changed.
<!==========================================================================>*/
public class TreeOptions
{
   private ChangedHandler handler;
   private State state = new State();

   public TreeOptions(ChangedHandler handler)
   {
      this.handler = handler;
   }

   public void showDialog()
   {
      VerticalPanel dialogContents = new VerticalPanel();
      dialogContents.setSpacing(4);

      // Add the tree type radio buttons
      RadioButton dynamicChoice = createTreeChoice("treeTypeGroup", "Dynamic Tree", false);
      RadioButton staticChoice = createTreeChoice("treeTypeGroup", "Static Tree", true);
      if (state.isStaticTree)
         staticChoice.setValue(true);
      else
         dynamicChoice.setValue(true);
      dialogContents.add(dynamicChoice);
      dialogContents.add(staticChoice);

      // Add filtering support
      dialogContents.add(new Label("Filter by trend name (leave empty to not filter)"));
      HorizontalPanel sourcesPanel = new HorizontalPanel();
      sourcesPanel.setSpacing(10);
      final ListBox sourceListBox = new ListBox(true);
      sourceListBox.setSize("10em", "5em");
      for (String sourceName : state.sourceNames)
         sourceListBox.addItem(sourceName);
      sourcesPanel.add(sourceListBox);
      VerticalPanel sourcesButtonPanel = new VerticalPanel();
      sourcesButtonPanel.add(new Button("Add...", new ClickHandler()
      {
         @Override public void onClick(ClickEvent clickEvent)
         {
            final TextBox textBox = new TextBox();
            Dialog.showInputDialog("Add Filter", textBox, new Dialog.Handler()
            {
               @Override public void setupButtons(final Button ok, final Button cancel)
               {
                  textBox.addKeyPressHandler(new KeyPressHandler()
                  {
                     @Override public void onKeyPress(KeyPressEvent keyPressEvent)
                     {
                        if (keyPressEvent.getCharCode() == (char)13)
                           ok.click();
                        else if (keyPressEvent.getCharCode() == (char)27)
                           cancel.click();
                     }
                  });
               }

               @Override public void dialogClosed(boolean wasCancelled)
               {
                  String name = textBox.getText().trim();
                  if (!wasCancelled && !name.isEmpty())
                  {
                     state.sourceNames.add(name);
                     sourceListBox.addItem(name);
                  }
               }
            });
            textBox.setFocus(true);
         }
      }));
      sourcesButtonPanel.add(new Button("Remove", new ClickHandler()
      {
         @Override public void onClick(ClickEvent clickEvent)
         {
            int count = sourceListBox.getItemCount();
            for (int i = count-1; i >= 0; i--)
               if (sourceListBox.isItemSelected(i))
               {
                  state.sourceNames.remove(sourceListBox.getItemText(i));
                  sourceListBox.removeItem(i);
               }
         }
      }));
      sourcesPanel.add(sourcesButtonPanel);
      dialogContents.add(sourcesPanel);

      final State originalState = state.copy();
      Dialog.showInputDialog("Tree Options", dialogContents, new Dialog.Handler()
      {
         @Override public void setupButtons(Button ok, Button cancel) { }
         @Override public void dialogClosed(boolean wasCancelled)
         {
            if (wasCancelled)
               state = originalState;
            else if (!state.equals(originalState))
               handler.optionsChanged(TreeOptions.this);
         }
      });
   }

   private RadioButton createTreeChoice(String groupId, String label, final boolean isStatic)
   {
      RadioButton dynamicChoice = new RadioButton(groupId, label);
      dynamicChoice.addValueChangeHandler(new ValueChangeHandler<Boolean>()
      {
         @Override public void onValueChange(ValueChangeEvent<Boolean> event)
         {
            if (event.getValue())
               state.isStaticTree = isStatic;
         }
      });
      return dynamicChoice;
   }

   private Widget createButtonPanel(DialogBox dialogBox, State originalState)
   {
      HorizontalPanel buttonPanel = new HorizontalPanel();
      buttonPanel.setSpacing(15);
      Button okButton = createButton("OK", dialogBox, false, originalState);
      buttonPanel.add(okButton);
      Button cancelButton = createButton("Cancel", dialogBox, true, null);
      buttonPanel.add(cancelButton);
      return buttonPanel;
   }

   private Button createButton(String label, final DialogBox dialogBox, final boolean isCancel, final State originalState)
   {
      return new Button(label, new ClickHandler()
      {
         public void onClick(ClickEvent event)
         {
            dialogBox.hide();
            if (isCancel)
               state = originalState;
            else if (!state.equals(originalState))
               handler.optionsChanged(TreeOptions.this);
         }
      });
   }

   public boolean isStaticTree() { return state.isStaticTree; }
   public Set<String> getSourceNames() { return Collections.unmodifiableSet(state.sourceNames); }
   public String[] getSourceNamesArray() { return state.sourceNames.toArray(new String[state.sourceNames.size()]); }

   private static class State
   {
      boolean isStaticTree;
      Set<String> sourceNames = new TreeSet<String>();

      State copy()
      {
         State state = new State();
         state.isStaticTree = isStaticTree;
         state.sourceNames.addAll(sourceNames);
         return state;
      }

      @Override
      public boolean equals(Object o)
      {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;

         State state = (State) o;

         if (isStaticTree != state.isStaticTree) return false;
         if (!sourceNames.equals(state.sourceNames)) return false;

         return true;
      }

      @Override
      public int hashCode()
      {
         int result = isStaticTree ? 1 : 0;
         result = 31 * result + sourceNames.hashCode();
         return result;
      }
   }

   public interface ChangedHandler
   {
      void optionsChanged(TreeOptions options);
   }
}
