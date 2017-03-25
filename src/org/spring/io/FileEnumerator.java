
package org.spring.io;

import java.util.*;
import java.io.*;

public class FileEnumerator implements Enumeration {

  private String PathName;
  private String files[];
  private int iFile;

  public FileEnumerator( String pathname ) {
    this( new File( pathname ), null );
  };

  public FileEnumerator( String pathname, FilenameFilter filter ) {
    this( new File( pathname ), filter );
  };

  public FileEnumerator( File pathname ) {
    this( pathname, null );
  };

  public FileEnumerator( File pathname, FilenameFilter filter ) {
    if ( pathname.isDirectory() )
      PathName = pathname.getAbsolutePath()+File.separator;
    if ( filter != null )
      files = pathname.list( filter );
    else
      files = pathname.list();
    iFile = 0;
  };

  public boolean hasMoreElements() {
    if ( files!=null && iFile<files.length )
      return true;
    return false;
  };

  public boolean hasMoreFiles() {
    return this.hasMoreElements();
  };

  public Object nextElement() {
    if ( !this.hasMoreElements() )
      throw new NoSuchElementException();
    return files[iFile++];
  };

  public String nextFileName() {
    return (String) this.nextElement();
  };

  public File nextFile() {
    return new File( PathName+this.nextElement() );
  };

};