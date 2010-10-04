/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.facade;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MethodNotSupportedException; //FIMXE use other execption class
import javax.persistence.Transient;


import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.Sex;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.Stage;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * This class is a facade to the eu.etaxonomy.cdm.model.occurrence package from
 * a specimen based view. It does not support all functionality available in the
 * occurrence package.<BR>
 * The most significant restriction is that a specimen may derive only from
 * one direct derivation event and there must be only one field observation (gathering event)
 * it derives from.<BR> 
 * 
 * @author a.mueller
 * @date 14.05.2010
 */
public class DerivedUnitFacade {
	private static final Logger logger = Logger.getLogger(DerivedUnitFacade.class);
	
	private static final String notSupportMessage = "A specimen facade not supported exception has occurred at a place where this should not have happened. The developer should implement not support check properly during class initialization ";
	
	/**
	 * Enum that defines the class the "Specimen" belongs to.
	 * Some methods of the facade are not available for certain classes
	 * and will throw an Exception when invoking them.
	 */
	public enum DerivedUnitType{
		Specimen ("Specimen"),
		Observation("Observation"),
		LivingBeing("Living Being"),
		Fossil("Fossil"),
		DerivedUnit("Derived Unit");
		
		String representation;
		private DerivedUnitType(String representation){
			this.representation = representation;
		}
		
		/**
		 * @return the representation
		 */
		public String getRepresentation() {
			return representation;
		}
		
		private DerivedUnitBase getNewDerivedUnitInstance(){
			if (this == DerivedUnitType.Specimen){
				return eu.etaxonomy.cdm.model.occurrence.Specimen.NewInstance();
			}else if (this == DerivedUnitType.Observation){
				return eu.etaxonomy.cdm.model.occurrence.Observation.NewInstance();
			}else if (this == DerivedUnitType.LivingBeing){
				return eu.etaxonomy.cdm.model.occurrence.LivingBeing.NewInstance();
			}else if (this == DerivedUnitType.Fossil){
				return eu.etaxonomy.cdm.model.occurrence.Fossil.NewInstance();
			}else if (this == DerivedUnitType.DerivedUnit){
				return eu.etaxonomy.cdm.model.occurrence.DerivedUnit.NewInstance();
			}else{
				throw new IllegalStateException("Unknown derived unit type " +  this.getRepresentation());
			}
		}
		
	}
	
	
	private DerivedUnitFacadeConfigurator config;
	
	//private GatheringEvent gatheringEvent;
	private DerivedUnitType type;  //needed?
	
	private FieldObservation fieldObservation;
	
	private DerivedUnitBase derivedUnit;

	//media - the text data holding the media
	private TextData derivedUnitMediaTextData;
	private TextData fieldObjectMediaTextData;
	
	
	private TextData ecology;
	private TextData plantDescription;
	
	
	/**
	 * Creates a derived unit facade for a new derived unit of type <code>type</code>.
	 * @param type
	 * @return
	 */
	public static DerivedUnitFacade NewInstance(DerivedUnitType type){
		return new DerivedUnitFacade(type);
	}

	/**
	 * Creates a derived unit facade for a given derived unit using the default configuation.
	 * @param derivedUnit
	 * @return
	 * @throws DerivedUnitFacadeNotSupportedException
	 */
	public static DerivedUnitFacade NewInstance(DerivedUnitBase derivedUnit) throws DerivedUnitFacadeNotSupportedException{
		return new DerivedUnitFacade(derivedUnit, null);
	}
	
	public static DerivedUnitFacade NewInstance(DerivedUnitBase derivedUnit, DerivedUnitFacadeConfigurator config) throws DerivedUnitFacadeNotSupportedException{
		return new DerivedUnitFacade(derivedUnit, config);
	}


	
// ****************** CONSTRUCTOR ****************************************************
	
	private DerivedUnitFacade(DerivedUnitType type){
		this.config = DerivedUnitFacadeConfigurator.NewInstance();
		
		//derivedUnit
		derivedUnit = type.getNewDerivedUnitInstance();
		setCacheStrategy();
	}
	
	private DerivedUnitFacade(DerivedUnitBase derivedUnit, DerivedUnitFacadeConfigurator config) throws DerivedUnitFacadeNotSupportedException{
		
		if (config == null){
			config = DerivedUnitFacadeConfigurator.NewInstance();
		}
		this.config = config;
		
		//derived unit
		this.derivedUnit = derivedUnit;
		setCacheStrategy();
		
		//derivation event
		if (this.derivedUnit.getDerivedFrom() != null){
			DerivationEvent derivationEvent = getDerivationEvent(true);
			//fieldObservation
			Set<FieldObservation> fieldOriginals = getFieldObservationsOriginals(derivationEvent, null);
			if (fieldOriginals.size() > 1){
				throw new DerivedUnitFacadeNotSupportedException("Specimen must not have more than 1 derivation event");
			}else if (fieldOriginals.size() == 0){
				//fieldObservation = FieldObservation.NewInstance();
			}else if (fieldOriginals.size() == 1){
				fieldObservation = fieldOriginals.iterator().next();
				//###fieldObservation = getInitializedFieldObservation(fieldObservation);
				fieldObservation.addPropertyChangeListener(getNewEventPropagationListener());
			}else{
				throw new IllegalStateException("Illegal state");
			}	
		}
		// #### derivedUnit = getInitializedDerivedUnit(derivedUnit);

		//test if unsupported
		
		//media
		//specimen
//		String objectTypeExceptionText = "Specimen";
//		SpecimenDescription imageGallery = getImageGalleryWithSupportTest(derivedUnit, objectTypeExceptionText, false);
//		getImageTextDataWithSupportTest(imageGallery, objectTypeExceptionText);
		this.derivedUnitMediaTextData = inititialzeTextDataWithSupportTest(Feature.IMAGE(), this.derivedUnit, false, true);
		
		//field observation
//		objectTypeExceptionText = "Field observation";
//		imageGallery = getImageGalleryWithSupportTest(fieldObservation, objectTypeExceptionText, false);
//		getImageTextDataWithSupportTest(imageGallery, objectTypeExceptionText);
		fieldObjectMediaTextData = initializeFieldObjectTextDataWithSupportTest(Feature.IMAGE(), false, true);
		
		//handle derivedUnit.getMedia()
		if (derivedUnit.getMedia().size() > 0){
			//TODO better changed model here to allow only one place for images
			if (this.config.isMoveDerivedUnitMediaToGallery()){
				Set<Media> mediaSet = derivedUnit.getMedia();
				for (Media media : mediaSet){
					this.addDerivedUnitMedia(media);
				}
				mediaSet.removeAll(getDerivedUnitMedia());
			}else{
				throw new DerivedUnitFacadeNotSupportedException("Specimen may not have direct media. Only (one) image gallery is allowed");
			}
		}
		
		//handle fieldObservation.getMedia()
		if (fieldObservation != null && fieldObservation.getMedia() != null && fieldObservation.getMedia().size() > 0){
			//TODO better changed model here to allow only one place for images
			if (this.config.isMoveFieldObjectMediaToGallery()){
				Set<Media> mediaSet = fieldObservation.getMedia();
				for (Media media : mediaSet){
					this.addFieldObjectMedia(media);
				}
				mediaSet.removeAll(getFieldObjectMedia());
			}else{
				throw new DerivedUnitFacadeNotSupportedException("Field object may not have direct media. Only (one) image gallery is allowed");
			}
		}
		
		//test if descriptions are supported
		ecology = initializeFieldObjectTextDataWithSupportTest(Feature.ECOLOGY(), false, false);
		plantDescription = initializeFieldObjectTextDataWithSupportTest(Feature.DESCRIPTION(), false, false);
	}
	

