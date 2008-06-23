/**
 * 
 */
package eu.etaxonomy.cdm.io.berlinModel;

import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * @author a.mueller
 *
 */
public class BerlinModelTaxonIO {
	private static final Logger logger = Logger.getLogger(BerlinModelTaxonIO.class);

	private static int modCount = 30000;

	public static boolean check(BerlinModelImportConfigurator bmiConfig){
		boolean result = true;
		logger.warn("Checking for Taxa not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	public static boolean checkRelations(BerlinModelImportConfigurator bmiConfig){
		boolean result = true;
		logger.warn("Checking for TaxonRelations not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	public static boolean invoke(BerlinModelImportConfigurator bmiConfig, CdmApplicationController cdmApp, 
			MapWrapper<TaxonBase> taxonMap, MapWrapper<TaxonNameBase> taxonNameMap, MapWrapper<ReferenceBase> referenceMap){
		Source source = bmiConfig.getSource();
		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start makeTaxa ...");
		
		ITaxonService taxonService = cdmApp.getTaxonService();
		boolean delete = bmiConfig.isDeleteAll();

		try {
			//get data from database
			String strQuery = 
					" SELECT * " + 
					" FROM PTaxon " +
					" WHERE (1=1)";
			
			ResultSet rs = source.getResultSet(strQuery) ;
			
			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0){ logger.info("PTaxa handled: " + (i-1));}
				
				//create TaxonName element
				int taxonId = rs.getInt("RIdentifier");
				int statusFk = rs.getInt("statusFk");
				
				int nameFk = rs.getInt("PTNameFk");
				int refFk = rs.getInt("PTRefFk");
				String doubtful = rs.getString("DoubtfulFlag");
				
				TaxonNameBase taxonName = null;
				if (taxonNameMap != null){
					taxonName  = taxonNameMap.get(nameFk);
				}
								
				ReferenceBase reference = null;
				if (referenceMap != null){
					reference = referenceMap.get(refFk);
				}
				
				if (taxonName == null ){
					logger.warn("TaxonName belonging to taxon (RIdentifier = " + taxonId + ") could not be found in store. Taxon will not be transported");
					continue;
				}else if (reference == null ){
					logger.warn("Reference belonging to taxon could not be found in store. Taxon will not be imported");
					continue;
				}else{
					TaxonBase taxonBase;
					Synonym synonym;
					Taxon taxon;
					try {
						logger.debug(statusFk);
						if (statusFk == T_STATUS_ACCEPTED){
							taxon = Taxon.NewInstance(taxonName, reference);
							taxonBase = taxon;
						}else if (statusFk == T_STATUS_SYNONYM){
							synonym = Synonym.NewInstance(taxonName, reference);
							taxonBase = synonym;
						}else{
							logger.warn("TaxonStatus " + statusFk + " not yet implemented. Taxon (RIdentifier = " + taxonId + ") left out.");
							continue;
						}
						
						//TODO
//						dbAttrName = "Detail";
//						cdmAttrName = "Micro";
//						ImportHelper.addStringValue(rs, taxonBase, dbAttrName, cdmAttrName);
						
						if (doubtful.equals("a")){
							taxonBase.setDoubtful(false);
						}else if(doubtful.equals("d")){
							taxonBase.setDoubtful(true);
						}else if(doubtful.equals("i")){
							//TODO
							logger.warn("Doubtful = i (inactivated) not yet implemented. Doubtful set to false");
						}
						
						//nameId
						ImportHelper.setOriginalSource(taxonBase, bmiConfig.getSourceReference(), taxonId);

						
						//TODO
						//
						//Created
						//Note
						//ALL
						
						taxonMap.put(taxonId, taxonBase);
					} catch (Exception e) {
						logger.warn("An exception occurred when creating taxon with id " + taxonId + ". Taxon could not be saved.");
					}
				}
			}
			//invokeRelations(source, cdmApp, deleteAll, taxonMap, referenceMap);
			logger.info("saving taxa ...");
			taxonService.saveTaxonAll(taxonMap.objects());
			
			logger.info("end makeTaxa ...");
			
			return true;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}
	

	public static boolean invokeRelations(BerlinModelImportConfigurator bmiConfig, CdmApplicationController cdmApp, 
			MapWrapper<TaxonBase> taxonMap, MapWrapper<ReferenceBase> referenceMap){

		Set<TaxonBase> taxonStore = new HashSet<TaxonBase>();
		Source source = bmiConfig.getSource();
		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start makeTaxonRelationships ...");
		
		ITaxonService taxonService = cdmApp.getTaxonService();
		boolean delete = bmiConfig.isDeleteAll();

		try {
			//get data from database
			String strQuery = 
					" SELECT RelPTaxon.*, FromTaxon.RIdentifier as taxon1Id, ToTaxon.RIdentifier as taxon2Id " + 
					" FROM PTaxon as FromTaxon INNER JOIN " +
                      	" RelPTaxon ON FromTaxon.PTNameFk = RelPTaxon.PTNameFk1 AND FromTaxon.PTRefFk = RelPTaxon.PTRefFk1 INNER JOIN " +
                      	" PTaxon AS ToTaxon ON RelPTaxon.PTNameFk2 = ToTaxon.PTNameFk AND RelPTaxon.PTRefFk2 = ToTaxon.PTRefFk "+
                    " WHERE (1=1)";
			ResultSet rs = source.getResultSet(strQuery) ;
			
			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0){ logger.info("RelPTaxa handled: " + (i-1));}
				
				int relPTaxonId = rs.getInt("RelPTaxonId");
				int taxon1Id = rs.getInt("taxon1Id");
				int taxon2Id = rs.getInt("taxon2Id");
				int relRefFk = rs.getInt("relRefFk");
				int relQualifierFk = rs.getInt("relQualifierFk");
				
				TaxonBase taxon1 = taxonMap.get(taxon1Id);
				TaxonBase taxon2 = taxonMap.get(taxon2Id);
				
				//TODO
				ReferenceBase citation = null;
				String microcitation = null;

				if (taxon2 != null && taxon1 != null){
					if (relQualifierFk == TAX_REL_IS_INCLUDED_IN){
						((Taxon)taxon2).addTaxonomicChild((Taxon)taxon1, citation, microcitation);
					}else if (relQualifierFk == TAX_REL_IS_SYNONYM_OF){
						((Taxon)taxon2).addSynonym((Synonym)taxon1, SynonymRelationshipType.SYNONYM_OF());
					}else if (relQualifierFk == TAX_REL_IS_HOMOTYPIC_SYNONYM_OF){
						if (taxon1 instanceof Synonym){
							((Taxon)taxon2).addHomotypicSynonym((Synonym)taxon1, citation, microcitation);
						}else{
							logger.error("Taxon (ID = " + taxon1.getId()+ ", RIdentifier = " + taxon1Id + ") can't be casted to Synonym");
						}
					}else if (relQualifierFk == TAX_REL_IS_HETEROTYPIC_SYNONYM_OF){
						if (Synonym.class.isAssignableFrom(taxon1.getClass())){
							((Taxon)taxon2).addSynonym((Synonym)taxon1, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
						}else{
							logger.error("Taxon (ID = " + taxon1.getId()+ ", RIdentifier = " + taxon1Id + ") can not be casted to Synonym");
						}
					}else if (relQualifierFk == TAX_REL_IS_MISAPPLIED_NAME_OF){
						((Taxon)taxon2).addMisappliedName((Taxon)taxon1, citation, microcitation);
					}else {
						//TODO
						logger.warn("TaxonRelationShipType " + relQualifierFk + " not yet implemented");
					}
					taxonStore.add(taxon2);
					
					//TODO
					//Reference
					//ID
					//etc.
				}else{
					//TODO
					logger.warn("Taxa for RelPTaxon " + relPTaxonId + " do not exist in store");
				}
			}
			logger.info("Taxa to save: " + taxonStore.size());
			taxonService.saveTaxonAll(taxonStore);
			
			logger.info("end makeRelTaxa ...");
			return true;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}
	
	
}
