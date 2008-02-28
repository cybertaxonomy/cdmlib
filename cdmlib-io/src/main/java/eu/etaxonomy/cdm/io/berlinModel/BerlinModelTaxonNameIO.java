package eu.etaxonomy.cdm.io.berlinModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.io.source.Source;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownRankException;

public class BerlinModelTaxonNameIO {
	private static final Logger logger = Logger.getLogger(BerlinModelTaxonNameIO.class);

	private static int modCount = 1000;

	public static boolean invoke(
			Source source, 
			CdmApplicationController cdmApp, 
			boolean deleteAll, 
			MapWrapper<TaxonNameBase> taxonNameMap,
			MapWrapper<ReferenceBase> referenceMap){
		
		
		String dbAttrName;
		String cdmAttrName;
		boolean success = true ;
		
		logger.info("start makeTaxonNames ...");
		INameService nameService = cdmApp.getNameService();
		IReferenceService referenceService = cdmApp.getReferenceService();
		boolean delete = deleteAll;
		
		
//		if (delete){
//			List<TaxonNameBase> listAllNames =  nameService.getAllNames(0, 1000);
//			while(listAllNames.size() > 0 ){
//				for (TaxonNameBase name : listAllNames ){
//					//FIXME
//					//nameService.remove(name);
//				}
//				listAllNames =  nameService.getAllNames(0, 1000);
//			}
//		}
		
		try {
			
			
			//get data from database
			String strQuery = 
					"SELECT Name.* , RefDetail.RefDetailId, RefDetail.RefFk, " +
                      		" RefDetail.FullRefCache, RefDetail.FullNomRefCache, RefDetail.PreliminaryFlag AS RefDetailPrelim, RefDetail.Details, " + 
                      		" RefDetail.SecondarySources, RefDetail.IdInSource " +
                    " FROM Name LEFT OUTER JOIN RefDetail ON Name.NomRefDetailFk = RefDetail.RefDetailId AND Name.NomRefDetailFk = RefDetail.RefDetailId AND " +
                    " Name.NomRefFk = RefDetail.RefFk AND Name.NomRefFk = RefDetail.RefFk"; 
			ResultSet rs = source.getResultSet(strQuery) ;
			
			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0){ logger.info("Names handled: " + (i-1));}
				
				//create TaxonName element
				int nameId = rs.getInt("nameId");
				int rankId = rs.getInt("rankFk");
				Object nomRefFk = rs.getInt("NomRefFk");
				
				try {
					logger.info(rankId);
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
							if (INomenclaturalReference.class.isAssignableFrom(nomenclaturalReference.getClass())){
								botanicalName.setNomenclaturalReference((INomenclaturalReference)nomenclaturalReference);
							}else{
								logger.error("Nomenclatural reference (nomRefFk = " + nomRefFkInt + ") for TaxonName (nameId = " + nameId + ")"+
										" is not assignable from INomenclaturalReference. Relation was not set!!");
							}
						}
					}
					
					
					//name ID
					//refId
					//TODO
					// Annotation annotation = new Annotation("Berlin Model nameId: " + String.valueOf(refId), Language.DEFAULT());
					// botanicalName.addAnnotations(annotation);
					
					taxonNameMap.put(nameId, botanicalName);
					
				} catch (UnknownRankException e) {
					logger.warn("Name with id " + nameId + " has unknown rankId " + rankId + " and could not be saved.");
					success = false; 
				}
				
			} //while rs.hasNext()
			nameService.saveTaxonNameAll(taxonNameMap.objects());
			
				
//				//Code
//				strAttrName = "nomenclaturalCode";
//				strValue = "Botanical";
//				parent = elTaxonName;
//				xml.addStringAttribute(strValue, parent,  strAttrName, NS_NULL);
//				
//				//Simple
//				strDbAttr = "FullNameCache";
//				strElName = "Simple";
//				parent = elTaxonName;
//				xml.addElement(rs,strDbAttr, parent, strElName, nsTcs, OBLIGATORY);
//				
//				if (fullVersion){
//					//Rank
//					strDbAttr = "RankAbbrev";
//					strElName = "Rank";
//					parent = elTaxonName;
//					xml.addElement(rs,strDbAttr, parent, strElName, nsTcs, OBLIGATORY);
//					
//					//CanonicalName
//					parent = elTaxonName;
//					makeCanonicalName(rs, parent);
//					
//					
//					//CanonicalAuthorship
//					parent = elTaxonName;
//					makeCanonicalAuthorship(rs, parent);
//				
//				}  //fi fullVersion
//				
//				//PublishedIn
//				strDbAttr = "NomRefFk";
//				strAttrName = "ref";
//				strElName = "PublishedIn";
//				parent = elTaxonName;
//				Attribute attrPublRef = xml.addAttributeInElement(rs, strDbAttr, parent, strAttrName, strElName, nsTcs, FACULTATIVE);
//				
//				if (attrPublRef != null){
//					//does Publication exist?
//					String ref = attrPublRef.getValue();
//					if (! publicationMap.containsKey(ref)){
//						logger.error("PublishedIn ref " + ref + " for " + nameId + " does not exist.");
//					}
//				}
//				
//				
//				if (fullVersion){
//					//Year
//					String year = rs.getString("RefYear");
//					if (year == null) {
//						year = rs.getString("HigherRefYear");
//					}
//					strValue = year;
//					strElName = "Year";
//					parent = elTaxonName;
//					xml.addStringElement(strValue, parent, strElName, nsTcs, FACULTATIVE);
//					
//					//MicroReference
//					strDbAttr = "Details";
//					strElName = "MicroReference";
//					parent = elTaxonName;
//					xml.addElement(rs,strDbAttr, parent, strElName, nsTcs, FACULTATIVE);
//
//				}//fi fullversion
//			}//while
//			
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
	
}
