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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.event.logical.shared.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**<!=========================================================================>
   The class is responsible for handling the tree.  It handles both the static
   and dynamic trees as well as maintains knowledge about when items in the tree
   have been checked.
 <!==========================================================================>*/
public class TreeManager
{
   /**<!=========================================================================>
      Interface for when tree selections change.  This allows other things in the
      UI to react to those changes.
   <!==========================================================================>*/
   public static interface TreeSelectionListener
   {
      public void selectionChanged();
   }

   private static final String VIRTUAL_CHILD_NAME = "";
   private TreeSelectionListener listener;
   private TreeOptions options;
   private HorizontalPanel resultPanel;
   private Set<TreeEntry> checkedEntries = new HashSet<TreeEntry>();

   public TreeManager(final GraphManager graphManager)
   {
      options = new TreeOptions(new TreeOptions.ChangedHandler()
      {
         @Override public void optionsChanged(TreeOptions options)
         {
            if (options.isStaticTree())
               resetResultPanel("Searching for trend sources...");
            else
               resetResultPanel("Getting tree root node...");
            graphManager.reset();
            checkedEntries.clear();
            requestTree();
         }
      });
   }

   public void setListener(TreeSelectionListener listener)
   {
      this.listener = listener;
   }

   /**<!====== createPanel ===================================================>
      Returns a panel that displays the tree.  When the tree options are changed,
      the panel is updated to hold the new tree, so this method does not need to
      be called more than once.
   <!=======================================================================>*/
   public Widget createPanel()
   {
      Panel treePanel = new VerticalPanel();
      treePanel.setStylePrimaryName("tree-treePanel");

      // create a panel for the resulting tree(s) now (it's used below)
      // but don't add it to the panel until the end (so it's at the bottom)
      resultPanel = new HorizontalPanel();

      Button optionsButton = new Button("Change Tree Options...");
      optionsButton.addClickHandler(new ClickHandler()
      {
         @Override public void onClick(ClickEvent clickEvent)
         {
            options.showDialog();
         }
      });
      treePanel.add(optionsButton);

      resetResultPanel("Getting tree root node...");
      requestTree();

      treePanel.add(resultPanel);
      return treePanel;
   }

   private void resetResultPanel(String message)
   {
      // Add loading label to the result panel
      setResultPanelContents(new Label(message));
   }

   private void setResultPanelContents(Widget widget)
   {
      while (resultPanel.getWidgetCount() > 0)
         resultPanel.remove(0);
      resultPanel.add(widget);
   }

   /**<!====== requestTree ===================================================>
      Based on the options, requests the desired tree from the server and
      displays it.
   <!=======================================================================>*/
   public void requestTree()
   {
      if (options.isStaticTree())
         requestStaticTree();
      else
         requestDynamicTree();
   }

   private void requestStaticTree()
   {
      TreeService.App.getInstance().getStaticTreeRootEntry(options.getSourceNamesArray(), new AsyncCallback<TreeEntry>()
      {
         public void onFailure(Throwable caught)
         {
            Dialog.showHtmlErrorDialog("Error building tree", caught.toString());
         }

         public void onSuccess(TreeEntry result)
         {
            setResultPanelContents(createStaticTree(result));
         }
      });
   }

   private Tree createStaticTree(TreeEntry root)
   {
      Tree staticTree = new Tree();
      staticTree.setAnimationEnabled(true);
      staticTree.addItem(createTreeItemAndChildren(root));
      return staticTree;
   }

   private TreeItem createTreeItemAndChildren(TreeEntry entry)
   {
      TreeItem item = createTreeItem(entry);
      for (TreeEntry childEntry : entry.getChildren())
         item.addItem(createTreeItemAndChildren(childEntry));
      return item;
   }

   private TreeItem createTreeItem(final TreeEntry entry)
   {
      TreeItem item = new TreeItem();
      if (!entry.hasTrendSources())
         item.setText(entry.getName());
      else
      {
         CheckBox checkBox = new CheckBox(entry.getName());
         checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>()
         {
            @Override public void onValueChange(ValueChangeEvent<Boolean> event)
            {
               if (event.getValue())
                  checkedEntries.add(entry);
               else
                  checkedEntries.remove(entry);
               listener.selectionChanged();
            }
         });
         item.setWidget(checkBox);
      }

      item.setUserObject(entry);
      return item;         
   }

   private void requestDynamicTree()
   {
      TreeService.App.getInstance().getDynamicTreeRootEntry(options.getSourceNamesArray(), new AsyncCallback<TreeEntry>()
      {
         public void onFailure(Throwable caught)
         {
            Dialog.showHtmlErrorDialog("Error getting tree root", caught.toString());
         }

         public void onSuccess(TreeEntry result)
         {
            setResultPanelContents(createDynamicTree(result));
         }
      });
   }

   private TreeItem createDynamicTreeItem(TreeEntry entry)
   {
      TreeItem item = createTreeItem(entry);
      if (entry.isAllowsChildren())
         item.addItem(new TreeItem(VIRTUAL_CHILD_NAME));
      return item;
   }

   private Tree createDynamicTree(TreeEntry root)
   {
      Tree dynamicTree = new Tree();
      dynamicTree.setAnimationEnabled(true);
      dynamicTree.addItem(createDynamicTreeItem(root));

      // Add a handler to get the next set of children
      dynamicTree.addOpenHandler(new OpenHandler<TreeItem>()
      {
         @Override public void onOpen(OpenEvent<TreeItem> event)
         {
            TreeEntry entry = (TreeEntry) event.getTarget().getUserObject();
            requestDynamicTreeChildren(event.getTarget(), entry);
         }
      });
      return dynamicTree;
   }

   private void requestDynamicTreeChildren(final TreeItem parentItem, final TreeEntry parent)
   {
      TreeService.App.getInstance().getDynamicTreeChildren(parent, new AsyncCallback<List<TreeEntry>>()
      {
         public void onFailure(Throwable caught)
         {
            Dialog.showHtmlErrorDialog("Error getting children", caught.getMessage());
         }

         public void onSuccess(List<TreeEntry> result)
         {
            parentItem.removeItems(); // get rid of the "virtual child"
            for (TreeEntry child : result)
            {
               parent.addChild(child);
               parentItem.addItem(createDynamicTreeItem(child));
            }
         }
      });
   }

   public Set<TreeEntry> getCheckedEntries()
   {
      return Collections.unmodifiableSet(checkedEntries);
   }
}
