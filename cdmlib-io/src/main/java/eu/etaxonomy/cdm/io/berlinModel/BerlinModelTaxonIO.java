/**
 * 
 */
package eu.etaxonomy.cdm.io.berlinModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.io.source.Source;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 *
 */
public class BerlinModelTaxonIO {
	private static final Logger logger = Logger.getLogger(BerlinModelTaxonIO.class);

	private static int modCount = 1000;

	public static boolean invoke(
			Source source, 
			CdmApplicationController cdmApp, 
			boolean deleteAll, 
			Map<Integer, UUID> taxonMap,
			Map<Integer, UUID> taxonNameMap,
			Map<Integer, UUID> referenceMap){
		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start makeTaxa ...");
		
		ITaxonService taxonService = cdmApp.getTaxonService();
		INameService nameService = cdmApp.getNameService();
		IReferenceService referenceService = cdmApp.getReferenceService();
		boolean delete = deleteAll;
		
//		if (delete){
//			List<TaxonBase> listAllTaxa =  taxonService.getAllTaxa(0, 1000);
//			while(listAllTaxa.size() > 0 ){
//				for (TaxonBase taxon : listAllTaxa ){
//					//FIXME
//					//nameService.remove(name);
//				}
//				listAllTaxa =  taxonService.getAllTaxa(0, 1000);
//			}			
//		}
		try {
			//get data from database
			String strQuery = 
					" SELECT *  " +
                    " FROM PTaxon " ;
			ResultSet rs = source.getResultSet(strQuery) ;
			
			
			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0){ logger.info("Names handled: " + (i-1));}
				
				//create TaxonName element
				int taxonId = rs.getInt("taxonId");
				int statusFk = rs.getInt("statusFk");
				
				int nameFk = rs.getInt("nameFk");
				int refFk = rs.getInt("refFk");
				
				TaxonNameBase taxonName;
				UUID nameUuid = taxonNameMap.get(nameFk);
				if (nameUuid == null){
					taxonName = null;
				}else{
					taxonName  = nameService.getTaxonNameByUuid(nameUuid);
				}
				
				
				ReferenceBase reference;
				UUID refUuid = referenceMap.get(refFk);
				if (refUuid == null){
					reference = null;
				}else{
					reference  = referenceService.getReferenceByUuid(refUuid);
				}
				
				TaxonBase taxonBase;
				Synonym synonym;
				Taxon taxon;
				try {
					logger.info(statusFk);
					if (statusFk == 1){
						taxon = Taxon.NewInstance(taxonName, reference);
						taxonBase = taxon;
					}else if (statusFk == 2){
						synonym = Synonym.NewInstance(taxonName, reference);
						taxonBase = synonym;
					}else{
						synonym = Synonym.NewInstance(taxonName, reference);
						taxonBase = synonym;
					}
					
					dbAttrName = "xxx";
					cdmAttrName = "yyy";
					ImportHelper.addStringValue(rs, taxonBase, dbAttrName, cdmAttrName);
					
					dbAttrName = "genusSubdivisionEpi";
					cdmAttrName = "infraGenericEpithet";
					ImportHelper.addStringValue(rs, taxonBase, dbAttrName, cdmAttrName);
					
					dbAttrName = "isDoubtful";
					cdmAttrName = "isDoubtful";
					ImportHelper.addBooleanValue(rs, taxonBase, dbAttrName, cdmAttrName);


					//TODO
					//Created
					//Note
					//ALL
					
					UUID taxonUuid = taxonService.saveTaxon(taxonBase);
					taxonMap.put(taxonId, taxonUuid);
					
				} catch (Exception e) {
					logger.warn("An exception occurred when creating taxon with id " + taxonId + ". Taxon could not be saved.");
				}
				
			}	
			
			logger.info("end makeTaxa ...");
			return true;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}
	
}