	private DerivedUnitBase getInitializedDerivedUnit(DerivedUnitBase derivedUnit) {
		IOccurrenceService occurrenceService = this.config.getOccurrenceService();
		if (occurrenceService == null){
			return derivedUnit;
		}
		List<String> propertyPaths = this.config.getPropertyPaths();
		if (propertyPaths == null){
			return derivedUnit;
		}
		propertyPaths = getDerivedUnitPropertyPaths(propertyPaths);
		DerivedUnitBase result = (DerivedUnitBase)occurrenceService.load(derivedUnit.getUuid(), propertyPaths);
		return result;
	}

	/**
	 * Initializes the derived unit according to the configuartions property path.
	 * If the property path is <code>null</code> or no occurrence service is given the
	 * returned object is the same as the input parameter.
	 * @param fieldObservation2
	 * @return
	 */
	private FieldObservation getInitializedFieldObservation(FieldObservation fieldObservation) {
		IOccurrenceService occurrenceService = this.config.getOccurrenceService();
		if (occurrenceService == null){
			return fieldObservation;
		}
		List<String> propertyPaths = this.config.getPropertyPaths();
		if (propertyPaths == null){
			return fieldObservation;
		}
		propertyPaths = getFieldObjectPropertyPaths(propertyPaths);
		FieldObservation result = (FieldObservation)occurrenceService.load(fieldObservation.getUuid(), propertyPaths);
		return result;
	}

	/**
	 * Transforms the property paths in a way that the facade is handled just like an 
	 * ordinary CdmBase object.<BR>
	 * E.g. a property path "collectinAreas" will be translated into gatheringEvent.collectingAreas
	 * @param propertyPaths
	 * @return
	 */
	private List<String> getFieldObjectPropertyPaths(List<String> propertyPaths) {
		List<String> result = new ArrayList<String>();
		for (String facadePath : propertyPaths){
			// collecting areas (named area)
			if (facadePath.startsWith("collectingAreas")){
				facadePath = "gatheringEvent." + facadePath;
				result.add(facadePath);
			}
			// collector (agentBase)
			else if (facadePath.startsWith("collector")){
				facadePath = facadePath.replace("collector", "gatheringEvent.actor");
				result.add(facadePath);
			}
			// exactLocation (agentBase)
			else if (facadePath.startsWith("exactLocation")){
				facadePath = "gatheringEvent." + facadePath;
				result.add(facadePath);
			}
			// gatheringPeriod (TimePeriod)
			else if (facadePath.startsWith("gatheringPeriod")){
				facadePath = facadePath.replace("gatheringPeriod", "gatheringEvent.timeperiod");
				result.add(facadePath);
			}
			// (locality/ localityLanguage , LanguageString)
			else if (facadePath.startsWith("locality")){
				facadePath = "gatheringEvent." + facadePath;
				result.add(facadePath);
			}
			
			//*********** FIELD OBJECT ************
			// fieldObjectDefinitions (Map<language, languageString)
			else if (facadePath.startsWith("fieldObjectDefinitions")){
				// TODO or definition ???
				facadePath = facadePath.replace("fieldObjectDefinitions", "description");
				result.add(facadePath);
			}
			// fieldObjectMedia  (Media)
			else if (facadePath.startsWith("fieldObjectMedia")){
				// TODO ??? 
				facadePath = facadePath.replace("fieldObjectMedia", "descriptions.elements.media");
				result.add(facadePath);
			}
			
			//Gathering Event will always be added
			result.add("gatheringEvent");
			
		}
		
/*
		Gathering Event
		====================
		- gatheringEvent (GatheringEvent)

		Field Object
		=================
		- ecology/ ecologyAll (String)  ???
		- plant description (like ecology)
		
		- fieldObjectImageGallery (SpecimenDescription)  - is automatically initialized via fieldObjectMedia

*/
		
		return result;
	}
	
	/**
	 * Transforms the property paths in a way that the facade is handled just like an 
	 * ordinary CdmBase object.<BR>
	 * E.g. a property path "collectinAreas" will be translated into gatheringEvent.collectingAreas
	 * @param propertyPaths
	 * @return
	 */
	private List<String> getDerivedUnitPropertyPaths(List<String> propertyPaths) {
		List<String> result = new ArrayList<String>();
		for (String facadePath : propertyPaths){
			// determinations (DeterminationEvent)
			if (facadePath.startsWith("determinations")){
				facadePath = "" + facadePath;  //no change
				result.add(facadePath);
			}
			// storedUnder (TaxonNameBase)
			else if (facadePath.startsWith("storedUnder")){
				facadePath = "" + facadePath;  //no change
				result.add(facadePath);
			}
			// sources (IdentifiableSource)
			else if (facadePath.startsWith("sources")){
				facadePath = "" + facadePath;  //no change
				result.add(facadePath);
			}
			// collection (Collection)
			else if (facadePath.startsWith("collection")){
				facadePath = "" + facadePath;  //no change
				result.add(facadePath);
			}
			// (locality/ localityLanguage , LanguageString)
			else if (facadePath.startsWith("locality")){
				facadePath = "gatheringEvent." + facadePath;
				result.add(facadePath);
			}
		
			//*********** FIELD OBJECT ************
			// derivedUnitDefinitions (Map<language, languageString)
			else if (facadePath.startsWith("derivedUnitDefinitions")){
				// TODO or definition ???
				facadePath = facadePath.replace("derivedUnitDefinitions", "description");
				result.add(facadePath);
			}
			
			// derivedUnitMedia  (Media)
			else if (facadePath.startsWith("derivedUnitMedia")){
				// TODO ??? 
				facadePath = facadePath.replace("derivedUnitMedia", "descriptions.elements.media");
				result.add(facadePath);
			}
			
		}
		
/*
		//TODO
		Derived Unit
		=====================
		
		- derivedUnitImageGallery (SpecimenDescription)  - is automatically initialized via derivedUnitMedia
		
		- derivationEvent (DerivationEvent)  - will always be initialized
		- duplicates (??? Specimen???) ???
*/
		
		return result;
	}

	/**
	 * 
	 */
	private void setCacheStrategy() {
		derivedUnit.setCacheStrategy(new DerivedUnitFacadeCacheStrategy());
	}


	/**
	 * @param feature
	 * @param createIfNotExists
	 * @param isImageGallery
	 * @return
	 * @throws DerivedUnitFacadeNotSupportedException
	 */
	private TextData initializeFieldObjectTextDataWithSupportTest(Feature feature, boolean createIfNotExists, boolean isImageGallery) throws DerivedUnitFacadeNotSupportedException {
		//field object
		FieldObservation fieldObject = getFieldObservation(createIfNotExists) ;
		if (fieldObject == null){
			return null;
		}
		return inititialzeTextDataWithSupportTest(feature, fieldObject, createIfNotExists, isImageGallery);
	}


