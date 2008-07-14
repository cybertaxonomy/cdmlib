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
public class BerlinModelTaxonRelationIO  extends BerlinModelIOBase  {
	private static final Logger logger = Logger.getLogger(BerlinModelTaxonRelationIO.class);

	private static int modCount = 30000;

	public static boolean check(BerlinModelImportConfigurator bmiConfig){
		boolean result = true;
		logger.warn("Checking for TaxonRelations not yet fully implemented");
		result &= checkTaxonStatus(bmiConfig);
		//result &= checkArticlesWithoutJournal(bmiConfig);
		
		return result;
	}
	
	private static boolean checkTaxonStatus(BerlinModelImportConfigurator bmiConfig){
		try {
			boolean result = true;
			Source source = bmiConfig.getSource();
			String strQueryPartOfJournal = " SELECT RelPTaxon.RelQualifierFk, RelPTaxon.relPTaxonId, PTaxon.PTNameFk, PTaxon.PTRefFk, PTaxon_1.PTNameFk AS Expr1, PTaxon.RIdentifier, PTaxon_1.RIdentifier AS Expr3, Name.FullNameCache "  +
				" FROM RelPTaxon " + 
					" INNER JOIN PTaxon ON RelPTaxon.PTNameFk1 = PTaxon.PTNameFk AND RelPTaxon.PTRefFk1 = PTaxon.PTRefFk " + 
					" INNER JOIN PTaxon AS PTaxon_1 ON RelPTaxon.PTNameFk2 = PTaxon_1.PTNameFk AND RelPTaxon.PTRefFk2 = PTaxon_1.PTRefFk  " + 
					" INNER JOIN Name ON PTaxon.PTNameFk = Name.NameId " +
				" WHERE (dbo.PTaxon.StatusFk = 1) AND ((RelPTaxon.RelQualifierFk = 7) OR (RelPTaxon.RelQualifierFk = 6) OR (RelPTaxon.RelQualifierFk = 2)) ";
			ResultSet rs = source.getResultSet(strQueryPartOfJournal);
			boolean firstRow = true;
			int i = 0;
			while (rs.next()){
				i++;
				if (firstRow){
					System.out.println("========================================================");
					logger.warn("There are taxa that have a 'is synonym of' - relationship but having taxon status 'accepted'!");
					System.out.println("========================================================");
				}
				int rIdentifier = rs.getInt("RIdentifier");
				int nameFk = rs.getInt("PTNameFk");
				int refFk = rs.getInt("PTRefFk");
				int relPTaxonId = rs.getInt("relPTaxonId");
				String taxonName = rs.getString("FullNameCache");
				
				System.out.println("RIdentifier:" + rIdentifier + "\n  name: " + nameFk + 
						"\n  taxonName: " + taxonName + "\n  refId: " + refFk + "\n  RelPTaxonId: " + relPTaxonId );
				result = firstRow = false;
			}
			if (i > 0){
				System.out.println(" ");
			}
			
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean invoke(BerlinModelImportConfigurator config, CdmApplicationController cdmApp, 
			MapWrapper<TaxonBase> taxonMap, MapWrapper<ReferenceBase> referenceMap){

		Set<TaxonBase> taxonStore = new HashSet<TaxonBase>();
		Source source = config.getSource();
		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start makeTaxonRelationships ...");
		
		ITaxonService taxonService = cdmApp.getTaxonService();

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
