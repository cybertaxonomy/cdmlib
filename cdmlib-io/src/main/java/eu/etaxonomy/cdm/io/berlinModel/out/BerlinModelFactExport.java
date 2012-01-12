/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.berlinModel.out;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelFactsImport;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.RefDetailMapper;
import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.mapping.out.CdmDbExportMapping;
import eu.etaxonomy.cdm.io.common.mapping.out.CreatedAndNotesMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbIntegerAnnotationMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbMarkerMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbObjectMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.IdMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.MethodMapper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.taxon.Taxon;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelFactExport extends BerlinModelExportBase<TextData> {
	private static final Logger logger = Logger.getLogger(BerlinModelFactExport.class);

	private static int modCount = 2500;
	private static final String dbTableName = "Fact";
	private static final String pluralString = "Facts";
	private static final Class<? extends CdmBase> standardMethodParameter = TextData.class;
	@Deprecated
	private static Source source;

	public BerlinModelFactExport(){
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
		mapping.addMapper(IdMapper.NewInstance("FactId"));
		mapping.addMapper(MethodMapper.NewInstance("PTNameFk", this.getClass(), "getPTNameFk", TextData.class, DbExportStateBase.class));
		mapping.addMapper(MethodMapper.NewInstance("PTRefFk", this.getClass(), "getPTRefFk", TextData.class, DbExportStateBase.class));
		mapping.addMapper(MethodMapper.NewInstance("Fact", this));
		mapping.addMapper(MethodMapper.NewInstance("FactCategoryFk", this));
		
		mapping.addMapper(DbObjectMapper.NewInstance("citation", "FactRefFk"));
		mapping.addMapper(RefDetailMapper.NewInstance("citationMicroReference","citation", "FactRefDetailFk"));
		mapping.addMapper(DbObjectMapper.NewInstance("citation", "PTDesignationRefFk"));
		mapping.addMapper(RefDetailMapper.NewInstance("citationMicroReference","citation", "PTDesignationRefDetailFk"));
		mapping.addMapper(DbMarkerMapper.NewInstance(MarkerType.IS_DOUBTFUL(), "DoubtfulFlag", false));
		mapping.addMapper(DbMarkerMapper.NewInstance(MarkerType.IS_DOUBTFUL(), "PublishFlag", true));
		mapping.addMapper(DbIntegerAnnotationMapper.NewInstance(BerlinModelFactsImport.SEQUENCE_PREFIX, "Sequence", 999));
		mapping.addMapper(CreatedAndNotesMapper.NewInstance());
		
		//TODO
//	       designationRef
		return mapping;
	}
	
	protected void doInvoke(BerlinModelExportState state){
		try{
			logger.info("start make " + pluralString + " ...");
			boolean success = true ;
			doDelete(state);
			
			TransactionStatus txStatus = startTransaction(true);
			
			List<DescriptionBase> list = getDescriptionService().list(null,1000000000, 0,null,null);
			
			CdmDbExportMapping<BerlinModelExportState, BerlinModelExportConfigurator> mapping = getMapping();
			mapping.initialize(state);
			
			this.source = state.getConfig().getDestination(); 
			int count = 0;
			for (DescriptionBase<?> desc : list){
				for (DescriptionElementBase descEl : desc.getElements()){
					doCount(count++, modCount, pluralString);
					if (descEl.isInstanceOf(TextData.class)){
						success &= mapping.invoke(descEl);		
					}else{
						logger.warn (descEl.getClass().getSimpleName() + " not yet supported for Fact Export.");
					}
				}
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
		//Fact
		sql = "DELETE FROM Fact";
		destination.setQuery(sql);
		destination.update(sql);

		return true;
	}
		
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(BerlinModelExportState state){
		return ! state.getConfig().isDoFacts();
	}
	
	//called by MethodMapper
	@SuppressWarnings("unused")
	private static Integer getFactCategoryFk(TextData textData){
		Feature feature = textData.getFeature();
		Integer catFk = BerlinModelTransformer.textData2FactCategoryFk(feature);
		//catFk = 302;
		if (catFk == null){
			catFk = findCategory(feature);
		}
		if (catFk == null){
			//catFk = insertCategory(feature);
		}
		return catFk;
	}
	
	
	//called by MethodMapper
	@SuppressWarnings("unused")
	private static Integer getPTNameFk(TextData textData, DbExportStateBase<?> state){
		return getObjectFk(textData, state, true);
	}
	
	//called by MethodMapper
	@SuppressWarnings("unused")
	private static Integer getPTRefFk(TextData textData, DbExportStateBase<?> state){
		return getObjectFk(textData, state, false);
	}

	private static Integer getObjectFk(TextData textData, DbExportStateBase<?> state, boolean isName){
		DescriptionBase<?> desc = textData.getInDescription();
		if (desc.isInstanceOf(TaxonDescription.class)){
			TaxonDescription taxonDesc = (TaxonDescription)desc;
			Taxon taxon = taxonDesc.getTaxon();
			if (taxon != null){
				CdmBase cdmBase = (isName) ? taxon.getName(): taxon.getSec();
				return state.getDbId(cdmBase);
			}
		}
		logger.warn("No taxon found for description: " + textData.toString());
		return null;
	}
	
	//called by MethodMapper
	@SuppressWarnings("unused")
	private static String getFact(TextData textData){
//		Map<Language, LanguageString> map = textData.getMultilanguageText();
		
		String result = textData.getText(Language.DEFAULT());
		if (result == null){
			Map<Language, LanguageString> map = textData.getMultilanguageText();
			for (Language language : map.keySet()){
				String tmp = textData.getText(language);
				if (! CdmUtils.Nz(tmp).trim().equals("")){
					result = tmp;
					break;
				}
			}
		}
		return result;
	}
	
	private static Map<Feature, Integer> featureMap = new HashMap<Feature, Integer>();
	
	@Deprecated  //TODO quick and dirty for Salvador export
	private static Integer findCategory(Feature feature){
		if (featureMap.get(feature) != null){
			return featureMap.get(feature);
		}
		Integer result = null;
		String label = feature.getLabel();
		ResultSet rs = source.getResultSet("SELECT FactCategoryId FROM FactCategory WHERE FactCategory = '"+label+"'");
		try {
			while (rs.next()){
				if (result != null){
					logger.warn("FactCategory is not distinct: " + label);
				}else{
					result = rs.getInt(1) ;
				}
			}
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return null;
		}
		featureMap.put(feature, result);
		return result;
	}
		
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.BerlinModelExportBase#getStandardMethodParameter()
	 */
	@Override
	public Class<? extends CdmBase> getStandardMethodParameter() {
		return standardMethodParameter;
	}
}
