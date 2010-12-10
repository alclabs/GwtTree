package com.controlj.addon.gwttree.client;

import com.google.gwt.core.client.GWT;

/**<!=========================================================================>
   A utility class.
<!==========================================================================>*/
public class Util
{
   /**<!====== makeWebAppURL =================================================>
      Makes a URL to the current webapp (which means that users don't need to
      know the webapp name).  Also takes a sets of parameters and adds them to
      the resulting URL.
   <!=======================================================================>*/
   public static String makeWebAppURL(String end, String... params)
   {
      StringBuilder bldr = new StringBuilder(GWT.getModuleBaseURL());
      bldr.delete(bldr.length() - GWT.getModuleName().length() - 1, bldr.length());
      bldr.append(end);
      if (params.length > 0)
      {
         bldr.append('?');
         for (String param : params)
            bldr.append(param).append('&');
      }
      return bldr.toString();
   }
}

