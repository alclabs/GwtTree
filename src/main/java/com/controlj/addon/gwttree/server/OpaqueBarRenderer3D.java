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

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleEdge;

import java.awt.*;
import java.awt.geom.*;

/**<!=========================================================================>
   Renders an opaque 3D bar for a data series.  Paints the bar with a gradient
   from the specified color to a darker shade.
<!==========================================================================>*/
public class OpaqueBarRenderer3D extends BarRenderer3D
{
   public OpaqueBarRenderer3D()
   {
      super();
   }

   public OpaqueBarRenderer3D(double xOffset, double yOffset)
   {
      super(xOffset, yOffset);
   }

   /**<!====== drawItem ======================================================>
      Draws a 3D bar to represent one data item.
      <!      Name       Description>
      @param  g2         the graphics device.
      @param  state      the renderer state.
      @param  dataArea   the area for plotting the data.
      @param  plot       the plot.
      @param  domainAxis the domain axis.
      @param  rangeAxis  the range axis.
      @param  dataset    the dataset.
      @param  row        the row index (zero-based).
      @param  column     the column index (zero-based).
      @param  pass       the pass index.
   <!=======================================================================>*/
   @Override
   public void drawItem(Graphics2D g2,
                        CategoryItemRendererState state,
                        Rectangle2D dataArea,
                        CategoryPlot plot,
                        CategoryAxis domainAxis,
                        ValueAxis rangeAxis,
                        CategoryDataset dataset,
                        int row,
                        int column,
                        int pass) {

       // check the value we are plotting...
       Number dataValue = dataset.getValue(row, column);
       if (dataValue == null) {
           return;
       }

      g2.setStroke(new BasicStroke(1));
       double value = dataValue.doubleValue();

       Rectangle2D adjusted = new Rectangle2D.Double(dataArea.getX(),
               dataArea.getY() + getYOffset(),
               dataArea.getWidth() - getXOffset(),
               dataArea.getHeight() - getYOffset());

       PlotOrientation orientation = plot.getOrientation();

       double barW0 = calculateBarW0(plot, orientation, adjusted, domainAxis,
               state, row, column);
       double[] barL0L1 = calculateBarL0L1(value);
       if (barL0L1 == null) {
           return;  // the bar is not visible
       }

       RectangleEdge edge = plot.getRangeAxisEdge();
       double transL0 = rangeAxis.valueToJava2D(barL0L1[0], adjusted, edge);
       double transL1 = rangeAxis.valueToJava2D(barL0L1[1], adjusted, edge);
       double barL0 = Math.min(transL0, transL1);
       double barLength = Math.abs(transL1 - transL0);

       // draw the bar...
       Rectangle2D bar = null;
       if (orientation == PlotOrientation.HORIZONTAL) {
           bar = new Rectangle2D.Double(barL0, barW0, barLength,
                   state.getBarWidth());
       }
       else {
           bar = new Rectangle2D.Double(barW0, barL0, state.getBarWidth(),
                   barLength);
       }
       Paint itemPaint = getItemPaint(row, column);
      if (itemPaint instanceof Color)
      {
         Color endColor = getFrontDark((Color) itemPaint);
         Color startColor = (Color) itemPaint;
         Paint paint = new GradientPaint((float)bar.getX(), (float)bar.getY(), startColor, (float)(bar.getX()), (float)(bar.getY()+bar.getHeight()), endColor);
         g2.setPaint(paint);
      }
      g2.fill(bar);

       double x0 = bar.getMinX();                  // left
       double x1 = x0 + getXOffset();              // offset left
       double x2 = bar.getMaxX();                  // right
       double x3 = x2 + getXOffset();              // offset right

       double y0 = bar.getMinY() - getYOffset();   // offset top
       double y1 = bar.getMinY();                  // bar top
       double y2 = bar.getMaxY() - getYOffset();   // offset bottom
       double y3 = bar.getMaxY();                  // bottom

      //Rectangle2D.Double line = new Rectangle2D.Double(x2, y1, 2, bar.getHeight());

      Line2D.Double line = new Line2D.Double(x2, y1, x2, y3);
      g2.draw(line);

      GeneralPath bar3dRight = null;
       GeneralPath bar3dTop = null;
      g2.setPaint(itemPaint);

      // Draw the right side
       if (barLength > 0.0) {
           bar3dRight = new GeneralPath();
           bar3dRight.moveTo((float) x2, (float) y3);
           bar3dRight.lineTo((float) x2, (float) y1);
           bar3dRight.lineTo((float) x3, (float) y0);
           bar3dRight.lineTo((float) x3, (float) y2);
           bar3dRight.closePath();

           if (itemPaint instanceof Color) {
              Color startColor = getSideLight((Color) itemPaint);
              Color endColor = getSideDark((Color) itemPaint);
              Paint paint = new GradientPaint((float)x3, (float)y0, startColor, (float)x2, (float)y3, endColor);
              g2.setPaint(paint);
           }
           g2.fill(bar3dRight);
       }

      // Draw the top
       bar3dTop = new GeneralPath();
       bar3dTop.moveTo((float) x0, (float) y1);    // bottom left
       bar3dTop.lineTo((float) x1, (float) y0);    // top left
       bar3dTop.lineTo((float) x3, (float) y0);    // top right
       bar3dTop.lineTo((float) x2, (float) y1);    // bottom right
       bar3dTop.closePath();
      if (itemPaint instanceof Color)
      {
         Color endColor = getTopDark((Color) itemPaint);
         Color startColor = getTopLight((Color) itemPaint);
         //Paint paint = new GradientPaint((float)x2, (float)y0, startColor, (float)x0, (float)(y1), endColor);
         Point2D.Double topRight = new Point2D.Double(x3, y0);
         Point2D.Double bottomLeft = new Point2D.Double(x0, y1);
         //Point2D.Double darkEnd = getTargetPoint(bottomLeft, topRight, ((y0-y1)/(x3-x2)));
         Point2D.Double darkEnd = new Point2D.Double(x1, y0-(x3-x1)*((y0-y1)/(x3-x2)));
         Paint paint = new GradientPaint((float)topRight.getX(), (float)topRight.getY(), startColor, (float)darkEnd.getX(), (float)darkEnd.getY(), endColor);
         g2.setPaint(paint);
         //drawMarker(topRight, g2, startColor);
      }
      g2.fill(bar3dTop);
      g2.setPaint(itemPaint);

       if (isDrawBarOutline()
               && state.getBarWidth() > BAR_OUTLINE_WIDTH_THRESHOLD) {
           g2.setStroke(getItemOutlineStroke(row, column));
           g2.setPaint(getItemOutlinePaint(row, column));
           g2.draw(bar);
           if (bar3dRight != null) {
               g2.draw(bar3dRight);
           }
           if (bar3dTop != null) {
               g2.draw(bar3dTop);
           }
       }

       CategoryItemLabelGenerator generator
           = getItemLabelGenerator(row, column);
       if (generator != null && isItemLabelVisible(row, column)) {
           drawItemLabel(g2, dataset, row, column, plot, generator, bar,
                   (value < 0.0));
       }

       // add an item entity, if this information is being collected
       EntityCollection entities = state.getEntityCollection();
       if (entities != null) {
           GeneralPath barOutline = new GeneralPath();
           barOutline.moveTo((float) x0, (float) y3);
           barOutline.lineTo((float) x0, (float) y1);
           barOutline.lineTo((float) x1, (float) y0);
           barOutline.lineTo((float) x3, (float) y0);
           barOutline.lineTo((float) x3, (float) y2);
           barOutline.lineTo((float) x2, (float) y3);
           barOutline.closePath();
           addItemEntity(entities, dataset, row, column, barOutline);
       }

   }

