package com.controlj.addon.gwttree.client;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/**<!=========================================================================>
   Class to hold information about each node in the tree.  Instances of this
   class get "remoted" to and from the server, so it has limits to the types of
   things it can hold (see the GWT documentation for details).
<!==========================================================================>*/
public class TreeEntry implements IsSerializable
{
   private String name;
   private String path;
   private boolean allowsChildren = true;
   private List<TreeEntry> children = new ArrayList<TreeEntry>();
   private List<TrendSource> sources = new ArrayList<TrendSource>();

   public TreeEntry() { }

   public String getName() { return name; }
   public void setName(String name) { this.name = name; }

   public String getPath() { return path; }
   public void setPath(String path) { this.path = path; }

   public boolean isAllowsChildren() { return allowsChildren; }
   public void setAllowsChildren(boolean allowsChildren) { this.allowsChildren = allowsChildren; }

   public List<TreeEntry> getChildren() { return Collections.unmodifiableList(children); }
   public void addChild(TreeEntry child) { children.add(child); }

   public boolean hasTrendSources() { return !sources.isEmpty(); }
   public Collection<TrendSource> getTrendSources() { return Collections.unmodifiableCollection(sources); }
   public void addTrendSource(TrendSource source) { sources.add(source); }

   public static class TrendSource implements IsSerializable, Comparable<TrendSource>
   {
      private String name;

      public String getName() { return name; }
      public void setName(String name) { this.name = name; }

      @Override public int compareTo(TrendSource o)
      {
         return name.compareTo(o.name);
      }
   }
}