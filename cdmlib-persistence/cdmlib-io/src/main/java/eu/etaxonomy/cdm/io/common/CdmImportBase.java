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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.media.ImageInfo;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IOriginalSource;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @created 01.07.2008
 * @version 1.0
 */
public abstract class CdmImportBase<CONFIG extends IImportConfigurator, STATE extends ImportStateBase> extends CdmIoBase<STATE> implements ICdmImport<CONFIG, STATE>{
	private static Logger logger = Logger.getLogger(CdmImportBase.class);
	
	protected static final boolean CREATE = true;
	protected static final boolean IMAGE_GALLERY = true;

	public static final UUID uuidUserDefinedNamedAreaLevelVocabulary = UUID.fromString("255144da-8d95-457e-a327-9752a8f85e5a");
	public static final UUID uuidUserDefinedNamedAreaVocabulary = UUID.fromString("b2238399-a3af-4f6d-b7eb-ff5d0899bf1b");
	public static final UUID uuidUserDefinedExtensionTypeVocabulary = UUID.fromString("e28c1394-1be8-4847-8b81-ab44eb6d5bc8");
	public static final UUID uuidUserDefinedReferenceSystemVocabulary = UUID.fromString("467591a3-10b4-4bf1-9239-f06ece33e90a");
	
	private static final String UuidOnly = "UUIDOnly";
	private static final String UuidLabel = "UUID or label";
	private static final String UuidLabelAbbrev = "UUID, label or abbreviation";
	private static final String UuidAbbrev = "UUID or abbreviation";
	
	public enum TermMatchMode{
		UUID_ONLY(0, UuidOnly)
		,UUID_LABEL(1, UuidLabel)
		,UUID_LABEL_ABBREVLABEL(2, UuidLabelAbbrev)
		,UUID_ABBREVLABEL(3, UuidAbbrev)
		;
		
		
		private int id;
		private String representation;
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
	
