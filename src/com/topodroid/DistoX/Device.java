/* @file Device.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX device object
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.util.Log;

class Device
{
  // FIXME VirtualDistoX
  // static final String ZERO_ADDRESS = "00-00-00-00-00-00";

  String mAddress;  // device mac address
  String mModel;    // device model (type string)
  String mName;     // device name (X310 only)
  String mNickname; // device nickname
  int mType;        // device type
  private int mHead;
  private int mTail;

  final static int DISTO_NONE = 0; // device types - used as index in the arrays below
  final static int DISTO_A3   = 1;
  final static int DISTO_X310 = 2;
  // final static int DISTO_X000 = 3; // FIXME VirtualDistoX
  final static int DISTO_BLE5 = 4;

  final static String[] typeString = { "Unknown", "A3", "X310", "X000", "BLE5" };
  final static private String[] typeSimpleString = { "Unknown", "DistoX", "DistoX2", "DistoX0", "Ble5" };
  
  static String typeToString( int type ) { return typeString[ type ]; }

  static String modelToName( String model )
  {
    if ( model.startsWith("DistoX-") ) {
      return model.replace("DistoX-", "" );
    } 
    // if ( model.startsWith("Ble-") ) { // FIXME BLE
    //   return model.replace("Ble-", "" );
    // }
    return "-";
  }

  static int  stringToType( String model ) 
  {
    if ( model != null ) {
      // TDLog.Log( TDLog.LOG_DEBUG, "stringToType " + model );
      if ( model.equals( "BLE5" ) || model.startsWith( "DistoX-BLE" ) ) return DISTO_BLE5; // FIXME BLE
      if ( model.equals( "X310" ) || model.startsWith( "DistoX-" ) ) return DISTO_X310;
      if ( model.equals( "A3" ) || model.equals( "DistoX" ) ) return DISTO_A3;
      // if ( model.equals( "X000" ) || model.equals( "DistoX0" ) ) return DISTO_X000; // FIXME VirtualDistoX
    }
    return DISTO_NONE;
  }

  // -------------------------------------------------------------------------------

  // nickname can be null
  Device( String addr, String model, int h, int t, String name, String nickname )
  {
    mAddress = addr;
    mModel = model;
    mType = stringToType( model );

    if ( name == null ) {
      mName = "-";
    } else { 
      mName = name.trim();
      if ( mName.length() == 0 || mName.equals("null") ) mName = "-";
    }
    mNickname = nickname;
    mHead = h;
    mTail = t;
  }

  // nickname can be null
  Device( String addr, String model, String name, String nickname )
  {
    mAddress = addr;
    mModel = model;
    mType = stringToType( model );

    if ( name == null ) {
      mName = "-";
    } else { 
      mName = name.trim();
      if ( mName.length() == 0 || mName.equals("null") ) mName = "-";
    }

    mNickname = nickname;
    mHead = 0;
    mTail = 0;
  }

  public String getNickname()
  {
    if ( mNickname != null && mNickname.length() > 0 ) return mNickname;
    return mName;
  }

  // X310 is the only device that has firmware support 
  public boolean hasFirmwareSupport() { return mType == DISTO_X310; }


  public String toString() 
  { 
    if ( mNickname != null && mNickname.length() > 0 ) {
      return typeString[ mType ] + " " + mName + " " + mNickname;
    }
    return typeString[ mType ] + " " + mName + " " + mAddress;
  }

  String toSimpleString() { return typeSimpleString[ mType ] + " " + mName; }
  
}
