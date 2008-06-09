package eu.etaxonomy.cdm.io.berlinModel;

import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.io.source.Source;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


public class BerlinModelTaxonNameIO {
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
	
	public static boolean checkNomStatus(BerlinModelImportConfigurator bmiConfig){
		boolean result = true;
		logger.warn("Checking for NomenclaturalStatus not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	//TODO
	public static boolean invokeStatus(BerlinModelImportConfigurator bmiConfig, CdmApplicationController cdmApp, 
			MapWrapper<TaxonNameBase> taxonNameMap,	MapWrapper<ReferenceBase> referenceMap){
		
		Set<TaxonNameBase> nameStore = new HashSet<TaxonNameBase>();
		Source source = bmiConfig.getSource();
		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start makeNameStatus ...");
		
		INameService nameService = cdmApp.getNameService();
		
		try {
			//get data from database
			String strQuery = 
					" SELECT NomStatusRel.*, NomStatus.NomStatus " + 
					" FROM NomStatusRel INNER JOIN " +
                      	" NomStatus ON NomStatusRel.NomStatusFk = NomStatus.NomStatusId " +
                    " WHERE (1=1)";
			ResultSet rs = source.getResultSet(strQuery) ;
			
			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0){ logger.info("NomStatus handled: " + (i-1));}
				
				int nomStatusRelId = rs.getInt("RIdentifier");
				int nomStatusFk = rs.getInt("NomStatusFk");
				int nameId = rs.getInt("nameFk");
				int refFk = rs.getInt("nomStatusRefFk");
				int detailFk = rs.getInt("nomStatusRefDetailFk");
				
				TaxonNameBase taxonName = taxonNameMap.get(nameId);
				
				//TODO
				ReferenceBase citation = null;
				String microcitation = null;
				//TODO doubtful
				
				if (taxonName != null ){
					if (nomStatusFk == NAME_ST_NOM_INVAL){
						//TODO references, mikroref, etc überall
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.INVALID()));
					}else if (nomStatusFk == NAME_ST_NOM_ILLEG){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ILLEGITIMATE()));
					}else if (nomStatusFk == NAME_ST_NOM_NUD){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.NUDUM()));
					}else if (nomStatusFk == NAME_ST_NOM_REJ){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.REJECTED()));
					}else if (nomStatusFk == NAME_ST_NOM_REJ_PROP){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.REJECTED_PROP()));
					}else if (nomStatusFk == NAME_ST_NOM_UTIQUE_REJ){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.UTIQUE_REJECTED()));
					}else if (nomStatusFk == NAME_ST_NOM_UTIQUE_REJ_PROP){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.UTIQUE_REJECTED_PROP()));
					}else if (nomStatusFk == NAME_ST_NOM_CONS){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.CONSERVED()));
					}else if (nomStatusFk == NAME_ST_NOM_CONS_PROP){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.CONSERVED_PROP()));
					}else if (nomStatusFk == NAME_ST_ORTH_CONS){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED()));
					}else if (nomStatusFk == NAME_ST_ORTH_CONS_PROP){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED_PROP()));
					}else if (nomStatusFk == NAME_ST_NOM_SUPERFL){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.SUPERFLUOUS()));
					}else if (nomStatusFk == NAME_ST_NOM_AMBIG){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.AMBIGUOUS()));
					}else if (nomStatusFk == NAME_ST_NOM_PROVIS){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.PROVISIONAL()));
					}else if (nomStatusFk == NAME_ST_NOM_DUB){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.DOUBTFUL()));
					}else if (nomStatusFk == NAME_ST_NOM_NOV){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.NOVUM()));
					}else if (nomStatusFk == NAME_ST_NOM_CONFUS){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.CONFUSUM()));
					}else if (nomStatusFk == NAME_ST_NOM_ALTERN){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ALTERNATIVE()));
					}else if (nomStatusFk == NAME_ST_COMB_INVAL){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.COMBINATION_INVALID()));
					}else {
						//TODO
						logger.warn("NomStatusType " + nomStatusFk + " not yet implemented");
					}
					nameStore.add(taxonName);
					//TODO
					//Reference
					//ID
					//etc.
				}else{
					logger.warn("TaxonName for NomStatus (" + nomStatusRelId + ") does not exist in store");
				}
			}
			logger.info("TaxonNames to save: " + nameStore.size());
			nameService.saveTaxonNameAll(nameStore);
			
			logger.info("end makeNameStatus ...");
			return true;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}
	
	public static boolean invoke(BerlinModelImportConfigurator bmiConfig, CdmApplicationController cdmApp, 
			MapWrapper<TaxonNameBase> taxonNameMap, MapWrapper<ReferenceBase> referenceMap, MapWrapper<Team> authorMap){
		
		Source source = bmiConfig.getSource();
		String dbAttrName;
		String cdmAttrName;
		boolean success = true ;
		
		logger.info("start makeTaxonNames ...");
		INameService nameService = cdmApp.getNameService();
		boolean delete = bmiConfig.isDeleteAll();
		
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
				
				Object createdWhen = rs.getObject("Created_When");
				Object createdWho = rs.getObject("Created_Who");
//				Object updatedWhen = rs.getObject("Updated_When");
//				Object updatedWho = rs.getObject("Updated_who");
				Object updatedWhen = "";
				Object updatedWho = "";
				Object notes = rs.getObject("notes");
				
				try {
					if (logger.isDebugEnabled()){logger.debug(rankId);}
					Rank rank = BerlinModelTransformer.rankId2Rank(rankId);
					//FIXME
					//BotanicalName name = BotanicalName.NewInstance(BerlinModelTransformer.rankId2Rank(rankId));
					BotanicalName botanicalName = BotanicalName.NewInstance(rank);
					
					if (rankId < 40){
						dbAttrName = "supraGenericName";
					}else{
						dbAttrName = "genus";
					}
					cdmAttrName = "genusOrUninomial";
					success &= ImportHelper.addStringValue(rs, botanicalName, dbAttrName, cdmAttrName);
					
					dbAttrName = "genusSubdivisionEpi";
					cdmAttrName = "infraGenericEpithet";
					success &= ImportHelper.addStringValue(rs, botanicalName, dbAttrName, cdmAttrName);
					
					dbAttrName = "speciesEpi";
					cdmAttrName = "specificEpithet";
					success &= ImportHelper.addStringValue(rs, botanicalName, dbAttrName, cdmAttrName);
					

					dbAttrName = "infraSpeciesEpi";
					cdmAttrName = "infraSpecificEpithet";
					success &= ImportHelper.addStringValue(rs, botanicalName, dbAttrName, cdmAttrName);
					
					dbAttrName = "unnamedNamePhrase";
					cdmAttrName = "appendedPhrase";
					success &= ImportHelper.addStringValue(rs, botanicalName, dbAttrName, cdmAttrName);
					
					dbAttrName = "preliminaryFlag";
					cdmAttrName = "XX" + "protectedTitleCache";
					success &= ImportHelper.addBooleanValue(rs, botanicalName, dbAttrName, cdmAttrName);

					dbAttrName = "HybridFormulaFlag";
					cdmAttrName = "isHybridFormula";
					success &= ImportHelper.addBooleanValue(rs, botanicalName, dbAttrName, cdmAttrName);

					dbAttrName = "MonomHybFlag";
					cdmAttrName = "isMonomHybrid";
					success &= ImportHelper.addBooleanValue(rs, botanicalName, dbAttrName, cdmAttrName);

					dbAttrName = "BinomHybFlag";
					cdmAttrName = "isBinomHybrid";
					success &= ImportHelper.addBooleanValue(rs, botanicalName, dbAttrName, cdmAttrName);

					dbAttrName = "TrinomHybFlag";
					cdmAttrName = "isTrinomHybrid";
					success &= ImportHelper.addBooleanValue(rs, botanicalName, dbAttrName, cdmAttrName);

					//botanicalName.s

//					dbAttrName = "notes";
//					cdmAttrName = "isTrinomHybrid";
//					ImportHelper.addStringValue(rs, botanicalName, dbAttrName, cdmAttrName);
					
					//TODO
					//Created
					//Note
					//makeAuthorTeams
					//CultivarGroupName
					//CultivarName
					//Source_Acc
					//OrthoProjection
					
					//Details
					
					dbAttrName = "details";
					cdmAttrName = "nomenclaturalMicroReference";
					success &= ImportHelper.addStringValue(rs, botanicalName, dbAttrName, cdmAttrName);

					//TODO
					//preliminaryFlag
					
					//authorTeams
					if (authorMap != null){
						botanicalName.setCombinationAuthorTeam(getAuthorTeam(authorMap, authorFk, nameId));
						botanicalName.setExCombinationAuthorTeam(getAuthorTeam(authorMap, exAuthorFk, nameId));
						botanicalName.setBasionymAuthorTeam(getAuthorTeam(authorMap, basAuthorFk, nameId));
						botanicalName.setExBasionymAuthorTeam(getAuthorTeam(authorMap, exBasAuthorFk, nameId));
					}
					
					//nomenclatural Reference
					if (referenceMap != null){
						if (nomRefFk != null){
							int nomRefFkInt = (Integer)nomRefFk;
							ReferenceBase nomenclaturalReference = referenceMap.get(nomRefFkInt);
							if (nomenclaturalReference == null){
								//TODO
								logger.warn("Nomenclatural reference (nomRefFk = " + nomRefFkInt + ") for TaxonName (nameId = " + nameId + ")"+
								" was not found in reference store. Relation was not set!!");
							}else if (! INomenclaturalReference.class.isAssignableFrom(nomenclaturalReference.getClass())){
								logger.error("Nomenclatural reference (nomRefFk = " + nomRefFkInt + ") for TaxonName (nameId = " + nameId + ")"+
								" is not assignable from INomenclaturalReference. Relation was not set!! (Class = " + nomenclaturalReference.getClass()+ ")");
							}else{
								botanicalName.setNomenclaturalReference((INomenclaturalReference)nomenclaturalReference);
							}
						}
					}
					
					//refId
					String createdAnnotationString = "Berlin Model record was created By: " + String.valueOf(createdWho) + " (" + String.valueOf(createdWhen) + ") " +
					 						" and updated By: " + String.valueOf(updatedWho) + " (" + String.valueOf(updatedWhen) + ")";
					Annotation annotation = Annotation.NewInstance(createdAnnotationString, Language.ENGLISH());
					annotation.setCommentator(bmiConfig.getCommentator());
//					try {
//						URL linkbackUrl = new URL("http:\\www.abc.de");
//						annotation.setLinkbackUrl(linkbackUrl);
//					} catch (MalformedURLException e) {
//						logger.warn("MalformedURLException");
//					}
					botanicalName.addAnnotation(annotation);
					
					if (notes != null){
						String notesString = String.valueOf(notes);
						if (notesString.length() > 254 ){
							notesString = notesString.substring(0, 250) + "...";
						}
						Annotation notesAnnotation = Annotation.NewInstance(notesString, null);
						//notes.setCommentator(bmiConfig.getCommentator());
						botanicalName.addAnnotation(notesAnnotation);
					}
					
					
					boolean flag = true;
					Marker marker = Marker.NewInstance(MarkerType.TO_BE_CHECKED() ,flag);
					botanicalName.addMarker(marker);
					
					
					//nameId
					ImportHelper.setOriginalSource(botanicalName, bmiConfig.getSourceReference(), nameId);
					
					taxonNameMap.put(nameId, botanicalName);
					
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
	
	private static Team getAuthorTeam(MapWrapper<Team> authorMap, Object teamIdObject, int nameId){
		if (teamIdObject == null){
			return null;
		}else {
			int teamId = (Integer)teamIdObject;
			Team team = authorMap.get(teamId);
			if (team == null){
				//TODO
				logger.warn("AuthorTeam (teamId = " + teamId + ") for TaxonName (nameId = " + nameId + ")"+
				" was not found in authorTeam store. Relation was not set!!");
				return null;
			}else{
				return team;
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
					" SELECT RelName.*, FromName.nameId as name1Id, ToName.nameId as name2Id " + 
					" FROM Name as FromName INNER JOIN " +
                      	" RelName ON FromName.NameId = RelName.NameFk1 INNER JOIN " +
                      	" Name AS ToName ON RelName.NameFk2 = ToName.NameId "+
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
				int relQualifierFk = rs.getInt("relNameQualifierFk");
				
				TaxonNameBase nameFrom = nameMap.get(name1Id);
				TaxonNameBase nameTo = nameMap.get(name2Id);
				
				//TODO
				ReferenceBase citation = null;
				String microcitation = null;

				if (nameFrom != null && nameTo != null){
					if (relQualifierFk == NAME_REL_IS_BASIONYM_FOR){
						//TODO references, mikroref, etc
						nameTo.setBasionym(nameFrom);
					}else if (relQualifierFk == NAME_REL_IS_LATER_HOMONYM_OF){
						String rule = null;  //TODO
						nameFrom.addRelationshipToName(nameTo, NameRelationshipType.LATER_HOMONYM(), rule) ;
						//TODO reference
					}else if (relQualifierFk == NAME_REL_IS_REPLACED_SYNONYM_FOR){
						String rule = null;  //TODO
						nameFrom.addRelationshipToName(nameTo, NameRelationshipType.REPLACED_SYNONYM(), rule) ;
						//TODO reference
					}else if (relQualifierFk == NAME_REL_IS_TYPE_OF){
						logger.warn("NameRelationShipType " + relQualifierFk + " not yet implemented");
						//TODO reference
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
					//Reference
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
