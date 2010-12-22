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

