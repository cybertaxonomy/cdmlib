// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.out.mapper;

import org.apache.log4j.Logger;
import org.hsqldb.Types;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
public class DbStringMapper extends DbSingleAttributeExportMapperBase<DbExportStateBase<?>> implements IDbExportMapper<DbExportStateBase<?>> {
	private static final Logger logger = Logger.getLogger(DbStringMapper.class);
	
	public static DbStringMapper NewInstance(String cdmAttributeString, String dbAttributeString){
		return new DbStringMapper(cdmAttributeString, dbAttributeString, null, true);
	}
	
	public static DbStringMapper NewFacultativeInstance(String cdmAttributeString, String dbAttributeString){
		return new DbStringMapper(cdmAttributeString, dbAttributeString, null, false);
	}

	public static DbStringMapper NewInstance(String cdmAttributeString, String dbAttributeString, String defaultValue){
		return new DbStringMapper(cdmAttributeString, dbAttributeString, defaultValue, false);
	}
	
	public static DbStringMapper NewInstance(String cdmAttributeString, String dbAttributeString, String defaultValue, boolean obligatory){
		return new DbStringMapper(cdmAttributeString, dbAttributeString, defaultValue, obligatory);
	}

	/**
	 * @param dbAttributeString
	 * @param cdmAttributeString
	 */
	private DbStringMapper(String cdmAttributeString, String dbAttributeString, String defaultValue, boolean obligatory) {
		super(cdmAttributeString, dbAttributeString, defaultValue, obligatory);
	}
	


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValue(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	@Override
	protected Object getValue(CdmBase cdmBase) {
		String result = (String)super.getValue(cdmBase);
		if (result != null && result.length() > getPrecision()){
			logger.warn("Truncation (" + result.length() + "->" + getPrecision() + ") needed for Attribute " + getDestinationAttribute() + " in " +  cdmBase + "." );
			result = result.substring(0, getPrecision());
		}
		return result;
	}
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValueType()
	 */
	@Override
	protected int getSqlType() {
		return Types.VARCHAR;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase#getTypeClass()
	 */
	@Override
	public Class<?> getTypeClass() {
		return String.class;
	}

}
