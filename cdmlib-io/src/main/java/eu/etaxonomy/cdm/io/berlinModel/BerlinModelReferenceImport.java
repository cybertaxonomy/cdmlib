/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel;

import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_ARTICLE;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_BOOK;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_CONFERENCE_PROCEEDINGS;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_DATABASE;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_INFORMAL;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_JOURNAL;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_JOURNAL_VOLUME;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_PART_OF_OTHER_TITLE;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_PRINT_SERIES;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_UNKNOWN;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.REF_WEBSITE;
import static eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES.ALL;
import static eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES.CONCEPT_REFERENCES;
import static eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES.NOMENCLATURAL;
import static eu.etaxonomy.cdm.io.common.ImportHelper.OBLIGATORY;
import static eu.etaxonomy.cdm.io.common.ImportHelper.OVERWRITE;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.CdmIoMapperBase;
import eu.etaxonomy.cdm.io.common.CdmIoMapping;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.BookSection;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.reference.PrintSeries;
import eu.etaxonomy.cdm.model.reference.Proceedings;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
import eu.etaxonomy.cdm.model.reference.WebPage;

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelReferenceImport extends BerlinModelImportBase {
	private static final Logger logger = Logger.getLogger(BerlinModelReferenceImport.class);

	private int modCount = 1000;
	
	public BerlinModelReferenceImport(){
		super();
	}
	
	
	protected static CdmIoMapperBase[] classMappers = new CdmIoMapperBase[]{
		new CdmStringMapper("edition", "edition"),
		new CdmStringMapper("volume", "volume"),
		new CdmStringMapper("publicationTown", "placePublished"),
		new CdmStringMapper("publisher", "publisher"),
		new CdmStringMapper("isbn", "isbn"),
		new CdmStringMapper("pageString", "pages"),
		new CdmStringMapper("series", "series"),
		new CdmStringMapper("issn", "issn"),
		new CdmStringMapper("url", "uri")
	};
	
	
	protected static String[] operationalAttributes = new String[]{
		"refId", "refCache", "nomRefCache", "preliminaryFlag", "inRefFk", "title", "nomTitleAbbrev",
		"refAuthorString", "nomAuthorTeamFk",
		"refCategoryFk", "thesisFlag", "informalRefCategory", "idInSource"
	};
	
	protected static String[] createdAndNotesAttributes = new String[]{
			"created_When", "updated_When", "created_Who", "updated_Who", "notes"
	};
	
	protected static String[] unclearMappers = new String[]{
			"dateString", "refYear", "nomStandard", 
			"refDepositedAt", "isPaper", "exportDate", 
			"refSourceFk"
	};
	
	
	protected boolean checkObligatoryAttributes(IImportConfigurator config){
		boolean result = true;
		
		try {
			String strQuery = " SELECT Reference.* " +
			    " FROM Reference " +
//            	" INNER JOIN Reference ON Reference.RefId = RefDetail.RefFk " +
			    " WHERE (1=0) ";
			BerlinModelImportConfigurator bmiConfig = (BerlinModelImportConfigurator)config;
			Source source = bmiConfig.getSource();
			ResultSet rs = source.getResultSet(strQuery);
			int colCount = rs.getMetaData().getColumnCount();
			Set<String> existingAttributes = new HashSet<String>();
			for (int c = 0; c < colCount ; c++){
				existingAttributes.add(rs.getMetaData().getColumnLabel(c+1).toLowerCase());
			}
			Set<String> obligatoryAttributes = getObligatoryAttributes(true);
			
			obligatoryAttributes.removeAll(existingAttributes);
			for (String attr : obligatoryAttributes){
				logger.warn("Missing attribute: " + attr);
			}
			
			//additional Attributes
			obligatoryAttributes = getObligatoryAttributes(true);
			
			existingAttributes.removeAll(obligatoryAttributes);
			for (String attr : existingAttributes){
				logger.warn("Additional attribute: " + attr);
			}
		} catch (SQLException e) {
			logger.error(e);
			e.printStackTrace();
			result = false;
		}
		return result;
	}
	
	protected Set<String> getObligatoryAttributes(boolean lowerCase){
		Set<String> result = new HashSet<String>();
		result.addAll(Arrays.asList(unclearMappers));
		result.addAll(Arrays.asList(createdAndNotesAttributes));
		result.addAll(Arrays.asList(operationalAttributes));
		CdmIoMapping mapping = new CdmIoMapping();
		for (CdmIoMapperBase mapper : classMappers){
			mapping.addMapper(mapper);
		}
		result.addAll(mapping.getSourceAttributes());
		if (lowerCase){
			Set<String> lowerCaseResult = new HashSet<String>();
			for (String str : result){
				if (str != null){lowerCaseResult.add(str.toLowerCase());}
			}
			result = lowerCaseResult;
		}
		return result;
	}
	
	
	//type to count the references nomReferences that have been created and saved
	private class RefCounter{
		RefCounter() {nomRefCount = 0; referenceCount = 0;};
		int nomRefCount;
		int referenceCount;
		public String toString(){return String.valueOf(nomRefCount) + "," +String.valueOf(referenceCount);};
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IImportConfigurator config){
		boolean result = true;
		BerlinModelImportConfigurator bmiConfig = (BerlinModelImportConfigurator)config;
		result &= checkArticlesWithoutJournal(bmiConfig);
		result &= checkPartOfJournal(bmiConfig);
		result &= checkPartOfUnresolved(bmiConfig);
		result &= checkPartOfPartOf(bmiConfig);
		result &= checkPartOfArticle(bmiConfig);
		result &= checkJournalsWithSeries(bmiConfig);
		result &= checkObligatoryAttributes(bmiConfig);
		
		if (result == false ){System.out.println("========================================================");}
		
		
		return result;
	}
		

	
	private boolean doPreliminaryRefDetails(IImportConfigurator config, Map<String, MapWrapper<? extends CdmBase>> stores){
		
		MapWrapper<TeamOrPersonBase> authorMap = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.AUTHOR_STORE);
		MapWrapper<ReferenceBase> refDetailMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REF_DETAIL_STORE);
		MapWrapper<ReferenceBase> nomRefDetailMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.NOMREF_DETAIL_STORE);
		
		BerlinModelImportConfigurator bmiConfig = (BerlinModelImportConfigurator)config;
		Source source = bmiConfig.getSource();
		boolean success = true;
		logger.info("start makeRefDetails ...");
		
		try {
			//get data from database
			String strQuery = 
					" SELECT RefDetail.*, Reference.RefYear " +
                    " FROM RefDetail " +
                    	" INNER JOIN Reference ON Reference.RefId = RefDetail.RefFk " +
                    " WHERE (1=1) AND (RefDetail.PreliminaryFlag = 1)";
			
			ResultSet rs = source.getResultSet(strQuery);
			String namespace = "RefDetail";
			
			int i = 0;
			RefCounter refCounter  = new RefCounter();
			while (rs.next()){
				
				if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("RefDetails handled: " + (i-1) );}
				int refDetailId = rs.getInt("refDetailId"); 
				String refYear = rs.getString("RefYear"); 
				
				//nomRef
				String fullNomRefCache = rs.getString("fullNomRefCache"); 
				if (! CdmUtils.Nz(fullNomRefCache).trim().equals("") ){
					Generic genericReference = Generic.NewInstance();
					genericReference.setTitleCache(fullNomRefCache);
					nomRefDetailMap.put(refDetailId, genericReference);
					//refId, created, notes
					doIdCreatedUpdatedNotes(bmiConfig, genericReference, rs, refDetailId, namespace );						
					//year
					genericReference.setDatePublished(ImportHelper.getDatePublished(refYear)); 
					refCounter.nomRefCount++;
				}	
				
				//biblioRef
				String fullRefCache = rs.getString("fullRefCache"); 
				if (! CdmUtils.Nz(fullRefCache).trim().equals("") && 
						fullRefCache.equals(fullNomRefCache)){
					Generic genericReference = Generic.NewInstance();
					genericReference.setTitleCache(fullRefCache);
					refDetailMap.put(refDetailId, genericReference);
					
					//refId, created, notes
					doIdCreatedUpdatedNotes(bmiConfig, genericReference, rs, refDetailId, namespace );						
					//year
					genericReference.setDatePublished(ImportHelper.getDatePublished(refYear)); 
					refCounter.referenceCount++;
				}

				
				//TODO
				//SecondarySources
				//IdInSource
			}
			//save and store in map
			logger.info("Save nomenclatural preliminary references (" + refCounter.nomRefCount + ")");
			Collection<ReferenceBase> col = nomRefDetailMap.objects();
			getReferenceService().saveReferenceAll(col);
			logger.info("Save bibliographical preliminary references (" + refCounter.referenceCount +")");
			getReferenceService().saveReferenceAll(refDetailMap.objects());
			
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
		return success;
	}
	
	

	@Override
	protected boolean doInvoke(IImportConfigurator config,
			Map<String, MapWrapper<? extends CdmBase>> stores){
		
		MapWrapper<TeamOrPersonBase> authorMap = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.AUTHOR_STORE);
				
		BerlinModelImportConfigurator bmiConfig = (BerlinModelImportConfigurator)config;
		Source source = bmiConfig.getSource();
		boolean success = true;
		MapWrapper<ReferenceBase> referenceStore= new MapWrapper<ReferenceBase>(null);
		MapWrapper<ReferenceBase> nomRefStore= new MapWrapper<ReferenceBase>(null);
		
		//preliminary RefDetails  //TODO -> move to own class ?
		doPreliminaryRefDetails(config, stores);
		
		logger.info("start makeReferences ...");
		
		try {
			//get data from database
			String strQueryBase = 
					" SELECT Reference.* , InReference.RefId as InRefId, InReference.RefCategoryFk as InRefCategoryFk,  " +
						" InInReference.RefId as InInRefId, InInReference.RefCategoryFk as InInRefCategoryFk, " +
						" InReference.InRefFk AS InRefInRefFk, InInReference.InRefFk AS InInRefInRefFk " +
                    " FROM Reference AS InInReference " +
                    	" RIGHT OUTER JOIN Reference AS InReference ON InInReference.RefId = InReference.InRefFk " + 
                    	" RIGHT OUTER JOIN Reference ON InReference.RefId = dbo.Reference.InRefFk " + 
					" WHERE (1=1)  "; 
				//strQueryBase += " AND Reference.refId = 1933 " ; //7000000
			String strQueryNoInRef = strQueryBase + 
				" AND (Reference.InRefFk is NULL) ";
			
			String strQuery1InRef = strQueryBase + 
				" AND (Reference.InRefFk is NOT NULL) AND (InReference.InRefFk is NULL) ";

			String strQuery2InRef = strQueryBase + 
				" AND (Reference.InRefFk is NOT NULL) AND (InReference.InRefFk is NOT NULL) AND (InInReference.InRefFk is NULL) ";

			String strQueryTesMaxRecursion = strQueryBase + 
				" AND (Reference.InRefFk is NOT NULL) AND (InReference.InRefFk is NOT NULL) AND (InInReference.InRefFk is NOT NULL) ";

			ResultSet testMaxRecursionResultSet = source.getResultSet(strQueryTesMaxRecursion);
			if (testMaxRecursionResultSet.next() == true){
				logger.error("Maximum allowed InReference recursions exceeded in Berlin Model. Maximum recursion level is 2.");
				return false;
			}

			if (bmiConfig.getDoReferences() == CONCEPT_REFERENCES){
				strQueryNoInRef += " AND ( Reference.refId IN ( SELECT ptRefFk FROM PTaxon) ) ";
			}
			
			List<ResultSet> resultSetList = new ArrayList<ResultSet>();
			resultSetList.add(source.getResultSet(strQueryNoInRef));
			if (bmiConfig.getDoReferences() == ALL || bmiConfig.getDoReferences() == NOMENCLATURAL){
				resultSetList.add(source.getResultSet(strQuery1InRef));
				resultSetList.add(source.getResultSet(strQuery2InRef));
			}
			
			String namespace = "Reference";
			
			int j = 0;
			Iterator<ResultSet> resultSetListIterator =  resultSetList.listIterator();
			//for each resultsetlist
			while (resultSetListIterator.hasNext()){
				int i = 0;
				RefCounter refCounter  = new RefCounter();
				
				ResultSet rs = resultSetListIterator.next();
				//for each resultset
				while (rs.next()){
					
					if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("References handled: " + (i-1) + " in round " + j);}

					Map<String, Object> valueMap = getValueMap(rs);
					
					Integer categoryFk = (Integer)valueMap.get("refCategoryFk".toLowerCase());
					Integer refId = (Integer)valueMap.get("refId".toLowerCase());
					
					
					StrictReferenceBase referenceBase;
					try {
						logger.debug("RefCategoryFk: " + categoryFk);
						
						if (categoryFk == REF_JOURNAL){
							referenceBase = makeJournal(valueMap);
						}else if(categoryFk == REF_BOOK){
							referenceBase = makeBook(valueMap, referenceStore, nomRefStore);
						}else if(categoryFk == REF_DATABASE){
							referenceBase = makeDatabase(valueMap);
						}else if(categoryFk == REF_INFORMAL){
							referenceBase = makeInformal(valueMap);
						}else if(categoryFk == REF_WEBSITE){
							referenceBase = makeWebSite(valueMap);
						}else if(categoryFk == REF_UNKNOWN){
							referenceBase = makeUnknown(valueMap);
						}else if(categoryFk == REF_PRINT_SERIES){
							referenceBase = makePrintSeries(valueMap);
						}else if(categoryFk == REF_CONFERENCE_PROCEEDINGS){
							referenceBase = makeProceedings(valueMap);
						}else if(categoryFk == REF_ARTICLE){
							referenceBase = makeArticle(valueMap, referenceStore, nomRefStore);
						}else if(categoryFk == REF_JOURNAL_VOLUME){
							referenceBase = makeJournalVolume(valueMap);
						}else if(categoryFk == REF_PART_OF_OTHER_TITLE){
							referenceBase = makePartOfOtherTitle(valueMap, referenceStore, nomRefStore);
						}else{
							logger.warn("Unknown categoryFk (" + categoryFk + "). Create 'Generic instead'");
							referenceBase = Generic.NewInstance();
						}
										
						
						String refYear = (String)valueMap.get("refYear".toLowerCase());
						
						//refId, created, notes
						doIdCreatedUpdatedNotes(bmiConfig, referenceBase, rs, refId, namespace );						
						//refYear
						referenceBase.setDatePublished(ImportHelper.getDatePublished(refYear)); 
						
						//
						success &= makeNomAndBiblioReference(rs, refId, referenceBase, refCounter, 
								referenceStore, nomRefStore, authorMap, stores );

					} catch (Exception e) {
						logger.warn("Reference with BM refId " + refId +  " threw Exception and could not be saved");
						e.printStackTrace();
						success = false;
						return success;
					}
				} // end resultSet
				
				//for the concept reference a fixed uuid may be needed -> change uuid
				ReferenceBase sec = referenceStore.get(bmiConfig.getSourceSecId());
				if (sec == null){
					sec = nomRefStore.get(bmiConfig.getSourceSecId());	
				}
				if (sec != null){
					sec.setUuid(bmiConfig.getSecUuid());
					logger.info("SecUuid changed to: " + bmiConfig.getSecUuid());
				}
				
				//save and store in map
				logger.info("Save nomenclatural references (" + refCounter.nomRefCount + ")");
				getReferenceService().saveReferenceAll(nomRefStore.objects());
				logger.info("Save bibliographical references (" + refCounter.referenceCount +")");
				getReferenceService().saveReferenceAll(referenceStore.objects());
				j++;
			}//end resultSetList	

			logger.info("end makeReferences ...");
			return success;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	}
	

	
	private boolean makeNomAndBiblioReference(ResultSet rs, 
				int refId, 
				StrictReferenceBase referenceBase,  
				RefCounter refCounter, 
				MapWrapper<ReferenceBase> referenceStore, 
				MapWrapper<ReferenceBase> nomRefStore, 
				MapWrapper<TeamOrPersonBase> authorMap,
				Map<String, MapWrapper<? extends CdmBase>> stores				
				) throws SQLException{
		
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
		MapWrapper<ReferenceBase> nomRefMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.NOMREF_STORE);
		
		
		String refCache = rs.getString("refCache");
		String nomRefCache = rs.getString("nomRefCache");
		String title = rs.getString("title");
		String nomTitleAbbrev = rs.getString("nomTitleAbbrev");
		boolean isPreliminary = rs.getBoolean("PreliminaryFlag");
		String refAuthorString = rs.getString("refAuthorString");
		int nomAuthorTeamFk = rs.getInt("NomAuthorTeamFk");
		TeamOrPersonBase nomAuthor = authorMap.get(nomAuthorTeamFk);
		
		boolean hasNomRef = false;
		//is Nomenclatural Reference
		if ( (! CdmUtils.Nz(nomRefCache).equals("") && isPreliminary) || (! CdmUtils.Nz(nomTitleAbbrev).equals("") && ! isPreliminary) ){
			referenceBase.setTitle(nomTitleAbbrev);
			TeamOrPersonBase author = getAuthorTeam(refAuthorString , nomAuthor, true);
			referenceBase.setAuthorTeam(author);
			//referenceBase.setNomenclaturallyRelevant(true);
			if (isPreliminary){
				referenceBase.setTitleCache(nomRefCache);
			}
			if (! nomRefStore.containsId(refId)){
				if (referenceBase == null){
					logger.warn("refBase is null");
				}
				nomRefStore.put(refId, referenceBase);
			}else{
				logger.warn("Duplicate refId in Berlin Model database. Second reference was not imported !!");
			}
			nomRefMap.put(refId, referenceBase);
			hasNomRef = true;
			refCounter.nomRefCount++;
		}
		//is bibliographical Reference
		if ((! CdmUtils.Nz(refCache).equals("") && isPreliminary) || (! CdmUtils.Nz(title).equals("") && ! isPreliminary) || hasNomRef == false){
			if (hasNomRef){
				referenceBase = (StrictReferenceBase)referenceBase.clone();
			}
			referenceBase.setTitle(title);
			TeamOrPersonBase author = getAuthorTeam(refAuthorString , nomAuthor, false);
			referenceBase.setAuthorTeam(author);
			referenceBase.setNomenclaturallyRelevant(false);
			if (isPreliminary){
				referenceBase.setTitleCache(refCache);
			}
			if (! referenceStore.containsId(refId)){
				referenceStore.put(refId, referenceBase);
			}else{
				logger.warn("Duplicate refId in Berlin Model database. Second reference was not imported !!");
			}
			referenceMap.put(refId, referenceBase);
			refCounter.referenceCount++;
		}
		return true;
		
	}

	
	
	private StrictReferenceBase makeArticle (Map<String, Object> valueMap, MapWrapper<ReferenceBase> referenceStore, MapWrapper<ReferenceBase> nomRefStore){
		Article article = Article.NewInstance();
		Object inRefFk = valueMap.get("inRefFk".toLowerCase());
		Integer inRefCategoryFk = (Integer)valueMap.get("inRefCategoryFk".toLowerCase());
		Integer refId = (Integer)valueMap.get("refId".toLowerCase());
//		
//		//FIXME pages
//		String pages = (String)valueMap.get("pages".toLowerCase());
//		String series = (String)valueMap.get("series".toLowerCase());
//		String volume = (String)valueMap.get("volume".toLowerCase());
//		String url = (String)valueMap.get("url".toLowerCase());
		
		
		if (inRefFk != null){
			if (inRefCategoryFk == REF_JOURNAL){
				int inRefFkInt = (Integer)inRefFk;
				if (nomRefStore.containsId(inRefFkInt) || referenceStore.containsId(inRefFkInt)){
					ReferenceBase inJournal = nomRefStore.get(inRefFkInt);
					if (inJournal == null){
						inJournal = referenceStore.get(inRefFkInt);
						logger.info("inJournal (" + inRefFkInt + ") found in referenceStore instead of nomRefStore.");
						nomRefStore.put(inRefFkInt, inJournal);
					}
					if (inJournal == null){
						logger.warn("inJournal for " + inRefFkInt + " is null. "+
						" InReference relation could not be set");
					}else if (Journal.class.isAssignableFrom(inJournal.getClass())){
						article.setInJournal((Journal)inJournal);
//						article.setPages(pages);
//						article.setSeries(series);
//						article.setVolume(volume);
//						article.setUri(url);
						//logger.info("InJournal success " + inRefFkInt);
					}else{
						logger.warn("InJournal is not of type journal but of type " + inJournal.getClass().getSimpleName() +
							" Inreference relation could not be set");
					}
				}else{
					logger.error("Journal (refId = " + inRefFkInt + " ) for Article (refID = " + refId +") could not be found in nomRefStore. Inconsistency error. ");
					//success = false;;
				}
			}else{
				logger.warn("Wrong inrefCategory for Article (refID = " + refId +"). Type must be 'Journal' but was not (RefCategoryFk=" + inRefCategoryFk + "))." +
					" InReference was not added to Article! ");
			}
		}
		makeStandardMapper(valueMap, article); //url, pages, series, volume
		return article;
	}
	
	private StrictReferenceBase makePartOfOtherTitle (Map<String, Object> valueMap, MapWrapper<ReferenceBase> referenceStore, MapWrapper<ReferenceBase> nomRefStore){
		StrictReferenceBase result;
		Object inRefFk = valueMap.get("inRefFk".toLowerCase());
		Integer inRefCategoryFk = (Integer)valueMap.get("inRefCategoryFk".toLowerCase());
		Integer refId = (Integer)valueMap.get("refId".toLowerCase());
		
		if (inRefCategoryFk == REF_BOOK){
			//BookSection
			BookSection bookSection = BookSection.NewInstance();
			result = bookSection;
			if (inRefFk != null){
				int inRefFkInt = (Integer)inRefFk;
				if (nomRefStore.containsId(inRefFkInt) || referenceStore.containsId(inRefFkInt)){
					ReferenceBase inBook = nomRefStore.get(inRefFkInt);
					if (inBook == null){
						inBook = referenceStore.get(inRefFkInt);
						logger.info("inBook (" + inRefFkInt + ") found in referenceStore instead of nomRefStore.");
						nomRefStore.put(inRefFkInt, inBook);
					}
					if (inBook == null){
						logger.warn("inBook for " + inRefFkInt + " is null. "+
						" InReference relation could not be set");;
					}else if (Book.class.isAssignableFrom(inBook.getClass())){
						bookSection.setInBook((Book)inBook);
						//TODO
					}else{
						logger.warn("InBook is not of type book but of type " + inBook.getClass().getSimpleName() +
								" Inreference relation could not be set");
					}
				}else{
					logger.error("Book (refId = " + inRefFkInt + ") for part_of_other_title (refID = " + refId +") could not be found in nomRefStore. Inconsistency error. ");
					//success = false;
				}
			}
		}else if (inRefCategoryFk == REF_ARTICLE){
			//Article
			//TODO 
			logger.warn("Reference (refId = " + refId + ") of type 'part_of_other_title' is part of 'article'." +
					" This type is not implemented yet. Generic reference created instead") ;
			result = Generic.NewInstance();
		}else if (inRefCategoryFk == REF_JOURNAL){
			//TODO 
			logger.warn("Reference (refId = " + refId + ") of type 'part_of_other_title' has inReference of type 'journal'." +
					" This is not allowed! Generic reference created instead") ;
			result = Generic.NewInstance();
			result.addMarker(Marker.NewInstance(MarkerType.TO_BE_CHECKED(), true));
		}else{
			logger.warn("InReference type (catFk = " + inRefCategoryFk + ") of part-of-reference not recognized for refId " + refId + "." +
				" Create 'Generic' reference instead");
			result = Generic.NewInstance();
		}
		makeStandardMapper(valueMap, result); //url, pages
		return result;
	}
	
	private StrictReferenceBase makeWebSite(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Website'");}
		WebPage webPage = WebPage.NewInstance();
		makeStandardMapper(valueMap, webPage); //placePublished, publisher
		return webPage;
	}
	
	private StrictReferenceBase makeUnknown(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Unknown'");}
		Generic generic = Generic.NewInstance();
//		generic.setSeries(series);
		makeStandardMapper(valueMap, generic); //pages, placePublished, publisher, series, volume
		return generic;
	}

	private StrictReferenceBase makeInformal(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Informal'");}
		Generic generic =  Generic.NewInstance();
//		informal.setSeries(series);
		makeStandardMapper(valueMap, generic);//editor, pages, placePublished, publisher, series, volume
		return generic;
	}
	
	private StrictReferenceBase makeDatabase(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Database'");}
		Database database =  Database.NewInstance();
		makeStandardMapper(valueMap, database); //?
		return database;
	}
	
	private StrictReferenceBase makeJournal(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Journal'");}
		Journal journal = Journal.NewInstance();
		
		Set<String> omitAttributes = new HashSet<String>();
		String series = "series";
//		omitAttributes.add(series);
		
		makeStandardMapper(valueMap, journal, omitAttributes); //issn,placePublished,publisher
//		if (valueMap.get(series) != null){
//			logger.warn("Series not yet implemented for journal!");
//		}
		return journal;
	}
	
	private StrictReferenceBase makeBook(Map<String, Object> valueMap, MapWrapper<ReferenceBase> referenceStore, MapWrapper<ReferenceBase> nomRefStore){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Book'");}
		Book book = Book.NewInstance();
		Integer refId = (Integer)valueMap.get("refId".toLowerCase());
		
		//Set bookAttributes = new String[]{"edition", "isbn", "pages","publicationTown","publisher","volume"};
		
		Set<String> omitAttributes = new HashSet<String>();
		String attrSeries = "series";
//		omitAttributes.add(attrSeries);
		
		makeStandardMapper(valueMap, book, omitAttributes);
		
		//Series (as String)
		PrintSeries printSeries = null;
		if (valueMap.get(attrSeries) != null){
			String series = (String)valueMap.get("title".toLowerCase());
			if (series == null){
				String nomTitle = (String)valueMap.get("nomTitleAbbrev".toLowerCase());
				series = nomTitle;
			}
			printSeries = PrintSeries.NewInstance(series);
			//TODO only one for ref and nomRef
			logger.warn("Implementation of printSeries is preliminary");
		}
		Object inRefFk = valueMap.get("inRefFk".toLowerCase());
		//Series (as Reference)
		if (inRefFk != null){
			int inRefFkInt = (Integer)inRefFk;
			if (nomRefStore.containsId(inRefFkInt) || referenceStore.containsId(inRefFkInt)){
				ReferenceBase inSeries = nomRefStore.get(inRefFkInt);
				if (inSeries == null){
					inSeries = referenceStore.get(inRefFkInt);
					logger.info("inSeries (" + inRefFkInt + ") found in referenceStore instead of nomRefStore.");
					nomRefStore.put(inRefFkInt, inSeries);
				}
				if (inSeries == null){
					logger.warn("inSeries for " + inRefFkInt + " is null. "+
					" InReference relation could not be set");;
				}else if (PrintSeries.class.isAssignableFrom(inSeries.getClass())){
					book.setInSeries((PrintSeries)inSeries);
					//TODO
				}else{
					logger.warn("inSeries is not of type PrintSeries but of type " + inSeries.getClass().getSimpleName() +
							" Inreference relation could not be set");
				}
			}else{
				logger.error("PrintSeries (refId = " + inRefFkInt + ") for book (refID = " + refId +") could not be found in nomRefStore. Inconsistency error. ");
				//success = false;
			}
		}
		if (book.getInSeries() != null && printSeries != null){
			logger.warn("Book has series string and inSeries reference. Can not take both. Series string neglected");
		}else{
			book.setInSeries(printSeries);
		}
		book.setEditor(null);
		return book;
		
	}
	
	private StrictReferenceBase makePrintSeries(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'PrintSeries'");}
		PrintSeries printSeries = PrintSeries.NewInstance();
		makeStandardMapper(valueMap, printSeries, null);
		return printSeries;
	}
	
	private StrictReferenceBase makeProceedings(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Proceedings'");}
		Proceedings proceedings = Proceedings.NewInstance();
		makeStandardMapper(valueMap, proceedings, null);	
		return proceedings;
	}
	
	private StrictReferenceBase makeJournalVolume(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'JournalVolume'");}
		//Proceedings proceedings = Proceedings.NewInstance();
		Generic journalVolume = Generic.NewInstance();
		makeStandardMapper(valueMap, journalVolume, null);	
		logger.warn("Journal volumes not yet implemented. Generic created instead but with errors");
		return journalVolume;
	}
	
	private boolean makeStandardMapper(Map<String, Object> valueMap, StrictReferenceBase ref){
		return makeStandardMapper(valueMap, ref, null);
	}

	
	private boolean makeStandardMapper(Map<String, Object> valueMap, StrictReferenceBase ref, Set<String> omitAttributes){
		if (omitAttributes == null){
			omitAttributes = new HashSet<String>();
		}
		boolean result = true;	
		for (CdmIoMapperBase mapper : classMappers){
			String sourceAttribute = mapper.getSourceAttribute().toLowerCase();
			Object value = valueMap.get(sourceAttribute);
			if (value != null){
				String destinationAttribute = mapper.getDestinationAttribute();
				if (! omitAttributes.contains(destinationAttribute)){
					result &= ImportHelper.addValue(value, ref, destinationAttribute, mapper.getTypeClass(), OVERWRITE, OBLIGATORY);
				}
			}
		}
		return true;
	}
	
	private static TeamOrPersonBase getAuthorTeam(String authorString, TeamOrPersonBase nomAuthor, boolean preferNomeclaturalAuthor){
		TeamOrPersonBase result;
		if (preferNomeclaturalAuthor){
			if (nomAuthor != null){
				result = nomAuthor;
			}else{
				if (CdmUtils.Nz(authorString).equals("")){
					result = null;
				}else{
					TeamOrPersonBase team = Team.NewInstance();
					//TODO which one to use??
					team.setNomenclaturalTitle(authorString);
					team.setTitleCache(authorString);
					result = team;
				}
			}
		}else{ //prefer bibliographic
			if (! CdmUtils.Nz(authorString).equals("")){
				TeamOrPersonBase team = Team.NewInstance();
				//TODO which one to use??
				team.setNomenclaturalTitle(authorString);
				team.setTitleCache(authorString);
				result = team;
			}else{
				result = nomAuthor;
			}
		}
		return result;
	}


	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return (config.getDoReferences() == IImportConfigurator.DO_REFERENCES.NONE);
	}

	
	