	/**
	 * @param feature
	 * @param specimen
	 * @param createIfNotExists
	 * @param isImageGallery
	 * @return
	 * @throws DerivedUnitFacadeNotSupportedException
	 */
	private TextData inititialzeTextDataWithSupportTest(Feature feature, SpecimenOrObservationBase specimen, boolean createIfNotExists, 
				boolean isImageGallery) throws DerivedUnitFacadeNotSupportedException {
		if (feature == null ){
			return null;
		}
		TextData textData = null;
		if (createIfNotExists){
			textData = TextData.NewInstance(feature);
		}
		
		Set<SpecimenDescription> descriptions;
		if (isImageGallery){
			descriptions = specimen.getSpecimenDescriptionImageGallery();
		}else{
			descriptions = specimen.getSpecimenDescriptions(false);
		}
		if (descriptions.size() == 0){
			if (createIfNotExists){
				SpecimenDescription newSpecimenDescription = SpecimenDescription.NewInstance(specimen);
				newSpecimenDescription.addElement(textData);
				return textData;
			}else{
				return null;
			}
		}
		Set<DescriptionElementBase> existingTextData = new HashSet<DescriptionElementBase>();
		for (SpecimenDescription description : descriptions){
			for (DescriptionElementBase element: description.getElements()){
				if (element.isInstanceOf(TextData.class) && ( feature.equals(element.getFeature() )|| isImageGallery ) ){
					existingTextData.add(element);
				}
			}
		}
		if (existingTextData.size() > 1){
			throw new DerivedUnitFacadeNotSupportedException("Specimen facade does not support more than one description text data of type " + feature.getLabel());
			
		}else if (existingTextData.size() == 1){
			return CdmBase.deproxy(existingTextData.iterator().next(), TextData.class);
		}else{
			SpecimenDescription description = descriptions.iterator().next();
			description.addElement(textData);
			return textData;
		}
	}

//************************** METHODS *****************************************	

	private TextData getDerivedUnitImageGalleryTextData(boolean createIfNotExists) throws DerivedUnitFacadeNotSupportedException{
		if (this.derivedUnitMediaTextData == null && createIfNotExists){
			this.derivedUnitMediaTextData = getImageGalleryTextData(derivedUnit, "Specimen");
		}
		return this.derivedUnitMediaTextData;
	}
	
	private TextData getObservationImageGalleryTextData(boolean createIfNotExists) throws DerivedUnitFacadeNotSupportedException{
		if (this.fieldObjectMediaTextData == null && createIfNotExists){
			this.fieldObjectMediaTextData = getImageGalleryTextData(fieldObservation, "Field observation");
		}
		return this.fieldObjectMediaTextData;
	}

	
	
	/**
	 * @param derivationEvent2
	 * @return
	 * @throws DerivedUnitFacadeNotSupportedException 
	 */
	private Set<FieldObservation> getFieldObservationsOriginals(DerivationEvent derivationEvent, Set<SpecimenOrObservationBase> recursionAvoidSet) throws DerivedUnitFacadeNotSupportedException {
		if (recursionAvoidSet == null){
			recursionAvoidSet = new HashSet<SpecimenOrObservationBase>();
		}
		Set<FieldObservation> result = new HashSet<FieldObservation>();
		Set<SpecimenOrObservationBase> originals = derivationEvent.getOriginals();
		for (SpecimenOrObservationBase original : originals){
			if (original.isInstanceOf(FieldObservation.class)){
				result.add(CdmBase.deproxy(original, FieldObservation.class));
			}else if (original.isInstanceOf(DerivedUnitBase.class)){
				//if specimen has already been tested exclude it from further recursion
				if (recursionAvoidSet.contains(original)){
					continue;
				}
				DerivedUnitBase derivedUnit = CdmBase.deproxy(original, DerivedUnitBase.class);
				DerivationEvent originalDerivation = derivedUnit.getDerivedFrom();
//				Set<DerivationEvent> derivationEvents = original.getDerivationEvents(); 
//				for (DerivationEvent originalDerivation : derivationEvents){
					Set<FieldObservation> fieldObservations = getFieldObservationsOriginals(originalDerivation, recursionAvoidSet);
					result.addAll(fieldObservations);
//				}
			}else{
				throw new DerivedUnitFacadeNotSupportedException("Unhandled specimen or observation base type: " + original.getClass().getName() );
			}
			
		}
		return result;
	}
	
	//*********** MEDIA METHODS ******************************
	
//	/**
//	 * Returns the media list for a specimen. Throws an exception if the existing specimen descriptions
//	 * are not supported by this facade.
//	 * @param specimen the specimen the media belongs to
//	 * @param specimenExceptionText text describing the specimen for exception messages
//	 * @return
//	 * @throws DerivedUnitFacadeNotSupportedException
//	 */
//	private List<Media> getImageGalleryMedia(SpecimenOrObservationBase specimen, String specimenExceptionText) throws DerivedUnitFacadeNotSupportedException{
//		List<Media> result;
//		SpecimenDescription imageGallery = getImageGalleryWithSupportTest(specimen, specimenExceptionText, true);
//		TextData textData = getImageTextDataWithSupportTest(imageGallery, specimenExceptionText);
//		result = textData.getMedia();
//		return result;
//	}
	
	/**
	 * Returns the media list for a specimen. Throws an exception if the existing specimen descriptions
	 * are not supported by this facade.
	 * @param specimen the specimen the media belongs to
	 * @param specimenExceptionText text describing the specimen for exception messages
	 * @return
	 * @throws DerivedUnitFacadeNotSupportedException
	 */
	private TextData getImageGalleryTextData(SpecimenOrObservationBase specimen, String specimenExceptionText) throws DerivedUnitFacadeNotSupportedException{
		TextData result;
		SpecimenDescription imageGallery = getImageGalleryWithSupportTest(specimen, specimenExceptionText, true);
		result = getImageTextDataWithSupportTest(imageGallery, specimenExceptionText);
		return result;
	}
	
	
	/**
	 * Returns the image gallery of the according specimen. Throws an exception if the attached
	 * image gallerie(s) are not supported by this facade.
	 * If no image gallery exists a new one is created if <code>createNewIfNotExists</code> is true and
	 * if specimen is not <code>null</code>.
	 * @param specimen
	 * @param specimenText
	 * @param createNewIfNotExists
	 * @return
	 * @throws DerivedUnitFacadeNotSupportedException
	 */
	private SpecimenDescription getImageGalleryWithSupportTest(SpecimenOrObservationBase<?> specimen, String specimenText, boolean createNewIfNotExists) throws DerivedUnitFacadeNotSupportedException{
		if (specimen == null){
			return null;
		}
		SpecimenDescription imageGallery;
		if (hasMultipleImageGalleries(specimen)){
			throw new DerivedUnitFacadeNotSupportedException( specimenText + " must not have more than 1 image gallery");
		}else{
			imageGallery = getImageGallery(specimen, createNewIfNotExists);
			getImageTextDataWithSupportTest(imageGallery, specimenText);
		}
		return imageGallery;
	}
	
