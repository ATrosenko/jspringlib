
package org.spring.java.util.zip;

import java.util.zip.*;
import java.io.*;

public class MSExpand5InputStream extends InflaterInputStream {

  protected boolean eos;
  private boolean closed = false;

  private byte lastByteOfFilename;
  private long Filesize;
  private long currentPos;

  public MSExpand5InputStream( InputStream in, int size ) throws IOException {
    super( in, new LZ77Inflater(), size );
    currentPos = 0;
    readHeader();
  };

  public MSExpand5InputStream( InputStream in ) throws IOException {
    this( in, 512 );
  };

  public byte getLastByteOfFilename() {
    return lastByteOfFilename;
  };

  public long getFilesize() {
    return Filesize;
  };

  private void ensureOpen() throws IOException {
    if ( closed )
      throw new IOException( "Stream closed" );
  };

  public int available() throws IOException {
    ensureOpen();
    if ( eos )
      return ( 0 );
    return ( Filesize>currentPos ? 1 : 0 );
  };

  public int read( byte[] buf, int off, int len ) throws IOException {
    ensureOpen();
    if ( eos )
      return ( -1 );
    len = super.read( buf, off, (int)Math.min(Filesize-currentPos,len) );
    if ( len == -1 )
      eos = true;
    else
      currentPos+=len;
    return len;
  };

  public void close() throws IOException {
    inf.end();
    in.close();
    eos = true;
    closed = true;
  };

  private final static int SIGNATURE = 0x44445a53;

  private void readHeader() throws IOException {
    if ( readInt(in,4) != SIGNATURE )
      throw new IOException("Not in MS Compress/Expand for DOS5 format");
    readInt(in,4);  // reserved long
    readInt(in,1);  // reserved byte
    lastByteOfFilename = (byte)readInt(in,1);
    Filesize = readInt(in,4);
    if ( Filesize<0 )
      throw new IOException("Corrupted header");
  };

  private long readInt( InputStream in, int size ) throws IOException {
    if ( size<1 || size>4 )
      throw ( new IOException() );
    byte[] b = new byte[4];
    b[0]=0; b[1]=0; b[2]=0; b[3]=0;
    if ( in.read( b, 0, size ) != size )
      throw new EOFException();
    return ( (((((b[3]<<8)|b[2])<<8)|b[1])<<8)|b[0] );
  };

};
