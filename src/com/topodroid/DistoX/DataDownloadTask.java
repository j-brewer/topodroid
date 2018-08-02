/* @file DataDownloadTask.java
 *
 * @author marco corvi
 * @date feb 2012
 *
 * @brief TopoDroid batch data-download task
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import java.util.ArrayList;

// import android.widget.Toast;
import android.os.AsyncTask;
// import android.os.Handler;

// import android.util.Log;

class DataDownloadTask extends AsyncTask< String, Integer, Integer >
{
  private final TopoDroidApp mApp; // FIXME LEAK
  private static DataDownloadTask running = null;
  // private ILister mLister;
  private final ListerHandler mLister; // FIXME_LISTER
  private final GMActivity mGMactivity;

  DataDownloadTask( TopoDroidApp app, ListerHandler /* ILister */ lister, GMActivity gm_activity ) // FIXME_LISTER
  {
    // TDLog.Error( "DataDownloadTask cstr" );
    // Log.v("DistoX", "data download task cstr");
    mApp = app;
    mLister = lister;
    mGMactivity = gm_activity;
  }

// -------------------------------------------------------------------
  @Override
  protected Integer doInBackground( String... statuses )
  {
    if ( mGMactivity != null ) {
      int algo = mGMactivity.getAlgo();
      if ( algo == CalibInfo.ALGO_AUTO ) { 
        algo = mApp.getCalibAlgoFromDevice();
        if ( algo < CalibInfo.ALGO_AUTO ) {
          // TDToast.make( R.string.device_algo_failed );
          algo = CalibInfo.ALGO_LINEAR; 
        }
        mApp.updateCalibAlgo( algo );
        mGMactivity.setAlgo( algo );
      }
    }

    if ( ! lock() ) return null;
    // long time = System.currentTimeMillis();
    return mApp.downloadDataBatch( mLister );
    // time = System.currentTimeMillis() - time;
    // Log.v("DistoX", "READ " + nRead + " data in " + time + " msec");

    // if ( nRead < 0 ) {
    //   TDToast.make( mApp.DistoXConnectionError[ -nRead ] );
    // TDLog.Error( "doInBackground read " + nRead );
  }

  // @Override
  // protected void onProgressUpdate( Integer... values)
  // {
  //   super.onProgressUpdate( values );
  //   // TDLog.Log( TDLog.LOG_COMM, "onProgressUpdate " + values );
  // }

  @Override
  protected void onPostExecute( Integer res )
  {
    // TDLog.Log( TDLog.LOG_COMM, "onPostExecute res " + res );
    if ( res != null ) {
      int r = res.intValue();
      mLister.refreshDisplay( r, true );  // true: toast a message
    }
    mApp.mDataDownloader.setDownload( false );
    mApp.mDataDownloader.notifyConnectionStatus( false );
    unlock();
    // Log.v("DistoX", "data download task post exec");
  }

  private synchronized boolean lock()
  {
    // Log.v("DistoX", "data download task lock: running is " 
    //               + ( (running == null )? "null" : (running == this)? "this" : "other") );
    if ( running != null ) return false;
    running = this;
    return true;
  }

  private synchronized void unlock()
  {
    // Log.v("DistoX", "data download task unlock: running is " 
    //               + ( (running == null )? "null" : (running == this)? "this" : "other") );
    if ( running == this ) running = null;
  }

}
