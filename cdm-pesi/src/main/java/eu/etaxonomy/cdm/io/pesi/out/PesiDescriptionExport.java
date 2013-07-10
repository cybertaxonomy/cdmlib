// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.pesi.out;

import static java.util.EnumSet.of;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.mapping.DbIgnoreMapper;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.common.mapping.out.CollectionExportMapping;
import eu.etaxonomy.cdm.io.common.mapping.out.DbAreaMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbConstantMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbDescriptionElementTaxonMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbDistributionStatusMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbExportIgnoreMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbExportNotYetImplementedMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbLanguageMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbObjectMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbOriginalNameMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbSimpleFilterMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbSingleSourceMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbStringMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbTextDataMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbTimePeriodMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.IdMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.MethodMapper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.TaxonInteraction;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.profiler.ProfilerController;
/**
 * The export class for {@link eu.etaxonomy.cdm.model.description.DescriptionElementBase DescriptionElements}.<p>
 * Inserts into DataWarehouse database table <code>Note</code>.<p>
 * It is divided into two phases:<ul>
 * <li>Phase 1:	Export of DescriptionElements as Notes.
 * <li>Phase 2:	Export of TaxonName extensions <code>taxComment</code>, <code>fauComment</code> and <code>fauExtraCodes</code> as Notes.</ul>
 * @author e.-m.lee
 * @date 23.02.2010
 *
 */
@Component
public class PesiDescriptionExport extends PesiExportBase {
	private static final Logger logger = Logger.getLogger(PesiDescriptionExport.class);
	
	private static final Class<? extends CdmBase> standardMethodParameter = DescriptionElementBase.class;

	private static int modCount = 1000;
	private static final String dbNoteTableName = "Note";
	private static final String dbOccurrenceTableName = "Occurrence";
	private static final String dbVernacularTableName = "CommonName";
	private static final String dbImageTableName = "Image";
	private static final String dbAdditionalSourceTableName = "AdditionalTaxonSource";
	private static final String pluralString = "attached infos";
	private static final String parentPluralString = "Taxa";

	//decide where to handle them best (configurator, transformer, single method, ...)
	private static Set<Integer> excludedNoteCategories = new HashSet<Integer>(Arrays.asList(new Integer[]{250,251,252,253,10,11,13}));

	
	//debugging
	private static int countDescriptions;
	private static int countTaxa;
	private static int countDistribution;
	private static int countAdditionalSources;
	private static int countImages;
	private static int countNotes;
	
	private static int countCommonName;
	private static int countOccurrence;
	private static int countOthers;
	
