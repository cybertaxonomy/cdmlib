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
import org.hsqldb.Types;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.CreatedAndNotesMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbBooleanMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbNullMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbObjectMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.MethodMapper;
import eu.etaxonomy.cdm.io.common.IExportConfigurator;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelTaxonExport extends BerlinModelExportBase {
	private static final Logger logger = Logger.getLogger(BerlinModelTaxonExport.class);

	private static int modCount = 1000;
	private static final String dbTableName = "PTaxon";
	private static final String pluralString = "Taxa";
	private static final Class<? extends CdmBase> standardMethodParameter = TaxonBase.class;

	public BerlinModelTaxonExport(){
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
		mapping.addMapper(DbObjectMapper.NewInstance("name", "PTNameFk"));
		mapping.addMapper(DbObjectMapper.NewInstance("sec", "PTRefFk"));
		mapping.addMapper(DbNullMapper.NewInstance("Detail", Types.VARCHAR));
		mapping.addMapper(MethodMapper.NewInstance("StatusFk", this));
		mapping.addMapper(DbBooleanMapper.NewInstance("isDoubtful", "DoubtfulFlag"));
		mapping.addMapper(CreatedAndNotesMapper.NewInstance());

		//TODO
//	        ,[RIdentifier]
//		        ,[IdInSource]
//		        ,ProParte etc.
//		        ,[NamePhrase]
//		        ,[UseNameCacheFlag]
//		        ,[PublishFlag]
//		TaxonBase<?> n = null;
//		n.isDoubtful()
		return mapping;
	}
	
	protected boolean doInvoke(BerlinModelExportState<BerlinModelExportConfigurator> state){
		try{
			logger.info("start make " + pluralString + " ...");
			boolean success = true ;
			doDelete(state);
			
			TransactionStatus txStatus = startTransaction(true);
			
			List<TaxonBase> list = getTaxonService().list(100000000, 0);
			
			BerlinModelExportMapping mapping = getMapping();
			mapping.initialize(state);
			
			int count = 0;
			for (TaxonBase<?> taxon : list){
				doCount(count++, modCount, pluralString);
				success &= mapping.invoke(taxon);
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
		//Fact
		sql = "DELETE FROM Fact";
		destination.setQuery(sql);
		destination.update(sql);
		//PTaxon
		sql = "DELETE FROM PTaxon";
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
	private static Integer getStatusFk(TaxonBase<?> taxon){
		return BerlinModelTransformer.taxonBase2statusFk(taxon);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.BerlinModelExportBase#getStandardMethodParameter()
	 */
	@Override
	public Class<? extends CdmBase> getStandardMethodParameter() {
		return standardMethodParameter;
	}
}
