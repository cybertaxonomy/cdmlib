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
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.mapping.out.CdmDbExportMapping;
import eu.etaxonomy.cdm.io.common.mapping.out.CreatedAndNotesMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbBooleanMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbExtensionMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbIntegerExtensionMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbMarkerMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbObjectMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbStringMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.MethodMapper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelTaxonExport extends BerlinModelExportBase<TaxonBase> {
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
	protected boolean doCheck(BerlinModelExportState state){
		boolean result = true;
		logger.warn("Checking for " + pluralString + " not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	private CdmDbExportMapping<BerlinModelExportState, BerlinModelExportConfigurator> getMapping(){
		String tableName = dbTableName;
		CdmDbExportMapping<BerlinModelExportState, BerlinModelExportConfigurator> mapping = new CdmDbExportMapping<BerlinModelExportState, BerlinModelExportConfigurator>(tableName);
		mapping.addMapper(DbObjectMapper.NewInstance("name", "PTNameFk"));
		mapping.addMapper(DbObjectMapper.NewInstance("sec", "PTRefFk"));
		mapping.addMapper(MethodMapper.NewInstance("StatusFk", this));
		mapping.addMapper(MethodMapper.NewInstance("DoubtfulFlag", this) );
		mapping.addMapper(DbBooleanMapper.NewInstance("useNameCache", "UseNameCacheFlag", false));
		mapping.addMapper(DbStringMapper.NewFacultativeInstance("appendedPhrase", "NamePhrase"));
		
		//detail
		ExtensionType detailExtensionType = (ExtensionType)getTermService().find(BerlinModelTransformer.DETAIL_EXT_UUID);
		if (detailExtensionType != null){
			mapping.addMapper(DbExtensionMapper.NewInstance(detailExtensionType, "Detail"));
		}
		//idInSource
		ExtensionType idInSourceExtensionType = (ExtensionType)getTermService().find(BerlinModelTransformer.ID_IN_SOURCE_EXT_UUID);
		if (idInSourceExtensionType != null){
			mapping.addMapper(DbIntegerExtensionMapper.NewInstance(idInSourceExtensionType, "IdInSource"));
		}
//		//namePhrase
//		ExtensionType namePhraseExtensionType = (ExtensionType)getTermService().getTermByUuid(BerlinModelTaxonImport.APPENDED_TITLE_PHRASE);
//		if (namePhraseExtensionType != null){
//			mapping.addMapper(DbExtensionMapper.NewInstance(namePhraseExtensionType, "NamePhrase"));
//		}
//		//useNameCacheFlag
//		MarkerType useNameCacheMarkerType = (MarkerType)getTermService().getTermByUuid(BerlinModelTaxonImport.USE_NAME_CACHE);
//		if (useNameCacheMarkerType != null){
//			mapping.addMapper(DbMarkerMapper.NewInstance(useNameCacheMarkerType, "UseNameCacheFlag", false));
//		}
		//publisheFlag
		mapping.addMapper(DbMarkerMapper.NewInstance(MarkerType.PUBLISH(), "PublishFlag", true));
		
		//notes
		mapping.addMapper(CreatedAndNotesMapper.NewInstance());

		//TODO
//	    ,[RIdentifier]
//		,ProParte etc.

		return mapping;
	}
	
	protected void doInvoke(BerlinModelExportState state){
		try{
			logger.info("start make " + pluralString + " ...");
			boolean success = true ;
			doDelete(state);
			
			TransactionStatus txStatus = startTransaction(true);
			
			List<TaxonBase> list = getTaxonService().list(null,100000000, 0, null, null);
			
			CdmDbExportMapping<BerlinModelExportState, BerlinModelExportConfigurator> mapping = getMapping();
			mapping.initialize(state);
			
			int count = 0;
			for (TaxonBase<?> taxon : list){
				doCount(count++, modCount, pluralString);
				success &= mapping.invoke(taxon);
			}
			commitTransaction(txStatus);
			logger.info("end make " + pluralString + " ..." + getSuccessString(success));
			if (!success){
				state.setUnsuccessfull();
			}
			return;
		}catch(SQLException e){
			e.printStackTrace();
			logger.error(e.getMessage());
			state.setUnsuccessfull();
			return;
		}
	}

	

	
	protected boolean doDelete(BerlinModelExportState state){
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
	protected boolean isIgnore(BerlinModelExportState state){
		return ! state.getConfig().isDoTaxa();
	}
	
	//called by MethodMapper
	@SuppressWarnings("unused")
	private static Integer getStatusFk(TaxonBase<?> taxon){
		return BerlinModelTransformer.taxonBase2statusFk(taxon);
	}
	
	@SuppressWarnings("unused")
	private static String getDoubtfulFlag(TaxonBase<?> taxon){
		if (taxon.isDoubtful()){
			return "d";
		}else{
			return "a";
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.BerlinModelExportBase#getStandardMethodParameter()
	 */
	@Override
	public Class<? extends CdmBase> getStandardMethodParameter() {
		return standardMethodParameter;
	}
}
