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
