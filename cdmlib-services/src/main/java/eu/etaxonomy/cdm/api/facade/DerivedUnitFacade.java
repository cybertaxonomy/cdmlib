/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.facade;

import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MethodNotSupportedException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.agent.AgentBase;
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
	private List<Media> specimenMedia;
	private List<Media> fieldObservationMedia;
	
	private TextData ecology;
	private TextData plantDescription;
	
	public static DerivedUnitFacade NewInstance(DerivedUnitType type){
		return new DerivedUnitFacade(type);
	}

	public static DerivedUnitFacade NewInstance(DerivedUnitBase derivedUnit) throws DerivedUnitFacadeNotSupportedException{
		return new DerivedUnitFacade(derivedUnit, null, DerivedUnitType.Specimen);
	}
	
	public static DerivedUnitFacade NewInstance(DerivedUnitBase derivedUnit, DerivedUnitFacadeConfigurator config) throws DerivedUnitFacadeNotSupportedException{
		return new DerivedUnitFacade(derivedUnit, config, DerivedUnitType.Specimen);
	}

// ****************** CONSTRUCTOR ****************************************************
	
	private DerivedUnitFacade(DerivedUnitType type){
		this.config = DerivedUnitFacadeConfigurator.NewInstance();
		
		//gatheringEvent
//		GatheringEvent gatheringEvent = GatheringEvent.NewInstance();
//		
//		//observation
//		fieldObservation = FieldObservation.NewInstance();
//		fieldObservation.setGatheringEvent(gatheringEvent);
//		
//		//derivationEvent
//		DerivationEvent derivationEvent = DerivationEvent.NewInstance();
//		derivationEvent.addOriginal(fieldObservation);
		
		//derivedUnit
		derivedUnit = type.getNewDerivedUnitInstance();
		setCacheStrategy();
//		derivationEvent.addDerivative(specimen);
		
		//image galleries
		//specimenImageGallery = SpecimenDescription.NewInstance(specimen);
		//fieldObservationImageGallery = SpecimenDescription.NewInstance(fieldObservation);
	}

	/**
	 * 
	 */
	private void setCacheStrategy() {
		derivedUnit.setCacheStrategy(new DerivedUnitFacadeCacheStrategy());
	}
	
	private DerivedUnitFacade(DerivedUnitBase derivedUnit, DerivedUnitFacadeConfigurator config, DerivedUnitType type) throws DerivedUnitFacadeNotSupportedException{
		//this.type = type;  ??
		
		if (config == null){
			config = DerivedUnitFacadeConfigurator.NewInstance();
		}
		this.config = config;
		
		//derived unit
		this.derivedUnit = derivedUnit;
		setCacheStrategy();
		
		//derivation event
		if (this.derivedUnit.getDerivedFrom() != null){
			DerivationEvent derivationEvent = getDerivationEvent();
			//fieldObservation
			Set<FieldObservation> fieldOriginals = getFieldObservationsOriginals(derivationEvent, null);
			if (fieldOriginals.size() > 1){
				throw new DerivedUnitFacadeNotSupportedException("Specimen must not have more than 1 derivation event");
			}else if (fieldOriginals.size() == 0){
				//fieldObservation = FieldObservation.NewInstance();
			}else if (fieldOriginals.size() == 1){
				fieldObservation = fieldOriginals.iterator().next();
			}else{
				throw new IllegalStateException("Illegal state");
			}	
		}
		
		//gatheringEvent
//		if (fieldObservation.getGatheringEvent() == null ){
//			GatheringEvent gatheringEvent = GatheringEvent.NewInstance();
//			fieldObservation.setGatheringEvent(gatheringEvent);
//		}
		
		//media
		//initalize only if necessary
		//specimenMedia = getImageGalleryMedia(specimen, "Specimen");
		//fieldObservationMedia = getImageGalleryMedia(fieldObservation, "Field observation");

		//test if unsupported
		//specimen
		String objectTypeExceptionText = "Specimen";
		SpecimenDescription imageGallery = getImageGalleryWithSupportTest(derivedUnit, objectTypeExceptionText, false);
		getImageTextDataWithSupportTest(imageGallery, objectTypeExceptionText);
		
		//field observation
		objectTypeExceptionText = "Field observation";
		imageGallery = getImageGalleryWithSupportTest(fieldObservation, objectTypeExceptionText, false);
		getImageTextDataWithSupportTest(imageGallery, objectTypeExceptionText);
		
		//direct media of derived unit
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
		
		//direct media of field observation
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
		ecology = initializeFieldObjectTextDataWithSupportTest(Feature.ECOLOGY(), false);
		plantDescription = initializeFieldObjectTextDataWithSupportTest(Feature.DESCRIPTION(), false);
	}

