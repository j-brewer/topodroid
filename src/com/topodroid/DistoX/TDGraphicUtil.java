/* @file TDGraphicUtil.java
 *
 * @author marco corvi
 * @date feb 2020
 *
 * @grief graphical utilities
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.graphics.Path;
import android.graphics.Canvas;
import android.graphics.Matrix;

class TDGraphicUtil
{
  static void drawGreenDot( Canvas canvas, Matrix matrix, SelectionPoint pt, float dot_radius )
  {
    Path path = new Path();
    if ( pt.mPoint != null ) { // line-point
      path.addCircle( pt.mPoint.x, pt.mPoint.y, dot_radius, Path.Direction.CCW );
    } else {  
      path.addCircle( pt.mItem.cx, pt.mItem.cy, dot_radius, Path.Direction.CCW );
    }
    path.transform( matrix );
    canvas.drawPath( path, BrushManager.highlightPaint2 );
  }

}
