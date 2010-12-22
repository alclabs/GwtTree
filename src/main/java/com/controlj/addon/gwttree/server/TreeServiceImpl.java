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

package com.controlj.addon.gwttree.server;

import com.controlj.addon.gwttree.client.TreeEntry;
import com.controlj.addon.gwttree.client.TreeService;
import com.controlj.green.addonsupport.AddOnInfo;
import com.controlj.green.addonsupport.access.*;
import com.controlj.green.addonsupport.access.aspect.AnalogTrendSource;
import com.controlj.green.addonsupport.access.aspect.TrendSource;
import com.controlj.green.addonsupport.access.util.Acceptors;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.util.*;

/**<!=========================================================================>
   The server side part of the TreeService.  See {@link TreeService}
   and {@link com.controlj.addon.gwttree.client.TreeServiceAsync} for the client
   side parts.
<!==========================================================================>*/
public class TreeServiceImpl extends RemoteServiceServlet implements TreeService
{
   private AspectAcceptor<TrendSource> acceptor;

   public TreeEntry getDynamicTreeRootEntry(String[] sourceNames) throws Exception
   {
      acceptor = createAcceptor(sourceNames);
      SystemConnection connection = AddOnInfo.getAddOnInfo().getUserSystemConnection(getThreadLocalRequest());
      return connection.runReadAction(new ReadActionResult<TreeEntry>()
      {
         public TreeEntry execute(SystemAccess access) throws Exception
         {
            return createEntry(access.getGeoRoot());
         }
      });
   }

   public List<TreeEntry> getDynamicTreeChildren(final TreeEntry parent) throws Exception
   {
      SystemConnection connection = AddOnInfo.getAddOnInfo().getUserSystemConnection(getThreadLocalRequest());
      return connection.runReadAction(new ReadActionResult<List<TreeEntry>>()
      {
         public List<TreeEntry> execute(SystemAccess access) throws Exception
         {
            Location parentLoc = access.getTree(SystemTree.Geographic).resolve(parent.getPath());
            Collection<Location> children = parentLoc.getChildren();
            List<TreeEntry> entries = new ArrayList<TreeEntry>(children.size());
            for (Location child : children)
               entries.add(createEntry(child));
            return entries;
         }
      });
   }

   private AspectAcceptor<TrendSource> createAcceptor(String[] sourceNames)
   {
      if (sourceNames.length == 0)
         return Acceptors.enabledTrendSource();

      return Acceptors.enabledTrendSourceByName(sourceNames);
   }

   private TreeEntry createEntry(Location location)
   {
      TreeEntry treeEntry = new TreeEntry();
      treeEntry.setName(location.getDisplayName());
      treeEntry.setPath(location.getTransientLookupString());
      if (location.getType() == LocationType.Equipment)
      {
         treeEntry.setAllowsChildren(false);
         Collection<AnalogTrendSource> sources = location.find(AnalogTrendSource.class, acceptor);
         for (TrendSource source : sources)
         {
            TreeEntry.TrendSource treeSource = new TreeEntry.TrendSource();
            treeSource.setName(source.getLocation().getReferenceName());
            treeEntry.addTrendSource(treeSource);
         }
      }
      return treeEntry;
   }

   public TreeEntry getStaticTreeRootEntry(String[] sourceNames) throws Exception
   {
      acceptor = createAcceptor(sourceNames);
      try
      {
         SystemConnection connection = AddOnInfo.getAddOnInfo().getUserSystemConnection(getThreadLocalRequest());
         return connection.runReadAction(new ReadActionResult<TreeEntry>()
         {
            public TreeEntry execute(SystemAccess access) throws Exception
            {
               return access.visit(access.getGeoRoot(), new TrendSparseTreeVisitor());
            }
         });
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw e;
      }
   }

