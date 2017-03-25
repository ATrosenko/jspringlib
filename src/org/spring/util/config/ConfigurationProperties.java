
package org.spring.util.config;

import java.util.*;
import java.lang.reflect.*;

public class ConfigurationProperties {

  protected String MasterKey;
  public Class ConfigStruct;

  public ConfigurationProperties( String MasterKey, Class ConfigStruct ) {
    this.MasterKey = MasterKey;
    this.ConfigStruct = ConfigStruct;
  };

//  public Object[] testread( Properties source ) throws Exception {
//    ConfigStruct.

  public Object[] read( Properties source ) throws Exception {
    Object[] o = new Object[1];
    if ( ConfigStruct.isArray() ) {
      int variants = Integer.parseInt( source.getProperty( MasterKey, "0" ) );
      o = new Object[variants+1];
      for ( int i=1; i<=variants; i++ ) {
        ConfigurationProperties cp = new ConfigurationProperties(
          MasterKey+"["+i+"]", ConfigStruct.getComponentType() );
        o[i] = cp.read( source )[0];
      };
      o[0] = ConfigStruct.getComponentType().newInstance();
    } else
      o[0] = ConfigStruct.newInstance();
    Field[] f = o[0].getClass().getFields();
    for ( int i=0; i<f.length; i++ ) {
      String fieldName = f[i].getName();
      String fieldType = f[i].getType().getName();
//      if ( fieldType.equalsIgnoreCase("void") )
//        continue;
      String propName = MasterKey+(fieldName.equalsIgnoreCase("_Comment")?"":"."+fieldName);
      String value = source.getProperty( propName );
      if ( value==null && ( f[i].getType().isPrimitive() ||
          fieldType.equalsIgnoreCase("java.lang.String") ) )
        continue;
      try {
        if ( fieldType.equalsIgnoreCase("boolean") ) {
//          f[i].set( o[0], null );
          // read values for booleans
          Object[] trueValues=null, falseValues=null;
          try {
            Method m;
            m = o[0].getClass().getMethod( "_"+fieldName+"_True_Values", null );
            trueValues = (Object[])m.invoke( o[0], null );
            m = o[0].getClass().getMethod( "_"+fieldName+"_False_Values", null );
            falseValues = (Object[])m.invoke( o[0], null );
          } catch ( NoSuchMethodException e ) {};
          if ( trueValues==null && falseValues==null ) {
            try {
              Method m;
              m = o[0].getClass().getMethod( "_Boolean_True_Values", null );
              trueValues = (Object[])m.invoke( o[0], null );
              m = o[0].getClass().getMethod( "_Boolean_False_Values", null );
              falseValues = (Object[])m.invoke( o[0], null );
            } catch ( NoSuchMethodException e ) {};
          };
          // default value =false if default true/false values are used
          if ( trueValues==null && falseValues==null )
            f[i].setBoolean( o[0], false );
          // default values for boolean
          if ( trueValues==null )
            trueValues = new String[] { "true", "on", "enable", "enabled" };
          if ( falseValues==null )
            falseValues = new String[] { "false", "off", "disable", "disabled", "0" };
          // parsing boolean value
//          if ( value!=null ) {
            for ( int itrue=0; itrue<trueValues.length; itrue++ ) {
              if ( value.equalsIgnoreCase((String)trueValues[itrue]) ) {
                f[i].setBoolean( o[0], true );
                break;
              };
            };
            for ( int ifalse=0; ifalse<falseValues.length; ifalse++ ) {
              if ( value.equalsIgnoreCase((String)falseValues[ifalse]) ) {
                f[i].setBoolean( o[0], false );
                break;
              };
            };
//          };
          // setting default value
          if ( f[i].get(o[0])==null ) {
            if ( trueValues.length==0 )
              f[i].setBoolean( o[0], true );
            else if ( falseValues.length==0 )
              f[i].setBoolean( o[0], false );
          };
          if ( f[i].get(o[0])==null )
            throw ( new IllegalArgumentException( "'"+propName+"'='"+value+"'" ) );
        } else if ( fieldType.equalsIgnoreCase("byte") ) {
          f[i].setByte( o[0], Byte.parseByte(value) );
        } else if ( fieldType.equalsIgnoreCase("short") ) {
          f[i].setShort( o[0], Short.parseShort(value) );
        } else if ( fieldType.equalsIgnoreCase("int") ) {
          f[i].setInt( o[0], Integer.parseInt(value) );
        } else if ( fieldType.equalsIgnoreCase("long") ) {
          f[i].setLong( o[0], Long.parseLong(value) );
        } else if ( fieldType.equalsIgnoreCase("float") ) {
          f[i].setFloat( o[0], Float.parseFloat(value) );
        } else if ( fieldType.equalsIgnoreCase("double") ) {
          f[i].setDouble( o[0], Double.parseDouble(value) );
        } else if ( fieldType.equalsIgnoreCase("char") ) {
          if ( value.length()!=1 )
            throw ( new IllegalArgumentException() );
          f[i].setChar( o[0], value.charAt(0) );
        } else if ( fieldType.equalsIgnoreCase("java.lang.String") ) {
          f[i].set( o[0], value );
        } else {
          ConfigurationProperties cp2 =
            new ConfigurationProperties( MasterKey+"."+fieldName, f[i].getType() );
          f[i].set( o[0], cp2.read(source)[0] );
        };
      } catch ( NumberFormatException e ) {
        throw ( new IllegalArgumentException( "'"+propName+"'='"+value+"'" ) );
      } catch ( IllegalArgumentException e ) {
        throw ( new IllegalArgumentException( "'"+propName+"'='"+value+"'" ) );
      };
      try {
        o[0].getClass().getMethod( "_"+fieldName+"_Validator", null ).invoke( o[0], null );
      } catch ( NoSuchMethodException e ) {};
//      } catch ( Exception e ) {
//        throw ( new IllegalArgumentException( "'"+propName+"'='"+value+"'" ) );
//      };
    };
    try {
      o[0].getClass().getMethod( "_Validator", null ).invoke( o[0], null );
    } catch ( NoSuchMethodException e ) {};
    return o;
  };

/*  public Properties write( Object o ) {
    Properties p = new Properties();
    if ( o.getClass().isArray() ) {
      p.setProperty( MasterKey, ""+o.length );
      for ( int i=1; i<=o.length; i++ ) {
        ConfigurationProperties cp = new ConfigurationProperties(
          MasterKey+"["+i+"]", o.getClass().getComponentType() );
        cp.write( o[i] );
      };
    };
    Field[] f = data[0].getClass().getFields();
    for ( int i=0; i<f.length; i++ ) {
      String fieldName = f[i].getName();
      String fieldType = f[i].getType().getName();
      if ( fieldType.equalsIgnoreCase("null") )
        continue;
      String propName = MasterKey+(fieldName.equalsIgnoreCase("_Comment")?"":"."+fieldName);
      String value;
      if ( fieldType.equalsIgnoreCase("boolean") ) {
        // read values for booleans
        Object[] trueValues=null, falseValues=null;
        try {
          trueValues = o[0].getClass().getMethod( "_"+fieldName+"_True_Values", null ).invoke( o[0], null );
          falseValues = o[0].getClass().getMethod( "_"+fieldName+"_False_Values", null ).invoke( o[0], null );
        } catch ( NoSuchMethodException e ) {};
        if ( trueValues==null && falseValues==null ) {
          try {
            trueValues = o[0].getClass().getMethod( "_Boolean_True_Values", null ).invoke( o[0], null );
            falseValues = o[0].getClass().getMethod( "_Boolean_False_Values", null ).invoke( o[0], null );
          } catch ( NoSuchMethodException e ) {};
        };
        // default values for boolean
        if ( trueValues=null )
          trueValues = new String[] { "true", "on", "enable", "enabled" };
        if ( falseValues=null )
          falseValues = new String[] { "false", "off", "disable", "disabled", "0" };
        // writing boolean value
        if ( f[i].getBoolean(o[0])==true ) {
          if ( trueValues.length==0 )
            continue;
          p.setProperty( propName, trueValues[0] );
        } else {
          if ( falseValues.length==0 )
            continue;
          p.setProperty( propName, falseValues[0] );
        };
      } else if ( fieldType.equalsIgnoreCase("java.lang.String") ) {
        p.setProperty( propName, value );
      } else if ( f[i].getType().isPrimitive() ) {
        p.setProperty( propName, new String( f[i].get(o[0]) ) );
      } else {
        ConfigurationProperties cp2 =
          new ConfigurationProperties( MasterKey+"."+fieldName, f[i].getType() );
        p.addAll( cp2.write( f[i].get(o[0]) ) );
      };
    };
    return p;
  };*/

};
