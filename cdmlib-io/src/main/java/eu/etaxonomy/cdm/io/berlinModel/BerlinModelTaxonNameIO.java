package eu.etaxonomy.cdm.io.berlinModel;

import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.*;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.io.source.Source;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.common.OriginalSource;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownRankException;

public class BerlinModelTaxonNameIO {
	private static final Logger logger = Logger.getLogger(BerlinModelTaxonNameIO.class);

	private static int modCount = 5000;

	//TODO
	static boolean invokeStatus(Source source, CdmApplicationController cdmApp, 
			boolean deleteAll, MapWrapper<TaxonNameBase> taxonNameMap,
			MapWrapper<ReferenceBase> referenceMap, MapWrapper<Agent> authorMap){
		return false;
	}
	
	public static boolean invoke(Source source, CdmApplicationController cdmApp, 
			boolean deleteAll, MapWrapper<TaxonNameBase> taxonNameMap,
			MapWrapper<ReferenceBase> referenceMap, MapWrapper<Agent> authorMap){
		
		
		String dbAttrName;
		String cdmAttrName;
		boolean success = true ;
		
		logger.info("start makeTaxonNames ...");
		INameService nameService = cdmApp.getNameService();
		boolean delete = deleteAll;
		
		try {
			
			
			//get data from database
			String strQuery = 
					"SELECT Name.* , RefDetail.RefDetailId, RefDetail.RefFk, " +
                      		" RefDetail.FullRefCache, RefDetail.FullNomRefCache, RefDetail.PreliminaryFlag AS RefDetailPrelim, RefDetail.Details, " + 
                      		" RefDetail.SecondarySources, RefDetail.IdInSource " +
                    " FROM Name LEFT OUTER JOIN RefDetail ON Name.NomRefDetailFk = RefDetail.RefDetailId AND Name.NomRefDetailFk = RefDetail.RefDetailId AND " +
                    	" Name.NomRefFk = RefDetail.RefFk AND Name.NomRefFk = RefDetail.RefFk" +
                    " WHERE (1=1) AND Name.Created_When > '03.03.2004'";
			
			
			ResultSet rs = source.getResultSet(strQuery) ;
			
			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0){ logger.info("Names handled: " + (i-1));}
				
				//create TaxonName element
				int nameId = rs.getInt("nameId");
				int rankId = rs.getInt("rankFk");
				Object nomRefFk = rs.getObject("NomRefFk");
				
				try {
					if (logger.isDebugEnabled()){logger.debug(rankId);}
					Rank rank = BerlinModelTransformer.rankId2Rank(rankId);
					//FIXME
					//BotanicalName name = BotanicalName.NewInstance(BerlinModelTransformer.rankId2Rank(rankId));
					BotanicalName botanicalName = new BotanicalName(rank);
					
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
					
					if (referenceMap != null){
						if (nomRefFk != null){
							int nomRefFkInt = (Integer)nomRefFk;
							ReferenceBase nomenclaturalReference = referenceMap.get(nomRefFkInt);
							if (nomenclaturalReference == null){
								//TODO
//								logger.warn("Nomenclatural reference (nomRefFk = " + nomRefFkInt + ") for TaxonName (nameId = " + nameId + ")"+
//								" was not found in reference store. Relation was not set!!");
							}else if (! INomenclaturalReference.class.isAssignableFrom(nomenclaturalReference.getClass())){
								logger.error("Nomenclatural reference (nomRefFk = " + nomRefFkInt + ") for TaxonName (nameId = " + nameId + ")"+
								" is not assignable from INomenclaturalReference. Relation was not set!! (Class = " + nomenclaturalReference.getClass()+ ")");
							}else{
								botanicalName.setNomenclaturalReference((INomenclaturalReference)nomenclaturalReference);
							}
						}
					}
					
					
					//name ID
					//refId
					//TODO
					// Annotation annotation = new Annotation("Berlin Model nameId: " + String.valueOf(refId), Language.DEFAULT());
					// botanicalName.addAnnotations(annotation);
					
					OriginalSource originalSource = new OriginalSource();
					originalSource.setIdInSource(String.valueOf(nameId));
					//originalSource.setCitation(originalCitation);
					botanicalName.addSource(originalSource);
					
					taxonNameMap.put(nameId, botanicalName);
					
				} catch (UnknownRankException e) {
					logger.warn("Name with id " + nameId + " has unknown rankId " + rankId + " and could not be saved.");
					success = false; 
				}
				
			} //while rs.hasNext()
			nameService.saveTaxonNameAll(taxonNameMap.objects());
			
		
//			//insert related Names (Basionyms, ReplacedSyns, etc.
//			makeSpellingCorrections(nameMap);
//			makeBasionyms(nameMap);
//			makeLaterHomonyms(nameMap);
//			makeReplacedNames(nameMap);
//			
//			//insert Status infos
//			makeNameSpecificData(nameMap);
			//cdmApp.flush();
			logger.info("end makeTaxonNames ...");
			return success;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}
	
	public static boolean invokeRelations(Source source, CdmApplicationController cdmApp, boolean deleteAll, 
			MapWrapper<TaxonNameBase> nameMap, MapWrapper<ReferenceBase> referenceMap){

//		Set<TaxonBase> taxonStore = new HashSet<TaxonBase>();

		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start makeNameRelationships ...");
		
		INameService nameService = cdmApp.getNameService();
		boolean delete = deleteAll;

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
				
				TaxonNameBase name1 = nameMap.get(name1Id);
				TaxonNameBase name2 = nameMap.get(name2Id);
				
				//TODO
				ReferenceBase citation = null;
				String microcitation = null;

				if (name1 != null && name2 != null){
					if (relQualifierFk == NAME_REL_IS_BASIONYM_FOR){
						//TODO references, mikroref, etc
						name2.setBasionym(name1);
					}else if (relQualifierFk == NAME_REL_IS_LATER_HOMONYM_OF){
						NameRelationship nameRelation = new NameRelationship();
						nameRelation.setFromName(name1);
						nameRelation.setToName(name2);
						name2.addNameRelation(nameRelation);
						logger.warn("NameRelationShipType " + relQualifierFk + " not yet implemented");
						//TODO name2.add
					}else if (relQualifierFk == NAME_REL_IS_REPLACED_SYNONYM_FOR){
						logger.warn("NameRelationShipType " + relQualifierFk + " not yet implemented");
						//TODO name2.add
					}else if (relQualifierFk == NAME_REL_IS_ORTHOGRAPHIC_VARIANT_OF){
						logger.warn("NameRelationShipType " + relQualifierFk + " not yet implemented");
						//TODO name2.add
					}else {
						//TODO
						logger.warn("NameRelationShipType " + relQualifierFk + " not yet implemented");
					}
					//nameStore.add(taxon2);
					
					//TODO
					//Reference
					//ID
					//etc.
				}else{
					//TODO
					//logger.warn("Taxa for RelPTaxon " + relPTaxonId + " do not exist in store");
				}
			}
			//logger.info("Taxa to save: " + taxonStore.size());
			//nameService.saveTaxonNameAll(nameStore);
			
			logger.info("end makeRelTaxa ...");
			return true;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}
	
}
