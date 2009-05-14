/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.berlinModel.out;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.CreatedAndNotesMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbObjectMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.IdMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.MethodMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.RefDetailMapper;
import eu.etaxonomy.cdm.io.common.IExportConfigurator;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelNomStatusExport extends BerlinModelExportBase {
	private static final Logger logger = Logger.getLogger(BerlinModelNomStatusExport.class);

	private static int modCount = 1000;
	private static final String dbTableName = "NomStatusRel";
	private static final String pluralString = "NomStatus";
	private static final Class<? extends CdmBase> standardMethodParameter = NomenclaturalStatus.class;


	public BerlinModelNomStatusExport(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IExportConfigurator config){
		boolean result = true;
		logger.warn("Checking for " + pluralString + " not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	private BerlinModelExportMapping getMapping(){
		String tableName = dbTableName;
		BerlinModelExportMapping mapping = new BerlinModelExportMapping(tableName);
//		NameFk
//		NomStatusFk
//		NomStatusRefFk
//		NomStatusRefDetailFk
//		DoubtfulFlag
		
		mapping.addMapper(DbObjectMapper.NewInstance("fromName", "NameFk"));
		mapping.addMapper(DbObjectMapper.NewInstance("toName", "NameFk1"));

		
		mapping.addMapper(MethodMapper.NewInstance("NomStatusFk", this));
		
		mapping.addMapper(DbObjectMapper.NewInstance("citation", "NomStatusRefFk"));
		mapping.addMapper(RefDetailMapper.NewInstance("citationMicroReference","citation", "NomStatusRefDetailFk"));
		
		mapping.addMapper(CreatedAndNotesMapper.NewInstance());
		
		NomenclaturalStatus r = null;
		
		
		return mapping;
	}
	
	protected boolean doInvoke(BerlinModelExportState<BerlinModelExportConfigurator> state){
		try{
			logger.info("start make " + pluralString + " ...");
			boolean success = true ;
			doDelete(state);
			
			TransactionStatus txStatus = startTransaction(true);
			
			List<RelationshipBase> list = getNameService().getAllRelationships(100000000, 0);
			
			BerlinModelExportMapping mapping = getMapping();
			mapping.initialize(state);
			
			int count = 0;
			for (RelationshipBase<?,?,?> rel : list){
				if (rel.isInstanceOf(NameRelationship.class) || rel.isInstanceOf(HybridRelationship.class )){
					doCount(count++, modCount, pluralString);
					success &= mapping.invoke(rel);
				}
			}
			commitTransaction(txStatus);
			logger.info("end make " + pluralString + " ...");
			return success;
		}catch(SQLException e){
			e.printStackTrace();
			logger.error(e.getMessage());
			return false;
		}
	}

	
	protected boolean doDelete(BerlinModelExportState<BerlinModelExportConfigurator> state){
		BerlinModelExportConfigurator bmeConfig = state.getConfig();
		
		String sql;
		Source destination =  bmeConfig.getDestination();
		//RelPTaxon
		sql = "DELETE FROM RelPTaxon";
		destination.setQuery(sql);
		destination.update(sql);

		return true;
	}
		
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IExportConfigurator config){
		return ! ((BerlinModelExportConfigurator)config).isDoTaxonNames();
	}
	
	//called by MethodMapper
	@SuppressWarnings("unused")
	private static Integer getNomStatusFk(NomenclaturalStatus status){
		return BerlinModelTransformer.nomStatus2nomStatusFk(status);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.BerlinModelExportBase#getStandardMethodParameter()
	 */
	@Override
	public Class<? extends CdmBase> getStandardMethodParameter() {
		return standardMethodParameter;
	}
}