	/**
	 * Returns the media holding text data element of the image gallery. Throws an exception if multiple
	 * such text data already exist.
	 * Creates a new text data if none exists and adds it to the image gallery.
	 * If image gallery is <code>null</code> nothing happens.
	 * @param imageGallery
	 * @param textData
	 * @return
	 * @throws DerivedUnitFacadeNotSupportedException 
	 */
	private TextData getImageTextDataWithSupportTest(SpecimenDescription imageGallery, String specimenText) throws DerivedUnitFacadeNotSupportedException {
		if (imageGallery == null){
			return null;
		}
		TextData textData = null;
		for (DescriptionElementBase element: imageGallery.getElements()){
			if (element.isInstanceOf(TextData.class) && element.getFeature().equals(Feature.IMAGE())){
				if (textData != null){
					throw new DerivedUnitFacadeNotSupportedException( specimenText + " must not have more than 1 image text data element in image gallery");
				}
				textData = CdmBase.deproxy(element, TextData.class);
			}
		}
		if (textData == null){
			textData = TextData.NewInstance(Feature.IMAGE());
			imageGallery.addElement(textData);
		}
		return textData;
	}

	/**
	 * Checks, if a specimen belongs to more than one description that is an image gallery
	 * @param derivedUnit
	 * @return
	 */
	private boolean hasMultipleImageGalleries(SpecimenOrObservationBase<?> derivedUnit){
		int count = 0;
		Set<SpecimenDescription> descriptions= derivedUnit.getSpecimenDescriptions();
		for (SpecimenDescription description : descriptions){
			if (description.isImageGallery()){
				count++;
			}
		}
		return (count > 1);
	}

	
	/**
	 * Returns the image gallery for a specimen. If there are multiple specimen descriptions
	 * marked as image galleries an arbitrary one is chosen.
	 * If no image gallery exists, a new one is created if <code>createNewIfNotExists</code>
	 * is <code>true</code>.<Br>
	 * If specimen is <code>null</code> a null pointer exception is thrown.
	 * @param createNewIfNotExists
	 * @return
	 */
	private SpecimenDescription getImageGallery(SpecimenOrObservationBase<?> specimen, boolean createIfNotExists) {
		SpecimenDescription result = null;
		Set<SpecimenDescription> descriptions= specimen.getSpecimenDescriptions();
		for (SpecimenDescription description : descriptions){
			if (description.isImageGallery()){
				result = description;
				break;
			}
		}
		if (result == null && createIfNotExists){
			result = SpecimenDescription.NewInstance(specimen);
			result.setImageGallery(true);
		}
		return result;
	}
	
	/**
	 * Adds a media to the specimens image gallery. If media is <code>null</code> nothing happens.
	 * @param media
	 * @param specimen
	 * @return true if media is not null (as specified by {@link java.util.Collection#add(Object) Collection.add(E e)} 
	 * @throws DerivedUnitFacadeNotSupportedException
	 */
	private boolean addMedia(Media media, SpecimenOrObservationBase<?> specimen) throws DerivedUnitFacadeNotSupportedException {
		if (media != null){
			List<Media> mediaList = getMedia(specimen, true);
			return mediaList.add(media);
		}else{
			return false;
		}
	}

	/**
	 * Removes a media from the specimens image gallery.
	 * @param media
	 * @param specimen
	 * @return true if an element was removed as a result of this call (as specified by {@link java.util.Collection#remove(Object) Collection.remove(E e)} 
	 * @throws DerivedUnitFacadeNotSupportedException
	 */
	private boolean removeMedia(Media media, SpecimenOrObservationBase<?> specimen) throws DerivedUnitFacadeNotSupportedException {
		List<Media> mediaList = getMedia(specimen, true);
		return mediaList == null ? null : mediaList.remove(media);
	}

	private List<Media> getMedia(SpecimenOrObservationBase<?> specimen, boolean createIfNotExists) throws DerivedUnitFacadeNotSupportedException {
		TextData textData = getMediaTextData(specimen, createIfNotExists);
		return textData == null ? null : textData.getMedia();
	}
	
	/**
	 * Returns the one media list of a specimen which is part of the only image gallery that 
	 * this specimen is part of.<BR>
	 * If these conditions are not hold an exception is thrwon.
	 * @param specimen
	 * @return
	 * @throws DerivedUnitFacadeNotSupportedException
	 */
//	private List<Media> getMedia(SpecimenOrObservationBase<?> specimen) throws DerivedUnitFacadeNotSupportedException {
//		if (specimen == null){
//			return null;
//		}
//		if (specimen == this.derivedUnit){
//			return getDerivedUnitImageGalleryMedia();
//		}else if (specimen == this.fieldObservation){
//			return getObservationImageGalleryTextData();
//		}else{
//			return getImageGalleryMedia(specimen, "Undefined specimen ");
//		}
//	}
	
	/**
	 * Returns the one media list of a specimen which is part of the only image gallery that 
	 * this specimen is part of.<BR>
	 * If these conditions are not hold an exception is thrwon.
	 * @param specimen
	 * @return
	 * @throws DerivedUnitFacadeNotSupportedException
	 */
	private TextData getMediaTextData(SpecimenOrObservationBase<?> specimen, boolean createIfNotExists) throws DerivedUnitFacadeNotSupportedException {
		if (specimen == null){
			return null;
		}
		if (specimen == this.derivedUnit){
			return getDerivedUnitImageGalleryTextData(createIfNotExists);
		}else if (specimen == this.fieldObservation){
			return getObservationImageGalleryTextData(createIfNotExists);
		}else{
			return getImageGalleryTextData(specimen, "Undefined specimen ");
		}
	}
	
	
//****************** GETTER / SETTER / ADDER / REMOVER ***********************/	
	
// ****************** Gathering Event *********************************/
	
	//country
	@Transient
	public NamedArea getCountry(){
		return  (hasGatheringEvent() ? getGatheringEvent(true).getCountry() : null);
	}
	
	public void setCountry(NamedArea country){
		getGatheringEvent(true).setCountry(country);
	}
	
	
	//Collecting area
	public void addCollectingArea(NamedArea area) {
		getGatheringEvent(true).addCollectingArea(area);
	}
	public void addCollectingAreas(java.util.Collection<NamedArea> areas) {
		for (NamedArea area : areas){
			getGatheringEvent(true).addCollectingArea(area);
		}
	}
	@Transient
	public Set<NamedArea> getCollectingAreas() {
		return  (hasGatheringEvent() ? getGatheringEvent(true).getCollectingAreas() : null);
	}
	public void removeCollectingArea(NamedArea area) {
		if (hasGatheringEvent()){
			getGatheringEvent(true).removeCollectingArea(area);
		}
	}

	//absolute elevation  
	/** meter above/below sea level of the surface 
	 * @see #getAbsoluteElevationError()
	 * @see #getAbsoluteElevationRange()
	 **/
	@Transient
	public Integer getAbsoluteElevation() {
		return (hasGatheringEvent() ? getGatheringEvent(true).getAbsoluteElevation() : null);
	}
	public void setAbsoluteElevation(Integer absoluteElevation) {
		getGatheringEvent(true).setAbsoluteElevation(absoluteElevation);
	}