	protected Classification makeTree(STATE state, Reference reference){
		Reference ref = CdmBase.deproxy(reference, Reference.class);
		String treeName = "Classification (Import)";
		if (ref != null && CdmUtils.isNotEmpty(ref.getTitleCache())){
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
		state.putTree(ref, tree);
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
		if (ref != null && CdmUtils.isNotEmpty(ref.getTitleCache())){
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
	protected ExtensionType getExtensionType(STATE state, UUID uuid, String label, String text, String labelAbbrev, TermVocabulary voc){
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
					voc = getVocabulary(uuidUserDefinedExtensionTypeVocabulary, "User defined vocabulary for extension types", "User Defined Extension Types", null, null, isOrdered);
				}
				voc.addTerm(extensionType);
				getTermService().save(extensionType);
			}
			state.putExtensionType(extensionType);
		}
		return extensionType;
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
	
	protected MarkerType getMarkerType(STATE state, UUID uuid, String label, String text, String labelAbbrev){
		if (uuid == null){
			uuid = UUID.randomUUID();
		}
		MarkerType markerType = state.getMarkerType(uuid);
		if (markerType == null){
			markerType = (MarkerType)getTermService().find(uuid);
			if (markerType == null){
				markerType = MarkerType.NewInstance(label, text, labelAbbrev);
				markerType.setUuid(uuid);
				UUID uuidMarkerTypeVoc = UUID.fromString("19dffff7-e142-429c-a420-5d28e4ebe305");
				TermVocabulary voc = getVocabularyService().find(uuidMarkerTypeVoc);
				voc.addTerm(markerType);
				getTermService().save(markerType);
			}
			state.putMarkerType(markerType);
		}
		return markerType;
	}
	
	protected AnnotationType getAnnotationType(STATE state, UUID uuid, String label, String text, String labelAbbrev){
		if (uuid == null){
			uuid = UUID.randomUUID();
		}
		AnnotationType annotationType = state.getAnnotationType(uuid);
		if (annotationType == null){
			annotationType = (AnnotationType)getTermService().find(uuid);
			if (annotationType == null){
				annotationType = AnnotationType.NewInstance(label, text, labelAbbrev);
				annotationType.setUuid(uuid);
				UUID uuidAnnotationTypeVoc = UUID.fromString("ca04609b-1ba0-4d31-9c2e-aa8eb2f4e62d");
				TermVocabulary voc = getVocabularyService().find(uuidAnnotationTypeVoc);
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
					voc = getVocabulary(uuidUserDefinedReferenceSystemVocabulary, "User defined vocabulary for named areas", "User Defined Reference System", null, null, isOrdered);
				}
				voc.addTerm(refSystem);
				refSystem.setUuid(uuid);
				getTermService().save(refSystem);
			}
			state.putReferenceSystem(refSystem);
		}
		return refSystem;
		
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
		if (uuid == null){
			uuid = UUID.randomUUID();
		}
		if (matchMode == null){
			matchMode = TermMatchMode.UUID_ONLY;
		}
		NamedArea namedArea = state.getNamedArea(uuid);
		if (namedArea == null){
			//TODO matching still experimental
			namedArea = (NamedArea)getTermService().find(uuid);
			if (namedArea == null && matchMode.equals(TermMatchMode.UUID_LABEL)){
				logger.warn("UUID_LABEL not yet implemented");
			}
			if (namedArea == null && matchMode.equals(TermMatchMode.UUID_ABBREVLABEL)){
				Pager<NamedArea> areaPager = getTermService().findByRepresentationAbbreviation(labelAbbrev, NamedArea.class, null, null);
				namedArea = findBestMatchingArea(areaPager, uuid, label, text, labelAbbrev, areaType, level, voc);
			}
			if (namedArea == null && matchMode.equals(TermMatchMode.UUID_LABEL_ABBREVLABEL)){
				logger.warn("UUID_LABEL not yet implemented");
			}
			
			if (namedArea == null){
				namedArea = NamedArea.NewInstance(text, label, labelAbbrev);
				if (voc == null){
					boolean isOrdered = true;
					voc = getVocabulary(uuidUserDefinedNamedAreaVocabulary, "User defined vocabulary for named areas", "User Defined Named Areas", null, null, isOrdered);
				}
				voc.addTerm(namedArea);
				namedArea.setType(areaType);
				namedArea.setLevel(level);
				namedArea.setUuid(uuid);
				getTermService().save(namedArea);
			}
			state.putNamedArea(namedArea);
		}
		return namedArea;
	}
	
	
	private NamedArea findBestMatchingArea(Pager<NamedArea> areaPager, UUID uuid, String label, String text, String abbrev,
			NamedAreaType areaType, NamedAreaLevel level, TermVocabulary voc) {
		// TODO preliminary implementation
		List<NamedArea> list = areaPager.getRecords();
		if (list.size() == 0){
			return null;
		}else if (list.size() == 1){
			return list.get(0);
		}else if (list.size() > 1){
			String message = "There is more than 1 matching area for %s, %s, %s. As a preliminary implementation I take the first";
			message = String.format(message, label, abbrev, text);
			logger.warn(message);
			return list.get(0);
		}
		return null;
	}


