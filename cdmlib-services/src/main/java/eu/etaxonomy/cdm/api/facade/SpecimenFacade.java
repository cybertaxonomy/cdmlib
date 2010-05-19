/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.facade;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageText;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.Sex;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.Stage;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

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
public class SpecimenFacade {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SpecimenFacade.class);
	
	private static final String notSupportMessage = "A specimen facade not supported exception has occurred at place where this should not have happened. The developer should implement not support check properly during clas initialization ";
	
	private GatheringEvent gatheringEvent;

	private FieldObservation fieldObservation;
	private Specimen specimen;
	private List<Media> specimenMedia;
	private List<Media> fieldObservationMedia;
	
	private TextData ecology;
	private TextData plantDescription;
	
	public static SpecimenFacade NewInstance(){
		return new SpecimenFacade();
	}

	public static SpecimenFacade NewInstance(Specimen specimen) throws SpecimenFacadeNotSupportedException{
		return new SpecimenFacade(specimen);
	}

// ****************** CONSTRUCTOR ****************************************************
	
	private SpecimenFacade(){
		//gatheringEvent
		gatheringEvent = GatheringEvent.NewInstance();
		
		//observation
		fieldObservation = FieldObservation.NewInstance();
		fieldObservation.setGatheringEvent(gatheringEvent);
		
		//derivationEvent
		DerivationEvent derivationEvent = DerivationEvent.NewInstance();
		derivationEvent.addOriginal(fieldObservation);
		
		//derivedUnit
		specimen = Specimen.NewInstance();
		derivationEvent.addDerivative(specimen);
		
		//image galleries
		//specimenImageGallery = SpecimenDescription.NewInstance(specimen);
		//fieldObservationImageGallery = SpecimenDescription.NewInstance(fieldObservation);
	}
	
	private SpecimenFacade(Specimen specimen) throws SpecimenFacadeNotSupportedException{
		//specimen
		this.specimen = specimen;
		//derivation event
		DerivationEvent derivationEvent = getDerivationEvent();
		//fieldObservation
		Set<FieldObservation> fieldOriginals = getFieldObservationsOriginals(derivationEvent, null);
		if (fieldOriginals.size() > 1){
			throw new SpecimenFacadeNotSupportedException("Specimen must not have more than 1 derivation event");
		}else if (fieldOriginals.size() == 0){
			fieldObservation = FieldObservation.NewInstance();
		}else if (fieldOriginals.size() == 1){
			fieldObservation = fieldOriginals.iterator().next();
		}else{
			throw new IllegalStateException("Illegal state");
		}
		//gatheringEvent
		if (fieldObservation.getGatheringEvent() == null ){
			gatheringEvent = GatheringEvent.NewInstance();
			fieldObservation.setGatheringEvent(gatheringEvent);
		}
		//media
		//initalize only if necessary
		//specimenMedia = getImageGalleryMedia(specimen, "Specimen");
		//fieldObservationMedia = getImageGalleryMedia(fieldObservation, "Field observation");

		//test if unsupported
		//specimen
		String specimenExceptionText = "Specimen";
		SpecimenDescription imageGallery = getImageGalleryWithSupportTest(specimen, specimenExceptionText, false);
		getImageTextDataWithSupportTest(imageGallery, specimenExceptionText);
		//field observation
		specimenExceptionText = "Field observation";
		imageGallery = getImageGalleryWithSupportTest(fieldObservation, specimenExceptionText, false);
		getImageTextDataWithSupportTest(imageGallery, specimenExceptionText);
		
		//test if descriptions are supported
		ecology = initializeFieldObjectTextDataWithSupportTest(Feature.ECOLOGY(), false);
		plantDescription = initializeFieldObjectTextDataWithSupportTest(Feature.DESCRIPTION(), false);
		
		
	}

/**
	 * @param b
 * @throws SpecimenFacadeNotSupportedException 
	 */
	private TextData initializeFieldObjectTextDataWithSupportTest(Feature feature, boolean createIfNotExists) throws SpecimenFacadeNotSupportedException {
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
		Set<SpecimenDescription> descriptions = fieldObject.getSpecimenDescriptions();
		if (createIfNotExists && descriptions.size() == 0){
			SpecimenDescription newSpecimenDescription = SpecimenDescription.NewInstance(fieldObject);
			newSpecimenDescription.addElement(textData);
			return textData;
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
			throw new SpecimenFacadeNotSupportedException("Specimen facade does not support more than one description text data of type " + feature.getLabel());
			
		}else if (existingTextData.size() == 1){
			return CdmBase.deproxy(existingTextData.iterator().next(), TextData.class);
		}else{
			return textData;
		}
	}

