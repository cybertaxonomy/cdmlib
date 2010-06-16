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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.berlinModel.CdmOneToManyMapper;
import eu.etaxonomy.cdm.io.berlinModel.CdmStringMapper;
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
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.IJournal;
import eu.etaxonomy.cdm.model.reference.IPrintSeries;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.reference.ReferenceType;

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelReferenceImport extends BerlinModelImportBase {
	private static final Logger logger = Logger.getLogger(BerlinModelReferenceImport.class);

	public static final String NOM_REFERENCE_NAMESPACE = "NomReference";
	public static final String BIBLIO_REFERENCE_NAMESPACE = "BiblioReference";
	
	public static final UUID REF_DEPOSITED_AT_UUID = UUID.fromString("23ca88c7-ce73-41b2-8ca3-2cb22f013beb");
	public static final UUID REF_SOURCE = UUID.fromString("d6432582-2216-4b08-b0db-76f6c1013141");
	public static final UUID DATE_STRING_UUID = UUID.fromString("e4130eae-606e-4b0c-be4f-e93dc161be7d");
	
	
	private int modCount = 1000;
	private static final String pluralString = "references";
	private static final String dbTableName = "reference";

	
	public BerlinModelReferenceImport(){
		super();
	}
	
	protected boolean initializeMappers(BerlinModelImportState state){
		for (CdmAttributeMapperBase mapper: classMappers){
			if (mapper instanceof DbImportExtensionMapper){
				((DbImportExtensionMapper)mapper).initialize(state, ReferenceBase.class);
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
		DbImportExtensionMapper.NewInstance("NomStandard", ExtensionType.NOMENCLATURAL_STANDARD()),
		DbImportExtensionMapper.NewInstance("DateString", DATE_STRING_UUID, "Date String", "Date String", "dates"),
		DbImportExtensionMapper.NewInstance("RefDepositedAt", REF_DEPOSITED_AT_UUID, "RefDepositedAt", "reference is deposited at", "at"),
		DbImportExtensionMapper.NewInstance("RefSource", REF_SOURCE, "RefSource", "reference source", "source")
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
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
		return null;  //not needed
	}


	@Override
	protected boolean doInvoke(BerlinModelImportState state){
		boolean success = true;
		logger.info("start make " + getPluralString() + " ...");

		success &= initializeMappers(state);
		BerlinModelImportConfigurator config = state.getConfig();
		Source source = config.getSource();

		String strSelectId = "Select Reference.RefId as refId";
		String strSelectFull = 
			" SELECT Reference.* , InReference.RefId as InRefId, InReference.RefCategoryFk as InRefCategoryFk,  " +
			" InInReference.RefId as InInRefId, InInReference.RefCategoryFk as InInRefCategoryFk, " +
			" InReference.InRefFk AS InRefInRefFk, InInReference.InRefFk AS InInRefInRefFk, RefSource.RefSource " ;
		String strFrom =  " FROM Reference AS InInReference " +
		    	" RIGHT OUTER JOIN Reference AS InReference ON InInReference.RefId = InReference.InRefFk " + 
		    	" RIGHT OUTER JOIN Reference ON InReference.RefId = dbo.Reference.InRefFk " + 
		    	" LEFT OUTER JOIN RefSource ON Reference.RefSourceFk = RefSource.RefSourceId " +
		    	" WHERE (1=1) ";
		String strWherePartitioned = " AND (Reference.refId IN ("+ ID_LIST_TOKEN + ") ) "; 

		//test max number of recursions
		String strQueryTestMaxRecursion = strSelectId + strFrom +  
			" AND (Reference.InRefFk is NOT NULL) AND (InReference.InRefFk is NOT NULL) AND (InInReference.InRefFk is NOT NULL) ";
		ResultSet testMaxRecursionResultSet = source.getResultSet(strQueryTestMaxRecursion);
		try {
			if (testMaxRecursionResultSet.next() == true){
				logger.error("Maximum allowed InReference recursions exceeded in Berlin Model. Maximum recursion level is 2.");
				return false;
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			logger.error("There are references with more then 2 in-reference recursion. Maximum number of allowed recursions is 2. Records will not be stored.");
			success = false;
		}

		String strSelectIdBase = strSelectId + strFrom;
		
		String strIdQueryNoInRef = strSelectIdBase + 
			" AND (Reference.InRefFk is NULL) ";
		String strIdQuery1InRef = strSelectIdBase + 
			" AND (Reference.InRefFk is NOT NULL) AND (InReference.InRefFk is NULL) ";
		String strIdQuery2InRefs = strSelectIdBase + 
			" AND (Reference.InRefFk is NOT NULL) AND (InReference.InRefFk is NOT NULL) AND (InInReference.InRefFk is NULL) ";

		if (config.getDoReferences() == CONCEPT_REFERENCES){
			strIdQueryNoInRef += " AND ( Reference.refId IN ( SELECT ptRefFk FROM PTaxon) ) ";
		}

		String strRecordQuery = strSelectFull + strFrom + strWherePartitioned;
		
		int recordsPerTransaction = config.getRecordsPerTransaction();
		try{
			//NoInRefs
			ResultSetPartitioner partitioner = ResultSetPartitioner.NewInstance(source, strIdQueryNoInRef, strRecordQuery, recordsPerTransaction);
			while (partitioner.nextPartition()){
				partitioner.doPartition(this, state);
			}
			logger.info("end make references with no in-references ... " + getSuccessString(success));

			if (config.getDoReferences() == ALL || config.getDoReferences() == NOMENCLATURAL){

				//1InRef
				partitioner = ResultSetPartitioner.NewInstance(source, strIdQuery1InRef, strRecordQuery, recordsPerTransaction);
				while (partitioner.nextPartition()){
					partitioner.doPartition(this, state);
				}
				logger.info("end make references with no 1 in-reference ... " + getSuccessString(success));
	
				//2InRefs
				partitioner = ResultSetPartitioner.NewInstance(source, strIdQuery2InRefs, strRecordQuery, recordsPerTransaction);
				while (partitioner.nextPartition()){
					partitioner.doPartition(this, state);
				}
				logger.info("end make references with no 2 in-reference ... " + getSuccessString(success));
			}

		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
		logger.info("end make " + getPluralString() + " ... " + getSuccessString(success));
		return success;
	}

	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#doPartition(eu.etaxonomy.cdm.io.berlinModel.in.ResultSetPartitioner, eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState)
	 */
	public boolean doPartition(ResultSetPartitioner partitioner, BerlinModelImportState state) {
		boolean success = true;
//		MapWrapper<ReferenceBase> referenceStore= new MapWrapper<ReferenceBase>(null);
//		MapWrapper<ReferenceBase> nomRefStore= new MapWrapper<ReferenceBase>(null);

		Map<Integer, ReferenceBase> nomRefToSave = new HashMap<Integer, ReferenceBase>();
		Map<Integer, ReferenceBase> biblioRefToSave = new HashMap<Integer, ReferenceBase>();
		
		Map<String, ReferenceBase> relatedNomReferences = partitioner.getObjectMap(NOM_REFERENCE_NAMESPACE);
		Map<String, ReferenceBase> relatedBiblioReferences = partitioner.getObjectMap(BIBLIO_REFERENCE_NAMESPACE);
		
		BerlinModelImportConfigurator config = state.getConfig();
		
		try {
//			//get data from database
//				//strQueryBase += " AND Reference.refId = 1933 " ; //7000000
//			
//			int j = 0;
//			Iterator<ResultSet> resultSetListIterator =  resultSetList.listIterator();
//			//for each resultsetlist
//			while (resultSetListIterator.hasNext()){
				int i = 0;
				RefCounter refCounter  = new RefCounter();
				
//				ResultSet rs = resultSetListIterator.next();
				ResultSet rs = partitioner.getResultSet();
				//for each resultset
				while (rs.next()){
					if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("References handled: " + (i-1) + " in round -" );}
				
					success &= makeSingleReferenceRecord(rs, state, partitioner, biblioRefToSave, nomRefToSave, relatedBiblioReferences, relatedNomReferences, refCounter);
				} // end resultSet
								
				//for the concept reference a fixed uuid may be needed -> change uuid
				Integer sourceSecId = (Integer)config.getSourceSecId();
				ReferenceBase<?> sec = biblioRefToSave.get(sourceSecId);
				if (sec == null){
					sec = nomRefToSave.get(sourceSecId);	
				}
				if (sec != null){
					sec.setUuid(config.getSecUuid());
					logger.info("SecUuid changed to: " + config.getSecUuid());
				}
				
				//save and store in map
				logger.info("Save nomenclatural references (" + refCounter.nomRefCount + ")");
				getReferenceService().save(nomRefToSave.values());
				logger.info("Save bibliographical references (" + refCounter.referenceCount +")");
				getReferenceService().save(biblioRefToSave.values());
//				j++;
//			}//end resultSetList	

			logger.info("end makeReferences ..." + getSuccessString(success));;
			return success;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	}



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getRelatedObjectsForPartition(java.sql.ResultSet)
	 */
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs) {
		String nameSpace;
		Class cdmClass;
		Set<String> idSet;
		
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		
		try{
			Set<String> teamIdSet = new HashSet<String>();
			Set<String> referenceIdSet = new HashSet<String>();
			
			while (rs.next()){
				handleForeignKey(rs, teamIdSet, "NomAuthorTeamFk");
				handleForeignKey(rs, referenceIdSet, "InRefFk");
			}
			
			//team map
			nameSpace = BerlinModelAuthorTeamImport.NAMESPACE;
			cdmClass = Team.class;
			idSet = teamIdSet;
			Map<String, Team> teamMap = (Map<String, Team>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, teamMap);

			
			//nom reference map
			nameSpace = NOM_REFERENCE_NAMESPACE;
			cdmClass = ReferenceBase.class;
			idSet = referenceIdSet;
			Map<String, ReferenceBase> nomRefMap = (Map<String, ReferenceBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, nomRefMap);

			//biblio reference map
			nameSpace = BIBLIO_REFERENCE_NAMESPACE;
			cdmClass = ReferenceBase.class;
			idSet = referenceIdSet;
			Map<String, ReferenceBase> biblioRefMap = (Map<String, ReferenceBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, biblioRefMap);
			
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
				Map<Integer, ReferenceBase> biblioRefToSave, 
				Map<Integer, ReferenceBase> nomRefToSave, 
				Map<String, ReferenceBase> relatedBiblioReferences, 
				Map<String, ReferenceBase> relatedNomReferences, 
				RefCounter refCounter){
		boolean success = true;

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
				referenceBase = makeBook(valueMap, biblioRefToSave, nomRefToSave, relatedBiblioReferences, relatedNomReferences);
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
				referenceBase = makeArticle(valueMap, biblioRefToSave, nomRefToSave, relatedBiblioReferences, relatedNomReferences);
			}else if(categoryFk == REF_JOURNAL_VOLUME){
				referenceBase = makeJournalVolume(valueMap);
			}else if(categoryFk == REF_PART_OF_OTHER_TITLE){
				referenceBase = makePartOfOtherTitle(valueMap, biblioRefToSave, nomRefToSave, relatedBiblioReferences, relatedNomReferences);
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

			//isPaper
			if ((Boolean)valueMap.get("isPaper".toLowerCase())){
				logger.warn("IsPaper is not yet implemented, but reference " +  refId + " is paper");
			}
			
			//idInSource
			String idInSource = (String)valueMap.get("IdInSource".toLowerCase());
			if (CdmUtils.isNotEmpty(idInSource)){
				IdentifiableSource source = IdentifiableSource.NewInstance(idInSource);
				source.setIdNamespace("import to Berlin Model");
				referenceBase.addSource(source);
			}
			
			//nom&BiblioReference  - must be last because a clone is created
			success &= makeNomAndBiblioReference(rs, state, partitioner, refId, referenceBase, refCounter, 
					biblioRefToSave, nomRefToSave );


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
	 * @param referenceBase
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
				ReferenceBase<?> referenceBase,  
				RefCounter refCounter, 
				Map<Integer, ReferenceBase> biblioRefToSave, 
				Map<Integer, ReferenceBase> nomRefToSave
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
		ReferenceBase nomReference = null;
		
		boolean hasNomRef = false;
		boolean hasBiblioRef = false;
		ReferenceBase sourceReference = state.getConfig().getSourceReference();
		
		//is Nomenclatural Reference
		if ( (CdmUtils.isNotEmpty(nomRefCache) && isPreliminary) || (CdmUtils.isNotEmpty(nomTitleAbbrev) && ! isPreliminary) ){
			referenceBase.setTitle(nomTitleAbbrev);
			TeamOrPersonBase<?> author = getAuthorTeam(refAuthorString , nomAuthor, true);
			referenceBase.setAuthorTeam(author);
			//referenceBase.setNomenclaturallyRelevant(true);
			if (isPreliminary){
				referenceBase.setTitleCache(nomRefCache, true);
			}
			if (! nomRefToSave.containsKey(refId)){
				if (referenceBase == null){
					logger.warn("refBase is null");
				}
				nomRefToSave.put(refId, referenceBase);
			}else{
				logger.warn("Duplicate refId in Berlin Model database. Second reference was not imported !!");
			}
			
//			???
//			nomRefToSave.put(refId, referenceBase);
			hasNomRef = true;
			nomReference = referenceBase;
			refCounter.nomRefCount++;
		}
		//is bibliographical Reference
		if ((CdmUtils.isNotEmpty(refCache) && isPreliminary && ! refCache.equalsIgnoreCase(nomRefCache)) 
				|| (CdmUtils.isNotEmpty(title) && ! isPreliminary && ! title.equalsIgnoreCase(nomTitleAbbrev)) 
				|| hasNomRef == false){
			if (hasNomRef){
				referenceBase = (ReferenceBase)referenceBase.clone();
				copyCreatedUpdated(referenceBase, nomReference);
			}
			referenceBase.setTitle(title);
			TeamOrPersonBase author = getAuthorTeam(refAuthorString , nomAuthor, false);
			referenceBase.setAuthorTeam(author);
			referenceBase.setNomenclaturallyRelevant(false);
			if (isPreliminary){
				referenceBase.setTitleCache(refCache, true);
			}
			if (! biblioRefToSave.containsKey(refId)){
				biblioRefToSave.put(refId, referenceBase);
			}else{
				logger.warn("Duplicate refId in Berlin Model database. Second reference was not imported !!");
			}
			hasBiblioRef = true;
			
			//??
			//biblioRefToSave.put(refId, referenceBase);
			refCounter.referenceCount++;
		}
		//refId
		if (hasNomRef){
			ImportHelper.setOriginalSource(nomReference, sourceReference, refId, NOM_REFERENCE_NAMESPACE);
		}
		if (hasBiblioRef){
			ImportHelper.setOriginalSource(referenceBase, sourceReference, refId, BIBLIO_REFERENCE_NAMESPACE);
		}
		
		return true;
		
	}
	
	/**
	 * Copies the created and updated information from the nomReference to the cloned bibliographic reference
	 * @param referenceBase
	 * @param nomReference
	 */
	private void copyCreatedUpdated(ReferenceBase<?> biblioReference, ReferenceBase nomReference) {
		biblioReference.setCreatedBy(nomReference.getCreatedBy());
		biblioReference.setCreated(nomReference.getCreated());
		biblioReference.setUpdatedBy(nomReference.getUpdatedBy());
		biblioReference.setUpdated(nomReference.getUpdated());
		
	}

	private ReferenceBase<?> makeArticle (Map<String, Object> valueMap, Map<Integer, ReferenceBase> biblioRefToSave, Map<Integer, ReferenceBase> nomRefToSave, Map<String, ReferenceBase> relatedBiblioReferences, Map<String, ReferenceBase> relatedNomReferences){
		
		IArticle article = ReferenceFactory.newArticle();
		Object inRefFk = valueMap.get("inRefFk".toLowerCase());
		Integer inRefCategoryFk = (Integer)valueMap.get("inRefCategoryFk".toLowerCase());
		Integer refId = (Integer)valueMap.get("refId".toLowerCase());
		
		if (inRefFk != null){
			if (inRefCategoryFk == REF_JOURNAL){
				int inRefFkInt = (Integer)inRefFk;
				if (existsInMapOrToSave(inRefFkInt, biblioRefToSave, nomRefToSave, relatedBiblioReferences, relatedNomReferences)){
					ReferenceBase<?> inJournal = getReferenceFromMaps(inRefFkInt, nomRefToSave, relatedNomReferences);
					if (inJournal == null){
						inJournal = getReferenceFromMaps(inRefFkInt, biblioRefToSave, relatedBiblioReferences);
						logger.info("inJournal (" + inRefFkInt + ") found in referenceStore instead of nomRefStore.");
						nomRefToSave.put(inRefFkInt, inJournal);
					}
					if (inJournal == null){
						logger.warn("inJournal for " + inRefFkInt + " is null. "+
							" InReference relation could not be set");
					//}else if (ReferenceBase.class.isAssignableFrom(inJournal.getClass())){
					}else if (inJournal.getType().equals(ReferenceType.Journal)){
						article.setInJournal((IJournal)inJournal);
					}else{
						logger.warn("InJournal is not of type journal but of type " + inJournal.getType() +
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
	
	private ReferenceBase<?> makePartOfOtherTitle (Map<String, Object> valueMap, Map<Integer, ReferenceBase> biblioRefToSave, Map<Integer, ReferenceBase> nomRefToSave, Map<String, ReferenceBase> relatedBiblioReferences, Map<String, ReferenceBase> relatedNomReferences){
		ReferenceBase<?> result;
		Object inRefFk = valueMap.get("inRefFk".toLowerCase());
		Integer inRefCategoryFk = (Integer)valueMap.get("inRefCategoryFk".toLowerCase());
		Integer refId = (Integer)valueMap.get("refId".toLowerCase());
		
		if (inRefCategoryFk == null){
			//null -> error
			logger.warn("Part-Of-Other-Title has not inRefCategoryFk! RefId = " + refId + ". ReferenceType set to Generic.");
			result = makeUnknown(valueMap);
		}else if (inRefCategoryFk == REF_BOOK){
			//BookSection
			IBookSection bookSection = ReferenceFactory.newBookSection();
			result = (ReferenceBase)bookSection;
			if (inRefFk != null){
				int inRefFkInt = (Integer)inRefFk;
				if (existsInMapOrToSave(inRefFkInt, biblioRefToSave, nomRefToSave, relatedBiblioReferences, relatedNomReferences)){
					ReferenceBase<?> inBook = getReferenceFromMaps(inRefFkInt, nomRefToSave, relatedNomReferences);
					if (inBook == null){
						inBook = getReferenceFromMaps(inRefFkInt, biblioRefToSave, relatedBiblioReferences);
						logger.info("inBook (" + inRefFkInt + ") found in referenceStore instead of nomRefStore.");
						nomRefToSave.put(inRefFkInt, inBook);
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
			result = ReferenceFactory.newGeneric();
		}else if (inRefCategoryFk == REF_JOURNAL){
			//TODO 
			logger.warn("Reference (refId = " + refId + ") of type 'part_of_other_title' has inReference of type 'journal'." +
					" This is not allowed! Generic reference created instead") ;
			result = ReferenceFactory.newGeneric();
			result.addMarker(Marker.NewInstance(MarkerType.TO_BE_CHECKED(), true));
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
	private boolean existsInMapOrToSave(Integer inRefFkInt, Map<Integer, ReferenceBase> biblioRefToSave, Map<Integer, ReferenceBase> nomRefToSave, Map<String, ReferenceBase> relatedBiblioReferences, Map<String, ReferenceBase> relatedNomReferences) {
		boolean result = false;
		if (inRefFkInt == null){
			return false;
		}
		result |= nomRefToSave.containsKey(inRefFkInt);
		result |= biblioRefToSave.containsKey(inRefFkInt);
		result |= relatedBiblioReferences.containsKey(String.valueOf(inRefFkInt));
		result |= relatedNomReferences.containsKey(String.valueOf(inRefFkInt));
		return result;
	}

	private ReferenceBase<?> makeWebSite(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Website'");}
		ReferenceBase webPage = ReferenceFactory.newWebPage();
		makeStandardMapper(valueMap, webPage); //placePublished, publisher
		return webPage;
	}
	
	private ReferenceBase<?> makeUnknown(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Unknown'");}
		ReferenceBase generic = ReferenceFactory.newGeneric();
//		generic.setSeries(series);
		makeStandardMapper(valueMap, generic); //pages, placePublished, publisher, series, volume
		return generic;
	}

	private ReferenceBase<?> makeInformal(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Informal'");}
		ReferenceBase generic = ReferenceFactory.newGeneric();
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
		ReferenceBase database =  ReferenceFactory.newDatabase();
		makeStandardMapper(valueMap, database); //?
		return database;
	}
	
	private ReferenceBase<?> makeJournal(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Journal'");}
		ReferenceBase journal = ReferenceFactory.newJournal();
		
		Set<String> omitAttributes = new HashSet<String>();
		String series = "series";
//		omitAttributes.add(series);
		
		makeStandardMapper(valueMap, journal, omitAttributes); //issn,placePublished,publisher
//		if (valueMap.get(series) != null){
//			logger.warn("Series not yet implemented for journal!");
//		}
		return journal;
	}
	
	private ReferenceBase<?> makeBook(
				Map<String, Object> valueMap, 
				Map<Integer, ReferenceBase> biblioRefToSave, 
				Map<Integer, ReferenceBase> nomRefToSave, 
				Map<String, ReferenceBase> relatedBiblioReferences, 
				Map<String, ReferenceBase> relatedNomReferences){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Book'");}
		ReferenceBase book = ReferenceFactory.newBook();
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
		Object inRefFk = valueMap.get("inRefFk".toLowerCase());
		//Series (as Reference)
		if (inRefFk != null){
			int inRefFkInt = (Integer)inRefFk;
			if (existsInMapOrToSave(inRefFkInt, biblioRefToSave, nomRefToSave, relatedBiblioReferences, relatedNomReferences)){
				ReferenceBase<?> inSeries = getReferenceFromMaps(inRefFkInt, nomRefToSave, relatedNomReferences);
				if (inSeries == null){
					inSeries = getReferenceFromMaps(inRefFkInt, biblioRefToSave, relatedBiblioReferences);
					logger.info("inSeries (" + inRefFkInt + ") found in referenceStore instead of nomRefStore.");
					nomRefToSave.put(inRefFkInt, inSeries);
				}
				if (inSeries == null){
					logger.warn("inSeries for " + inRefFkInt + " is null. "+
					" InReference relation could not be set");;
				//}else if (PrintSeries.class.isAssignableFrom(inSeries.getClass())){
				}else if (inSeries.getType().equals(ReferenceType.PrintSeries)){
					book.setInSeries((IPrintSeries)inSeries);
					//TODO
				}else{
					logger.warn("inSeries is not of type PrintSeries but of type " + inSeries.getType().getMessage() +
							". In-reference relation could not be set for refId " + refId + " and inRefFk " + inRefFk);
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
	
	/**
	 * Returns the requested object if it exists in one of both maps. Prefers the refToSaveMap in ambigious cases.
	 * @param inRefFkInt
	 * @param nomRefToSave
	 * @param relatedNomReferences
	 * @return
	 */
	private ReferenceBase<?> getReferenceFromMaps(
			int inRefFkInt,
			Map<Integer, ReferenceBase> refToSaveMap,
			Map<String, ReferenceBase> relatedRefMap) {
		ReferenceBase result = null;
		result = refToSaveMap.get(inRefFkInt);
		if (result == null){
			result = relatedRefMap.get(String.valueOf(inRefFkInt));
		}
		return result;
	}

	private ReferenceBase<?> makePrintSeries(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'PrintSeries'");}
		ReferenceBase printSeries = ReferenceFactory.newPrintSeries();
		makeStandardMapper(valueMap, printSeries, null);
		return printSeries;
	}
	
	private ReferenceBase<?> makeProceedings(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Proceedings'");}
		ReferenceBase proceedings = ReferenceFactory.newProceedings();
		makeStandardMapper(valueMap, proceedings, null);	
		return proceedings;
	}

	private ReferenceBase<?> makeThesis(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'Thesis'");}
		ReferenceBase thesis = ReferenceFactory.newThesis();
		makeStandardMapper(valueMap, thesis, null);	
		return thesis;
	}

	
	private ReferenceBase<?> makeJournalVolume(Map<String, Object> valueMap){
		if (logger.isDebugEnabled()){logger.debug("RefType 'JournalVolume'");}
		//Proceedings proceedings = Proceedings.NewInstance();
		ReferenceBase journalVolume = ReferenceFactory.newGeneric();
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
		if (mapper instanceof DbImportExtensionMapper){
			result &= ((DbImportExtensionMapper)mapper).invoke(valueMap, cdmBase);
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
				if (CdmUtils.isEmpty(authorString)){
					result = null;
				}else{
					TeamOrPersonBase<?> team = Team.NewInstance();
					//TODO which one to use??
					team.setNomenclaturalTitle(authorString);
					team.setTitleCache(authorString, true);
					result = team;
				}
			}
		}else{ //prefer bibliographic
			if (CdmUtils.isNotEmpty(authorString)){
				TeamOrPersonBase<?> team = Team.NewInstance();
				//TODO which one to use??
				team.setNomenclaturalTitle(authorString);
				team.setTitleCache(authorString, true);
				result = team;
			}else{
				result = nomAuthor;
			}
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

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(BerlinModelImportState state){
		BerlinModelReferenceImportValidator validator = new BerlinModelReferenceImportValidator();
		return validator.validate(state, this);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getTableName()
	 */
	@Override
	protected String getTableName() {
		return dbTableName;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getPluralString()
	 */
	@Override
	public String getPluralString() {
		return pluralString;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(BerlinModelImportState state){
		return (state.getConfig().getDoReferences() == IImportConfigurator.DO_REFERENCES.NONE);
	}

	


}
