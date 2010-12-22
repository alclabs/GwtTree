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