	//absolute elevation error
	@Transient
	public Integer getAbsoluteElevationError() {
		return (hasGatheringEvent() ? getGatheringEvent(true).getAbsoluteElevationError() : null);
	}
	public void setAbsoluteElevationError(Integer absoluteElevationError) {
		getGatheringEvent(true).setAbsoluteElevationError(absoluteElevationError);
	}
	
	/**
	 * @see #getAbsoluteElevation()
	 * @see #getAbsoluteElevationError()
	 * @see #setAbsoluteElevationRange(Integer, Integer)
	 * @see #getAbsoluteElevationMaximum()
	 */
	@Transient
	public Integer getAbsoluteElevationMinimum(){
		if ( ! hasGatheringEvent() ){
			return null;
		}
		Integer minimum = getGatheringEvent(true).getAbsoluteElevation();
		if (getGatheringEvent(true).getAbsoluteElevationError() != null){
			minimum = minimum -  getGatheringEvent(true).getAbsoluteElevationError();
		}
		return minimum;
	}
	/**
	 * @see #getAbsoluteElevation()
	 * @see #getAbsoluteElevationError()
	 * @see #setAbsoluteElevationRange(Integer, Integer)
	 * @see #getAbsoluteElevationMinimum()
	 */
	@Transient
	public Integer getAbsoluteElevationMaximum(){
		if ( ! hasGatheringEvent() ){
			return null;
		}
		Integer maximum = getGatheringEvent(true).getAbsoluteElevation();
		if (getGatheringEvent(true).getAbsoluteElevationError() != null){
			maximum = maximum +  getGatheringEvent(true).getAbsoluteElevationError();
		}
		return maximum;
	}

	
	/**
	 * This method replaces absoluteElevation and absoulteElevationError by
	 * internally translating minimum and maximum values into
	 * average and error values. As all these values are integer based
	 * it is necessary that the distance is between minimum and maximum is <b>even</b>,
	 * otherwise we will get a rounding error resulting in a maximum that is increased
	 * by 1.
	 * @see #setAbsoluteElevation(Integer)
	 * @see #setAbsoluteElevationError(Integer)
	 * @param minimumElevation minimum of the range
	 * @param maximumElevation maximum of the range
	 */
	public void setAbsoluteElevationRange(Integer minimumElevation, Integer maximumElevation){
		if (minimumElevation == null || maximumElevation == null){
			Integer elevation = minimumElevation;
			Integer error = 0;
			if (minimumElevation == null){
				elevation = maximumElevation;
				if (elevation == null){
					error = null;
				}
			}
			getGatheringEvent(true).setAbsoluteElevation(elevation);
			getGatheringEvent(true).setAbsoluteElevationError(error);
		}else{
			if (! isEvenDistance(minimumElevation, maximumElevation) ){
				throw new IllegalArgumentException("Distance between minimum and maximum elevation must be even but was " + Math.abs(minimumElevation - maximumElevation));
			}
			Integer absoluteElevationError = Math.abs(maximumElevation - minimumElevation);
			absoluteElevationError = absoluteElevationError / 2;
			Integer absoluteElevation = minimumElevation + absoluteElevationError;
			getGatheringEvent(true).setAbsoluteElevation(absoluteElevation);
			getGatheringEvent(true).setAbsoluteElevationError(absoluteElevationError);
		}
	}

	/**
	 * @param minimumElevation
	 * @param maximumElevation
	 * @return
	 */
	private boolean isEvenDistance(Integer minimumElevation, Integer maximumElevation) {
		Integer diff = ( maximumElevation - minimumElevation);
		Integer testDiff = (diff /2) *2 ;
		return (testDiff == diff);
	}

	//collector
	@Transient
	public AgentBase getCollector() {
		return  (hasGatheringEvent() ? getGatheringEvent(true).getCollector() : null);
	}
	public void setCollector(AgentBase collector){
		getGatheringEvent(true).setCollector(collector);
	}

	//collecting method
	@Transient
	public String getCollectingMethod() {
		return  (hasGatheringEvent() ? getGatheringEvent(true).getCollectingMethod() : null);
	}
	public void setCollectingMethod(String collectingMethod) {
		getGatheringEvent(true).setCollectingMethod(collectingMethod);
	}

	//distance to ground
	@Transient
	public Integer getDistanceToGround() {
		return  (hasGatheringEvent() ? getGatheringEvent(true).getDistanceToGround() : null);
	}
	public void setDistanceToGround(Integer distanceToGround) {
		getGatheringEvent(true).setDistanceToGround(distanceToGround);
	}

	//distance to water surface
	@Transient
	public Integer getDistanceToWaterSurface() {
		return  (hasGatheringEvent() ? getGatheringEvent(true).getDistanceToWaterSurface() : null);
	}
	public void setDistanceToWaterSurface(Integer distanceToWaterSurface) {
		getGatheringEvent(true).setDistanceToWaterSurface(distanceToWaterSurface);
	}

	//exact location
	@Transient
	public Point getExactLocation() {
		return  (hasGatheringEvent() ? getGatheringEvent(true).getExactLocation() : null );
	}
	
	/**
	 * Returns a sexagesimal representation of the exact location (e.g. 12°59'N, 35°23E).
	 * If the exact location is <code>null</code> the empty string is returned.
	 * @param includeEmptySeconds
	 * @param includeReferenceSystem
	 * @return
	 */
	public String getExactLocationText(boolean includeEmptySeconds, boolean includeReferenceSystem){
		return (this.getExactLocation() == null ? "" : this.getExactLocation().toSexagesimalString(includeEmptySeconds, includeReferenceSystem));
	}
	public void setExactLocation(Point exactLocation) {
		getGatheringEvent(true).setExactLocation(exactLocation);
	}
	public void setExactLocationByParsing(String longitudeToParse, String latitudeToParse, ReferenceSystem referenceSystem, Integer errorRadius) throws ParseException{
		Point point = Point.NewInstance(null, null, referenceSystem, errorRadius);
		point.setLongitudeByParsing(longitudeToParse);
		point.setLatitudeByParsing(latitudeToParse);
		setExactLocation(point);
	}
	
	//gathering event description
	@Transient
	public String getGatheringEventDescription() {
		return  (hasGatheringEvent() ? getGatheringEvent(true).getDescription() : null);
	}
	public void setGatheringEventDescription(String description) {
		getGatheringEvent(true).setDescription(description);
	}

	//gatering period
	@Transient
	public TimePeriod getGatheringPeriod() {
		return (hasGatheringEvent() ? getGatheringEvent(true).getTimeperiod() : null);
	}
	public void setGatheringPeriod(TimePeriod timeperiod) {
		getGatheringEvent(true).setTimeperiod(timeperiod);
	}

	//locality
	@Transient
	public LanguageString getLocality(){
		return (hasGatheringEvent() ? getGatheringEvent(true).getLocality() : null);
	}
	@Transient
	public String getLocalityText(){
		LanguageString locality = getLocality();
		if(locality != null){
			return locality.getText();
		}
		return null;
	}
	@Transient
	public Language getLocalityLanguage(){
		LanguageString locality = getLocality();
		if(locality != null){
			return locality.getLanguage();
		}
		return null;
	}
	
