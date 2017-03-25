
package org.spring.logger;

import java.util.Date;

public interface LoggerAgent {

  public Class getParametersClass();
  public void setParameters( Object p ) throws Exception;
  public Object getParameters();

  public void LogEvent( Date at, String message ) throws Exception;

};