   /**<!=========================================================================>
      Implements a TreeVisitor to build the TreeEntries that we need for
      creating the GWT tree.  This visitor filters out all sub-trees that
      do not contain equipment with trend sources named <trendSourceName>
      (the only argument to the constructor).
      <p/>
      To see this algorithm work, imagine the following geographic tree
      (equipment marked with a star have enabled trend sources that match
      our given reference name):
      <pre>
                          system
                        /   |   \
                      /     |    \
                   area1   eq2   area3
                   /   \         /   \
                  /     \       /     \
               area4    eq5*  eq6*   area7
               /   \
             eq8   eq9

      This results in the following chain of calls:

         1) visit(null, system) --> returns a TreeEntry(system)
         2) visit(TE-system, area1) --> returns a TreeEntry(area1)
         3) visit(TE-area1, area4) --> returns a TreeEntry(area4)
         4) visitEquipment(TE-area4, eq8) --> returns a null because
            eq8 does not have a star
         5) Because we returned a null for eq8, addChild is not called
            even though we reached the bottom of the tree
         6) visitEquipment(TE-area4, eq9) --> returns a null because
            eq9 does not have a star
         7) Just like step 5, addChild is not called for eq9
         8) addChild(TE-area1, TE-area4) --> because TE-area4 is not
            an equipment TE and because it has not children, do not
            add it to TE-area1's list of children
         9) visitEquipment(TE-area1, eq5) --> eq5 has a trend source
            that we care about, so return TreeEntry(eq5)
        10) addChild(TE-area1, TE-eq5) --> because TE-eq5 is an
            equipment TE, add it to TE-area1's list of children
        11) addChild(TE-system, TE-area1) --> TE-area1 now has children,
            so it is added to TE-system's children.
        12) visitEquipment(TE-system, eq2) --> returns null (eq2 does
            not have a star)
        13) Does not call addChild for eq2 because of the null return.
        14) visit(TE-system, area3) --> returns a TreeEntry(area3)
        15) visitEquipment(TE-area3, eq6) --> eq6 is starred, so
            returns a TreeEntry(eq6)
        16) addChild(TE-area3, TE-eq6) --> add TE-eq6 to TE-area3's
            children
        17) visit(TE-area3, area7) --> returns a TreeEntry(area7)
        18) addChild(TE-area3, TE-area7) --> TE-area7 has no children
            so it is not added to TE-area3's children.
        19) addChild(TE-system, TE-area3) --> TE-area3 has a child, so
            it gets added to TE-system's children.

      This ends up resulting in this tree (which has pruned all the
      uninteresting parts out):
                        TE-system
                        /       \
                     area1     area3
                       |         |
                      eq5*      eq6*
      </pre>
   <!==========================================================================>*/
   private class TrendSparseTreeVisitor extends TreeVisitor<TreeEntry>
   {
      /**<!====== visitEquipment ================================================>
         Specially handle any equipment that we find.  In this case, we
         want to find if the equipment has any trend sources with the
         given name.  If not, return a null which tells the
         {@link SystemAccess#visit(Location, TreeVisitor)} method to stop
         walking this branch of the tree, and to not call {@link #addChild}.
         <!      Name       Description>
         @param  parent     the parent TreeEntry for this location.
         @param  eq         the equipment location being visited.
         @return the TreeEntry for this equipment, or null if the equipment
                 does not contain any trend sources of interest.
      <!=======================================================================>*/
      @Override public TreeEntry visitEquipment(TreeEntry parent, Location eq)
      {
         TreeEntry entry = createEntry(eq);
         return entry.hasTrendSources() ? entry : null;
      }

      /**<!====== visit =========================================================>
         Since we are walking the geographic tree, we can only encounter
         System, Area, and Equipment locations (we are not visiting microblocks
         or lower so those are not possibilities).  Since we have a special method
         for handling Equipment type locations, we only have to worry about
         System and Area type locations here.  For those, we'll just always create
         a TreeEntry object.  Later, when {@link #addChild} gets called we'll decide
         whether we want to keep these TreeEntries or not.
         <!      Name       Description>
         @param  parent     the parent location's TreeEntry.
         @param  location   the location being visited.
         @return the new TreeEntry for this location.
      <!=======================================================================>*/
      @Override public TreeEntry visit(TreeEntry parent, Location location)
      {
         return createEntry(location);
      }

      /**<!====== addChild ======================================================>
         This method allows us to "hook" the parent and children together.  Because
         this isn't called until the whole sub-tree has been visited, this gives us
         a chance to decide if we want this sub-tree before hooking it to it's parent.
         </p>
         If the child is an equipment we know we want to keep it because we'll only
         have an equipment TreeEntry if this equipment has interesting trends.  Otherwise
         we want to keep any area that has children (because that means it contains
         one or more interesting equipment in it's descendant tree).  So, if one of
         those two cases is true, add the child TreeEntry to the parent TreeEntry's
         list of children.
         </p>
         <!      Name       Description>
         @param  parent     the parent location's TreeEntry.
         @param  child      the child location' TreeEntry.
      <!=======================================================================>*/
      public void addChild(TreeEntry parent, TreeEntry child)
      {
         if (child.hasTrendSources() || !child.getChildren().isEmpty())
            parent.addChild(child);
      }
   }
}