//******************************** CHECK *************************************************
	
	private static boolean checkArticlesWithoutJournal(BerlinModelImportConfigurator bmiConfig){
		try {
			boolean result = true;
			Source source = bmiConfig.getSource();
			String strQueryArticlesWithoutJournal = "SELECT Reference.RefId, InRef.RefId AS InRefID, Reference.RefCategoryFk, InRef.RefCategoryFk AS InRefCatFk, Reference.RefCache, Reference.NomRefCache, Reference.Title, RefCategory.RefCategoryAbbrev, InRefCategory.RefCategoryAbbrev AS InRefCat, InRef.Title AS InRefTitle " + 
						" FROM Reference INNER JOIN Reference AS InRef ON Reference.InRefFk = InRef.RefId INNER JOIN RefCategory ON Reference.RefCategoryFk = RefCategory.RefCategoryId INNER JOIN RefCategory AS InRefCategory ON InRef.RefCategoryFk = InRefCategory.RefCategoryId " +
						" WHERE (Reference.RefCategoryFk = 1) AND (InRef.RefCategoryFk <> 9) ";
			ResultSet resulSetarticlesWithoutJournal = source.getResultSet(strQueryArticlesWithoutJournal);
			boolean firstRow = true;
			while (resulSetarticlesWithoutJournal.next()){
				if (firstRow){
					System.out.println("========================================================");
					logger.warn("There are Articles with wrong inRef type!");
					System.out.println("========================================================");
				}
				int refId = resulSetarticlesWithoutJournal.getInt("RefId");
				int categoryFk = resulSetarticlesWithoutJournal.getInt("RefCategoryFk");
				String cat = resulSetarticlesWithoutJournal.getString("RefCategoryAbbrev");
				int inRefFk = resulSetarticlesWithoutJournal.getInt("InRefId");
				int inRefCategoryFk = resulSetarticlesWithoutJournal.getInt("InRefCatFk");
				String inRefCat = resulSetarticlesWithoutJournal.getString("InRefCat");
				String refCache = resulSetarticlesWithoutJournal.getString("RefCache");
				String nomRefCache = resulSetarticlesWithoutJournal.getString("nomRefCache");
				String title = resulSetarticlesWithoutJournal.getString("title");
				String inRefTitle = resulSetarticlesWithoutJournal.getString("InRefTitle");
				
				System.out.println("RefID:" + refId + "\n  cat: " + cat + 
						"\n  refCache: " + refCache + "\n  nomRefCache: " + nomRefCache + "\n  title: " + title + 
						"\n  inRefFk: " + inRefFk + "\n  inRefCategory: " + inRefCat + 
						"\n  inRefTitle: " + inRefTitle );
				result = firstRow = false;
			}
			
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private static boolean checkPartOfJournal(BerlinModelImportConfigurator bmiConfig){
		try {
			boolean result = true;
			Source source = bmiConfig.getSource();
			String strQueryPartOfJournal = "SELECT Reference.RefId, InRef.RefId AS InRefID, Reference.RefCategoryFk, InRef.RefCategoryFk AS InRefCatFk, Reference.RefCache, Reference.NomRefCache, Reference.Title, RefCategory.RefCategoryAbbrev, InRefCategory.RefCategoryAbbrev AS InRefCat, InRef.Title AS InRefTitle " + 
			" FROM Reference INNER JOIN Reference AS InRef ON Reference.InRefFk = InRef.RefId INNER JOIN RefCategory ON Reference.RefCategoryFk = RefCategory.RefCategoryId INNER JOIN RefCategory AS InRefCategory ON InRef.RefCategoryFk = InRefCategory.RefCategoryId " +
						" WHERE (Reference.RefCategoryFk = 2) AND (InRef.RefCategoryFk = 9) ";
			ResultSet rs = source.getResultSet(strQueryPartOfJournal);
			boolean firstRow = true;
			while (rs.next()){
				if (firstRow){
					System.out.println("========================================================");
					logger.warn("There are part-of-references that have a Journal as in-reference!");
					System.out.println("========================================================");
				}
				int refId = rs.getInt("RefId");
				int categoryFk = rs.getInt("RefCategoryFk");
				String cat = rs.getString("RefCategoryAbbrev");
				int inRefFk = rs.getInt("InRefId");
				int inRefCategoryFk = rs.getInt("InRefCatFk");
				String inRefCat = rs.getString("InRefCat");
				String refCache = rs.getString("RefCache");
				String nomRefCache = rs.getString("nomRefCache");
				String title = rs.getString("title");
				String inRefTitle = rs.getString("InRefTitle");
				
				System.out.println("RefID:" + refId + "\n  cat: " + cat + 
						"\n  refCache: " + refCache + "\n  nomRefCache: " + nomRefCache + "\n  title: " + title + 
						"\n  inRefFk: " + inRefFk + "\n  inRefCategory: " + inRefCat + 
						"\n  inRefTitle: " + inRefTitle );
				result = firstRow = false;
			}
			
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	private static boolean checkPartOfUnresolved(BerlinModelImportConfigurator bmiConfig){
		try {
			boolean result = true;
			Source source = bmiConfig.getSource();
			String strQueryPartOfJournal = "SELECT Reference.RefId, InRef.RefId AS InRefID, Reference.RefCategoryFk, InRef.RefCategoryFk AS InRefCatFk, Reference.RefCache, Reference.NomRefCache, Reference.Title, RefCategory.RefCategoryAbbrev, InRefCategory.RefCategoryAbbrev AS InRefCat, InRef.Title AS InRefTitle " + 
			" FROM Reference INNER JOIN Reference AS InRef ON Reference.InRefFk = InRef.RefId INNER JOIN RefCategory ON Reference.RefCategoryFk = RefCategory.RefCategoryId INNER JOIN RefCategory AS InRefCategory ON InRef.RefCategoryFk = InRefCategory.RefCategoryId " +
						" WHERE (Reference.RefCategoryFk = 2) AND (InRef.RefCategoryFk = 10) ";
			ResultSet rs = source.getResultSet(strQueryPartOfJournal);
			boolean firstRow = true;
			while (rs.next()){
				if (firstRow){
					System.out.println("========================================================");
					logger.warn("There are part-of-references that have an 'unresolved' in-reference!");
					System.out.println("========================================================");
				}
				int refId = rs.getInt("RefId");
				int categoryFk = rs.getInt("RefCategoryFk");
				String cat = rs.getString("RefCategoryAbbrev");
				int inRefFk = rs.getInt("InRefId");
				int inRefCategoryFk = rs.getInt("InRefCatFk");
				String inRefCat = rs.getString("InRefCat");
				String refCache = rs.getString("RefCache");
				String nomRefCache = rs.getString("nomRefCache");
				String title = rs.getString("title");
				String inRefTitle = rs.getString("InRefTitle");
				
				System.out.println("RefID:" + refId + "\n  cat: " + cat + 
						"\n  refCache: " + refCache + "\n  nomRefCache: " + nomRefCache + "\n  title: " + title + 
						"\n  inRefFk: " + inRefFk + "\n  inRefCategory: " + inRefCat + 
						"\n  inRefTitle: " + inRefTitle );
				result = firstRow = false;
			}
			if (result == false){
				System.out.println("\nChoose a specific type from the following reference types: \n" +
						"  1) Article \n  2) Book \n  3) BookSection \n  4) CdDvd \n  5) ConferenceProceeding \n  6) Database\n" + 
						"  7) Generic \n  7) InProceedings \n  8) Journal \n  9) Map \n 10) Patent \n 11) PersonalCommunication\n" +
						" 12) PrintSeries \n 13) Proceedings \n 14) Report \n 15) Thesis \n 16) WebPage");
			}
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private static boolean checkPartOfPartOf(BerlinModelImportConfigurator bmiConfig){
		try {
			boolean result = true;
			Source source = bmiConfig.getSource();
			String strQueryPartOfJournal = "SELECT Reference.RefId, InRef.RefId AS InRefID, Reference.RefCategoryFk, InRef.RefCategoryFk AS InRefCatFk, Reference.RefCache, Reference.NomRefCache, Reference.Title, RefCategory.RefCategoryAbbrev, InRefCategory.RefCategoryAbbrev AS InRefCat, InRef.Title AS InRefTitle, InRef.InRefFk as InInRefId, InInRef.Title as inInRefTitle, InInRef.RefCategoryFk as inInRefCategory " + 
						" FROM Reference " +
							" INNER JOIN Reference AS InRef ON Reference.InRefFk = InRef.RefId " + 
							" INNER JOIN RefCategory ON Reference.RefCategoryFk = RefCategory.RefCategoryId " + 
							" INNER JOIN RefCategory AS InRefCategory ON InRef.RefCategoryFk = InRefCategory.RefCategoryId " +
							" INNER JOIN Reference AS InInRef ON InRef.InRefFk = InInRef.RefId " + 
						" WHERE (Reference.RefCategoryFk = 2) AND (InRef.RefCategoryFk = 2) ";
			ResultSet rs = source.getResultSet(strQueryPartOfJournal);
			boolean firstRow = true;
			while (rs.next()){
				if (firstRow){
					System.out.println("========================================================");
					logger.warn("There are part-of-references that are part of an other 'part-of' reference!\n" + 
							"         This is invalid or ambigous. Please try to determine the reference types more detailed ");
					System.out.println("========================================================");
				}
				int refId = rs.getInt("RefId");
				int categoryFk = rs.getInt("RefCategoryFk");
				String cat = rs.getString("RefCategoryAbbrev");
				int inRefFk = rs.getInt("InRefId");
				int inRefCategoryFk = rs.getInt("InRefCatFk");
				String inRefCat = rs.getString("InRefCat");
				String refCache = rs.getString("RefCache");
				String nomRefCache = rs.getString("nomRefCache");
				String title = rs.getString("title");
				String inRefTitle = rs.getString("InRefTitle");
				int inInRefId = rs.getInt("InInRefId");
				String inInRefTitle = rs.getString("inInRefTitle");
				int inInRefCategory = rs.getInt("inInRefCategory");
				
				System.out.println("RefID:" + refId + "\n  cat: " + cat + 
						"\n  refCache: " + refCache + "\n  nomRefCache: " + nomRefCache + "\n  title: " + title + 
						"\n  inRefFk: " + inRefFk + "\n  inRefCategory: " + inRefCat + 
						"\n  inRefTitle: " + inRefTitle + "\n  inInRefId: " + inInRefId + "\n  inInRefTitle: " + inInRefTitle +
						"\n  inInRefCategory: " + inInRefCategory );
				result = firstRow = false;
			}
			if (result == false){
				System.out.println("\nChoose a specific type from the following reference types: \n" +
						"  1) BookSection - Book - PrintSeries \n" +
						"  2) InProceedings - pProceedings  - PrintSeries");
			}
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	
	private static boolean checkPartOfArticle(BerlinModelImportConfigurator bmiConfig){
		try {
			boolean result = true;
			Source source = bmiConfig.getSource();
			String strQueryPartOfJournal = "SELECT Reference.RefId, InRef.RefId AS InRefID, Reference.RefCategoryFk, InRef.RefCategoryFk AS InRefCatFk, Reference.RefCache, Reference.NomRefCache, Reference.Title, Reference.NomTitleAbbrev as nomTitleAbbrev, RefCategory.RefCategoryAbbrev, InRefCategory.RefCategoryAbbrev AS InRefCat, InRef.Title AS InRefTitle, InRef.nomTitleAbbrev AS inRefnomTitleAbbrev, InRef.refCache AS inRefCache, InRef.nomRefCache AS inRefnomRefCache " + 
			" FROM Reference INNER JOIN Reference AS InRef ON Reference.InRefFk = InRef.RefId INNER JOIN RefCategory ON Reference.RefCategoryFk = RefCategory.RefCategoryId INNER JOIN RefCategory AS InRefCategory ON InRef.RefCategoryFk = InRefCategory.RefCategoryId " +
						" WHERE (Reference.RefCategoryFk = 2) AND (InRef.RefCategoryFk = 1) ";
			ResultSet rs = source.getResultSet(strQueryPartOfJournal);
			boolean firstRow = true;
			while (rs.next()){
				if (firstRow){
					System.out.println("========================================================");
					logger.warn("There are part-of-references that have an article as in-reference!");
					System.out.println("========================================================");
				}
				int refId = rs.getInt("RefId");
				int categoryFk = rs.getInt("RefCategoryFk");
				String cat = rs.getString("RefCategoryAbbrev");
				int inRefFk = rs.getInt("InRefId");
				int inRefCategoryFk = rs.getInt("InRefCatFk");
				String inRefCat = rs.getString("InRefCat");
				String refCache = rs.getString("RefCache");
				String nomRefCache = rs.getString("nomRefCache");
				String title = rs.getString("title");
				String nomTitleAbbrev = rs.getString("nomTitleAbbrev");
				String inRefTitle = rs.getString("InRefTitle");
				String inRefnomTitleAbbrev = rs.getString("inRefnomTitleAbbrev");
				String inRefnomRefCache = rs.getString("inRefnomRefCache");
				String inRefCache = rs.getString("inRefCache");
				
				System.out.println("RefID:" + refId + "\n  cat: " + cat + 
						"\n  refCache: " + refCache + "\n  nomRefCache: " + nomRefCache + "\n  title: " + title + "\n  titleAbbrev: " + nomTitleAbbrev + 
						"\n  inRefFk: " + inRefFk + "\n  inRefCategory: " + inRefCat + 
						"\n  inRefTitle: " + inRefTitle + "\n  inRefTitleAbbrev: " + inRefnomTitleAbbrev +
						"\n  inRefnomRefCache: " + inRefnomRefCache + "\n  inRefCache: " + inRefCache 
						);
				result = firstRow = false;
			}
			
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private static boolean checkJournalsWithSeries(BerlinModelImportConfigurator bmiConfig){
		try {
			boolean result = true;
			Source source = bmiConfig.getSource();
			String strQueryArticlesWithoutJournal = "SELECT Reference.RefId, Reference.RefCategoryFk, Reference.RefCache, Reference.NomRefCache, Reference.Title, Reference.NomTitleAbbrev, RefCategory.RefCategoryAbbrev  " + 
						" FROM Reference INNER JOIN " +
								" RefCategory ON Reference.RefCategoryFk = RefCategory.RefCategoryId  " +
						" WHERE (Reference.RefCategoryFk = 9)  AND ( Reference.series is not null OR Reference.series <>'') ";
			ResultSet rs = source.getResultSet(strQueryArticlesWithoutJournal);
			boolean firstRow = true;
			while (rs.next()){
				if (firstRow){
					System.out.println("========================================================");
					logger.warn("There are Journals with series!");
					System.out.println("========================================================");
				}
				int refId = rs.getInt("RefId");
				int categoryFk = rs.getInt("RefCategoryFk");
				String cat = rs.getString("RefCategoryAbbrev");
				String nomRefCache = rs.getString("nomRefCache");
				String refCache = rs.getString("refCache");
				String title = rs.getString("title");
				String nomTitleAbbrev = rs.getString("nomTitleAbbrev");
				
				System.out.println("RefID:" + refId + "\n  cat: " + cat + 
						"\n  refCache: " + refCache + "\n  nomRefCache: " + nomRefCache + 
						"\n  title: " + title +  "\n  nomTitleAbbrev: " + nomTitleAbbrev +
						"" );
				result = firstRow = false;
			}
			
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
//********************************** MAIN ************************************************

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TimePeriod timePeriod = ImportHelper.getDatePublished("1756 - 1783");
		System.out.println(timePeriod.getYear());
	}

}
