
package org.spring.logger;

import java.util.Date;
import org.spring.util.DateUtil;

public class LogStdErr implements LoggerAgent {

  protected ConfigClass Config;

  public LogStdErr() {
    Config = new ConfigClass();
  };

  public Class getParametersClass() {
    return ConfigClass.class;
  };

  public void setParameters( Object p ) {
    Config = (ConfigClass)p;
  };

  public Object getParameters() {
    return Config;
  };

  public void LogEvent( Date at, String message ) {
    message =
      DateUtil.DateString( at ) + " " + message + System.getProperty("line.separator");
    System.out.print( message );
  };

// Configuration class

  static public class ConfigClass {
  };

};
