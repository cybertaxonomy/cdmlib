package eu.etaxonomy.cdm.io.berlinModel;

import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_BASIONYM_FOR;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_CONSERVED_TYPE_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_LATER_HOMONYM_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_ORTHOGRAPHIC_VARIANT_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_REJECTED_TYPE_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_REPLACED_SYNONYM_FOR;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_TYPE_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_COMB_INVAL;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_ALTERN;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_AMBIG;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_CONFUS;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_CONS;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_CONS_PROP;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_DUB;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_ILLEG;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_INVAL;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_NOV;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_NUD;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_PROVIS;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_REJ;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_REJ_PROP;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_SUPERFL;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_UTIQUE_REJ;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_UTIQUE_REJ_PROP;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_ORTH_CONS;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_ORTH_CONS_PROP;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


public class BerlinModelTaxonNameIO extends BerlinModelIOBase {
	private static final Logger logger = Logger.getLogger(BerlinModelTaxonNameIO.class);

	private static int modCount = 5000;

	public static boolean check(BerlinModelImportConfigurator bmiConfig){
		boolean result = true;
		logger.warn("Checking for TaxonNames not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	public static boolean checkRelations(BerlinModelImportConfigurator bmiConfig){
		boolean result = true;
		logger.warn("Checking for TaxonNameRelations not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	public static boolean invoke(BerlinModelImportConfigurator bmiConfig, CdmApplicationController cdmApp, 
			MapWrapper<TaxonNameBase> taxonNameMap, MapWrapper<ReferenceBase> nomRefMap, MapWrapper<ReferenceBase> referenceMap, MapWrapper<TeamOrPersonBase> authorMap){
		
		Source source = bmiConfig.getSource();
		String dbAttrName;
		String cdmAttrName;
		boolean success = true ;
		
		logger.info("start makeTaxonNames ...");
		INameService nameService = cdmApp.getNameService();
		
		try {
			
			
			//get data from database
			String strQuery = 
					"SELECT Name.* , RefDetail.RefDetailId, RefDetail.RefFk, " +
                      		" RefDetail.FullRefCache, RefDetail.FullNomRefCache, RefDetail.PreliminaryFlag AS RefDetailPrelim, RefDetail.Details, " + 
                      		" RefDetail.SecondarySources, RefDetail.IdInSource " +
                    " FROM Name LEFT OUTER JOIN RefDetail ON Name.NomRefDetailFk = RefDetail.RefDetailId AND Name.NomRefDetailFk = RefDetail.RefDetailId AND " +
                    	" Name.NomRefFk = RefDetail.RefFk AND Name.NomRefFk = RefDetail.RefFk" +
                    " WHERE (1=1) ";
					//strQuery += " AND Name.Created_When > '03.03.2004' ";
			
			ResultSet rs = source.getResultSet(strQuery) ;
			
			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0){ logger.info("Names handled: " + (i-1));}
				
				//create TaxonName element
				int nameId = rs.getInt("nameId");
				int rankId = rs.getInt("rankFk");
				Object authorFk = rs.getObject("AuthorTeamFk");
				Object exAuthorFk = rs.getObject("ExAuthorTeamFk");
				Object basAuthorFk = rs.getObject("BasAuthorTeamFk");
				Object exBasAuthorFk = rs.getObject("ExBasAuthorTeamFk");
				Object nomRefFk = rs.getObject("NomRefFk");
				
				try {
					if (logger.isDebugEnabled()){logger.debug(rankId);}
					Rank rank = BerlinModelTransformer.rankId2Rank(rankId);
					
					TaxonNameBase taxonNameBase;
					if (bmiConfig.getNomenclaturalCode() != null){
						taxonNameBase = bmiConfig.getNomenclaturalCode().getNewTaxonNameInstance(rank);
					}else{
						taxonNameBase = NonViralName.NewInstance(rank);
					}
					
					if (rankId < 40){
						dbAttrName = "supraGenericName";
					}else{
						dbAttrName = "genus";
					}
					cdmAttrName = "genusOrUninomial";
					success &= ImportHelper.addStringValue(rs, taxonNameBase, dbAttrName, cdmAttrName);
					
					dbAttrName = "genusSubdivisionEpi";
					cdmAttrName = "infraGenericEpithet";
					success &= ImportHelper.addStringValue(rs, taxonNameBase, dbAttrName, cdmAttrName);
					
					dbAttrName = "speciesEpi";
					cdmAttrName = "specificEpithet";
					success &= ImportHelper.addStringValue(rs, taxonNameBase, dbAttrName, cdmAttrName);
					

					dbAttrName = "infraSpeciesEpi";
					cdmAttrName = "infraSpecificEpithet";
					success &= ImportHelper.addStringValue(rs, taxonNameBase, dbAttrName, cdmAttrName);
					
					dbAttrName = "unnamedNamePhrase";
					cdmAttrName = "appendedPhrase";
					success &= ImportHelper.addStringValue(rs, taxonNameBase, dbAttrName, cdmAttrName);
					
					dbAttrName = "preliminaryFlag";
					cdmAttrName = "XX" + "protectedTitleCache";
					success &= ImportHelper.addBooleanValue(rs, taxonNameBase, dbAttrName, cdmAttrName);
					
					//Details
					dbAttrName = "details";
					cdmAttrName = "nomenclaturalMicroReference";
					success &= ImportHelper.addStringValue(rs, taxonNameBase, dbAttrName, cdmAttrName);

					
					//nomenclatural Reference
					if (referenceMap != null){
						if (nomRefFk != null){
							int nomRefFkInt = (Integer)nomRefFk;
							ReferenceBase nomenclaturalReference = nomRefMap.get(nomRefFkInt);
							if (nomenclaturalReference == null){
								nomenclaturalReference = referenceMap.get(nomRefFkInt);
							}									
							if (nomenclaturalReference == null){
								//TODO
								logger.warn("Nomenclatural reference (nomRefFk = " + nomRefFkInt + ") for TaxonName (nameId = " + nameId + ")"+
								" was not found in reference store. Relation was not set!!");
							}else if (! INomenclaturalReference.class.isAssignableFrom(nomenclaturalReference.getClass())){
								logger.error("Nomenclatural reference (nomRefFk = " + nomRefFkInt + ") for TaxonName (nameId = " + nameId + ")"+
								" is not assignable from INomenclaturalReference. Relation was not set!! (Class = " + nomenclaturalReference.getClass()+ ")");
							}else{
								taxonNameBase.setNomenclaturalReference((INomenclaturalReference)nomenclaturalReference);
							}
						}
					}

					//created, notes
					doIdCreatedUpdatedNotes(bmiConfig, taxonNameBase, rs, nameId);
					
					//Marker
					boolean flag = true;
					Marker marker = Marker.NewInstance(MarkerType.TO_BE_CHECKED() ,flag);
					taxonNameBase.addMarker(marker);

					//NonViralName
					if (taxonNameBase instanceof NonViralName){
						NonViralName nonViralName = (NonViralName)taxonNameBase;
						
						//authorTeams
						if (authorMap != null ){
							nonViralName.setCombinationAuthorTeam(getAuthorTeam(authorMap, authorFk, nameId));
							nonViralName.setExCombinationAuthorTeam(getAuthorTeam(authorMap, exAuthorFk, nameId));
							nonViralName.setBasionymAuthorTeam(getAuthorTeam(authorMap, basAuthorFk, nameId));
							nonViralName.setExBasionymAuthorTeam(getAuthorTeam(authorMap, exBasAuthorFk, nameId));
						}
						
						
					}//nonviralName

					if (taxonNameBase instanceof ZoologicalName){
						ZoologicalName zooName = (ZoologicalName)taxonNameBase;
						//publicationYear
						String authorTeamYear = rs.getString("authorTeamYear");
						try {
							Integer publicationYear  = Integer.valueOf(authorTeamYear);
							zooName.setPublicationYear(publicationYear);
						} catch (NumberFormatException e) {
							logger.warn("authorTeamYear could not be parsed for taxonName: "+ nameId);
						}
						//original publication year
						String basAuthorTeamYear = rs.getString("basAuthorTeamYear");
						try {
							Integer OriginalPublicationYear  = Integer.valueOf(basAuthorTeamYear);
							zooName.setOriginalPublicationYear(OriginalPublicationYear);
						} catch (NumberFormatException e) {
							logger.warn("basAuthorTeamYear could not be parsed for taxonName: "+ nameId);
						}
						
					}else if (taxonNameBase instanceof BotanicalName){
						BotanicalName botanicalName = (BotanicalName)taxonNameBase;

						dbAttrName = "HybridFormulaFlag";
						cdmAttrName = "isHybridFormula";
						success &= ImportHelper.addBooleanValue(rs, taxonNameBase, dbAttrName, cdmAttrName);

						dbAttrName = "MonomHybFlag";
						cdmAttrName = "isMonomHybrid";
						success &= ImportHelper.addBooleanValue(rs, taxonNameBase, dbAttrName, cdmAttrName);

						dbAttrName = "BinomHybFlag";
						cdmAttrName = "isBinomHybrid";
						success &= ImportHelper.addBooleanValue(rs, taxonNameBase, dbAttrName, cdmAttrName);

						dbAttrName = "TrinomHybFlag";
						cdmAttrName = "isTrinomHybrid";
						success &= ImportHelper.addBooleanValue(rs, taxonNameBase, dbAttrName, cdmAttrName);

						if (taxonNameBase instanceof CultivarPlantName){
							//TODO
							//CultivarGroupName
							//CultivarName
						}	
					}
					
					
					//TODO
					//Source_Acc
					//OrthoProjection

					//TODO
					//preliminaryFlag see above
					
					taxonNameMap.put(nameId, taxonNameBase);
					
				}
				catch (UnknownCdmTypeException e) {
					logger.warn("Name with id " + nameId + " has unknown rankId " + rankId + " and could not be saved.");
					success = false; 
				}
				
			} //while rs.hasNext()
			logger.info(i + " names handled");
			nameService.saveTaxonNameAll(taxonNameMap.objects());
			
//			makeNameSpecificData(nameMap);

			logger.info("end makeTaxonNames ...");
			return success;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}
	
	private static TeamOrPersonBase getAuthorTeam(MapWrapper<TeamOrPersonBase> authorMap, Object teamIdObject, int nameId){
		if (teamIdObject == null){
			return null;
		}else {
			int teamId = (Integer)teamIdObject;
			TeamOrPersonBase author = authorMap.get(teamId);
			if (author == null){
				//TODO
				logger.warn("AuthorTeam (teamId = " + teamId + ") for TaxonName (nameId = " + nameId + ")"+
				" was not found in authorTeam store. Relation was not set!!");
				return null;
			}else{
				return author;
			}
		}
	}
	
	
	public static boolean invokeRelations(BerlinModelImportConfigurator bmiConfig, CdmApplicationController cdmApp,
			MapWrapper<TaxonNameBase> nameMap, MapWrapper<ReferenceBase> referenceMap){

		Set<TaxonNameBase> nameStore = new HashSet<TaxonNameBase>();
		Source source = bmiConfig.getSource();
		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start makeNameRelationships ...");
		
		INameService nameService = cdmApp.getNameService();
		boolean delete = bmiConfig.isDeleteAll();

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
						//TODO nameTo.addNameTypeDesignation(nameFrom, citation, microcitation, originalNameString, isRejectedType, isConservedType);
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
