/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.in;

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
import static eu.etaxonomy.cdm.io.common.ImportHelper.NO_OVERWRITE;
import static eu.etaxonomy.cdm.io.common.ImportHelper.OBLIGATORY;
import static eu.etaxonomy.cdm.io.common.ImportHelper.OVERWRITE;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.berlinModel.CdmExtensionMapper;
import eu.etaxonomy.cdm.io.berlinModel.CdmOneToManyMapper;
import eu.etaxonomy.cdm.io.berlinModel.CdmStringMapper;
import eu.etaxonomy.cdm.io.common.CdmAttributeMapperBase;
import eu.etaxonomy.cdm.io.common.CdmIoMapping;
import eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
/*import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.BookSection;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.Generic;*/
import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.IGeneric;
import eu.etaxonomy.cdm.model.reference.IJournal;
import eu.etaxonomy.cdm.model.reference.IPrintSeries;
import eu.etaxonomy.cdm.model.reference.IWebPage;
/*import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.reference.PrintSeries;
import eu.etaxonomy.cdm.model.reference.Proceedings;*/
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
/*import eu.etaxonomy.cdm.model.reference.Thesis;
import eu.etaxonomy.cdm.model.reference.WebPage;*/

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelReferenceImport extends BerlinModelImportBase {
	private static final Logger logger = Logger.getLogger(BerlinModelReferenceImport.class);

	public static final UUID REF_DEPOSITED_AT_UUID = UUID.fromString("23ca88c7-ce73-41b2-8ca3-2cb22f013beb");
	public static final UUID REF_SOURCE = UUID.fromString("d6432582-2216-4b08-b0db-76f6c1013141");
	public static final UUID DATE_STRING_UUID = UUID.fromString("e4130eae-606e-4b0c-be4f-e93dc161be7d");
	ReferenceFactory refFactory;
	private int modCount = 1000;
	
	public BerlinModelReferenceImport(){
		super();
		refFactory = ReferenceFactory.newInstance();
	}

	
	
	protected boolean initializeMappers(BerlinModelImportState state, String tableName){
		for (CdmAttributeMapperBase mapper: classMappers){
			if (mapper instanceof CdmExtensionMapper){
				((CdmExtensionMapper)mapper).initialize(getTermService(), state, tableName);
			}
		}
		return true;
	}
	
	protected static CdmAttributeMapperBase[] classMappers = new CdmAttributeMapperBase[]{
		new CdmStringMapper("edition", "edition"),
		new CdmStringMapper("volume", "volume"),
		new CdmStringMapper("publisher", "publisher"),
		new CdmStringMapper("publicationTown", "placePublished"),
		new CdmStringMapper("isbn", "isbn"),
		new CdmStringMapper("isbn", "isbn"),
		new CdmStringMapper("pageString", "pages"),
		new CdmStringMapper("series", "series"),
		new CdmStringMapper("issn", "issn"),
		new CdmStringMapper("url", "uri"),
		new CdmExtensionMapper("NomStandard", ExtensionType.NOMENCLATURAL_STANDARD()),
		new CdmExtensionMapper("DateString", DATE_STRING_UUID, "Date String", "Date String", "dates"),
		new CdmExtensionMapper("RefDepositedAt", REF_DEPOSITED_AT_UUID, "RefDepositedAt", "reference is deposited at", "at"),
		new CdmExtensionMapper("RefSource", REF_SOURCE, "RefSource", "reference source", "source")
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
			"isPaper", "exportDate", 
	};
	
	//TODO isPaper
	//
	
	
	
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
	protected boolean doCheck(BerlinModelImportState state){
		boolean result = true;
		BerlinModelImportConfigurator bmiConfig = state.getConfig();
		result &= checkArticlesWithoutJournal(bmiConfig);
		result &= checkPartOfJournal(bmiConfig);
		result &= checkPartOfUnresolved(bmiConfig);
		result &= checkPartOfPartOf(bmiConfig);
		result &= checkPartOfArticle(bmiConfig);
		result &= checkJournalsWithSeries(bmiConfig);
		result &= checkObligatoryAttributes(bmiConfig);
		result &= checkPartOfWithVolume(bmiConfig);
		result &= checkArticleWithEdition(bmiConfig);
		
		if (result == false ){System.out.println("========================================================");}
		
		
		return result;
	}
		

	
	private boolean doPreliminaryRefDetails(BerlinModelImportState state, Map<String, MapWrapper<? extends CdmBase>> stores){
		
		IImportConfigurator config = state.getConfig(); 
		MapWrapper<TeamOrPersonBase> teamMap = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.TEAM_STORE);
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
					ReferenceBase genericReference = refFactory.newGeneric();
					genericReference.setTitleCache(fullNomRefCache);
					nomRefDetailMap.put(refDetailId, genericReference);
					//refId, created, notes
					doIdCreatedUpdatedNotes(state, genericReference, rs, refDetailId, namespace );						
					//year
					genericReference.setDatePublished(ImportHelper.getDatePublished(refYear)); 
					refCounter.nomRefCount++;
				}	
				
				//biblioRef
				String fullRefCache = rs.getString("fullRefCache"); 
				if (! CdmUtils.Nz(fullRefCache).trim().equals("") && 
						fullRefCache.equals(fullNomRefCache)){
					ReferenceBase genericReference = refFactory.newGeneric();
					genericReference.setTitleCache(fullRefCache);
					refDetailMap.put(refDetailId, genericReference);
					
					//refId, created, notes
					doIdCreatedUpdatedNotes(state, genericReference, rs, refDetailId, namespace );						
					//year
					genericReference.setDatePublished(ImportHelper.getDatePublished(refYear)); 
					refCounter.referenceCount++;
				}
			}
			//save and store in map
			logger.info("Save nomenclatural preliminary references (" + refCounter.nomRefCount + ")");
			Collection<ReferenceBase> col = nomRefDetailMap.objects();
			getReferenceService().save(col);
			logger.info("Save bibliographical preliminary references (" + refCounter.referenceCount +")");
			getReferenceService().save(refDetailMap.objects());
			
			//TODO
			//SecondarySources
			//IdInSource

		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
		return success;
	}
	
	

	@Override
	protected boolean doInvoke(BerlinModelImportState state){
		String teamStore = ICdmIO.TEAM_STORE;
		MapWrapper<? extends CdmBase> store = state.getStore(teamStore);
		MapWrapper<ReferenceBase> referenceStore= new MapWrapper<ReferenceBase>(null);
		MapWrapper<ReferenceBase> nomRefStore= new MapWrapper<ReferenceBase>(null);
		
		BerlinModelImportConfigurator config = state.getConfig();
		Source source = config.getSource();
		
		boolean success = true;
		
		//preliminary RefDetails  //TODO -> move to own class ?
		doPreliminaryRefDetails(state, state.getStores());
		
		success &= initializeMappers(state, "Reference");
		
		logger.info("start makeReferences ...");
		
		try {
			//get data from database
			String strQueryBase = 
					" SELECT Reference.* , InReference.RefId as InRefId, InReference.RefCategoryFk as InRefCategoryFk,  " +
						" InInReference.RefId as InInRefId, InInReference.RefCategoryFk as InInRefCategoryFk, " +
						" InReference.InRefFk AS InRefInRefFk, InInReference.InRefFk AS InInRefInRefFk, RefSource.RefSource " +
                    " FROM Reference AS InInReference " +
                    	" RIGHT OUTER JOIN Reference AS InReference ON InInReference.RefId = InReference.InRefFk " + 
                    	" RIGHT OUTER JOIN Reference ON InReference.RefId = dbo.Reference.InRefFk " + 
                    	" LEFT OUTER JOIN RefSource ON Reference.RefSourceFk = RefSource.RefSourceId " +
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

			if (config.getDoReferences() == CONCEPT_REFERENCES){
				strQueryNoInRef += " AND ( Reference.refId IN ( SELECT ptRefFk FROM PTaxon) ) ";
			}
			
			List<ResultSet> resultSetList = new ArrayList<ResultSet>();
			resultSetList.add(source.getResultSet(strQueryNoInRef));
			if (config.getDoReferences() == ALL || config.getDoReferences() == NOMENCLATURAL){
				resultSetList.add(source.getResultSet(strQuery1InRef));
				resultSetList.add(source.getResultSet(strQuery2InRef));
			}
			
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
				
					success &= makeSingleReferenceRecord(rs, state, referenceStore, nomRefStore, refCounter);
				} // end resultSet
								
				//for the concept reference a fixed uuid may be needed -> change uuid
				ReferenceBase<?> sec = referenceStore.get(config.getSourceSecId());
				if (sec == null){
					sec = nomRefStore.get(config.getSourceSecId());	
				}
				if (sec != null){
					sec.setUuid(config.getSecUuid());
					logger.info("SecUuid changed to: " + config.getSecUuid());
				}
				
				//save and store in map
				logger.info("Save nomenclatural references (" + refCounter.nomRefCount + ")");
				getReferenceService().save(nomRefStore.objects());
				logger.info("Save bibliographical references (" + refCounter.referenceCount +")");
				getReferenceService().save(referenceStore.objects());
				j++;
			}//end resultSetList	

			logger.info("end makeReferences ..." + getSuccessString(success));;
			return success;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	}
	
	
	private boolean makeSingleReferenceRecord(ResultSet rs, BerlinModelImportState state, MapWrapper<ReferenceBase> referenceStore, MapWrapper<ReferenceBase> nomRefStore, RefCounter refCounter){
		boolean success = true;
		String namespace = "Reference";
		String teamStore = ICdmIO.TEAM_STORE;
		MapWrapper<? extends CdmBase> store = state.getStore(teamStore);
		MapWrapper<TeamOrPersonBase> teamMap = (MapWrapper<TeamOrPersonBase>)store;
		
		Integer refId = null;
		try {
			Map<String, Object> valueMap = getValueMap(rs);
			
			Integer categoryFk = (Integer)valueMap.get("refCategoryFk".toLowerCase());
			refId = (Integer)valueMap.get("refId".toLowerCase());
			Boolean thesisFlag = (Boolean)valueMap.get("thesisFlag".toLowerCase());
			
			
			ReferenceBase<?> referenceBase;
			logger.debug("RefCategoryFk: " + categoryFk);
			
			if (thesisFlag){
				referenceBase = makeThesis(valueMap);
			}else if (categoryFk == REF_JOURNAL){
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
				referenceBase = refFactory.newGeneric();
				success = false;
			}
							
			//refId, created, notes
			doIdCreatedUpdatedNotes(state, referenceBase, rs, refId, namespace );						
			//refYear
			String refYear = (String)valueMap.get("refYear".toLowerCase());
			referenceBase.setDatePublished(ImportHelper.getDatePublished(refYear)); 
			
			//nom&BiblioReference
			success &= makeNomAndBiblioReference(rs, refId, referenceBase, refCounter, 
					referenceStore, nomRefStore, teamMap, state.getStores() );
			
			//idInSource
			String idInSource = (String)valueMap.get("IdInSource".toLowerCase());
			if (CdmUtils.isNotEmpty(idInSource)){
				IdentifiableSource source = IdentifiableSource.NewInstance(idInSource);
				source.setIdNamespace("import to Berlin Model");
				referenceBase.addSource(source);
			}
			
			//isPaper
			if ((Boolean)valueMap.get("isPaper".toLowerCase())){
				logger.warn("IsPaper is not yet implemented, but reference " +  refId + " is paper");
			}

		} catch (Exception e) {
			logger.warn("Reference with BM refId '" + CdmUtils.Nz(refId) +  "' threw Exception and could not be saved");
			e.printStackTrace();
			success = false;
		}
		return success;
	}

	
	private boolean makeNomAndBiblioReference(ResultSet rs, 
				int refId, 
				ReferenceBase<?> referenceBase,  
				RefCounter refCounter, 
				MapWrapper<ReferenceBase> referenceStore, 
				MapWrapper<ReferenceBase> nomRefStore, 
				MapWrapper<TeamOrPersonBase> teamMap,
				Map<String, MapWrapper<? extends CdmBase>> stores				
				) throws SQLException{
		
		MapWrapper<ReferenceBase<?>> referenceMap = (MapWrapper<ReferenceBase<?>>)stores.get(ICdmIO.REFERENCE_STORE);
		MapWrapper<ReferenceBase<?>> nomRefMap = (MapWrapper<ReferenceBase<?>>)stores.get(ICdmIO.NOMREF_STORE);
		
		
		String refCache = rs.getString("refCache");
		String nomRefCache = rs.getString("nomRefCache");
		String title = rs.getString("title");
		String nomTitleAbbrev = rs.getString("nomTitleAbbrev");
		boolean isPreliminary = rs.getBoolean("PreliminaryFlag");
		String refAuthorString = rs.getString("refAuthorString");
		int nomAuthorTeamFk = rs.getInt("NomAuthorTeamFk");
		TeamOrPersonBase<?> nomAuthor = teamMap.get(nomAuthorTeamFk);
		
		boolean hasNomRef = false;
		//is Nomenclatural Reference
		if ( (CdmUtils.isNotEmpty(nomRefCache) && isPreliminary) || (CdmUtils.isNotEmpty(nomTitleAbbrev) && ! isPreliminary) ){
			referenceBase.setTitle(nomTitleAbbrev);
			TeamOrPersonBase<?> author = getAuthorTeam(refAuthorString , nomAuthor, true);
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
		if ((CdmUtils.isNotEmpty(refCache) && isPreliminary && ! refCache.equalsIgnoreCase(nomRefCache)) 
				|| (CdmUtils.isNotEmpty(title) && ! isPreliminary && ! title.equalsIgnoreCase(nomTitleAbbrev)) 
				|| hasNomRef == false){
			if (hasNomRef){
				referenceBase = (ReferenceBase)referenceBase.clone();
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
	
	private ReferenceBase<?> makeArticle (Map<String, Object> valueMap, MapWrapper<ReferenceBase> referenceStore, MapWrapper<ReferenceBase> nomRefStore){
		
		IArticle article = refFactory.newArticle();
		Object inRefFk = valueMap.get("inRefFk".toLowerCase());
		Integer inRefCategoryFk = (Integer)valueMap.get("inRefCategoryFk".toLowerCase());
		Integer refId = (Integer)valueMap.get("refId".toLowerCase());
		
		if (inRefFk != null){
			if (inRefCategoryFk == REF_JOURNAL){
				int inRefFkInt = (Integer)inRefFk;
				if (nomRefStore.containsId(inRefFkInt) || referenceStore.containsId(inRefFkInt)){
					ReferenceBase<?> inJournal = nomRefStore.get(inRefFkInt);
					if (inJournal == null){
						inJournal = referenceStore.get(inRefFkInt);
						logger.info("inJournal (" + inRefFkInt + ") found in referenceStore instead of nomRefStore.");
						nomRefStore.put(inRefFkInt, inJournal);
					}
					if (inJournal == null){
						logger.warn("inJournal for " + inRefFkInt + " is null. "+
						" InReference relation could not be set");
					//}else if (ReferenceBase.class.isAssignableFrom(inJournal.getClass())){
					}else if (inJournal.getType().equals(ReferenceType.Journal)){
						article.setInJournal((IJournal)inJournal);
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
		makeStandardMapper(valueMap, (ReferenceBase)article); //url, pages, series, volume
		return (ReferenceBase)article;
	}
	
	private ReferenceBase<?> makePartOfOtherTitle (Map<String, Object> valueMap, MapWrapper<ReferenceBase> referenceStore, MapWrapper<ReferenceBase> nomRefStore){
		ReferenceBase<?> result;
		Object inRefFk = valueMap.get("inRefFk".toLowerCase());
		Integer inRefCategoryFk = (Integer)valueMap.get("inRefCategoryFk".toLowerCase());
		Integer refId = (Integer)valueMap.get("refId".toLowerCase());
		
		if (inRefCategoryFk == REF_BOOK){
			//BookSection
			IBookSection bookSection = refFactory.newBookSection();
			result = (ReferenceBase)bookSection;
			if (inRefFk != null){
				int inRefFkInt = (Integer)inRefFk;
				if (nomRefStore.containsId(inRefFkInt) || referenceStore.containsId(inRefFkInt)){
					ReferenceBase<?> inBook = nomRefStore.get(inRefFkInt);
					if (inBook == null){
						inBook = referenceStore.get(inRefFkInt);
						logger.info("inBook (" + inRefFkInt + ") found in referenceStore instead of nomRefStore.");
						nomRefStore.put(inRefFkInt, inBook);
					}
					if (inBook == null){
						logger.warn("inBook for " + inRefFkInt + " is null. "+
						" InReference relation could not be set");;
					//}else if (Book.class.isAssignableFrom(inBook.getClass())){
					}else if (inBook.getType().equals(ReferenceType.Book)){
						bookSection.setInBook((IBook)inBook);
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
			result = refFactory.newGeneric();
		}else if (inRefCategoryFk == REF_JOURNAL){
			//TODO 
			logger.warn("Reference (refId = " + refId + ") of type 'part_of_other_title' has inReference of type 'journal'." +
					" This is not allowed! Generic reference created instead") ;
			result = refFactory.newGeneric();
			result.addMarker(Marker.NewInstance(MarkerType.TO_BE_CHECKED(), true));
		}else{
			logger.warn("InReference type (catFk = " + inRefCategoryFk + ") of part-of-reference not recognized for refId " + refId + "." +
				" Create 'Generic' reference instead");
			result = refFactory.newGeneric();
		}
		makeStandardMapper(valueMap, result); //url, pages
		return result;
	}
	
	private ReferenceBase<?> makeWebSite(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Website'");}
		ReferenceBase webPage = refFactory.newWebPage();
		makeStandardMapper(valueMap, webPage); //placePublished, publisher
		return webPage;
	}
	
	private ReferenceBase<?> makeUnknown(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Unknown'");}
		ReferenceBase generic = refFactory.newGeneric();
//		generic.setSeries(series);
		makeStandardMapper(valueMap, generic); //pages, placePublished, publisher, series, volume
		return generic;
	}

	private ReferenceBase<?> makeInformal(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Informal'");}
		ReferenceBase generic = refFactory.newGeneric();
//		informal.setSeries(series);
		makeStandardMapper(valueMap, generic);//editor, pages, placePublished, publisher, series, volume
		String informal = (String)valueMap.get("InformalRefCategory".toLowerCase());
		if (CdmUtils.isNotEmpty(informal) ){
			generic.addExtension(informal, ExtensionType.INFORMAL_CATEGORY());
		}
		return generic;
	}
	
	private ReferenceBase<?> makeDatabase(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Database'");}
		ReferenceBase database =  refFactory.newDatabase();
		makeStandardMapper(valueMap, database); //?
		return database;
	}
	
	private ReferenceBase<?> makeJournal(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Journal'");}
		ReferenceBase journal = refFactory.newJournal();
		
		Set<String> omitAttributes = new HashSet<String>();
		String series = "series";
//		omitAttributes.add(series);
		
		makeStandardMapper(valueMap, journal, omitAttributes); //issn,placePublished,publisher
//		if (valueMap.get(series) != null){
//			logger.warn("Series not yet implemented for journal!");
//		}
		return journal;
	}
	
	private ReferenceBase<?> makeBook(Map<String, Object> valueMap, MapWrapper<ReferenceBase> referenceStore, MapWrapper<ReferenceBase> nomRefStore){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Book'");}
		ReferenceBase book = refFactory.newBook();
		Integer refId = (Integer)valueMap.get("refId".toLowerCase());
		
		//Set bookAttributes = new String[]{"edition", "isbn", "pages","publicationTown","publisher","volume"};
		
		Set<String> omitAttributes = new HashSet<String>();
		String attrSeries = "series";
//		omitAttributes.add(attrSeries);
		
		makeStandardMapper(valueMap, book, omitAttributes);
		
		//Series (as String)
		IPrintSeries printSeries = null;
		if (valueMap.get(attrSeries) != null){
			String series = (String)valueMap.get("title".toLowerCase());
			if (series == null){
				String nomTitle = (String)valueMap.get("nomTitleAbbrev".toLowerCase());
				series = nomTitle;
			}
			printSeries = refFactory.newPrintSeries(series);
			//TODO only one for ref and nomRef
			logger.warn("Implementation of printSeries is preliminary");
		}
		Object inRefFk = valueMap.get("inRefFk".toLowerCase());
		//Series (as Reference)
		if (inRefFk != null){
			int inRefFkInt = (Integer)inRefFk;
			if (nomRefStore.containsId(inRefFkInt) || referenceStore.containsId(inRefFkInt)){
				ReferenceBase<?> inSeries = nomRefStore.get(inRefFkInt);
				if (inSeries == null){
					inSeries = referenceStore.get(inRefFkInt);
					logger.info("inSeries (" + inRefFkInt + ") found in referenceStore instead of nomRefStore.");
					nomRefStore.put(inRefFkInt, inSeries);
				}
				if (inSeries == null){
					logger.warn("inSeries for " + inRefFkInt + " is null. "+
					" InReference relation could not be set");;
				//}else if (PrintSeries.class.isAssignableFrom(inSeries.getClass())){
				}else if (inSeries.getType().equals(ReferenceType.PrintSeries)){
					book.setInSeries((IPrintSeries)inSeries);
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
	
	private ReferenceBase<?> makePrintSeries(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'PrintSeries'");}
		ReferenceBase printSeries = refFactory.newPrintSeries();
		makeStandardMapper(valueMap, printSeries, null);
		return printSeries;
	}
	
	private ReferenceBase<?> makeProceedings(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Proceedings'");}
		ReferenceBase proceedings = refFactory.newProceedings();
		makeStandardMapper(valueMap, proceedings, null);	
		return proceedings;
	}

	private ReferenceBase<?> makeThesis(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Thesis'");}
		ReferenceBase thesis = refFactory.newThesis();
		makeStandardMapper(valueMap, thesis, null);	
		return thesis;
	}

	
	private ReferenceBase<?> makeJournalVolume(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'JournalVolume'");}
		//Proceedings proceedings = Proceedings.NewInstance();
		ReferenceBase journalVolume = refFactory.newGeneric();
		makeStandardMapper(valueMap, journalVolume, null);	
		logger.warn("Journal volumes not yet implemented. Generic created instead but with errors");
		return journalVolume;
	}
	
	private boolean makeStandardMapper(Map<String, Object> valueMap, ReferenceBase<?> ref){
		return makeStandardMapper(valueMap, ref, null);
	}

	
	private boolean makeStandardMapper(Map<String, Object> valueMap, CdmBase cdmBase, Set<String> omitAttributes){
		boolean result = true;	
		for (CdmAttributeMapperBase mapper : classMappers){
			if (mapper instanceof CdmSingleAttributeMapperBase){
				result &= makeStandardSingleMapper(valueMap, cdmBase, (CdmSingleAttributeMapperBase)mapper, omitAttributes);
			}else if (mapper instanceof CdmOneToManyMapper){
				result &= makeMultipleValueAddMapper(valueMap, cdmBase, (CdmOneToManyMapper)mapper, omitAttributes);
			}else{
				logger.error("Unknown mapper type");
				result = false;
			}
		}
		return result;
	}
	
	private boolean makeStandardSingleMapper(Map<String, Object> valueMap, CdmBase cdmBase, CdmSingleAttributeMapperBase mapper, Set<String> omitAttributes){
		boolean result = true;
		if (omitAttributes == null){
			omitAttributes = new HashSet<String>();
		}
		if (mapper instanceof CdmExtensionMapper){
			result &= ((CdmExtensionMapper)mapper).invoke(valueMap, cdmBase);
		}else{
			String sourceAttribute = mapper.getSourceAttributeList().get(0).toLowerCase();
			Object value = valueMap.get(sourceAttribute);
			if (value != null){
				String destinationAttribute = mapper.getDestinationAttribute();
				if (! omitAttributes.contains(destinationAttribute)){
					result &= ImportHelper.addValue(value, cdmBase, destinationAttribute, mapper.getTypeClass(), OVERWRITE, OBLIGATORY);
				}
			}
		}
		return result;
	}

	
	private boolean makeMultipleValueAddMapper(Map<String, Object> valueMap, CdmBase cdmBase, CdmOneToManyMapper<CdmBase, CdmBase, CdmSingleAttributeMapperBase> mapper, Set<String> omitAttributes){
		if (omitAttributes == null){
			omitAttributes = new HashSet<String>();
		}
		boolean result = true;
		String destinationAttribute = mapper.getSingleAttributeName();
		List<Object> sourceValues = new ArrayList<Object>();
		List<Class> classes = new ArrayList<Class>();
		for (CdmSingleAttributeMapperBase singleMapper : mapper.getSingleMappers()){
			String sourceAttribute = singleMapper.getSourceAttribute();
			Object value = valueMap.get(sourceAttribute);
			sourceValues.add(value);
			Class<?> clazz = singleMapper.getTypeClass();
			classes.add(clazz);
		}
		
		result &= ImportHelper.addMultipleValues(sourceValues, cdmBase, destinationAttribute, classes, NO_OVERWRITE, OBLIGATORY);
//		//only for testing
//		if (cdmBase instanceof PublicationBase){
//			PublicationBase pub = ((PublicationBase)cdmBase);
//			pub.addPublisher("A new publisher for " + pub.getTitleCache(), "A nice place");
//		}
		return result;
	}

	
	private static TeamOrPersonBase<?> getAuthorTeam(String authorString, TeamOrPersonBase<?> nomAuthor, boolean preferNomeclaturalAuthor){
		TeamOrPersonBase<?> result;
		if (preferNomeclaturalAuthor){
			if (nomAuthor != null){
				result = nomAuthor;
			}else{
				if (CdmUtils.Nz(authorString).equals("")){
					result = null;
				}else{
					TeamOrPersonBase<?> team = Team.NewInstance();
					//TODO which one to use??
					team.setNomenclaturalTitle(authorString);
					team.setTitleCache(authorString);
					result = team;
				}
			}
		}else{ //prefer bibliographic
			if (! CdmUtils.Nz(authorString).equals("")){
				TeamOrPersonBase<?> team = Team.NewInstance();
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
	protected boolean isIgnore(BerlinModelImportState state){
		return (state.getConfig().getDoReferences() == IImportConfigurator.DO_REFERENCES.NONE);
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
				//int categoryFk = resulSetarticlesWithoutJournal.getInt("RefCategoryFk");
				String cat = resulSetarticlesWithoutJournal.getString("RefCategoryAbbrev");
				int inRefFk = resulSetarticlesWithoutJournal.getInt("InRefId");
				//int inRefCategoryFk = resulSetarticlesWithoutJournal.getInt("InRefCatFk");
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
				//int categoryFk = rs.getInt("RefCategoryFk");
				String cat = rs.getString("RefCategoryAbbrev");
				int inRefFk = rs.getInt("InRefId");
				//int inRefCategoryFk = rs.getInt("InRefCatFk");
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
				//int categoryFk = rs.getInt("RefCategoryFk");
				String cat = rs.getString("RefCategoryAbbrev");
				int inRefFk = rs.getInt("InRefId");
				//int inRefCategoryFk = rs.getInt("InRefCatFk");
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
				//int categoryFk = rs.getInt("RefCategoryFk");
				String cat = rs.getString("RefCategoryAbbrev");
				int inRefFk = rs.getInt("InRefId");
				//int inRefCategoryFk = rs.getInt("InRefCatFk");
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
				//int categoryFk = rs.getInt("RefCategoryFk");
				String cat = rs.getString("RefCategoryAbbrev");
				int inRefFk = rs.getInt("InRefId");
				//int inRefCategoryFk = rs.getInt("InRefCatFk");
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
				//int categoryFk = rs.getInt("RefCategoryFk");
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
	
	private static boolean checkPartOfWithVolume(BerlinModelImportConfigurator bmiConfig){
		try {
			boolean result = true;
			Source source = bmiConfig.getSource();
			String strQueryArticlesWithoutJournal = "SELECT Ref.RefId as refId, RefCategory.RefCategoryAbbrev as refCategoryAbbrev, Ref.nomRefCache as nomRefCache, Ref.refCache as refCache,Ref.volume as volume, Ref.Series as series, Ref.Edition as edition, Ref.title as title, Ref.nomTitleAbbrev as nomTitleAbbrev,InRef.RefCache as inRefRefCache, InRef.NomRefCache  as inRefNomRefCache, InRef.RefId as inRefId, InRef.Volume as inRefVol, InRef.Series as inRefSeries, InRef.Edition as inRefEdition" +
					" FROM Reference AS Ref " + 
					 	" INNER JOIN RefCategory ON Ref.RefCategoryFk = RefCategory.RefCategoryId " +
					 	"  LEFT OUTER JOIN Reference AS InRef ON Ref.InRefFk = InRef.RefId " +
					" WHERE (Ref.RefCategoryFk = 2) AND ((Ref.Volume IS NOT NULL) OR (Ref.Series IS NOT NULL) OR (Ref.Edition IS NOT NULL)) " ; 
			ResultSet rs = source.getResultSet(strQueryArticlesWithoutJournal);
			boolean firstRow = true;
			while (rs.next()){
				if (firstRow){
					System.out.println("========================================================");
					logger.warn("There are PartOfOtherTitles with volumes, editions or series !");
					System.out.println("========================================================");
				}
				int refId = rs.getInt("refId");
				String cat = rs.getString("refCategoryAbbrev");
				String nomRefCache = rs.getString("nomRefCache");
				String refCache = rs.getString("refCache");
				String title = rs.getString("title");
				String nomTitleAbbrev = rs.getString("nomTitleAbbrev");
				String volume = rs.getString("volume");
				String edition = rs.getString("edition");
				String series = rs.getString("series");
				String inRefRefCache = rs.getString("inRefRefCache");
				String inRefNomRefCache = rs.getString("inRefNomRefCache");
				int inRefId = rs.getInt("inRefId");
				String inRefVolume = rs.getString("inRefVol");
				String inRefSeries = rs.getString("inRefSeries");
				String inRefEdition = rs.getString("inRefEdition");
				
				System.out.println("RefID:" + refId + "\n  cat: " + cat + 
						"\n  refCache: " + refCache + "\n  nomRefCache: " + nomRefCache + 
						"\n  title: " + title +  "\n  nomTitleAbbrev: " + nomTitleAbbrev + "\n  volume: " + volume + "\n  series: " + series +"\n  edition: " + edition +
						"\n  inRef-ID:" + inRefId + "\n  inRef-cache: " + inRefRefCache +  
						"\n  inRef-nomCache: " + inRefNomRefCache + "\n  inRef-volume: " + inRefVolume +"\n  inRef-series: " + inRefSeries +"\n  inRef-edition: " + inRefEdition +
						"" );
				result = firstRow = false;
			}
			
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private static boolean checkArticleWithEdition(BerlinModelImportConfigurator bmiConfig){
		try {
			boolean result = true;
			Source source = bmiConfig.getSource();
			String strQueryArticlesWithoutJournal = "SELECT Ref.RefId as refId, RefCategory.RefCategoryAbbrev as refCategoryAbbrev, Ref.nomRefCache as nomRefCache, Ref.refCache as refCache,Ref.edition as edition, Ref.title as title, Ref.nomTitleAbbrev as nomTitleAbbrev,InRef.RefCache as inRefRefCache, InRef.NomRefCache  as inRefNomRefCache, InRef.RefId as inRefId, InRef.Edition as inRefEdition" +
					" FROM Reference AS Ref " + 
					 	" INNER JOIN RefCategory ON Ref.RefCategoryFk = RefCategory.RefCategoryId " +
					 	"  LEFT OUTER JOIN Reference AS InRef ON Ref.InRefFk = InRef.RefId " +
					" WHERE (Ref.RefCategoryFk = 1) AND (NOT (Ref.Edition IS NULL))  " +
					" ORDER BY InRef.RefId "; 
			ResultSet rs = source.getResultSet(strQueryArticlesWithoutJournal);
			boolean firstRow = true;
			while (rs.next()){
				if (firstRow){
					System.out.println("========================================================");
					logger.warn("There are Articles with editions !");
					System.out.println("========================================================");
				}
				int refId = rs.getInt("refId");
				String cat = rs.getString("refCategoryAbbrev");
				String nomRefCache = rs.getString("nomRefCache");
				String refCache = rs.getString("refCache");
				String title = rs.getString("title");
				String nomTitleAbbrev = rs.getString("nomTitleAbbrev");
				String edition = rs.getString("edition");
				String inRefRefCache = rs.getString("inRefRefCache");
				String inRefNomRefCache = rs.getString("inRefNomRefCache");
				int inRefId = rs.getInt("inRefId");
				String inRefEdition = rs.getString("inRefEdition");
				
				System.out.println("RefID:" + refId + "\n  cat: " + cat + 
						"\n  refCache: " + refCache + "\n  nomRefCache: " + nomRefCache + 
						"\n  title: " + title +  "\n  nomTitleAbbrev: " + nomTitleAbbrev + "\n  edition: " + edition + 
						"\n  inRef-ID:" + inRefId + "\n  inRef-cache: " + inRefRefCache +  
						"\n  inRef-nomCache: " + inRefNomRefCache + "\n  inRef-edition: " + inRefEdition +
						"" );
				result = firstRow = false;
			}
			
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
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
		for (CdmAttributeMapperBase mapper : classMappers){
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

	protected boolean checkRefDetailUnimplementedAttributes(IImportConfigurator config){
		boolean result = true;
		
		try {
			String strQuery = " SELECT Count(*) as n" +
			    " FROM RefDetail " +
//            	" INNER JOIN Reference ON Reference.RefId = RefDetail.RefFk " +
			    " WHERE SecondarySources is not NULL AND SecondarySources <> '' ";
			BerlinModelImportConfigurator bmiConfig = (BerlinModelImportConfigurator)config;
			Source source = bmiConfig.getSource();
			ResultSet rs = source.getResultSet(strQuery);
			
			rs.next();
			int count = rs.getInt("n");
			if (count > 0){
				System.out.println("========================================================");
				logger.warn("There are "+ count + " RefDetails with SecondarySources <> NULL ! Secondary sources are not yet implemented for Berlin Model Import");
				System.out.println("========================================================");
				
			}
			strQuery = " SELECT Count(*) as n" +
			    " FROM RefDetail " +
//	            	" INNER JOIN Reference ON Reference.RefId = RefDetail.RefFk " +
			    " WHERE IdInSource is not NULL AND IdInSource <> '' ";
			rs = source.getResultSet(strQuery);
			
			rs.next();
			count = rs.getInt("n");
			if (count > 0){
				System.out.println("========================================================");
				logger.warn("There are "+ count + " RefDetails with IdInSource <> NULL ! IdInSource are not yet implemented for Berlin Model Import");
				System.out.println("========================================================");
				
			}
			
		} catch (SQLException e) {
			logger.error(e);
			e.printStackTrace();
			result = false;
		}
		return result;
	}

}
