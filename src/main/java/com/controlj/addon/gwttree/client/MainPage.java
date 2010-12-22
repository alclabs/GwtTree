/*
 * Copyright (c) 2010 Automated Logic Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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
