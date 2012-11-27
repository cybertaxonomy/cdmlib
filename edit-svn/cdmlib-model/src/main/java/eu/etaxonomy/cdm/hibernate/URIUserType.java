/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate;


import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.TypeMismatchException;
import org.hibernate.usertype.UserType;

/**
 * This class maps java.net.URI to varchar(255)
 * @author a.mueller
 *
 */
public class URIUserType implements UserType, Serializable  {
	private static final long serialVersionUID = -5825017496962569105L;
	
	/**
     * SQL type for this usertype.
     */
    private static final int[] SQL_TYPES = {Types.VARCHAR};

    /**
     * @return returns SQL type for the column to which the UserType is mapped
     */
   public int[] sqlTypes() {
        return SQL_TYPES;
    }

    /**
     * @return returns the class of the instance being managed by the UserType
     */
    public Class returnedClass() {
        return URI.class;
    }

    /**
     * @param x first object to compare
     * @param y second object to compare
     * @return comparison result
     * @throws HibernateException
     */
    public boolean equals(Object x, Object y) {
        if (x == y) {
            return true;
        } else if (x == null || y == null) {
            return false;
        } else {
            return x.equals(y);
        }
    }

    /**
     * @param resultSet resultset object
     * @param names names of the columns in the resultset
     * @param owner parent object on which the value is to be set
     * @return returns URI object
     * @throws SQLException throws exception when error occurs in accessing the resultSet
     */
    public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner)
            throws SQLException {
        String strURI = resultSet.getString(names[0]);
        URI uri = null;
        if (null != strURI) {
            try {
                uri = new URI(strURI);
            } catch (URISyntaxException e) {
                throw new TypeMismatchException(e);
            }
        }
        return uri;
    }

    /**
     * @param statement prepared statement object
     * @param value value being set in the statement
     * @param index location of the value in the statement
     * @throws SQLException throws exception when error occurs in accessing the statement
     */
    public void nullSafeSet(PreparedStatement statement, Object value, int index)
            throws SQLException {
        if (value == null) {
            statement.setString(index, null);
        } else {
            URI uri = (URI) value;
            statement.setString(index, uri.toString());
        }
    }

    /**
     * @param value value being copied
     * @return copied value
     */
    public Object deepCopy(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return new URI(((URI) value).toString());
        } catch (URISyntaxException e) {
            throw new TypeMismatchException(e);
        }
    }

    /**
     * @return returns false
     */
    public boolean isMutable() {
        return false;
    }

    /**
     * @param arg0 object to be assembled
     * @param arg1 object to be assembled
     * @return returns assembled object
     */
    public Object assemble(Serializable arg0, Object arg1) {
        return (Serializable) deepCopy(arg0);
    }

    /**
     * @param arg0 object to be disassembled
     * @return returns disassembled object
     */
    public Serializable disassemble(Object arg0) {
        return (Serializable) deepCopy(arg0);
    }

    /**
     * @param arg0 object whose hashcode is to be determined
     * @return returns 0
     */
    public int hashCode(Object arg0) {
        return 0;
    }

    /**
     * @param arg0 argument 0
     * @param arg1 argument 1
     * @param arg2 argument 2
     * @return returns arg0
     */
    public Object replace(Object arg0, Object arg1, Object arg2) {
        return arg0;
    }
}