	protected NamedAreaLevel getNamedAreaLevel(STATE state, UUID uuid, String label, String text, String labelAbbrev, TermVocabulary<NamedAreaLevel> voc){
		if (uuid == null){
			uuid = UUID.randomUUID();
		}
		NamedAreaLevel namedAreaLevel = state.getNamedAreaLevel(uuid);
		if (namedAreaLevel == null){
			namedAreaLevel = CdmBase.deproxy(getTermService().find(uuid), NamedAreaLevel.class);
			if (namedAreaLevel == null){
				namedAreaLevel = NamedAreaLevel.NewInstance(text, label, labelAbbrev);
				if (voc == null){
					boolean isOrdered = true;
					voc = getVocabulary(uuidUserDefinedNamedAreaLevelVocabulary, "User defined vocabulary for named area levels", "User Defined Named Area Levels", null, null, isOrdered);
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
	 * Returns a feature for a given uuid by first ...
	 * @param state
	 * @param uuid
	 * @param label
	 * @param text
	 * @param labelAbbrev
	 * @return
	 */
	protected Feature getFeature(STATE state, UUID uuid, String label, String text, String labelAbbrev){
		if (uuid == null){
			return null;
		}
		Feature feature = state.getFeature(uuid);
		if (feature == null){
			feature = (Feature)getTermService().find(uuid);
			if (feature == null){
				feature = Feature.NewInstance(text, label, labelAbbrev);
				feature.setUuid(uuid);
				feature.setSupportsTextData(true);
				//set vocabulary ; FIXME use another user-defined vocabulary
				UUID uuidFeatureVoc = UUID.fromString("b187d555-f06f-4d65-9e53-da7c93f8eaa8"); 
				TermVocabulary<Feature> voc = getVocabularyService().find(uuidFeatureVoc);
				voc.addTerm(feature);
				getTermService().save(feature);
			}
			state.putFeature(feature);
		}
		return feature;
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
	protected PresenceTerm getPresenceTerm(STATE state, UUID uuid, String label, String text, String labelAbbrev){
		if (uuid == null){
			return null;
		}
		PresenceTerm presenceTerm = state.getPresenceTerm(uuid);
		if (presenceTerm == null){
			presenceTerm = (PresenceTerm)getTermService().find(uuid);
			if (presenceTerm == null){
				presenceTerm = PresenceTerm.NewInstance(text, label, labelAbbrev);
				presenceTerm.setUuid(uuid);
				//set vocabulary ; FIXME use another user-defined vocabulary
				UUID uuidPresenceVoc = UUID.fromString("adbbbe15-c4d3-47b7-80a8-c7d104e53a05"); 
				TermVocabulary<PresenceTerm> voc = getVocabularyService().find(uuidPresenceVoc);
				voc.addTerm(presenceTerm);
				getTermService().save(presenceTerm);
			}
			state.putPresenceTerm(presenceTerm);
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
		if (uuid == null){
			return null;
		}
		Language language = state.getLanguage(uuid);
		if (language == null){
			language = (Language)getTermService().find(uuid);
			if (language == null){
				language = Language.NewInstance(text, label, labelAbbrev);
				
				language.setUuid(uuid);
				//set vocabulary ; FIXME use another user-defined vocabulary
				UUID uuidLanguageVoc = UUID.fromString("45ac7043-7f5e-4f37-92f2-3874aaaef2de"); 
				TermVocabulary<Language> voc = getVocabularyService().find(uuidLanguageVoc);
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
	protected TermVocabulary getVocabulary(UUID uuid, String text, String label, String abbrev, URI termSourceUri, boolean isOrdered) {
		TermVocabulary voc = getVocabularyService().find(uuid);
		if (voc == null){
			if (isOrdered){
				voc = OrderedTermVocabulary.NewInstance(text, label, abbrev, termSourceUri);
			}else{
				voc = TermVocabulary.NewInstance(text, label, abbrev, termSourceUri);
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
	public void addOriginalSource(CdmBase cdmBase, Object idAttributeValue, String namespace, Reference citation)  {
		if (cdmBase instanceof ISourceable ){
			IOriginalSource source;
			ISourceable sourceable = (ISourceable)cdmBase;
			Object id = idAttributeValue;
			String strId = String.valueOf(id);
			String microCitation = null;
			if (cdmBase instanceof IdentifiableEntity){
				source = IdentifiableSource.NewInstance(strId, namespace, citation, microCitation);
			}else if (cdmBase instanceof DescriptionElementBase){
				source = DescriptionElementSource.NewInstance(strId, namespace, citation, microCitation);
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
		NonViralName parentName = HibernateProxyHelper.deproxy(parentTaxon.getName(), NonViralName.class);
		NonViralName childName = HibernateProxyHelper.deproxy(childTaxon.getName(), NonViralName.class);
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
	protected void fillMissingEpithets(NonViralName parentName, NonViralName childName) {
		if (CdmUtils.isEmpty(childName.getGenusOrUninomial()) && childName.getRank().isLower(Rank.GENUS()) ){
			childName.setGenusOrUninomial(parentName.getGenusOrUninomial());
		}
		
		if (CdmUtils.isEmpty(childName.getSpecificEpithet()) && childName.getRank().isLower(Rank.SPECIES()) ){
			childName.setSpecificEpithet(parentName.getSpecificEpithet());
		}
		if (childName.isAutonym() && childName.getCombinationAuthorTeam() == null && childName.getBasionymAuthorTeam() == null ){
			childName.setCombinationAuthorTeam(parentName.getCombinationAuthorTeam());
			childName.setBasionymAuthorTeam(parentName.getBasionymAuthorTeam());
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
	public TaxonDescription getTaxonDescription(Taxon taxon, boolean isImageGallery, boolean createNewIfNotExists) {
		TaxonDescription result = null;
		Set<TaxonDescription> descriptions= taxon.getDescriptions();
		for (TaxonDescription description : descriptions){
			if (description.isImageGallery() == isImageGallery){
				result = description;
				break;
			}
		}
		if (result == null && createNewIfNotExists){
			result = TaxonDescription.NewInstance(taxon);
			result.setImageGallery(isImageGallery);
		}
		return result;
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
			Set<Taxon> acceptedTaxa = synonym.getAcceptedTaxa();
			if (acceptedTaxa.size() == 0){
				return null;
			}else if (acceptedTaxa.size() == 1){
				return acceptedTaxa.iterator().next();
			}else{
				Reference sec = synonym.getSec();
				if (sec != null){
					Set<Taxon> taxaWithSameSec = new HashSet<Taxon>();
					for (Taxon taxon: acceptedTaxa){
						if (sec.equals(taxon.getSec())){
							taxaWithSameSec.add(taxon);
						}
					}
					if (taxaWithSameSec.size() == 1){
						return taxaWithSameSec.iterator().next();
					}
				}
				throw new IllegalStateException("Can't define the one accepted taxon for a synonym out of multiple accept taxa");
			}
		}else{
			throw new IllegalStateException("Unknown TaxonBase subclass: " + taxonBase.getClass().getName());
		}
	}

	

	/**
	 * @param derivedUnitFacade
	 * @param multimediaObject
	 * @throws MalformedURLException
	 */
	protected Media getImageMedia(String multimediaObject, boolean readDataFromUrl) throws MalformedURLException {
		if( multimediaObject == null){
			return null;
		} else {
			ImageInfo imageInfo = null;
			URI uri;
			try {
				uri = new URI(multimediaObject);
				try {
					if (readDataFromUrl){
						imageInfo = ImageInfo.NewInstance(uri, 0);
					}
				} catch (Exception e) {
					String message = "An error occurred when trying to read image meta data: " +  e.getMessage();
					logger.warn(message);
				}
				ImageFile imageFile = ImageFile.NewInstance(uri, null, imageInfo);
				MediaRepresentation representation = MediaRepresentation.NewInstance();
				if(imageInfo != null){
					representation.setMimeType(imageInfo.getMimeType());
				}
				representation.addRepresentationPart(imageFile);
				Media media = Media.NewInstance();
				media.addRepresentation(representation);
				return media;
			} catch (URISyntaxException e1) {
				String message = "An URISyntaxException occurred when trying to create uri from multimedia objcet string: " +  multimediaObject;
				logger.warn(message);
				return null;
			}
		}
	}

	
}
