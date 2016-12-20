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
import eu.etaxonomy.cdm.model.common.ExtensionType;

/**
 * @author a.mueller
 * @created 10.06.2009
 * @version 1.0
 */
public class DbIntegerExtensionMapper extends DbExtensionMapper {
	private static final Logger logger = Logger.getLogger(DbIntegerExtensionMapper.class);



	public static DbIntegerExtensionMapper NewInstance(ExtensionType extensionType, String dbAttributeString){
		return new DbIntegerExtensionMapper(extensionType, dbAttributeString);
	}


	/**
	 * @param dbAttributeString
	 * @param cdmAttributeString
	 */
	private DbIntegerExtensionMapper(ExtensionType extensionType, String dbAttributeString) {
		super(extensionType, dbAttributeString);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbAnnotationMapper#getValue(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
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

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbAnnotationMapper#getSqlType()
	 */
	@Override
	protected int getSqlType() {
		return Types.INTEGER;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbAnnotationMapper#getTypeClass()
	 */
	@Override
	public Class<?> getTypeClass() {
		return Integer.class;
	}





}
