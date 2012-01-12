// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.out;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hsqldb.Types;

import eu.etaxonomy.cdm.io.common.DbExportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
public class DbObjectMapper extends DbSingleAttributeExportMapperBase<DbExportStateBase<?>> implements IDbExportMapper<DbExportStateBase<?>> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbObjectMapper.class);
	
	public static DbObjectMapper NewInstance(String cdmAttributeString, String dbAttributeString){
		return new DbObjectMapper(cdmAttributeString, dbAttributeString, null);
	}
	
	/**
	 * @param dbAttributeString
	 * @param cdmAttributeString
	 */
	protected DbObjectMapper(String cdmAttributeString, String dbAttributeString, Object defaultValue) {
		super(cdmAttributeString, dbAttributeString, defaultValue);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValue()
	 */
	@Override
	protected Object getValue(CdmBase cdmBase) {
		CdmBase value = (CdmBase)super.getValue(cdmBase);
		if (value == null){
			return null;
		}
		if (! Hibernate.isInitialized(value)){
			Hibernate.initialize(value);
		}
		Object result = getId(value);
//		getState().getConfig().getCdmAppController().commitTransaction(tx);
		return result;
	}
	

	protected Integer getId(CdmBase cdmBase){
		DbExportStateBase<?> state = getState();
		DbExportConfiguratorBase config = state.getConfig();
		if (false && config.getIdType() == DbExportConfiguratorBase.IdType.CDM_ID){
			return cdmBase.getId();
		}else{
			Integer id = getState().getDbId(cdmBase);
			return id;
		}
	}	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValueType()
	 */
	@Override
	protected int getSqlType() {
		return Types.INTEGER;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase#getTypeClass()
	 */
	@Override
	public Class<?> getTypeClass() {
		return CdmBase.class;
	}

	
}
