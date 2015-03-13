/** @file PTId.java
 *
 * @author marco corvi
 * @date march 2010
 *
 * @brief PocketTopo file IO
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.topodroid.DistoX;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.io.PrintWriter;

// import android.util.Log;

class PTId
{
  static final int ID_UNDEFINED = 0x80000000;
  static final int ID_NUMBER = 0x80000001;
  static int ID_COUNT = 0;

  int _id; //!< id: 0x80000000 undefined, 
                   //!<     < 0        plain number + 0x80000001
                   //!<     >= 0       major<<16 | minor

  PTId() 
  {
    _id = ID_UNDEFINED;
  }


  int id() { return _id; }  

  void set( String str )
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_PTOPO, "PT ID set " + str ); 
    if ( str == null || str.length() == 0 ) {
      setUndef();
      return;
    }
    String[] vals = str.split( "." );
    if ( vals.length > 1 ) {
      int major = 0;
      int minor = 0;
      try {
        major = Integer.parseInt( vals[0] );
        minor = Integer.parseInt( vals[1] );
      } catch ( NumberFormatException e ) {
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "PTId::set major/minor parse error " + vals[0] + " " + vals[1] );
      }
      setMajorMinor( major, minor );
    } else {
      try { 
        int n = Integer.parseInt( str );
        setNumber( n + 0x80000001 );
      } catch ( NumberFormatException e ) { // should not happen
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "PTId::set setNumber parse error " + str );
        setNumber( ID_COUNT + 0x80000001 );
        ++ ID_COUNT;
      }
    }
  }

  private void setUndef() { _id = ID_UNDEFINED; }
  private void setNumber( int n ) { _id = n - 0x80000001; }
  private void setMajorMinor( int major, int minor ) 
  {
    _id = ( (major<<16)&0xffff0000 ) | ( minor & 0x0000ffff );
  }

  boolean isUndef() { return _id == ID_UNDEFINED; }
  boolean isNumber() { return _id != ID_UNDEFINED && _id < 0; }
  boolean isMajorMinor() { return _id != ID_UNDEFINED && _id >= 0; }

  int getNumber() 
  {
    if ( ! isNumber() ) return -1;
    return _id + 0x80000001;
  }

  // UNUSED
  // void xgetMajorMinor( int major, int minor )
  // {
  //   if ( isMajorMinor() ) {
  //     major = _id>>16;
  //     minor = _id & 0xffff; 
  //   }
  // }

  public String toString()
  {
    if ( isUndef() ) return new String("-");
    if ( isNumber() ) {
      // sprintf(tmp, "%d", _id + 0x80000001 ); // FIXME this is the correct form
      return new String("-");
    }
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.printf( "%04d.%d", _id>>16, _id & 0xffff );
    return sw.getBuffer().toString();
  }
    
  // -----------------------------------------------------------

  boolean equal( PTId other ) { return _id == other._id; }
  boolean not_equal( PTId other ) { return _id != other._id; }

  // -----------------------------------------------------------

  void read( FileInputStream fs )
  { 
    _id = PTFile.readInt( fs );
  }

  void write( FileOutputStream fs )
  {
    PTFile.writeInt( fs, _id );
  }

  // void print() 
  // { 
  //   if ( (int)_id == ID_UNDEFINED ) {
  //     Log.v( TopoDroidApp.TAG, "ID undef" );
  //   } else if ( _id < 0 ) {
  //     Log.v( TopoDroidApp.TAG, "ID number " + (_id + 0x80000001 ) );
  //   } else {
  //     Log.v( TopoDroidApp.TAG, "ID major " + (_id>>16) + " minor " + (_id & 0xffff) );
  //   }
  // }

}

