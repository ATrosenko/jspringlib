package org.spring.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {

	final static protected String defDateFormatString;

	static {
		defDateFormatString = "yyyy/MM/dd HH:mm:ss";
	}

	static public String NowString() {
		return( DateString( new Date(), defDateFormatString ) );
	}

	static public String NowString( Date date, String template ) {
		return( DateString( new Date(), template ) );
	}

	static public String DateString( Date date ) {
		return( DateString( date, defDateFormatString ) );
	}

	static public String DateString( Date date, String template ) {
		SimpleDateFormat datefmt = new SimpleDateFormat( template );
		datefmt.setTimeZone( TimeZone.getDefault() );
		return( datefmt.format( date ) );
	}

	static public String DateString( Date date, Locale l ) {
		return DateFormat.getDateTimeInstance( DateFormat.MEDIUM, DateFormat.MEDIUM,
																					 l ).format( date );
	}

}