   @Override
   public void drawBackground(Graphics2D g2, CategoryPlot plot,
                              Rectangle2D dataArea) {

       float x0 = (float) dataArea.getX();
       float x1 = x0 + (float) Math.abs(getXOffset());
       float x3 = (float) dataArea.getMaxX();
       float x2 = x3 - (float) Math.abs(getXOffset());

       float y0 = (float) dataArea.getMaxY();
       float y1 = y0 - (float) Math.abs(getYOffset());
       float y3 = (float) dataArea.getMinY();
       float y2 = y3 + (float) Math.abs(getYOffset());

       GeneralPath clip = new GeneralPath();
       clip.moveTo(x0, y0);
       clip.lineTo(x0, y2);
       clip.lineTo(x1, y3);
       clip.lineTo(x3, y3);
       clip.lineTo(x3, y1);
       clip.lineTo(x2, y0);
       clip.closePath();

       Composite originalComposite = g2.getComposite();
       g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
               plot.getBackgroundAlpha()));

       // fill background...
       Paint backgroundPaint = plot.getBackgroundPaint();
       if (backgroundPaint != null) {
           g2.setPaint(backgroundPaint);
           g2.fill(clip);
       }

       GeneralPath bottomWall = new GeneralPath();
       bottomWall.moveTo(x0, y0);
       bottomWall.lineTo(x1, y1);
       bottomWall.lineTo(x3, y1);
       bottomWall.lineTo(x2, y0);
       bottomWall.closePath();
       g2.setPaint(getWallPaint());
       g2.fill(bottomWall);


       // draw background image, if there is one...
       Image backgroundImage = plot.getBackgroundImage();
       if (backgroundImage != null) {
           Rectangle2D adjusted = new Rectangle2D.Double(dataArea.getX()
                   + getXOffset(), dataArea.getY(),
                   dataArea.getWidth() - getXOffset(),
                   dataArea.getHeight() - getYOffset());
           plot.drawBackgroundImage(g2, adjusted);
       }

       g2.setComposite(originalComposite);

   }

   private Color getTopLight(Color primary)
   {
      float hsbVals[] = new float[3];
      Color.RGBtoHSB(primary.getRed(), primary.getGreen(), primary.getBlue(), hsbVals);
      hsbVals[1] = 0.3f;
      hsbVals[2] = 0.97f;
      return Color.getHSBColor(hsbVals[0], hsbVals[1], hsbVals[2]);
   }

   private Color getTopDark(Color primary)
   {
      float hsbVals[] = new float[3];
      Color.RGBtoHSB(primary.getRed(), primary.getGreen(), primary.getBlue(), hsbVals);
      hsbVals[1] = 0.9f;
      hsbVals[2] = 0.6f;
      return Color.getHSBColor(hsbVals[0], hsbVals[1], hsbVals[2]);
   }

   private Color getFrontDark(Color primary)
   {
      float hsbVals[] = new float[3];
      Color.RGBtoHSB(primary.getRed(), primary.getGreen(), primary.getBlue(), hsbVals);
      hsbVals[1] = 1.0f;
      hsbVals[2] = 0.5f;
      return Color.getHSBColor(hsbVals[0], hsbVals[1], hsbVals[2]);
   }

   private Color getSideLight(Color primary)
   {
      float hsbVals[] = new float[3];
      Color.RGBtoHSB(primary.getRed(), primary.getGreen(), primary.getBlue(), hsbVals);
      hsbVals[1] = .6f;
      hsbVals[2] = 0.6f;
      return Color.getHSBColor(hsbVals[0], hsbVals[1], hsbVals[2]);
   }

   private Color getSideDark(Color primary)
   {
      float hsbVals[] = new float[3];
      Color.RGBtoHSB(primary.getRed(), primary.getGreen(), primary.getBlue(), hsbVals);
      hsbVals[1] = 1.0f;
      hsbVals[2] = 0.3f;
      return Color.getHSBColor(hsbVals[0], hsbVals[1], hsbVals[2]);
   }

   private Point2D.Double getTargetPoint(Point2D.Double darkTarget, Point2D.Double lightTarget, double slope)
   {
      Point2D.Double  normalizedStart = new Point2D.Double();
      normalizedStart.setLocation(lightTarget.getX() - darkTarget.getX(), lightTarget.getY() - darkTarget.getY());

      Point2D.Double normalizedResult = new Point2D.Double();
      double normX = (Math.pow(slope, 2.0) * normalizedStart.getX() - (normalizedStart.getY() * slope)) / (Math.pow(slope, 2.0) - 1.0);
      normalizedResult.setLocation(normX, normX / slope);
      Point2D.Double result = new Point2D.Double();

      result.setLocation(normalizedResult.getX() + darkTarget.getX(), normalizedResult.getY() + darkTarget.getY());
      return result;
   }

   // Diagnostic method for drawing a mark on the graph
   private void drawMarker(Point2D.Double point, Graphics2D g2, Color color)
   {
      Stroke oldStroke = g2.getStroke();
      Paint oldPaint = g2.getPaint();
      g2.setPaint(color);
      //Shape line = new Line2D.Double(point, point);
      Shape marker = new Ellipse2D.Double(point.getX()-1.5, point.getY()-1.5, 3, 3);
      g2.fill(marker);
      g2.setPaint(oldPaint);
   }
}