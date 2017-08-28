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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;
import org.jadira.usertype.dateandtime.shared.spi.AbstractUserType;

import eu.etaxonomy.cdm.model.common.IKeyTerm;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.metadata.CdmMetaDataPropertyName;
import eu.etaxonomy.cdm.model.molecular.SequenceDirection;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.RankClass;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.AuthorityType;
import eu.etaxonomy.cdm.model.reference.ReferenceType;

/**
 * User type for IEnumTerm
 * Partly copied from http://stackoverflow.com/questions/9839553/hibernate-map-enum-to-varchar
 * @author a.mueller
 * @created 15-07-2013
 */
public class EnumUserType<E extends Enum<E>>  extends AbstractUserType implements UserType, ParameterizedType {
	private static final long serialVersionUID = 4641078915907621907L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(EnumUserType.class);

	private Class<E> clazz = null;

	public EnumUserType(){}

    public EnumUserType(Class<E> c) {
    	this.clazz = c;
    }


	@Override
	@SuppressWarnings("unchecked")
	public void setParameterValues(Properties parameters) {
		try {
			this.clazz = (Class<E>) Class.forName(parameters.getProperty("enumClass"));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private static final int[] SQL_TYPES = { Types.VARCHAR };


	@Override
	public Object deepCopy(Object o) throws HibernateException {
		return o;
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException {
	    return (Serializable)value;
	}

	@Override
	public IKeyTerm nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
			throws HibernateException, SQLException {
        String val = (String) StandardBasicTypes.STRING.nullSafeGet(rs, names, session, owner);

		if(val == null) {
			return null;
		} else {
			// TermType
			if (clazz.equals(TermType.class)){
				return TermType.getByKey(val);
			//Reference Type
			}else if (clazz.equals(ReferenceType.class)){
					return ReferenceType.getByKey(val);
			//OriginalSourceType
			}else if (clazz.equals(OriginalSourceType.class)){
				return OriginalSourceType.getByKey(val);
			//NomenclaturalCode
			}else if (clazz.equals(NomenclaturalCode.class)){
				return NomenclaturalCode.getByKey(val);
			//RankClass
			}else if (clazz.equals(RankClass.class)){
				return RankClass.getByKey(val);
			//SpecimenOrObservationType
			}else if (clazz.equals(SpecimenOrObservationType.class)){
				return SpecimenOrObservationType.getByKey(val);
			//SequenceDirection
			}else if (clazz.equals(SequenceDirection.class)){
				return SequenceDirection.getByKey(val);
            //RegistrationStatus
			}else if (clazz.equals(RegistrationStatus.class)){
                return RegistrationStatus.getByKey(val);
            //CdmMetaDataPropertyName
            }else if (clazz.equals(CdmMetaDataPropertyName.class)){
                return CdmMetaDataPropertyName.getByKey(val);
            //EntityAuthority
            }else if (clazz.equals(AuthorityType.class)){
                return AuthorityType.getByKey(val);
            }else{
	        	throw new IllegalArgumentException(String.format("EnumType %s not supported by %s.", clazz.getSimpleName(), EnumUserType.class.getSimpleName()));
	        }
		}
	}

	@Override
	public void nullSafeSet(PreparedStatement statement, Object value, int index, SharedSessionContractImplementor session)
			throws HibernateException, SQLException {
		if (value == null) {
            StandardBasicTypes.STRING.nullSafeSet(statement, value, index, session);
        } else {
        	IKeyTerm term = (IKeyTerm)value;
            StandardBasicTypes.STRING.nullSafeSet(statement, term.getKey(), index, session);
        }
	}

	@Override
	public Class<E> returnedClass() {
		return clazz;
	}

	@Override
	public int[] sqlTypes() {
		return SQL_TYPES;
	}


}
