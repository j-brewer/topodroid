/* @file DrawingPath.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: paths (points, lines, and areas)
 * 
 * FixedPath path is a straight line between the two endpoints
 * GridPath paths are also straight lines
 * PreviewPath path is a line with "many" points
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.num.TDNum;
import com.topodroid.math.TDVector;
import com.topodroid.prefs.TDSetting;

import java.io.PrintWriter;
import java.io.DataOutputStream;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * direct/indirect subclasses:
 *   - DrawingPointLinePath
 *      - DrawingLinePath
 *      - DrawingAreaPath
 *   - DrawingPointPath
 *   - DrawingStationPath
 */

public class DrawingPath extends RectF
                         implements ICanvasCommand
{
  public static final int DRAWING_PATH_FIXED   = 0; // leg
  public static final int DRAWING_PATH_SPLAY   = 1; // splay
  public static final int DRAWING_PATH_GRID    = 2; // grid
  public static final int DRAWING_PATH_STATION = 3; // station point (user inserted)
  public static final int DRAWING_PATH_POINT   = 4; // drawing point
  public static final int DRAWING_PATH_LINE    = 5;
  public static final int DRAWING_PATH_AREA    = 6;
  public static final int DRAWING_PATH_NAME    = 7; // station name (from survey data)
  public static final int DRAWING_PATH_NORTH   = 8; // north line (5m long)
  public static final int DRAWING_PATH_GEO     = 9; // georeferenced point

  // static final int SPLAY_MODE_LINE  = 1;
  // static final int SPLAY_MODE_POINT = 2;
  // static int mSplayMode = SPLAY_MODE_LINE;

  // static int toggleSplayMode()
  // {
  //   if ( mSplayMode == SPLAY_MODE_LINE ) {
  //     mSplayMode = SPLAY_MODE_POINT;
  //   } else {
  //     mSplayMode = SPLAY_MODE_LINE;
  //   }
  //   TDLog.v("SPLAY MODE " + mSplayMode );
  //   return mSplayMode;
  // }

  // /** test whether to display splays as dots
  //  * @return true if splays as dots
  //  */
  // static public boolean splaysAsDots() { return mSplayMode == SPLAY_MODE_POINT; }

  protected Path mPath;
  protected Path mTransformedPath;

  Paint mPaint;          // drawing path paint
  public int mType;      // path type
  String mOptions;       // therion options
  public float x1, y1, x2, y2;  // endpoint scene coords  (not private just to write the scrap scale using mNorthLine )
  // private int dir; // 0 x1 < x2, 1 y1 < y2, 2 x2 < x1, 3 y2 < y1
  public DBlock mBlock;
  boolean mLandscape; // whether the canvas is in landscape presentation mode or not
  private float mCosine; // cosine value for splays (= cos of angle between splay and leg)
                         // x-sections: angle between splay and plane-normal
  String mPlotName;      // full plotname, ie, survey-plot (only for Overview window)
  public int mLevel;     // canvas levels flag (public for export classes)
  public int mScrap;     // plot scrap (public for export classes)

  public float cx, cy; // midpoint scene coords
  float deltaX, deltaY, len2; // used for Cave3D export
  // RectF mBBox;   // path boundig box (scene coords)

  void setCosine( float cosine ) { mCosine = cosine; }
  float getCosine() { return mCosine; }
  
  // FIXME-COPYPATH
  // // overridable
  // DrawingPath copyPath()
  // {
  //   DrawingPath ret = new DrawingPath( mType, mBlock );
  //   copyTo( ret );
  //   return ret;
  // }
  // 
  // // copy utility
  // protected void copyTo( DrawingPath ret )
  // {
  //   ret.left    = left; // RectF
  //   ret.right   = right;
  //   ret.bottom  = bottom;
  //   ret.top     = top; 
  //   ret.mOptions   = mOptions;
  //   ret.mPaint     = mPaint;
  //   ret.mLandscape = mLandscape;
  //   ret.mCosine    = mCosine;
  //   ret.mPlotName  = mPlotName;
  //   ret.mLevel     = mLevel;
  //   ret.mScrap     = mScrap;
  //   ret.x1 = x1;
  //   ret.y1 = y1;
  //   ret.x2 = x2;
  //   ret.y2 = y2;
  //   ret.cx = cx;
  //   ret.cy = cy;
  //   mPath = null;
  //   mTransformedPath = null;
  // }

  DrawingPath( int type, DBlock blk, int scrap )
  {
    mType    = type;
    mOptions = null;
    mBlock   = blk; 
    mScrap   = scrap;
    // mBBox  = new RectF();
    mPaint = BrushManager.errorPaint;
    // dir = 4;
    // x1 = y1 = 0.0f;
    // x2 = y2 = 1.0f;
    // dx = dy = 1.0f;
    mLandscape = false;
    mCosine = 1;
    mPlotName = null;
    mLevel = DrawingLevel.LEVEL_DEFAULT;
  }

  // boolean isDeletable( ) 
  // {
  //   return mType >= DRAWING_PATH_STATION && mType <= DRAWING_PATH_AREA; 
  // }

  void setPaintAlpha( boolean on ) { mPaint.setAlpha( (on ? 0xff : 0) ); }

  static boolean isReferenceType( int type ) 
  {
    return type < DRAWING_PATH_STATION || type >= DRAWING_PATH_NAME;
  }

  static boolean isDrawingType( int type ) 
  {
    return type >= DRAWING_PATH_STATION && type < DRAWING_PATH_NAME;
  }

  boolean isPoint() { return mType == DRAWING_PATH_POINT; }
  boolean isLine()  { return mType == DRAWING_PATH_LINE; }
  boolean isArea()  { return mType == DRAWING_PATH_AREA; }
  boolean isLineOrArea()  { return mType == DRAWING_PATH_LINE || mType == DRAWING_PATH_AREA; }

  float getX() { return cx; }
  float getY() { return cy; }

  void setBBox( float x1, float x2, float y1, float y2 )
  {
    // assert( x1 <= x2 ) && assert( y1 <= y2 )
    left   = x1;
    right  = x2;
    top    = y1;
    bottom = y2;
  }

  boolean intersects( RectF bbox )
  { 
    // return true;
    if ( bbox == null ) return true;
    return (bbox.right >= left)
            && (bbox.left <= right)
            && (bbox.top <= bottom)
            && (bbox.bottom >= top);
  }
  
  /** flip horizontally
   * @param z   unused
   * @note from ICanvasCommand
   */
  public void flipXAxis( float z )
  {
    float dx = 2 * DrawingUtil.CENTER_X;
    float offx = dx - 2 * cx; // OK for non-orientable points
    cx = dx - cx;
    x1 = dx - x1;
    x2 = dx - x2;
    float r1 = dx - left;
    left = dx - right;
    right = r1;
    boolean flip_path = false;
    if ( mType == DRAWING_PATH_POINT ) {
      DrawingPointPath dpp = (DrawingPointPath)this;
      if ( dpp.mOrientation != 0 ) {
        dpp.mOrientation = 360 - dpp.mOrientation;
        flip_path = true;
        offx = dx;
      // } else {
      }
      // TDLog.v( "x0 " + x0 + " offx " + offx + " Cx " + oldcx + " -> " + cx);
      if ( mPath != null ) {
        if ( flip_path ) {
          float[] m; // { -1, 0, 0, 0, 1, 0, 0, 0, 1 };
          m = new float[9];
          android.graphics.Matrix mat = new android.graphics.Matrix();
          mat.getValues( m );
          m[0] = -m[0];
          mat.setValues( m );
          mPath.transform( mat );
        }
        mPath.offset( offx, 0 );
      }
    } else if ( mType == DRAWING_PATH_STATION ) {
      if ( mPath != null ) {
        mPath.offset( offx, 0 );
      }
    }
  }

  /** get the path color (or white)
   * @return the color of the path
   */
  public int color() { return ( mPaint != null )? mPaint.getColor() : 0xffffffff; }

  // void log()
  // {
  //   TDLog.v("PATH " + "Path " + x1 + " " + y1 + "   " + x2 + " " + y2 );
  // }

  /** make the path copying from another path
   * @param path   the path to copy or null for an empty path
   * @param m      transform matrix
   * @param off_x  offset X
   * @param off_y  offset Y
   */
  void makePath( Path path, Matrix m, float off_x, float off_y )
  {
    // TDLog.v("make offset path - type " + mType + " at " + x1 + " " + y1 );
    if ( path != null ) {
      mPath = new Path( path );
      mPath.transform( m );
    } else { // SPECIAL_DOT
      mPath = new Path();
      // mPath.addCircle( 0, 0, TDSetting.mLineThickness, Path.Direction.CCW );
      float d = TDSetting.mLineThickness / 2.0f;
      mPath.moveTo( d,  d );
      mPath.lineTo(-d, -d );
      mPath.moveTo( d, -d );
      mPath.lineTo(-d,  d );
    }
    mPath.offset( off_x, off_y ); // FIXME-PATH this was only for path != null
  }

  void makePath( float x1, float y1, float x2, float y2 )
  {
    // TDLog.v("make endpoint path - type " + mType + " at " + x1 + " " + y1 );
    mPath = new Path( );
    mPath.moveTo( x1, y1 );
    mPath.lineTo( x2, y2 );
  }

  // implemented in DrawingUtil
  // void makeDotPath( float x2, float y2, float off_x, float off_y )
  // {
  //   mPath = new Path();
  //   mPath.addCircle( x2, y2, TDSetting.mLineThickness, Path.Direction.CCW );
  //   mPath.offset( off_x, off_y );
  //   // mPaint = BrushManager.mDotPaint; 
  // }

  void makeStraightPath( float x1, float y1, float x2, float y2, float off_x, float off_y )
  {
    // TDLog.v("make straight path - type " + mType + " at " + x1 + " " + y1 );
    mPath = new Path();
    mPath.moveTo( x1, y1 );
    mPath.lineTo( x2, y2 );
    mPath.offset( off_x, off_y );
  }

  void pathAddLineTo( float x, float y )
  {
    mPath.lineTo( x, y );
    mPath.moveTo( x+5, y+5 );
    mPath.lineTo( x-5, y-5 );
    mPath.moveTo( x+5, y-5 );
    mPath.lineTo( x-5, y+5 );
    mPath.moveTo( x, y );
    setEndPoints( x1, y1, x, y );
  }

  void makeTrianglePath( float x, float y, float r, float off_x, float off_y )
  {
    float r2 = r * 1.732f;
    mPath = new Path();
    mPath.moveTo( x1-r, y1 );
    mPath.lineTo( x1+r, y1 );
    mPath.lineTo( x1, y1-r2 );
    mPath.lineTo( x1-r, y1 );
    mPath.offset( off_x, off_y );
  }

  void setPathPaint( Paint paint ) { mPaint = paint; }

  // x10, y10 first endpoint scene coords
  // x20, y20 second endpoint scene coords
  void setEndPoints( float x10, float y10, float x20, float y20 )
  {
    x1 = x10;
    y1 = y10;
    x2 = x20;
    y2 = y20;
    // dir = ( Math.abs( x2-x1 ) >= Math.abs( y2-y1 ) )?
    //          ( (x2 > x1)? 0 : 2 ) : ( (y2>y1)? 1 : 3 );
    // d = Math.sqrt( (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1) );
    cx = (x20+x10)/2;
    cy = (y20+y10)/2;
    if ( x1 < x2 ) {
      left  = x1;
      right = x2;
    } else {
      left  = x2;
      right = x1;
    }
    if ( y1 < y2 ) {
      top    = y1;
      bottom = y2;
    } else {
      top    = y2;
      bottom = y1;
    }
  }

  // intersection of 
  //    x = x1 + t*(x2-x1)
  //    y = y1 + t*(y2-y1)
  // and
  //    x = x10 + s*(x20-x10)
  //    y = y10 + s*(y20-y10)
  //
  // t * (x2-x1) - s*(x20-x10) = x10 - x1;
  // t * (y2-y1) - s*(y20-y10) = y10 - y1;
  // inverse
  // t   | -(y20-y10)  +(x20-x10) | | x10 - x1 |
  // s   | -(y2-y1)    +(x2-x1)   | | y10 - y1 |
  //
  float intersectSegment( float x10, float y10, float x20, float y20 )
  {
    float det = -(x2-x1)*(y20-y10) + (x20-x10)*(y2-y1);
    float t = ( -(y20-y10)*(x10 - x1) + (x20-x10)*(y10 - y1) )/det;
    float s = ( -(y2-y1)*(x10 - x1) + (x2-x1)*(y10 - y1) )/det;
    // TDLog.v( "intersection tt "+ t );
    if ( t > 0.0f && t < 1.0f && s > 0.0f && s < 1.0f ) return t;
    return -1;
  }


  // DrawingPath by default does not shift nor scale
  void shiftBy( float dx, float dy ) { }

  void scaleBy( float z, Matrix m ) { }

  void affineTransformBy( float[] mm, Matrix m ) { }

  // by default does not rotate (return false)
  boolean rotateBy( float dy ) { return false; }

  // from ICanvasCommand
  public void shiftPathBy( float dx, float dy ) 
  {
    x1 += dx;
    y1 += dy;
    x2 += dx;
    y2 += dy;
    cx += dx;
    cy += dy;
    mPath.offset( dx, dy );
    left   += dx;
    right  += dx;
    top    += dy;
    bottom += dy;
  }

  // from ICanvasCommand
  public void scalePathBy( float z, Matrix m )
  {
    x1 *= z;
    y1 *= z;
    x2 *= z;
    y2 *= z;
    cx *= z;
    cy *= z;
    mPath.transform( m );
    left   *= z;
    right  *= z;
    top    *= z;
    bottom *= z;
  }

  // x' = a x + b y + c
  // y' = d x + e y + f
  public void affineTransformPathBy( float[] mm, Matrix m )
  {
    float x  = mm[0] * x1 + mm[1] * y1 + mm[2];
          y1 = mm[3] * x1 + mm[4] * y1 + mm[5];
          x1 = x;
          x  = mm[0] * x2 + mm[1] * y2 + mm[2];
          y2 = mm[3] * x2 + mm[4] * y2 + mm[5];
          x2 = x;
          x  = mm[0] * cx + mm[1] * cy + mm[2];
          cy = mm[3] * cx + mm[4] * cy + mm[5];
          cx = x;
    mPath.transform( m );
    float xlt = mm[0] * left + mm[1] * top + mm[2];
    float ylt = mm[3] * left + mm[4] * top + mm[5];
    float xlb = mm[0] * left + mm[1] * bottom + mm[2];
    float ylb = mm[3] * left + mm[4] * bottom + mm[5];
    float xrt = mm[0] * right + mm[1] * top + mm[2];
    float yrt = mm[3] * right + mm[4] * top + mm[5];
    float xrb = mm[0] * right + mm[1] * bottom + mm[2];
    float yrb = mm[3] * right + mm[4] * bottom + mm[5];
    left   = (xlt < xlb)? xlt : xlb;
    right  = (xrt > xrb)? xrt : xrb;
    top    = (ylt < yrt)? ylt : yrt;
    bottom = (ylb > yrb)? ylb : yrb;
  }

  // this is used only by the Selection 
  float distanceToPoint( float x, float y )
  {
    // if ( mBlock == null ) return 1000.0f; // a large number
    double dx = x - cx;
    double dy = y - cy;
    return (float)( Math.sqrt( dx*dx + dy*dy ) );
  }

  // int type() { return mType; }

  public void draw( Canvas canvas )
  {
    drawPath( mPath, canvas );
  }

  public void draw( Canvas canvas, RectF bbox )
  {
    if ( intersects( bbox ) ) {
      if ( mType == DRAWING_PATH_AREA ) {
        // TDLog.Log( TDLog.LOG_PLOT, "DrawingPath::draw area" );
        mPath.close();
      }
      drawPath( mPath, canvas );
    }
  }

  // N.B. canvas is guaranteed ! null
  public void draw( Canvas canvas, Matrix matrix, float scale, RectF bbox )
  {
    if ( intersects( bbox ) ) 
    {
      mTransformedPath = new Path( mPath );
      mTransformedPath.transform( matrix );
      drawPath( mTransformedPath, canvas );
    }
  }

  // used in executeAll
  boolean isBlockRecent( )
  {
    return mBlock != null && mBlock.isRecent();
  }

  // used in executeAll to draw yellow extend control segnment
  int getBlockExtend( )
  {
    return ( mBlock == null )? 0 : mBlock.getIntExtend();
  }
 
  // setSplayExtend is used for the plan view
  // cosine = cos(angle_splay-leg)
  // called by DrawingCommandManager
  void setSplayPaintPlan( DBlock blk, float cosine, Paint h_paint, Paint v_paint ) { }
  
  // setSplayClino is used for the profile view
  void setSplayPaintProfile( DBlock blk, Paint h_paint, Paint v_paint ) { }

  /* FIXME apparently this can be called when mPaint is still null
   *        and when fixedBluePaint is null
   *
   * NOTE DrawingAreaPath overrides this
   */
  void drawPath( Path path, Canvas canvas )
  {
    if (    mType == DRAWING_PATH_SPLAY  // FIXME_X_SPLAY
         && mBlock != null ) {
      if ( TDSetting.mSplayColor ) {
        if ( mBlock.isRecent( ) ) { 
          canvas.drawPath( path, BrushManager.lightBluePaint );
          return;
        }
        if ( TDLevel.overExpert ) { // splay user-color only at tester level
          Paint paint = mBlock.getPaint();
          if ( paint != null ) {
            canvas.drawPath( path, paint );
            return;
          }
        }
      }
    } 
    if ( mPaint != null ) canvas.drawPath( path, mPaint );
  }

  void setOrientation( double angle ) { }

  String toTherion( ) { return null; } // FIXME

  void toDataStream( DataOutputStream dos, int scrap ) { TDLog.Error( "ERROR DrawingPath toDataStream with scrap executed"); }

  // void toCsurvey( PrintWriter pw, String survey, String cave, String branch, String bind /* , DrawingUtil mDrawingUtil */ ) { }
  void toTCsurvey( PrintWriter pw, String survey, String cave, String branch, String bind /* , DrawingUtil mDrawingUtil */ ) { }

  // ------ ICanvasCommand interface
  /** @return the type of the command, namely 0
   */
  public int commandType() { return 0; }

  // void undoCommand() { // TODO this would be changed later }

  // --------- from ICanvasCommand
  /** compute the bounding box - it does nothing
   * @param bounds   bounding box (unaffected)
   * @param b        not used (see android.graphics.Path)
   */
  public void computeBounds( RectF bound, boolean b ) 
  { 
    mPath.computeBounds( bound, b );
  }

  // void transform( Matrix matrix ) { mPath.transform( matrix ); }

  // --------- Therion options
  /** add an option to the options string
   * @param option  new string to add to the options
   */
  void addOption( String option ) 
  {
    if ( option == null ) return;
    if ( mOptions == null ) {
      mOptions = option;
    } else {
      mOptions = mOptions + " " + option;
    }
  }

  /** @return the (string) value of an option, or null if the key is not found
   * @param key  option key, it must be not null and start with '-'
   */
  public String getOption( String key )
  {
    if ( mOptions == null ) return null;
    String[] vals = mOptions.split(" ");
    int len = vals.length;
    for ( int k = 0; k < len; ++k ) {
      if ( key.equals( vals[k] ) ) {
        while ( ++k < len ) if ( vals[k].length() > 0 ) return vals[k];
        break;
      }
    }
    return null;
  }

  /** @return the options string, or empty if it is null
   */
  public String getOptionString() { return ( mOptions == null )? "" : mOptions; }

  /** @return the options (string)
   */
  public String getOptions() { return mOptions; }

  /** set the options string
   * @param options   new options string
   */
  void setOptions( String options ) { mOptions = options; }

  void prepareCave3D() 
  {
    deltaX = x2 - x1;
    deltaY = y2 - y1;
    len2   = deltaX * deltaX + deltaY * deltaY;
  }

  void toCave3D( PrintWriter pw, int type, DrawingCommandManager cmd, TDNum num ) { }

  void toCave3D( PrintWriter pw, int type, TDVector V1, TDVector v2 ) { }

  /** @return a vector in world frame, V1 * X0 + V2 * Y0
   * @param x   X coord of ...
   * @param y   Y coord of ...
   * @param V1  ...
   * @param V2  ...
   */
  static TDVector getCave3D( float x, float y, TDVector V1, TDVector V2 )
  {
    float x0 = DrawingUtil.sceneToWorldX( x, y );
    float y0 = DrawingUtil.sceneToWorldY( x, y );
    return V1.times(x0).plus( V2.times(y0) );
  }

}
