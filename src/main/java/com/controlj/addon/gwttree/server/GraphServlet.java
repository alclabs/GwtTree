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

import com.controlj.green.addonsupport.InvalidConnectionRequestException;
import com.controlj.green.addonsupport.access.*;
import com.controlj.green.addonsupport.access.aspect.AnalogTrendSource;
import com.controlj.green.addonsupport.access.trend.TrendAnalogSample;
import com.controlj.green.addonsupport.access.trend.TrendData;
import com.controlj.green.addonsupport.access.trend.TrendProcessor;
import com.controlj.green.addonsupport.access.trend.TrendRangeFactory;
import com.controlj.green.addonsupport.access.util.Acceptors;
import org.jfree.chart.*;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;
import java.text.DateFormat;
import java.util.*;
import java.util.List;

/**<!=========================================================================>
   This class uses JFreeChart to create and serve up the desired chart.
<!==========================================================================>*/
public class GraphServlet extends HttpServlet
{
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
      doPost(request, response);
   }

   /**<!====== doPost ========================================================>
      Generates a graph from the specified parameters in the request.  The request
      will contain a date parameter with the date to graph.  The list of
      trend sources is obtained from the TreeService (synchronized via GWT RPC
      mechanism and not passed on every request).
   <!=======================================================================>*/
   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
      SystemConnection connection = createConnection(request);
      GraphParameters params = extractParameters(request);
      JFreeChart chart = createChart(connection, params);

      // generate response
      response.setHeader("Cache-Control", "no-cache");
      response.setContentType("image/png");
      try
      {
         // Generate the graph from the dataset and write out as a PNG
         ChartUtilities.writeChartAsPNG(response.getOutputStream(), chart, 600, 400);
      }
      catch (IOException e)
      {
         //ignore errors writing out image
      }
   }

   private GraphParameters extractParameters(HttpServletRequest request)
   {
      Date date = new Date(Long.parseLong(request.getParameter("date")));
      String trend = request.getParameter("trend");
      List<String> eqs = new ArrayList<String>();
      int eqNum = 0;
      for (;;)
      {
         String eqName = request.getParameter("eq"+eqNum);
         if (eqName == null)
            break;
         eqs.add(eqName);
         ++eqNum;
      }

      return new GraphParameters(date, trend, eqs);
   }

   private SystemConnection createConnection(HttpServletRequest request) throws ServletException
   {
      try
      {
         return DirectAccess.getDirectAccess().getUserSystemConnection(request);
      }
      catch (InvalidConnectionRequestException e)
      {
         throw new ServletException(e);
      }
   }

   private JFreeChart createChart(SystemConnection connection, GraphParameters params) throws ServletException, IOException
   {
      DefaultCategoryDataset dataset = new DefaultCategoryDataset();

      for (String path : params.getEqs())
      {
         try
         {
            // Find the name to title this series
            String displayName = getDisplayName(connection, path);

            // Calculate the starting and ending Dates
            Date start = getMidnight(params.getDate());
            Calendar endCal = new GregorianCalendar();
            endCal.setTime(start);
            endCal.add(Calendar.DATE, 1);
            Date end = endCal.getTime();

            // Total up the demand from the specified trend source
            float value = totalDemand(connection, path, params.getTrend(), start, end);

            // Add this value as a new data series in the graph's dataset
            dataset.addValue(value, displayName, params.getDateString());
         }
         catch (Exception e)
         {
            throw new ServletException(e);
         }
      }

      return getChart(dataset);
   }

   /**<!====== getChart ======================================================>
      Generates the chart.  Note that most of these options are not required
      for a simple chart, but we wanted to try to get a particular look that
      matched another charting package, so we went to some trouble to set
      a lot of non-standard options.  In particular, the OpaqueBarRenderer3D
      is a non-standard renderer that gives much better looking 3D bars.
   <!=======================================================================>*/
   private static JFreeChart getChart(DefaultCategoryDataset dataset)
   {
      JFreeChart chart = ChartFactory.createBarChart3D("Integration Over Time", "24 Hour Period", "", dataset, PlotOrientation.VERTICAL, true, true, false);
      chart.setBackgroundPaint(Color.black);
      chart.getTitle().setPaint(Color.white);
      chart.getTitle().setFont(new Font("Verdana", Font.BOLD, 20));
      chart.setAntiAlias(true);
      chart.setTextAntiAlias(true);
      CategoryPlot plot = (CategoryPlot) chart.getPlot();
      plot.setForegroundAlpha(1);
      OpaqueBarRenderer3D renderer = new OpaqueBarRenderer3D();
      renderer.setItemMargin(0.1);
      plot.setRenderer(renderer);

      renderer.setSeriesPaint(0, new Color(116,225,118));
      renderer.setSeriesPaint(1, new Color(245,107,107));
      renderer.setSeriesPaint(2, new Color(250,187,107));
      renderer.setSeriesPaint(3, new Color(72,72,238));
      renderer.setSeriesPaint(4, new Color(184,32,157));
      renderer.setDrawBarOutline(false);

      Font small = new Font("Verdana", Font.PLAIN, 12);
      Font big = new Font("Verdana", Font.BOLD, 14);

      renderer.setItemLabelPaint(Color.white);
      plot.getDomainAxis().setTickLabelPaint(Color.white);
      plot.getDomainAxis().setTickLabelFont(small);
      plot.getDomainAxis().setLabelPaint(Color.white);
      plot.getDomainAxis().setLabelFont(big);
      plot.getDomainAxis().setCategoryMargin(0.2);

      plot.getRangeAxis().setTickLabelPaint(Color.white);
      plot.getRangeAxis().setTickLabelFont(small);
      plot.getRangeAxis().setLabelPaint(Color.white);
      plot.getRangeAxis().setLabelFont(big);
      plot.setBackgroundPaint(Color.black);

      chart.getLegend().setBackgroundPaint(Color.black);
      chart.getLegend().setItemPaint(Color.white);
      chart.getLegend().setItemFont(big);

      return chart;
   }

   private String getDisplayName(SystemConnection connection, final String path) throws SystemException, ActionExecutionException
   {
      return connection.runReadAction(new ReadActionResult<String>()
      {
         public String execute(SystemAccess access) throws Exception
         {
            return access.getTree(SystemTree.Geographic).resolve(path).getDisplayName();
         }
      });
   }

   /**<!====== totalDemand ===================================================>
      Calculates the consumption (in kwH) from a specified trend source of
      demand (kW) over a specified time range.
      <!      Name                 Description>
      @param  connection           SystemConnection to use to get trend data.
      @param  path                 LookupString for trend source.
      @param  trendName            .
      @param  start                Starting Date for totaling demand.
      @param  end                  Ending Date for totaling demand.
      @return total consumption in kWH.
   <!=======================================================================>*/
   private float totalDemand(SystemConnection connection, final String path, final String trendName, final Date start, final Date end) throws SystemException, ActionExecutionException
   {
      return connection.runReadAction(FieldAccessFactory.newFieldAccess(), new ReadActionResult<Float>()
      {
         public Float execute(SystemAccess access) throws Exception
         {
            Location eq = access.getTree(SystemTree.Geographic).resolve(path);
            Collection<AnalogTrendSource> trendSourceCollection = eq.find(AnalogTrendSource.class,
                                                                          Acceptors.enabledTrendSourceByName(trendName));

            AnalogTrendSource source = trendSourceCollection.iterator().next();
            TrendData<TrendAnalogSample> data = source.getTrendData(TrendRangeFactory.byDateRange(start, end));
            Integrator integrator = data.process(new Integrator());
            return integrator.getValue();
         }
      });
   }

   /**<!=========================================================================>
      TrendProcessor that integrates the samples from an electrical kW trend source
      to determine kWH demand.  Holes in the data are not counted towards the total
      demand.
   <!==========================================================================>*/
   private static class Integrator implements TrendProcessor<TrendAnalogSample>
   {
      private float total = 0;
      TrendAnalogSample lastSample = null;
      Date lastTime = null;

      public void processStart(Date startTime, TrendAnalogSample startBookend)
      {
         // For this application, ignore the time before the first sample.
         // For some applications (like COV binary trends) you really need to know what the previous
         // real sample value was before the requested range
      }

      public void processData(TrendAnalogSample sample)
      {
         // Don't process the very first sample - we need two samples to integrate between.  Just store it in lastSample
         if (lastSample != null)
         {
            // We have two samples, add their contribution to the total
            total += integrateStep(lastSample, sample);
         }
         else if (lastTime != null)    // Handle the first data after a hole
         {
            // assume same value from last known time and this sample.  This handle trailing edge of processHole
            total += integrateStep(lastTime.getTime(), sample.floatValue(), sample.getTimeInMillis(), sample.floatValue());
            lastTime = null;
         }
         lastSample = sample;
      }

      public void processEnd(Date endTime, TrendAnalogSample endBookend)
      {
         // ignore the time after the last sample
      }

      public void processHole(Date start, Date end)
      {
         // Total up the contribution from the last sample to the beginning of the hole (assuming the value didn't change)
         total += integrateStep(lastSample.getTimeInMillis(), lastSample.floatValue(), start.getTime(), lastSample.floatValue());
         lastTime = end;
         lastSample = null;
      }

      public float getValue()
      {
         return total;
      }

      /**<!====== integrateStep =================================================>
         Perform a geometric integration of the area under this piece of a curve
         (assuming a straight line approximation of values between the two points).
         <!      Name       Description>
         @param  startTime  Time of starting sample in milliSeconds.
         @param  startValue Starting value in kW.
         @param  endTime    Time of ending sample in milliSeconds.
         @param  endValue   Ending value in kW.
         @return area under this piece of the curve in kwH.
      <!=======================================================================>*/
      private double integrateStep(long startTime, float startValue, long endTime, float endValue)
      {
         double min = Math.min(startValue, endValue);
         double max = Math.max(startValue, endValue);
         double hours = ((double)(endTime - startTime)) / (1000.0 * 60.0 *60.0);
         return (min + (max - min)/2) * hours;
      }

      private double integrateStep(TrendAnalogSample first, TrendAnalogSample second)
      {
         return integrateStep(first.getTimeInMillis(), first.floatValue(), second.getTimeInMillis(), second.floatValue());
      }

   }

   Date getMidnight(Date onDate)
   {
      Calendar cal = new GregorianCalendar();
      cal.setTime(onDate);
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);
      return cal.getTime();
   }

   private static class GraphParameters
   {
      private static final DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.SHORT);

      private final Date date;
      private final String trend;
      private final List<String> eqs;

      private GraphParameters(Date date, String trend, List<String> eqs)
      {
         this.date = date;
         this.trend = trend;
         this.eqs = eqs;
      }

      public Date getDate() { return date; }
      public String getDateString() { return DATE_FORMAT.format(date); }
      public String getTrend() { return trend; }
      public List<String> getEqs() { return eqs; }
   }
}