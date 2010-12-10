package com.controlj.addon.gwttree.client;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.*;

import java.util.*;

/**<!=========================================================================>
   Manages the tabs and their associated graphs.  This class is responsible for
   showing the correct tabs based on the items that have been selected by the
   user in the tree, and fetching and displaying the graph for a single tab.
<!==========================================================================>*/
public class GraphManager
{
   private static final String EMPTY_GRAPH = "../default.gif";

   private final GraphFetcher fetcher = new GraphFetcher();
   private final List<Tab> tabs = new ArrayList<Tab>();
   private final TabPanel tabPanel = new DecoratedTabPanel();

   /**<!====== createPanel ===================================================>
      Creates and returns the panel that contains the tabs and graph.  Used when
      building the inital UI.
   <!=======================================================================>*/
   public Widget createPanel()
   {
      showEmptyTab();
      tabPanel.addSelectionHandler(new SelectionHandler<Integer>()
      {
         @Override public void onSelection(SelectionEvent<Integer> selection)
         {
            Tab selectedTab = tabs.get(selection.getSelectedItem());
            fetcher.update(selectedTab);
         }
      });

      return tabPanel;
   }

   /**<!====== reset =========================================================>
      Resets the view to show the default "Graph" tab with the "Select something"
      image.  Called whenever the user changes tree options.
   <!=======================================================================>*/
   public void reset()
   {
      clearTabs();
      showEmptyTab();
   }

   private void showEmptyTab()
   {
      addGraphTab("Graph", Collections.<TreeEntry>emptyList());
      tabPanel.selectTab(0);
   }

   private void clearTabs()
   {
      tabs.clear();
      while (tabPanel.getWidgetCount() > 0)
         tabPanel.remove(0);
   }

   /**<!====== reset =========================================================>
      Changes the date for the selected graph (and future graphs).  Called when
      a new date is picked in from the calendar.
   <!=======================================================================>*/
   public void updateSelectedGraph(Date date)
   {
      fetcher.date = date;
      fetcher.update(getSelected());
   }

   private Tab getSelected()
   {
      return tabs.get(tabPanel.getTabBar().getSelectedTab());
   }

   /**<!====== selectedEqsChanged ============================================>
      Changes the available tabs across the top and autoselects the first one
      for displaying the graph.  Called whenever the user checks/unchecks an
      equipment in the tree.
   <!=======================================================================>*/
   public void selectedEqsChanged(Set<TreeEntry> checkedEntries)
   {
      Set<String> allTrends = new TreeSet<String>();
      for (TreeEntry entry : checkedEntries)
         for (TreeEntry.TrendSource source : entry.getTrendSources())
            allTrends.add(source.getName());

      Tab selected = getSelected();
      clearTabs();

      if (allTrends.isEmpty())
      {
         showEmptyTab();
      }
      else
      {
         int selectedIdx = 0;
         for (String trend : allTrends)
         {
            addGraphTab(trend, findEqsWithTrend(trend, checkedEntries));
            if (trend.equals(selected.name))
               selectedIdx = tabs.size() - 1;
         }
         tabPanel.selectTab(selectedIdx);
      }
   }

   private List<TreeEntry> findEqsWithTrend(String trend, Set<TreeEntry> allEqs)
   {
      List<TreeEntry> entries = new ArrayList<TreeEntry>();
      for (TreeEntry eq : allEqs)
      {
         if (containsTrend(trend, eq.getTrendSources()))
            entries.add(eq);
      }
      return entries;
   }

   private boolean containsTrend(String trend, Collection<TreeEntry.TrendSource> sources)
   {
      for (TreeEntry.TrendSource source : sources)
      {
         if (source.getName().equals(trend))
            return true;
      }
      return false;
   }

   private void addGraphTab(String name, List<TreeEntry> eqs)
   {
      Tab tab = new Tab(name, eqs);
      tabs.add(tab);
      tabPanel.add(tab.createPanel(), tab.getTitle());
   }

   /**<!=========================================================================>
      Private helper class to retrieve a graph from the server.
   <!==========================================================================>*/
   private static class GraphFetcher
   {
      private Date date = new Date();

      public void update(Tab tab)
      {
         String url = tab.eqs.isEmpty() ? EMPTY_GRAPH : requestGraph(tab.name, tab.eqs);
         tab.image.setUrl(url);
      }

      private String requestGraph(String sourceName, List<TreeEntry> eqs)
      {
         String[] params = new String[2+eqs.size()];
         params[0] = "date="+date.getTime();
         params[1] = "trend="+sourceName;
         int idx = 0;
         for (TreeEntry entry : eqs)
         {
            params[idx+2] = "eq" + idx + '=' + entry.getPath();
            ++idx;
         }

         return Util.makeWebAppURL("servlet/graph", params);
      }
   }

   /**<!=========================================================================>
      Private helper class to maintain information about a given tab.  Each tab is
      for a single trend (whose name is stored in "name") and each such trend may
      be located in multiple equipment (stored in "eqs").
   <!==========================================================================>*/
   private static class Tab
   {
      private String name;
      private Image image;
      private List<TreeEntry> eqs;

      private Tab(String name, List<TreeEntry> eqs)
      {
         this.name = name;
         this.eqs = eqs;
      }

      public Widget createPanel()
      {
         FlowPanel graph = new FlowPanel();
         image = new Image(EMPTY_GRAPH);
         image.setHeight("400");
         image.setWidth("600");
         graph.add(image);
         graph.addStyleName("tabView");
         return graph;
      }

      public String getTitle()
      {
         if (eqs.size() <= 1)
            return name;
         else
            return name + ' ' + '(' + eqs.size() + ')';
      }
   }
}