	public PesiDescriptionExport() {
		super();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.DbExportBase#getStandardMethodParameter()
	 */
	@Override
	public Class<? extends CdmBase> getStandardMethodParameter() {
		return standardMethodParameter;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(PesiExportState state) {
		boolean result = true;
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected void doInvoke(PesiExportState state) {
		try {
			logger.info("*** Started Making " + pluralString + " ...");

			// Stores whether this invoke was successful or not.
			boolean success = true;
	
			// Get specific mappings: (CDM) DescriptionElement -> (PESI) Note
			PesiExportMapping notesMapping = getNotesMapping();
			notesMapping.initialize(state);

			// Get specific mappings: (CDM) DescriptionElement -> (PESI) Occurrence
			PesiExportMapping occurrenceMapping = getOccurrenceMapping();
			occurrenceMapping.initialize(state);

			// Get specific mappings: (CDM) DescriptionElement -> (PESI) Additional taxon source
			PesiExportMapping addSourceSourceMapping = getAddTaxonSourceSourceMapping();
			addSourceSourceMapping.initialize(state);
			PesiExportMapping additionalSourceMapping = getAdditionalTaxonSourceMapping();
			additionalSourceMapping.initialize(state);

			// Get specific mappings: (CDM) DescriptionElement -> (PESI) Additional taxon source
			PesiExportMapping vernacularMapping = getVernacularNamesMapping();
			vernacularMapping.initialize(state);
			
			// Get specific mappings: (CDM) DescriptionElement -> (PESI) Additional taxon source
			PesiExportMapping imageMapping = getImageMapping();
			imageMapping.initialize(state);
			
			// Start transaction
			success &= doPhase01(state, notesMapping, occurrenceMapping, addSourceSourceMapping, additionalSourceMapping, vernacularMapping, imageMapping);

			// Start transaction
			success &= doPhase01b(state, notesMapping, occurrenceMapping, addSourceSourceMapping, additionalSourceMapping, vernacularMapping, imageMapping);

			
			logger.info("PHASE 2...");
			success &= doPhase02(state);


			logger.info("*** Finished Making " + pluralString + " ..." + getSuccessString(success));
			
			if (!success){
				state.setUnsuccessfull();
			}
			return;
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			state.setUnsuccessfull();
		}
	}

	//PHASE 01: Description Elements
	private boolean doPhase01(PesiExportState state, PesiExportMapping notesMapping, PesiExportMapping occurrenceMapping, PesiExportMapping addSourceSourceMapping, 
			PesiExportMapping additionalSourceMapping, PesiExportMapping vernacularMapping, PesiExportMapping imageMapping) throws SQLException {
		logger.info("PHASE 1...");
		int count = 0;
		int pastCount = 0;
		boolean success = true;
		int limit = state.getConfig().getLimitSave();

		List<Taxon> taxonList = null;
		
		TransactionStatus txStatus = startTransaction(true);
		logger.info("Started new transaction. Fetching some " + pluralString + " (max: " + limit + ") ...");
		List<String> propPath = Arrays.asList(new String[]{"descriptions.elements.*"});
		
		logger.debug("Start snapshot, before starting loop");
		ProfilerController.memorySnapshot();
		//taxon descriptions
		int partitionCount = 0;
		while ((taxonList = getNextTaxonPartition(Taxon.class, limit, partitionCount++, propPath )) != null   ) {

			logger.info("Fetched " + taxonList.size() + " " + pluralString + ". Exporting...");
			
			for (Taxon taxon : taxonList) {
				countTaxa++;
				doCount(count++, modCount, pluralString);
				state.setCurrentTaxon(taxon);
				success &= handleSingleTaxon(taxon, state, notesMapping, occurrenceMapping, addSourceSourceMapping, 
						additionalSourceMapping, vernacularMapping, imageMapping);	
			}
			taxonList = null;
			state.setCurrentTaxon(null);

			// Commit transaction
			commitTransaction(txStatus);
			logger.info("Exported " + (count - pastCount) + " " + pluralString + ". Total: " + count);
			pastCount = count;

			// Start transaction
			txStatus = startTransaction(true);
			logger.info("Started new transaction. Fetching some " + pluralString + " (max: " + limit + ") for description import ...");
		}
		
	
		logger.info("No " + pluralString + " left to fetch.");
		logger.info("Partition: " + partitionCount);
		logger.info("Taxa: " + countTaxa);
		logger.info("Desc: " + countDescriptions);
		logger.info("Distr: " + countDistribution);
		logger.info("Occur: " + countOccurrence);
		logger.info("Commons: " + countCommonName);
		logger.info("AddSrc: " + countAdditionalSources);
		logger.info("Images: " + countImages);
		logger.info("Notes: " + countNotes);
		logger.info("Others: " + countOthers);
		
		// Commit transaction
		commitTransaction(txStatus);
		logger.debug("Committed transaction.");
		return success;
	}
	
	//PHASE 01b: Name Descriptions
	private boolean doPhase01b(PesiExportState state, PesiExportMapping notesMapping, PesiExportMapping occurrenceMapping, PesiExportMapping addSourceSourceMapping, 
			PesiExportMapping additionalSourceMapping, PesiExportMapping vernacularMapping, PesiExportMapping imageMapping) throws SQLException {
		logger.info("PHASE 1b...");
		int count = 0;
		int pastCount = 0;
		boolean success = true;
		int limit = state.getConfig().getLimitSave();
		
		List<TaxonNameDescription> nameDescList = null;
		
		TransactionStatus txStatus = startTransaction(true);
		logger.info("Started new transaction. Fetching some name descriptions (max: " + limit + ") ...");
		List<String> propPath = Arrays.asList(new String[]{"descriptions.elements.*"});
		
		//name descriptions
		int partitionCount = 0;
		while ((nameDescList = getNextNameDescriptionPartition( limit, partitionCount++, propPath )) != null   ) {
			
			logger.info("Fetched " + nameDescList.size() + " name descriptions. Exporting...");
			
			for (TaxonNameDescription desc : nameDescList) {
				countTaxa++;
				doCount(count++, modCount, "name descriptions");
				boolean isImageGallery = desc.isImageGallery();
				
				TaxonNameBase<?,?> name = desc.getTaxonName();
				
				for (DescriptionElementBase element : desc.getElements()){
					if (isPurePesiName(name)){
						success &= handleDescriptionElement(state, notesMapping, occurrenceMapping, vernacularMapping, imageMapping,
								addSourceSourceMapping, additionalSourceMapping, isImageGallery, element);
					}else{
						for (TaxonBase<?> taxonBase : name.getTaxonBases()){
							if (isPesiTaxon(taxonBase)){
								state.setCurrentTaxon(taxonBase);
								success &= handleDescriptionElement(state, notesMapping, occurrenceMapping, vernacularMapping, imageMapping,
										addSourceSourceMapping, additionalSourceMapping, isImageGallery, element);
								state.setSourceForAdditionalSourceCreated(true);
							}
						}
						state.setSourceForAdditionalSourceCreated(false);
					}
				}
			}
			nameDescList = null;
			state.setCurrentTaxon(null);

			// Commit transaction
			commitTransaction(txStatus);
			logger.info("Exported " + (count - pastCount) + " name descriptions. Total: " + count);
			pastCount = count;

			// Start transaction
			txStatus = startTransaction(true);
			logger.info("Started new transaction. Fetching some name descriptions (max: " + limit + ") for description import ...");
		}
		
		logger.info("No " + pluralString + " left to fetch.");
		logger.info("Partition: " + partitionCount);
		logger.info("Taxa: " + countTaxa);
		logger.info("Desc: " + countDescriptions);
		logger.info("Distr: " + countDistribution);
		logger.info("Occur: " + countOccurrence);
		logger.info("Commons: " + countCommonName);
		logger.info("AddSrc: " + countAdditionalSources);
		logger.info("Images: " + countImages);
		logger.info("Notes: " + countNotes);
		logger.info("Others: " + countOthers);
		
		// Commit transaction
		commitTransaction(txStatus);
		logger.debug("Committed transaction.");
		return success;
	}

	private boolean handleSingleTaxon(Taxon taxon, PesiExportState state, PesiExportMapping notesMapping, PesiExportMapping occurrenceMapping,
			PesiExportMapping addSourceSourceMapping, PesiExportMapping additionalSourceMapping, 
			PesiExportMapping vernacularMapping, PesiExportMapping imageMapping) throws SQLException {
		boolean success = true;
		Set<DescriptionBase<?>> descriptions = new HashSet<DescriptionBase<?>>();
		descriptions.addAll(taxon.getDescriptions());
		
		for (DescriptionBase<?> desc : descriptions){
			countDescriptions++;

			boolean isImageGallery = desc.isImageGallery();
			for (DescriptionElementBase element : desc.getElements()){
				success &= handleDescriptionElement(state, notesMapping, occurrenceMapping, vernacularMapping, imageMapping,
						addSourceSourceMapping, additionalSourceMapping, isImageGallery, element);
			}
		}
		return success;
	}

	private boolean handleDescriptionElement(PesiExportState state, PesiExportMapping notesMapping,
			PesiExportMapping occurrenceMapping, PesiExportMapping vernacularMapping, PesiExportMapping imageMapping, 
			PesiExportMapping addSourceSourceMapping, PesiExportMapping additionalSourceMapping, boolean isImageGallery, DescriptionElementBase element) throws SQLException {
		try {
			boolean success = true;
			if (isImageGallery){
				//TODO handle Images
				countImages++;
				success &= imageMapping.invoke(element);
			}else if (isCommonName(element)){
				countCommonName++;
				if (element.isInstanceOf(TextData.class)){
					//we do not import text data common names
				}else{
					success &= vernacularMapping.invoke(element);
				}
			}else if (isOccurrence(element)){
				countOccurrence++;
				Distribution distribution = CdmBase.deproxy(element, Distribution.class);
				MarkerType markerType = getUuidMarkerType(PesiTransformer.uuidMarkerTypeHasNoLastAction, state);
				
				distribution.addMarker(Marker.NewInstance(markerType, true));
				if (isPesiDistribution(state, distribution)){
					countDistribution++;
					success &=occurrenceMapping.invoke(element);
				}
			}else if (isAdditionalTaxonSource(element)){
				countAdditionalSources++;
				if (! state.isSourceForAdditionalSourceCreated()){
					success &= addSourceSourceMapping.invoke(element);
				}
				success &= additionalSourceMapping.invoke(element);
			}else if (isExcludedNote(element)){
				//do nothing
			}else if (isPesiNote(element)){
				countNotes++;
				success &= notesMapping.invoke(element);
			
			}else{
				countOthers++;
				String featureTitle = element.getFeature() == null ? "no feature" :element.getFeature().getTitleCache();
				logger.warn("Description element type not yet handled by PESI export: " + element.getUuid() + ", " +  element.getClass() + ", " +  featureTitle);
			}
			return success;
		} catch (Exception e) {
			logger.warn("Exception appeared in description element handling: " + e);
			e.printStackTrace();
			return false;
		}
	}

	private boolean isExcludedNote(DescriptionElementBase element) {
		Integer categoryFk = PesiTransformer.feature2NoteCategoryFk(element.getFeature());
		//TODO decide where to handle them best (configurator, transformer, single method, ...)
		return (excludedNoteCategories.contains(categoryFk));
	}

	private boolean isPesiDistribution(PesiExportState state, Distribution distribution) {
		//currently we use the E+M summary status to decide if a distribution should be exported
		if (distribution.getStatus() == null){
			return false;
		}
		
		//...this may change in future so we keep the following code
		Integer key;
		//area filter
		NamedArea area = distribution.getArea();
		if (area == null){
			logger.warn("Area is null for distribution " +  distribution.getUuid());
			return false;
		}else if (area.getUuid().equals(BerlinModelTransformer.euroMedUuid)){
			//E+M area only holds endemic status information and therefore is not exported to PESI
			return false;
		}else if (area.equals(TdwgArea.getAreaByTdwgAbbreviation("1"))){
			//Europe area never holds status information (may probably be deleted in E+M)
			return false;
//		}else if (area.equals(TdwgArea.getAreaByTdwgAbbreviation("21"))){
//			//Macaronesia records should not be exported to PESI
//			return false;
//		//TODO exclude Russion areas Rs*, and maybe ohters
		
		} else
			try {
				if (state.getTransformer().getKeyByNamedArea(area) == null){
					String warning = "Area (%s,%s) not available in PESI transformer for taxon %S: ";
					TaxonBase<?> taxon =  state.getCurrentTaxon();
					warning = String.format(warning, area.getTitleCache(), area.getRepresentation(Language.ENGLISH()).getAbbreviatedLabel(),taxon ==null? "-" : taxon.getTitleCache());
					logger.warn(warning);
					return false;
				}
			} catch (UndefinedTransformerMethodException e1) {
				logger.warn("Area not available in PESI transformer " +  area.getTitleCache());
				return false;
			}
		return true;
		
//		
//		//status
//		PresenceAbsenceTermBase<?> status = distribution.getStatus();
//		if (status == null){
//			logger.warn("No status for distribution: " +  distribution.getUuid());
//			return false;
//		}
//		try {
//			key = (Integer)state.getTransformer().getKeyByPresenceAbsenceTerm(status);
//			if (key != null){
//				return true;
//			}else{
//				logger.warn("PresenceAbsenceTerm " + status.getTitleCache() + "not handled in transformer");
//				return false;
//			}
//		} catch (UndefinedTransformerMethodException e) {
//			logger.warn("PresenceAbsenceTerm " + status.getTitleCache() + "not handled in transformer");
//			return false;
//		}
	}

	private boolean isPesiNote(DescriptionElementBase element) {
		return (getNoteCategoryFk(element) != null);
	}

	private boolean isAdditionalTaxonSource(DescriptionElementBase element) {
		Feature feature = element.getFeature();
		if (feature == null){
			return false;
		}
		return (feature.equals(Feature.CITATION()) || feature.equals(Feature.ADDITIONAL_PUBLICATION()));
	}

	private boolean isOccurrence(DescriptionElementBase element) {
		Feature feature = element.getFeature();
		if (feature != null && feature.equals(Feature.DISTRIBUTION())){
			return true;
		}else if (element.isInstanceOf(Distribution.class)){
			logger.warn("Description element has class 'Distribution' but has no feature 'Distribution'");
			return true;
		}else{
			return false;
		}
	}

	private boolean isCommonName(DescriptionElementBase element) {
		Feature feature = element.getFeature();
		if (feature == null){
			return false;
		}
		return (feature.equals(Feature.COMMON_NAME()));
	}

	//PHASE 02: Name extensions
	private boolean doPhase02(PesiExportState state) {
		TransactionStatus txStatus;
		boolean success =  true;
		
		// Get the limit for objects to save within a single transaction.
		int limit = state.getConfig().getLimitSave();
					
		txStatus = startTransaction(true);
		ExtensionType taxCommentExtensionType = (ExtensionType)getTermService().find(PesiTransformer.taxCommentUuid);
		ExtensionType fauCommentExtensionType = (ExtensionType)getTermService().find(PesiTransformer.fauCommentUuid);
		ExtensionType fauExtraCodesExtensionType = (ExtensionType)getTermService().find(PesiTransformer.fauExtraCodesUuid);
		List<TaxonNameBase> taxonNameList = null;
		
		int count = 0;
		int pastCount = 0;
		Connection connection = state.getConfig().getDestination().getConnection();
		logger.info("Started new transaction. Fetching some " + parentPluralString + " first (max: " + limit + ") ...");
		logger.warn("TODO handle extensions on taxon level, not name level (");
		while ((taxonNameList = getNameService().list(null, limit, count, null, null)).size() > 0) {

			logger.info("Fetched " + taxonNameList.size() + " names. Exporting...");
			for (TaxonNameBase<?,?> taxonName : taxonNameList) {
				Set<Extension> extensions = taxonName.getExtensions();
				for (Extension extension : extensions) {
					if (extension.getType().equals(taxCommentExtensionType)) {
						String taxComment = extension.getValue();
						invokeNotes(taxComment, 
								PesiTransformer.getNoteCategoryFk(PesiTransformer.taxCommentUuid), 
								PesiTransformer.getNoteCategoryCache(PesiTransformer.taxCommentUuid),
								null, null, getTaxonKey(taxonName, state),connection);
					} else if (extension.getType().equals(fauCommentExtensionType)) {
						String fauComment = extension.getValue();
						invokeNotes(fauComment, 
								PesiTransformer.getNoteCategoryFk(PesiTransformer.fauCommentUuid), 
								PesiTransformer.getNoteCategoryCache(PesiTransformer.fauCommentUuid),
								null, null, getTaxonKey(taxonName, state),connection);
					} else if (extension.getType().equals(fauExtraCodesExtensionType)) {
						String fauExtraCodes = extension.getValue();
						invokeNotes(fauExtraCodes, 
								PesiTransformer.getNoteCategoryFk(PesiTransformer.fauExtraCodesUuid), 
								PesiTransformer.getNoteCategoryCache(PesiTransformer.fauExtraCodesUuid),
								null, null, getTaxonKey(taxonName, state),connection);
					}
				}
				
				doCount(count++, modCount, pluralString);
			}

			// Commit transaction
			commitTransaction(txStatus);
			logger.debug("Committed transaction.");
			logger.info("Exported " + (count - pastCount) + " names. Total: " + count);
			pastCount = count;

			// Start transaction
			txStatus = startTransaction(true);
			logger.info("Started new transaction. Fetching some names first (max: " + limit + ") ...");
		}
		if (taxonNameList.size() == 0) {
			logger.info("No names left to fetch.");
		}
		taxonNameList = null;
		// Commit transaction
		commitTransaction(txStatus);
		logger.debug("Committed transaction.");
		return success;
	}

	/**
	 * @param taxComment
	 * @param noteCategoryFk
	 * @param noteCategoryCache
	 * @param object
	 * @param object2
	 */
	private void invokeNotes(String note, Integer noteCategoryFk,
			String noteCategoryCache, Integer languageFk, String languageCache, 
			Integer taxonFk, Connection connection) {
		String notesSql = "UPDATE Note SET Note_1 = ?, NoteCategoryFk = ?, NoteCategoryCache = ?, LanguageFk = ?, LanguageCache = ? WHERE TaxonFk = ?"; 
		try {
			PreparedStatement notesStmt = connection.prepareStatement(notesSql);
			
			if (note != null) {
				notesStmt.setString(1, note);
			} else {
				notesStmt.setObject(1, null);
			}
			
			if (noteCategoryFk != null) {
				notesStmt.setInt(2, noteCategoryFk);
			} else {
				notesStmt.setObject(2, null);
			}
			
			if (noteCategoryCache != null) {
				notesStmt.setString(3, noteCategoryCache);
			} else {
				notesStmt.setObject(3, null);
			}
			
			if (languageFk != null) {
				notesStmt.setInt(4, languageFk);
			} else {
				notesStmt.setObject(4, null);
			}
			
			if (languageCache != null) {
				notesStmt.setString(5, languageCache);
			} else {
				notesStmt.setObject(5, null);
			}
			
			if (taxonFk != null) {
				notesStmt.setInt(6, taxonFk);
			} else {
				notesStmt.setObject(6, null);
			}
			
			notesStmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Note could not be created: " + note);
			e.printStackTrace();
		}


	}

	/**
	 * Deletes all entries of database tables related to <code>Note</code>.
	 * @param state The PesiExportState
	 * @return Whether the delete operation was successful or not.
	 */
	protected boolean doDelete(PesiExportState state) {
		PesiExportConfigurator pesiConfig = (PesiExportConfigurator) state.getConfig();
		
		String sql;
		Source destination =  pesiConfig.getDestination();

		// Clear NoteSource
		sql = "DELETE FROM NoteSource";
		destination.setQuery(sql);
		destination.update(sql);

		// Clear Note
		sql = "DELETE FROM " + dbNoteTableName;
		destination.setQuery(sql);
		destination.update(sql);
		return true;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean isIgnore(PesiExportState state) {
		return ! state.getConfig().isDoDescription();
	}


	/**
	 * Returns the <code>Note_2</code> attribute.
	 * @param descriptionElement The {@link DescriptionElementBase DescriptionElement}.
	 * @return The <code>Note_2</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused") //used for mapper
	private static String getNote_2(DescriptionElementBase element) {
		//E+M map links -> medium
		if (element.getFeature() != null && element.getFeature().getUuid().equals(BerlinModelTransformer.uuidFeatureMaps)){
			String text = CdmBase.deproxy(element, TextData.class).getText(Language.ENGLISH());
			if (text.contains("medium")){
				return "medium";
			}
		}
		return null;
	}

	/**
	 * Returns the <code>NoteCategoryFk</code> attribute.
	 * @param descriptionElement The {@link DescriptionElementBase DescriptionElement}.
	 * @return The <code>NoteCategoryFk</code> attribute.
	 * @see MethodMapper
	 */
	private static Integer getNoteCategoryFk(DescriptionElementBase descriptionElement) {
		Integer result = null;
		result = PesiTransformer.feature2NoteCategoryFk(descriptionElement.getFeature());
		//TODO decide where to handle them best (configurator, transformer, single method, ...)
		if (excludedNoteCategories.contains(result)){
			result = null;
		}
		return result;
	}
	
	/**
	 * Returns the <code>NoteCategoryCache</code> attribute.
	 * @param descriptionElement The {@link DescriptionElementBase DescriptionElement}.
	 * @return The <code>NoteCategoryCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getNoteCategoryCache(DescriptionElementBase descriptionElement, PesiExportState state) {
		return state.getTransformer().getCacheByFeature(descriptionElement.getFeature());
	}
	
	


	/**
	 * Returns the <code>LanguageFk</code> attribute.
	 * @param descriptionElement The {@link DescriptionElementBase DescriptionElement}.
	 * @return The <code>LanguageFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getLanguageFk(DescriptionElementBase descriptionElement) {
		Language language = getLanguage(descriptionElement);

		return PesiTransformer.language2LanguageId(language);
	}

	/**
	 * Returns the <code>LanguageCache</code> attribute.
	 * @param descriptionElement The {@link DescriptionElementBase DescriptionElement}.
	 * @return The <code>LanguageCache</code> attribute.
	 * @throws UndefinedTransformerMethodException 
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getLanguageCache(DescriptionElementBase descriptionElement, PesiExportState state) throws UndefinedTransformerMethodException {
		Language language = getLanguage(descriptionElement);
		return state.getTransformer().getCacheByLanguage(language);
	}

	private static Language getLanguage(DescriptionElementBase descriptionElement) {
		Language language = null;

		Map<Language, LanguageString> multilanguageText = null;
		if (descriptionElement.isInstanceOf(CommonTaxonName.class)) {
			CommonTaxonName commonTaxonName = CdmBase.deproxy(descriptionElement, CommonTaxonName.class);
			language = commonTaxonName.getLanguage();
		} else if (descriptionElement.isInstanceOf(TextData.class)) {
			TextData textData = CdmBase.deproxy(descriptionElement, TextData.class);
			multilanguageText = textData.getMultilanguageText();
		} else if (descriptionElement.isInstanceOf(IndividualsAssociation.class)) {
			IndividualsAssociation individualsAssociation = CdmBase.deproxy(descriptionElement, IndividualsAssociation.class);
			multilanguageText = individualsAssociation.getDescription();
		} else if (descriptionElement.isInstanceOf(TaxonInteraction.class)) {
			TaxonInteraction taxonInteraction = CdmBase.deproxy(descriptionElement, TaxonInteraction.class);
			multilanguageText = taxonInteraction.getDescriptions();
		} else {
			logger.debug("Given descriptionElement does not support languages. Hence LanguageCache could not be determined: " + descriptionElement.getUuid());
		}
		
		if (multilanguageText != null) {
			Set<Language> languages = multilanguageText.keySet();

			// TODO: Think of something more sophisticated than this
			if (languages.size() > 0) {
				language = languages.iterator().next();
			}
			if (languages.size() > 1){
				logger.warn("There is more than 1 language for a given description (" + descriptionElement.getClass().getSimpleName() + "):" + descriptionElement.getUuid());
			}
		}
		return language;
	}

//	/**
//	 * Returns the <code>Region</code> attribute.
//	 * @param descriptionElement The {@link DescriptionElementBase DescriptionElement}.
//	 * @return The <code>Region</code> attribute.
//	 * @see MethodMapper
//	 */
//	@SuppressWarnings("unused")
//	private static String getRegion(DescriptionElementBase descriptionElement) {
//		String result = null;
//		DescriptionBase<?> inDescription = descriptionElement.getInDescription();
//		
//		// Area information are associated to TaxonDescriptions and Distributions.
//		if (descriptionElement.isInstanceOf(Distribution.class)) {
//			Distribution distribution = CdmBase.deproxy(descriptionElement, Distribution.class);
//			result = PesiTransformer.area2AreaCache(distribution.getArea());
//		} else if (inDescription != null && inDescription.isInstanceOf(TaxonDescription.class)) {
//			TaxonDescription taxonDescription = CdmBase.deproxy(inDescription, TaxonDescription.class);
//			Set<NamedArea> namedAreas = taxonDescription.getGeoScopes();
//			if (namedAreas.size() == 1) {
//				result = PesiTransformer.area2AreaCache(namedAreas.iterator().next());
//			} else if (namedAreas.size() > 1) {
//				logger.warn("This TaxonDescription contains more than one NamedArea: " + taxonDescription.getTitleCache());
//			}
//		}
//		return result;
//	}

	
	/**
	 * Returns the TaxonFk for a given TaxonName or Taxon.
	 * @param state The {@link DbExportStateBase DbExportState}.
	 * @return
	 */
	@SuppressWarnings("unused")  //used by mapper
	private static Integer getTaxonFk(DescriptionElementBase deb, PesiExportState state) {
		TaxonBase<?> entity = state.getCurrentTaxon();
		return state.getDbId(entity);
	}
	
	/**
	 * Returns the TaxonFk for a given TaxonName.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @param state The {@link DbExportStateBase DbExportState}.
	 * @return
	 */
	private static Integer getTaxonKey(TaxonNameBase<?,?> taxonName, DbExportStateBase<?, PesiTransformer> state) {
		return state.getDbId(taxonName);
	}
	
	/**
	 * Returns the <code>FullName</code> attribute.
	 * @param taxonName The {@link NonViralName NonViralName}.
	 * @return The <code>FullName</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getTaxonFullNameCache(DescriptionElementBase deb, PesiExportState state) {
		
		TaxonBase<?> taxon =  state.getCurrentTaxon();
		TaxonNameBase<?,?> taxonName = taxon.getName();
		NonViralName<?> nvn = CdmBase.deproxy(taxonName, NonViralName.class);
		String result = getCacheStrategy(nvn).getTitleCache(nvn);
		return result;
	}


	/**
	 * Returns the CDM to PESI specific export mappings for PESI notes.
	 * @return The {@link PesiExportMapping PesiExportMapping}.
	 */
	private PesiExportMapping getNotesMapping() {
		PesiExportMapping mapping = new PesiExportMapping(dbNoteTableName);
		
		mapping.addMapper(IdMapper.NewInstance("NoteId"));
		mapping.addMapper(DbTextDataMapper.NewInstance(Language.ENGLISH(), "Note_1"));
		//TODO
		mapping.addMapper(MethodMapper.NewInstance("Note_2", this, DescriptionElementBase.class));
		mapping.addMapper(MethodMapper.NewInstance("NoteCategoryFk", this, DescriptionElementBase.class ));
		
		mapping.addMapper(MethodMapper.NewInstance("NoteCategoryCache", this, DescriptionElementBase.class, PesiExportState.class ));
		mapping.addMapper(MethodMapper.NewInstance("LanguageFk", this));
		mapping.addMapper(MethodMapper.NewInstance("LanguageCache", this, DescriptionElementBase.class, PesiExportState.class));
		
//		mapping.addMapper(MethodMapper.NewInstance("Region", this));
		mapping.addMapper(DbDescriptionElementTaxonMapper.NewInstance("taxonFk"));
		mapping.addMapper(ExpertsAndLastActionMapper.NewInstance());
		mapping.addCollectionMapping(getNoteSourceMapping());
		return mapping;
	}
	
	private CollectionExportMapping<PesiExportState, PesiExportConfigurator,PesiTransformer> getNoteSourceMapping() {
		String tableName = "NoteSource";
		String collectionAttribute = "sources";
		IdMapper parentMapper = IdMapper.NewInstance("NoteFk");
		CollectionExportMapping<PesiExportState, PesiExportConfigurator, PesiTransformer> mapping = CollectionExportMapping.NewInstance(tableName, collectionAttribute, parentMapper);
		mapping.addMapper(DbSimpleFilterMapper.NewSingleNullAttributeInstance("idInSource", "Sources with idInSource currently handle data lineage"));
		mapping.addMapper(DbObjectMapper.NewInstance("Citation", "SourceFk"));
		mapping.addMapper(DbObjectMapper.NewInstance("Citation", "SourceNameCache", IS_CACHE));
		mapping.addMapper(DbStringMapper.NewInstance("CitationMicroReference", "SourceDetail"));
		return mapping;
	}
	

	/**
	 * Returns the CDM to PESI specific export mappings for occurrences.
	 * @return The {@link PesiExportMapping PesiExportMapping}.
	 */
	private PesiExportMapping getOccurrenceMapping() {
		PesiExportMapping mapping = new PesiExportMapping(dbOccurrenceTableName);
		
		mapping.addMapper(IdMapper.NewInstance("OccurrenceId"));
		mapping.addMapper(DbDescriptionElementTaxonMapper.NewInstance("taxonFk"));
		mapping.addMapper(DbDescriptionElementTaxonMapper.NewInstance("TaxonFullNameCache", true, true, null)); 
		
		mapping.addMapper(DbAreaMapper.NewInstance(Distribution.class, "Area", "AreaFk", ! IS_CACHE));
		mapping.addMapper(DbAreaMapper.NewInstance(Distribution.class, "Area", "AreaNameCache", IS_CACHE));
		mapping.addMapper(DbDistributionStatusMapper.NewInstance("OccurrenceStatusFk", ! IS_CACHE));
		mapping.addMapper(DbDistributionStatusMapper.NewInstance("OccurrenceStatusCache", IS_CACHE));
		
//		Use Occurrence source instead
		mapping.addMapper(DbExportIgnoreMapper.NewInstance("SourceFk", "Use OccurrenceSource table for sources instead"));
		mapping.addMapper(DbExportIgnoreMapper.NewInstance("SourceNameCache", "Use OccurrenceSource table for sources instead"));
		
		
		mapping.addMapper(DbExportNotYetImplementedMapper.NewInstance("Notes", "Needs reimplementation in description export"));
		mapping.addMapper(ExpertsAndLastActionMapper.NewInstance());
		mapping.addCollectionMapping(getOccurrenceSourceMapping());
		
		return mapping;
	}

	private CollectionExportMapping<PesiExportState, PesiExportConfigurator, PesiTransformer> getOccurrenceSourceMapping() {
		String tableName = "OccurrenceSource";
		String collectionAttribute = "sources";
		IdMapper parentMapper = IdMapper.NewInstance("OccurrenceFk");
		CollectionExportMapping<PesiExportState, PesiExportConfigurator, PesiTransformer> mapping = CollectionExportMapping.NewInstance(tableName, collectionAttribute, parentMapper);
		mapping.addMapper(DbSimpleFilterMapper.NewSingleNullAttributeInstance("idInSource", "Sources with idInSource currently handle data lineage"));
		mapping.addMapper(DbObjectMapper.NewInstance("Citation", "SourceFk"));
		mapping.addMapper(DbObjectMapper.NewInstance("Citation", "SourceNameCache", IS_CACHE));
		mapping.addMapper(DbOriginalNameMapper.NewInstance("OldTaxonName", IS_CACHE, null));

		return mapping;
	}
	

	/**
	 * Returns the CDM to PESI specific export mappings for additional taxon sources to create a new
	 * source for the additional source
	 * @see #{@link PesiDescriptionExport#getAdditionalTaxonSourceMapping()}
	 * @return The {@link PesiExportMapping PesiExportMapping}.
	 */
	private PesiExportMapping getAddTaxonSourceSourceMapping() {
		PesiExportMapping sourceMapping = new PesiExportMapping(PesiSourceExport.dbTableName);
		
		sourceMapping.addMapper(IdMapper.NewInstance("SourceId"));
		sourceMapping.addMapper(DbConstantMapper.NewInstance("SourceCategoryFk", Types.INTEGER, PesiTransformer.REF_UNRESOLVED));
		sourceMapping.addMapper(DbConstantMapper.NewInstance("SourceCategoryCache", Types.VARCHAR, PesiTransformer.REF_STR_UNRESOLVED));
		
//		sourceMapping.addMapper(MethodMapper.NewInstance("NomRefCache", PesiSourceExport.class, "getNomRefCache", Reference.class));
		
		sourceMapping.addMapper(DbTextDataMapper.NewInstance(Language.ENGLISH(), "NomRefCache"));
		
		return sourceMapping;
	}

	
	/**
	 * Returns the CDM to PESI specific export mappings for additional taxon sources.
	 * @see #{@link PesiDescriptionExport#getAddTaxonSourceSourceMapping()}
	 * @return The {@link PesiExportMapping PesiExportMapping}.
	 */
	private PesiExportMapping getAdditionalTaxonSourceMapping() {
	
		PesiExportMapping mapping = new PesiExportMapping(dbAdditionalSourceTableName);
		
		mapping.addMapper(MethodMapper.NewInstance("TaxonFk", this, DescriptionElementBase.class, PesiExportState.class));
		
		mapping.addMapper(IdMapper.NewInstance("SourceFk"));
		mapping.addMapper(DbTextDataMapper.NewInstance(Language.ENGLISH(), "SourceNameCache"));
		
		mapping.addMapper(DbConstantMapper.NewInstance("SourceUseFk", Types.INTEGER, PesiTransformer.NOMENCLATURAL_REFERENCE));
		mapping.addMapper(DbConstantMapper.NewInstance("SourceUseCache", Types.VARCHAR, PesiTransformer.STR_NOMENCLATURAL_REFERENCE));
		
		mapping.addMapper(DbExportIgnoreMapper.NewInstance("SourceDetail", "SourceDetails not available for additional sources"));
		
		return mapping;
	}
	
	/**
	 * Returns the CDM to PESI specific export mappings for common names.
	 * @return The {@link PesiExportMapping PesiExportMapping}.
	 */
	private PesiExportMapping getVernacularNamesMapping() {
		PesiExportMapping mapping = new PesiExportMapping(dbVernacularTableName);
		
		mapping.addMapper(IdMapper.NewInstance("CommonNameId"));
		mapping.addMapper(DbDescriptionElementTaxonMapper.NewInstance("taxonFk"));
		
		mapping.addMapper(DbStringMapper.NewInstance("Name", "CommonName"));
		mapping.addMapper(DbAreaMapper.NewInstance(CommonTaxonName.class, "Area", "Region", IS_CACHE));
		
		mapping.addMapper(DbLanguageMapper.NewInstance(CommonTaxonName.class, "Language", "LanguageFk", ! IS_CACHE));
		mapping.addMapper(DbLanguageMapper.NewInstance(CommonTaxonName.class, "Language", "LanguageCache", IS_CACHE));
		
		mapping.addMapper(DbSingleSourceMapper.NewInstance("SourceFk", of ( DbSingleSourceMapper.EXCLUDE.WITH_ID) , ! IS_CACHE));
		mapping.addMapper(DbSingleSourceMapper.NewInstance("SourceNameCache", of ( DbSingleSourceMapper.EXCLUDE.WITH_ID) , IS_CACHE));
		mapping.addMapper(ExpertsAndLastActionMapper.NewInstance());
		return mapping;

	}
	
	private PesiExportMapping getImageMapping() {
		PesiExportMapping mapping = new PesiExportMapping(dbImageTableName);
		mapping.addMapper(DbDescriptionElementTaxonMapper.NewInstance("taxonFk"));
		
		//TODO xxx
		
		return mapping;
	}

}
