package org.spring.logger;

import java.util.Date;

public class Logger {

	final static public int OFF = -1;
	final static public int ERROR = OFF;
	final static public int ERR = ERROR;
	final static public int NORMAL = 0;
	final static public int NORM = NORMAL;
	final static public int VERBOSE = 1;
	final static public int VERB = VERBOSE;
	final static public int DIAGNOSTIC = 2;
	final static public int DIAG = DIAGNOSTIC;
	final static public int DEBUG = 3;

	protected class LoggerAgentRecord {
		protected int level;
		protected LoggerAgent agent;
	}

	protected LoggerAgentRecord[] laList;

	public Logger() {
		laList = new LoggerAgentRecord[0];
	}

	public void addLogger( LoggerAgent newLogger ) {
		this.addLogger( newLogger, NORMAL );
	}

	public void addLogger( LoggerAgent newLogger, int level ) {
		LoggerAgentRecord[] laList2 = new LoggerAgentRecord[laList.length + 1];
		System.arraycopy( laList, 0, laList2, 0, laList.length );
		LoggerAgentRecord lar = new LoggerAgentRecord();
		lar.level = level;
		lar.agent = newLogger;
		laList2[laList2.length - 1] = lar;
		laList = laList2;
	}

	public void removeLogger( LoggerAgent oldLogger ) {
		boolean successFlag = true;
		for( int i = 0; i < laList.length; i++ ) {
			if( laList[i].agent.equals( oldLogger ) ) {
				LoggerAgentRecord[] laList2 = new LoggerAgentRecord[laList.length - 1];
				System.arraycopy( laList, 0, laList2, 0, i );
				System.arraycopy( laList, i + 1, laList2, 0, laList.length - i - 1 );
				laList = laList2;
				successFlag = false;
				i--;
			}
		}
		if( !successFlag ) {
			throw( new IllegalArgumentException() );
		}
	}

	public void removeLogger( int oldLogger ) {
		if( oldLogger >= 0 && oldLogger < laList.length ) {
			LoggerAgentRecord[] laList2 = new LoggerAgentRecord[laList.length - 1];
			System.arraycopy( laList, 0, laList2, 0, oldLogger );
			System.arraycopy( laList, oldLogger + 1, laList2, 0,
												laList.length - oldLogger - 1 );
			laList = laList2;
		} else {
			throw( new IllegalArgumentException() );
		}
	}

	public LoggerAgent getLogger( int logger ) {
		if( logger < 0 || logger >= laList.length ) {
			throw( new IllegalArgumentException() );
		}
		return( laList[logger].agent );
	}

	public void Write() {
		this.Write( "", NORMAL );
	}

	public void Write( String message ) {
		this.Write( message, NORMAL );
	}

	public void WriteWarning( String message ) {
		this.WriteWarning( message, VERBOSE );
	}

	public void WriteWarning( String message, int level ) {
		this.Write( "*** Warning: " + message, level );
	}

	public void WriteError( String message ) {
		this.WriteError( message, NORMAL );
	}

	public void WriteError( String message, int level ) {
		this.Write( "*** Error: " + message, level );
	}

	public void Write( String message, int level ) {
		Date now = new Date();
		for( int i = 0; i < laList.length; i++ ) {
			if( laList[i].level >= level ) {
				try {
					laList[i].agent.LogEvent( now, message );
				} catch( Exception e ) {
					System.out.println(
							"" + laList[i].agent.getClass().getName() + ": " + e.toString() );
				}
			}
		}
	}

	public static void main( String[] argv ) throws Exception {
		Logger l = new Logger();
		{
			LogStdOut s = new LogStdOut();
			l.addLogger( s, Logger.DEBUG );
		}
		{
			LogStdErr s = new LogStdErr();
			l.addLogger( s, Logger.DEBUG );
		}
		{
			LogFile f = new LogFile();
			LogFile.ConfigClass fc = ( LogFile.ConfigClass )f.getParameters();
//			fc.LogNaming = "c:\\!\\LOGFILE"+LogFile.SERIAL(1)+".LOG";
			fc.LogNaming = "c:\\!\\LOGFILE.LOG";
			fc.RollOver.Size = 1024;
			fc.RollOver.Append = true;
			f.setParameters( fc );
			l.addLogger( f, Logger.DEBUG );
		}
		l.WriteWarning( "Test Application started !" );
		l.WriteWarning( "Blah-Blah..." );
		l.WriteWarning( "Test Application stopped !" );
	}

}
