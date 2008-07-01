/**
 * 
 */
package eu.etaxonomy.cdm.io.berlinModel;

import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.FACT_DESCRIPTION;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.FACT_DISTIRBUTION_EM;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.FACT_OBSERVATION;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * @author a.mueller
 *
 */
public class BerlinModelFactsIO  extends BerlinModelIOBase {
	private static final Logger logger = Logger.getLogger(BerlinModelFactsIO.class);

	private static int modCount = 10000;

	public static boolean check(BerlinModelImportConfigurator bmiConfig){
		boolean result = true;
		logger.warn("Checking for Facts not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}

	private static Map<Integer, Feature> invokeFactCategories(BerlinModelImportConfigurator bmiConfig, CdmApplicationController cdmApp){
		
		Map<Integer, Feature> featureMap = new HashMap<Integer, Feature>();
		IDescriptionService descriptionService = cdmApp.getDescriptionService();
		ITermService termService = cdmApp.getTermService();

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
				
				if ((i++ % modCount) == 0){ logger.info("FactCategories handled: " + (i-1));}
				
				int factCategoryId = rs.getInt("factCategoryId");
				String factCategory = rs.getString("factCategory");

				Feature feature = Feature.NewInstance(factCategory, factCategory, null);
				feature.setSupportsTextData(true);
				featureMap.put(factCategoryId, feature);

				//TODO
//				MaxFactNumber	int	Checked
//				ExtensionTableName	varchar(100)	Checked
//				Description	nvarchar(1000)	Checked
//				locExtensionFormName	nvarchar(80)	Checked
//				RankRestrictionFk	int	Checked
	
			}
			Collection col = featureMap.values();
			termService.saveTermsAll(col);
			return featureMap;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return null;
		}

	}
	
	public static boolean invoke(BerlinModelImportConfigurator bmiConfig, CdmApplicationController cdmApp, 
			MapWrapper<TaxonBase> taxonMap, MapWrapper<ReferenceBase> referenceMap, MapWrapper<ReferenceBase> nomRefMap){
		
		Set<TaxonBase> taxonStore = new HashSet<TaxonBase>();
		Source source = bmiConfig.getSource();
		ITaxonService taxonService = cdmApp.getTaxonService();
		
		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start makeFacts ...");
		
		Map<Integer, Feature> featureMap = invokeFactCategories(bmiConfig, cdmApp);
		
		Feature commonNameFeature = Feature.NewInstance("CommonName", "CommonName", null);
		//for testing only
		//TaxonBase taxonBase = Taxon.NewInstance(BotanicalName.NewInstance(null), null);
		
		
		try {
			//get data from database
			String strQuery = 
					" SELECT Fact.*, PTaxon.RIdentifier as taxonId " + 
					" FROM Fact " +
                      	" INNER JOIN PTaxon ON Fact.PTNameFk = PTaxon.PTNameFk AND Fact.PTRefFk = PTaxon.PTRefFk " +
                      	" LEFT OUTER JOIN RefDetail ON Fact.FactRefDetailFk = RefDetail.RefDetailId AND Fact.FactRefFk = RefDetail.RefFk " +
                      	" WHERE (1=1)";
			ResultSet rs = source.getResultSet(strQuery) ;

			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0){ logger.info("Facts handled: " + (i-1));}
				
				int factId = rs.getInt("factId");
				int taxonId = rs.getInt("taxonId");
				int factRefFk = rs.getInt("factRefFk");
				String details = rs.getString("Details");
//				int ptDesignationRefFk = rs.getInt("PTDesignationRefFk");
//				String ptDesignation details = rs.getInt("PTDesignationRefDetailFk");
				int categoryFk = rs.getInt("factCategoryFk");
				String fact = rs.getString("Fact");
				String notes = rs.getString("notes");
				
				TaxonBase taxonBase = taxonMap.get(taxonId);
				Feature feature = featureMap.get(categoryFk); 
				
				if (taxonBase != null){
					Taxon taxon;
					if ( taxonBase instanceof Taxon ) {
						taxon = (Taxon) taxonBase;
					}else{
						logger.warn("TaxonBase for Fact " + factId + " was not of type Taxon but: " + taxonBase.getClass().getSimpleName());
						continue;
					}
					
					TaxonDescription taxonDescription = TaxonDescription.NewInstance();
					
					taxon.addDescription(taxonDescription);
					//textData
					TextData textData = TextData.NewInstance();
					//TODO textData.putText(fact, bmiConfig.getFactLanguage());  //doesn't work because  bmiConfig.getFactLanguage() is not not a persistent Language Object
					//throws  in thread "main" org.springframework.dao.InvalidDataAccessApiUsageException: object references an unsaved transient instance - save the transient instance before flushing: eu.etaxonomy.cdm.model.common.Language; nested exception is org.hibernate.TransientObjectException: object references an unsaved transient instance - save the transient instance before flushing: eu.etaxonomy.cdm.model.common.Language
					
					//for diptera database
					if (categoryFk == 99 && notes != null && notes.contains("<OriginalName>")){
						notes = notes.replaceAll("<OriginalName>", "");
						notes = notes.replaceAll("<\\OriginalName>", "");
						fact = notes + ": " +  fact ;
					}
					textData.putText(fact, Language.DEFAULT());
					textData.setType(feature);
					ReferenceBase citation = referenceMap.get(factRefFk);
					if (citation == null){
						citation = nomRefMap.get(factRefFk);
					}
					if (citation == null){
						logger.warn("Citation not found");
					}

					
					textData.setCitation(citation);
					textData.setCitationMicroReference(details);
					taxonDescription.addElement(textData);
					
					
					
//					//FIXME delete, just for testing
//					//commonNames
//					String commonNameString;
//					if (taxon.getName() != null){
//						commonNameString = "Common " + taxon.getName().getTitleCache(); 
//					}else{
//						commonNameString = "Common (null)";
//					}
//					Language language = bmiConfig.getFactLanguage();
//					language = null;
//					CommonTaxonName commonName = CommonTaxonName.NewInstance(commonNameString, language);
//					commonName.setFeature(commonNameFeature);
//					taxonDescription.addElement(commonName);
					
					if (categoryFk == FACT_DESCRIPTION){
						//;
					}else if (categoryFk == FACT_OBSERVATION){
						//;
					}else if (categoryFk == FACT_DISTIRBUTION_EM){
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
			logger.info("Taxa to save: " + taxonStore.size());
			taxonService.saveTaxonAll(taxonStore);	
			
			logger.info("end makeFacts ...");
			return true;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}

	
}
