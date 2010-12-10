package com.controlj.addon.gwttree.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

import java.util.List;
import java.util.Set;

/**<!=========================================================================>
   Part of the client side of the TreeService.  See {@link TreeServiceAsync}
   and {@link com.controlj.addon.gwttree.server.TreeServiceImpl} for the other
   parts.
<!==========================================================================>*/
public interface TreeService extends RemoteService
{
   TreeEntry getStaticTreeRootEntry(String[] sourceNames) throws Exception;
   TreeEntry getDynamicTreeRootEntry(String[] sourceNames) throws Exception;
   List<TreeEntry> getDynamicTreeChildren(TreeEntry parent) throws Exception;

   /**<!=========================================================================>
      Utility/Convenience class.
      Use TreeService.App.getInstance() to access static instance of TreeServiceAsync
   <!==========================================================================>*/
   public static class App
   {
      private static TreeServiceAsync ourInstance = null;

      public static synchronized TreeServiceAsync getInstance()
      {
         if (ourInstance == null)
         {
            ourInstance = (TreeServiceAsync) GWT.create(TreeService.class);
            ((ServiceDefTarget) ourInstance).setServiceEntryPoint(Util.makeWebAppURL("service/tree"));
         }
         return ourInstance;
      }
   }
}
