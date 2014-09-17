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
import static eu.etaxonomy.cdm.io.common.ImportHelper.NO_OVERWRITE;
import static eu.etaxonomy.cdm.io.common.ImportHelper.OBLIGATORY;
import static eu.etaxonomy.cdm.io.common.ImportHelper.OVERWRITE;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.berlinModel.in.validation.BerlinModelReferenceImportValidator;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.mapping.CdmAttributeMapperBase;
import eu.etaxonomy.cdm.io.common.mapping.CdmIoMapping;
import eu.etaxonomy.cdm.io.common.mapping.CdmSingleAttributeMapperBase;
import eu.etaxonomy.cdm.io.common.mapping.DbImportExtensionMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportMarkerMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbSingleAttributeImportMapperBase;
import eu.etaxonomy.cdm.io.common.mapping.berlinModel.CdmOneToManyMapper;
import eu.etaxonomy.cdm.io.common.mapping.berlinModel.CdmStringMapper;
import eu.etaxonomy.cdm.io.common.mapping.berlinModel.CdmUriMapper;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.IPrintSeries;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @created 20.03.2008
 */
@Component
public class BerlinModelReferenceImport extends BerlinModelImportBase {
	private static final Logger logger = Logger.getLogger(BerlinModelReferenceImport.class);

	public static final String REFERENCE_NAMESPACE = "Reference";
	
	public static final UUID REF_DEPOSITED_AT_UUID = UUID.fromString("23ca88c7-ce73-41b2-8ca3-2cb22f013beb");
	public static final UUID REF_SOURCE_UUID = UUID.fromString("d6432582-2216-4b08-b0db-76f6c1013141");
	public static final UUID DATE_STRING_UUID = UUID.fromString("e4130eae-606e-4b0c-be4f-e93dc161be7d");
	public static final UUID IS_PAPER_UUID = UUID.fromString("8a326129-d0d0-4f9d-bbdf-8d86b037c65e");
	
	
	private int modCount = 1000;
	private static final String pluralString = "references";
	private static final String dbTableName = "reference";

	
	public BerlinModelReferenceImport(){
		super(dbTableName, pluralString);
	}
	
	protected void initializeMappers(BerlinModelImportState state){
		for (CdmAttributeMapperBase mapper: classMappers){
			if (mapper instanceof DbSingleAttributeImportMapperBase){
				DbSingleAttributeImportMapperBase singleMapper = (DbSingleAttributeImportMapperBase)mapper;
				singleMapper.initialize(state, Reference.class);
			}
		}
		return;
	}
	
	protected static CdmAttributeMapperBase[] classMappers = new CdmAttributeMapperBase[]{
		new CdmStringMapper("edition", "edition"),
		new CdmStringMapper("volume", "volume"),
		new CdmStringMapper("publisher", "publisher"),
		new CdmStringMapper("publicationTown", "placePublished"),
		new CdmStringMapper("isbn", "isbn"),
		new CdmStringMapper("isbn", "isbn"),
		new CdmStringMapper("pageString", "pages"),
		new CdmStringMapper("series", "seriesPart"),
		new CdmStringMapper("issn", "issn"),
		new CdmUriMapper("url", "uri"),
		DbImportExtensionMapper.NewInstance("NomStandard", ExtensionType.NOMENCLATURAL_STANDARD()),
		DbImportExtensionMapper.NewInstance("DateString", DATE_STRING_UUID, "Date String", "Date String", "dates"),
		DbImportExtensionMapper.NewInstance("RefDepositedAt", REF_DEPOSITED_AT_UUID, "RefDepositedAt", "reference is deposited at", "at"),
		DbImportExtensionMapper.NewInstance("RefSource", REF_SOURCE_UUID, "RefSource", "reference source", "source"),
		DbImportMarkerMapper.NewInstance("isPaper", IS_PAPER_UUID, "is paper", "is paper", "paper", false)
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
			/*"isPaper",*/ "exportDate", 
	};
	
	//TODO isPaper
	//
	
	
	
