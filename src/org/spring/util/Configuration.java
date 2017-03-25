
package org.spring.util;

import java.io.*;
import java.util.*;

public class Configuration extends Properties {

  public Date TimeStamp;
  public InputStream Input;
  public OutputStream Output;
  protected boolean Changed;

  public Configuration() {
    super();
    TimeStamp = new Date();
    Changed = true;
  };

  public Configuration( Properties def ) {
    super( def );
    TimeStamp = new Date();
    Changed = true;
  };

  public Configuration( Configuration def ) {
    super( (Properties)def );
    TimeStamp = new Date();
    Changed = true;
  };

  public boolean IsChanged() {
    boolean changed = Changed;
    Changed = false;
    return changed;
  };

  public boolean Reset() {
    TimeStamp = new Date();
    return true;
  };

  public boolean Read() throws IOException {
    return true;
  };

  public boolean Write() throws IOException {
    return true;
  };

//  public static String KeyName( String format, Object[] parm ) {

};