	/**
	 * Sets the locality string in the default language
	 * @param locality
	 */
	public void setLocality(String locality){
		Language language = Language.DEFAULT();
		setLocality(locality, language);
	}
	public void setLocality(String locality, Language language){
		LanguageString langString = LanguageString.NewInstance(locality, language);
		setLocality(langString);
	}
	public void setLocality(LanguageString locality){
		getGatheringEvent(true).setLocality(locality);
	}
	
	/**
	 * The gathering event will be used for the field object instead of the old gathering event.<BR>
	 * <B>This method will override all gathering values (see below).</B>
	 * @see #getAbsoluteElevation()
	 * @see #getAbsoluteElevationError()
	 * @see #getDistanceToGround()
	 * @see #getDistanceToWaterSurface()
	 * @see #getExactLocation()
	 * @see #getGatheringEventDescription()
	 * @see #getGatheringPeriod()
	 * @see #getCollectingAreas()
	 * @see #getCollectingMethod()
	 * @see #getLocality()
	 * @see #getCollector()	
	 * @param gatheringEvent
	 */
	public void setGatheringEvent(GatheringEvent gatheringEvent) {
		getFieldObservation(true).setGatheringEvent(gatheringEvent);
	}
	public boolean hasGatheringEvent(){
		return (getGatheringEvent(false) != null);
	}
	@Transient
	public GatheringEvent getGatheringEvent() {
		return getGatheringEvent(false);
	}
	
	public GatheringEvent getGatheringEvent(boolean createIfNotExists) {
		if (! hasFieldObservation() && ! createIfNotExists){
			return null;
		}
		if (createIfNotExists && getFieldObservation(true).getGatheringEvent() == null ){
			GatheringEvent gatheringEvent = GatheringEvent.NewInstance();
			getFieldObservation(true).setGatheringEvent(gatheringEvent);
		}
		return getFieldObservation(true).getGatheringEvent();
	}
	
// ****************** Field Object ************************************/
	
	/**
	 * Returns true if a field observation exists (even if all attributes are empty or <code>null<code>.
	 * @return
	 */
	public boolean hasFieldObject(){
		return this.fieldObservation != null;
	}
	
	//ecology
	@Transient
	public String getEcology(){
		return getEcology(Language.DEFAULT());
	}
	public String getEcology(Language language){
		LanguageString languageString = getEcologyAll().get(language);
		return (languageString == null ? null : languageString.getText());
	}
//	public String getEcologyPreferred(List<Language> languages){
//		LanguageString languageString = getEcologyAll().getPreferredLanguageString(languages);
//		return languageString.getText();
//	}
	@Transient
	public Map<Language, LanguageString> getEcologyAll(){
		if (ecology == null){
			try {
				ecology = initializeFieldObjectTextDataWithSupportTest(Feature.ECOLOGY(), false, false);
			} catch (DerivedUnitFacadeNotSupportedException e) {
				throw new IllegalStateException(notSupportMessage, e);
			}
			if (ecology == null){
				return new HashMap<Language, LanguageString>();
			}
		}
		return ecology.getMultilanguageText();
	}
	
	public void setEcology(String ecology){
		setEcology(ecology, null);
	}
	public void setEcology(String ecologyText, Language language){
		if (language == null){
			language = Language.DEFAULT();
		}
		if (ecology == null){
			try {
				ecology = initializeFieldObjectTextDataWithSupportTest(Feature.ECOLOGY(), true, false);
			} catch (DerivedUnitFacadeNotSupportedException e) {
				throw new IllegalStateException(notSupportMessage, e);
			}
		}
		if (ecologyText == null){
			ecology.removeText(language);
		}else{
			ecology.putText(ecologyText, language);
		}
	}
	public void removeEcology(Language language){
		setEcology(null, language);
	}
	/**
	 * Removes ecology for the default language
	 */
	public void removeEcology(){
		setEcology(null, null);
	}
	public void removeEcologyAll(){
		
	}

	
	//plant description
	@Transient
	public String getPlantDescription(){
		return getPlantDescription(null);
	}
	public String getPlantDescription(Language language){
		if (language == null){
			language = Language.DEFAULT();
		}
		LanguageString languageString = getPlantDescriptionAll().get(language);
		return (languageString == null ? null : languageString.getText());
	}
//	public String getPlantDescriptionPreferred(List<Language> languages){
//		LanguageString languageString = getPlantDescriptionAll().getPreferredLanguageString(languages);
//		return languageString.getText();
//	}
	@Transient
	public Map<Language, LanguageString> getPlantDescriptionAll(){
		if (plantDescription == null){
			try {
				plantDescription = initializeFieldObjectTextDataWithSupportTest(Feature.DESCRIPTION(), false, false);
			} catch (DerivedUnitFacadeNotSupportedException e) {
				throw new IllegalStateException(notSupportMessage, e);
			}
			if (plantDescription == null){
				return new HashMap<Language, LanguageString>();
			}
		}
		return plantDescription.getMultilanguageText();
	}
	public void setPlantDescription(String plantDescription){
		setPlantDescription(plantDescription, null);
	}
	public void setPlantDescription(String plantDescriptionText, Language language){
		if (language == null){
			language = Language.DEFAULT();
		}
		if (plantDescription == null){
			try {
				plantDescription = initializeFieldObjectTextDataWithSupportTest(Feature.DESCRIPTION(), true, false);
			} catch (DerivedUnitFacadeNotSupportedException e) {
				throw new IllegalStateException(notSupportMessage, e);
			}
		}
		if (plantDescriptionText == null){
			plantDescription.removeText(language);
		}else{
			plantDescription.putText(plantDescriptionText, language);
		}
	}
	public void removePlantDescription(Language language){
		setPlantDescription(null, language);
	}
	
	//field object definition
	public void addFieldObjectDefinition(String text, Language language) {
		getFieldObservation(true).addDefinition(text, language);
	}
	@Transient
	public Map<Language, LanguageString> getFieldObjectDefinition() {
		if (! hasFieldObservation()){
			return new HashMap<Language, LanguageString>();
		}else{
			return getFieldObservation(true).getDefinition();
		}
	}
	public String getFieldObjectDefinition(Language language) {
		Map<Language, LanguageString> map = getFieldObjectDefinition();
		LanguageString languageString = (map == null? null : map.get(language));
		if (languageString != null){
			return languageString.getText();
		}else {
			return null;
		}
	}
	public void removeFieldObjectDefinition(Language lang) {
		if (hasFieldObservation()){
			getFieldObservation(true).removeDefinition(lang);
		}
	}
	

	//media
	public boolean addFieldObjectMedia(Media media)  {
		try {
			return addMedia(media, getFieldObservation(true));
		} catch (DerivedUnitFacadeNotSupportedException e) {
			throw new IllegalStateException(notSupportMessage, e);
		}
	}
	/**
	 * Returns true, if an image gallery for the field object exists.<BR>
	 * Returns also <code>true</code> if the image gallery is empty. 
	 * @return
	 */
	public boolean hasFieldObjectImageGallery(){
		if (! hasFieldObject()){
			return false;
		}else{
			return (getImageGallery(fieldObservation, false) != null);
		}
	}
	
