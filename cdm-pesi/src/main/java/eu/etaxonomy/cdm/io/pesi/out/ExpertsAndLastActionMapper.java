// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.pesi.out;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.io.common.mapping.MultipleAttributeMapperBase;
import eu.etaxonomy.cdm.io.common.mapping.out.DbExportNotYetImplementedMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbLastActionMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbSingleAttributeExportMapperBase;
import eu.etaxonomy.cdm.io.common.mapping.out.IDbExportMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.io.common.mapping.out.IndexCounter;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
public class ExpertsAndLastActionMapper extends MultipleAttributeMapperBase<DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>>> implements IDbExportMapper<DbExportStateBase<?, IExportTransformer>, IExportTransformer>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ExpertsAndLastActionMapper.class);
	
	private static final boolean IS_ACTION_TYPE = true;
	
	public static ExpertsAndLastActionMapper NewInstance(){
		return new ExpertsAndLastActionMapper();
	}
	

	/**
	 * @param dbAttributString
	 * @param cdmAttributeString
	 */
	private ExpertsAndLastActionMapper() {
		singleMappers.add(DbLastActionMapper.NewInstance("LastActionDate", ! IS_ACTION_TYPE));
		singleMappers.add(DbLastActionMapper.NewInstance("LastAction", IS_ACTION_TYPE));
		singleMappers.add(DbExportNotYetImplementedMapper.NewInstance("SpeciesExpertName", "Need to better understand what the species expert name is"));
		singleMappers.add(DbExportNotYetImplementedMapper.NewInstance("SpeciesExpertGUID", "SpeciesExpertGUID derives from an external mapeing list: name to GUID from expertsDB"));
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.IDbExportMapper#initialize(java.sql.PreparedStatement, eu.etaxonomy.cdm.io.berlinModel.out.mapper.IndexCounter, eu.etaxonomy.cdm.io.berlinModel.out.DbExportState)
	 */
	public void initialize(PreparedStatement stmt, IndexCounter index, DbExportStateBase<?, IExportTransformer> state, String tableName) {
		for (DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> mapper : singleMappers){
			mapper.initialize(stmt, index, state, tableName);
		}	
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.IDbExportMapper#invoke(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	public boolean invoke(CdmBase cdmBase) throws SQLException {
		boolean result = true;
		for (DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> mapper : singleMappers){
			result &= mapper.invoke(cdmBase);
		}
		return result;
	}

	
}
