package org.spring.logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;

import org.spring.util.DateUtil;

public class LogFile
		implements LoggerAgent {

	final static public String YEAR4 = "{1,number,0000}";
	final static public String MONTH2 = "{2,number,00}";
	final static public String WEEK_OF_YEAR2 = "{3,number,00}";
	final static public String WEEK_OF_MONTH1 = "{4,number,0}";
	final static public String DATE2 = "{5,number,00}";
	final static public String DAY_OF_YEAR3 = "{6,number,000}";
	final static public String DAY_OF_WEEK1 = "{7,number,0}";
	final static public String HOUR2 = "{8,number,00}";
	final static public String MINUTE = "{9,number,00}";

	static public String SERIAL( int digits ) {
		if( digits < 1 ) {
			throw( new IllegalArgumentException() );
		}
		String value = "";
		for( ; digits > 0; digits-- ) {
			value += "0";
		}
		return( "{0,number," + value + "}" );
	}

	protected ResourceBundle res;
	protected Locale locale;

	protected ConfigClass Config;
	protected File logFile;
	protected PrintWriter logWriter;
	protected long logFileSize;
	protected Date logFileCreatedAt;

	public LogFile() {
		this( Locale.getDefault() );
	}

	public LogFile( Locale l ) {
		locale = l;
		res = ResourceBundle.getBundle( this.getClass().getName(), l );
		logFile = null;
		logWriter = null;
		Config = new ConfigClass();
	}

	public Class getParametersClass() {
		return ConfigClass.class;
	}

	public void setParameters( Object p ) throws FileNotFoundException {
		Config = ( ConfigClass )p;
		if( Config.LogNaming == null || Config.LogNaming.length() == 0 ) {
			throw( new FileNotFoundException( res.getString( "Arg_LogNaming" ) ) );
		}
		if( Config.MinLogNumber < 0 || Config.MinLogNumber > Config.MaxLogNumber ) {
			throw( new IllegalArgumentException( res.getString( "Arg_MinLogNumber" ) ) );
		}
		if( Config.RollOver.Size < 0 ) {
			throw( new IllegalArgumentException( res.getString( "Arg_Rollover" ) ) );
		}
		if( Config.OldLogs.MaxNumber < 0 ) {
			throw( new IllegalArgumentException( res.getString( "Arg_MaxOldLogs" ) ) );
		}
	}

	public Object getParameters() {
		return Config;
	}

	public void LogEvent( Date at, String message ) throws IOException {
		message = MessageFormat.format( res.getString( "Log_record" ), new Object[] {DateUtil.DateString( at, locale ),
																		message} ) + System.getProperty( "line.separator" );
		openCurrentLogFile( message );
		logWriter.print( message );
		logFileSize += message.length();
		logWriter.flush();
	}

	protected void finalize() {
		try {
			if( logWriter != null ) {
				logWriter.print( MessageFormat.format( res.getString( "LogFile_closed_at" ),
																							 new Object[] {DateUtil.DateString( new Date(), locale )} ) );
				logWriter.flush();
				logWriter.close();
			}
		} catch( Exception e ) {}
	}

	protected String getLogFileName( TimeZone tz, Date d, long iLog ) {
		Calendar c = Calendar.getInstance();
		c.setTimeZone( tz );
		c.setTime( d );
		Object[] dateArgs = {new Long( iLog ), new Long( c.get( Calendar.YEAR ) ), new Long( c.get( Calendar.MONTH ) + 1 ),
												new Long( c.get( Calendar.WEEK_OF_YEAR ) ), new Long( c.get( Calendar.WEEK_OF_MONTH ) ),
												new Long( c.get( Calendar.DATE ) ), new Long( c.get( Calendar.DAY_OF_YEAR ) ), //
												new Long( c.get( Calendar.DAY_OF_WEEK ) ), new Long( c.get( Calendar.HOUR_OF_DAY ) ), //
												new Long( c.get( Calendar.MINUTE ) )};
		return( MessageFormat.format( Config.LogNaming, dateArgs ) );
	}

	protected Date periodStartsAt( Date dateInPeriod ) {
		return( new Date( ( ( dateInPeriod.getTime() - Config.RollOver.StartingAt.getTime() )
												/ Config.RollOver.Period.getTime() ) * Config.RollOver.Period.getTime()
											+ Config.RollOver.StartingAt.getTime() ) );
	}

	protected boolean needsToBeRolledOver( String message ) throws IOException {
		if( logFile != null ) {
			if( Config.RollOver.Period.getTime() != 0
					&& logFileCreatedAt.getTime()
					>= periodStartsAt( logFileCreatedAt ).getTime() + Config.RollOver.Period.getTime() ) {
				return( true );
			}
			if( Config.RollOver.Size != 0 && logFileSize + message.length() > Config.RollOver.Size ) {
				return( true );
			}
		} else {
			return( true );
		}
		return( false );
	}

	protected void openCurrentLogFile( String message ) throws FileNotFoundException, IOException {
		Date periodStart = periodStartsAt( new Date() );
		if( logFile != null && needsToBeRolledOver( message ) ) {
			String reasonText = Config.RollOver.Size != 0 && logFileSize + message.length() > Config.RollOver.Size
													?
													MessageFormat.format( res.getString( "Its_exceeds_maximum_size" ),
																								new Object[] {new Long( Config.RollOver.Size )} )
													: MessageFormat.
													format( res.getString( "Its_exceeds_maximum_time" ),
																	new Object[] {new Long( Config.RollOver.Period.getTime() / 1000 )} );
			logWriter.print( MessageFormat.format( res.getString( "LogFile_switched_at" ),
																						 new Object[] {DateUtil.DateString( new Date(), locale ), message} )
											 + System.getProperty( "line.separator" ) + reasonText + System.getProperty( "line.separator" ) );
			logWriter.flush();
			logWriter.close();
			logWriter = null;
			logFile = null;
		}
		if( logFile == null ) {
			String prevFile = "";
			for( long i = Config.MinLogNumber; i <= Config.MaxLogNumber; i++ ) {
				logFile = new File( this.getLogFileName( TimeZone.getDefault(), periodStart, i ) );
				if( prevFile.equalsIgnoreCase( logFile.getAbsolutePath() ) ) {
					break;
				} else {
					prevFile = logFile.getAbsolutePath();
				}
				if( !logFile.exists() ) {
					SimpleDateFormat headerFormat = new SimpleDateFormat( MessageFormat.format( res.getString(
							"LogFile_created_at" ), new Object[] {DateUtil.DateString( new Date(), locale )} ) );
					headerFormat.setTimeZone( TimeZone.getDefault() );
					if( Config.RollOver.Append && i > Config.MinLogNumber ) {
						logFile = new File( this.getLogFileName( TimeZone.getDefault(), periodStart, i - 1 ) );
						getLogFileParms( logFile, headerFormat );
						if( logFileCreatedAt == null || !needsToBeRolledOver( message ) ) {
							try {
								logWriter = new PrintWriter( new FileOutputStream( logFile.getAbsolutePath(), true ) );
							} catch( FileNotFoundException e ) {
								logFile = null;
								throw new Error( "Never reaching point - LogFile.openCurrentLogFile().1 caused FileNotFoundException" );
							}
						}
					}
					if( logWriter == null ) {
						logFile = new File( this.getLogFileName( TimeZone.getDefault(), periodStart, i ) );
						logFile.getParentFile().mkdirs();
						try {
							logWriter = new PrintWriter( new FileOutputStream( logFile ) );
						} catch( FileNotFoundException e ) {
							logFile = null;
							throw new Error( "Never reaching point - LogFile.openCurrentLogFile().2 caused FileNotFoundException" );
						}
						logFileCreatedAt = new Date();
						String commentLine = headerFormat.format( logFileCreatedAt ) + System.getProperty( "line.separator" );
						logWriter.print( commentLine );
						logFileSize = commentLine.length();
					}
					break;
				}
			}
			if( logFile == null ) {
				throw( new FileNotFoundException() );
			} else {
				logWriter = new PrintWriter( new FileOutputStream( logFile.getAbsolutePath(), true ) );
			}
		}
	}

	protected void getLogFileParms( File logFile, SimpleDateFormat headerFormat ) throws FileNotFoundException,
			IOException {
		// read privious log parameters (creation time & size)
		BufferedReader logFileCreatedReader = new BufferedReader( new FileReader( logFile ) );
		String commentLine = logFileCreatedReader.readLine();
		logFileCreatedReader.close();
		logFileCreatedAt = new Date( logFile.lastModified() );
		if( commentLine != null ) {
			try {
				logFileCreatedAt = headerFormat.parse( commentLine );
			} catch( ParseException e ) {}
		}
		logFileSize = logFile.length();
	}

// Configuration class

	static public class ConfigClass {

		public String LogNaming;
		public long MinLogNumber;
		public long MaxLogNumber;
		public RollOver RollOver;
		public OldLogs OldLogs;

		static public class RollOver {
			public Date StartingAt; // rollover from...
			public Date Period; // rollover each...
			public long Size;
			public boolean Append;
			public RollOver() {
				StartingAt = new Date( 0 );
				Period = new Date( 24 * 60 * 60 * 1000 );
				Size = 0;
				Append = false;
			}
		}

		static public class OldLogs {
			public Object Action;
			public boolean Delete;
			public long MaxNumber;
			public Date MaxTime;
			public OldLogs() {
				Action = new Object();
				Delete = false;
				MaxNumber = 0;
				MaxTime = new Date( 0 );
			}
		}

		public ConfigClass() {
			LogNaming = "";
			MinLogNumber = 0;
			MaxLogNumber = 999;
			RollOver = new RollOver();
			OldLogs = new OldLogs();
		}
	}

}
