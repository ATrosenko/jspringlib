
package org.spring.io;

import java.io.*;

public class FileUtil {

  public static void Copy( String src, String dst )
      throws IOException, FileNotFoundException {
    Copy( new File( src ), new File( dst ) );
  };

  public static void Copy( File src, File dst )
      throws IOException, FileNotFoundException {
    FileInputStream in = new FileInputStream( src );
    FileOutputStream out = new FileOutputStream( dst );
    byte buf[] = new byte[16384];
    int len;
    while ( (len=in.read(buf))!=-1 )
      out.write( buf, 0, len );
    out.close();
    in.close();
  };

};