
package org.spring.application;

import org.spring.logger.Logger;
import org.spring.util.*;
import java.io.IOException;

public class Agent {

  public int StatusLineWidth;
  public int SleepTime;
  final static public int Default_SleepTime = 30;
  protected Logger LogStream;
  protected Configuration Configuration;

  public Agent() {
    StatusLineWidth = 80;
    SleepTime = Default_SleepTime;
    LogStream = new Logger();
    Configuration = new Configuration();
  };

/*  void Terminate( String message ) {
    if ( message != "" ) {
      LogFile.Log( message );
      System.exit(1);
    } else {
      LogFile.Log();
      System.exit(0);
    };
  };*/

  public void ShowStatus( String message ) {
    int LF = message.lastIndexOf('\n');
    int trailers = StatusLineWidth -
                ( LF>=0 ? message.substring(LF+1).length() : message.length() );
    System.out.print( "\r"+message );
    for ( ; trailers>1; trailers-- )
      System.out.print( " " );
    System.out.print( "\r" );
  };

  public boolean Initialize() {
    return true;
  };

  public boolean SingleLoop() {
    return true;
  };

  public boolean Terminate() {
    return true;
  };

  public boolean ConfigurationChanged() {
    return true;
  };

  public boolean Execute() {
    while ( true ) {
      if ( Configuration.IsChanged() ) {
        try {
          Configuration.clear();
          Configuration.Read();
          this.ConfigurationChanged();
        } catch ( IOException e ) {
          LogStream.Write("Error reading configuration");
          continue;
        };
        this.Initialize();
      };
      if ( !this.SingleLoop() )
        break;
      try {
        Sleep();
      } catch ( InterruptedException e ) {
        break;
      };
    };
    this.Terminate();
    return false;
  };

  boolean Sleep() throws InterruptedException {
    for ( long suspdelay=SleepTime<2?Default_SleepTime:SleepTime; suspdelay>0; suspdelay-- ) {
      ShowStatus( "Suspended for "+suspdelay+" sec..." );
      Thread.currentThread().sleep(1000);
      if ( this.CheckForShutdown() ) {
        LogStream.Write( "Shutdown file triggered immediate shutdown" );
        do {
          Thread.currentThread().sleep(1000);
        } while ( !this.Terminate() );
        System.exit(0);
      };
    };
    return true;
  };

  public void Log( String s, int level ) {
    LogStream.Write( s, level );
  };

  public boolean CheckForShutdown() {
    return false;
  };

};
