/**
 * 
 */
package eu.etaxonomy.cdm.io.berlinModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.*;
import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.source.Source;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatus;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;



/**
 * @author a.mueller
 *
 */
public class BerlinModelTypesIO {
	private static final Logger logger = Logger.getLogger(BerlinModelTypesIO.class);

	private static int modCount = 10000;

	public static boolean check(BerlinModelImportConfigurator bmiConfig){
		boolean result = true;
		logger.warn("Checking for Types not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}

	public static boolean invoke(BerlinModelImportConfigurator bmiConfig, CdmApplicationController cdmApp, 
			MapWrapper<TaxonNameBase> taxonNameMap, MapWrapper<ReferenceBase> referenceMap){
		
		Set<TaxonNameBase> taxonNameStore = new HashSet<TaxonNameBase>();
		Source source = bmiConfig.getSource();
		INameService nameService = cdmApp.getNameService();
		
		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start makeTypes ...");
		
		boolean delete = bmiConfig.isDeleteAll();

		try {
			//get data from database
			String strQuery = 
					" SELECT TypeDesignation.*, TypeStatus.Status " + 
					" FROM TypeDesignation INNER JOIN" +
                      	" TypeStatus ON TypeDesignation.TypeStatusFk = TypeStatus.TypeStatusId " + 
                    " WHERE (1=1) ";
			ResultSet rs = source.getResultSet(strQuery) ;

			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0){ logger.info("Types handled: " + (i-1));}
				
				int typeDesignationId = rs.getInt("typeDesignationId");
				int nameId = rs.getInt("nameFk");
				int typeStatusFk = rs.getInt("typeStatusFk");
				int refFk = rs.getInt("refFk");
				String refDetail = rs.getString("refDetail");
				String status = rs.getString("Status");
				String typePhrase = rs.getString("typePhrase");
				
				//TODO
				//TypeCache leer
				//RejectedFlag false
				//PublishFlag xxx
				
				
				TaxonNameBase taxonNameBase = taxonNameMap.get(nameId);
				
				if (taxonNameBase != null){
					try{
						TypeDesignationStatus typeDesignationStatus = BerlinModelTransformer.typeStatusId2TypeStatus(typeStatusFk);
						ReferenceBase citation = referenceMap.get(refFk);
						
						Specimen specimen = Specimen.NewInstance();
						specimen.setTitleCache(typePhrase);
						boolean addToAllNames = true;
						String originalNameString = null;
						taxonNameBase.addSpecimenTypeDesignation(specimen, typeDesignationStatus, citation, refDetail, originalNameString, addToAllNames);
						
						taxonNameStore.add(taxonNameBase);
					}catch (UnknownCdmTypeException e) {
						logger.warn("TypeStatus '" + status + "' not yet implemented");
					}
				}else{
					//TODO
					logger.warn("TaxonName for TypeDesignation " + typeDesignationId + " does not exist in store");
				}
				//put
			}
			logger.info("Names to save: " + taxonNameStore.size());
			nameService.saveTaxonNameAll(taxonNameStore);	
			
			logger.info("end makeTypes ...");
			return true;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}

	
}
