/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.out;

import java.sql.SQLException;
import java.sql.Types;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbExportConfiguratorBase.IdType;
import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * Adds a single value to the mapping which represents the id of the mapped object
 *
 * @author a.mueller
 * @created 12.05.2009
 */
public class IdMapper
        extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>>
        implements IDbExportMapper<DbExportStateBase<?, IExportTransformer>, IExportTransformer>{
	private static final Logger logger = Logger.getLogger(IdMapper.class);

	public static IdMapper NewInstance(String dbIdAttributeString){
		return new IdMapper(dbIdAttributeString);
	}

	/**
	 * @param dbAttributString
	 * @param cdmAttributeString
	 */
	protected IdMapper(String dbIdAttributeString) {
		super(null, dbIdAttributeString, null);
	}


	@Override
	public Class<?> getTypeClass() {
		return Integer.class;
	}

	@Override
	protected boolean doInvoke(CdmBase cdmBase) throws SQLException{
		boolean result = super.doInvoke(cdmBase);
		getState().putDbId(cdmBase, (Integer)getValue(cdmBase));
		return result;

	}

	@Override
	protected Object getValue(CdmBase cdmBase) {
		IdType type = getState().getConfig().getIdType();
		if (type == IdType.CDM_ID){
			return cdmBase.getId();
		}else if (type == IdType.CDM_ID_WITH_EXCEPTIONS){
				return getState().getCurrentIO().getDbId(cdmBase, getState());
		}else if(type == IdType.MAX_ID){
			//TODO
			logger.warn("MAX_ID not yet implemented");
			return cdmBase.getId();
		}else if(type == IdType.ORIGINAL_SOURCE_ID){
			//TODO
			logger.warn("ORIGINAL_SOURCE_ID not yet implemented");
			return cdmBase.getId();
		}else{
			logger.warn("Unknown idType: " + type);
			return cdmBase.getId();
		}
	}



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValueType()
	 */
	@Override
	protected int getSqlType() {
		return Types.INTEGER;
	}

}
