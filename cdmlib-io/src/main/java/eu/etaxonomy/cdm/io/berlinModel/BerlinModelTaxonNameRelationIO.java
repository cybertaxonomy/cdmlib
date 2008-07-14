package eu.etaxonomy.cdm.io.berlinModel;

import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_BASIONYM_FOR;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_CONSERVED_TYPE_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_LATER_HOMONYM_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_ORTHOGRAPHIC_VARIANT_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_REJECTED_TYPE_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_REPLACED_SYNONYM_FOR;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_TYPE_OF;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;


public class BerlinModelTaxonNameRelationIO extends BerlinModelIOBase {
	private static final Logger logger = Logger.getLogger(BerlinModelTaxonNameRelationIO.class);

	private static int modCount = 5000;

	public static boolean check(BerlinModelImportConfigurator config){
		boolean result = true;
		logger.warn("Checking for TaxonNameRelations not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	public static boolean invoke(BerlinModelImportConfigurator config, CdmApplicationController cdmApp,
			MapWrapper<TaxonNameBase> nameMap, MapWrapper<ReferenceBase> referenceMap){

		Set<TaxonNameBase> nameStore = new HashSet<TaxonNameBase>();
		Source source = config.getSource();
		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start makeNameRelationships ...");
		
		INameService nameService = cdmApp.getNameService();
		try {
			//get data from database
			String strQuery = 
					" SELECT RelName.*, FromName.nameId as name1Id, ToName.nameId as name2Id, RefDetail.Details " + 
					" FROM Name as FromName INNER JOIN " +
                      	" RelName ON FromName.NameId = RelName.NameFk1 INNER JOIN " +
                      	" Name AS ToName ON RelName.NameFk2 = ToName.NameId LEFT OUTER JOIN "+
                      	" RefDetail ON RelName.RefDetailFK = RefDetail.RefDetailId " + 
                    " WHERE (1=1)";
			ResultSet rs = source.getResultSet(strQuery) ;
			
			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0){ logger.info("RelName handled: " + (i-1));}
				
				int relNameId = rs.getInt("RelNameId");
				int name1Id = rs.getInt("name1Id");
				int name2Id = rs.getInt("name2Id");
				int relRefFk = rs.getInt("refFk");
				String details = rs.getString("details");
				int relQualifierFk = rs.getInt("relNameQualifierFk");
				
				TaxonNameBase nameFrom = nameMap.get(name1Id);
				TaxonNameBase nameTo = nameMap.get(name2Id);
				
				ReferenceBase citation = referenceMap.get(relRefFk);
				//TODO (preliminaryFlag = true testen
				String microcitation = details;

				if (nameFrom != null && nameTo != null){
					if (relQualifierFk == NAME_REL_IS_BASIONYM_FOR){
						//TODO references, mikroref, etc
						nameTo.addBasionym(nameFrom);
					}else if (relQualifierFk == NAME_REL_IS_LATER_HOMONYM_OF){
						String rule = null;  //TODO
						nameFrom.addRelationshipToName(nameTo, NameRelationshipType.LATER_HOMONYM(), rule) ;
						//TODO reference
					}else if (relQualifierFk == NAME_REL_IS_REPLACED_SYNONYM_FOR){
						String rule = null;  //TODO
						nameFrom.addRelationshipToName(nameTo, NameRelationshipType.REPLACED_SYNONYM(), rule) ;
						//TODO reference
					}else if (relQualifierFk == NAME_REL_IS_TYPE_OF || relQualifierFk == NAME_REL_IS_REJECTED_TYPE_OF ||  relQualifierFk == NAME_REL_IS_CONSERVED_TYPE_OF ){
						//TODO
						String originalNameString = null;
						boolean isRejectedType = (relQualifierFk == NAME_REL_IS_REJECTED_TYPE_OF);
						boolean isConservedType = (relQualifierFk == NAME_REL_IS_CONSERVED_TYPE_OF);
						nameTo.addNameTypeDesignation(nameFrom, citation, microcitation, originalNameString, isRejectedType, isConservedType);
					}else if (relQualifierFk == NAME_REL_IS_ORTHOGRAPHIC_VARIANT_OF){
						String rule = null;  //TODO
						nameFrom.addRelationshipToName(nameTo, NameRelationshipType.ORTHOGRAPHIC_VARIANT(), rule) ;
						//TODO reference
					}else {
						//TODO
						logger.warn("NameRelationShipType " + relQualifierFk + " not yet implemented");
					}
					nameStore.add(nameFrom);
					
					//TODO
					//ID
					//etc.
				}else{
					//TODO
					if (nameFrom == null){
						 logger.warn("from TaxonName for RelName (" + relNameId + ") does not exist in store");
					}
					if (nameTo == null){
						logger.warn("to TaxonNames for RelName (" + relNameId + ") does not exist in store");
					}
				}
			}
			logger.info("TaxonName to save: " + nameStore.size());
			nameService.saveTaxonNameAll(nameStore);
			
			logger.info("end makeRelName ...");
			return true;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}
	
}
