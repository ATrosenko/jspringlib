
package org.spring.io;

import java.util.*;
import java.io.*;

public class MaskFileFilter implements FileFilter {

  public DirAction Directories;
  public boolean FilterPathname;
  public boolean CaseSensitive;

  public class DirAction {
    public boolean Accept = true;
    public boolean Filter = false;
  };

  protected List masks;

  public MaskFileFilter( String newmask ) {
    this( new String[] { newmask } );
  };

  public MaskFileFilter( String[] newmask ) {
    this( newmask, new File("A").compareTo(new File("a")) != 0 );
  };

  public MaskFileFilter( String newmask, boolean casesens ) {
    this( new String[] { newmask }, casesens );
  };

  public MaskFileFilter( String[] newmask, boolean casesens ) {
    masks = new ArrayList();
    Directories = new DirAction();
    FilterPathname = false;
    CaseSensitive = casesens;
    for ( int i=0; i<newmask.length; i++ )
      masks.add( newmask[i] );
  };

  public boolean accept( File f ) {
    if( f == null )
      return false;
    if ( f.isDirectory() ) {
      if ( !Directories.Accept )
        return false;
      if ( !Directories.Filter )
        return true;
    };
    String name = FilterPathname ? f.getAbsolutePath() : f.getName();
    if ( !CaseSensitive )
      name = name.toUpperCase();
    for ( int iMask=0; iMask<masks.size(); iMask++ ) {
      String mask = (String)masks.get(iMask);
      if ( !CaseSensitive )
        mask = mask.toUpperCase();
      int asterisks = 0;
      for ( int i=-1; (i=mask.indexOf("*",i+1))>=0; asterisks++ );
      int maxAsterisksLen = name.length()-(mask.length()-asterisks);
      // Prepair the initial asterisks disposition
      if ( maxAsterisksLen<0 )
        return false;
      int[] asterisk = new int[asterisks];
      for ( int i=0; i<asterisks; i++ )
        asterisk[i] = i==0 ? maxAsterisksLen : 0;
      boolean doJump = false;
      do {
        // Compare name & mask
        boolean nameDifferFromMask = false;
        for ( int name_i=0, mask_i=0, asterisk_i=0;
            name_i<name.length(); name_i++, mask_i++ ) {
          if ( mask.charAt(mask_i)=='*' ) {
            name_i += asterisk[asterisk_i++]-1;
          } else if ( mask.charAt(mask_i)!='?' ) {
            if ( name.charAt(name_i) != mask.charAt(mask_i) ) {
              nameDifferFromMask = true;
              break;
            };
          };
        };
        if ( !nameDifferFromMask )
          return true; // We've got it !
        // Prepair the next asterisks disposition
        for ( int i=asterisks-2; i>=0; i-- ) {
          if ( asterisk[i]>0 ) {
            asterisk[i]--;
            if ( doJump ) {
              int newValue = asterisk[asterisks-1] + 1;
              asterisk[asterisks-1] = 0;
              asterisk[i+1] = newValue;
            } else
              asterisk[i+1]++;
            doJump = ( i==asterisks-2 );
            break;
          };
        };
      } while ( asterisks>1 && asterisk[asterisks-1]<maxAsterisksLen );
    };
    return false;
  };

};
