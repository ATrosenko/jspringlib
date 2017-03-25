package org.spring.util;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
import java.util.NoSuchElementException;

public class Selector
		implements Cloneable, Serializable {

	public Selector() {
		Selections = new ArrayList();
		String[] slist = this.Initialize();
		for( int i = 0; i < slist.length; i++ ) {
			Selections.add( slist[i].toUpperCase() );
		}
		Selection = 0;
	}

	public Selector( int s ) {
		this();
		this.Set( s );
	}

	public Selector( String s ) {
		this();
		this.Set( s );
	}

	public Selector( Selector s ) {
		this();
		Selections.clear();
		for( int i = 0; i < s.Selections.size(); i++ ) {
			Selections.add( s.Selections.get( i ) );
		}
	}

	protected String[] Initialize() {
		return new String[0];
	}

	public Selector Set( String s ) throws NoSuchElementException {
		int sel = s == null || s.length() == 0 ? 0 :
							Selections.indexOf( s.toUpperCase() );
		if( sel < 0 ) {
			throw( new NoSuchElementException( s ) );
		}
		Selection = sel;
		return this;
	}

	public Selector Set( int s ) throws NoSuchElementException {
		if( s >= Selections.size() || s < 0 ) {
			throw( new NoSuchElementException( "" + s ) );
		}
		Selection = s;
		return this;
	}

	public int Get() {
		return Selection;
	}

	public String GetString() {
		return( String )Selections.get( Selection );
	}

	public String toString() {
		return this.GetString();
	}

	public Object clone() {
		try {
			return super.clone();
		} catch( CloneNotSupportedException e ) {
			throw new Error(
					"Never reaching point - Selector.clone() caused CloneNotSupportedException" );
		}
	}

	public int compareTo( Object o ) {
		return( Selection - ( ( Selector )o ).Get() );
	}

	protected int Selection;
	protected List Selections;
}
