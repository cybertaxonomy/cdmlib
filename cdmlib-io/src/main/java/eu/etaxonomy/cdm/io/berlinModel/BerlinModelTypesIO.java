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
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;



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
		
		logger.info("start makeNameFacts ...");
		
		boolean delete = bmiConfig.isDeleteAll();

		try {
			//get data from database
			String strQuery = 
					" SELECT Type.*, Name.NameID as nameId, NameFactCategory.xxx " + 
					" FROM NameType INNER JOIN " +
                      	" Name ON NameFact.PTNameFk = Name.NameId  INNER JOIN "+
                      	" NameFactCategory ON NameFactCategory.ID = NameFact.NameFactCategoryFK " + 
                    " WHERE (1=1) ";
			ResultSet rs = source.getResultSet(strQuery) ;

			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0){ logger.info("Types handled: " + (i-1));}
				
				int typeId = rs.getInt("typeId");
				int nameId = rs.getInt("nameId");
				int nameFactRefFk = rs.getInt("nameFactRefFk");
				int categoryFk = rs.getInt("nameFactCategoryFk");
				String category = CdmUtils.Nz(rs.getString("xxx"));
				String nameFact = CdmUtils.Nz(rs.getString("nameFact"));
				
				TaxonNameBase taxonNameBase = taxonNameMap.get(nameId);
				
				if (taxonNameBase != null){
					//PROTOLOGUE
					if (categoryFk == NAME_TYPE_HOLOTYPE){
						//;
					}else if (categoryFk == NAME_TYPE_LECTOTYPE){
						//;
					}else if (categoryFk == NAME_TYPE_NEOTYPE){
						//;
					}else if (categoryFk == NAME_TYPE_EPITYPE){
						//
					}else {
						//TODO
						logger.warn("TypeCategory '" + category + "' not yet implemented");
					}
					
					//TODO
					//References
					//etc.
					
					taxonNameStore.add(taxonNameBase);
				}else{
					//TODO
					logger.warn("TaxonName for Type " + typeId + " does not exist in store");
				}
				//put
			}
			logger.info("Names to save: " + taxonNameStore.size());
			nameService.saveTaxonNameAll(taxonNameStore);	
			
			logger.info("end makeFacts ...");
			return true;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}

	
}