	//type to count the references nomReferences that have been created and saved
	private class RefCounter{
		RefCounter() {refCount = 0;};
		int refCount;

		public String toString(){return String.valueOf(refCount) ;};
	}

	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
		return null;  //not needed
	}

	@Override
	protected void doInvoke(BerlinModelImportState state){
		logger.info("start make " + getPluralString() + " ...");

		boolean success = true;
		initializeMappers(state);
		BerlinModelImportConfigurator config = state.getConfig();
		Source source = config.getSource();

		String strSelectId = " SELECT Reference.RefId as refId ";
		String strSelectFull = 
			" SELECT Reference.* ,InReference.RefCategoryFk as InRefCategoryFk, RefSource.RefSource " ;
		String strFrom =  " FROM %s  " + 
		    	" LEFT OUTER JOIN Reference as InReference ON InReference.refId = Reference.inRefFk " +
				" LEFT OUTER JOIN RefSource ON Reference.RefSourceFk = RefSource.RefSourceId " +
		    	" WHERE (1=1) ";
		String strWherePartitioned = " AND (Reference.refId IN ("+ ID_LIST_TOKEN + ") ) "; 
		
		String referenceTable = CdmUtils.Nz(state.getConfig().getReferenceIdTable());
		referenceTable = referenceTable.isEmpty() ? " Reference"  : referenceTable + " as Reference ";
		String strIdFrom = String.format(strFrom, referenceTable );
		
		String strSelectIdBase = strSelectId + strIdFrom;
		
		String referenceFilter = CdmUtils.Nz(state.getConfig().getReferenceIdTable());
		if (! referenceFilter.isEmpty()){
			referenceFilter = " AND " + referenceFilter + " ";
		}
		referenceFilter = "";  //don't use it for now
		
		String strIdQueryFirstPath = strSelectId + strIdFrom ;
		String strIdQuerySecondPath = strSelectId + strIdFrom + " AND (Reference.InRefFk is NOT NULL) ";
		
//		if (config.getDoReferences() == CONCEPT_REFERENCES){
//			strIdQueryNoInRef += " AND ( Reference.refId IN ( SELECT ptRefFk FROM PTaxon) ) " + referenceFilter;
//		}

		String strRecordQuery = strSelectFull + String.format(strFrom, " Reference ") + strWherePartitioned;
		
		int recordsPerTransaction = config.getRecordsPerTransaction();
		try{
			//firstPath 
			ResultSetPartitioner partitioner = ResultSetPartitioner.NewInstance(source, strIdQueryFirstPath, strRecordQuery, recordsPerTransaction);
			while (partitioner.nextPartition()){
				partitioner.doPartition(this, state);
			}
			logger.info("end make references without in-references ... " + getSuccessString(success));
			state.setReferenceSecondPath(true);

//			if (config.getDoReferences() == ALL || config.getDoReferences() == NOMENCLATURAL){

			//secondPath
			partitioner = ResultSetPartitioner.NewInstance(source, strIdQuerySecondPath, strRecordQuery, recordsPerTransaction);
			while (partitioner.nextPartition()){
				partitioner.doPartition(this, state);
			}
			logger.info("end make references with no 1 in-reference ... " + getSuccessString(success));
			state.setReferenceSecondPath(false);
			
//			}

		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			state.setUnsuccessfull();
			return;
		}
		logger.info("end make " + getPluralString() + " ... " + getSuccessString(success));
		if (! success){
			state.setUnsuccessfull();
		}
		return;
	}

	@Override
	public boolean doPartition(ResultSetPartitioner partitioner, BerlinModelImportState state) {
		if (state.isReferenceSecondPath()){
			return doPartitionSecondPath(partitioner, state);
		}
		boolean success = true;

		Map<Integer, Reference> refToSave = new HashMap<Integer, Reference>();
		
		Map<String, Reference> relatedReferences = partitioner.getObjectMap(REFERENCE_NAMESPACE);
		
		BerlinModelImportConfigurator config = state.getConfig();
		
		try {

				int i = 0;
				RefCounter refCounter  = new RefCounter();
				
				ResultSet rs = partitioner.getResultSet();

				//for each resultset
				while (rs.next()){
					if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("References handled: " + (i-1) + " in round -" );}
				
					success &= makeSingleReferenceRecord(rs, state, partitioner, refToSave, relatedReferences, refCounter);
				} // end resultSet
								
				//for the concept reference a fixed uuid may be needed -> change uuid
				Integer sourceSecId = (Integer)config.getSourceSecId();
				Reference<?> sec = refToSave.get(sourceSecId);

				if (sec != null){
					sec.setUuid(config.getSecUuid());
					logger.info("SecUuid changed to: " + config.getSecUuid());
				}
				
				//save and store in map
				logger.info("Save references (" + refCounter.refCount + ")");
				getReferenceService().saveOrUpdate(refToSave.values());

//			}//end resultSetList	

//			logger.info("end makeReferences ..." + getSuccessString(success));;
			return success;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	}



	/**
	 * Adds the inReference to the according references.
	 * @param partitioner
	 * @param state
	 * @return
	 */
	private boolean doPartitionSecondPath(ResultSetPartitioner partitioner, BerlinModelImportState state) {
		boolean success = true;

		Map<Integer, Reference> refToSave = new HashMap<Integer, Reference>();
		
		Map<String, Reference> relatedReferencesMap = partitioner.getObjectMap(REFERENCE_NAMESPACE);
		
		try {
				int i = 0;
				RefCounter refCounter  = new RefCounter();
			
				ResultSet rs = partitioner.getResultSet();
				//for each resultset
				while (rs.next()){
					if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("References handled: " + (i-1) + " in round -" );}
				
					Integer refId = rs.getInt("refId");
					Integer inRefFk = rs.getInt("inRefFk");
					
					if (inRefFk != null){
						
						Reference<?> thisRef = relatedReferencesMap.get(String.valueOf(refId));
						
						Reference<?> inRef = relatedReferencesMap.get(String.valueOf(inRefFk));
						
						if (thisRef != null){
							if (inRef == null){
								logger.warn("No InRef found for nomRef: " + thisRef.getTitleCache() + "; RefId: " +  refId + "; inRefFK: " +  inRefFk);
							}
							thisRef.setInReference(inRef);
							refToSave.put(refId, thisRef);
							thisRef.setTitleCache(null);
							thisRef.getTitleCache();
						}
					}
					
				} // end resultSet

				//save and store in map
				logger.info("Save references (" + refCounter.refCount + ")");
				getReferenceService().saveOrUpdate(refToSave.values());
				
//			}//end resultSetList	

//			logger.info("end makeReferences ..." + getSuccessString(success));;
			return success;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	}


	@Override
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs, BerlinModelImportState state) {
		String nameSpace;
		Class<?> cdmClass;
		Set<String> idSet;
		
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		
		try{
			Set<String> teamIdSet = new HashSet<String>();
			Set<String> referenceIdSet = new HashSet<String>();
			
			while (rs.next()){
				handleForeignKey(rs, teamIdSet, "NomAuthorTeamFk");
				handleForeignKey(rs, referenceIdSet, "InRefFk");
				//TODO only needed in second path but state not available here to check if state is second path
				handleForeignKey(rs, referenceIdSet, "refId");
			}
			
			//team map
			nameSpace = BerlinModelAuthorTeamImport.NAMESPACE;
			cdmClass = Team.class;
			idSet = teamIdSet;
			Map<String, Team> teamMap = (Map<String, Team>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, teamMap);

			//reference map
			nameSpace = BerlinModelReferenceImport.REFERENCE_NAMESPACE;
			cdmClass = Reference.class;
			idSet = referenceIdSet;
			Map<String, Reference> referenceMap = (Map<String, Reference>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, referenceMap);
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	
	
	/**
	 * Handles a single reference record
	 * @param rs
	 * @param state
	 * @param biblioRefToSave
	 * @param nomRefToSave
	 * @param relatedBiblioReferences
	 * @param relatedNomReferences
	 * @param refCounter
	 * @return
	 */
	private boolean makeSingleReferenceRecord(
				ResultSet rs, 
				BerlinModelImportState state,
				ResultSetPartitioner<BerlinModelImportState> partitioner,
				Map<Integer, Reference> refToSave, 
				Map<String, Reference> relatedReferences, 
				RefCounter refCounter){
		boolean success = true;

		Integer refId = null;
		try {
			Map<String, Object> valueMap = getValueMap(rs);
			
			Integer categoryFk = (Integer)valueMap.get("refCategoryFk".toLowerCase());
			refId = (Integer)valueMap.get("refId".toLowerCase());
			Boolean thesisFlag = (Boolean)valueMap.get("thesisFlag".toLowerCase());
			
			
			Reference<?> referenceBase;
			logger.debug("RefCategoryFk: " + categoryFk);
			
			if (thesisFlag){
				referenceBase = makeThesis(valueMap);
			}else if (categoryFk == REF_JOURNAL){
				referenceBase = makeJournal(valueMap);
			}else if(categoryFk == REF_BOOK){
				referenceBase = makeBook(valueMap, refToSave, relatedReferences);
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
				referenceBase = makeArticle(valueMap, refToSave, relatedReferences);
			}else if(categoryFk == REF_JOURNAL_VOLUME){
				referenceBase = makeJournalVolume(valueMap);
			}else if(categoryFk == REF_PART_OF_OTHER_TITLE){
				referenceBase = makePartOfOtherTitle(valueMap, refToSave, relatedReferences);
			}else{
				logger.warn("Unknown categoryFk (" + categoryFk + "). Create 'Generic instead'");
				referenceBase = ReferenceFactory.newGeneric();
				success = false;
			}
							
			//refYear
			String refYear = (String)valueMap.get("refYear".toLowerCase());
			referenceBase.setDatePublished(ImportHelper.getDatePublished(refYear)); 
			
			//created, updated, notes
			doCreatedUpdatedNotes(state, referenceBase, rs);						
			
			//idInSource
			String idInSource = (String)valueMap.get("IdInSource".toLowerCase());
			if (isNotBlank(idInSource)){
				IdentifiableSource source = IdentifiableSource.NewDataImportInstance(idInSource);
				source.setIdNamespace("import to Berlin Model");
				referenceBase.addSource(source);
			}
			
			//nom&BiblioReference  - must be last because a clone is created
			success &= makeNomAndBiblioReference(rs, state, partitioner, refId, referenceBase, refCounter, refToSave);


		} catch (Exception e) {
			logger.warn("Reference with BM refId '" + CdmUtils.Nz(refId) +  "' threw Exception and could not be saved");
			e.printStackTrace();
			success = false;
		}
		return success;
	}

	
	/**
	 * Creates and saves a nom. reference and a biblio. reference after checking necessity
	 * @param rs
	 * @param refId
	 * @param ref
	 * @param refCounter
	 * @param biblioRefToSave
	 * @param nomRefToSave
	 * @param teamMap
	 * @param stores
	 * @return
	 * @throws SQLException
	 */
	private boolean makeNomAndBiblioReference(
				ResultSet rs, 
				BerlinModelImportState state,
				ResultSetPartitioner partitioner,
				int refId, 
				Reference<?> ref,  
				RefCounter refCounter, 
				Map<Integer, Reference> refToSave
				) throws SQLException{
		
		Map<String, Team> teamMap = partitioner.getObjectMap(BerlinModelAuthorTeamImport.NAMESPACE);
		
		String refCache = rs.getString("refCache");
		String nomRefCache = rs.getString("nomRefCache");
		String title = rs.getString("title");
		String nomTitleAbbrev = rs.getString("nomTitleAbbrev");
		boolean isPreliminary = rs.getBoolean("PreliminaryFlag");
		String refAuthorString = rs.getString("refAuthorString");
		Integer nomAuthorTeamFk = rs.getInt("NomAuthorTeamFk");
		String strNomAuthorTeamFk = String.valueOf(nomAuthorTeamFk);
		TeamOrPersonBase<?> nomAuthor = teamMap.get(strNomAuthorTeamFk);

		Reference<?> sourceReference = state.getTransactionalSourceReference();
		
		//preliminary
		if (isPreliminary){
			ref.setAbbrevTitleCache(nomRefCache, true);
			ref.setTitleCache(refCache, true);
		}

		//title/abbrevTitle
		if (isNotBlank(nomTitleAbbrev)){
			ref.setAbbrevTitle(nomTitleAbbrev);
		}
		if (isNotBlank(title)){
			ref.setTitle(title);
		}

		//author
		TeamOrPersonBase<?> author = getAuthorTeam(refAuthorString , nomAuthor);
		ref.setAuthorTeam(author);
		
		//save
		if (! refToSave.containsKey(refId)){
			refToSave.put(refId, ref);
		}else{
			logger.warn("Duplicate refId in Berlin Model database. Second reference was not imported !!");
		}
		refCounter.refCount++;
		
		//refId
		ImportHelper.setOriginalSource(ref, sourceReference, refId, REFERENCE_NAMESPACE);
		
		return true;
	}
	
	/**
	 * Copies the created and updated information from the nomReference to the cloned bibliographic reference
	 * @param referenceBase
	 * @param nomReference
	 */
	private void copyCreatedUpdated(Reference<?> biblioReference, Reference nomReference) {
		biblioReference.setCreatedBy(nomReference.getCreatedBy());
		biblioReference.setCreated(nomReference.getCreated());
		biblioReference.setUpdatedBy(nomReference.getUpdatedBy());
		biblioReference.setUpdated(nomReference.getUpdated());
		
	}

	private Reference<?> makeArticle (Map<String, Object> valueMap, Map<Integer, Reference> refToSave, Map<String, Reference> relatedReferences){
		
		IArticle article = ReferenceFactory.newArticle();
		Object inRefFk = valueMap.get("inRefFk".toLowerCase());
		Integer inRefCategoryFk = (Integer)valueMap.get("inRefCategoryFk".toLowerCase());
		Integer refId = (Integer)valueMap.get("refId".toLowerCase());
		
		if (inRefFk != null){
			if (inRefCategoryFk != REF_JOURNAL){
				logger.warn("Wrong inrefCategory for Article (refID = " + refId +"). Type must be 'Journal' but was not (RefCategoryFk=" + inRefCategoryFk + "))." +
					" InReference was added anyway! ");
			}
		}else{
			logger.warn ("Article has no inreference: " + refId);
		}
		makeStandardMapper(valueMap, (Reference)article); //url, pages, series, volume
		return (Reference<?>)article;
	}
	
	private Reference<?> makePartOfOtherTitle (Map<String, Object> valueMap, 
			Map<Integer, Reference> refToSave, Map<String, Reference> relatedReferences){
		
		Reference<?> result;
		Object inRefFk = valueMap.get("inRefFk".toLowerCase());
		Integer inRefCategoryFk = (Integer)valueMap.get("inRefCategoryFk".toLowerCase());
		Integer refId = (Integer)valueMap.get("refId".toLowerCase());
		
		if (inRefCategoryFk == null){
			//null -> error
			logger.warn("Part-Of-Other-Title has no inRefCategoryFk! RefId = " + refId + ". ReferenceType set to Generic.");
			result = makeUnknown(valueMap);
		}else if (inRefFk == null){
			//TODO is this correct ??
			logger.warn("Part-Of-Other-Title has no in reference: " + refId);
			result = makeUnknown(valueMap);
		}else if (inRefCategoryFk == REF_BOOK){
			//BookSection
			IBookSection bookSection = ReferenceFactory.newBookSection();
			result = (Reference<?>)bookSection;
		}else if (inRefCategoryFk == REF_ARTICLE){
			//Article
			logger.info("Reference (refId = " + refId + ") of type 'part_of_other_title' is part of 'article'." +
					" We use the section reference type for such in references now.") ;
			result = ReferenceFactory.newSection();
		}else if (inRefCategoryFk == REF_JOURNAL){
			//TODO 
			logger.warn("Reference (refId = " + refId + ") of type 'part_of_other_title' has inReference of type 'journal'." +
					" This is not allowed! Generic reference created instead") ;
			result = ReferenceFactory.newGeneric();
			result.addMarker(Marker.NewInstance(MarkerType.TO_BE_CHECKED(), true));
		}else if (inRefCategoryFk == REF_PART_OF_OTHER_TITLE){
			logger.info("Reference (refId = " + refId + ") of type 'part_of_other_title' has inReference 'part of other title'." +
					" This is allowed, but may be true only for specific cases (e.g. parts of book chapters). You may want to check if this is correct") ;
			result = ReferenceFactory.newSection();
		}else{
			logger.warn("InReference type (catFk = " + inRefCategoryFk + ") of part-of-reference not recognized for refId " + refId + "." +
				" Create 'Generic' reference instead");
			result = ReferenceFactory.newGeneric();
		}
		makeStandardMapper(valueMap, result); //url, pages
		return result;
	}
	

	/**
	 * @param inRefFkInt
	 * @param biblioRefToSave
	 * @param nomRefToSave
	 * @param relatedBiblioReferences
	 * @param relatedNomReferences
	 * @return
	 */
	private boolean existsInMapOrToSave(Integer inRefFkInt, Map<Integer, Reference> refToSave, Map<String, Reference> relatedReferences) {
		boolean result = false;
		if (inRefFkInt == null){
			return false;
		}
		result |= refToSave.containsKey(inRefFkInt);
		result |= relatedReferences.containsKey(String.valueOf(inRefFkInt));
		return result;
	}

	private Reference<?> makeWebSite(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Website'");}
		Reference<?> webPage = ReferenceFactory.newWebPage();
		makeStandardMapper(valueMap, webPage); //placePublished, publisher
		return webPage;
	}
	
	private Reference<?> makeUnknown(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Unknown'");}
		Reference<?> generic = ReferenceFactory.newGeneric();
//		generic.setSeries(series);
		makeStandardMapper(valueMap, generic); //pages, placePublished, publisher, series, volume
		return generic;
	}

	private Reference<?> makeInformal(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Informal'");}
		Reference<?> generic = ReferenceFactory.newGeneric();
//		informal.setSeries(series);
		makeStandardMapper(valueMap, generic);//editor, pages, placePublished, publisher, series, volume
		String informal = (String)valueMap.get("InformalRefCategory".toLowerCase());
		if (isNotBlank(informal) ){
			generic.addExtension(informal, ExtensionType.INFORMAL_CATEGORY());
		}
		return generic;
	}
	
	private Reference<?> makeDatabase(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Database'");}
		Reference<?> database =  ReferenceFactory.newDatabase();
		makeStandardMapper(valueMap, database); //?
		return database;
	}
	
	private Reference<?> makeJournal(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Journal'");}
		Reference<?> journal = ReferenceFactory.newJournal();
		
		Set<String> omitAttributes = new HashSet<String>();
		String series = "series";
//		omitAttributes.add(series);
		
		makeStandardMapper(valueMap, journal, omitAttributes); //issn,placePublished,publisher
//		if (valueMap.get(series) != null){
//			logger.warn("Series not yet implemented for journal!");
//		}
		return journal;
	}
	
	private Reference<?> makeBook(
				Map<String, Object> valueMap, 
				Map<Integer, Reference> refToSave, 
				Map<String, Reference> relatedReferences){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Book'");}
		Reference<?> book = ReferenceFactory.newBook();
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
			printSeries = ReferenceFactory.newPrintSeries(series);
			logger.info("Implementation of printSeries is preliminary");
		}
		//Series (as Reference)
		if (book.getInSeries() != null && printSeries != null){
			logger.warn("Book has series string and inSeries reference. Can not take both. Series string neglected");
		}else{
			book.setInSeries(printSeries);
		}
		book.setEditor(null);
		return book;
		
	}
	
	/**
	 * Returns the requested object if it exists in one of both maps. Prefers the refToSaveMap in ambigious cases.
	 * @param inRefFkInt
	 * @param nomRefToSave
	 * @param relatedNomReferences
	 * @return
	 */
	private Reference<?> getReferenceFromMaps(
			int inRefFkInt,
			Map<Integer, Reference> refToSaveMap,
			Map<String, Reference> relatedRefMap) {
		Reference<?> result = null;
		result = refToSaveMap.get(inRefFkInt);
		if (result == null){
			result = relatedRefMap.get(String.valueOf(inRefFkInt));
		}
		return result;
	}

	private Reference<?> makePrintSeries(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'PrintSeries'");}
		Reference<?> printSeries = ReferenceFactory.newPrintSeries();
		makeStandardMapper(valueMap, printSeries, null);
		return printSeries;
	}
	
	private Reference<?> makeProceedings(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Proceedings'");}
		Reference<?> proceedings = ReferenceFactory.newProceedings();
		makeStandardMapper(valueMap, proceedings, null);	
		return proceedings;
	}

	private Reference<?> makeThesis(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Thesis'");}
		Reference<?> thesis = ReferenceFactory.newThesis();
		makeStandardMapper(valueMap, thesis, null);	
		return thesis;
	}

	
	private Reference<?> makeJournalVolume(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'JournalVolume'");}
		//Proceedings proceedings = Proceedings.NewInstance();
		Reference<?> journalVolume = ReferenceFactory.newGeneric();
		makeStandardMapper(valueMap, journalVolume, null);	
		logger.warn("Journal volumes not yet implemented. Generic created instead but with errors");
		return journalVolume;
	}
	
	private boolean makeStandardMapper(Map<String, Object> valueMap, Reference<?> ref){
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
		if (mapper instanceof DbImportExtensionMapper){
			result &= ((DbImportExtensionMapper)mapper).invoke(valueMap, cdmBase);
		}else if (mapper instanceof DbImportMarkerMapper){
			result &= ((DbImportMarkerMapper)mapper).invoke(valueMap, cdmBase);
		}else{
			String sourceAttribute = mapper.getSourceAttributeList().get(0).toLowerCase();
			Object value = valueMap.get(sourceAttribute);
			if (mapper instanceof CdmUriMapper && value != null){
				try {
					value = new URI (value.toString());
				} catch (URISyntaxException e) {
					logger.error("URI syntax exception: " + value.toString());
					value = null;
				}
			}
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
		return result;
	}

	
	private static TeamOrPersonBase<?> getAuthorTeam(String authorString, TeamOrPersonBase<?> nomAuthor){
		TeamOrPersonBase<?> result;
		if (nomAuthor != null){
			result = nomAuthor;
		} else if (StringUtils.isNotBlank(authorString)){
			//FIXME check for existing team / persons
			TeamOrPersonBase<?> team = Team.NewInstance();
			team.setNomenclaturalTitle(authorString);
			team.setTitleCache(authorString, true);
			team.setNomenclaturalTitle(authorString);
			result = team;
		}else{
			result = null;
		}
		
		return result;
	}
	
	
	/**
	 * @param lowerCase
	 * @param config
	 * @return
	 */
	public Set<String> getObligatoryAttributes(boolean lowerCase, BerlinModelImportConfigurator config){
		Set<String> result = new HashSet<String>();
		Class<ICdmIO>[] ioClassList = config.getIoClassList();
		logger.warn("getObligatoryAttributes has been commented because it still needs to be adapted to the new package structure");
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

	@Override
	protected boolean doCheck(BerlinModelImportState state){
		BerlinModelReferenceImportValidator validator = new BerlinModelReferenceImportValidator();
		return validator.validate(state, this);
	}
	
	@Override
	protected boolean isIgnore(BerlinModelImportState state){
		return (state.getConfig().getDoReferences() == IImportConfigurator.DO_REFERENCES.NONE);
	}

	


}
