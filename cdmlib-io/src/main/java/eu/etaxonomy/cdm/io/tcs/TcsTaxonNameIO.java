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
	
	public static boolean checkNomStatus(TcsImportConfigurator tcsConfig){
		boolean result = true;
		logger.warn("Checking for NomenclaturalStatus not yet implemented");
		//result &= checkArticlesWithoutJournal(tcsConfig);
		//result &= checkPartOfJournal(tcsConfig);
		
		return result;
	}
	
	//TODO
	public static boolean invokeStatus(TcsImportConfigurator tcsConfig, CdmApplicationController cdmApp, 
			MapWrapper<TaxonNameBase> taxonNameMap,	MapWrapper<ReferenceBase> referenceMap){
		
		Set<TaxonNameBase> nameStore = new HashSet<TaxonNameBase>();
		Element source = tcsConfig.getSourceRoot();
		String dbAttrName;
		String cdmAttrName;
		
//		logger.info("start makeNameStatus ...");
//		
//		INameService nameService = cdmApp.getNameService();
//		
//		try {
//			//get data from database
//			String strQuery = 
//					" SELECT NomStatusRel.*, NomStatus.NomStatus " + 
//					" FROM NomStatusRel INNER JOIN " +
//                      	" NomStatus ON NomStatusRel.NomStatusFk = NomStatus.NomStatusId " +
//                    " WHERE (1=1)";
//			ResultSet rs = source.getResultSet(strQuery) ;
//			
//			int i = 0;
//			//for each reference
//			while (rs.next()){
//				
//				if ((i++ % modCount) == 0){ logger.info("NomStatus handled: " + (i-1));}
//				
//				int nomStatusRelId = rs.getInt("RIdentifier");
//				int nomStatusFk = rs.getInt("NomStatusFk");
//				int nameId = rs.getInt("nameFk");
//				int refFk = rs.getInt("nomStatusRefFk");
//				int detailFk = rs.getInt("nomStatusRefDetailFk");
//				
//				TaxonNameBase taxonName = taxonNameMap.get(nameId);
//				
//				//TODO
//				ReferenceBase citation = null;
//				String microcitation = null;
//				//TODO doubtful
//				
//				if (taxonName != null ){
//					if (nomStatusFk == NAME_ST_NOM_INVAL){
//						//TODO references, mikroref, etc überall
//						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.INVALID()));
//					}else if (nomStatusFk == NAME_ST_NOM_ILLEG){
//						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ILLEGITIMATE()));
//					}else if (nomStatusFk == NAME_ST_NOM_NUD){
//						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.NUDUM()));
//					}else if (nomStatusFk == NAME_ST_NOM_REJ){
//						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.REJECTED()));
//					}else if (nomStatusFk == NAME_ST_NOM_REJ_PROP){
//						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.REJECTED_PROP()));
//					}else if (nomStatusFk == NAME_ST_NOM_UTIQUE_REJ){
//						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.UTIQUE_REJECTED()));
//					}else if (nomStatusFk == NAME_ST_NOM_UTIQUE_REJ_PROP){
//						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.UTIQUE_REJECTED_PROP()));
//					}else if (nomStatusFk == NAME_ST_NOM_CONS){
//						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.CONSERVED()));
//					}else if (nomStatusFk == NAME_ST_NOM_CONS_PROP){
//						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.CONSERVED_PROP()));
//					}else if (nomStatusFk == NAME_ST_ORTH_CONS){
//						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED()));
//					}else if (nomStatusFk == NAME_ST_ORTH_CONS_PROP){
//						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED_PROP()));
//					}else if (nomStatusFk == NAME_ST_NOM_SUPERFL){
//						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.SUPERFLUOUS()));
//					}else if (nomStatusFk == NAME_ST_NOM_AMBIG){
//						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.AMBIGUOUS()));
//					}else if (nomStatusFk == NAME_ST_NOM_PROVIS){
//						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.PROVISIONAL()));
//					}else if (nomStatusFk == NAME_ST_NOM_DUB){
//						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.DOUBTFUL()));
//					}else if (nomStatusFk == NAME_ST_NOM_NOV){
//						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.NOVUM()));
//					}else if (nomStatusFk == NAME_ST_NOM_CONFUS){
//						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.CONFUSUM()));
//					}else if (nomStatusFk == NAME_ST_NOM_ALTERN){
//						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ALTERNATIVE()));
//					}else if (nomStatusFk == NAME_ST_COMB_INVAL){
//						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.COMBINATION_INVALID()));
//					}else {
//						//TODO
//						logger.warn("NomStatusType " + nomStatusFk + " not yet implemented");
//					}
//					nameStore.add(taxonName);
//					//TODO
//					//Reference
//					//ID
//					//etc.
//				}else{
//					logger.warn("TaxonName for NomStatus (" + nomStatusRelId + ") does not exist in store");
//				}
//			}
//			logger.info("TaxonNames to save: " + nameStore.size());
//			nameService.saveTaxonNameAll(nameStore);
//			
//			logger.info("end makeNameStatus ...");
//			return true;
//		} catch (SQLException e) {
//			logger.error("SQLException:" +  e);
//			return false;
//		}
		return false;

	}
	
	public static boolean invoke(TcsImportConfigurator tcsConfig, CdmApplicationController cdmApp, 
			MapWrapper<TaxonNameBase> taxonNameMap, MapWrapper<ReferenceBase> referenceMap, MapWrapper<Team> authorMap){
		
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
//			System.out.println(elTaxonName.toString());
//			System.out.println(about.getValue());
			

			//create TaxonName element
			String nameId = elTaxonName.getAttributeValue("about", rdfNamespace);
			String strRank = XmlHelp.getChildAttributeValue(elTaxonName, "rank", taxonNameNamespace, "resource", rdfNamespace);
			//FIXME namespace
			String strNomenclaturalCode = XmlHelp.getChildAttributeValue(elTaxonName, "nomenclaturalCode", taxonConceptNamespace, "resource", rdfNamespace);
			System.out.println(strRank);
			
			String tcsElementName;
			String cdmAttrName;
			String value;
			try {
				Rank rank = TcsTransformer.rankString2Rank(strRank);
				NomenclaturalCode nomCode = TcsTransformer.nomCodeString2NomCode(strNomenclaturalCode);
				TaxonNameBase nameBase = nomCode.getNewTaxonNameInstance(rank);
				
				//Epethita
				tcsElementName = "genusPart";
				cdmAttrName = "genusOrUninomial";
				success &= ImportHelper.addXmlStringValue(elTaxonName, nameBase, tcsElementName, taxonNameNamespace, cdmAttrName);

				tcsElementName = "specificEpithet";
				cdmAttrName = "specificEpithet";
				success &= ImportHelper.addXmlStringValue(elTaxonName, nameBase, tcsElementName, taxonNameNamespace, cdmAttrName);

				tcsElementName = "specificEpithet";
				cdmAttrName = "infraSpecificEpithet";
				success &= ImportHelper.addXmlStringValue(elTaxonName, nameBase, tcsElementName, taxonNameNamespace, cdmAttrName);
				
				tcsElementName = "specificEpithet";
				cdmAttrName = "infraGenericEpithet";
				success &= ImportHelper.addXmlStringValue(elTaxonName, nameBase, tcsElementName, taxonNameNamespace, cdmAttrName);
				
				//Reference
				//TODO
				tcsElementName = "publishedIn";
				cdmAttrName = "nomenclaturalReference";
				value = (String)ImportHelper.getXmlInputValue(elTaxonName, tcsElementName, commonNamespace);
				if (value != null){
					Generic nomRef = Generic.NewInstance(); //TODO
					nomRef.setTitleCache(value);
					nameBase.setNomenclaturalReference(nomRef);
					
					//TODO
					tcsElementName = "year";
					Integer year = null;
					try {
						value = (String)ImportHelper.getXmlInputValue(elTaxonName, tcsElementName, taxonNameNamespace);
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
				cdmAttrName = "nomenclaturalMicroReference";
				success &= ImportHelper.addXmlStringValue(elTaxonName, nameBase, tcsElementName, taxonNameNamespace, cdmAttrName);
				
				//Status
				Element elAnnotation = elTaxonName.getChild("hasAnnotation", taxonNameNamespace);
				if (elAnnotation != null){
					Element elNomenclaturalNote = elAnnotation.getChild("NomenclaturalNote", taxonNameNamespace);
					if (elNomenclaturalNote != null){
						String statusValue = (String)ImportHelper.getXmlInputValue(elNomenclaturalNote, "note", taxonNameNamespace);
						String type = XmlHelp.getChildAttributeValue(elNomenclaturalNote, "type", taxonConceptNamespace, "resource", rdfNamespace);
						String tdwgType = "http://rs.tdwg.org/ontology/voc/TaxonName#PublicationStatus";
						if (tdwgType.equalsIgnoreCase(type)){
							try {
								NomenclaturalStatusType statusType = TcsTransformer.nomStatusString2NomStatus(statusValue);
								if (statusType != null){
									nameBase.addStatus(NomenclaturalStatus.NewInstance(statusType));
								}
							} catch (UnknownCdmTypeException e) {
								logger.warn("Unknown NomenclaturalStatusType: " +  statusValue);
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
				System.out.println(nameBase);
				
			} catch (UnknownCdmTypeException e) {
				//FIXME
				logger.warn("Name with id " + nameId + " has unknown rank " + strRank + " and could not be saved.");
				success = false; 
			}
			
			
		}
		return false;

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
