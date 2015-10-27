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

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.UserType;
import org.jadira.usertype.dateandtime.shared.spi.AbstractUserType;

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
public class WSDLDefinitionUserType extends AbstractUserType implements UserType {
	private static final long serialVersionUID = 186785968465961559L;
	private static final int[] SQL_TYPES = { Types.CLOB };

	@Override
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


	//not tested if this works with jadira.usertype
	@Override
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

	//not tested if this works with jadira.usertype
	@Override
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



	@Override
	public Definition nullSafeGet(ResultSet rs, String[]names, SessionImplementor session, Object o)
			throws HibernateException, SQLException {
		Clob val = (Clob)StandardBasicTypes.CLOB.nullSafeGet(rs, names, session, o);
//		Clob val = (Clob) rs.getClob(names[0]);
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



	@Override
	public void nullSafeSet(PreparedStatement statement, Object value, int index, SessionImplementor session)
			throws HibernateException, SQLException {
		if (value == null) {
//            statement.setNull(index, Types.CLOB);   //old version
            StandardBasicTypes.CLOB.nullSafeSet(statement, value, index, session);
        } else {
			try {
				Definition definition = (Definition) value;
				WSDLFactory wsdlFactory = WSDLFactoryImpl.newInstance();
				StringWriter stringWriter = new StringWriter();
	    		WSDLWriter writer = wsdlFactory.newWSDLWriter();
	    	    writer.writeWSDL(definition, stringWriter);
//	    	    statement.setClob(index, Hibernate.createClob(stringWriter.getBuffer().toString()));  //old version
	        	StandardBasicTypes.CLOB.nullSafeSet(statement, stringWriter.getBuffer().toString(), index, session);
			} catch (WSDLException e) {
				throw new HibernateException(e);
			}

        }
	}


	@Override
    public Class returnedClass() {
		return Definition.class;
	}


	@Override
	public int[] sqlTypes() {
		return SQL_TYPES;
	}

}
