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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hsqldb.Types;

import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.berlinModel.out.BerlinModelExportConfigurator;
import eu.etaxonomy.cdm.io.berlinModel.out.BerlinModelExportMapping;
import eu.etaxonomy.cdm.io.berlinModel.out.BerlinModelExportState;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;

/**
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
//TODO inheritence
public class NomStatusMapper /*extends DbSingleAttributeExportMapperBase<BerlinModelExportState<?>>*/ implements IDbExportMapper<BerlinModelExportState<?>>{
	private static final Logger logger = Logger.getLogger(NomStatusMapper.class);
	
	private BerlinModelExportMapping mapping = null; 
	private BerlinModelExportMapping nameMapping = null; 
	private String dbTableName = "NomStatusRel";
	protected BerlinModelExportState<?> state;
	
	
	public static NomStatusMapper NewInstance(){
		return new NomStatusMapper();
	}
	

	private BerlinModelExportMapping getStatusMapping(){
		boolean doExecute = false;
		String tableName = dbTableName;
		BerlinModelExportMapping mapping = new BerlinModelExportMapping(tableName);

		mapping.addMapper(MethodMapper.NewInstance("NomStatusFk", this.getClass(), "getNomStatusFk", NomenclaturalStatus.class));
		mapping.addMapper(DbObjectMapper.NewInstance("citation", "NomStatusRefFk"));
		mapping.addMapper(RefDetailMapper.NewInstance("citationMicroReference","citation", "NomStatusRefDetailFk"));
		
		mapping.addMapper(CreatedAndNotesMapper.NewInstance());
		//TODO
//		DoubtfulFlag
		
		return mapping;
	}



	private BerlinModelExportMapping getNameMapping(){
		boolean doExecute = true;
		String tableName = dbTableName;
		BerlinModelExportMapping mapping = new BerlinModelExportMapping(dbTableName);
		mapping.addMapper(IdMapper.NewInstance("NameFk"));
		return mapping;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#initialize(java.sql.PreparedStatement, eu.etaxonomy.cdm.io.berlinModel.out.mapper.IndexCounter, eu.etaxonomy.cdm.io.berlinModel.out.DbExportState)
	 */
	public void initialize(PreparedStatement stmt, IndexCounter index,BerlinModelExportState<?> state, String tableName) {
		this.state = state;
		
		try {
			mapping = getStatusMapping();
			nameMapping = getNameMapping();
			mapping.initialize(state);
	//		nameMapping.initialize(state, mapping.getPreparedStatement());
		} catch (SQLException e) {
			logger.error("SQLException in NomStatusMapper.initialize()");
			throw new RuntimeException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.IDbExportMapper#invoke(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	public boolean invoke(CdmBase cdmBase) throws SQLException {
		boolean result = true;
		try {
			Set<NomenclaturalStatus> statusSet = ((TaxonNameBase)cdmBase).getStatus();
			for (NomenclaturalStatus status : statusSet){
				result &= mapping.invoke(status);
				result &= nameMapping.invoke(cdmBase);
			}
			return result; 
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	protected boolean doDelete(BerlinModelExportState<BerlinModelExportConfigurator> state){
		BerlinModelExportConfigurator bmeConfig = state.getConfig();
		
		String sql;
		Source destination =  bmeConfig.getDestination();
		//NomStatusRel
		sql = "DELETE FROM NomStatusRel";
		destination.setQuery(sql);
		destination.update(sql);
		return true;
	}
	
	//called by MethodMapper
	@SuppressWarnings("unused")
	private static Integer getNomStatusFk(NomenclaturalStatus status){
		return BerlinModelTransformer.nomStatus2nomStatusFk(status.getType());
	}

}
