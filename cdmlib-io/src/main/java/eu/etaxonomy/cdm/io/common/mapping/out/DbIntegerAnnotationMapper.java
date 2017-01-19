/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.out;

import java.sql.Types;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 10.06.2009
 */
public class DbIntegerAnnotationMapper extends DbAnnotationMapper {
	private static final Logger logger = Logger.getLogger(DbIntegerAnnotationMapper.class);



	public static DbIntegerAnnotationMapper NewInstance(String annotationPrefix, String dbAttributeString){
		return new DbIntegerAnnotationMapper(annotationPrefix, dbAttributeString, null);
	}

	public static DbIntegerAnnotationMapper NewInstance(String annotationPrefix, String dbAttributeString, Integer defaultValue){
		return new DbIntegerAnnotationMapper(annotationPrefix, dbAttributeString, defaultValue);
	}

	/**
	 * @param dbAttributeString
	 * @param cdmAttributeString
	 */
	private DbIntegerAnnotationMapper(String annotationPrefix, String dbAttributeString, Integer defaultValue) {
		super("annotations", dbAttributeString, defaultValue, null);
	}

	@Override
	protected Object getValue(CdmBase cdmBase) {
		String strValue = (String)super.getValue(cdmBase);
		Integer intValue;
		try {
			intValue = (strValue == null) ? null : Integer.valueOf(strValue);
		} catch (NumberFormatException e) {
			logger.warn("Annotation could not be casted to Integer: " + strValue);
			return null;
		}
		return intValue;
	}

	@Override
	protected int getSqlType() {
		return Types.INTEGER;
	}

	@Override
	public Class<?> getTypeClass() {
		return Integer.class;
	}





}