	/**
	 * @param createIfNotExists
	 * @return
	 */
	public SpecimenDescription getFieldObjectImageGallery(boolean createIfNotExists){
		TextData textData;
		try {
			textData = initializeFieldObjectTextDataWithSupportTest(Feature.IMAGE(), createIfNotExists, true);
		} catch (DerivedUnitFacadeNotSupportedException e) {
			throw new IllegalStateException(notSupportMessage, e);
		}
		if (textData != null){
			return CdmBase.deproxy(textData.getInDescription(), SpecimenDescription.class);
		}else{
			return null;
		}
	}
	/**
	 * Returns the media for the field object.<BR>
	 * @return
	 */
	@Transient
	public List<Media> getFieldObjectMedia() {
		try {
			List<Media> result = getMedia(getFieldObservation(false), false);
			return result == null ? new ArrayList<Media>() : result;
		} catch (DerivedUnitFacadeNotSupportedException e) {
			throw new IllegalStateException(notSupportMessage, e);
		}
	}
	public boolean removeFieldObjectMedia(Media media) {
		try {
			return removeMedia(media, getFieldObservation(false));
		} catch (DerivedUnitFacadeNotSupportedException e) {
			throw new IllegalStateException(notSupportMessage, e);
		}
	}

	//field number
	@Transient
	public String getFieldNumber() {
		if (! hasFieldObservation()){
			return null;
		}else{
			return getFieldObservation(true).getFieldNumber();
		}
	}
	public void setFieldNumber(String fieldNumber) {
		getFieldObservation(true).setFieldNumber(fieldNumber);
	}

	
	//field notes
	@Transient
	public String getFieldNotes() {
		if (! hasFieldObservation()){
			return null;
		}else{
			return getFieldObservation(true).getFieldNotes();
		}
	}
	public void setFieldNotes(String fieldNotes) {
		getFieldObservation(true).setFieldNotes(fieldNotes);
	}


	//individual counts
	@Transient
	public Integer getIndividualCount() {
		return (hasFieldObservation()? getFieldObservation(true).getIndividualCount() : null );
	}
	public void setIndividualCount(Integer individualCount) {
		getFieldObservation(true).setIndividualCount(individualCount);
	}

	//life stage
	@Transient
	public Stage getLifeStage() {
		return (hasFieldObservation()? getFieldObservation(true).getLifeStage() : null );
	}
	public void setLifeStage(Stage lifeStage) {
		getFieldObservation(true).setLifeStage(lifeStage);
	}

	//sex
	@Transient
	public Sex getSex() {
		return (hasFieldObservation()? getFieldObservation(true).getSex() : null );
	}
	public void setSex(Sex sex) {
		getFieldObservation(true).setSex(sex);
	}
	
	
	//field observation
	public boolean hasFieldObservation(){
		return (getFieldObservation(false) != null);
	}
	
	/**
	 * Returns the field observation as an object.
	 * @return
	 */
	@Transient
	public FieldObservation getFieldObservation(){
		return getFieldObservation(false);
	}

	/**
	 * Returns the field observation as an object.
	 * @return
	 */
	public FieldObservation getFieldObservation(boolean createIfNotExists){
		if (fieldObservation == null && createIfNotExists){
			fieldObservation = FieldObservation.NewInstance();
			fieldObservation.addPropertyChangeListener(getNewEventPropagationListener());
			DerivationEvent derivationEvent = getDerivationEvent(true);
			derivationEvent.addOriginal(fieldObservation);
		}
		return this.fieldObservation;
	}

	


	
//****************** Specimen **************************************************	
	
	//Definition
	public void addDerivedUnitDefinition(String text, Language language) {
		derivedUnit.addDefinition(text, language);
	}
	@Transient
	public Map<Language, LanguageString> getDerivedUnitDefinitions(){
		return this.derivedUnit.getDefinition();
	}
	public String getDerivedUnitDefinition(Language language) {
		Map<Language,LanguageString> languageMap = derivedUnit.getDefinition();
		LanguageString languageString = languageMap.get(language);
		if (languageString != null){
			return languageString.getText();
		}else {
			return null;
		}
	}
	public void removeDerivedUnitDefinition(Language lang) {
		derivedUnit.removeDefinition(lang);
	}

	//Determination
	public void addDetermination(DeterminationEvent determination) {
		derivedUnit.addDetermination(determination);
	}
	@Transient
	public Set<DeterminationEvent> getDeterminations() {
		return derivedUnit.getDeterminations();
	}
	public void removeDetermination(DeterminationEvent determination) {
		derivedUnit.removeDetermination(determination);
	}
	
	//Media
	public boolean addDerivedUnitMedia(Media media)  {
		try {
			return addMedia(media, derivedUnit);
		} catch (DerivedUnitFacadeNotSupportedException e) {
			throw new IllegalStateException(notSupportMessage, e);
		}
	}
	/**
	 * Returns true, if an image gallery exists for the specimen.<BR>
	 * Returns also <code>true</code> if the image gallery is empty. 
	 */
	public boolean hasDerivedUnitImageGallery(){
		return (getImageGallery(derivedUnit, false) != null);
	}
	
	public SpecimenDescription getDerivedUnitImageGallery(boolean createIfNotExists){
		TextData textData;
		try {
			textData = inititialzeTextDataWithSupportTest(Feature.IMAGE(), derivedUnit, createIfNotExists, true);
		} catch (DerivedUnitFacadeNotSupportedException e) {
			throw new IllegalStateException(notSupportMessage, e);
		}
		if (textData != null){
			return CdmBase.deproxy(textData.getInDescription(), SpecimenDescription.class);
		}else{
			return null;
		}
	}
	
	/**
	 * Returns the media for the specimen.<BR>
	 * @return
	 */
	@Transient
	public List<Media> getDerivedUnitMedia() {
		try {
			List<Media> result = getMedia(derivedUnit, false);
			return result == null ? new ArrayList<Media>() : result;
		} catch (DerivedUnitFacadeNotSupportedException e) {
			throw new IllegalStateException(notSupportMessage, e);
		}
	}
	public boolean removeDerivedUnitMedia(Media media) {
		try {
			return removeMedia(media, derivedUnit);
		} catch (DerivedUnitFacadeNotSupportedException e) {
			throw new IllegalStateException(notSupportMessage, e);
		}
	}

	
	//Accession Number
	@Transient
	public String getAccessionNumber() {
		return derivedUnit.getAccessionNumber();
	}
	public void setAccessionNumber(String accessionNumber) {
		derivedUnit.setAccessionNumber(accessionNumber);
	}

	@Transient
	public String getCatalogNumber() {
		return derivedUnit.getCatalogNumber();
	}
	public void setCatalogNumber(String catalogNumber) {
		derivedUnit.setCatalogNumber(catalogNumber);
	}

	@Transient
	public String getBarcode() {
		return derivedUnit.getBarcode();
	}
	public void setBarcode(String barcode) {
		derivedUnit.setCatalogNumber(barcode);
	}

	
	//Preservation Method
	
