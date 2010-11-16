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
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.mediaMetaData.ImageMetaData;
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
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.Classification;

/**
 * @author a.mueller
 * @created 01.07.2008
 * @version 1.0
 */
public abstract class CdmImportBase<CONFIG extends IImportConfigurator, STATE extends ImportStateBase> extends CdmIoBase<STATE> implements ICdmImport<CONFIG, STATE>{
	private static Logger logger = Logger.getLogger(CdmImportBase.class);

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
		ExtensionType extensionType = state.getExtensionType(uuid);
		if (extensionType == null){
			extensionType = (ExtensionType)getTermService().find(uuid);
			if (extensionType == null){
				extensionType = ExtensionType.NewInstance(text, label, labelAbbrev);
				extensionType.setUuid(uuid);
				ExtensionType.DOI().getVocabulary().addTerm(extensionType);
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
		MarkerType markerType = state.getMarkerType(uuid);
		if (markerType == null){
			markerType = (MarkerType)getTermService().find(uuid);
			if (markerType == null){
				markerType = MarkerType.NewInstance(label, text, labelAbbrev);
				markerType.setUuid(uuid);
				MarkerType.COMPLETE().getVocabulary().addTerm(markerType);
				getTermService().save(markerType);
			}
			state.putMarkerType(markerType);
		}
		return markerType;
	}
	
	protected AnnotationType getAnnotationType(STATE state, UUID uuid, String label, String text, String labelAbbrev){
		AnnotationType annotationType = state.getAnnotationType(uuid);
		if (annotationType == null){
			annotationType = (AnnotationType)getTermService().find(uuid);
			if (annotationType == null){
				annotationType = AnnotationType.NewInstance(label, text, labelAbbrev);
				annotationType.setUuid(uuid);
				AnnotationType.EDITORIAL().getVocabulary().addTerm(annotationType);
				getTermService().save(annotationType);
			}
			state.putAnnotationType(annotationType);
		}
		return annotationType;
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
		NamedArea namedArea = state.getNamedArea(uuid);
		if (namedArea == null){
			namedArea = (NamedArea)getTermService().find(uuid);
			if (namedArea == null){
				namedArea = NamedArea.NewInstance(text, label, labelAbbrev);
				namedArea.setType(areaType);
				namedArea.setLevel(level);
				namedArea.setUuid(uuid);
				getTermService().save(namedArea);
			}
			state.putNamedArea(namedArea);
		}
		return namedArea;
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
	public void addOriginalSource(CdmBase cdmBase, Object idAttributeValue, String namespace, Reference citation) throws SQLException {
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
	 * Returns the image gallery for a taxon. If there are multiple taxon descriptions
	 * marked as image galleries an arbitrary one is chosen.
	 * If no image gallery exists, a new one is created if <code>createNewIfNotExists</code>
	 * is <code>true</code>.
	 * @param createNewIfNotExists
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
	 * @param derivedUnitFacade
	 * @param multimediaObject
	 * @throws MalformedURLException
	 */
	protected Media getImageMedia(String multimediaObject, boolean readDataFromUrl) throws MalformedURLException {
		if( multimediaObject == null){
			return null;
		} else {
			ImageMetaData imd = ImageMetaData.newInstance();
			try {
				if (readDataFromUrl){
					URL url = new URL(multimediaObject);
					imd.readMetaData(url.toURI(), 0);
				}
			} catch (Exception e) {
				String message = "An error occurred when trying to read image meta data: " +  e.getMessage();
				logger.warn(message);
			}
			ImageFile imf = ImageFile.NewInstance(multimediaObject, null, imd);
			MediaRepresentation representation = MediaRepresentation.NewInstance();
			representation.setMimeType(imd.getMimeType());
			representation.addRepresentationPart(imf);
			Media media = Media.NewInstance();
			media.addRepresentation(representation);
				
			return media;
		}
	}

	
}
