package org.spring.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.List;

public class Flags
		implements Cloneable, Comparable, Serializable {

	public class Flag {
		public boolean State;
		public String Name;
	}

	public Flags() {
		Settings = new ArrayList();
		String[] slist = this.Initialize();
		for( int i = 0; i < slist.length; i++ ) {
			Flag f = new Flag();
			f.State = false;
			f.Name = slist[i].toUpperCase();
			Settings.add( f );
		}
	}

	public Flags( int[] f ) {
		this();
		this.Set( f );
	}

	public Flags( String[] f ) throws NoSuchElementException {
		this();
		this.Set( f );
	}

	public Flags( Flags fs ) throws NoSuchElementException {
		this();
		for( int i = 0; i < Settings.size(); i++ ) {
			Flag f = new Flag();
			f.State = ( ( Flag )fs.Settings.get( i ) ).State;
			f.Name = ( ( Flag )fs.Settings.get( i ) ).Name;
			Settings.add( f );
		}
	}

	protected String[] Initialize() {
		return new String[0];
	}

	private void Set( int i ) throws NoSuchElementException {
		if( Settings.size() <= i || i < 0 ) {
			throw( new NoSuchElementException( "" + i ) );
		}
		( ( Flag )Settings.get( i ) ).State = true;
	}

	public void Set( int[] i ) throws NoSuchElementException {
		for( int j = 0; j < i.length; j++ ) {
			this.Set( i[j] );
		}
	}

	private void Set( String s ) throws NoSuchElementException {
		StringTokenizer st = new StringTokenizer( s, "," );
		while( st.hasMoreTokens() ) {
			String se = st.nextToken().trim();
			if( se.length() > 0 ) {
				int sel;
				for( sel = 0; sel < Settings.size(); sel++ ) {
					if( ( ( Flag )Settings.get( sel ) ).Name.equalsIgnoreCase( se ) ) {
						break;
					}
				}
				if( sel == Settings.size() ) {
					throw( new NoSuchElementException( se ) );
				}
				( ( Flag )Settings.get( sel ) ).State = true;
			}
		}
	}

	public void Set( String[] s ) throws NoSuchElementException {
		for( int i = 0; i < s.length; i++ ) {
			this.Set( s[i] );
		}
	}

	private void Toggle( int i ) throws NoSuchElementException {
		if( Settings.size() <= i || i < 0 ) {
			throw( new NoSuchElementException( "" + i ) );
		}
		( ( Flag )Settings.get( i ) ).State = ! ( ( Flag )Settings.get( i ) ).State;
	}

	public void Toggle( int[] i ) throws NoSuchElementException {
		for( int j = 0; j < i.length; j++ ) {
			this.Set( i[j] );
		}
	}

	private void Toggle( String s ) throws NoSuchElementException {
		int sel;
		for( sel = 0; sel < Settings.size(); sel++ ) {
			if( ( ( Flag )Settings.get( sel ) ).Name.equalsIgnoreCase( s ) ) {
				break;
			}
		}
		if( sel == Settings.size() ) {
			throw( new NoSuchElementException( s ) );
		}
		( ( Flag )Settings.get( sel ) ).State = ! ( ( Flag )Settings.get( sel ) ).
																						State;
	}

	public void Toggle( String[] s ) throws NoSuchElementException {
		for( int i = 0; i < s.length; i++ ) {
			this.Toggle( s[i] );
		}
	}

	public boolean Get( int i ) throws NoSuchElementException {
		if( Settings.size() <= i || i < 0 ) {
			throw( new NoSuchElementException( "" + i ) );
		}
		return( ( Flag )Settings.get( i ) ).State;
	}

	public boolean Get( String s ) throws NoSuchElementException {
		int sel;
		for( sel = 0; sel < Settings.size(); sel++ ) {
			if( ( ( Flag )Settings.get( sel ) ).Name.equalsIgnoreCase( s ) ) {
				break;
			}
		}
		if( sel == Settings.size() ) {
			throw( new NoSuchElementException( s ) );
		}
		return( ( Flag )Settings.get( sel ) ).State;
	}

	public String toString() {
		String result = "";
		for( int i = 0; i < Settings.size(); i++ ) {
			Flag f = ( Flag )Settings.get( i );
			if( f.State == true ) {
				result += ( result.length() != 0 ? "," : "" ) + f.Name;
			}
		}
//    return this.getClass().getName() + "(" + result + ")";
		return result;
	}

	public Object clone() {
		try {
			return super.clone();
		} catch( CloneNotSupportedException e ) {
			throw new Error(
					"Never reaching point - Flags.clone() caused CloneNotSupportedException" );
		}
	}

	public int compareTo( Object o ) {
		for( int sel = 0; ; sel++ ) {
			try {
				boolean a = this.Get( sel );
				try {
					boolean b = ( ( Flags )o ).Get( sel );
					if( a != b ) {
						return( a ? 1 : -1 );
					}
				} catch( NoSuchElementException e ) {
					return 1;
				}
			} catch( NoSuchElementException e ) {
				try {
					( ( Flags )o ).Get( sel );
					return -1;
				} catch( NoSuchElementException e2 ) {
					return 0;
				}
			}
		}
	}

	protected List Settings;
}