	/**
	 * Only supported by specimen and fossils
	 * @see #DerivedUnitType
	 * @return
	 */
	@Transient
	public PreservationMethod getPreservationMethod() throws MethodNotSupportedByDerivedUnitTypeException {
		if (derivedUnit.isInstanceOf(Specimen.class)){
			return CdmBase.deproxy(derivedUnit, Specimen.class).getPreservation();
		}else{
			if (this.config.isThrowExceptionForNonSpecimenPreservationMethodRequest()){
				throw new MethodNotSupportedByDerivedUnitTypeException("A preservation method is only available in derived units of type 'Specimen' or 'Fossil'");
			}else{
				return null;
			}
		}
	}
	/**
	 * Only supported by specimen and fossils
	 * @see #DerivedUnitType
	 * @return
	 */
	public void setPreservationMethod(PreservationMethod preservation)throws MethodNotSupportedByDerivedUnitTypeException  {
		if (derivedUnit.isInstanceOf(Specimen.class)){
			CdmBase.deproxy(derivedUnit, Specimen.class).setPreservation(preservation);
		}else{
			if (this.config.isThrowExceptionForNonSpecimenPreservationMethodRequest()){
				throw new MethodNotSupportedByDerivedUnitTypeException("A preservation method is only available in derived units of type 'Specimen' or 'Fossil'");
			}else{
				return;
			}
		}
	}

	//Stored under name
	@Transient
	public TaxonNameBase getStoredUnder() {
		return derivedUnit.getStoredUnder();
	}
	public void setStoredUnder(TaxonNameBase storedUnder) {
		derivedUnit.setStoredUnder(storedUnder);
	}

	//colletors number
	@Transient
	public String getCollectorsNumber() {
		return derivedUnit.getCollectorsNumber();
	}
	public void setCollectorsNumber(String collectorsNumber) {
		this.derivedUnit.setCollectorsNumber(collectorsNumber);
	}

	//title cache
	public String getTitleCache() {
		if (! derivedUnit.isProtectedTitleCache()){
			//always compute title cache anew as long as there are no property change listeners on 
			//field observation, gathering event etc 
			derivedUnit.setTitleCache(null, false);
		}
		return this.derivedUnit.getTitleCache();
	}
	public void setTitleCache(String titleCache, boolean isProtected) {
		this.derivedUnit.setTitleCache(titleCache, isProtected);
	}


	/**
	 * Returns the derived unit itself.
	 * @return the derived unit
	 */
	@Transient
	public DerivedUnitBase getDerivedUnit() {
		return this.derivedUnit;
	}
	
	private boolean hasDerivationEvent(){
		return getDerivationEvent() == null ? false : true;
	}
	private DerivationEvent getDerivationEvent(){
		return getDerivationEvent(false);
	}
	private DerivationEvent getDerivationEvent(boolean createIfNotExists){
		DerivationEvent result = derivedUnit.getDerivedFrom();
		if (result == null){
			result = DerivationEvent.NewInstance();
			derivedUnit.setDerivedFrom(result);
		}
		return result;
	}
	@Transient
	public String getExsiccatum() {
		logger.warn("Exsiccatum method not yet supported. Needs model change");
		return null;
	}
	
	public String setExsiccatum() throws MethodNotSupportedException{
		throw new MethodNotSupportedException("Exsiccatum method not yet supported. Needs model change");
	}
	
	
	// **** sources **/
	public void addSource(IdentifiableSource source){
		this.derivedUnit.addSource(source);
	}
	/**
	 * Creates an orignal source, adds it to the specimen and returns it.
	 * @param reference
	 * @param microReference
	 * @param originalNameString
	 * @return
	 */
	public IdentifiableSource addSource(ReferenceBase reference, String microReference, String originalNameString){
		IdentifiableSource source = IdentifiableSource.NewInstance(reference, microReference);
		source.setOriginalNameString(originalNameString);
		derivedUnit.addSource(source);
		return source;
	}

	@Transient
	public Set<IdentifiableSource> getSources(){
		return derivedUnit.getSources();
	}

	public void removeSource(IdentifiableSource source){
		this.derivedUnit.removeSource(source);
	}
	
	
	/**
	 * @return the collection
	 */
	@Transient
	public Collection getCollection() {
		return derivedUnit.getCollection();
	}


	/**
	 * @param collection the collection to set
	 */
	public void setCollection(Collection collection) {
		derivedUnit.setCollection(collection);
	}
	
	//annotation	
	public void addAnnotation(Annotation annotation){
		this.derivedUnit.addAnnotation(annotation);
	}

	@Transient
	public void getAnnotations(){
		this.derivedUnit.getAnnotations();
	}
	
	public void removeAnnotation(Annotation annotation){
		this.derivedUnit.removeAnnotation(annotation);
	}
	
	
// ******************************* Events *********************************************
	
	/**
	 * @return
	 */
	private PropertyChangeListener getNewEventPropagationListener() {
		PropertyChangeListener listener = new PropertyChangeListener(){
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				derivedUnit.firePropertyChange(event);
			}
			
		};
		return listener;
	}

		
	

//**************** Other Collections ***************************************************	
	
	/**
	 * Creates a duplicate specimen which derives from the same derivation event
	 * as the facade specimen and adds collection data to it (all data available in
	 * DerivedUnitBase and Specimen. Data from SpecimenOrObservationBase and above
	 * are not yet shared at the moment. 
	 * @param collection
	 * @param catalogNumber
	 * @param accessionNumber
	 * @param collectorsNumber
	 * @param storedUnder
	 * @param preservation
	 * @return
	 */
	public Specimen addDuplicate(Collection collection, String catalogNumber, String accessionNumber, 
				String collectorsNumber, TaxonNameBase storedUnder, PreservationMethod preservation){
		Specimen duplicate = Specimen.NewInstance();
		duplicate.setDerivedFrom(getDerivationEvent(true));
		duplicate.setCollection(collection);
		duplicate.setCatalogNumber(catalogNumber);
		duplicate.setAccessionNumber(accessionNumber);
		duplicate.setCollectorsNumber(collectorsNumber);
		duplicate.setStoredUnder(storedUnder);
		duplicate.setPreservation(preservation);
		return duplicate;
	}
	
	public void addDuplicate(DerivedUnitBase duplicateSpecimen){
		//TODO check derivedUnitType
		getDerivationEvent(true).addDerivative(duplicateSpecimen);  
	}
	
	@Transient
	public Set<Specimen> getDuplicates(){
		Set<Specimen> result = new HashSet<Specimen>();
		if (hasDerivationEvent()){
			for (DerivedUnitBase derivedUnit: getDerivationEvent(true).getDerivatives()){
				if (derivedUnit.isInstanceOf(Specimen.class) && ! derivedUnit.equals(this.derivedUnit)){
					result.add(CdmBase.deproxy(derivedUnit, Specimen.class));
				}
			}
		}
		return result;
	}
	public void removeDuplicate(Specimen duplicateSpecimen){
		if (hasDerivationEvent()){
			getDerivationEvent(true).removeDerivative(duplicateSpecimen);
		}
	}
	
	
	
	
}
