/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel;

import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.FACT_DESCRIPTION;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.FACT_DISTRIBUTION_EM;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.FACT_OBSERVATION;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelFactsIO  extends BerlinModelIOBase {
	private static final Logger logger = Logger.getLogger(BerlinModelFactsIO.class);

	private int modCount = 10000;
	
	public BerlinModelFactsIO(){
		super();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IImportConfigurator config){
		boolean result = true;
		logger.warn("Checking for Facts not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}

	private MapWrapper<Feature> invokeFactCategories(BerlinModelImportConfigurator bmiConfig){
		
//		Map<Integer, Feature> featureMap = new HashMap<Integer, Feature>();
		MapWrapper<Feature> result = bmiConfig.getFeatureMap();

		Source source = bmiConfig.getSource();

		
		try {
			//get data from database
			String strQuery = 
					" SELECT FactCategory.* " + 
					" FROM FactCategory "+
                    " WHERE (1=1)";
			ResultSet rs = source.getResultSet(strQuery) ;

			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("FactCategories handled: " + (i-1));}
				
				int factCategoryId = rs.getInt("factCategoryId");
				String factCategory = rs.getString("factCategory");
				
				Feature feature;
				try {
					feature = BerlinModelTransformer.factCategory2Feature(factCategoryId);
				} catch (UnknownCdmTypeException e) {
					logger.warn("New Feature (FactCategoryId: " + factCategoryId + ")");
					feature = Feature.NewInstance(factCategory, factCategory, null);
					feature.setSupportsTextData(true);
					//TODO
//					MaxFactNumber	int	Checked
//					ExtensionTableName	varchar(100)	Checked
//					Description	nvarchar(1000)	Checked
//					locExtensionFormName	nvarchar(80)	Checked
//					RankRestrictionFk	int	Checked
				}
								
			//	featureMap.put(factCategoryId, feature);
				result.put(factCategoryId, feature);
	
			}
			Collection<Feature> col = result.getAllValues();
			getTermService().saveTermsAll(col);
			return result;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return null;
		}

	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	@Override
	protected boolean doInvoke(IImportConfigurator config,
			Map<String, MapWrapper<? extends CdmBase>> stores){
			
		MapWrapper<TaxonBase> taxonMap = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
		MapWrapper<ReferenceBase> nomRefMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.NOMREF_STORE);
		
		Set<TaxonBase> taxonStore = new HashSet<TaxonBase>();
		
		BerlinModelImportConfigurator bmiConfig = (BerlinModelImportConfigurator)config;
		Source source = bmiConfig.getSource();
		
		logger.info("start makeFacts ...");
		
		MapWrapper<Feature> featureMap = invokeFactCategories(bmiConfig);
		
		//for testing only
		//TaxonBase taxonBase = Taxon.NewInstance(BotanicalName.NewInstance(null), null);
		
		
		try {
			//get data from database
			String strQuery = 
					" SELECT Fact.*, PTaxon.RIdentifier as taxonId, RefDetail.Details " + 
					" FROM Fact " +
                      	" INNER JOIN PTaxon ON Fact.PTNameFk = PTaxon.PTNameFk AND Fact.PTRefFk = PTaxon.PTRefFk " +
                      	" LEFT OUTER JOIN RefDetail ON Fact.FactRefDetailFk = RefDetail.RefDetailId AND Fact.FactRefFk = RefDetail.RefFk " +
                      	" WHERE (1=1)";
			ResultSet rs = source.getResultSet(strQuery) ;

			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0){ logger.info("Facts handled: " + (i-1));}
				
				int taxonId = rs.getInt("taxonId");
				int factId = rs.getInt("factId");
				int factRefFk = rs.getInt("factRefFk");
				String details = rs.getString("Details");
//				int ptDesignationRefFk = rs.getInt("PTDesignationRefFk");
//				String ptDesignation details = rs.getInt("PTDesignationRefDetailFk");
				int categoryFk = rs.getInt("factCategoryFk");
				String fact = CdmUtils.Nz(rs.getString("Fact"));
				String notes = CdmUtils.Nz(rs.getString("notes"));
				
				TaxonBase taxonBase = taxonMap.get(taxonId);
				Feature feature = featureMap.get(categoryFk); 
				
				if (taxonBase != null){
					Taxon taxon;
					if ( taxonBase instanceof Taxon ) {
						taxon = (Taxon) taxonBase;
					}else{
						logger.warn("TaxonBase " + taxonId + " for Fact " + factId + " was not of type Taxon but: " + taxonBase.getClass().getSimpleName());
						continue;
					}
					
					TaxonDescription taxonDescription;
					Set<TaxonDescription> descriptionSet= taxon.getDescriptions();
					if (descriptionSet.size() > 0) {
						taxonDescription = descriptionSet.iterator().next(); 
					}else{
						taxonDescription = TaxonDescription.NewInstance();
						taxon.addDescription(taxonDescription);
					}
					
					//textData
					TextData textData = TextData.NewInstance();
					//TODO textData.putText(fact, bmiConfig.getFactLanguage());  //doesn't work because  bmiConfig.getFactLanguage() is not not a persistent Language Object
					//throws  in thread "main" org.springframework.dao.InvalidDataAccessApiUsageException: object references an unsaved transient instance - save the transient instance before flushing: eu.etaxonomy.cdm.model.common.Language; nested exception is org.hibernate.TransientObjectException: object references an unsaved transient instance - save the transient instance before flushing: eu.etaxonomy.cdm.model.common.Language
					
					//for diptera database
					if (categoryFk == 99 && notes.contains("<OriginalName>")){
						notes = notes.replaceAll("<OriginalName>", "");
						notes = notes.replaceAll("</OriginalName>", "");
						fact = notes + ": " +  fact ;
					}
					textData.putText(fact, Language.DEFAULT());
					textData.setType(feature);
					
					//
					ReferenceBase citation = referenceMap.get(factRefFk);
					if (citation == null){
						citation = nomRefMap.get(factRefFk);
					}
					if (citation == null && factRefFk != 0){
						logger.warn("Citation not found in referenceMap: " + CdmUtils.Nz(factRefFk));
					}

					
					textData.setCitation(citation);
					textData.setCitationMicroReference(details);
					taxonDescription.addElement(textData);
					
					if (categoryFk == FACT_DESCRIPTION){
						//;
					}else if (categoryFk == FACT_OBSERVATION){
						//;
					}else if (categoryFk == FACT_DISTRIBUTION_EM){
						//
					}else {
						//TODO
						//logger.warn("FactCategory " + categoryFk + " not yet implemented");
					}
					
					//TODO
					//References
					//etc.
					
					//TODO created, notes
					//doIdCreatedUpdatedNotes(bmiConfig, textData, rs, factId);

					
					taxonStore.add(taxon);
				}else{
					//TODO
					logger.warn("Taxon for Fact " + factId + " does not exist in store");
				}
				//put
			}
			logger.info("Facts handled: " + (i-1));
			logger.info("Taxa to save: " + taxonStore.size());
			getTaxonService().saveTaxonAll(taxonStore);	
			
			logger.info("end makeFacts ...");
			return true;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return ! config.isDoFacts();
	}

}
