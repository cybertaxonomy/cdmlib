/**
 * 
 */
package eu.etaxonomy.cdm.io.berlinModel;

import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_ARTICLE;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_BOOK;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_DATABASE;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_INFORMAL;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_JOURNAL;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_PART_OF_OTHER_TITLE;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_UNKNOWN;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_WEBSITE;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.io.source.Source;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.Language;
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
public class BerlinModelReferenceIO {
	private static final Logger logger = Logger.getLogger(BerlinModelReferenceIO.class);

	private static int modCount = 1000;

	public static boolean invoke(
			Source source, 
			CdmApplicationController cdmApp, 
			boolean deleteAll, 
			MapWrapper<ReferenceBase> referenceMap){
		
		String dbAttrName;
		String cdmAttrName;
		boolean success = true;
		MapWrapper<ReferenceBase> referenceStore= new MapWrapper<ReferenceBase>(null);
		//Map<Integer, ReferenceBase> referenceCollectionMap = new HashMap<Integer, ReferenceBase>();
		
		
		logger.info("start makeReferences ...");
		IReferenceService referenceService = cdmApp.getReferenceService();
		boolean delete = deleteAll;

//		if (delete){
//			List<TaxonNameBase> listAllReferences =  referenceService.getAllReferences(0, 1000);
//			while(listAllReferences.size() > 0 ){
//				for (TaxonNameBase name : listAllReferences ){
//					//FIXME
//					//nameService.remove(name);
//				}
//				listAllReferences =  referenceService.getAllReferences(0, 1000);
//			}			
//		}
		try {
			
			
			//get data from database
			String strQueryBase = 
					" SELECT Reference.* , InReference.RefId as InRefId, InReference.RefCategoryFk as InRefCategoryFk,  " +
						" InInReference.RefId as InInRefId, InInReference.RefCategoryFk as InInRefCategoryFk, " +
						" InReference.InRefFk AS InRefInRefFk, InInReference.InRefFk AS InInRefInRefFk " +
                    " FROM Reference AS InInReference " +
                    	" RIGHT OUTER JOIN Reference AS InReference ON InInReference.RefId = InReference.InRefFk " + 
                    	" RIGHT OUTER JOIN Reference ON InReference.RefId = dbo.Reference.InRefFk ";
			
			String strQueryNoInRef = strQueryBase + 
				" WHERE (Reference.InRefFk is NULL) ";

			
			String strQuery1InRef = strQueryBase + 
				" WHERE (Reference.InRefFk is NOT NULL) AND (InReference.InRefFk is NULL) ";

			String strQuery2InRef = strQueryBase + 
				" WHERE (Reference.InRefFk is NOT NULL) AND (InReference.InRefFk is NOT NULL) AND (InInReference.InRefFk is NULL) ";

			String strQueryTesMaxRecursion = strQueryBase + 
				" WHERE (Reference.InRefFk is NOT NULL) AND (InReference.InRefFk is NOT NULL) AND (InInReference.InRefFk is NOT NULL) ";

			ResultSet testMaxRecursionResultSet = source.getResultSet(strQueryTesMaxRecursion);
			if (testMaxRecursionResultSet.next() == true){
				logger.error("Maximum allowed InReference recursions exceeded in Berlin Model. Maximum recursion level is 2.");
				return false;
			}
			
			List<ResultSet> resultSetList = new ArrayList<ResultSet>();
			resultSetList.add(source.getResultSet(strQueryNoInRef));
			resultSetList.add(source.getResultSet(strQuery1InRef));
			resultSetList.add(source.getResultSet(strQuery2InRef));
			
			int i = 0;
			//for each reference
			Iterator<ResultSet> resultSetListIterator =  resultSetList.listIterator();
			while (resultSetListIterator.hasNext()){
				i = 0;
				ResultSet rs = resultSetListIterator.next();
				while (rs.next()){
					
					if ((i++ % modCount) == 0){ logger.info("References handled: " + (i-1));}
					
					//create TaxonName element
					int refId = rs.getInt("refId");
					int categoryFk = rs.getInt("refCategoryFk");
					Object inRefFk = rs.getObject("inRefFk");
					int inRefCategoryFk = rs.getInt("InRefCategoryFk");
					
					StrictReferenceBase referenceBase;
					try {
						logger.debug("RefCategoryFk: " + categoryFk);
						
						if (categoryFk == REF_JOURNAL){
							referenceBase = new Journal();
						}else if(categoryFk == REF_BOOK){
							referenceBase = new Book();
						}else if(categoryFk == REF_ARTICLE){
							referenceBase = new Article();
							if (inRefFk != null){
								if (inRefCategoryFk == REF_JOURNAL){
									int inRefFkInt = (Integer)inRefFk;
									if (referenceStore.containsId(inRefFkInt)){
										ReferenceBase inJournal = referenceStore.get(inRefFkInt);
										if (Journal.class.isAssignableFrom(inJournal.getClass())){
											((Article)referenceBase).setInJournal((Journal)inJournal);
										}else{
											logger.warn("InJournal is not of type journal but of type " + inJournal.getClass().getSimpleName() +
												" Inreference relation could not be set");
										}
									}else{
										logger.error("Journal for Article (refID = " + refId +") could not be found. Inconsistency error. ");
										return false;
									}
								}else{
									logger.warn("Wrong inrefCategory for Article (refID = " + refId +"). Type must be 'Journal' but was not)." +
									" InReference was not added to Article! ");
								}
							}
						}else if(categoryFk == REF_DATABASE){
							referenceBase = new Database();
						}else if(categoryFk == REF_PART_OF_OTHER_TITLE){
							if (inRefCategoryFk == REF_BOOK){
								referenceBase = new BookSection();
								if (inRefFk != null){
									int inRefFkInt = (Integer)inRefFk;
									if (referenceStore.containsId(inRefFkInt)){
										ReferenceBase inBook = referenceStore.get(inRefFkInt);
										if (Book.class.isAssignableFrom(inBook.getClass())){
											((BookSection)referenceBase).setInBook((Book)inBook);
										}else{
											logger.warn("InBook is not of type book but of type " + inBook.getClass().getSimpleName() +
													" Inreference relation could not be set");
										}
									}else{
										logger.error("Book (refId = " + inRefFkInt + " for part_of_other_title (refID = " + refId +") could not be found in Hashmap. Inconsistency error. ");
										return false;
									}
								}
							}else if (inRefCategoryFk == REF_ARTICLE){
								//TODO 
								logger.warn("Reference (refId = " + refId + ") of type 'part_of_other_title' is part of 'article'." +
										" This type is not implemented yet. Generic reference created instead") ;
								referenceBase = new Generic();
							}else{
								logger.warn("InReference type (catFk = " + inRefCategoryFk + ") of part-of-reference not recognized for refId " + refId + "." +
									" Create 'Generic' reference instead");
								referenceBase = new Generic();
							}
						}else if(categoryFk == REF_INFORMAL){
							if (logger.isDebugEnabled()){logger.debug("RefType 'Informal'");}
							referenceBase = new Generic();
						}else if(categoryFk == REF_WEBSITE){
							if (logger.isDebugEnabled()){logger.debug("RefType 'Website'");}
							referenceBase = new Generic();
						}else if(categoryFk == REF_UNKNOWN){
							if (logger.isDebugEnabled()){logger.debug("RefType 'Unknown'");}
							referenceBase = new Generic();
						}else{
							logger.warn("Unknown categoryFk (" + categoryFk + "). Create 'Generic instead'");
							referenceBase = new Generic();	
						}
						
						dbAttrName = "refCache";
						cdmAttrName = "titleCache";
						//TODO wohin kommt der refCache
						//INomenclaturalReference hat nur getNomenclaturalCitation , müsste es nicht so was wie setAbbrevTitle geben? 
						success &= ImportHelper.addStringValue(rs, referenceBase, dbAttrName, cdmAttrName);
						
						dbAttrName = "nomRefCache";
						cdmAttrName = "titleCache";
						success &= ImportHelper.addStringValue(rs, referenceBase, dbAttrName, cdmAttrName);
						
						
						//refId
						//TODO
//						Annotation annotation = new Annotation("Berlin Model id: " + String.valueOf(refId), Language.DEFAULT());
//						referenceBase.addAnnotations(annotation);
						//referenceBase.setLsid(String.valueOf(refId));
						
						//	dbAttrName = "BinomHybFlag";
						//	cdmAttrName = "isBinomHybrid";
						//	ImportHelper.addBooleanValue(rs, ref, dbAttrName, cdmAttrName);
						
						//TODO
						// all attributes
						
						if (! referenceStore.containsId(refId)){
							referenceStore.put(refId, referenceBase);
							referenceMap.put(refId, referenceBase);
						}else{
							logger.warn("Duplicate refId in Berlin Model database. Second reference was not imported !!");
						}
					} catch (Exception e) {
						logger.warn("Reference with id " + refId +  " threw Exception and could not be saved");
						e.printStackTrace();
						success = false;
						return success;
					}
					
				} // end resultSet
				//save and store in map
				referenceService.saveReferenceAll(referenceStore.objects());
			}//end resultSetList	

			logger.info("end makeReferences ...");
			return success;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	}
	
}
