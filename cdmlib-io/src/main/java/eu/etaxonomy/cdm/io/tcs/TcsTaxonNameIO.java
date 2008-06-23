package eu.etaxonomy.cdm.io.tcs;

import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.*;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


public class TcsTaxonNameIO {
	private static final Logger logger = Logger.getLogger(TcsTaxonNameIO.class);

	private static int modCount = 5000;

	public static boolean check(TcsImportConfigurator tcsConfig){
		boolean result = true;
		logger.warn("BasionymRelations not yet implemented");
		logger.warn("Checking for TaxonNames not yet implemented");
		//result &= checkArticlesWithoutJournal(tcsConfig);
		//result &= checkPartOfJournal(tcsConfig);
		
		return result;
	}
	
	public static boolean checkRelations(TcsImportConfigurator tcsConfig){
		boolean result = true;
		logger.warn("Checking for TaxonNameRelations not yet implemented");
		//result &= checkArticlesWithoutJournal(tcsConfig);
		//result &= checkPartOfJournal(tcsConfig);
		
		return result;
	}
	
	
	public static boolean invoke(TcsImportConfigurator tcsConfig, CdmApplicationController cdmApp, 
			MapWrapper<TaxonNameBase> taxonNameMap, MapWrapper<ReferenceBase> referenceMap, MapWrapper<Team> authorMap){
		
		String tcsElementName;
		Namespace tcsNamespace;
		String cdmAttrName;
		String value;
		
		logger.info("start makeTaxonNames ...");
		Element root = tcsConfig.getSourceRoot();
		boolean success =true;
		INameService nameService = cdmApp.getNameService();
		
		Namespace rdfNamespace = root.getNamespace();
		String prefix = "tn";
		Namespace taxonNameNamespace = root.getNamespace(prefix);
		prefix = "tc";
		Namespace taxonConceptNamespace = root.getNamespace(prefix);
		prefix = "tcom";
		Namespace commonNamespace = root.getNamespace(prefix);
		//String strTnNamespace = "http://rs.tdwg.org/ontology/voc/TaxonName#";
		//Namespace taxonNameNamespace = Namespace.getNamespace("tn", strTnNamespace);
		
		List<Element> elTaxonNames = root.getChildren("TaxonName", taxonNameNamespace);

		
		int i = 0;
		//for each taxonName
		for (Element elTaxonName : elTaxonNames){
			
			if ((i++ % modCount) == 0){ logger.info("Names handled: " + (i-1));}
			
			Attribute about = elTaxonName.getAttribute("about", rdfNamespace);

			//create TaxonName element
			String nameAbout = elTaxonName.getAttributeValue("about", rdfNamespace);
			String strRank = XmlHelp.getChildAttributeValue(elTaxonName, "rank", taxonNameNamespace, "resource", rdfNamespace);
			//FIXME namespace
			String strNomenclaturalCode = XmlHelp.getChildAttributeValue(elTaxonName, "nomenclaturalCode", taxonConceptNamespace, "resource", rdfNamespace);
			
			try {
				Rank rank = TcsTransformer.rankString2Rank(strRank);
				NomenclaturalCode nomCode = TcsTransformer.nomCodeString2NomCode(strNomenclaturalCode);
				TaxonNameBase nameBase = nomCode.getNewTaxonNameInstance(rank);
				
				//Epethita
				tcsElementName = "genusPart";
				tcsNamespace = taxonNameNamespace;
				cdmAttrName = "genusOrUninomial";
				success &= ImportHelper.addXmlStringValue(elTaxonName, nameBase, tcsElementName, tcsNamespace, cdmAttrName);

				tcsElementName = "specificEpithet";
				tcsNamespace = taxonNameNamespace;
				cdmAttrName = "specificEpithet";
				success &= ImportHelper.addXmlStringValue(elTaxonName, nameBase, tcsElementName, tcsNamespace, cdmAttrName);

				tcsElementName = "specificEpithet";
				tcsNamespace = taxonNameNamespace;
				cdmAttrName = "infraSpecificEpithet";
				success &= ImportHelper.addXmlStringValue(elTaxonName, nameBase, tcsElementName, tcsNamespace, cdmAttrName);
				
				tcsElementName = "specificEpithet";
				tcsNamespace = taxonNameNamespace;
				cdmAttrName = "infraGenericEpithet";
				success &= ImportHelper.addXmlStringValue(elTaxonName, nameBase, tcsElementName, tcsNamespace, cdmAttrName);
				
				//Reference
				//TODO
				tcsElementName = "publishedIn";
				tcsNamespace = commonNamespace;
				value = (String)ImportHelper.getXmlInputValue(elTaxonName, tcsElementName, tcsNamespace);
				if (value != null){
					Generic nomRef = Generic.NewInstance(); //TODO
					nomRef.setTitleCache(value);
					nameBase.setNomenclaturalReference(nomRef);
					
					//TODO
					tcsElementName = "year";
					tcsNamespace = taxonNameNamespace;
					Integer year = null;
					try {
						value = (String)ImportHelper.getXmlInputValue(elTaxonName, tcsElementName, tcsNamespace);
						year = Integer.valueOf(value);
						Calendar cal = Calendar.getInstance();
						//FIXME
						cal.set(year, 1, 1);
						nomRef.setDatePublished(TimePeriod.NewInstance(cal));
					} catch (RuntimeException e) {
						logger.warn("year could not be parsed");
					}
				}
				
				
				
				//microReference
				tcsElementName = "microReference";
				tcsNamespace = commonNamespace;
				cdmAttrName = "nomenclaturalMicroReference";
				success &= ImportHelper.addXmlStringValue(elTaxonName, nameBase, tcsElementName, tcsNamespace, cdmAttrName);
				
				//Status
				tcsNamespace = taxonNameNamespace;
				Element elAnnotation = elTaxonName.getChild("hasAnnotation", tcsNamespace);
				if (elAnnotation != null){
					Element elNomenclaturalNote = elAnnotation.getChild("NomenclaturalNote", tcsNamespace);
					if (elNomenclaturalNote != null){
						String statusValue = (String)ImportHelper.getXmlInputValue(elNomenclaturalNote, "note", tcsNamespace);
						String type = XmlHelp.getChildAttributeValue(elNomenclaturalNote, "type", taxonConceptNamespace, "resource", rdfNamespace);
						String tdwgType = "http://rs.tdwg.org/ontology/voc/TaxonName#PublicationStatus";
						if (tdwgType.equalsIgnoreCase(type)){
							try {
								//NomenclaturalStatusType statusType = TcsTransformer.nomStatusString2NomStatus(statusValue);
								NomenclaturalStatusType statusType = NomenclaturalStatusType.getNomenclaturalStatusTypeByAbbreviation(statusValue);
								if (statusType != null){
									nameBase.addStatus(NomenclaturalStatus.NewInstance(statusType));
								}
							} catch (UnknownCdmTypeException e) {
								if (! statusValue.equals("valid")){
									logger.warn("Unknown NomenclaturalStatusType: " +  statusValue);
								}
							}
						}
					}
				}
				
				if (nameBase instanceof NonViralName){
					NonViralName nonViralName = (NonViralName)nameBase;
					
					//AuthorTeams
					//TODO
					tcsElementName = "basionymAuthorship";
					value = (String)ImportHelper.getXmlInputValue(elTaxonName, tcsElementName, taxonNameNamespace);
					if (value != null){
						INomenclaturalAuthor basionymAuthor = Team.NewInstance();
						basionymAuthor.setNomenclaturalTitle(value);
						nonViralName.setBasionymAuthorTeam(basionymAuthor);
					}
						
					//TODO
					tcsElementName = "combinationAuthorship";
					value = (String)ImportHelper.getXmlInputValue(elTaxonName, tcsElementName, taxonNameNamespace);
					if (value != null){
						INomenclaturalAuthor combinationAuthor = Team.NewInstance();
						combinationAuthor.setNomenclaturalTitle(value);
						nonViralName.setCombinationAuthorTeam(combinationAuthor);
					}
						
				}
				//nameId
				//TODO
				//ImportHelper.setOriginalSource(nameBase, tcsConfig.getSourceReference(), nameId);
				taxonNameMap.put(nameAbout, nameBase);
				
			} catch (UnknownCdmTypeException e) {
				//FIXME
				logger.warn("Name with id " + nameAbout + " has unknown rank " + strRank + " and could not be saved.");
				success = false; 
			}
		}
		logger.info(i + " names handled");
		nameService.saveTaxonNameAll(taxonNameMap.objects());
//		makeNameSpecificData(nameMap);
		logger.info("end makeTaxonNames ...");
		return success;

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
	
	
	public static boolean invokeRelations(TcsImportConfigurator tcsConfig, CdmApplicationController cdmApp,
			MapWrapper<TaxonNameBase> nameMap, MapWrapper<ReferenceBase> referenceMap){

		Set<TaxonNameBase> nameStore = new HashSet<TaxonNameBase>();
		Element source = tcsConfig.getSourceRoot();
		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start makeNameRelationships ...");
		
		INameService nameService = cdmApp.getNameService();
		boolean delete = tcsConfig.isDeleteAll();

//		try {
//			//get data from database
//			String strQuery = 
//					" SELECT RelName.*, FromName.nameId as name1Id, ToName.nameId as name2Id, RefDetail.Details " + 
//					" FROM Name as FromName INNER JOIN " +
//                      	" RelName ON FromName.NameId = RelName.NameFk1 INNER JOIN " +
//                      	" Name AS ToName ON RelName.NameFk2 = ToName.NameId LEFT OUTER JOIN "+
//                      	" RefDetail ON RelName.RefDetailFK = RefDetail.RefDetailId " + 
//                    " WHERE (1=1)";
//			ResultSet rs = source.getResultSet(strQuery) ;
//			
//			int i = 0;
//			//for each reference
//			while (rs.next()){
//				
//				if ((i++ % modCount) == 0){ logger.info("RelName handled: " + (i-1));}
//				
//				int relNameId = rs.getInt("RelNameId");
//				int name1Id = rs.getInt("name1Id");
//				int name2Id = rs.getInt("name2Id");
//				int relRefFk = rs.getInt("refFk");
//				String details = rs.getString("details");
//				int relQualifierFk = rs.getInt("relNameQualifierFk");
//				
//				TaxonNameBase nameFrom = nameMap.get(name1Id);
//				TaxonNameBase nameTo = nameMap.get(name2Id);
//				
//				ReferenceBase citation = referenceMap.get(relRefFk);
//				//TODO (preliminaryFlag = true testen
//				String microcitation = details;
//
//				if (nameFrom != null && nameTo != null){
//					if (relQualifierFk == NAME_REL_IS_BASIONYM_FOR){
//						//TODO references, mikroref, etc
//						nameTo.setBasionym(nameFrom);
//					}else if (relQualifierFk == NAME_REL_IS_LATER_HOMONYM_OF){
//						String rule = null;  //TODO
//						nameFrom.addRelationshipToName(nameTo, NameRelationshipType.LATER_HOMONYM(), rule) ;
//						//TODO reference
//					}else if (relQualifierFk == NAME_REL_IS_REPLACED_SYNONYM_FOR){
//						String rule = null;  //TODO
//						nameFrom.addRelationshipToName(nameTo, NameRelationshipType.REPLACED_SYNONYM(), rule) ;
//						//TODO reference
//					}else if (relQualifierFk == NAME_REL_IS_TYPE_OF || relQualifierFk == NAME_REL_IS_REJECTED_TYPE_OF ||  relQualifierFk == NAME_REL_IS_CONSERVED_TYPE_OF ){
//						//TODO
//						String originalNameString = null;
//						boolean isRejectedType = (relQualifierFk == NAME_REL_IS_REJECTED_TYPE_OF);
//						boolean isConservedType = (relQualifierFk == NAME_REL_IS_CONSERVED_TYPE_OF);
//						//TODO nameTo.addNameTypeDesignation(nameFrom, citation, microcitation, originalNameString, isRejectedType, isConservedType);
//					}else if (relQualifierFk == NAME_REL_IS_ORTHOGRAPHIC_VARIANT_OF){
//						String rule = null;  //TODO
//						nameFrom.addRelationshipToName(nameTo, NameRelationshipType.ORTHOGRAPHIC_VARIANT(), rule) ;
//						//TODO reference
//					}else {
//						//TODO
//						logger.warn("NameRelationShipType " + relQualifierFk + " not yet implemented");
//					}
//					nameStore.add(nameFrom);
//					
//					//TODO
//					//ID
//					//etc.
//				}else{
//					//TODO
//					if (nameFrom == null){
//						 logger.warn("from TaxonName for RelName (" + relNameId + ") does not exist in store");
//					}
//					if (nameTo == null){
//						logger.warn("to TaxonNames for RelName (" + relNameId + ") does not exist in store");
//					}
//				}
//			}
//			logger.info("TaxonName to save: " + nameStore.size());
//			nameService.saveTaxonNameAll(nameStore);
//			
//			logger.info("end makeRelName ...");
//			return true;
//		} catch (SQLException e) {
//			logger.error("SQLException:" +  e);
//			return false;
//		}
		return false;
	}
}
