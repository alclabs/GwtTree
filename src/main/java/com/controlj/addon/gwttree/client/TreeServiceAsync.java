package com.controlj.addon.gwttree.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;
import java.util.Set;

/**<!=========================================================================>
   Part of the client side of the TreeService.  See {@link TreeService}
   and {@link com.controlj.addon.gwttree.server.TreeServiceImpl} for the other
   parts.
<!==========================================================================>*/
public interface TreeServiceAsync
{
   void getStaticTreeRootEntry(String[] sourceNames, AsyncCallback<TreeEntry> async);
   void getDynamicTreeRootEntry(String[] sourceNames, AsyncCallback<TreeEntry> async);
   void getDynamicTreeChildren(TreeEntry parent, AsyncCallback<List<TreeEntry>> async);   
}
