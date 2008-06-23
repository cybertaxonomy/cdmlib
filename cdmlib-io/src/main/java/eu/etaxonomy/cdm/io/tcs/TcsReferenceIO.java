/**
 * 
 */
package eu.etaxonomy.cdm.io.tcs;

import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_ARTICLE;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_BOOK;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_DATABASE;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_INFORMAL;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_JOURNAL;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_PART_OF_OTHER_TITLE;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_UNKNOWN;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_WEBSITE;
import static eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES.ALL;
import static eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES.CONCEPT_REFERENCES;
import static eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES.NOMENCLATURAL;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.BookSection;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;

/**
 * @author a.mueller
 *
 */
public class TcsReferenceIO {
	private static final Logger logger = Logger.getLogger(TcsReferenceIO.class);

	private static int modCount = 1000;

	public static boolean check(IImportConfigurator bmiConfig){
		boolean result = true;
		result &= checkArticlesWithoutJournal(bmiConfig);
		result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
		
	private static boolean checkArticlesWithoutJournal(IImportConfigurator bmiConfig){
		try {
			boolean result = true;
//			Source source = bmiConfig.getSource();
//			String strQueryArticlesWithoutJournal = "SELECT Reference.RefId, InRef.RefId AS InRefID, Reference.RefCategoryFk, InRef.RefCategoryFk AS InRefCatFk, Reference.RefCache, Reference.NomRefCache, Reference.Title, RefCategory.RefCategoryAbbrev, InRefCategory.RefCategoryAbbrev AS InRefCat, InRef.Title AS InRefTitle " + 
//						" FROM Reference INNER JOIN Reference AS InRef ON Reference.InRefFk = InRef.RefId INNER JOIN RefCategory ON Reference.RefCategoryFk = RefCategory.RefCategoryId INNER JOIN RefCategory AS InRefCategory ON InRef.RefCategoryFk = InRefCategory.RefCategoryId " +
//						" WHERE (Reference.RefCategoryFk = 1) AND (InRef.RefCategoryFk <> 9) ";
//			ResultSet resulSetarticlesWithoutJournal = source.getResultSet(strQueryArticlesWithoutJournal);
//			boolean firstRow = true;
//			while (resulSetarticlesWithoutJournal.next()){
//				if (firstRow){
//					System.out.println("========================================================");
//					logger.warn("There are Articles with wrong inRef type!");
//					System.out.println("========================================================");
//				}
//				int refId = resulSetarticlesWithoutJournal.getInt("RefId");
//				int categoryFk = resulSetarticlesWithoutJournal.getInt("RefCategoryFk");
//				String cat = resulSetarticlesWithoutJournal.getString("RefCategoryAbbrev");
//				int inRefFk = resulSetarticlesWithoutJournal.getInt("InRefId");
//				int inRefCategoryFk = resulSetarticlesWithoutJournal.getInt("InRefCatFk");
//				String inRefCat = resulSetarticlesWithoutJournal.getString("InRefCat");
//				String refCache = resulSetarticlesWithoutJournal.getString("RefCache");
//				String nomRefCache = resulSetarticlesWithoutJournal.getString("nomRefCache");
//				String title = resulSetarticlesWithoutJournal.getString("title");
//				String inRefTitle = resulSetarticlesWithoutJournal.getString("InRefTitle");
//				
//				System.out.println("RefID:" + refId + "\n  cat: " + cat + 
//						"\n  refCache: " + refCache + "\n  nomRefCache: " + nomRefCache + "\n  title: " + title + 
//						"\n  inRefFk: " + inRefFk + "\n  inRefCategory: " + inRefCat + 
//						"\n  inRefTitle: " + inRefTitle );
//				result = firstRow = false;
//			}
//			
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private static boolean checkPartOfJournal(IImportConfigurator bmiConfig){
		try {
			boolean result = true;
//			Source source = bmiConfig.getSource();
//			String strQueryPartOfJournal = "SELECT Reference.RefId, InRef.RefId AS InRefID, Reference.RefCategoryFk, InRef.RefCategoryFk AS InRefCatFk, Reference.RefCache, Reference.NomRefCache, Reference.Title, RefCategory.RefCategoryAbbrev, InRefCategory.RefCategoryAbbrev AS InRefCat, InRef.Title AS InRefTitle " + 
//			" FROM Reference INNER JOIN Reference AS InRef ON Reference.InRefFk = InRef.RefId INNER JOIN RefCategory ON Reference.RefCategoryFk = RefCategory.RefCategoryId INNER JOIN RefCategory AS InRefCategory ON InRef.RefCategoryFk = InRefCategory.RefCategoryId " +
//						" WHERE (Reference.RefCategoryFk = 2) AND (InRef.RefCategoryFk = 9) ";
//			ResultSet rs = source.getResultSet(strQueryPartOfJournal);
//			boolean firstRow = true;
//			while (rs.next()){
//				if (firstRow){
//					System.out.println("========================================================");
//					logger.warn("There are part-of-references that have a Journal as in-reference!");
//					System.out.println("========================================================");
//				}
//				int refId = rs.getInt("RefId");
//				int categoryFk = rs.getInt("RefCategoryFk");
//				String cat = rs.getString("RefCategoryAbbrev");
//				int inRefFk = rs.getInt("InRefId");
//				int inRefCategoryFk = rs.getInt("InRefCatFk");
//				String inRefCat = rs.getString("InRefCat");
//				String refCache = rs.getString("RefCache");
//				String nomRefCache = rs.getString("nomRefCache");
//				String title = rs.getString("title");
//				String inRefTitle = rs.getString("InRefTitle");
//				
//				System.out.println("RefID:" + refId + "\n  cat: " + cat + 
//						"\n  refCache: " + refCache + "\n  nomRefCache: " + nomRefCache + "\n  title: " + title + 
//						"\n  inRefFk: " + inRefFk + "\n  inRefCategory: " + inRefCat + 
//						"\n  inRefTitle: " + inRefTitle );
//				result = firstRow = false;
//			}
//			
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	public static boolean invoke(TcsImportConfigurator tcsConfig, CdmApplicationController cdmApp,
			MapWrapper<ReferenceBase> referenceMap, MapWrapper<Team> authorMap){
		Element source = tcsConfig.getSourceRoot();
		
		String dbAttrName;
		String cdmAttrName;
		boolean success = true;
		MapWrapper<ReferenceBase> referenceStore= new MapWrapper<ReferenceBase>(null);
		//Map<Integer, ReferenceBase> referenceCollectionMap = new HashMap<Integer, ReferenceBase>();
		
		
//		logger.info("start makeReferences ...");
//		IReferenceService referenceService = cdmApp.getReferenceService();
//		try {
//			
//			
//			
//			//get data from database
//			String strQueryBase = 
//					" SELECT Reference.* , InReference.RefId as InRefId, InReference.RefCategoryFk as InRefCategoryFk,  " +
//						" InInReference.RefId as InInRefId, InInReference.RefCategoryFk as InInRefCategoryFk, " +
//						" InReference.InRefFk AS InRefInRefFk, InInReference.InRefFk AS InInRefInRefFk " +
//                    " FROM Reference AS InInReference " +
//                    	" RIGHT OUTER JOIN Reference AS InReference ON InInReference.RefId = InReference.InRefFk " + 
//                    	" RIGHT OUTER JOIN Reference ON InReference.RefId = dbo.Reference.InRefFk " + 
//					" WHERE (1=1) "; 
//			//strQueryBase += " AND Reference.refId = 7000000 " ;
//			String strQueryNoInRef = strQueryBase + 
//				" AND (Reference.InRefFk is NULL) ";
//			
//			String strQuery1InRef = strQueryBase + 
//				" AND (Reference.InRefFk is NOT NULL) AND (InReference.InRefFk is NULL) ";
//
//			String strQuery2InRef = strQueryBase + 
//				" AND (Reference.InRefFk is NOT NULL) AND (InReference.InRefFk is NOT NULL) AND (InInReference.InRefFk is NULL) ";
//
//			String strQueryTesMaxRecursion = strQueryBase + 
//				" AND (Reference.InRefFk is NOT NULL) AND (InReference.InRefFk is NOT NULL) AND (InInReference.InRefFk is NOT NULL) ";
//
//			ResultSet testMaxRecursionResultSet = source.getResultSet(strQueryTesMaxRecursion);
//			if (testMaxRecursionResultSet.next() == true){
//				logger.error("Maximum allowed InReference recursions exceeded in Berlin Model. Maximum recursion level is 2.");
//				return false;
//			}
//
//			if (bmiConfig.getDoReferences() == CONCEPT_REFERENCES){
//				strQueryNoInRef += " AND ( Reference.refId IN ( SELECT ptRefFk FROM PTaxon) ) ";
//			}
//			
//			List<ResultSet> resultSetList = new ArrayList<ResultSet>();
//			resultSetList.add(source.getResultSet(strQueryNoInRef));
//			if (bmiConfig.getDoReferences() == ALL || bmiConfig.getDoReferences() == NOMENCLATURAL){
//				resultSetList.add(source.getResultSet(strQuery1InRef));
//				resultSetList.add(source.getResultSet(strQuery2InRef));
//			}
//			
//			
//			int i = 0;
//			//for each reference
//			Iterator<ResultSet> resultSetListIterator =  resultSetList.listIterator();
//			while (resultSetListIterator.hasNext()){
//				i = 0;
//				ResultSet rs = resultSetListIterator.next();
//				while (rs.next()){
//					
//					if ((i++ % modCount) == 0){ logger.info("References handled: " + (i-1));}
//					
//					//create TaxonName element
//					int refId = rs.getInt("refId");
//					int categoryFk = rs.getInt("refCategoryFk");
//					Object inRefFk = rs.getObject("inRefFk");
//					int inRefCategoryFk = rs.getInt("InRefCategoryFk");
//					
//					StrictReferenceBase referenceBase;
//					try {
//						logger.debug("RefCategoryFk: " + categoryFk);
//						
//						if (categoryFk == REF_JOURNAL){
//							referenceBase = new Journal();
//						}else if(categoryFk == REF_BOOK){
//							referenceBase = new Book();
//						}else if(categoryFk == REF_ARTICLE){
//							referenceBase = new Article();
//							if (inRefFk != null){
//								if (inRefCategoryFk == REF_JOURNAL){
//									int inRefFkInt = (Integer)inRefFk;
//									if (referenceStore.containsId(inRefFkInt)){
//										ReferenceBase inJournal = referenceStore.get(inRefFkInt);
//										if (Journal.class.isAssignableFrom(inJournal.getClass())){
//											((Article)referenceBase).setInJournal((Journal)inJournal);
//										}else{
//											logger.warn("InJournal is not of type journal but of type " + inJournal.getClass().getSimpleName() +
//												" Inreference relation could not be set");
//										}
//									}else{
//										logger.error("Journal for Article (refID = " + refId +") could not be found. Inconsistency error. ");
//										return false;
//									}
//								}else{
//									logger.warn("Wrong inrefCategory for Article (refID = " + refId +"). Type must be 'Journal' but was not (RefCategoryFk=" + inRefCategoryFk + "))." +
//										" InReference was not added to Article! ");
//								}
//							}
//						}else if(categoryFk == REF_DATABASE){
//							referenceBase = new Database();
//						}else if(categoryFk == REF_PART_OF_OTHER_TITLE){
//							if (inRefCategoryFk == REF_BOOK){
//								referenceBase = new BookSection();
//								if (inRefFk != null){
//									int inRefFkInt = (Integer)inRefFk;
//									if (referenceStore.containsId(inRefFkInt)){
//										ReferenceBase inBook = referenceStore.get(inRefFkInt);
//										if (Book.class.isAssignableFrom(inBook.getClass())){
//											((BookSection)referenceBase).setInBook((Book)inBook);
//										}else{
//											logger.warn("InBook is not of type book but of type " + inBook.getClass().getSimpleName() +
//													" Inreference relation could not be set");
//										}
//									}else{
//										logger.error("Book (refId = " + inRefFkInt + " for part_of_other_title (refID = " + refId +") could not be found in Hashmap. Inconsistency error. ");
//										return false;
//									}
//								}
//							}else if (inRefCategoryFk == REF_ARTICLE){
//								//TODO 
//								logger.warn("Reference (refId = " + refId + ") of type 'part_of_other_title' is part of 'article'." +
//										" This type is not implemented yet. Generic reference created instead") ;
//								referenceBase = new Generic();
//							}else if (inRefCategoryFk == REF_JOURNAL){
//								//TODO 
//								logger.warn("Reference (refId = " + refId + ") of type 'part_of_other_title' has inReference of type 'journal'." +
//										" This is not allowed! Generic reference created instead") ;
//								referenceBase = new Generic();
//								referenceBase.addMarker(Marker.NewInstance(MarkerType.TO_BE_CHECKED(), true));
//							}else{
//								logger.warn("InReference type (catFk = " + inRefCategoryFk + ") of part-of-reference not recognized for refId " + refId + "." +
//									" Create 'Generic' reference instead");
//								referenceBase = new Generic();
//							}
//						}else if(categoryFk == REF_INFORMAL){
//							if (logger.isDebugEnabled()){logger.debug("RefType 'Informal'");}
//							referenceBase = new Generic();
//						}else if(categoryFk == REF_WEBSITE){
//							if (logger.isDebugEnabled()){logger.debug("RefType 'Website'");}
//							referenceBase = new Generic();
//						}else if(categoryFk == REF_UNKNOWN){
//							if (logger.isDebugEnabled()){logger.debug("RefType 'Unknown'");}
//							referenceBase = new Generic();
//						}else{
//							logger.warn("Unknown categoryFk (" + categoryFk + "). Create 'Generic instead'");
//							referenceBase = new Generic();	
//						}
//						
//						dbAttrName = "refCache";
//						cdmAttrName = "titleCache";
//						//TODO wohin kommt der refCache
//						//INomenclaturalReference hat nur getNomenclaturalCitation , müsste es nicht so was wie setAbbrevTitle geben? 
//						success &= ImportHelper.addStringValue(rs, referenceBase, dbAttrName, cdmAttrName);
//						
//						dbAttrName = "nomRefCache";
//						cdmAttrName = "titleCache";
//						success &= ImportHelper.addStringValue(rs, referenceBase, dbAttrName, cdmAttrName, ImportHelper.NO_OVERWRITE);
//						
//						//refId
//						ImportHelper.setOriginalSource(referenceBase, bmiConfig.getSourceReference(), refId);
//						
//						//	dbAttrName = "BinomHybFlag";
//						//	cdmAttrName = "isBinomHybrid";
//						//	ImportHelper.addBooleanValue(rs, ref, dbAttrName, cdmAttrName);
//						
//						//TODO
//						// all attributes
//						
//						if (! referenceStore.containsId(refId)){
//							referenceStore.put(refId, referenceBase);
//							referenceMap.put(refId, referenceBase);
//						}else{
//							logger.warn("Duplicate refId in Berlin Model database. Second reference was not imported !!");
//						}
//					} catch (Exception e) {
//						logger.warn("Reference with id " + refId +  " threw Exception and could not be saved");
//						e.printStackTrace();
//						success = false;
//						return success;
//					}
//					
//				} // end resultSet
//				//save and store in map
//				referenceService.saveReferenceAll(referenceStore.objects());
//			}//end resultSetList	
//
//			logger.info("end makeReferences ...");
//			return success;
//		} catch (SQLException e) {
//			logger.error("SQLException:" +  e);
//			return false;
//		}
		return false;
	}
	
}