/**
	 * @param b
 * @throws DerivedUnitFacadeNotSupportedException 
	 */
	private TextData initializeFieldObjectTextDataWithSupportTest(Feature feature, boolean createIfNotExists) throws DerivedUnitFacadeNotSupportedException {
		if (feature == null){
			return null;
		}
		TextData textData = null;
		if (createIfNotExists){
			textData = TextData.NewInstance(feature);
		}
		
		//field object
		FieldObservation fieldObject = this.fieldObservation;
		if (fieldObject == null){
			if (createIfNotExists){
				fieldObject = getFieldObservation();
			}else{
				return null;
			}
		}
		Set<SpecimenDescription> descriptions = fieldObject.getSpecimenDescriptions(false);
		if (descriptions.size() == 0){
			if (createIfNotExists){
				SpecimenDescription newSpecimenDescription = SpecimenDescription.NewInstance(fieldObject);
				newSpecimenDescription.addElement(textData);
				return textData;
			}else{
				return null;
			}
		}
		Set<DescriptionElementBase> existingTextData = new HashSet<DescriptionElementBase>();
		for (SpecimenDescription description : descriptions){
			for (DescriptionElementBase element: description.getElements()){
				if (element.isInstanceOf(TextData.class) && feature.equals(element.getFeature()) ){
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

	private List<Media> setDerivedUnitImageGalleryMedia() throws DerivedUnitFacadeNotSupportedException{
		if (specimenMedia == null){
			specimenMedia = getImageGalleryMedia(derivedUnit, "Specimen");
		}
		return specimenMedia;
	}

	private List<Media> setObservationImageGalleryMedia() throws DerivedUnitFacadeNotSupportedException{
		if (fieldObservationMedia == null){
			fieldObservationMedia = getImageGalleryMedia(fieldObservation, "Field observation");
		}
		return fieldObservationMedia;
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
	
	/**
	 * Returns the media list for a specimen. Throws an exception if the existing specimen descriptions
	 * are not supported by this facade.
	 * @param specimen the specimen the media belongs to
	 * @param specimenExceptionText text describing the specimen for exception messages
	 * @return
	 * @throws DerivedUnitFacadeNotSupportedException
	 */
	private List<Media> getImageGalleryMedia(SpecimenOrObservationBase specimen, String specimenExceptionText) throws DerivedUnitFacadeNotSupportedException{
		List<Media> result;
		SpecimenDescription imageGallery = getImageGalleryWithSupportTest(specimen, specimenExceptionText, true);
		TextData textData = getImageTextDataWithSupportTest(imageGallery, specimenExceptionText);
		result = textData.getMedia();
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
	 * is <code>true</code>.
	 * @param createNewIfNotExists
	 * @return
	 */
	private SpecimenDescription getImageGallery(SpecimenOrObservationBase<?> specimen, boolean createNewIfNotExists) {
		SpecimenDescription result = null;
		Set<SpecimenDescription> descriptions= specimen.getSpecimenDescriptions();
		for (SpecimenDescription description : descriptions){
			if (description.isImageGallery()){
				result = description;
				break;
			}
		}
		if (result == null && createNewIfNotExists){
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
			List<Media> mediaList = getMedia(specimen);
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
		List<Media> mediaList = getMedia(specimen);
		return mediaList.remove(media);
	}

	/**
	 * Returns the one media list of a specimen which is part of the only image gallery that 
	 * this specimen is part of.<BR>
	 * If these conditions are not hold an exception is thrwon.
	 * @param specimen
	 * @return
	 * @throws DerivedUnitFacadeNotSupportedException
	 */
	private List<Media> getMedia(SpecimenOrObservationBase<?> specimen) throws DerivedUnitFacadeNotSupportedException {
		if (specimen == null){
			return null;
		}
		if (specimen == this.derivedUnit){
			return setDerivedUnitImageGalleryMedia();
		}else if (specimen == this.fieldObservation){
			return setObservationImageGalleryMedia();
		}else{
			return getImageGalleryMedia(specimen, "Undefined specimen ");
		}
	}
	
	
//****************** GETTER / SETTER / ADDER / REMOVER ***********************/	
	
// ****************** Gathering Event *********************************/
	
	//Collecting area
	public void addCollectingArea(NamedArea area) {
		getGatheringEvent().addCollectingArea(area);
	}
	public void addCollectingAreas(java.util.Collection<NamedArea> areas) {
		for (NamedArea area : areas){
			getGatheringEvent().addCollectingArea(area);
		}
	}
	public Set<NamedArea> getCollectingAreas() {
		return getGatheringEvent().getCollectingAreas();
	}
	public void removeCollectingArea(NamedArea area) {
		getGatheringEvent().removeCollectingArea(area);
	}

	//absolute elevation  
	/** meter above/below sea level of the surface 
	 * @see #getAbsoluteElevationError()
	 * @see #getAbsoluteElevationRange()
	 **/
	public Integer getAbsoluteElevation() {
		return getGatheringEvent().getAbsoluteElevation();
	}
	public void setAbsoluteElevation(Integer absoluteElevation) {
		getGatheringEvent().setAbsoluteElevation(absoluteElevation);
	}

	//absolute elevation error
	public Integer getAbsoluteElevationError() {
		return getGatheringEvent().getAbsoluteElevationError();
	}
	public void setAbsoluteElevationError(Integer absoluteElevationError) {
		getGatheringEvent().setAbsoluteElevationError(absoluteElevationError);
	}
	
	/**
	 * @see #getAbsoluteElevation()
	 * @see #getAbsoluteElevationError()
	 * @see #setAbsoluteElevationRange(Integer, Integer)
	 * @see #getAbsoluteElevationMaximum()
	 */
	public Integer getAbsoluteElevationMinimum(){
		Integer minimum = getGatheringEvent().getAbsoluteElevation();
		if (getGatheringEvent().getAbsoluteElevationError() != null){
			minimum = minimum -  getGatheringEvent().getAbsoluteElevationError();
		}
		return minimum;
	}
	/**
	 * @see #getAbsoluteElevation()
	 * @see #getAbsoluteElevationError()
	 * @see #setAbsoluteElevationRange(Integer, Integer)
	 * @see #getAbsoluteElevationMinimum()
	 */
	public Integer getAbsoluteElevationMaximum(){
		Integer maximum = getGatheringEvent().getAbsoluteElevation();
		if (getGatheringEvent().getAbsoluteElevationError() != null){
			maximum = maximum +  getGatheringEvent().getAbsoluteElevationError();
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
			getGatheringEvent().setAbsoluteElevation(elevation);
			getGatheringEvent().setAbsoluteElevationError(error);
		}else{
			if (! isEvenDistance(minimumElevation, maximumElevation) ){
				throw new IllegalArgumentException("Distance between minimum and maximum elevation must be even but was " + Math.abs(minimumElevation - maximumElevation));
			}
			Integer absoluteElevationError = Math.abs(maximumElevation - minimumElevation);
			absoluteElevationError = absoluteElevationError / 2;
			Integer absoluteElevation = minimumElevation + absoluteElevationError;
			getGatheringEvent().setAbsoluteElevation(absoluteElevation);
			getGatheringEvent().setAbsoluteElevationError(absoluteElevationError);
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
	public AgentBase getCollector() {
		return getGatheringEvent().getCollector();
	}
	public void setCollector(AgentBase collector){
		getGatheringEvent().setCollector(collector);
	}

	//collecting method
	public String getCollectingMethod() {
		return getGatheringEvent().getCollectingMethod();
	}
	public void setCollectingMethod(String collectingMethod) {
		getGatheringEvent().setCollectingMethod(collectingMethod);
	}

	//distance to ground
	public Integer getDistanceToGround() {
		return getGatheringEvent().getDistanceToGround();
	}
	public void setDistanceToGround(Integer distanceToGround) {
		getGatheringEvent().setDistanceToGround(distanceToGround);
	}

	//distance to water surface
	public Integer getDistanceToWaterSurface() {
		return getGatheringEvent().getDistanceToWaterSurface();
	}
	public void setDistanceToWaterSurface(Integer distanceToWaterSurface) {
		getGatheringEvent().setDistanceToWaterSurface(distanceToWaterSurface);
	}

	//exact location
	public Point getExactLocation() {
		return getGatheringEvent().getExactLocation();
	}
	public void setExactLocation(Point exactLocation) {
		getGatheringEvent().setExactLocation(exactLocation);
	}
	public void setExactLocationByParsing(String longitudeToParse, String latitudeToParse, ReferenceSystem referenceSystem, Integer errorRadius) throws ParseException{
		Point point = Point.NewInstance(null, null, referenceSystem, errorRadius);
		point.setLongitudeByParsing(longitudeToParse);
		point.setLatitudeByParsing(latitudeToParse);
		setExactLocation(point);
	}
	
	//gathering event description
	public String getGatheringEventDescription() {
		return getGatheringEvent().getDescription();
	}
	public void setGatheringEventDescription(String description) {
		getGatheringEvent().setDescription(description);
	}

	//gatering period
	public TimePeriod getGatheringPeriod() {
		return getGatheringEvent().getTimeperiod();
	}
	public void setGatheringPeriod(TimePeriod timeperiod) {
		getGatheringEvent().setTimeperiod(timeperiod);
	}

	//locality
	public LanguageString getLocality(){
		return getGatheringEvent().getLocality();
	}
	public String getLocalityText(){
		LanguageString locality = getGatheringEvent().getLocality();
		if(locality != null){
			return locality.getText();
		}
		return null;
	}
	public Language getLocalityLanguage(){
		LanguageString locality = getGatheringEvent().getLocality();
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
		getGatheringEvent().setLocality(locality);
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
		getFieldObservation().setGatheringEvent(gatheringEvent);
	}
	public GatheringEvent getGatheringEvent() {
		if (getFieldObservation().getGatheringEvent() == null){
			GatheringEvent gatheringEvent = GatheringEvent.NewInstance();
			getFieldObservation().setGatheringEvent(gatheringEvent);
		}
		return getFieldObservation().getGatheringEvent();
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
	public Map<Language, LanguageString> getEcologyAll(){
		if (ecology == null){
			try {
				ecology = initializeFieldObjectTextDataWithSupportTest(Feature.ECOLOGY(), true);
			} catch (DerivedUnitFacadeNotSupportedException e) {
				throw new IllegalStateException(notSupportMessage, e);
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
				ecology = initializeFieldObjectTextDataWithSupportTest(Feature.ECOLOGY(), true);
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
	public Map<Language, LanguageString> getPlantDescriptionAll(){
		if (plantDescription == null){
			try {
				plantDescription = initializeFieldObjectTextDataWithSupportTest(Feature.DESCRIPTION(), true);
			} catch (DerivedUnitFacadeNotSupportedException e) {
				throw new IllegalStateException(notSupportMessage, e);
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
				plantDescription = initializeFieldObjectTextDataWithSupportTest(Feature.DESCRIPTION(), true);
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
		getFieldObservation().addDefinition(text, language);
	}
	public Map<Language, LanguageString> getFieldObjectDefinition() {
		return getFieldObservation().getDefinition();
	}
	public String getFieldObjectDefinition(Language language) {
		LanguageString languageString = getFieldObservation().getDefinition().get(language);
		if (languageString != null){
			return languageString.getText();
		}else {
			return null;
		}
	}
	public void removeFieldObjectDefinition(Language lang) {
		getFieldObservation().removeDefinition(lang);
	}
	

	//media
	public boolean addFieldObjectMedia(Media media)  {
		try {
			return addMedia(media, getFieldObservation());
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
	 * Returns the media for the field object.<BR>
	 * Please handle with care as <B>this method will create an empty image gallery</B>
	 * if it does not yet exist. Use {@link #hasFieldObjectImageGallery()} first
	 * to test if an image gallery exists at all to avoid creating an empty gallery.
	 * @return
	 */
	public List<Media> getFieldObjectMedia() {
		try {
			return getMedia(getFieldObservation());
		} catch (DerivedUnitFacadeNotSupportedException e) {
			throw new IllegalStateException(notSupportMessage, e);
		}
	}
	public boolean removeFieldObjectMedia(Media media) {
		try {
			return removeMedia(media, getFieldObservation());
		} catch (DerivedUnitFacadeNotSupportedException e) {
			throw new IllegalStateException(notSupportMessage, e);
		}
	}

	//field number
	public String getFieldNumber() {
		return getFieldObservation().getFieldNumber();
	}
	public void setFieldNumber(String fieldNumber) {
		getFieldObservation().setFieldNumber(fieldNumber);
	}

	
	//field notes
	public String getFieldNotes() {
		return getFieldObservation().getFieldNotes();
	}
	public void setFieldNotes(String fieldNotes) {
		getFieldObservation().setFieldNotes(fieldNotes);
	}


	//individual counts
	public Integer getIndividualCount() {
		return getFieldObservation().getIndividualCount();
	}
	public void setIndividualCount(Integer individualCount) {
		getFieldObservation().setIndividualCount(individualCount);
	}

	//life stage
	public Stage getLifeStage() {
		return getFieldObservation().getLifeStage();
	}
	public void setLifeStage(Stage lifeStage) {
		getFieldObservation().setLifeStage(lifeStage);
	}

	//sex
	public Sex getSex() {
		return getFieldObservation().getSex();
	}
	public void setSex(Sex sex) {
		getFieldObservation().setSex(sex);
	}
	
	
	//field observation
	/**
	 * Returns the field observation as an object.
	 * @return
	 */
	public FieldObservation getFieldObservation(){
		if (fieldObservation == null){
			fieldObservation = FieldObservation.NewInstance();
			DerivationEvent derivationEvent = getDerivationEvent();
			derivationEvent.addOriginal(fieldObservation);
		}
		return this.fieldObservation;
	}


//****************** Specimen **************************************************	
	
	//Definition
	public void addDerivedUnitDefinition(String text, Language language) {
		derivedUnit.addDefinition(text, language);
	}
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
	public boolean hasSpecimenImageGallery(){
		return (getImageGallery(derivedUnit, false) != null);
	}	
	/**
	 * Returns the media for the specimen.<BR>
	 * Please handle with care as <B>this method will create an empty image gallery</B>
	 * if it does not yet exist. Use {@link #hasFieldObjectImageGallery()} first
	 * to test if an image gallery exists at all to avoid creating an empty gallery.
	 * @return
	 */
	public List<Media> getDerivedUnitMedia() {
		try {
			return getMedia(derivedUnit);
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
	public String getAccessionNumber() {
		return derivedUnit.getAccessionNumber();
	}
	public void setAccessionNumber(String accessionNumber) {
		derivedUnit.setAccessionNumber(accessionNumber);
	}

	//Catalog Number
	public String getCatalogNumber() {
		return derivedUnit.getCatalogNumber();
	}
	public void setCatalogNumber(String catalogNumber) {
		derivedUnit.setCatalogNumber(catalogNumber);
	}

	//Preservation Method
	
	/**
	 * Only supported by specimen and fossils
	 * @see #DerivedUnitType
	 * @return
	 */
	public PreservationMethod getPreservationMethod() throws MethodNotSupportedByDerivedUnitTypeException {
		if (derivedUnit.isInstanceOf(Specimen.class)){
			return CdmBase.deproxy(derivedUnit, Specimen.class).getPreservation();
		}else{
			throw new MethodNotSupportedByDerivedUnitTypeException("A preservation method is only available in derived units of type 'Specimen' or 'Fossil'");
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
			throw new MethodNotSupportedByDerivedUnitTypeException("A preservation method is only available in derived units of type 'Specimen' or 'Fossil'");
			
		}
	}

	//Stored under name
	public TaxonNameBase getStoredUnder() {
		return derivedUnit.getStoredUnder();
	}
	public void setStoredUnder(TaxonNameBase storedUnder) {
		derivedUnit.setStoredUnder(storedUnder);
	}

	//colletors number
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
	public DerivedUnitBase getDerivedUnit() {
		return this.derivedUnit;
	}
	
	private DerivationEvent getDerivationEvent(){
		DerivationEvent result = derivedUnit.getDerivedFrom();
		if (result == null){
			result = DerivationEvent.NewInstance();
			derivedUnit.setDerivedFrom(result);
		}
		return result;
	}
	
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
	
	public Set<IdentifiableSource> getSources(){
		return derivedUnit.getSources();
	}

	public void removeSource(IdentifiableSource source){
		this.derivedUnit.removeSource(source);
	}
	
	
	/**
	 * @return the collection
	 */
	public Collection getCollection() {
		return derivedUnit.getCollection();
	}


	/**
	 * @param collection the collection to set
	 */
	public void setCollection(Collection collection) {
		derivedUnit.setCollection(collection);
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
		duplicate.setDerivedFrom(getDerivationEvent());
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
		getDerivationEvent().addDerivative(duplicateSpecimen);  
	}
	public Set<Specimen> getDuplicates(){
		Set<Specimen> result = new HashSet<Specimen>();
		for (DerivedUnitBase derivedUnit: getDerivationEvent().getDerivatives()){
			if (derivedUnit.isInstanceOf(Specimen.class) && ! derivedUnit.equals(this.derivedUnit)){
				result.add(CdmBase.deproxy(derivedUnit, Specimen.class));
			}
		}
		return result;
	}
	public void removeDuplicate(Specimen duplicateSpecimen){
		getDerivationEvent().removeDerivative(duplicateSpecimen);  	
	}
	
	
	
	
}
