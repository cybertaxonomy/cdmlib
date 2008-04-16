/**
 * 
 */
package eu.etaxonomy.cdm.io.berlinModel;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.*;
import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.io.source.Source;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * @author a.mueller
 *
 */
public class BerlinModelFactsIO {
	private static final Logger logger = Logger.getLogger(BerlinModelFactsIO.class);

	private static int modCount = 10000;


	public static boolean invoke(BerlinModelImportConfigurator bmiConfig, CdmApplicationController cdmApp, 
			MapWrapper<TaxonBase> taxonMap, MapWrapper<ReferenceBase> referenceMap){
		Source source = bmiConfig.getSource();
		
		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start makeFacts ...");
		
		boolean delete = bmiConfig.isDeleteAll();

		try {
			//get data from database
			String strQuery = 
					" SELECT Fact.*, PTaxon.RIdentifier as taxonId " + 
					" FROM Fact INNER JOIN " +
                      	" dbo.PTaxon ON Fact.PTNameFk = PTaxon.PTNameFk AND Fact.PTRefFk = PTaxon.PTRefFk "+
                    " WHERE (1=1)";
			ResultSet rs = source.getResultSet(strQuery) ;
			
			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0){ logger.info("Facts handled: " + (i-1));}
				
				int factId = rs.getInt("factId");
				int taxonId = rs.getInt("taxonId");
				int factRefFk = rs.getInt("factRefFk");
				int ptDesignationRefFk = rs.getInt("PTDesignationRefFk");
				int categoryFk = rs.getInt("factCategoryFk");
				
				TaxonBase taxon = taxonMap.get(taxonId);
				
				if (taxon != null){
					if (categoryFk == FACT_DESCRIPTION){
						//;
					}else if (categoryFk == FACT_OBSERVATION){
						//;
					}else if (categoryFk == FACT_DISTIRBUTION_EM){
						//
					}else {
						//TODO
						logger.warn("FactCategory " + categoryFk + " not yet implemented");
					}
					
					//TODO
					//References
					//etc.
				}else{
					//TODO
					//logger.warn("Taxa for RelPTaxon " + relPTaxonId + " do not exist in store");
				}
					//put
			}
			//taxonService.saveTaxonAll(taxonMap.objects());
			
			logger.info("end makeFacts ...");
			return true;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}

	
}
