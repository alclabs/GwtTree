package com.controlj.addon.gwttree.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.DatePicker;

import java.util.Date;

/**<!=========================================================================>
   Main GWT Module, this constructs the main page and wires together the other
   main classes.
 <!==========================================================================>*/
public class MainPage implements EntryPoint
{
   private final GraphManager graphManager = new GraphManager();
   private final TreeManager treeManager = new TreeManager(graphManager);
   private final DatePicker picker = new DatePicker();

   private void setupUI()
   {
      // Add horizontal split
      HorizontalSplitPanel split = new HorizontalSplitPanel();
      split.setLeftWidget(createLeftPanel());
      split.setRightWidget(graphManager.createPanel());
      split.setSplitPosition("20%");

      RootPanel.get("main").add(split);
   }

   private Widget createLeftPanel()
   {
      VerticalPanel vpanel = new VerticalPanel();

      // setup Calendar
      picker.setValue(new Date());
      picker.addValueChangeHandler(new ValueChangeHandler<Date>()
      {
         public void onValueChange(ValueChangeEvent<Date> event)
         {
            graphManager.updateSelectedGraph(event.getValue());
         }
      });
      vpanel.add(picker);
      vpanel.add(treeManager.createPanel());

      return vpanel;
   }

   private void configureTreeManager()
   {
      treeManager.setListener(new TreeManager.TreeSelectionListener()
      {
         @Override public void selectionChanged()
         {
            graphManager.selectedEqsChanged(treeManager.getCheckedEntries());         
         }
      });
   }

   /**<!====== onModuleLoad ==================================================>
      Construct the page after the main framework is loaded.
   <!=======================================================================>*/
   public void onModuleLoad()
   {
      // create the UI
      setupUI();

      // Register a listener with the tree so we know when to generate graphs
      configureTreeManager();
   }
}
