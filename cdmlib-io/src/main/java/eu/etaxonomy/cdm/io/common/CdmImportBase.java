/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.common.media.ImageInfo;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.markup.MarkupTransformer;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.IOriginalSource;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.RankClass;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;

/**
 * @author a.mueller
 * @created 01.07.2008
 */
public abstract class CdmImportBase<CONFIG extends IImportConfigurator, STATE extends ImportStateBase>
            extends CdmIoBase<STATE, ImportResult>
            implements ICdmImport<CONFIG, STATE>{

    private static final long serialVersionUID = 8730012744209195616L;
    private static final Logger logger = Logger.getLogger(CdmImportBase.class);

	protected static final boolean CREATE = true;
	protected static final boolean IMAGE_GALLERY = true;
	protected static final boolean READ_MEDIA_DATA = true;

	public static final UUID uuidUserDefinedNamedAreaLevelVocabulary = UUID.fromString("255144da-8d95-457e-a327-9752a8f85e5a");
	public static final UUID uuidUserDefinedNamedAreaVocabulary = UUID.fromString("b2238399-a3af-4f6d-b7eb-ff5d0899bf1b");
	public static final UUID uuidUserDefinedExtensionTypeVocabulary = UUID.fromString("e28c1394-1be8-4847-8b81-ab44eb6d5bc8");
	public static final UUID uuidUserDefinedIdentifierTypeVocabulary = UUID.fromString("194b173b-e2c8-49f1-bbfa-d5d51556cf68");
	public static final UUID uuidUserDefinedReferenceSystemVocabulary = UUID.fromString("467591a3-10b4-4bf1-9239-f06ece33e90a");
	public static final UUID uuidUserDefinedFeatureVocabulary = UUID.fromString("fe5fccb3-a2f2-4b97-b199-6e2743cf1627");
	public static final UUID uuidUserDefinedMeasurementUnitVocabulary = UUID.fromString("d5e72bb7-f312-4080-bb86-c695d04a6e66");
	public static final UUID uuidUserDefinedStatisticalMeasureVocabulary = UUID.fromString("62a89836-c730-4b4f-a904-3d859dbfc400");
	public static final UUID uuidUserDefinedStateVocabulary = UUID.fromString("f7cddb49-8392-4db1-8640-65b48a0e6d13");
	public static final UUID uuidUserDefinedTaxonRelationshipTypeVocabulary = UUID.fromString("31a324dc-408d-4877-891f-098db21744c6");
	public static final UUID uuidUserDefinedAnnotationTypeVocabulary = UUID.fromString("cd9ecdd2-9cae-4890-9032-ad83293ae883");
	public static final UUID uuidUserDefinedMarkerTypeVocabulary = UUID.fromString("5f02a261-fd7d-4fce-bbe4-21472de8cd51");
	public static final UUID uuidUserDefinedRankVocabulary = UUID.fromString("4dc57931-38e2-46c3-974d-413b087646ba");
	public static final UUID uuidUserDefinedPresenceAbsenceVocabulary = UUID.fromString("6b8a2581-1471-4ea6-b8ad-b2d931cfbc23");

	public static final UUID uuidUserDefinedModifierVocabulary = UUID.fromString("2a8b3838-3a95-49ea-9ab2-3049614b5884");
	public static final UUID uuidUserDefinedKindOfUnitVocabulary = UUID.fromString("e7c5deb2-f485-4a66-9104-0c5398efd481");



	private static final String UuidOnly = "UUIDOnly";
	private static final String UuidLabel = "UUID or label";
	private static final String UuidLabelAbbrev = "UUID, label or abbreviation";
	private static final String UuidAbbrev = "UUID or abbreviation";

	private final static String authorSeparator = ", ";
    private final static String lastAuthorSeparator = " & ";

	public enum TermMatchMode{
		UUID_ONLY(0, UuidOnly)
		,UUID_LABEL(1, UuidLabel)
		,UUID_LABEL_ABBREVLABEL(2, UuidLabelAbbrev)
		,UUID_ABBREVLABEL(3, UuidAbbrev)
		;


		private final int id;
		private final String representation;
		private TermMatchMode(int id, String representation){
			this.id = id;
			this.representation = representation;
		}
		public int getId() {
			return id;
		}
		public String getRepresentation() {
			return representation;
		}
		public TermMatchMode valueOf(int id){
			switch (id){
				case 0: return UUID_ONLY;
				case 1: return UUID_LABEL;
				case 2: return UUID_LABEL_ABBREVLABEL;
				case 3: return UUID_ABBREVLABEL;
				default: return UUID_ONLY;
			}
 		}


	}

    @Override
    protected ImportResult getNoDataResult(STATE state) {
        return ImportResult.NewNoDataInstance();
    }

    @Override
    protected ImportResult getDefaultResult(STATE state) {
        return ImportResult.NewInstance();
    }

	protected Classification makeTree(STATE state, Reference reference){
		String treeName = "Classification (Import)";
		if (reference != null && StringUtils.isNotBlank(reference.getTitleCache())){
			treeName = reference.getTitleCache();
		}
		Classification tree = Classification.NewInstance(treeName);
		tree.setReference(reference);


		// use defined uuid for first tree
		CONFIG config = (CONFIG)state.getConfig();
		if (state.countTrees() < 1 ){
			tree.setUuid(config.getClassificationUuid());
		}
		getClassificationService().save(tree);
		state.putTree(reference, tree);
		return tree;
	}


	/**
	 * Alternative memory saving method variant of
	 * {@link #makeTree(STATE state, Reference ref)} which stores only the
	 * UUID instead of the full tree in the <code>ImportStateBase</code> by
	 * using <code>state.putTreeUuid(ref, tree);</code>
	 *
	 * @param state
	 * @param ref
	 * @return
	 */
	protected Classification makeTreeMemSave(STATE state, Reference ref){
		String treeName = "Classification (Import)";
		if (ref != null && StringUtils.isNotBlank(ref.getTitleCache())){
			treeName = ref.getTitleCache();
		}
		Classification tree = Classification.NewInstance(treeName);
		tree.setReference(ref);


		// use defined uuid for first tree
		CONFIG config = (CONFIG)state.getConfig();
		if (state.countTrees() < 1 ){
			tree.setUuid(config.getClassificationUuid());
		}
		getClassificationService().save(tree);
		state.putTreeUuid(ref, tree);
		return tree;
	}




	protected ExtensionType getExtensionType(STATE state, UUID uuid, String label, String text, String labelAbbrev){
		return getExtensionType(state, uuid, label, text, labelAbbrev, null);
	}
	protected ExtensionType getExtensionType(STATE state, UUID uuid, String label, String text, String labelAbbrev, TermVocabulary<ExtensionType> voc){
		if (uuid == null){
			uuid = UUID.randomUUID();
		}
		ExtensionType extensionType = state.getExtensionType(uuid);
		if (extensionType == null){
			extensionType = (ExtensionType)getTermService().find(uuid);
			if (extensionType == null){
				extensionType = ExtensionType.NewInstance(text, label, labelAbbrev);
				extensionType.setUuid(uuid);
				if (voc == null){
					boolean isOrdered = false;
					voc = getVocabulary(TermType.ExtensionType, uuidUserDefinedExtensionTypeVocabulary, "User defined vocabulary for extension types", "User Defined Extension Types", null, null, isOrdered, extensionType);
				}
				voc.addTerm(extensionType);
				getTermService().saveOrUpdate(extensionType);
			}
			state.putExtensionType(extensionType);
		}
		return extensionType;
	}

	protected DefinedTerm getIdentiferType(STATE state, UUID uuid, String label, String text, String labelAbbrev, TermVocabulary<DefinedTerm> voc){
		if (uuid == null){
			uuid = UUID.randomUUID();
		}
		DefinedTerm identifierType = state.getIdentifierType(uuid);
		if (identifierType == null){
			identifierType = (DefinedTerm)getTermService().find(uuid);
			if (identifierType == null){
				identifierType = DefinedTerm .NewIdentifierTypeInstance(text, label, labelAbbrev);
				identifierType.setUuid(uuid);
				if (voc == null){
					boolean isOrdered = false;
					voc = getVocabulary(TermType.IdentifierType, uuidUserDefinedIdentifierTypeVocabulary, "User defined vocabulary for identifier types", "User Defined Identifier Types", null, null, isOrdered, identifierType);
				}
				voc.addTerm(identifierType);
				getTermService().saveOrUpdate(identifierType);
			}
			state.putIdentifierType(identifierType);
		}
		return identifierType;
	}


	protected MarkerType getMarkerType(STATE state, String keyString) {
		IInputTransformer transformer = state.getTransformer();
		MarkerType markerType = null;
		try {
			markerType = transformer.getMarkerTypeByKey(keyString);
		} catch (UndefinedTransformerMethodException e) {
			logger.info("getMarkerTypeByKey not yet implemented for this import");
		}
		if (markerType == null ){
			UUID uuid;
			try {
				uuid = transformer.getMarkerTypeUuid(keyString);
				return getMarkerType(state, uuid, keyString, keyString, keyString);
			} catch (UndefinedTransformerMethodException e) {
				logger.warn("getMarkerTypeUuid not yet implemented for this import");
			}
		}
		return null;
	}

	protected MarkerType getMarkerType(STATE state, UUID uuid, String label, String description, String labelAbbrev){
		return getMarkerType(state, uuid, label, description, labelAbbrev, null, null);
	}

	protected MarkerType getMarkerType(STATE state, UUID uuid, String label, String description, String labelAbbrev, TermVocabulary<MarkerType> voc){
	    return this.getMarkerType(state, uuid, label, description, labelAbbrev, voc, null);
	}


	protected MarkerType getMarkerType(STATE state, UUID uuid, String label, String description, String labelAbbrev, TermVocabulary<MarkerType> voc, Language language){
		if (uuid == null){
			uuid = UUID.randomUUID();
		}
		MarkerType markerType = state.getMarkerType(uuid);
		if (markerType == null){
			markerType = (MarkerType)getTermService().find(uuid);
			if (markerType == null){
				markerType = MarkerType.NewInstance(description, label, labelAbbrev);
				if (language != null){
				    markerType.getRepresentations().iterator().next().setLanguage(language);
				}
				markerType.setUuid(uuid);
				if (voc == null){
					boolean isOrdered = false;
					voc = getVocabulary(TermType.MarkerType, uuidUserDefinedMarkerTypeVocabulary, "User defined vocabulary for marker types", "User Defined Marker Types", null, null, isOrdered, markerType);
				}
				voc.addTerm(markerType);
				getTermService().save(markerType);
			}
			state.putMarkerType(markerType);
		}
		return markerType;
	}

	protected AnnotationType getAnnotationType(STATE state, UUID uuid, String label, String text, String labelAbbrev, TermVocabulary<AnnotationType> voc){
		if (uuid == null){
			uuid = UUID.randomUUID();
		}
		AnnotationType annotationType = state.getAnnotationType(uuid);
		if (annotationType == null){
			annotationType = (AnnotationType)getTermService().find(uuid);
			if (annotationType == null){
				annotationType = AnnotationType.NewInstance(label, text, labelAbbrev);
				annotationType.setUuid(uuid);
				if (voc == null){
					boolean isOrdered = false;
					voc = getVocabulary(TermType.AnnotationType, uuidUserDefinedAnnotationTypeVocabulary, "User defined vocabulary for annotation types", "User Defined Annotation Types", null, null, isOrdered, annotationType);
				}

				voc.addTerm(annotationType);
				getTermService().save(annotationType);
			}
			state.putAnnotationType(annotationType);
		}
		return annotationType;
	}


	protected ReferenceSystem getReferenceSystem(STATE state, UUID uuid, String label, String text, String labelAbbrev, TermVocabulary voc){
		if (uuid == null){
			uuid = UUID.randomUUID();
		}
		ReferenceSystem refSystem = state.getReferenceSystem(uuid);
		if (refSystem == null){
			refSystem = (ReferenceSystem)getTermService().find(uuid);
			if (refSystem == null){
				refSystem = ReferenceSystem.NewInstance(text, label, labelAbbrev);
				if (voc == null){
					boolean isOrdered = false;
					voc = getVocabulary(TermType.ReferenceSystem, uuidUserDefinedReferenceSystemVocabulary, "User defined vocabulary for reference systems", "User Defined Reference System", null, null, isOrdered, refSystem);
				}
				voc.addTerm(refSystem);
				refSystem.setUuid(uuid);
				getTermService().save(refSystem);
			}
			state.putReferenceSystem(refSystem);
		}
		return refSystem;

	}



	protected Rank getRank(STATE state, UUID uuid, String label, String text, String labelAbbrev,OrderedTermVocabulary<Rank> voc, Rank lowerRank, RankClass rankClass){
		if (uuid == null){
			uuid = UUID.randomUUID();
		}
		Rank rank = state.getRank(uuid);
		if (rank == null){
			rank = (Rank)getTermService().find(uuid);
			if (rank == null){
				rank = Rank.NewInstance(rankClass, text, label, labelAbbrev);
				if (voc == null){
					boolean isOrdered = true;
					voc = (OrderedTermVocabulary)getVocabulary(TermType.Rank, uuidUserDefinedRankVocabulary, "User defined vocabulary for ranks", "User Defined Reference System", null, null, isOrdered, rank);
				}
				if (lowerRank == null){
					voc.addTerm(rank);
				}else{
					voc.addTermAbove(rank, lowerRank);
				}
				rank.setUuid(uuid);
				getTermService().save(rank);
			}
			state.putRank(rank);
		}
		return rank;

	}

	/**
	 * Returns a named area for a given uuid by first . If the named area does not
	 * @param state
	 * @param uuid
	 * @param label
	 * @param text
	 * @param labelAbbrev
	 * @param areaType
	 * @param level
	 * @return
	 */
	protected NamedArea getNamedArea(STATE state, UUID uuid, String label, String text, String labelAbbrev, NamedAreaType areaType, NamedAreaLevel level){
		return getNamedArea(state, uuid, label, text, labelAbbrev, areaType, level, null, null);
	}

	protected NamedArea getNamedArea(STATE state, UUID uuid, String label, String text, String labelAbbrev, NamedAreaType areaType, NamedAreaLevel level, TermVocabulary voc, TermMatchMode matchMode){
		return getNamedArea(state, uuid, label, text, labelAbbrev, areaType, level, voc, matchMode, null);
	}


	protected NamedArea getNamedArea(STATE state, UUID uuid, String label, String text, String labelAbbrev, NamedAreaType areaType, NamedAreaLevel level, TermVocabulary voc, TermMatchMode matchMode,
			List<TermVocabulary<NamedArea>> vocabularyPreference){
		Class<NamedArea> clazz = NamedArea.class;
		if (uuid == null){
			uuid = UUID.randomUUID();
		}
		if (matchMode == null){
			matchMode = TermMatchMode.UUID_ONLY;
		}
		NamedArea namedArea = state.getNamedArea(uuid);
		if (namedArea == null){
			DefinedTermBase<?> term = getTermService().find(uuid);
			namedArea = CdmBase.deproxy(term,NamedArea.class);

			if (vocabularyPreference == null){
				vocabularyPreference =  new ArrayList<TermVocabulary<NamedArea>>();
			}
			if (vocabularyPreference.isEmpty()){  //add TDWG vocabulary if preferences are empty
				vocabularyPreference.add(Country.GERMANY().getVocabulary());
				vocabularyPreference.add(TdwgAreaProvider.getAreaByTdwgAbbreviation("GER").getVocabulary());
			}


			//TODO matching still experimental
			if (namedArea == null && (matchMode.equals(TermMatchMode.UUID_LABEL) || matchMode.equals(TermMatchMode.UUID_LABEL_ABBREVLABEL ))){
				//TODO test
				Pager<NamedArea> areaPager = (Pager)getTermService().findByTitle(clazz, label, null, null, null, null, null, null);
				namedArea = findBestMatchingArea(areaPager, uuid, label, text, labelAbbrev, vocabularyPreference);
			}
			if (namedArea == null && (matchMode.equals(TermMatchMode.UUID_ABBREVLABEL) || matchMode.equals(TermMatchMode.UUID_LABEL_ABBREVLABEL))){
				Pager<NamedArea> areaPager = getTermService().findByRepresentationAbbreviation(labelAbbrev, clazz, null, null);
				namedArea = findBestMatchingArea(areaPager, uuid, label, text, labelAbbrev, vocabularyPreference);
			}

			if (namedArea == null){
				namedArea = NamedArea.NewInstance(text, label, labelAbbrev);
				if (voc == null){
					boolean isOrdered = true;
					voc = getVocabulary(TermType.NamedArea, uuidUserDefinedNamedAreaVocabulary, "User defined vocabulary for named areas", "User Defined Named Areas", null, null, isOrdered, namedArea);
				}
				voc.addTerm(namedArea);
				namedArea.setType(areaType);
				namedArea.setLevel(level);
				namedArea.setUuid(uuid);
				getTermService().saveOrUpdate(namedArea);
			}
			state.putNamedArea(namedArea);
		}
		return namedArea;
	}


	private NamedArea findBestMatchingArea(Pager<NamedArea> areaPager, UUID uuid, String label, String text, String abbrev, List<TermVocabulary<NamedArea>> vocabularyPreference) {
		// TODO preliminary implementation
		List<NamedArea> list = areaPager.getRecords();
		if (list.size() == 0){
			return null;
		}else if (list.size() == 1){
			return list.get(0);
		}else if (list.size() > 1){
			List<NamedArea> preferredList = new ArrayList<>();
			for (TermVocabulary<NamedArea> voc: vocabularyPreference){
				for (NamedArea area : list){
					if (voc.equals(area.getVocabulary())){
						preferredList.add(area);
					}
				}
				if (preferredList.size() > 0){
					break;
				}
			}
			if (preferredList.size() > 1 ){
				preferredList = getHighestLevelAreas(preferredList);
			}else if (preferredList.size() == 0 ){
				preferredList = list;
			}
			if (preferredList.size() == 1 ){
				return preferredList.get(0);
			}else if (preferredList.size() > 1 ){
				String message = "There is more than 1 matching area for %s, %s, %s. As a preliminary implementation I take the first";
				message = String.format(message, label, abbrev, text);
				logger.warn(message);
				return list.get(0);
			}
		}
		return null;
	}


	private List<NamedArea> getHighestLevelAreas(List<NamedArea> preferredList) {
		List<NamedArea> result = new ArrayList<>();
		for (NamedArea area : preferredList){
			if (result.isEmpty()){
				result.add(area);
			}else {
				int compare = compareAreaLevel(area, result.get(0));
				if (compare > 0){
					result = new ArrayList<>();
					result.add(area);
				}else if (compare == 0){
					result.add(area);
				}else{
					//do nothing
				}
			}
		}

		return result;
	}


	private int compareAreaLevel(NamedArea area1, NamedArea area2) {
		NamedAreaLevel level1 = area1.getLevel();
		NamedAreaLevel level2 = area2.getLevel();
		if (level1 == null){
			return (level2 == null)? 0 : 1;
		}else if (level2 == null){
			return -1;
		}else{
			return level1.compareTo(level2);
		}
	}


	protected NamedAreaLevel getNamedAreaLevel(STATE state, UUID uuid, String label, String text, String labelAbbrev, TermVocabulary<NamedAreaLevel> voc){
		if (uuid == null){
			uuid = UUID.randomUUID();
		}
		NamedAreaLevel namedAreaLevel = state.getNamedAreaLevel(uuid);
		if (namedAreaLevel == null){
			//TODO propPath just for testing
			List<String> propPath = Arrays.asList("vocabulary");
			DefinedTermBase<NamedAreaLevel> term = getTermService().load(uuid, propPath);
			namedAreaLevel = CdmBase.deproxy(term, NamedAreaLevel.class);
			if (namedAreaLevel == null){
				namedAreaLevel = NamedAreaLevel.NewInstance(text, label, labelAbbrev);
				if (voc == null){
					boolean isOrdered = true;
					voc = getVocabulary(TermType.NamedAreaLevel, uuidUserDefinedNamedAreaLevelVocabulary, "User defined vocabulary for named area levels", "User Defined Named Area Levels", null, null, isOrdered, namedAreaLevel);
				}
				//FIXME only for debugging
				Set<NamedAreaLevel> terms = voc.getTerms();
				for (NamedAreaLevel level : terms){
					TermVocabulary<NamedAreaLevel> levelVoc = level.getVocabulary();
					if (levelVoc == null){
						logger.error("ONLY FOR DEBUG: Level voc is null");
					}else{
						logger.info("ONLY FOR DEBUG: Level voc is not null");
					}
				}
				voc.addTerm(namedAreaLevel);
				namedAreaLevel.setUuid(uuid);
				getTermService().save(namedAreaLevel);
			}
			state.putNamedAreaLevel(namedAreaLevel);
		}
		return namedAreaLevel;
	}

	/**
	 * Returns a {@link State} if it exists. <code>null</code> otherwise.
	 * @param state
	 * @param uuid
	 * @return {@link State}
	 */
	protected State getStateTerm(STATE state, UUID uuid){
		return getStateTerm(state, uuid, null, null, null, null);
	}


	/**
	 * Returns a {@link State} for a given uuid by first checking if the uuid has already been used in this import, if not
	 * checking if the state exists in the database, if not creating it anew (with vocabulary etc.).
	 * If label, text and labelAbbrev are all <code>null</code> no state is created.
	 * @param importState
	 * @param uuid
	 * @param label
	 * @param text
	 * @param labelAbbrev
	 * @param voc
	 * @return
	 */
	protected State getStateTerm(STATE importState, UUID uuid, String label, String text, String labelAbbrev, OrderedTermVocabulary<State> voc) {
		if (uuid == null){
			return null;
		}
		State stateTerm = importState.getStateTerm(uuid);
		if (stateTerm == null){
			stateTerm = CdmBase.deproxy(getTermService().find(uuid), State.class);
			if (stateTerm == null && ! hasNoLabel(label, text, labelAbbrev)){
				stateTerm = State.NewInstance(text, label, labelAbbrev);
				stateTerm.setUuid(uuid);
				if (voc == null){
					boolean isOrdered = true;
					TermVocabulary<State> orderedVoc = getVocabulary(TermType.State, uuidUserDefinedStateVocabulary, "User defined vocabulary for states used by Categorical Data", "User Defined States", null, null, isOrdered, stateTerm);
					voc = CdmBase.deproxy(orderedVoc, OrderedTermVocabulary.class);
				}
				voc.addTerm(stateTerm);
				getTermService().save(stateTerm);
			}else if (stateTerm == null){
                logger.warn("No label provided for new state with uuid " + uuid);
            }
			importState.putStateTerm(stateTerm);
		}
		return stateTerm;
	}

	/**
	 * Returns a feature if it exists, null otherwise.
	 * @see #getFeature(ImportStateBase, UUID, String, String, String, TermVocabulary)
	 * @param state
	 * @param uuid
	 * @return
	 */
	protected Feature getFeature(STATE state, UUID uuid){
		return getFeature(state, uuid, null, null, null, null);
	}

	/**
	 * Returns a feature for a given uuid by first checking if the uuid has already been used in this import, if not
	 * checking if the feature exists in the database, if not creating it anew (with vocabulary etc.).
	 * If label, text and labelAbbrev are all <code>null</code> no feature is created.
	 * @param state
	 * @param uuid
	 * @param label
	 * @param text
	 * @param labelAbbrev
	 * @return
	 */
	protected Feature getFeature(STATE state, UUID uuid, String label, String description, String labelAbbrev, TermVocabulary<Feature> voc){
		if (uuid == null){
			return null;
		}
		Feature feature = state.getFeature(uuid);
		if (feature == null){
			feature = (Feature)getTermService().find(uuid);
			if (feature == null && ! hasNoLabel(label, description, labelAbbrev)){
				feature = Feature.NewInstance(description, label, labelAbbrev);
				feature.setUuid(uuid);
				feature.setSupportsTextData(true);
//				UUID uuidFeatureVoc = UUID.fromString("b187d555-f06f-4d65-9e53-da7c93f8eaa8");
				if (voc == null){
					boolean isOrdered = false;
					voc = getVocabulary(TermType.Feature, uuidUserDefinedFeatureVocabulary, "User defined vocabulary for features", "User Defined Features", null, null, isOrdered, feature);
				}
				voc.addTerm(feature);
				getTermService().save(feature);
			}
			state.putFeature(feature);
		}
		return feature;
	}

	protected DefinedTerm getKindOfUnit(STATE state, UUID uuid, String label, String description, String labelAbbrev, TermVocabulary<DefinedTerm> voc){
		if (uuid == null){
		    uuid = UUID.randomUUID();
		}
		DefinedTerm unit = state.getKindOfUnit(uuid);
		if (unit == null){
			unit = (DefinedTerm)getTermService().find(uuid);
			if (unit == null && ! hasNoLabel(label, description, labelAbbrev)){
				unit = DefinedTerm.NewKindOfUnitInstance(description, label, labelAbbrev);
				unit.setUuid(uuid);
				if (voc == null){
					boolean isOrdered = false;
					voc = getVocabulary(TermType.KindOfUnit, uuidUserDefinedKindOfUnitVocabulary, "User defined vocabulary for kind-of-units", "User Defined Measurement kind-of-units", null, null, isOrdered, unit);
				}
				voc.addTerm(unit);
				getTermService().save(unit);
			}
			state.putKindOfUnit(unit);
		}
		return unit;
	}

	/**
	 * Returns a {@link MeasurementUnit} for a given uuid by first checking if the uuid has already been used in this import, if not
	 * checking if the {@link MeasurementUnit} exists in the database, if not creating it anew (with vocabulary etc.).
	 * If label, text and labelAbbrev are all <code>null</code> no {@link MeasurementUnit} is created.
	 * @param state
	 * @param uuid
	 * @param label
	 * @param text
	 * @param labelAbbrev
	 * @return
	 */
	protected MeasurementUnit getMeasurementUnit(STATE state, UUID uuid, String label, String description, String labelAbbrev, TermVocabulary<MeasurementUnit> voc){
		if (uuid == null){
			return null;
		}
		MeasurementUnit unit = state.getMeasurementUnit(uuid);
		if (unit == null){
			unit = (MeasurementUnit)getTermService().find(uuid);
			if (unit == null && ! hasNoLabel(label, description, labelAbbrev)){
				unit = MeasurementUnit.NewInstance(description, label, labelAbbrev);
				unit.setUuid(uuid);
				if (voc == null){
					boolean isOrdered = false;
					voc = getVocabulary(TermType.MeasurementUnit, uuidUserDefinedMeasurementUnitVocabulary, "User defined vocabulary for measurement units", "User Defined Measurement Units", null, null, isOrdered, unit);
				}
				voc.addTerm(unit);
				getTermService().save(unit);
			}
			state.putMeasurementUnit(unit);
		}
		return unit;
	}

	/**
	 * Returns a {@link StatisticalMeasure} for a given uuid by first checking if the uuid has already been used in this import, if not
	 * checking if the {@link StatisticalMeasure} exists in the database, if not creating it anew (with vocabulary etc.).
	 * If label, text and labelAbbrev are all <code>null</code> no {@link StatisticalMeasure} is created.
	 * @param state
	 * @param uuid
	 * @param label
	 * @param text
	 * @param labelAbbrev
	 * @return
	 */
	protected StatisticalMeasure getStatisticalMeasure(STATE state, UUID uuid, String label, String description, String labelAbbrev, TermVocabulary<StatisticalMeasure> voc){
		if (uuid == null){
			return null;
		}
		StatisticalMeasure statisticalMeasure = state.getStatisticalMeasure(uuid);
		if (statisticalMeasure == null){
			statisticalMeasure = (StatisticalMeasure)getTermService().find(uuid);
			if (statisticalMeasure == null && ! hasNoLabel(label, description, labelAbbrev)){
				statisticalMeasure = StatisticalMeasure.NewInstance(description, label, labelAbbrev);
				statisticalMeasure.setUuid(uuid);
				if (voc == null){
					boolean isOrdered = false;
					voc = getVocabulary(TermType.StatisticalMeasure, uuidUserDefinedStatisticalMeasureVocabulary, "User defined vocabulary for statistical measures", "User Defined Statistical Measures", null, null, isOrdered, statisticalMeasure);
				}
				voc.addTerm(statisticalMeasure);
				getTermService().save(statisticalMeasure);
			}
			state.putStatisticalMeasure(statisticalMeasure);
		}
		return statisticalMeasure;
	}

	/**
	 * Returns a {@link Modifier} for a given uuid by first checking if the uuid has already been used in this import, if not
	 * checking if the {@link Modifier} exists in the database, if not creating it anew (with vocabulary etc.).
	 * If label, text and labelAbbrev are all <code>null</code> no {@link Modifier} is created.
	 * @param state
	 * @param uuid
	 * @param label
	 * @param text
	 * @param labelAbbrev
	 * @return
	 */
	protected DefinedTerm getModifier(STATE state, UUID uuid, String label, String description, String labelAbbrev, TermVocabulary<DefinedTerm> voc){
		if (uuid == null){
			return null;
		}
		DefinedTerm modifier = state.getModifier(uuid);
		if (modifier == null){
			modifier = (DefinedTerm)getTermService().find(uuid);
			if (modifier == null && ! hasNoLabel(label, description, labelAbbrev)){
				modifier = DefinedTerm.NewModifierInstance(description, label, labelAbbrev);
				modifier.setUuid(uuid);
				if (voc == null){
					boolean isOrdered = false;
					voc = getVocabulary(TermType.Modifier, uuidUserDefinedModifierVocabulary, "User defined vocabulary for modifier", "User Defined Modifier", null, null, isOrdered, modifier);
				}
				voc.addTerm(modifier);
				getTermService().save(modifier);
			}
			state.putModifier(modifier);
		}
		return modifier;
	}

	/**
	 * Returns a taxon relationship type for a given uuid by first checking if the uuid has already been used in this import, if not
	 * checking if the taxon relationship type exists in the database, if not creating it anew (with vocabulary etc.).
	 * If label, text and labelAbbrev are all <code>null</code> no taxon relationship type is created.
	 * @param state
	 * @param uuid
	 * @param label
	 * @param text
	 * @param labelAbbrev
	 * @return
	 */
	protected TaxonRelationshipType getTaxonRelationshipType(STATE state, UUID uuid, String label, String text, String labelAbbrev, TermVocabulary<TaxonRelationshipType> voc){
		if (uuid == null){
			return null;
		}
		TaxonRelationshipType relType = state.getTaxonRelationshipType(uuid);
		if (relType == null){
			relType = (TaxonRelationshipType)getTermService().find(uuid);
			if (relType == null && ! hasNoLabel(label, text, labelAbbrev)){
				relType = TaxonRelationshipType.NewInstance(text, label, labelAbbrev, false, false);
				relType.setUuid(uuid);
				if (voc == null){
					boolean isOrdered = true;
					voc = getVocabulary(TermType.TaxonRelationshipType, uuidUserDefinedTaxonRelationshipTypeVocabulary, "User defined vocabulary for taxon relationship types", "User Defined Taxon Relationship Types", null, null, isOrdered, relType);
				}
				voc.addTerm(relType);
				getTermService().save(relType);
			}
			state.putTaxonRelationshipType(relType);
		}
		return relType;
	}

	private boolean hasNoLabel(String label, String text, String labelAbbrev) {
		return label == null && text == null && labelAbbrev == null;
	}

	protected PresenceAbsenceTerm getPresenceTerm(STATE state, UUID uuid, String label, String text, String labelAbbrev, boolean isAbsenceTerm){
	    return getPresenceTerm(state, uuid, label, text, labelAbbrev, isAbsenceTerm, null);
	}


	/**
	 * Returns a presence term for a given uuid by first ...
	 * @param state
	 * @param uuid
	 * @param label
	 * @param text
	 * @param labelAbbrev
	 * @return
	 */
	protected PresenceAbsenceTerm getPresenceTerm(STATE state, UUID uuid, String label, String text, String labelAbbrev, boolean isAbsenceTerm, TermVocabulary<PresenceAbsenceTerm> voc){
		if (uuid == null){
			return null;
		}
		PresenceAbsenceTerm presenceTerm = state.getPresenceAbsenceTerm(uuid);
		if (presenceTerm == null){
			presenceTerm = (PresenceAbsenceTerm)getTermService().find(uuid);
			if (presenceTerm == null){
				presenceTerm = PresenceAbsenceTerm.NewPresenceInstance(text, label, labelAbbrev);
				presenceTerm.setUuid(uuid);
				presenceTerm.setAbsenceTerm(isAbsenceTerm);
				//set vocabulary ; FIXME use another user-defined vocabulary
				if (voc == null){
                    boolean isOrdered = true;
                    voc = getVocabulary(TermType.PresenceAbsenceTerm, uuidUserDefinedPresenceAbsenceVocabulary, "User defined vocabulary for distribution status", "User Defined Distribution Status", null, null, isOrdered, presenceTerm);
                }
				voc.addTerm(presenceTerm);
				getTermService().save(presenceTerm);
			}
			state.putPresenceAbsenceTerm(presenceTerm);
		}
		return presenceTerm;
	}

	/**
	 * Returns a language for a given uuid by first ...
	 * @param state
	 * @param uuid
	 * @param label
	 * @param text
	 * @param labelAbbrev
	 * @return
	 */
	protected Language getLanguage(STATE state, UUID uuid, String label, String text, String labelAbbrev){
		return getLanguage(state, uuid, label, text, labelAbbrev, null);
	}

	protected Language getLanguage(STATE state, UUID uuid, String label, String text, String labelAbbrev, TermVocabulary voc){
		if (uuid == null){
			return null;
		}
		Language language = state.getLanguage(uuid);
		if (language == null){
			language = (Language)getTermService().find(uuid);
			if (language == null){
				language = Language.NewInstance(text, label, labelAbbrev);

				language.setUuid(uuid);
				if (voc == null){
					UUID uuidLanguageVoc = UUID.fromString("463a96f1-20ba-4a4c-9133-854c1682bd9b");
					boolean isOrdered = false;
					voc = getVocabulary(TermType.Language, uuidLanguageVoc, "User defined languages", "User defined languages", "User defined languages", null, isOrdered, language);
				}
				//set vocabulary ; FIXME use another user-defined vocabulary

				voc.addTerm(language);
				getTermService().save(language);
			}
			state.putLanguage(language);
		}
		return language;
	}


	/**
	 * @param uuid
	 * @return
	 *
	 */
	protected <T extends DefinedTermBase> TermVocabulary<T> getVocabulary(TermType termType, UUID uuid, String description, String label, String abbrev, URI termSourceUri, boolean isOrdered, T type) {
		List<String> propPath = Arrays.asList(new String[]{"terms"});
		TermVocabulary<T> voc = getVocabularyService().load(uuid, propPath);
		if (voc == null){
			if (isOrdered){
				voc = OrderedTermVocabulary.NewInstance(termType, description, label, abbrev, termSourceUri);
			}else{
				voc = TermVocabulary.NewInstance(termType, description, label, abbrev, termSourceUri);
			}
			voc.setUuid(uuid);
			getVocabularyService().save(voc);
		}
		return voc;
	}

	/**
	 * Adds an orginal source to a sourceable objects (implemented for Identifiable entity and description element.
	 * If cdmBase is not sourceable nothing happens.
	 * TODO Move to DbImportBase once this exists.
	 * TODO also implemented in DbImportObjectCreationMapper (reduce redundance)
	 * @param rs
	 * @param cdmBase
	 * @param dbIdAttribute
	 * @param namespace
	 * @param citation
	 * @throws SQLException
	 */
	public void addOriginalSource(ICdmBase cdmBase, Object idAttributeValue, String namespace, Reference citation)  {
		if (cdmBase instanceof ISourceable ){
			IOriginalSource source;
			ISourceable sourceable = (ISourceable<?>)cdmBase;
			Object id = idAttributeValue;
			String strId = String.valueOf(id);
			String microCitation = null;
			OriginalSourceType type = OriginalSourceType.Import;
			if (cdmBase instanceof IdentifiableEntity){
				source = IdentifiableSource.NewInstance(type, strId, namespace, citation, microCitation);
			}else if (cdmBase instanceof DescriptionElementBase){
				source = DescriptionElementSource.NewInstance(type, strId, namespace, citation, microCitation);
			}else{
				logger.warn("ISourceable not beeing identifiable entities or description element base are not yet supported. CdmBase is of type " + cdmBase.getClass().getName() + ". Original source not added.");
				return;
			}
			sourceable.addSource(source);
		}else if (cdmBase != null){
			logger.warn("Sourced object does not implement ISourceable: " + cdmBase.getClass() + "," + cdmBase.getUuid());
		}else{
			logger.warn("Sourced object is null");
		}
	}

	/**
	 * @see #addOriginalSource(CdmBase, Object, String, Reference)
	 * @param rs
	 * @param cdmBase
	 * @param dbIdAttribute
	 * @param namespace
	 * @param citation
	 * @throws SQLException
	 */
	public void addOriginalSource(ResultSet rs, CdmBase cdmBase, String dbIdAttribute, String namespace, Reference citation) throws SQLException {
		Object id = rs.getObject(dbIdAttribute);
		addOriginalSource(cdmBase, id, namespace, citation);
	}


	/**
	 * If the child taxon is missing genus or species epithet information and the rank is below <i>genus</i>
	 * or <i>species</i> respectively the according epithets are taken from the parent taxon.
	 * If the name is an autonym and has no combination author/basionym author the authors are taken from
	 * the parent.
	 * @param parentTaxon
	 * @param childTaxon
	 */
	protected void fillMissingEpithetsForTaxa(Taxon parentTaxon, Taxon childTaxon) {
		if (parentTaxon == null){
			logger.warn("Parent taxon is null. Missing name parts can not be taken from parent");
			return;
		}
		INonViralName parentName = parentTaxon.getName();
		INonViralName childName = childTaxon.getName();
		fillMissingEpithets(parentName, childName);
	}

	/**
	 * If the child name is missing genus or species epithet information and the rank is below <i>genus</i>
	 * or <i>species</i> respectively the according epithets are taken from the parent name.
	 * If the name is an autonym and has no combination author/basionym author the authors are taken from
	 * the parent.
	 * @param parentTaxon
	 * @param childTaxon
	 */
	protected void fillMissingEpithets(INonViralName parentName, INonViralName childName) {
		if (StringUtils.isBlank(childName.getGenusOrUninomial()) && childName.getRank().isLower(Rank.GENUS()) ){
			childName.setGenusOrUninomial(parentName.getGenusOrUninomial());
		}

		if (StringUtils.isBlank(childName.getSpecificEpithet()) && childName.getRank().isLower(Rank.SPECIES()) ){
			childName.setSpecificEpithet(parentName.getSpecificEpithet());
		}
		if (childName.isAutonym() && childName.getCombinationAuthorship() == null && childName.getBasionymAuthorship() == null ){
			childName.setCombinationAuthorship(parentName.getCombinationAuthorship());
			childName.setBasionymAuthorship(parentName.getBasionymAuthorship());
		}
	}

	/**
	 * Returns the taxon description for a taxon. If there are multiple taxon descriptions
	 * an arbitrary one is chosen.
	 * If no taxon description exists, a new one is created if <code>createNewIfNotExists</code>
	 * is <code>true</code>.
	 * @param createNewIfNotExists
	 * @param isImageGallery if true only taxon description being image galleries are considered.
	 * If false only taxon description being no image galleries are considered.
	 * @return
	 */
	public TaxonNameDescription getTaxonNameDescription(TaxonName name, boolean isImageGallery, boolean createNewIfNotExists) {
		Reference ref = null;
		return getTaxonNameDescription(name, ref, isImageGallery, createNewIfNotExists);
	}

	/**
	 * Like {@link #getTaxonDescription(Taxon, boolean, boolean)}
	 * Only matches a description if the given reference is a source of the description.<BR>
	 * If a new description is created the given reference will be added as a source.
	 *
	 * @see #getTaxonDescription(Taxon, boolean, boolean)
	 */
	public TaxonNameDescription getTaxonNameDescription(TaxonName<?,?> name, Reference ref, boolean isImageGallery, boolean createNewIfNotExists) {
		TaxonNameDescription result = null;
		Set<TaxonNameDescription> descriptions= name.getDescriptions();
		for (TaxonNameDescription description : descriptions){
			if (description.isImageGallery() == isImageGallery){
				if (hasCorrespondingSource(ref, description)){
					result = description;
					break;
				}
			}
		}
		if (result == null && createNewIfNotExists){
			result = TaxonNameDescription.NewInstance(name);
			result.setImageGallery(isImageGallery);
			if (ref != null){
				result.addImportSource(null, null, ref, null);
			}
		}
		return result;
	}

	/**
	 * Returns the taxon description for a taxon. If there are multiple taxon descriptions
	 * an arbitrary one is chosen.
	 * If no taxon description exists, a new one is created if <code>createNewIfNotExists</code>
	 * is <code>true</code>.
	 * @param createNewIfNotExists
	 * @param isImageGallery if true only taxon description being image galleries are considered.
	 * If false only taxon description being no image galleries are considered.
	 * @return
	 */
	public TaxonDescription getTaxonDescription(Taxon taxon, boolean isImageGallery, boolean createNewIfNotExists) {
		Reference ref = null;
		return getTaxonDescription(taxon, ref, isImageGallery, createNewIfNotExists);
	}

	/**
	 * Like {@link #getTaxonDescription(Taxon, boolean, boolean)}
	 * Only matches a description if the given reference is a source of the description.<BR>
	 * If a new description is created the given reference will be added as a source.
	 *
	 * @see #getTaxonDescription(Taxon, boolean, boolean)
	 * @see #getDefaultTaxonDescription(Taxon, boolean, boolean, Reference)
	 */
	public TaxonDescription getTaxonDescription(Taxon taxon, Reference ref, boolean isImageGallery,
	        boolean createNewIfNotExists) {
		TaxonDescription result = null;
		Set<TaxonDescription> descriptions= taxon.getDescriptions();
		for (TaxonDescription description : descriptions){
			if (description.isImageGallery() == isImageGallery){
				if (hasCorrespondingSource(ref, description)){
					result = description;
					break;
				}
			}
		}
		if (result == null && createNewIfNotExists){
			result = TaxonDescription.NewInstance(taxon);
			result.setImageGallery(isImageGallery);
			if (ref != null){
				result.addImportSource(null, null, ref, null);
			}
		}
		return result;
	}

	/**
	 * Returns the default taxon description. If no default taxon description exists,
	 * a new one is created, the default flag is set to true and, if a source is passed,
	 * it is added to the new description. Otherwise source has no influence.
	 * @param taxon
	 * @param isImageGallery
	 * @param createNewIfNotExists
	 * @param source
	 * @return the default description
	 * @see #getTaxonDescription(Taxon, Reference, boolean, boolean)
	 */
	public TaxonDescription getDefaultTaxonDescription(Taxon taxon, boolean isImageGallery,
            boolean createNewIfNotExists, Reference source) {
        TaxonDescription result = null;
        Set<TaxonDescription> descriptions= taxon.getDescriptions();
        for (TaxonDescription description : descriptions){
            if (description.isImageGallery() == isImageGallery){
                if (description.isDefault()){
                    result = description;
                    break;
                }
            }
        }
        if (result == null && createNewIfNotExists){
            result = TaxonDescription.NewInstance(taxon);
            result.setImageGallery(isImageGallery);
            result.setDefault(true);
            if (source != null){
                result.addImportSource(null, null, source, null);
            }
        }
        return result;
    }

    /**
     * Returns the taxon description with marked as <code>true</code> with the given marker type.
     * If createNewIfNotExists a new description is created if it does not yet exist.
     * For the new description the source and the title are set if not <code>null</code>.
     * @param taxon
     * @param markerType
     * @param isImageGallery
     * @param createNewIfNotExists
     * @param source
     * @param title
     * @return the existing or new taxon description
     */
   public TaxonDescription getMarkedTaxonDescription(Taxon taxon, MarkerType markerType, boolean isImageGallery,
            boolean createNewIfNotExists, Reference source, String title) {
        TaxonDescription result = null;
        Set<TaxonDescription> descriptions= taxon.getDescriptions();
        for (TaxonDescription description : descriptions){
            if (description.isImageGallery() == isImageGallery){
                if (description.hasMarker(markerType, true)){
                    result = description;
                    break;
                }
            }
        }
        if (result == null && createNewIfNotExists){
            result = TaxonDescription.NewInstance(taxon);
            result.setImageGallery(isImageGallery);
            result.addMarker(Marker.NewInstance(markerType, true));
            if (source != null){
                result.addImportSource(null, null, source, null);
            }
            if (isNotBlank(title)){
                result.setTitleCache(title, true);
            }
        }
        return result;
    }


	/**
	 * Returns the {@link SpecimenDescription specimen description} for a {@link SpecimenOrObservationBase specimen or observation}.
	 * If there are multiple specimen descriptions an arbitrary one is chosen.
	 * If no specimen description exists, a new one is created if <code>createNewIfNotExists</code> is <code>true</code>.
	 * @param createNewIfNotExists
	 * @param isImageGallery if true only specimen description being image galleries are considered.
	 * If false only specimen description being no image galleries are considered.
	 * @return
	 */
	public SpecimenDescription getSpecimenDescription(SpecimenOrObservationBase specimen, boolean isImageGallery, boolean createNewIfNotExists) {
		Reference ref = null;
		return getSpecimenDescription(specimen, ref, isImageGallery, createNewIfNotExists);
	}

	/**
	 * Like {@link #getSpecimenDescription(SpecimenOrObservationBase, boolean, boolean)}
	 * Only matches a description if the given reference is a source of the description.<BR>
	 * If a new description is created the given reference will be added as a source.
	 *
	 * @see #getTaxonDescription(Taxon, boolean, boolean)
	 */
	public SpecimenDescription getSpecimenDescription(SpecimenOrObservationBase specimen, Reference ref, boolean isImageGallery, boolean createNewIfNotExists) {
		SpecimenDescription result = null;
		Set<SpecimenDescription> descriptions= specimen.getDescriptions();
		for (SpecimenDescription description : descriptions){
			if (description.isImageGallery() == isImageGallery){
				if (hasCorrespondingSource(ref, description)){
					result = description;
					break;
				}
			}
		}
		if (result == null && createNewIfNotExists){
			result = SpecimenDescription.NewInstance(specimen);
			result.setImageGallery(isImageGallery);
			if (ref != null){
				result.addImportSource(null, null, ref, null);
			}
		}
		return result;
	}


	/**
	 * Returns the textdata that holds general information about a feature for a taxon description.
	 * This is mainly necessary for descriptions that have more than one description element for
	 * a given feature such as 'distribution', 'description' or 'common name'. It may also hold
	 * for hierarchical features where no description element exists for a higher hierarchy level.
	 * Example: the description feature has subfeatures. But some information like authorship, figures,
	 * sources need to be added to the description itself.
	 * Currently a feature placeholder is marked by a marker of type 'feature placeholder'. Maybe in future
	 * there will be a boolean marker in the TextData class itself.
	 *
	 * @param state
	 * @param feature
	 * @param taxon
	 * @param ref
	 * @param createIfNotExists
	 * @return
	 */
	protected TextData getFeaturePlaceholder(STATE state, DescriptionBase<?> description, Feature feature, boolean createIfNotExists) {
		UUID featurePlaceholderUuid = MarkupTransformer.uuidMarkerFeaturePlaceholder;
		for (DescriptionElementBase element : description.getElements()){
			if (element.isInstanceOf(TextData.class)){
				TextData textData = CdmBase.deproxy(element, TextData.class);
				if (textData.getFeature() == null || ! textData.getFeature().equals(feature)){
					continue;
				}
				for (Marker marker : textData.getMarkers()){
					MarkerType markerType = marker.getMarkerType();
					if (markerType != null &&
							markerType.getUuid().equals(featurePlaceholderUuid) &&
							marker.getValue() == true){
						return textData;
					}
				}
			}
		}
		if (createIfNotExists){
			TextData newPlaceholder = TextData.NewInstance(feature);
			MarkerType placeholderMarkerType = getMarkerType(state, featurePlaceholderUuid, "Feature Placeholder", "Feature Placeholder", null);
			Marker marker = Marker.NewInstance(placeholderMarkerType, true);
			newPlaceholder.addMarker(marker);
			description.addElement(newPlaceholder);
			return newPlaceholder;
		}else{
			return null;
		}
	}



	/**
	 * Returns true, if this description has a source with a citation equal to the given reference.
	 * Returns true if the given reference is null.
	 * @param ref
	 * @param description
	 */
	private boolean hasCorrespondingSource(Reference ref, DescriptionBase<?> description) {
		if (ref != null){
			for (IdentifiableSource source : description.getSources()){
				if (ref.equals(source.getCitation())){
					return true;
				}
			}
			return false;
		}
		return true;

	}


	/**
	 * Returns the accepted taxon of a {@link TaxonBase taxon base}. <BR>
	 * If taxonBase is of type taxon the same object is returned. If taxonBase is of type
	 * synonym the accepted taxon is returned if one exists. If no accepted taxon exists
	 * <code>null</code> is returned. If multiple accepted taxa exist the one taxon with the
	 * same secundum reference is returned. If no such single taxon exists an
	 * {@link IllegalStateException illegal state exception} is thrown.
	 * @param taxonBase
	 * @return
	 */
	protected Taxon getAcceptedTaxon(TaxonBase<?> taxonBase) {
		if (taxonBase == null){
			return null;
		}else if(taxonBase.isInstanceOf(Taxon.class)){
			return CdmBase.deproxy(taxonBase, Taxon.class);
		}else if(taxonBase.isInstanceOf(Synonym.class)){
			Synonym synonym = CdmBase.deproxy(taxonBase, Synonym.class);
			Taxon acceptedTaxon = synonym.getAcceptedTaxon();
			return acceptedTaxon;
		}else{
			throw new IllegalStateException("Unknown TaxonBase subclass: " + taxonBase.getClass().getName());
		}
	}

	protected Media getImageMedia(String uriString, boolean readMediaData) throws MalformedURLException {
	    return getImageMedia(uriString, null, readMediaData);
	}

	/**
	 * Creates
	 * @param uriString
	 * @param readDataFromUrl
	 * @see #READ_MEDIA_DATA
	 * @return
	 * @throws MalformedURLException
	 */
	protected Media getImageMedia(String uriString, String uriStrThumb, boolean readMediaData) throws MalformedURLException {
		if( uriString == null){
			return null;
		} else {
			uriString = uriString.replace(" ", "%20");  //replace whitespace
			try {
			    ImageInfo imageInfo = null;
				URI uri = new URI(uriString);

                try {
					if (readMediaData){
						logger.info("Read media data from: " + uri);
						imageInfo = ImageInfo.NewInstance(uri, 0);
					}
				} catch (Exception e) {
					String message = "An error occurred when trying to read image meta data for " + uri.toString() + ": " +  e.getMessage();
					logger.warn(message);
					fireWarningEvent(message, "unknown location", 2, 0);
				}
				ImageFile imageFile = ImageFile.NewInstance(uri, null, imageInfo);

				MediaRepresentation representation = MediaRepresentation.NewInstance();

				if(imageInfo != null){
					representation.setMimeType(imageInfo.getMimeType());
					representation.setSuffix(imageInfo.getSuffix());
				}
				representation.addRepresentationPart(imageFile);
				Media media = Media.NewInstance();
                media.addRepresentation(representation);

				if (uriStrThumb != null){
				    ImageInfo imageInfoThumb = null;
	                uriStrThumb = uriStrThumb.replace(" ", "%20");  //replace whitespace
	                URI uriThumb = new URI(uriStrThumb);
	                try {
	                    if (readMediaData){
	                        logger.info("Read media data from: " + uriThumb);
	                        imageInfoThumb = ImageInfo.NewInstance(uriThumb, 0);
	                    }
	                } catch (Exception e) {
	                    String message = "An error occurred when trying to read image meta data for " + uriThumb.toString() + ": " +  e.getMessage();
	                    logger.warn(message);
	                    fireWarningEvent(message, "unknown location", 2, 0);
	                }

	                ImageFile imageFileFhumb = ImageFile.NewInstance(uriThumb, null, imageInfoThumb);
				    MediaRepresentation reprThumb = MediaRepresentation.NewInstance();
				    if(imageInfoThumb != null){
				        reprThumb.setMimeType(imageInfoThumb.getMimeType());
				        reprThumb.setSuffix(imageInfoThumb.getSuffix());
	                }
				    reprThumb.addRepresentationPart(imageFileFhumb);
				    media.addRepresentation(reprThumb);
				}

				return media;
			} catch (URISyntaxException e1) {
				String message = "An URISyntaxException occurred when trying to create uri from multimedia objcet string: " +  uriString;
				logger.warn(message);
				fireWarningEvent(message, "unknown location", 4, 0);
				return null;
			}
		}
	}


	/**
	 * Retrieves an Integer value from a result set. If the value is NULL null is returned.
	 * ResultSet.getInt() returns 0 therefore we need a special handling for this case.
	 * @param rs
	 * @param columnName
	 * @return
	 * @throws SQLException
	 */
	protected Integer nullSafeInt(ResultSet rs, String columnName) throws SQLException {
		Object intObject = rs.getObject(columnName);
		if (intObject == null){
			return null;
		}else{
			return Integer.valueOf(intObject.toString());
		}
	}

	protected Boolean nullSafeBoolean(ResultSet rs, String columnName) throws SQLException {
		Object bitObject = rs.getObject(columnName);
		if (bitObject == null){
			return null;
		}else{
			return Boolean.valueOf(bitObject.toString());
		}
	}

	protected Double nullSafeDouble(ResultSet rs, String columnName) throws SQLException {
		Object doubleObject = rs.getObject(columnName);
		if (doubleObject == null){
			return null;
		}else{
			return Double.valueOf(doubleObject.toString());
		}
	}

	protected Float nullSafeFloat(ResultSet rs, String columnName) throws SQLException {
		Object doubleObject = rs.getObject(columnName);
		if (doubleObject == null){
			return null;
		}else{
			return Float.valueOf(doubleObject.toString());
		}
	}


	/**
	 * Returns <code>null</code> for all blank strings. Identity function otherwise.
	 * @param str
	 * @return
	 */
	protected String NB(String str) {
		if (StringUtils.isBlank(str)){
			return null;
		}else{
			return str;
		}
	}

	@Override
    public byte[] getByteArray() {
        // TODO Auto-generated method stub
        return null;
    }

	public static TeamOrPersonBase<?> parseAuthorString(String authorName){
        TeamOrPersonBase<?> author = null;
        String[] teamMembers = authorName.split(authorSeparator);
        String lastMember;
        String[] lastMembers;
        Person teamMember;
        if (teamMembers.length>1){
            lastMember = teamMembers[teamMembers.length -1];
            lastMembers = lastMember.split(lastAuthorSeparator);
            teamMembers[teamMembers.length -1] = "";
            author = Team.NewInstance();
            for(String member:teamMembers){
                if (!member.equals("")){
                    teamMember = Person.NewInstance();
                    teamMember.setTitleCache(member, true);
                   ((Team)author).addTeamMember(teamMember);
                }
            }
            if (lastMembers != null){
                for(String member:lastMembers){
                   teamMember = Person.NewInstance();
                   teamMember.setTitleCache(member, true);
                   ((Team)author).addTeamMember(teamMember);
                }
            }

        } else {
            teamMembers = authorName.split(lastAuthorSeparator);
            if (teamMembers.length>1){
                author = Team.NewInstance();
                for(String member:teamMembers){
                  teamMember = Person.NewInstance();
                  teamMember.setTitleCache(member, true);
                  ((Team)author).addTeamMember(teamMember);

                }
            }else{
                author = Person.NewInstance();
                author.setTitleCache(authorName, true);
            }
        }
        author.getTitleCache();
        return author;
    }


}
