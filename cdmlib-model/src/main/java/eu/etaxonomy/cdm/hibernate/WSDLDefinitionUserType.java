/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.hibernate;

import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import com.ibm.wsdl.factory.WSDLFactoryImpl;

import eu.etaxonomy.cdm.model.common.LSIDWSDLLocator;

/**
 * UserType which allows persistence of a wsdl definition - used to persist the 
 * wsdl definition of an LSIDAuthority
 * 
 * @author ben
 *
 * @see org.cateproject.model.lsid.PersistableLSIDAuthority
 * @see org.hibernate.usertype.UserType UserType
 * @see javax.wsdl.Definition Definition
 */
public class WSDLDefinitionUserType implements UserType {
	private static Log log = LogFactory.getLog(WSDLDefinitionUserType.class);
	private static final int[] TYPES = { Types.CLOB };

	public Object deepCopy(Object o) throws HibernateException {
		
		if (o == null) {
            return null;
        }
		
		Definition d = (Definition) o;

        try {
        	WSDLFactory wsdlFactory = WSDLFactoryImpl.newInstance();
        	StringWriter stringWriter = new StringWriter();
    		WSDLWriter writer = wsdlFactory.newWSDLWriter();
    	    writer.writeWSDL(d, stringWriter);
    		WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
    		Reader reader = new StringReader(stringWriter.getBuffer().toString());
    		WSDLLocator locator = new LSIDWSDLLocator("wsdl",reader,Thread.currentThread().getContextClassLoader());
    		Definition definition = wsdlReader.readWSDL(locator);
			return definition;
		} catch (Exception e) {
			throw new HibernateException(e);
		}
	}

	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		try {
			WSDLFactory wsdlFactory = WSDLFactoryImpl.newInstance();
			WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
			Reader reader = new StringReader(cached.toString());
			WSDLLocator locator = new LSIDWSDLLocator("wsdl",reader,Thread.currentThread().getContextClassLoader());
			Definition definition = wsdlReader.readWSDL(locator);
			return definition;
		} catch (Exception e) {
			throw new HibernateException(e);
		}
	}
	
	public Serializable disassemble(Object value) throws HibernateException {
		try {
			WSDLFactory wsdlFactory = WSDLFactoryImpl.newInstance();
			Definition definition = (Definition) value;
			StringWriter stringWriter = new StringWriter();
    		WSDLWriter writer = wsdlFactory.newWSDLWriter();
    	    writer.writeWSDL(definition, stringWriter);
    	    return stringWriter.getBuffer().toString();
		} catch (WSDLException e) {
			throw new HibernateException(e);
		}
	}

	public boolean equals(Object x, Object y) throws HibernateException {
		return (x == y) || (x != null && y != null && (x.equals(y)));
	}

	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	public boolean isMutable() {
		return true;
	}

	public Object nullSafeGet(ResultSet resultSet, String[] names, Object o)
			throws HibernateException, SQLException {
		Clob val = (Clob) resultSet.getClob(names[0]);
		if(val == null) {
			return null;
		} else {
            try {
            	WSDLFactory wsdlFactory = WSDLFactoryImpl.newInstance();
    			WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
    			Reader reader = val.getCharacterStream();
    			WSDLLocator locator = new LSIDWSDLLocator("wsdl",reader,Thread.currentThread().getContextClassLoader());
    			Definition definition = wsdlReader.readWSDL(locator);
    			return definition;
		    } catch (Exception e) {
			    throw new HibernateException(e);
		    }
		}
	}

	public void nullSafeSet(PreparedStatement preparedStatement, Object o, int index)
			throws HibernateException, SQLException {
		if (null == o) { 
            preparedStatement.setNull(index, Types.CLOB); 
        } else { 
			try {
				Definition definition = (Definition) o;
				WSDLFactory wsdlFactory = WSDLFactoryImpl.newInstance();
				StringWriter stringWriter = new StringWriter();
	    		WSDLWriter writer = wsdlFactory.newWSDLWriter();
	    	    writer.writeWSDL(definition, stringWriter);
	        	preparedStatement.setClob(index, Hibernate.createClob(stringWriter.getBuffer().toString()));
			} catch (WSDLException e) {
				throw new HibernateException(e);
			}
			
        }
	}

	public Object replace(Object original, Object target, Object owner)
			throws HibernateException {
		return original;
	}

	public Class returnedClass() {
		return Definition.class;
	}

	public int[] sqlTypes() {
		return TYPES;
	}

}