//************************** METHODS *****************************************	

	private List<Media> setSpecimenImageGalleryMedia() throws SpecimenFacadeNotSupportedException{
		if (specimenMedia == null){
			specimenMedia = getImageGalleryMedia(specimen, "Specimen");
		}
		return specimenMedia;
	}

	private List<Media> setObservationImageGalleryMedia() throws SpecimenFacadeNotSupportedException{
		if (fieldObservationMedia == null){
			fieldObservationMedia = getImageGalleryMedia(fieldObservation, "Field observation");
		}
		return fieldObservationMedia;
	}

	
	
	/**
	 * @param derivationEvent2
	 * @return
	 */
	private Set<FieldObservation> getFieldObservationsOriginals(DerivationEvent derivationEvent, Set<SpecimenOrObservationBase> recursionAvoidSet) {
		if (recursionAvoidSet == null){
			recursionAvoidSet = new HashSet<SpecimenOrObservationBase>();
		}
		Set<FieldObservation> result = new HashSet<FieldObservation>();
		Set<SpecimenOrObservationBase> originals = derivationEvent.getOriginals();
		for (SpecimenOrObservationBase original : originals){
			if (original.isInstanceOf(FieldObservation.class)){
				result.add(CdmBase.deproxy(original, FieldObservation.class));
			}else{
				//if specimen has already been tested exclude it from further recursion
				if (recursionAvoidSet.contains(original)){
					continue;
				}
				Set<DerivationEvent> derivationEvents = original.getDerivationEvents(); 
				for (DerivationEvent originalDerivation : derivationEvents){
					Set<FieldObservation> fieldObservations = getFieldObservationsOriginals(originalDerivation, recursionAvoidSet);
					result.addAll(fieldObservations);
				}
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
	 * @throws SpecimenFacadeNotSupportedException
	 */
	private List<Media> getImageGalleryMedia(SpecimenOrObservationBase specimen, String specimenExceptionText) throws SpecimenFacadeNotSupportedException{
		List<Media> result;
		SpecimenDescription imageGallery = getImageGalleryWithSupportTest(specimen, specimenExceptionText, true);
		TextData textData = getImageTextDataWithSupportTest(imageGallery, specimenExceptionText);
		result = textData.getMedia();
		return result;
	}
	
	
	/**
	 * Returns the image gallery of the according specimen. Throws an exception if the attached
	 * image gallerie(s) are not supported by this facade.
	 * If no image gallery exists a new one is created if <code>createNewIfNotExists</code> is true.
	 * @param specimen
	 * @param specimenText
	 * @param createNewIfNotExists
	 * @return
	 * @throws SpecimenFacadeNotSupportedException
	 */
	private SpecimenDescription getImageGalleryWithSupportTest(SpecimenOrObservationBase<?> specimen, String specimenText, boolean createNewIfNotExists) throws SpecimenFacadeNotSupportedException{
		SpecimenDescription imageGallery;
		if (hasMultipleImageGalleries(specimen)){
			throw new SpecimenFacadeNotSupportedException( specimenText + " must not have more than 1 image gallery");
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
	 * @throws SpecimenFacadeNotSupportedException 
	 */
	private TextData getImageTextDataWithSupportTest(SpecimenDescription imageGallery, String specimenText) throws SpecimenFacadeNotSupportedException {
		if (imageGallery == null){
			return null;
		}
		TextData textData = null;
		for (DescriptionElementBase element: imageGallery.getElements()){
			if (element.isInstanceOf(TextData.class) && element.getFeature().equals(Feature.IMAGE())){
				if (textData != null){
					throw new SpecimenFacadeNotSupportedException( specimenText + " must not have more than 1 image text data element in image gallery");
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
	 * @param specimen
	 * @return
	 */
	private boolean hasMultipleImageGalleries(SpecimenOrObservationBase<?> specimen){
		int count = 0;
		Set<SpecimenDescription> descriptions= specimen.getSpecimenDescriptions();
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
	 * @throws SpecimenFacadeNotSupportedException
	 */
	private boolean addMedia(Media media, SpecimenOrObservationBase<?> specimen) throws SpecimenFacadeNotSupportedException {
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
	 * @throws SpecimenFacadeNotSupportedException
	 */
	private boolean removeMedia(Media media, SpecimenOrObservationBase<?> specimen) throws SpecimenFacadeNotSupportedException {
		List<Media> mediaList = getMedia(specimen);
		return mediaList.remove(media);
	}

	/**
	 * Returns the one media list of a specimen which is part of the only image gallery that 
	 * this specimen is part of.<BR>
	 * If these conditions are not hold an exception is thrwon.
	 * @param specimen
	 * @return
	 * @throws SpecimenFacadeNotSupportedException
	 */
	private List<Media> getMedia(SpecimenOrObservationBase<?> specimen) throws SpecimenFacadeNotSupportedException {
		if (specimen == null){
			return null;
		}
		if (specimen == this.specimen){
			return setSpecimenImageGalleryMedia();
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
	public Set<NamedArea> getCollectingAreas() {
		return getGatheringEvent().getCollectingAreas();
	}
	public void removeCollectingArea(NamedArea area) {
		getGatheringEvent().removeCollectingArea(area);
	}

	//absolute elevation
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
		return locality.getText();
	}
	public Language getLocalityLanguage(){
		LanguageString locality = getGatheringEvent().getLocality();
		return locality.getLanguage();
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
			} catch (SpecimenFacadeNotSupportedException e) {
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
			} catch (SpecimenFacadeNotSupportedException e) {
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
			} catch (SpecimenFacadeNotSupportedException e) {
				throw new IllegalStateException(notSupportMessage, e);
			}
		}
		return ecology.getMultilanguageText();
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
			} catch (SpecimenFacadeNotSupportedException e) {
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
		} catch (SpecimenFacadeNotSupportedException e) {
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
		} catch (SpecimenFacadeNotSupportedException e) {
			throw new IllegalStateException(notSupportMessage, e);
		}
	}
	public boolean removeFieldObjectMedia(Media media) {
		try {
			return removeMedia(media, getFieldObservation());
		} catch (SpecimenFacadeNotSupportedException e) {
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
	public void addSpecimenDefinition(String text, Language language) {
		specimen.addDefinition(text, language);
	}
	public Map<Language, LanguageString> getSpecimenDefinitions(){
		return specimen.getDefinition();
	}
	public String getSpecimenDefinition(Language language) {
		LanguageString languageString = specimen.getDefinition().get(language);
		if (languageString != null){
			return languageString.getText();
		}else {
			return null;
		}
	}
	public void removeSpecimenDefinition(Language lang) {
		specimen.removeDefinition(lang);
	}

	//Determination
	public void addDetermination(DeterminationEvent determination) {
		specimen.addDetermination(determination);
	}
	public Set<DeterminationEvent> getDeterminations() {
		return specimen.getDeterminations();
	}
	public void removeDetermination(DeterminationEvent determination) {
		specimen.removeDetermination(determination);
	}
	
	//Media
	public boolean addSpecimenMedia(Media media)  {
		try {
			return addMedia(media, specimen);
		} catch (SpecimenFacadeNotSupportedException e) {
			throw new IllegalStateException(notSupportMessage, e);
		}
	}
	/**
	 * Returns true, if an image gallery exists for the specimen.<BR>
	 * Returns also <code>true</code> if the image gallery is empty. 
	 */
	public boolean hasSpecimenImageGallery(){
		return (getImageGallery(specimen, false) != null);
	}	
	/**
	 * Returns the media for the specimen.<BR>
	 * Please handle with care as <B>this method will create an empty image gallery</B>
	 * if it does not yet exist. Use {@link #hasFieldObjectImageGallery()} first
	 * to test if an image gallery exists at all to avoid creating an empty gallery.
	 * @return
	 */
	public List<Media> getSpecimenMedia() {
		try {
			return getMedia(specimen);
		} catch (SpecimenFacadeNotSupportedException e) {
			throw new IllegalStateException(notSupportMessage, e);
		}
	}
	public boolean removeSpecimenMedia(Media media) {
		try {
			return removeMedia(media, specimen);
		} catch (SpecimenFacadeNotSupportedException e) {
			throw new IllegalStateException(notSupportMessage, e);
		}
	}

	
	//Accession Number
	public String getAccessionNumber() {
		return specimen.getAccessionNumber();
	}
	public void setAccessionNumber(String accessionNumber) {
		specimen.setAccessionNumber(accessionNumber);
	}

	//Catalog Number
	public String getCatalogNumber() {
		return specimen.getCatalogNumber();
	}
	public void setCatalogNumber(String catalogNumber) {
		specimen.setCatalogNumber(catalogNumber);
	}

	//Preservation Method
	public PreservationMethod getPreservationMethod() {
		return specimen.getPreservation();
	}
	public void setPreservationMethod(PreservationMethod preservation) {
		specimen.setPreservation(preservation);
	}

	//Stored under name
	public TaxonNameBase getStoredUnder() {
		return specimen.getStoredUnder();
	}
	public void setStoredUnder(TaxonNameBase storedUnder) {
		specimen.setStoredUnder(storedUnder);
	}

	//colletors number
	public String getCollectorsNumber() {
		return specimen.getCollectorsNumber();
	}
	public void setCollectorsNumber(String collectorsNumber) {
		this.specimen.setCollectorsNumber(collectorsNumber);
	}

	//title cache
	public String getTitleCache() {
		if (! specimen.isProtectedTitleCache()){
			//always compute title cache anew as long as there are no property change listeners on 
			//field observation, gathering event etc 
			specimen.setTitleCache(null, false);
		}
		return this.specimen.getTitleCache();
	}
	public void setTitleCache(String titleCache, boolean isProtected) {
		this.specimen.setTitleCache(titleCache, isProtected);
	}


	/**
	 * Returns the specimen itself.
	 * @return the specimen
	 */
	public Specimen getSpecimen() {
		return this.specimen;
	}
	
	private DerivationEvent getDerivationEvent(){
		DerivationEvent result = specimen.getDerivedFrom();
		if (result == null){
			result = DerivationEvent.NewInstance();
		}
		return result;
	}


//**************** Collection ***************************************************	
	
	/**
	 * @return the collection
	 */
	public Collection getCollection() {
		return specimen.getCollection();
	}


	/**
	 * @param collection the collection to set
	 */
	public void setCollection(Collection collection) {
		specimen.setCollection(collection);
	}
	
	
	
}
