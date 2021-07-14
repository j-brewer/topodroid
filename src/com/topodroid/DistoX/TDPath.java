	/* @file TDPath.java
	 *
	 * @author marco corvi
	 * @date jan 2015 
	 *
	 * @brief TopoDroid application paths
	 * --------------------------------------------------------
	 *  Copyright This software is distributed under GPL-3.0 or later
	 *  See the file COPYING.
	 * --------------------------------------------------------
	 */
	package com.topodroid.DistoX;

	import com.topodroid.utils.TDLog;
	import com.topodroid.utils.TDFile;

	import android.util.Log;

	import android.os.Build;
	// import android.provider.DocumentsContract;

	import java.io.File;
	import java.io.FileFilter;
	import java.io.FilenameFilter;

	import java.util.List;
	import java.util.Locale;

	import android.content.Context;
	import android.os.Environment;

	public class TDPath
	{
	  // whether not having ANDROID 10
	  final static public boolean BELOW_ANDROID_4  = ( Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT ); // API_19
	  final static public boolean BELOW_ANDROID_5  = ( Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP ); // API_21
	  final static public boolean BELOW_ANDROID_10 = ( Build.VERSION.SDK_INT <= Build.VERSION_CODES.P );
	  final static public boolean BELOW_ANDROID_11 = ( Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q );

	  final static int NR_BACKUP = 5;
	  final static String BCK_SUFFIX = ".bck";

	  final static String C3D = ".c3d";
	  final static String CSN = ".csn";  // CaveSniper
	  final static String CSV = ".csv";
	  final static String DAT = ".dat";
	  final static String SVX = ".svx"; // Survex
	  final static String TDR = ".tdr";
	  final static private String TMP = ".tmp";

	  final static String TOP = ".top"; // PockeTopo
	  final static String TH  = ".th";
	  final static String TRO = ".tro"; // VisualTopo
	  final static String TROX = ".trox"; // VisualTopo
	  final static String TXT = ".txt";
	  final static String ZIP = ".zip";

	  final static String THCONFIG = ".thconfig";
	  // final static String TDCONFIG = ".tdconfig";

	  static String getSymbolPointDirname() { return "point"; } // "symbol/point"
	  static String getSymbolLineDirname()  { return "line"; }  // "symbol/line"
	  static String getSymbolAreaDirname()  { return "area"; }  // "symbol/area"

	  // ------------------------------------------------------------
	  // PATHS

	  // if BUILD
	  // If PATH_BASEDIR is left null the path is set in the method setPaths():
	  //    this works with Android-10 but the data are erased when TopoDroid is uninstalled
	  //    because the path is Android/data/com.topodroid.DistoX/files
	  // With "/sdcard" they remain
          /*
	  private static final String EXTERNAL_STORAGE_PATH_10 = Environment.getExternalStorageDirectory().getAbsolutePath();
	  private static final String EXTERNAL_STORAGE_PATH_11 =
	    BELOW_ANDROID_4 ? null : Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
	  private static final String EXTERNAL_STORAGE_PATH =  // app base path
	    BELOW_ANDROID_4 ? EXTERNAL_STORAGE_PATH_10
	      : ( TDFile.hasTopoDroidFile( EXTERNAL_STORAGE_PATH_11, "TopoDroid" ) )? EXTERNAL_STORAGE_PATH_11
		: BELOW_ANDROID_10 ? EXTERNAL_STORAGE_PATH_10
		  : ( BELOW_ANDROID_11  && TDFile.hasTopoDroidFile( EXTERNAL_STORAGE_PATH_10, "TopoDroid") )? EXTERNAL_STORAGE_PATH_10 
		    : EXTERNAL_STORAGE_PATH_11;
          */

	  // private static String PATH_BASEDIR  = EXTERNAL_STORAGE_PATH;
	  private static String PATH_BASEDIR  = TDFile.getExternalDir(null).getPath();
	  private static String PATH_APPNAME  = "TopoDroid/";
	  private static String PATH_APPBASE  = PATH_BASEDIR + "/TopoDroid";
	  private static String PATH_DEFAULT  = PATH_APPBASE;
	  private static String PATH_TDCONFIG; 
	  // private static String PATH_THCONFIG; // UNUSED
	  private static String APP_FOTO_PATH;  //  = PATH_APPBASE + "/photo/";
	  private static String APP_AUDIO_PATH; //  = PATH_APPBASE + "/audio/";
	  private static String APP_NOTE_PATH;  //  = PATH_APPBASE + "/note/";
	  private static String PATH_TDR;       //  = PATH_APPBASE + "/tdr/";
	  private static String PATH_ZIP;       //  = PATH_APPBASE + "/zip/";

	  // final static Object mTherionLock   = new Object(); // FIXME-THREAD_SAFE
	  final static Object mXSectionsLock = new Object();
	  final static Object mSelectionLock = new Object();
	  final static Object mCommandsLock  = new Object();
	  final static Object mStationsLock  = new Object();
	  final static Object mShotsLock     = new Object();
	  final static Object mGridsLock     = new Object();
	  // final static Object mFixedsLock    = new Object();
	  // final static Object mSelectedLock  = new Object();

	  static String getDatabase() { return PATH_APPBASE + "/distox14.sqlite"; }

	  // static String getDeviceDatabase() { return PATH_DEFAULT + "device10.sqlite"; }
	  // public static String getPacketDatabase() { return PATH_DEFAULT + "packet10.sqlite"; }
	  
	  // when this is called basedir exists and is writable
	  static boolean checkBasePath( String path )
	  {
	    File dir = TDFile.getExternalDir( path ); 
	    if ( ! dir.exists() ) {
	      if ( ! dir.mkdirs() ) TDLog.Error("mkdir error " + path );
	    }

	    boolean ret = false;
	    try {
	      ret = dir.exists() && dir.isDirectory() && dir.canWrite();
	    } catch ( SecurityException e ) { }
	    Log.v( "DistoX", "check base path: <" + path + ">: " + ret );
	    return ret;
	  }

	  public static String getBaseDir() { return PATH_BASEDIR; }
	 
	  static void setPaths( String path /*, String base */ )
	  {
	    // Log.v("DistoX", "set paths [0]: path " + path + " base " + base );

	    if ( path == null || ! path.toLowerCase().startsWith( "topodroid" ) ) return;

	    File dir = TDFile.getExternalDir( path ); // DistoX-SAF
	    // Log.v( "DistoX", "set paths [4]. Dir " + dir.getPath()  );
	    try {
	      if ( dir.exists() || dir.mkdirs() ) {
		if ( dir.isDirectory() && dir.canWrite() ) {
		  PATH_APPBASE = PATH_BASEDIR + "/" + path;
		  PATH_APPNAME = path;
		  PATH_TDCONFIG  = PATH_APPBASE + "/thconfig"; checkFilesystemDirs( PATH_TDCONFIG );
		  PATH_TDR       = PATH_APPBASE + "/tdr"     ; checkFilesystemDirs( PATH_TDR );
		  APP_NOTE_PATH  = PATH_APPBASE + "/note"    ; checkFilesystemDirs( APP_NOTE_PATH );
		  APP_FOTO_PATH  = PATH_APPBASE + "/photo"   ; checkFilesystemDirs( APP_FOTO_PATH );
		  APP_AUDIO_PATH = PATH_APPBASE + "/audio"   ; checkFilesystemDirs( APP_AUDIO_PATH );
		  PATH_ZIP       = PATH_APPBASE + "/zip"     ; checkFilesystemDirs( PATH_ZIP );
		}
	      }
	    } catch ( SecurityException e ) { 
	      Log.v("DistoX-PATH", "ext storage security error " + e.getMessage() );
	    }
	  }

	  static void clearSymbols( )
	  {
	    clearSymbolsDir( "point" );
	    clearSymbolsDir( "line" );
	    clearSymbolsDir( "area" );
	  }  

	  public static void checkPath( String filename )
	  {
	    if ( filename == null ) {
	      // Log.v("DistoX", "check path: null string" );
	      return;
	    }
	    checkPath( TDFile.getTopoDroidFile( filename ) ); // DistoX-SAF
	  }

	  // ------------------------------------------------------------------

	  public static File[] scanTdconfigDir() // DistoX-SAF
	  {
	    File dir = TDFile.getTopoDroidFile( PATH_TDCONFIG );
	    if ( ! dir.exists() ) {
	      if ( ! dir.mkdirs() ) {
		TDLog.Error("mkdir error");
		return null;
	      }
	    }
	    FilenameFilter filter = new FilenameFilter() {
	       public boolean accept( File dir, String name ) {
		 return name.endsWith( "tdconfig" );
	       }
	    };
	    return dir.listFiles( filter );
	  }

	  // ------------------------------------------------------------------
	  // FILE NAMES

	  static String getSqlFile() { return PATH_APPBASE + "/survey.sql"; }

	  public static String getPathBase() { return PATH_APPBASE; }

	  static String getManifestFile() { return PATH_APPBASE + "/manifest"; }
	  // static String getSymbolFile( String name ) { return name; }

	  static boolean hasTdrDir() { return TDFile.hasTopoDroidFile( PATH_TDR ); } // DistoX-SAF

	  static File getTdrDir() { return TDFile.makeTopoDroidDir( PATH_TDR ); } // DistoX-SAF

          // used by Archiver
	  static String getDirFile( String name )        { return PATH_APPBASE + "/" + name; }

	  public static String getZipFile( String name ) { return PATH_ZIP + "/" + name; }
	  public static String getTdrFile( String name ) { return PATH_TDR + "/" + name; }

	  public static String getTdconfigDir( ) { return PATH_TDCONFIG; }
	  public static String getTdconfigFile( String name ) { return PATH_TDCONFIG + name; }
	  // public static String getThconfigDir( ) { return PATH_THCONFIG; }
	  // public static String getSurveyThconfigFile( String survey ) { return PATH_THCONFIG + survey + THCONFIG; }

	  public static String getManFileName( String name ) { return "man/" + name; }

	  static String getNoteFile( String name )              { return APP_NOTE_PATH  + "/" + name; }
	  static String getJpgDir( String dir )                 { return APP_FOTO_PATH  + "/" + dir; }
	  static String getJpgFile( String dir, String name )   { return APP_FOTO_PATH  + "/" + dir + "/" + name; }
	  static String getAudioDir( String dir )               { return APP_AUDIO_PATH + "/" + dir; }
	  static String getAudioFile( String dir, String name ) { return APP_AUDIO_PATH + "/" + dir + "/" + name; }

	  static String getSurveyPlotTdrFile( String survey, String name ) { return PATH_TDR + "/" + survey + "-" + name + ".tdr" ; }

	  public static String getSurveyNoteFile( String title ) { return getPathname( APP_NOTE_PATH, title, TXT ); }
	  public static String getTdrFileWithExt( String name )  { return getPathname( PATH_TDR, name, TDR ); }
	  public static String getSurveyZipFile( String survey ) { return getPathname( PATH_ZIP, survey, ZIP ); }

	  public static File[] getBinFiles() { return getExternalFiles( "bin" ); }

	  public static File[] getCalibFiles() { return getExternalFiles( "ccsv" ); } // DistoX-SAF

	  // used only by CWDActivity to list "topodroid" dirs
	  static File[] getTopoDroidFiles( String basename ) // DistoX-SAF
	  {
	    File dir = TDFile.getTopoDroidFile( basename );
	    return dir.listFiles( new FileFilter() {
	      public boolean accept( File pathname ) { 
		if ( ! pathname.isDirectory() ) return false;
		return ( pathname.getName().toLowerCase(Locale.US).startsWith( "topodroid" ) );
	      }
	    } );
	  }

	  // NOTA BENE extensions include the dot, eg, ".th"
	  static final String[] IMPORT_EXT        = { TH, TOP, DAT, TRO, CSN, SVX };
	  static final String[] IMPORT_EXT_STREAM = { TOP, ZIP };
	  static final String[] IMPORT_EXT_READER = { TH, DAT, TRO, TROX, CSN, SVX };

	  // static File[] getImportFiles() // DistoX-SAF
	  // { 
	  //   return getFiles( PATH_IMPORT, IMPORT_EXT );
	  // }

	  static String checkImportTypeStream( String ext ) { return checkType( ext, IMPORT_EXT_STREAM ); }
	  static String checkImportTypeReader( String ext ) { return checkType( ext, IMPORT_EXT_READER ); }

	  static String[] getImportTypes() { return new String[] { TH, TOP, DAT, TRO, TROX, CSN, SVX }; }

	  static File[] getZipFiles() { return getFiles( PATH_ZIP, new String[] { ZIP } ); } // DistoX-SAF

	  // static String getSurveyPhotoFile( String survey, String name ) { return APP_FOTO_PATH + "/" + survey + "/" + name; }

	  static String getSurveyPhotoDir( String survey ) { return APP_FOTO_PATH  + "/" + survey; }
	  static String getSurveyAudioDir( String survey ) { return APP_AUDIO_PATH + "/" + survey; }

	  static String getSurveyJpgFile( String survey, String id )
	  {
	    TDFile.makeTopoDroidDir( APP_FOTO_PATH + "/" + survey + "/" );
	    return APP_FOTO_PATH + "/" + survey + "/" + id + ".jpg";
  }

  public static String getSurveyJpgFilename( String survey, String id )
  {
    return survey + "/" + id + ".jpg";
  }

  static String getSurveyWavFile( String survey, String id )
  {
    TDFile.makeTopoDroidDir( APP_AUDIO_PATH + "/" + survey );
    return APP_AUDIO_PATH + "/" + survey + "/" + id + ".wav";
  }
  
  public static String getSurveyWavFilename( String survey, String id )
  {
    return survey + "/" + id + ".wav";
  }

  static void rotateBackups( String filename, int rotate ) // filename has suffix BCK_SUFFIX
  {
    if ( rotate <= 0 ) return;
    for ( int i=rotate-1; i>0; --i ) {
      TDFile.moveFile( filename + Integer.toString(i-1), filename + Integer.toString(i) );
    }
    TDFile.moveFile( filename, filename + "0"  );
  }

  static void renamePlotFiles( String old_name, String new_name )
  {
    String old_tdr = TDPath.getTdrFile( old_name + ".tdr" );
    String new_tdr = TDPath.getTdrFile( new_name + ".tdr" );
    TDFile.renameFile( old_tdr, new_tdr );
    old_tdr = old_tdr + TDPath.BCK_SUFFIX;
    new_tdr = new_tdr + TDPath.BCK_SUFFIX;
    for ( int i=0; ; ++i ) {
      File file1 = TDFile.getTopoDroidFile( old_tdr + Integer.toString(i) ); // DistoX-SAF
      File file2 = TDFile.getTopoDroidFile( new_tdr + Integer.toString(i) );
      if ( ( ! file1.exists() ) || file2.exists() ) break;
      if ( ! file1.renameTo( file2 ) ) TDLog.Error("file rename failed");
    }
  }

  static void deleteSurveyFiles( String survey )
  {
    File imagedir = TDFile.getTopoDroidFile( getSurveyPhotoDir( survey ) ); // DistoX-SAF
    if ( imagedir.exists() ) {
      File[] files = imagedir.listFiles();
      if ( files != null ) {
        for (File f : files) if (!f.delete()) TDLog.Error("file delete error");
      }
      if ( ! imagedir.delete() ) TDLog.Error("Dir delete error");
    }
    TDFile.deleteFile( getSurveyNoteFile( survey ) );
    // deleteSurveyExportFiles( survey )
  }

  static void deleteBackups( String filename ) // filename has suffix BCK_SUFFIX
  {
    TDFile.deleteFile( filename );
    for ( int i=NR_BACKUP-1; i>=0; --i ) {
      TDFile.deleteFile( filename + Integer.toString(i) );
    }
  }

  static void deletePlotFileWithBackups( String filename )
  {
    TDFile.deleteFile( filename );
    String filepath = filename + TDPath.BCK_SUFFIX;
    TDFile.deleteFile( filepath );
    for ( int i = 0; i < NR_BACKUP; ++i ) {
      filepath = filename + TDPath.BCK_SUFFIX + Integer.toString(i);
      TDFile.deleteFile( filepath );
    }
  }

  static void deleteSurveyPlotFiles( String survey, List< PlotInfo > plots )
  {
    for ( PlotInfo p : plots ) {
      // String filename = getSurveyPlotTh2File( survey, p.name );
      // TDFile.deleteFile( filename );
      // deleteBackups( filename + BCK_SUFFIX );
      String filename = getSurveyPlotTdrFile( survey, p.name );
      TDFile.deleteFile( filename );
      deleteBackups( filename + BCK_SUFFIX );
      // deleteSurveyPlotExportFiles( survey, p.name )
    }
  }

  // -------------- PRIVATE ---------------------------------------------------------

  // private static String noSpaces( String s )
  // {
  //   return ( s == null )? null 
  //     : s.trim().replaceAll("\\s+", "_").replaceAll("/", "-").replaceAll("\\*", "+").replaceAll("\\\\", "");
  // }

  private static void setDefault()
  {
    PATH_DEFAULT = PATH_BASEDIR + "/TopoDroid/";
    PATH_APPBASE = PATH_DEFAULT;
    PATH_APPNAME = "TopoDroid";
    // Log.v( "DistoX", "set default path <" + PATH_DEFAULT + ">" );
  }

  private static void clearSymbolsDir( String dirname )
  {
    // Log.v("DistoX", "clear " + dirname );
    File dir = TDFile.getExternalDir( dirname );
    File [] files = dir.listFiles();
    if ( files == null ) return;
    for ( int i=0; i<files.length; ++i ) {
      if ( files[i].isDirectory() ) continue;
      if ( ! files[i].delete() ) TDLog.Error("File " + files[i].getPath() + " delete failed ");
    }
  }

  // private static File[] getFiles( String dirname, final String extension )
  // {
  //   File dir = TDFile.getTopoDroidFile( dirname );
  //   if ( dir.exists() ) {
  //     return dir.listFiles( new FileFilter() {
  //         public boolean accept( File pathname ) { return pathname.getName().endsWith( extension ); }
  //       } );
  //   }
  //   return null;
  // }

  private static void checkPath( File fp ) // DistoX-SAF
  {
    if ( fp == null ) {
      // Log.v("DistoX", "check path: file null " );
      return;
    }
    if ( fp.exists() ) {
      // Log.v("DistoX", "check path: file exists " + fp.getPath() );
      return;
    }
    File fpp = fp.getParentFile();
    if ( fpp.exists() ) {
      // Log.v("DistoX", "check path: parent file exists " + fpp.getPath() );
      return;
    }
    if ( ! fpp.mkdirs() ) {
      TDLog.Error("check path: failed mkdirs " + fpp.getPath() );
    }
  }

  private static String getPathname( String directory, String name, String ext ) 
  {
    checkFilesystemDirs( directory );
    return directory + "/" + name + ext;
  }

  private static String getPathname( String directory, String name ) 
  {
    checkFilesystemDirs( directory );
    return directory + "/" + name;
  }

  private static File[] getFiles( String dirname, final String[] ext ) // DistoX-SAF
  {
    File dir = TDFile.getTopoDroidFile( dirname );
    if ( dir.exists() ) {
      return dir.listFiles( new FileFilter() {
          public boolean accept( File pathname ) { 
            int ne = ext.length;
            if ( pathname.isDirectory() ) return false;
            if ( ne == 0 ) return true;
            String name = pathname.getName().toLowerCase(Locale.US);
            for ( int n = 0; n < ne; ++n ) {
              if ( name.endsWith( ext[n] ) ) return true;
            }
            return false;
          }
        } );
    }
    return null;
  }

  private static File[] getExternalFiles( String dirname )
  {
    File dir = TDFile.getExternalDir( dirname );
    if ( ! dir.isDirectory() ) return null;
    return dir.listFiles( new FileFilter() {
      public boolean accept( File pathname ) { return ( ! pathname.isDirectory() ); }
    } );
  }

  private static void checkFilesystemDirs( String path )
  {
    TDFile.makeTopoDroidDir( path );
  }

  private static String checkType( String ext, String[] exts )
  {
    for ( String e : exts ) if ( e.equals( ext ) ) return e;
    return null;
  }

  // ================================================================================

  // final static String CAV = ".cav"; // Topo
  // final static String CAVE = ".cave"; // Polygon
  // final static String XVI = ".xvi"; // xtherion
  // final static String CSX = ".csx";
  // final static String DXF = ".dxf";
  // final static String GRT = ".grt"; // Grottolf
  // final static String GTX = ".gtx"; // GHTopo
  // final static String HTML = ".html";
  // final static String JSON = ".json";
  // final static String KML = ".kml";
  // final static String PLT = ".plt"; // trackfile
  // final static String PNG = ".png";
  // final static String PDF = ".pdf";
  // final static String SHP = ".shp"; // shapefile
  // final static String SHX = ".shx";
  // final static String DBF = ".dbf";
  // final static String SHZ = ".shz"; // shapefile zip
  // final static String SRV = ".srv"; // Walls
  // final static String SUR = ".sur"; // WinKarst
  // final static String SVG = ".svg";
  // final static String TDR3 = ".tdr3";
  // final static String TNL = ".xml"; // Tunnel XML
  // final static String TH2 = ".th2";
  // final static String TH3 = ".th3";
  // final static String TRB = ".trb"; // TopoRobot

  // private static String PATH_C3D;    //  = PATH_APPNAME + "/c3d/";   // Cave3D
  // private static String PATH_CAVE;   //  = PATH_APPNAME + "/cave/";  // Polygon
  // private static String PATH_CAV;    //  = PATH_APPNAME + "/cav/";   // Topo
  // private static String PATH_CSV;    //  = PATH_APPNAME + "/csv/";   // CSV text
  // private static String PATH_CSX;    //  = PATH_APPNAME + "/csx/";   // cSurvey
  // private static String PATH_DAT;    //  = PATH_APPNAME + "/dat/";   // Compass
  // private static String PATH_GRT;    //  = PATH_APPNAME + "/grt/";   // Grottolf
  // private static String PATH_GTX;    //  = PATH_APPNAME + "/gtx/";   // GHTopo
  // // private static String PATH_DUMP;   //  = PATH_APPNAME + "/dump/";  // DistoX memory dumps
  // private static String PATH_DXF;    //  = PATH_APPNAME + "/dxf/";
  // private static String PATH_KML;    //  = PATH_APPNAME + "/kml/";
  // private static String PATH_JSON;   //  = PATH_APPNAME + "/json/";
  // private static String PATH_PLT;    //  = PATH_APPNAME + "/plt/";   // trackfile
  // private static String PATH_IMPORT; //  = PATH_APPNAME + "/import/";
  // private static String PATH_PNG;    //  = PATH_APPNAME + "/png/";
  // private static String PATH_PDF;    //  = PATH_APPNAME + "/pdf/";
  // private static String PATH_SHP;    //  = PATH_APPNAME + "/shp/";   // shapefile
  // private static String PATH_SRV;    //  = PATH_APPNAME + "/srv/";   // Walls
  // private static String PATH_SUR;    //  = PATH_APPNAME + "/sur/";   // WinKarst
  // private static String PATH_SVG;    //  = PATH_APPNAME + "/svg/";   
  // private static String PATH_SVX;    //  = PATH_APPNAME + "/svx/";   // Survex
  // private static String PATH_TH;     //  = PATH_APPNAME + "/th/";
  // private static String PATH_TDR3;   //  = PATH_APPNAME + "/tdr3/";
  // private static String PATH_TH2;    //  = PATH_APPNAME + "/th2/";
  // private static String PATH_TH3;    //  = PATH_APPNAME + "/th3/";
  // // private static String APP_TMP_PATH;    //  = PATH_APPNAME + "/tmp/";
  // private static String PATH_TNL;    //  = PATH_APPNAME + "/tnl/";   // Tunnel
  // private static String PATH_TOP;    //  = PATH_APPNAME + "/top/";   // PocketTopo
  // private static String PATH_TRB;    //  = PATH_APPNAME + "/trb/";   // TopoRobot
  // private static String PATH_TRO;    //  = PATH_APPNAME + "/tro/";   // VisualTopo
  // private static String PATH_XVI;    //  = PATH_APPNAME + "/xvi/";
  // // private static String APP_TLX_PATH ; //  = PATH_APPNAME + "/tlx/";

  // private static String PATH_BIN  = "/bin/";    // Firmwares  
  // private static String PATH_CCSV = "/ccsv/";  // calib CSV text
  // private static String PATH_MAN  = TDInstance.context.getFileDir(); // PATH_DEFAULT + "/man/"; // User Manual
  // private static String APP_SYMBOL_PATH = "/symbol";
  // private static String APP_POINT_PATH  = APP_SYMBOL_PATH + "/point/";
  // private static String APP_LINE_PATH   = APP_SYMBOL_PATH + "/line/";
  // private static String APP_AREA_PATH   = APP_SYMBOL_PATH + "/area/";

  // static String getSymbolPointFilename( String name ) { return "symbol/point/" + name; }
  // static String getSymbolLineFilename( String name )  { return "symbol/line/"  + name; }
  // static String getSymbolAreaFilename( String name )  { return "symbol/area/"  + name; }

  /* LOAD_MISSING
  private static String APP_SYMBOL_SAVE_PATH = APP_SYMBOL_PATH + "save/";
  private static String APP_SAVE_POINT_PATH  = APP_SYMBOL_SAVE_PATH + "point/";
  private static String APP_SAVE_LINE_PATH   = APP_SYMBOL_SAVE_PATH + "line/";
  private static String APP_SAVE_AREA_PATH   = APP_SYMBOL_SAVE_PATH + "area/";
  
  static String getSymbolSavePointPath( String filename ) { return APP_SAVE_POINT_PATH + filename; }
  static String getSymbolSaveLinePath( String filename )  { return APP_SAVE_LINE_PATH + filename; }
  static String getSymbolSaveAreaPath( String filename )  { return APP_SAVE_AREA_PATH + filename; }
  */



  /* FIXME_SKETCH_3D *
  static void deleteSurvey3dFiles( String survey, List< Sketch3dInfo > sketches )
  {
    if ( hasTh3Dir() ) {
      for ( Sketch3dInfo s : sketches ) {
        TDFile.deleteFile( getTh3FileWithExt( survey + "-" + s.name + TH3 ) );
      }
    }
    if ( hasTdr3Dir() ) {
      for ( Sketch3dInfo s : sketches ) {
        TDFile.deleteFile( getTdr3FileWithExt( survey + "-" + s.name + TDR3 ) );
      }
    }
  }
   * END_SKETCH_3D */


  // private static void deleteSurveyExportFiles( String survey )
  // {
  //   // TDFile.deleteFile( getSurveyTlxFile( survey ) );
  //   TDFile.deleteFile( getCavFile( survey + CAV ) );
  //   TDFile.deleteFile( getCaveFile( survey + CAVE ) );
  //   TDFile.deleteFile( getCsvFile( survey + CSV ) );
  //   TDFile.deleteFile( getCsxFile( survey + CSX ) );
  //   TDFile.deleteFile( getDatFile( survey + DAT ) );
  //   TDFile.deleteFile( getDxfFile( survey + DXF ) );
  //   TDFile.deleteFile( getGrtFile( survey + GRT ) );
  //   TDFile.deleteFile( getGtxFile( survey + GTX ) );
  //   TDFile.deleteFile( getKmlFile( survey + KML ) );
  //   TDFile.deleteFile( getJsonFile( survey + JSON ) );
  //   TDFile.deleteFile( getPltFile( survey + PLT ) );
  //   TDFile.deleteFile( getShzFile( survey + SHZ ) );
  //   // deleteShpFiles( survey ); // SHP stations/shots/splays shp/shx/dbf
  //   TDFile.deleteFile( getSrvFile( survey + SRV ) );
  //   TDFile.deleteFile( getSurFile( survey + SUR ) );
  //   TDFile.deleteFile( getSvgFile( survey + SVG ) );
  //   TDFile.deleteFile( getSvxFile( survey + SVX ) );
  //   TDFile.deleteFile( getThFile(  survey + TH  ) );
  //   TDFile.deleteFile( getTopFile( survey + TOP ) );
  //   // TDFile.deleteFile( getTnlFile( survey + TNL ) );
  //   TDFile.deleteFile( getTrbFile( survey + TRB ) );
  //   TDFile.deleteFile( getTroFile( survey + TRO ) );
  // }

  // static void deleteSurveyOverviewFiles( String survey )
  // {
  //   TDFile.deleteFile( getDxfFile( survey + "-p" + DXF ) );
  //   TDFile.deleteFile( getShzFile( survey + "-p" + SHZ ) );
  //   TDFile.deleteFile( getSvgFile( survey + "-p" + SVX ) );
  //   TDFile.deleteFile( getTh2File( survey + "-p" + TH2  ) );
  //   TDFile.deleteFile( getXviFile( survey + "-p" + XVI ) );

  //   TDFile.deleteFile( getDxfFile( survey + "-s" + DXF ) );
  //   TDFile.deleteFile( getShzFile( survey + "-s" + SHZ ) );
  //   TDFile.deleteFile( getSvgFile( survey + "-s" + SVX ) );
  //   TDFile.deleteFile( getTh2File( survey + "-s" + TH2  ) );
  //   TDFile.deleteFile( getXviFile( survey + "-s" + XVI ) );
  // }

  // static private void deleteShpFiles( String survey )
  // {
  //   File dir = TDFile.getTopoDroidFile( getShpPath( survey ) );
  //   if ( dir.exists() ) {
  //     for ( String filename : dir.list() ) {
  //       (TDFile.getTopoDroidFile( dir, filename )).delete();
  //     }
  //     dir.delete();
  //   }
  // } 

  // static void deleteShpDirs( String survey, List< String > plots ) 
  // {
  //   deleteShpFiles( survey );
  //   if ( plots != null ) {
  //     for ( String plot : plots ) {
  //       deleteShpFiles( survey + "-" + plot );
  //     }
  //   }
  // }

  // private static void deleteSurveyPlotExportFiles( String survey, String pname )
  // {
  //   TDFile.deleteFile( getSurveyPlotCsxFile( survey, pname ) );
  //   TDFile.deleteFile( getSurveyPlotDxfFile( survey, pname ) );
  //   TDFile.deleteFile( getSurveyPlotPngFile( survey, pname ) );
  //   TDFile.deleteFile( getSurveyPlotSvgFile( survey, pname ) );
  //   TDFile.deleteFile( getSurveyPlotShzFile( survey, pname ) );
  //   TDFile.deleteFile( getSurveyPlotTh2File( survey, pname ) );
  //   TDFile.deleteFile( getSurveyPlotXviFile( survey, pname ) );
  // }

  // static File getTmpDir() { return TDFile.getTopoDroidFile( APP_TMP_PATH ); } // DistoX-SAF
  // static String getTmpFileWithExt( String name ) { return getPathname( APP_TMP_PATH, name, TMP ); }
  // static File getCacheFileWithExt( String name ) { return TDFile.getAppCacheFile( name + TMP ); }

  // static String getTdr3FileWithExt( String name ) { return getPathname( PATH_TDR3, name, TDR3 ); }
  // static String getTh2FileWithExt( String name ) { return getPathname( PATH_TH2, name, TH2 ); }
  // static String getTnlFileWithExt( String name ) { return getPathname( PATH_TNL, name, TNL ); }
  // static String getTh3FileWithExt( String name ) { return getPathname( PATH_TH3, name, TH3 ); }
  // static String getDxfFileWithExt( String name ) { return getPathname( PATH_DXF, name, DXF ); }
  // static String getSvgFileWithExt( String name ) { return getPathname( PATH_SVG, name, SVG ); }
  // static String getXviFileWithExt( String name ) { return getPathname( PATH_XVI, name, XVI ); }
  // static String getPngFileWithExt( String name ) { return getPathname( PATH_PNG, name, PNG ); }
  // static String getPdfFileWithExt( String name ) { return getPathname( PATH_PDF, name, PDF ); }
  // static String getC3dFileWithExt( String name ) { return getPathname( PATH_C3D, name, C3D ); }

  // static String getShzFileWithExt( String name ) { return getPathname( PATH_SHP, name, SHZ ); }
  // static String getShpBasepath( String name )    { return getPathname( PATH_SHP, name ); }
  // static String getShpPath( String name )        { return PATH_SHP + name; }


  // static String getSurveyTlxFile( String survey ) { return getPathname( APP_TLX_PATH, survey, TLX ); }
  // static String getSurveyThFile( String survey ) { return getPathname( PATH_TH, survey, TH ); }
  // static String getSurveyCsvFile( String survey ) { return getPathname( PATH_CSV, survey, CSV ); }
  // static String getSurveyCsxFile( String survey ) { return getPathname( PATH_CSX, survey, CSX ); }
  // static String getSurveyCsxFile( String survey, String name ) { return getPathname( PATH_CSX, survey + "-" + name, CSX ); }
  // static String getSurveyCaveFile( String survey ) { return getPathname( PATH_CAVE, survey, CAVE ); }
  // static String getSurveyCavFile( String survey ) { return getPathname( PATH_CAV, survey, CAV ); }
  // static String getSurveyDatFile( String survey ) { return getPathname( PATH_DAT, survey, DAT ); }
  // static String getSurveyDxfFile( String survey ) { return getPathname( PATH_DXF, survey, DXF ); }
  // static String getSurveyGrtFile( String survey ) { return getPathname( PATH_GRT, survey, GRT ); }
  // static String getSurveyGtxFile( String survey ) { return getPathname( PATH_GTX, survey, GTX ); }
  // static String getSurveyKmlFile( String survey ) { return getPathname( PATH_KML, survey, KML ); }
  // static String getSurveyJsonFile( String survey ) { return getPathname( PATH_JSON, survey, JSON ); }
  // static String getSurveyPltFile( String survey ) { return getPathname( PATH_PLT, survey, PLT ); }
  // static String getSurveyShzFile( String survey ) { return getPathname( PATH_SHP, survey, SHZ ); }
  // static String getSurveySrvFile( String survey ) { return getPathname( PATH_SRV, survey, SRV ); }
  // static String getSurveySurFile( String survey ) { return getPathname( PATH_SUR, survey, SUR ); }
  // static String getSurveySvxFile( String survey ) { return getPathname( PATH_SVX, survey, SVX ); }
  // static String getSurveyTopFile( String survey ) { return getPathname( PATH_TOP, survey, TOP ); }
  // static String getSurveyTrbFile( String survey ) { return getPathname( PATH_TRB, survey, TRB ); }
  // static String getSurveyTroFile( String survey ) { return getPathname( PATH_TRO, survey, TRO ); }

  // static String getCaveFile( String name )   { return PATH_CAVE + name; }
  // static String getCavFile( String name )    { return PATH_CAV + name; }
  // static String getCsvFile( String name )    { return PATH_CSV + name; }
  // static String getCsxFile( String name )    { return PATH_CSX + name; }
  // static String getDatFile( String name )    { return PATH_DAT + name; }
  // static String getGrtFile( String name )    { return PATH_GRT + name; }
  // static String getGtxFile( String name )    { return PATH_GTX + name; }
  // static String getDxfFile( String name )    { return PATH_DXF + name; }
  // static String getKmlFile( String name )    { return PATH_KML + name; }
  // static String getJsonFile( String name )   { return PATH_JSON + name; }
  // static String getPltFile( String name )    { return PATH_PLT + name; }
  // static String getPngFile( String name )    { return PATH_PNG + name; }
  // static String getPdfFile( String name )    { return PATH_PDF + name; }
  // static String getSrvFile( String name )    { return PATH_SRV + name; }
  // static String getSurFile( String name )    { return PATH_SUR + name; }
  // static String getSvgFile( String name )    { return PATH_SVG + name; }
  // static String getSvxFile( String name )    { return PATH_SVX + name; }
  // static String getShzFile( String name )    { return PATH_SHP + name; }
  // static String getThFile( String name )     { return PATH_TH + name; }
  // static String getTh2File( String name )    { return PATH_TH2 + name; }
  // static String getTh3File( String name )    { return PATH_TH3 + name; }
  // static String getTopFile( String name )    { return PATH_TOP + name; }
  // static String getTnlFile( String name )    { return PATH_TNL + name; }
  // static String getTrbFile( String name )    { return PATH_TRB + name; }
  // static String getTroFile( String name )    { return PATH_TRO + name; }
  // static String getXviFile( String name )    { return PATH_XVI + name; }
  // static String getC3dFile( String name )    { return PATH_C3D + name; }

  // public static String getDumpFile( String name )    { return "dump/" + name; }
  // public static String getBinFile( String name )     { return "bin/" + name; }
  // public static String getCCsvFile( String name )    { return "ccsv/" + name; }

  // static String getSurveyPlotDxfFile( String survey, String name ) { return PATH_DXF + survey + "-" + name + DXF ; }
  // static String getSurveyPlotSvgFile( String survey, String name ) { return PATH_SVG + survey + "-" + name + SVG ; }
  // // static String getSurveyPlotHtmFile( String survey, String name ) { return PATH_SVG + survey + "-" + name + HTML ; }
  // static String getSurveyPlotTh2File( String survey, String name ) { return PATH_TH2 + survey + "-" + name + TH2 ; }
  // static String getSurveyPlotTnlFile( String survey, String name ) { return PATH_TNL + survey + "-" + name + TNL ; }
  // static String getSurveyPlotPngFile( String survey, String name ) { return PATH_PNG + survey + "-" + name + PNG ; }
  // static String getSurveyPlotPdfFile( String survey, String name ) { return PATH_PDF + survey + "-" + name + PDF ; }
  // static String getSurveyPlotXviFile( String survey, String name ) { return PATH_XVI + survey + "-" + name + XVI ; }
  // static String getSurveyPlotCsxFile( String survey, String name ) { return PATH_CSX + survey + "-" + name + CSX ; }
  // static String getSurveyPlotC3dFile( String survey, String name ) { return PATH_C3D + survey + "-" + name + C3D ; }
  // // static String getSurveyPlotShpDir( String survey, String name ) { return PATH_SHP + survey + "-" + name ; }
  // static String getSurveyPlotShzFile( String survey, String name ) { return PATH_SHP + survey + "-" + name + SHZ ; }

  // static String getSurveySketchInFile( String survey, String name ) { return PATH_TH3 + survey + "-" + name + TH3 ; }
  // static String getSurveySketchOutFile( String survey, String name ) { return PATH_TDR3 + survey + "-" + name + TDR3 ; }

  // LOAD_MISSING
  // static String getSymbolSaveFile( String name ) { return APP_SYMBOL_SAVE_PATH + name; }

  // static boolean hasTdr3Dir() { return (TDFile.getTopoDroidFile( PATH_TDR3 )).exists(); }
  // static boolean hasTh2Dir() { return (TDFile.getTopoDroidFile( PATH_TH2 )).exists(); }
  // static boolean hasTh3Dir() { return (TDFile.getTopoDroidFile( PATH_TH3 )).exists(); }
  // static boolean hasPngDir() { return (TDFile.getTopoDroidFile( PATH_PNG )).exists(); }
  // static boolean hasPdfDir() { return (TDFile.getTopoDroidFile( PATH_PDF )).exists(); }
  // static boolean hasDxfDir() { return (TDFile.getTopoDroidFile( PATH_DXF )).exists(); }
  // static boolean hasKmlDir() { return (TDFile.getTopoDroidFile( PATH_KML )).exists(); }
  // static boolean hasJsonDir() { return (TDFile.getTopoDroidFile( PATH_JSON )).exists(); }
  // static boolean hasPltDir() { return (TDFile.getTopoDroidFile( PATH_PLT )).exists(); }
  // static boolean hasSvgDir() { return (TDFile.getTopoDroidFile( PATH_SVG )).exists(); }
  // static boolean hasXviDir() { return (TDFile.getTopoDroidFile( PATH_XVI )).exists(); }

  // static File getPngDir() { return TDFile.makeDir( PATH_PNG ); } 
  // static File getPdfDir() { return TDFile.makeDir( PATH_PDF ); } 
  // static String getImportFile( String name ) { return PATH_IMPORT + name; }
  // static String getTdr3File( String name )   { return PATH_TDR3 + name; }

  // private static void setExportPaths()
  // {
  //   APP_TLX_PATH = PATH_APPBASE + "tlx/";
  //   checkDirs( APP_TLX_PATH );
  //   PATH_C3D  = PATH_APPBASE + "c3d"  + File.separator;    // FIXME checkDirs( PATH_C3D );
  //   PATH_CAV  = PATH_APPBASE + "cav"  + File.separator;    // FIXME checkDirs( PATH_CAV );
  //   PATH_CAVE = PATH_APPBASE + "cave" + File.separator;    // FIXME checkDirs( PATH_CAVE );
  //   PATH_CSV  = PATH_APPBASE + "csv"  + File.separator;    // FIXME checkDirs( PATH_CSV );
  //   PATH_CSX  = PATH_APPBASE + "csx"  + File.separator;    // FIXME checkDirs( PATH_CSX );
  //   PATH_DAT  = PATH_APPBASE + "dat"  + File.separator;    // FIXME checkDirs( PATH_DAT );
  //   PATH_DXF  = PATH_APPBASE + "dxf"  + File.separator;    // FIXME checkDirs( PATH_DXF );
  //   PATH_GRT  = PATH_APPBASE + "grt"  + File.separator;    // FIXME checkDirs( PATH_GRT );
  //   PATH_GTX  = PATH_APPBASE + "gtx"  + File.separator;    // FIXME checkDirs( PATH_GTX );
  //   PATH_JSON = PATH_APPBASE + "json" + File.separator;    // FIXME checkDirs( PATH_JSON );
  //   PATH_KML  = PATH_APPBASE + "kml"  + File.separator;    // FIXME checkDirs( PATH_KML );
  //   PATH_PLT  = PATH_APPBASE + "plt"  + File.separator;    // FIXME checkDirs( PATH_PLT );
  //   PATH_PNG  = PATH_APPBASE + "png"  + File.separator;    // FIXME checkDirs( PATH_PNG );
  //   PATH_PDF  = PATH_APPBASE + "pdf"  + File.separator;    // FIXME checkDirs( PATH_PDF );
  //   PATH_SHP  = PATH_APPBASE + "shp"  + File.separator;    // FIXME checkDirs( PATH_SHP );
  //   PATH_SRV  = PATH_APPBASE + "srv"  + File.separator;    // FIXME checkDirs( PATH_SRV );
  //   PATH_SUR  = PATH_APPBASE + "sur"  + File.separator;    // FIXME checkDirs( PATH_SUR );
  //   PATH_SVG  = PATH_APPBASE + "svg"  + File.separator;    // FIXME checkDirs( PATH_SVG );
  //   PATH_SVX  = PATH_APPBASE + "svx"  + File.separator;    // FIXME checkDirs( PATH_SVX );
  //   PATH_TDR3 = PATH_APPBASE + "tdr3" + File.separator;    checkDirs( PATH_TDR3 );
  //   PATH_TH   = PATH_APPBASE + "th"   + File.separator;    // FIXME checkDirs( PATH_TH );
  //   PATH_TH2  = PATH_APPBASE + "th2"  + File.separator;    checkDirs( PATH_TH2 );
  //   PATH_TH3  = PATH_APPBASE + "th3"  + File.separator;    checkDirs( PATH_TH3 );
  //   PATH_TNL  = PATH_APPBASE + "tnl"  + File.separator;    // FIXME checkDirs( PATH_TNL );
  //   PATH_TOP  = PATH_APPBASE + "top"  + File.separator;    // FIXME checkDirs( PATH_TOP );
  //   PATH_TRB  = PATH_APPBASE + "trb"  + File.separator;    // FIXME checkDirs( PATH_TRB );
  //   PATH_TRO  = PATH_APPBASE + "tro"  + File.separator;    // FIXME checkDirs( PATH_TRO );
  //   PATH_XVI  = PATH_APPBASE + "xvi"  + File.separator;    // FIXME checkDirs( PATH_XVI );
  //   APP_TMP_PATH   = PATH_APPBASE + "tmp"   + File.separator; checkDirs( APP_TMP_PATH );
  //   PATH_IMPORT = PATH_APPBASE + "import" + File.separator;   checkDirs( PATH_IMPORT );
  // }

  // static private void checkExternalDirs( String path )
  // {
  //   TDFile.makeExternalDir( path );
  // }

  // static void symbolsCheckDirs()
  // {
  //   checkExternalDirs( "point" );
  //   checkExternalDirs( "line" );
  //   checkExternalDirs( "area" );
  //   /* LOAD_MISSING
  //   checkDirs( APP_SYMBOL_SAVE_PATH );
  //   checkDirs( APP_SAVE_POINT_PATH );
  //   checkDirs( APP_SAVE_LINE_PATH );
  //   checkDirs( APP_SAVE_AREA_PATH );
  //   */
  // }

  // public static void checkCCsvDir() { checkExternalDirs( "ccsv" ); }
  // public static void checkBinDir()  { checkExternalDirs( "bin" ); }

  // static void checkManDir() { checkDirs( PATH_MAN ); }

}
