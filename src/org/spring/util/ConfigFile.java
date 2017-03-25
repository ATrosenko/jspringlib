
package org.spring.util;

import java.io.*;
import java.util.*;

public class ConfigFile extends Configuration {

  public File configFile;

  public ConfigFile( File file ) {
    super();
    configFile = file;
  };

  public ConfigFile( File file, Properties def ) {
    super( def );
    configFile = file;
  };

  public boolean IsChanged() {
    Date modified = new Date( configFile.lastModified() );
    if ( modified.getTime()>0 && !modified.equals( TimeStamp ) ) {
      Changed = true;
      TimeStamp = modified;
    };
    return super.IsChanged();
  };

  public boolean Read() throws IOException {
    return Read( System.getProperty("file.encoding") );
  };

  public boolean Read( String charset ) throws IOException {
    FileInputStream encfile = new FileInputStream( configFile );
    load( encfile );
    encfile.close();
    charset = this.getProperty( "CharSet", charset );
    clear();
    BufferedReader inifile = new BufferedReader(
      new InputStreamReader( new FileInputStream( configFile ), charset ) );
    String iniString = "";
    try {
      int nextChar;
      while ( (nextChar=inifile.read()) != -1 ) {
//        if ( nextChar>=127 || nextChar==9 || nextChar==32 ) // For production environment to work correctly
        if ( nextChar>=127 )
          iniString += "\\u0000".substring(0,6-Integer.toHexString(nextChar).length())+Integer.toHexString(nextChar);
        else
          iniString += String.valueOf((char)nextChar);
      };
    } catch ( IOException e ) {};
    inifile.close();
    load( new ByteArrayInputStream( iniString.getBytes() ) );
    inifile.close();
    return super.Read();
  };

  public boolean Write() throws IOException {
    FileOutputStream inifile = new FileOutputStream( configFile );
    store( inifile, " "+this.getClass().getName()+" configuration file" );
    inifile.close();
    return super.Write();
  };

  public String getProperty( String key, String defaultValue ) {
//    return super.getProperty( key.replace( '\t', '_' ).replace( ' ', '_' ), defaultValue );
    return super.getProperty( key, defaultValue );
  };

  public String getProperty( String key ) {
//    return super.getProperty( key.replace( '\t', '_' ).replace( ' ', '_' ) );
    return super.getProperty( key );
  };

  public Object setProperty( String key, String value ) {
//    return super.setProperty( key.replace( '\t', '_' ).replace( ' ', '_' ), value );
    return super.setProperty( key, value );
  };

};
