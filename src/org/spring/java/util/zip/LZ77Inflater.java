
package org.spring.java.util.zip;

import java.util.zip.*;

public class LZ77Inflater extends Inflater {

  private byte[]buffer = new byte[0];
  private int offset, length;
  private boolean finished;

  public LZ77Inflater() {
    length = 0;
    finished = false;
    for ( int i=0; i<ringBuffer.length; i++ )
      ringBuffer[i] = 0x20;
  };

  public void end() {  // Closes the decompressor and discards any unprocessed input.
    finished = true;
  };

  public boolean finished() {  // Closes the decompressor when garbage is collected.
    return finished;
  };

  public boolean needsDictionary() { // Returns true if a preset dictionary is needed for decompression.
    return false;
  };

  public boolean needsInput() {  // Returns true if no data remains in the input buffer.
    return ( length <= 0 );
  };

  public void setInput( byte[] b, int off, int len ) { // Sets input data for decompression.
    if (b == null)
      throw new NullPointerException();
    if ( off < 0 || len < 0 || off + len > b.length )
      throw new ArrayIndexOutOfBoundsException();
    buffer = b;
    offset = off;
    length = len;
  };

  private byte[] ringBuffer = new byte[4096];
  private int ringBufferDst = ringBuffer.length-16;
  private int ringBufferSrc = -1;
  private int guardByte;
  private int guardByteMask = 0;
  private int nextBlockLength = -1;

  public int inflate( byte[] b, int off, int len ) throws DataFormatException {
                                    // Uncompresses bytes into specified buffer.
    if (b == null)
      throw new NullPointerException();
    if ( off < 0 || len < 0 || off + len > b.length )
      throw new ArrayIndexOutOfBoundsException();
    int lenAtTheBegin = len;
    do {
      if ( guardByteMask==0 ) {
        if ( needsInput() )
          return lenAtTheBegin - len;
        guardByte = toByte( buffer[offset++] );
        length--;
        guardByteMask = 1;
      };
      for ( ; (guardByteMask&=255)>0; guardByteMask<<=1 ) {
        if ( (guardByte&guardByteMask)==0 ) {
          if ( ringBufferSrc<0 ) {
            if ( needsInput() )
              return lenAtTheBegin - len;
            ringBufferSrc = toByte( buffer[offset++] );
            length--;
          };
          if ( nextBlockLength<0 ) {
            if ( needsInput() )
              return lenAtTheBegin - len;
            ringBufferSrc += (toByte(buffer[offset])&0xF0) << 4;
            nextBlockLength = (toByte(buffer[offset++])&0x0F) + 3;
            length--;
          };
          for ( ; nextBlockLength>0; nextBlockLength-- ) {
            if ( off >= b.length )
              return lenAtTheBegin - len;
            ringBuffer[ringBufferDst] = ringBuffer[ringBufferSrc++];
            b[off++] = ringBuffer[ringBufferDst++];
//System.out.print( (char)b[off-1] );
            len--;
            ringBufferDst %= ringBuffer.length;
            ringBufferSrc %= ringBuffer.length;
          };
          ringBufferSrc = -1;
          nextBlockLength = -1;
        } else {
          if ( needsInput() )
            return lenAtTheBegin - len;
          if ( off >= b.length )
            return lenAtTheBegin - len;
          ringBuffer[ringBufferDst++] = buffer[offset];
          b[off++] = buffer[offset++];
          len--;
          length--;
          ringBufferDst %= ringBuffer.length;
        };
      };
    } while ( !needsInput() && len>0 );
    return lenAtTheBegin - len;
  };

  private int toByte( byte b ) {
    return ( b<0 ? 256+b : b );
  };

};
