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
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.IOriginalSource;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Identifier;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * This class is a facade to the eu.etaxonomy.cdm.model.occurrence package from
 * a specimen based view. It does not support all functionality available in the
 * occurrence package.<BR>
 * The most significant restriction is that a specimen may derive only from one
 * direct derivation event and there must be only one field unit
 * (gathering event) it derives from.<BR>
 *
 * @author a.mueller
 * @since 14.05.2010
 */
public class DerivedUnitFacade {
	private static final String METER = "m";

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DerivedUnitFacade.class);

	private static final String notSupportMessage = "A specimen facade not supported exception has occurred at a place where this should not have happened. The developer should implement not support check properly during class initialization ";

	private static final boolean CREATE = true;
	private static final boolean CREATE_NOT = false;

	private final DerivedUnitFacadeConfigurator config;

	private final Map<PropertyChangeListener, CdmBase> listeners = new HashMap<>();

	// Either fieldUnit or derivedUnit must not be null.
	private FieldUnit fieldUnit;
	private final DerivedUnit derivedUnit;

	// media - the text data holding the media
	private TextData derivedUnitMediaTextData;
	private TextData fieldObjectMediaTextData;

	private TextData ecology;
	private TextData plantDescription;
	private TextData lifeform;

	/**
	 * Creates a derived unit facade for a new derived unit of type
	 * <code>type</code>.
	 *
	 * @param type
	 * @return
	 */
	public static DerivedUnitFacade NewInstance(SpecimenOrObservationType type) {
		return new DerivedUnitFacade(type, null, null);
	}

	   /**
     * Creates a derived unit facade for a new derived unit of type
     * {@link SpecimenOrObservationType#PreservedSpecimen}.
     *
     * @return the derived unit facade
     */
    public static DerivedUnitFacade NewPreservedSpecimenInstance() {
        return new DerivedUnitFacade(SpecimenOrObservationType.PreservedSpecimen, null, null);
    }

	/**
	 * Creates a derived unit facade for a new derived unit of type
	 * <code>type</code>.
	 *
	 * @param type
	 * @return
	 */
	public static DerivedUnitFacade NewInstance(SpecimenOrObservationType type, FieldUnit fieldUnit) {
		return new DerivedUnitFacade(type, fieldUnit, null);
	}

	/**
	 * Creates a derived unit facade for a new derived unit of type
	 * <code>type</code>.
	 *
	 * @param type
	 * @param fieldUnit the field unit to use
	 * @param config the facade configurator to use
	 * //TODO are there any ambiguities to solve with defining a field unit or a configurator
	 * @return
	 */
	public static DerivedUnitFacade NewInstance(SpecimenOrObservationType type, FieldUnit fieldUnit, DerivedUnitFacadeConfigurator config) {
		return new DerivedUnitFacade(type, fieldUnit, config);
	}


	/**
	 * Creates a derived unit facade for a given derived unit using the default
	 * configuration.
	 *
	 * @param derivedUnit
	 * @return
	 * @throws DerivedUnitFacadeNotSupportedException
	 */
	public static DerivedUnitFacade NewInstance(DerivedUnit derivedUnit)
			throws DerivedUnitFacadeNotSupportedException {
		return new DerivedUnitFacade(derivedUnit, null);
	}

	public static DerivedUnitFacade NewInstance(DerivedUnit derivedUnit,
			DerivedUnitFacadeConfigurator config)
			throws DerivedUnitFacadeNotSupportedException {
		return new DerivedUnitFacade(derivedUnit, config);
	}

	// ****************** CONSTRUCTOR ******************************************

	private DerivedUnitFacade(SpecimenOrObservationType type, FieldUnit fieldUnit, DerivedUnitFacadeConfigurator config) {
		if (config == null){
			config = DerivedUnitFacadeConfigurator.NewInstance();
		}
		this.config = config;
		// derivedUnit
		derivedUnit = getNewDerivedUnitInstance(type);
		//TODO parameter checking should be solved in a more generic way if we start using other entity facades
		if(derivedUnit==null && fieldUnit==null && type.isFieldUnit()){
		    this.fieldUnit = getFieldUnit(CREATE);
		}
		setFieldUnit(fieldUnit);
		if (derivedUnit != null){
			setCacheStrategy();
		}else{
			setFieldUnitCacheStrategy();
		}
	}

	private DerivedUnit getNewDerivedUnitInstance(SpecimenOrObservationType type) {
		if (type.isFieldUnit()){
			return null;
		}else if(type.isAnyDerivedUnit()){
			return DerivedUnit.NewInstance(type);
		} else {
			String message = "Unknown specimen or observation type %s";
			message = String.format(message, type.getMessage());
			throw new IllegalStateException(message);
		}
	}

	private DerivedUnitFacade(DerivedUnit derivedUnit, DerivedUnitFacadeConfigurator config)
			throws DerivedUnitFacadeNotSupportedException {

	    if(derivedUnit==null){
	        throw new IllegalArgumentException("DerivedUnit must not be null");
	    }

		if (config == null) {
			config = DerivedUnitFacadeConfigurator.NewInstance();
		}
		this.config = config;

		// derived unit
		this.derivedUnit = derivedUnit;

		// derivation event
		if (this.derivedUnit.getDerivedFrom() != null) {
			DerivationEvent derivationEvent = getDerivationEvent(CREATE);
			// fieldUnit
			Set<FieldUnit> fieldOriginals = getFieldUnitOriginals(derivationEvent, null);
			if (fieldOriginals.size() > 1) {
				throw new DerivedUnitFacadeNotSupportedException(
						"Specimen must not have more than 1 derivation event");
			} else if (fieldOriginals.size() == 0) {
				// fieldUnit = FieldUnit.NewInstance();
			} else if (fieldOriginals.size() == 1) {
				fieldUnit = fieldOriginals.iterator().next();
				// ###fieldUnit =
				// getInitializedFieldUnit(fieldUnit);
				if (config.isFirePropertyChangeEvents()){
					addNewEventPropagationListener(fieldUnit);
				}
			} else {
				throw new IllegalStateException("Illegal state");
			}
		}

		this.derivedUnitMediaTextData = inititializeTextDataWithSupportTest(Feature.IMAGE(), this.derivedUnit, false, true);

		fieldObjectMediaTextData = initializeFieldObjectTextDataWithSupportTest(Feature.IMAGE(), false, true);


//direct media have been removed from specimenorobservationbase #3597
//		// handle derivedUnit.getMedia()
//		if (derivedUnit.getMedia().size() > 0) {
//			// TODO better changed model here to allow only one place for images
//			if (this.config.isMoveDerivedUnitMediaToGallery()) {
//				Set<Media> mediaSet = derivedUnit.getMedia();
//				for (Media media : mediaSet) {
//					this.addDerivedUnitMedia(media);
//				}
//				mediaSet.removeAll(getDerivedUnitMedia());
//			} else {
//				throw new DerivedUnitFacadeNotSupportedException(
//						"Specimen may not have direct media. Only (one) image gallery is allowed");
//			}
//		}
//
//		// handle fieldUnit.getMedia()
//		if (fieldUnit != null && fieldUnit.getMedia() != null
//				&& fieldUnit.getMedia().size() > 0) {
//			// TODO better changed model here to allow only one place for images
//			if (this.config.isMoveFieldObjectMediaToGallery()) {
//				Set<Media> mediaSet = fieldUnit.getMedia();
//				for (Media media : mediaSet) {
//					this.addFieldObjectMedia(media);
//				}
//				mediaSet.removeAll(getFieldObjectMedia());
//			} else {
//				throw new DerivedUnitFacadeNotSupportedException(
//						"Field object may not have direct media. Only (one) image gallery is allowed");
//			}
//		}

		// test if descriptions are supported
		ecology = initializeFieldObjectTextDataWithSupportTest(
				Feature.ECOLOGY(), false, false);
		plantDescription = initializeFieldObjectTextDataWithSupportTest(
				Feature.DESCRIPTION(), false, false);

		setCacheStrategy();

	}

	private DerivedUnit getInitializedDerivedUnit(
			DerivedUnit derivedUnit) {
		IOccurrenceService occurrenceService = this.config
				.getOccurrenceService();
		if (occurrenceService == null) {
			return derivedUnit;
		}
		List<String> propertyPaths = this.config.getPropertyPaths();
		if (propertyPaths == null) {
			return derivedUnit;
		}
		propertyPaths = getDerivedUnitPropertyPaths(propertyPaths);
		DerivedUnit result = (DerivedUnit) occurrenceService.load(
				derivedUnit.getUuid(), propertyPaths);
		return result;
	}

	/**
	 * Initializes the derived unit according to the configuartions property
	 * path. If the property path is <code>null</code> or no occurrence service
	 * is given the returned object is the same as the input parameter.
	 *
	 * @param fieldUnit
	 * @return
	 */
	private FieldUnit getInitializedFieldUnit(FieldUnit fieldUnit) {
		IOccurrenceService occurrenceService = this.config
				.getOccurrenceService();
		if (occurrenceService == null) {
			return fieldUnit;
		}
		List<String> propertyPaths = this.config.getPropertyPaths();
		if (propertyPaths == null) {
			return fieldUnit;
		}
		propertyPaths = getFieldObjectPropertyPaths(propertyPaths);
		FieldUnit result = (FieldUnit) occurrenceService.load(
				fieldUnit.getUuid(), propertyPaths);
		return result;
	}

	/**
	 * Transforms the property paths in a way that the facade is handled just
	 * like an ordinary CdmBase object.<BR>
	 * E.g. a property path "collectinAreas" will be translated into
	 * gatheringEvent.collectingAreas
	 *
	 * @param propertyPaths
	 * @return
	 */
	private List<String> getFieldObjectPropertyPaths(List<String> propertyPaths) {
		List<String> result = new ArrayList<String>();
		for (String facadePath : propertyPaths) {
			// collecting areas (named area)
			if (facadePath.startsWith("collectingAreas")) {
				facadePath = "gatheringEvent." + facadePath;
				result.add(facadePath);
			}
			// collector (agentBase)
			else if (facadePath.startsWith("collector")) {
				facadePath = facadePath.replace("collector",
						"gatheringEvent.actor");
				result.add(facadePath);
			}
			// exactLocation (agentBase)
			else if (facadePath.startsWith("exactLocation")) {
				facadePath = "gatheringEvent." + facadePath;
				result.add(facadePath);
			}
			// gatheringPeriod (TimePeriod)
			else if (facadePath.startsWith("gatheringPeriod")) {
				facadePath = facadePath.replace("gatheringPeriod",
						"gatheringEvent.timeperiod");
				result.add(facadePath);
			}
			// (locality/ localityLanguage , LanguageString)
			else if (facadePath.startsWith("locality")) {
				facadePath = "gatheringEvent." + facadePath;
				result.add(facadePath);
			}

			// *********** FIELD OBJECT ************
			// fieldObjectDefinitions (Map<language, languageString)
			else if (facadePath.startsWith("fieldObjectDefinitions")) {
				// TODO or definition ???
				facadePath = facadePath.replace("fieldObjectDefinitions",
						"description");
				result.add(facadePath);
			}
			// fieldObjectMedia (Media)
			else if (facadePath.startsWith("fieldObjectMedia")) {
				// TODO ???
				facadePath = facadePath.replace("fieldObjectMedia",
						"descriptions.elements.media");
				result.add(facadePath);
			}

			// Gathering Event will always be added
			result.add("gatheringEvent");

		}

		/*
		 * Gathering Event ==================== - gatheringEvent
		 * (GatheringEvent)
		 *
		 * Field Object ================= - ecology/ ecologyAll (String) ??? -
		 * plant description (like ecology)
		 *
		 * - fieldObjectImageGallery (SpecimenDescription) - is automatically
		 * initialized via fieldObjectMedia
		 */

		return result;
	}

	/**
	 * Transforms the property paths in a way that the facade is handled just
	 * like an ordinary CdmBase object.<BR>
	 * E.g. a property path "collectinAreas" will be translated into
	 * gatheringEvent.collectingAreas
	 *
	 * Not needed (?) as the facade works with REST service property paths
	 * without using this method.
	 *
	 * @param propertyPaths
	 * @return
	 */
	private List<String> getDerivedUnitPropertyPaths(List<String> propertyPaths) {
		List<String> result = new ArrayList<String>();
		for (String facadePath : propertyPaths) {
			// determinations (DeterminationEvent)
			if (facadePath.startsWith("determinations")) {
				facadePath = "" + facadePath; // no change
				result.add(facadePath);
			}
			// storedUnder (TaxonName)
			else if (facadePath.startsWith("storedUnder")) {
				facadePath = "" + facadePath; // no change
				result.add(facadePath);
			}
			// sources (IdentifiableSource)
			else if (facadePath.startsWith("sources")) {
				facadePath = "" + facadePath; // no change
				result.add(facadePath);
			}
			// collection (Collection)
			else if (facadePath.startsWith("collection")) {
				facadePath = "" + facadePath; // no change
				result.add(facadePath);
			}
			// (locality/ localityLanguage , LanguageString)
			else if (facadePath.startsWith("locality")) {
				facadePath = "gatheringEvent." + facadePath;
				result.add(facadePath);
			}

			// *********** FIELD OBJECT ************
			// derivedUnitDefinitions (Map<language, languageString)
			else if (facadePath.startsWith("derivedUnitDefinitions")) {
				// TODO or definition ???
				facadePath = facadePath.replace("derivedUnitDefinitions",
						"description");
				result.add(facadePath);
			}

			// derivedUnitMedia (Media)
			else if (facadePath.startsWith("derivedUnitMedia")) {
				// TODO ???
				facadePath = facadePath.replace("derivedUnitMedia",
						"descriptions.elements.media");
				result.add(facadePath);
			}

		}

		/*
		 * //TODO Derived Unit =====================
		 *
		 * - derivedUnitImageGallery (SpecimenDescription) - is automatically
		 * initialized via derivedUnitMedia
		 *
		 * - derivationEvent (DerivationEvent) - will always be initialized -
		 * duplicates (??? Specimen???) ???
		 */

		return result;
	}

	/**
	 *
	 */
	private void setCacheStrategy() {
		if (derivedUnit == null) {
			throw new NullPointerException(
					"Facade's derviedUnit must not be null to set cache strategy");
		}else{
			derivedUnit.setCacheStrategy(new DerivedUnitFacadeCacheStrategy());
			setFieldUnitCacheStrategy();
		}
	}

	private void setFieldUnitCacheStrategy() {
		if (this.hasFieldObject()){
			DerivedUnitFacadeFieldUnitCacheStrategy strategy = new DerivedUnitFacadeFieldUnitCacheStrategy();
			this.fieldUnit.setCacheStrategy(strategy);
		}
	}

	/**
	 * @param feature
	 * @param createIfNotExists
	 * @param isImageGallery
	 * @return
	 * @throws DerivedUnitFacadeNotSupportedException
	 */
	private TextData initializeFieldObjectTextDataWithSupportTest(
			Feature feature, boolean createIfNotExists, boolean isImageGallery)
			throws DerivedUnitFacadeNotSupportedException {
		// field object
		FieldUnit fieldObject = getFieldUnit(createIfNotExists);
		if (fieldObject == null) {
			return null;
		}
		return inititializeTextDataWithSupportTest(feature, fieldObject,
				createIfNotExists, isImageGallery);
	}

	/**
	 * @param feature
	 * @param specimen
	 * @param createIfNotExists
	 * @param isImageGallery
	 * @return
	 * @throws DerivedUnitFacadeNotSupportedException
	 */
	private TextData inititializeTextDataWithSupportTest(Feature feature,
			SpecimenOrObservationBase<?> specimen, boolean createIfNotExists,
			boolean isImageGallery)
			throws DerivedUnitFacadeNotSupportedException {
		if (feature == null) {
			return null;
		}
		TextData textData = null;
		if (createIfNotExists) {
			textData = TextData.NewInstance(feature);
		}

		Set<SpecimenDescription> descriptions;
		if (isImageGallery) {
			descriptions = specimen.getSpecimenDescriptionImageGallery();
		} else {
			descriptions = specimen.getSpecimenDescriptions(false);
		}
		// no description exists yet for this specimen
		if (descriptions.size() == 0) {
			if (createIfNotExists) {
				SpecimenDescription newSpecimenDescription = SpecimenDescription
						.NewInstance(specimen);
				newSpecimenDescription.addElement(textData);
				newSpecimenDescription.setImageGallery(isImageGallery);
				return textData;
			} else {
				return null;
			}
		}
		// description already exists
		Set<DescriptionElementBase> existingTextData = new HashSet<>();
		for (SpecimenDescription description : descriptions) {
			// collect all existing text data
			for (DescriptionElementBase element : description.getElements()) {
				if (element.isInstanceOf(TextData.class)
						&& (feature.equals(element.getFeature()) || isImageGallery)) {
					existingTextData.add(element);
				}
			}
		}
		// use existing text data if exactly one exists
		if (existingTextData.size() > 1) {
			throw new DerivedUnitFacadeNotSupportedException(
					"Specimen facade does not support more than one description text data of type "
							+ feature.getLabel());

		} else if (existingTextData.size() == 1) {
			return CdmBase.deproxy(existingTextData.iterator().next(),
					TextData.class);
		} else {
			if (createIfNotExists) {
				SpecimenDescription description = descriptions.iterator()
						.next();
				description.addElement(textData);
			}
			return textData;
		}
	}

	/**
	 * Tests if a given image gallery is supported by the derived unit facade.
	 * It returns the only text data attached to the given image gallery. If the
	 * given image gallery does not have text data attached, it is created and
	 * attached.
	 *
	 * @param imageGallery
	 * @return
	 * @throws DerivedUnitFacadeNotSupportedException
	 */
	private TextData testImageGallery(SpecimenDescription imageGallery)
			throws DerivedUnitFacadeNotSupportedException {
		if (imageGallery.isImageGallery() == false) {
			throw new DerivedUnitFacadeNotSupportedException(
					"Image gallery needs to have image gallery flag set");
		}
		if (imageGallery.getElements().size() > 1) {
			throw new DerivedUnitFacadeNotSupportedException(
					"Image gallery must not have more then one description element");
		}
		TextData textData;
		if (imageGallery.getElements().size() == 0) {
			textData = TextData.NewInstance(Feature.IMAGE());
			imageGallery.addElement(textData);
		} else {
			if (!imageGallery.getElements().iterator().next()
					.isInstanceOf(TextData.class)) {
				throw new DerivedUnitFacadeNotSupportedException(
						"Image gallery must only have TextData as element");
			} else {
				textData = CdmBase.deproxy(imageGallery.getElements()
						.iterator().next(), TextData.class);
			}
		}
		return textData;
	}

	// ************************** METHODS
	// *****************************************

	private TextData getDerivedUnitImageGalleryTextData(
			boolean createIfNotExists)
			throws DerivedUnitFacadeNotSupportedException {
		if (this.derivedUnitMediaTextData == null && createIfNotExists) {
			this.derivedUnitMediaTextData = getImageGalleryTextData(
					derivedUnit, "Specimen");
		}
		return this.derivedUnitMediaTextData;
	}

	private TextData getObservationImageGalleryTextData(
			boolean createIfNotExists)
			throws DerivedUnitFacadeNotSupportedException {
		if (this.fieldObjectMediaTextData == null && createIfNotExists) {
			this.fieldObjectMediaTextData = getImageGalleryTextData(fieldUnit, "Field unit");
		}
		return this.fieldObjectMediaTextData;
	}

	/**
	 * @param derivationEvent
	 * @return
	 * @throws DerivedUnitFacadeNotSupportedException
	 */
	private Set<FieldUnit> getFieldUnitOriginals(
			DerivationEvent derivationEvent,
			Set<SpecimenOrObservationBase> recursionAvoidSet)
			throws DerivedUnitFacadeNotSupportedException {
		if (recursionAvoidSet == null) {
			recursionAvoidSet = new HashSet<>();
		}
		Set<FieldUnit> result = new HashSet<>();
		Set<SpecimenOrObservationBase> originals = derivationEvent.getOriginals();
		for (SpecimenOrObservationBase original : originals) {
			if (original.isInstanceOf(FieldUnit.class)) {
				result.add(CdmBase.deproxy(original, FieldUnit.class));
			} else if (original.isInstanceOf(DerivedUnit.class)) {
				// if specimen has already been tested exclude it from further
				// recursion
				if (recursionAvoidSet.contains(original)) {
					continue;
				}
				DerivedUnit derivedUnit = CdmBase.deproxy(original,	DerivedUnit.class);
				DerivationEvent originalDerivation = derivedUnit.getDerivedFrom();
				// Set<DerivationEvent> derivationEvents =
				// original.getDerivationEvents();
				// for (DerivationEvent originalDerivation : derivationEvents){
				if(originalDerivation!=null){
				    Set<FieldUnit> fieldUnits = getFieldUnitOriginals(
				            originalDerivation, recursionAvoidSet);
				    result.addAll(fieldUnits);
				}
				// }
			} else {
				throw new DerivedUnitFacadeNotSupportedException(
						"Unhandled specimen or observation base type: "
								+ original.getClass().getName());
			}

		}
		return result;
	}

	// *********** MEDIA METHODS ******************************

	// /**
	// * Returns the media list for a specimen. Throws an exception if the
	// existing specimen descriptions
	// * are not supported by this facade.
	// * @param specimen the specimen the media belongs to
	// * @param specimenExceptionText text describing the specimen for exception
	// messages
	// * @return
	// * @throws DerivedUnitFacadeNotSupportedException
	// */
	// private List<Media> getImageGalleryMedia(SpecimenOrObservationBase
	// specimen, String specimenExceptionText) throws
	// DerivedUnitFacadeNotSupportedException{
	// List<Media> result;
	// SpecimenDescription imageGallery =
	// getImageGalleryWithSupportTest(specimen, specimenExceptionText, true);
	// TextData textData = getImageTextDataWithSupportTest(imageGallery,
	// specimenExceptionText);
	// result = textData.getMedia();
	// return result;
	// }

	/**
	 * Returns the media list for a specimen. Throws an exception if the
	 * existing specimen descriptions are not supported by this facade.
	 *
	 * @param specimen
	 *            the specimen the media belongs to
	 * @param specimenExceptionText
	 *            text describing the specimen for exception messages
	 * @return
	 * @throws DerivedUnitFacadeNotSupportedException
	 */
	private TextData getImageGalleryTextData(SpecimenOrObservationBase specimen, String specimenExceptionText)
			throws DerivedUnitFacadeNotSupportedException {
		TextData result;
		SpecimenDescription imageGallery = getImageGalleryWithSupportTest(
				specimen, specimenExceptionText, true);
		result = getImageTextDataWithSupportTest(imageGallery,
				specimenExceptionText);
		return result;
	}

	/**
	 * Returns the image gallery of the according specimen. Throws an exception
	 * if the attached image gallerie(s) are not supported by this facade. If no
	 * image gallery exists a new one is created if
	 * <code>createNewIfNotExists</code> is true and if specimen is not
	 * <code>null</code>.
	 *
	 * @param specimen
	 * @param specimenText
	 * @param createNewIfNotExists
	 * @return
	 * @throws DerivedUnitFacadeNotSupportedException
	 */
	private SpecimenDescription getImageGalleryWithSupportTest(
			SpecimenOrObservationBase<?> specimen, String specimenText,
			boolean createNewIfNotExists)
			throws DerivedUnitFacadeNotSupportedException {
		if (specimen == null) {
			return null;
		}
		SpecimenDescription imageGallery;
		if (hasMultipleImageGalleries(specimen)) {
			throw new DerivedUnitFacadeNotSupportedException(specimenText
					+ " must not have more than 1 image gallery");
		} else {
			imageGallery = getImageGallery(specimen, createNewIfNotExists);
			getImageTextDataWithSupportTest(imageGallery, specimenText);
		}
		return imageGallery;
	}

	/**
	 * Returns the media holding text data element of the image gallery. Throws
	 * an exception if multiple such text data already exist. Creates a new text
	 * data if none exists and adds it to the image gallery. If image gallery is
	 * <code>null</code> nothing happens.
	 *
	 * @param imageGallery
	 * @param textData
	 * @return
	 * @throws DerivedUnitFacadeNotSupportedException
	 */
	private TextData getImageTextDataWithSupportTest(
			SpecimenDescription imageGallery, String specimenText)
			throws DerivedUnitFacadeNotSupportedException {
		if (imageGallery == null) {
			return null;
		}
		TextData textData = null;
		for (DescriptionElementBase element : imageGallery.getElements()) {
			if (element.isInstanceOf(TextData.class)
					&& element.getFeature().equals(Feature.IMAGE())) {
				if (textData != null) {
					throw new DerivedUnitFacadeNotSupportedException(
							specimenText
									+ " must not have more than 1 image text data element in image gallery");
				}
				textData = CdmBase.deproxy(element, TextData.class);
			}
		}
		if (textData == null) {
			textData = TextData.NewInstance(Feature.IMAGE());
			imageGallery.addElement(textData);
		}
		return textData;
	}

	/**
	 * Checks, if a specimen belongs to more than one description that is an
	 * image gallery
	 *
	 * @param derivedUnit
	 * @return
	 */
	private boolean hasMultipleImageGalleries(
			SpecimenOrObservationBase<?> derivedUnit) {
		int count = 0;
		Set<SpecimenDescription> descriptions = derivedUnit
				.getSpecimenDescriptions();
		for (SpecimenDescription description : descriptions) {
			if (description.isImageGallery()) {
				count++;
			}
		}
		return (count > 1);
	}

	/**
	 * Returns the image gallery for a specimen. If there are multiple specimen
	 * descriptions marked as image galleries an arbitrary one is chosen. If no
	 * image gallery exists, a new one is created if
	 * <code>createNewIfNotExists</code> is <code>true</code>.<Br>
	 * If specimen is <code>null</code> a null pointer exception is thrown.
	 *
	 * @param createNewIfNotExists
	 * @return
	 */
	private SpecimenDescription getImageGallery(SpecimenOrObservationBase<?> specimen, boolean createIfNotExists) {
		SpecimenDescription result = null;
		Set<SpecimenDescription> descriptions = specimen.getSpecimenDescriptions();
		for (SpecimenDescription description : descriptions) {
			if (description.isImageGallery()) {
				result = description;
				break;
			}
		}
		if (result == null && createIfNotExists) {
			result = SpecimenDescription.NewInstance(specimen);
			result.setImageGallery(true);
		}
		return result;
	}

	/**
	 * Adds a media to the specimens image gallery. If media is
	 * <code>null</code> nothing happens.
	 *
	 * @param media
	 * @param specimen
	 * @return true if media is not null (as specified by
	 *         {@link java.util.Collection#add(Object) Collection.add(E e)}
	 * @throws DerivedUnitFacadeNotSupportedException
	 */
	private boolean addMedia(Media media, SpecimenOrObservationBase<?> specimen) throws DerivedUnitFacadeNotSupportedException {
		if (media != null) {
			List<Media> mediaList = getMediaList(specimen, true);
			if (! mediaList.contains(media)){
				return mediaList.add(media);
			}else{
				return true;
			}
		} else {
			return false;
		}
	}

	/**
	 * Removes a media from the specimens image gallery.
	 *
	 * @param media
	 * @param specimen
	 * @return true if an element was removed as a result of this call (as
	 *         specified by {@link java.util.Collection#remove(Object)
	 *         Collection.remove(E e)}
	 * @throws DerivedUnitFacadeNotSupportedException
	 */
	private boolean removeMedia(Media media,
			SpecimenOrObservationBase<?> specimen)
			throws DerivedUnitFacadeNotSupportedException {
		List<Media> mediaList = getMediaList(specimen, true);
		return mediaList == null ? null : mediaList.remove(media);
	}

	private List<Media> getMediaList(SpecimenOrObservationBase<?> specimen, boolean createIfNotExists)
			throws DerivedUnitFacadeNotSupportedException {
		TextData textData = getMediaTextData(specimen, createIfNotExists);
		return textData == null ? null : textData.getMedia();
	}

	/**
	 * Returns the one media list of a specimen which is part of the only image
	 * gallery that this specimen is part of.<BR>
	 * If these conditions are not hold an exception is thrwon.
	 *
	 * @param specimen
	 * @return
	 * @throws DerivedUnitFacadeNotSupportedException
	 */
	// private List<Media> getMedia(SpecimenOrObservationBase<?> specimen)
	// throws DerivedUnitFacadeNotSupportedException {
	// if (specimen == null){
	// return null;
	// }
	// if (specimen == this.derivedUnit){
	// return getDerivedUnitImageGalleryMedia();
	// }else if (specimen == this.fieldUnit){
	// return getObservationImageGalleryTextData();
	// }else{
	// return getImageGalleryMedia(specimen, "Undefined specimen ");
	// }
	// }

	/**
	 * Returns the one media list of a specimen which is part of the only image
	 * gallery that this specimen is part of.<BR>
	 * If these conditions are not hold an exception is thrown.
	 *
	 * @param specimen
	 * @return
	 * @throws DerivedUnitFacadeNotSupportedException
	 */
	private TextData getMediaTextData(SpecimenOrObservationBase<?> specimen,
			boolean createIfNotExists)
			throws DerivedUnitFacadeNotSupportedException {
		if (specimen == null) {
			return null;
		}
		if (specimen == this.derivedUnit) {
			return getDerivedUnitImageGalleryTextData(createIfNotExists);
		} else if (specimen == this.fieldUnit) {
			return getObservationImageGalleryTextData(createIfNotExists);
		} else {
			return getImageGalleryTextData(specimen, "Undefined specimen ");
		}
	}

	// ****************** GETTER / SETTER / ADDER / REMOVER
	// ***********************/

	// ****************** Gathering Event *********************************/

	// country
	@Transient
	public NamedArea getCountry() {
		return (hasGatheringEvent() ? getGatheringEvent(true).getCountry()
				: null);
	}

	public void setCountry(NamedArea country) {
		getGatheringEvent(true).setCountry(country);
	}

	// Collecting area
	public void addCollectingArea(NamedArea area) {
		getGatheringEvent(true).addCollectingArea(area);
	}

	public void addCollectingAreas(java.util.Collection<NamedArea> areas) {
		for (NamedArea area : areas) {
			getGatheringEvent(true).addCollectingArea(area);
		}
	}

	@Transient
	public Set<NamedArea> getCollectingAreas() {
		return (hasGatheringEvent() ? getGatheringEvent(true)
				.getCollectingAreas() : null);
	}

	public void removeCollectingArea(NamedArea area) {
		if (hasGatheringEvent()) {
			getGatheringEvent(true).removeCollectingArea(area);
		}
	}

	static final String ALTITUDE_POSTFIX = " m";

	/**
	 * Returns the correctly formatted <code>absolute elevation</code> information.
	 * If absoluteElevationText is set, this will be returned,
	 * otherwise we absoluteElevation will be returned, followed by absoluteElevationMax
	 * if existing, separated by " - "
	 * @return
	 */
	@Transient
	public String absoluteElevationToString() {
		if (! hasGatheringEvent()){
			return null;
		}else{
			GatheringEvent ev = getGatheringEvent(true);
			if (StringUtils.isNotBlank(ev.getAbsoluteElevationText())){
				return ev.getAbsoluteElevationText();
			}else{
				String text = ev.getAbsoluteElevationText();
				Integer min = getAbsoluteElevation();
				Integer max = getAbsoluteElevationMaximum();
				return distanceString(min, max, text, METER);
			}
		}
	}


	/**
	 * meter above/below sea level of the surface
	 *
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

	public void setAbsoluteElevationMax(Integer absoluteElevationMax) {
		getGatheringEvent(true).setAbsoluteElevationMax(absoluteElevationMax);
	}

	public void setAbsoluteElevationText(String absoluteElevationText) {
		getGatheringEvent(true).setAbsoluteElevationText(absoluteElevationText);
	}

	/**
	 * @see #getAbsoluteElevation()
	 * @see #getAbsoluteElevationError()
	 * @see #setAbsoluteElevationRange(Integer, Integer)
	 * @see #getAbsoluteElevationMinimum()
	 */
	@Transient
	public Integer getAbsoluteElevationMaximum() {
		if (!hasGatheringEvent()) {
			return null;
		}else{
			return getGatheringEvent(true).getAbsoluteElevationMax();
		}
	}

	/**
	 * @see #getAbsoluteElevation()
	 * @see #getAbsoluteElevationError()
	 * @see #setAbsoluteElevationRange(Integer, Integer)
	 * @see #getAbsoluteElevationMinimum()
	 */
	@Transient
	public String getAbsoluteElevationText() {
		if (!hasGatheringEvent()) {
			return null;
		}else{
			return getGatheringEvent(true).getAbsoluteElevationText();
		}
	}

	/**
	 * Convenience method to set absolute elevation minimum and maximum.
	 *
	 * @see #setAbsoluteElevation(Integer)
	 * @see #setAbsoluteElevationMax(Integer)
	 * @param minimumElevation minimum of the range
	 * @param maximumElevation maximum of the range
	 */
	public void setAbsoluteElevationRange(Integer minimumElevation, Integer maximumElevation) {
		getGatheringEvent(true).setAbsoluteElevation(minimumElevation);
		getGatheringEvent(true).setAbsoluteElevationMax(maximumElevation);
	}

	// collector
	@Transient
	public AgentBase getCollector() {
		return (hasGatheringEvent() ? getGatheringEvent(true).getCollector()
				: null);
	}

	public void setCollector(AgentBase collector) {
		getGatheringEvent(true).setCollector(collector);
	}

	// collecting method
	@Transient
	public String getCollectingMethod() {
		return (hasGatheringEvent() ? getGatheringEvent(true).getCollectingMethod() : null);
	}

	public void setCollectingMethod(String collectingMethod) {
		getGatheringEvent(true).setCollectingMethod(collectingMethod);
	}

	// distance to ground

	/**
	 * Returns the correctly formatted <code>distance to ground</code> information.
	 * If distanceToGroundText is not blank, it will be returned,
	 * otherwise distanceToGround will be returned, followed by distanceToGroundMax
	 * if existing, separated by " - "
	 * @return
	 */
	@Transient
	public String distanceToGroundToString() {
		if (! hasGatheringEvent()){
			return null;
		}else{
			GatheringEvent ev = getGatheringEvent(true);
			String text = ev.getDistanceToGroundText();
			Double min = getDistanceToGround();
			Double max = getDistanceToGroundMax();
			return distanceString(min, max, text, METER);
		}
	}

	@Transient
	public Double getDistanceToGround() {
		return (hasGatheringEvent() ? getGatheringEvent(true).getDistanceToGround() : null);
	}

	public void setDistanceToGround(Double distanceToGround) {
		getGatheringEvent(true).setDistanceToGround(distanceToGround);
	}

	/**
	 * @see #getDistanceToGround()
	 * @see #getDistanceToGroundRange(Integer, Integer)
	 */
	@Transient
	public Double getDistanceToGroundMax() {
		if (!hasGatheringEvent()) {
			return null;
		}else{
			return getGatheringEvent(true).getDistanceToGroundMax();
		}
	}

	public void setDistanceToGroundMax(Double distanceToGroundMax) {
		getGatheringEvent(true).setDistanceToGroundMax(distanceToGroundMax);
	}

	/**
	 * @see #getDistanceToGround()
	 * @see #setDistanceToGroundRange(Integer, Integer)
	 */
	@Transient
	public String getDistanceToGroundText() {
		if (!hasGatheringEvent()) {
			return null;
		}else{
			return getGatheringEvent(true).getDistanceToGroundText();
		}
	}
	public void setDistanceToGroundText(String distanceToGroundText) {
		getGatheringEvent(true).setDistanceToGroundText(distanceToGroundText);
	}

	/**
	 * Convenience method to set distance to ground minimum and maximum.
	 *
	 * @see #getDistanceToGround()
	 * @see #getDistanceToGroundMax()
	 * @param minimumDistance minimum of the range
	 * @param maximumDistance maximum of the range
	 */
	public void setDistanceToGroundRange(Double minimumDistance, Double maximumDistance) throws IllegalArgumentException{
		getGatheringEvent(true).setDistanceToGround(minimumDistance);
		getGatheringEvent(true).setDistanceToGroundMax(maximumDistance);
	}


	/**
	 * Returns the correctly formatted <code>distance to water surface</code> information.
	 * If distanceToWaterSurfaceText is not blank, it will be returned,
	 * otherwise distanceToWaterSurface will be returned, followed by distanceToWatersurfaceMax
	 * if existing, separated by " - "
	 * @return
	 */
	@Transient
	public String distanceToWaterSurfaceToString() {
		if (! hasGatheringEvent()){
			return null;
		}else{
			GatheringEvent ev = getGatheringEvent(true);
			String text = ev.getDistanceToWaterSurfaceText();
			Double min = getDistanceToWaterSurface();
			Double max = getDistanceToWaterSurfaceMax();
			return distanceString(min, max, text, METER);
		}
	}

	// distance to water surface
	@Transient
	public Double getDistanceToWaterSurface() {
		return (hasGatheringEvent() ? getGatheringEvent(true).getDistanceToWaterSurface() : null);
	}

	public void setDistanceToWaterSurface(Double distanceToWaterSurface) {
		getGatheringEvent(true).setDistanceToWaterSurface(distanceToWaterSurface);
	}

	/**
	 * @see #getDistanceToWaterSurface()
	 * @see #getDistanceToWaterSurfaceRange(Double, Double)
	 */
	@Transient
	public Double getDistanceToWaterSurfaceMax() {
		if (!hasGatheringEvent()) {
			return null;
		}else{
			return getGatheringEvent(true).getDistanceToWaterSurfaceMax();
		}
	}

	public void setDistanceToWaterSurfaceMax(Double distanceToWaterSurfaceMax) {
		getGatheringEvent(true).setDistanceToWaterSurfaceMax(distanceToWaterSurfaceMax);
	}

	/**
	 * @see #getDistanceToWaterSurface()
	 * @see #getDistanceToWaterSurfaceRange(Double, Double)
	 */
	@Transient
	public String getDistanceToWaterSurfaceText() {
		if (!hasGatheringEvent()) {
			return null;
		}else{
			return getGatheringEvent(true).getDistanceToWaterSurfaceText();
		}
	}
	public void setDistanceToWaterSurfaceText(String distanceToWaterSurfaceText) {
		getGatheringEvent(true).setDistanceToWaterSurfaceText(distanceToWaterSurfaceText);
	}

	/**
	 * Convenience method to set distance to ground minimum and maximum.
	 *
	 * @see #getDistanceToWaterSurface()
	 * @see #getDistanceToWaterSurfaceMax()
	 * @param minimumDistance minimum of the range, this is the distance which is closer to the water surface
	 * @param maximumDistance maximum of the range, this is the distance which is farer to the water surface
	 */
	public void setDistanceToWaterSurfaceRange(Double minimumDistance, Double maximumDistance) throws IllegalArgumentException{
		getGatheringEvent(true).setDistanceToWaterSurface(minimumDistance);
		getGatheringEvent(true).setDistanceToWaterSurfaceMax(maximumDistance);
	}


	// exact location
	@Transient
	public Point getExactLocation() {
		return (hasGatheringEvent() ? getGatheringEvent(true).getExactLocation() : null);
	}

	/**
	 * Returns a sexagesimal representation of the exact location (e.g.
	 * 12°59'N, 35°23E). If the exact location is <code>null</code> the empty
	 * string is returned.
	 *
	 * @param includeEmptySeconds
	 * @param includeReferenceSystem
	 * @return
	 */
	public String getExactLocationText(boolean includeEmptySeconds,
			boolean includeReferenceSystem) {
		return (this.getExactLocation() == null ? "" : this.getExactLocation()
				.toSexagesimalString(includeEmptySeconds,
						includeReferenceSystem));
	}

	public void setExactLocation(Point exactLocation) {
		getGatheringEvent(true).setExactLocation(exactLocation);
	}

	public void setExactLocationByParsing(String longitudeToParse,
			String latitudeToParse, ReferenceSystem referenceSystem,
			Integer errorRadius) throws ParseException {
		Point point = Point.NewInstance(null, null, referenceSystem,
				errorRadius);
		point.setLongitudeByParsing(longitudeToParse);
		point.setLatitudeByParsing(latitudeToParse);
		setExactLocation(point);
	}

	// gathering event description
	@Transient
	public String getGatheringEventDescription() {
		return (hasGatheringEvent() ? getGatheringEvent(true).getDescription()
				: null);
	}

	public void setGatheringEventDescription(String description) {
		getGatheringEvent(true).setDescription(description);
	}

	// gatering period
	@Transient
	public TimePeriod getGatheringPeriod() {
		return (hasGatheringEvent() ? getGatheringEvent(true).getTimeperiod()
				: null);
	}

	public void setGatheringPeriod(TimePeriod timeperiod) {
		getGatheringEvent(true).setTimeperiod(timeperiod);
	}

	// locality
	@Transient
	public LanguageString getLocality() {
		return (hasGatheringEvent() ? getGatheringEvent(true).getLocality()
				: null);
	}

	/**
	 * convienience method for {@link #getLocality()}.
	 * {@link LanguageString#getText() getText()}
	 *
	 * @return
	 */
	@Transient
	public String getLocalityText() {
		LanguageString locality = getLocality();
		if (locality != null) {
			return locality.getText();
		}
		return null;
	}

	/**
	 * convienience method for {@link #getLocality()}.
	 * {@link LanguageString#getLanguage() getLanguage()}
	 *
	 * @return
	 */
	@Transient
	public Language getLocalityLanguage() {
		LanguageString locality = getLocality();
		if (locality != null) {
			return locality.getLanguage();
		}
		return null;
	}

	/**
	 * Sets the locality string in the default language
	 *
	 * @param locality
	 */
	public void setLocality(String locality) {
		Language language = Language.DEFAULT();
		setLocality(locality, language);
	}

	public void setLocality(String locality, Language language) {
		LanguageString langString = LanguageString.NewInstance(locality, language);
		setLocality(langString);
	}

	public void setLocality(LanguageString locality) {
		getGatheringEvent(true).setLocality(locality);
	}

	/**
	 * The gathering event will be used for the field object instead of the old
	 * gathering event.<BR>
	 * <B>This method will override all gathering values (see below).</B>
	 *
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
		getFieldUnit(true).setGatheringEvent(gatheringEvent);
	}

	public boolean hasGatheringEvent() {
		return (getGatheringEvent(false) != null);
	}

	public GatheringEvent innerGatheringEvent() {
		return getGatheringEvent(false);
	}

	public GatheringEvent getGatheringEvent(boolean createIfNotExists) {
		if (!hasFieldUnit() && !createIfNotExists) {
			return null;
		}
		if (createIfNotExists && getFieldUnit(true).getGatheringEvent() == null) {
			GatheringEvent gatheringEvent = GatheringEvent.NewInstance();
			getFieldUnit(true).setGatheringEvent(gatheringEvent);
		}
		return getFieldUnit(true).getGatheringEvent();
	}

	// ****************** Field Object ************************************/

	/**
	 * Returns true if a field unit exists (even if all attributes are
	 * empty or <code>null<code>.
	 *
	 * @return
	 */
	public boolean hasFieldObject() {
		return this.fieldUnit != null;
	}

	// ecology
	@Transient
	public String getEcology() {
		return getEcology(Language.DEFAULT());
	}

	public String getEcology(Language language) {
		LanguageString languageString = getEcologyAll().get(language);
		return (languageString == null ? null : languageString.getText());
	}

	// public String getEcologyPreferred(List<Language> languages){
	// LanguageString languageString =
	// getEcologyAll().getPreferredLanguageString(languages);
	// return languageString.getText();
	// }
	/**
	 * Returns a copy of the multilanguage text holding the ecology data.
	 *
	 * @see {@link TextData#getMultilanguageText()}
	 * @return
	 */
	@Transient
	public Map<Language, LanguageString> getEcologyAll() {
		if (ecology == null) {
			try {
				ecology = initializeFieldObjectTextDataWithSupportTest(
						Feature.ECOLOGY(), false, false);
			} catch (DerivedUnitFacadeNotSupportedException e) {
				throw new IllegalStateException(notSupportMessage, e);
			}
			if (ecology == null) {
				return new HashMap<>();
			}
		}
		return ecology.getMultilanguageText();
	}

	public void setEcology(String ecology) {
		setEcology(ecology, null);
	}

	public void setEcology(String ecologyText, Language language) {
		if (language == null) {
			language = Language.DEFAULT();
		}
		boolean isEmpty = StringUtils.isBlank(ecologyText);
		if (ecology == null) {
			try {
				ecology = initializeFieldObjectTextDataWithSupportTest(
						Feature.ECOLOGY(), !isEmpty, false);
			} catch (DerivedUnitFacadeNotSupportedException e) {
				throw new IllegalStateException(notSupportMessage, e);
			}
		}
		if (ecology != null){
			if (ecologyText == null) {
				ecology.removeText(language);
			} else {
				ecology.putText(language, ecologyText);
			}
		}
	}

	public void removeEcology(Language language) {
		setEcology(null, language);
	}

	/**
	 * Removes ecology for the default language
	 */
	public void removeEcology() {
		setEcology(null, null);
	}

	// plant description
	@Transient
	public String getPlantDescription() {
		return getPlantDescription(null);
	}

	public String getPlantDescription(Language language) {
		if (language == null) {
			language = Language.DEFAULT();
		}
		LanguageString languageString = getPlantDescriptionAll().get(language);
		return (languageString == null ? null : languageString.getText());
	}

	// public String getPlantDescriptionPreferred(List<Language> languages){
	// LanguageString languageString =
	// getPlantDescriptionAll().getPreferredLanguageString(languages);
	// return languageString.getText();
	// }
	/**
	 * Returns a copy of the multilanguage text holding the description data.
	 *
	 * @see {@link TextData#getMultilanguageText()}
	 * @return
	 */
	@Transient
	public Map<Language, LanguageString> getPlantDescriptionAll() {
		if (plantDescription == null) {
			try {
				plantDescription = initializeFieldObjectTextDataWithSupportTest(
						Feature.DESCRIPTION(), false, false);
			} catch (DerivedUnitFacadeNotSupportedException e) {
				throw new IllegalStateException(notSupportMessage, e);
			}
			if (plantDescription == null) {
				return new HashMap<>();
			}
		}
		return plantDescription.getMultilanguageText();
	}

	public void setPlantDescription(String plantDescription) {
		setPlantDescription(plantDescription, null);
	}

	public void setPlantDescription(String plantDescriptionText, Language language) {
		if (language == null) {
			language = Language.DEFAULT();
		}
		boolean isEmpty = StringUtils.isBlank(plantDescriptionText);
		if (plantDescription == null) {
			try {
				plantDescription = initializeFieldObjectTextDataWithSupportTest(
						Feature.DESCRIPTION(), !isEmpty, false);
			} catch (DerivedUnitFacadeNotSupportedException e) {
				throw new IllegalStateException(notSupportMessage, e);
			}
		}
		if (plantDescription != null){
			if (plantDescriptionText == null) {
				plantDescription.removeText(language);
			} else {
				plantDescription.putText(language, plantDescriptionText);
			}
		}
	}

	public void removePlantDescription(Language language) {
		setPlantDescription(null, language);
	}

	// life-form
    @Transient
    public String getLifeform() {
        return getLifeform(Language.DEFAULT());
    }

    public String getLifeform(Language language) {
        LanguageString languageString = getLifeformAll().get(language);
        return (languageString == null ? null : languageString.getText());
    }

    // public String getLifeformPreferred(List<Language> languages){
    // LanguageString languageString =
    // getLifeformAll().getPreferredLanguageString(languages);
    // return languageString.getText();
    // }
    /**
     * Returns a copy of the multi language text holding the life-form data.
     *
     * @see {@link TextData#getMultilanguageText()}
     * @return
     */
    @Transient
    public Map<Language, LanguageString> getLifeformAll() {
        if (lifeform == null) {
            try {
                lifeform = initializeFieldObjectTextDataWithSupportTest(
                        Feature.LIFEFORM(), false, false);
            } catch (DerivedUnitFacadeNotSupportedException e) {
                throw new IllegalStateException(notSupportMessage, e);
            }
            if (lifeform == null) {
                return new HashMap<>();
            }
        }
        return lifeform.getMultilanguageText();
    }

    public void setLifeform(String lifeform) {
        setLifeform(lifeform, null);
    }

    public void setLifeform(String lifeformText, Language language) {
        if (language == null) {
            language = Language.DEFAULT();
        }
        boolean isEmpty = StringUtils.isBlank(lifeformText);
        if (lifeform == null) {
            try {
                lifeform = initializeFieldObjectTextDataWithSupportTest(
                        Feature.LIFEFORM(), !isEmpty, false);
            } catch (DerivedUnitFacadeNotSupportedException e) {
                throw new IllegalStateException(notSupportMessage, e);
            }
        }
        if (lifeform != null){
            if (lifeformText == null) {
                lifeform.removeText(language);
            } else {
                lifeform.putText(language, lifeformText);
            }
        }
    }

    public void removeLifeform(Language language) {
        setLifeform(null, language);
    }

    /**
     * Removes life-form for the default language
     */
    public void removeLifeform() {
        setLifeform(null, null);
    }

	// field object definition
	public void addFieldObjectDefinition(String text, Language language) {
		getFieldUnit(true).putDefinition(language, text);
	}

	@Transient
	public Map<Language, LanguageString> getFieldObjectDefinition() {
		if (!hasFieldUnit()) {
			return new HashMap<>();
		} else {
			return getFieldUnit(true).getDefinition();
		}
	}

	public String getFieldObjectDefinition(Language language) {
		Map<Language, LanguageString> map = getFieldObjectDefinition();
		LanguageString languageString = (map == null ? null : map.get(language));
		if (languageString != null) {
			return languageString.getText();
		} else {
			return null;
		}
	}

	public void removeFieldObjectDefinition(Language lang) {
		if (hasFieldUnit()) {
			getFieldUnit(true).removeDefinition(lang);
		}
	}

	// media
	public boolean addFieldObjectMedia(Media media) {
		try {
			return addMedia(media, getFieldUnit(true));
		} catch (DerivedUnitFacadeNotSupportedException e) {
			throw new IllegalStateException(notSupportMessage, e);
		}
	}

	/**
	 * Returns true, if an image gallery for the field object exists.<BR>
	 * Returns also <code>true</code> if the image gallery is empty.
	 *
	 * @return
	 */
	public boolean hasFieldObjectImageGallery() {
		if (!hasFieldObject()) {
			return false;
		} else {
			return (getImageGallery(fieldUnit, false) != null);
		}
	}

	public void setFieldObjectImageGallery(SpecimenDescription imageGallery)
			throws DerivedUnitFacadeNotSupportedException {
		SpecimenDescription existingGallery = getFieldObjectImageGallery(false);

		// test attached specimens contain this.derivedUnit
		SpecimenOrObservationBase<?> facadeFieldUnit = innerFieldUnit();
		testSpecimenInImageGallery(imageGallery, facadeFieldUnit);

		if (existingGallery != null) {
			if (existingGallery != imageGallery) {
				throw new DerivedUnitFacadeNotSupportedException(
						"DerivedUnitFacade does not allow more than one image gallery");
			} else {
				// do nothing
			}
		} else {
			TextData textData = testImageGallery(imageGallery);
			this.fieldObjectMediaTextData = textData;
		}
	}

	/**
	 * Returns the field object image gallery. If no such image gallery exists
	 * and createIfNotExists is true an new one is created. Otherwise null is
	 * returned.
	 *
	 * @param createIfNotExists
	 * @return
	 */
	public SpecimenDescription getFieldObjectImageGallery(
			boolean createIfNotExists) {
		TextData textData;
		try {
			textData = initializeFieldObjectTextDataWithSupportTest(
					Feature.IMAGE(), createIfNotExists, true);
		} catch (DerivedUnitFacadeNotSupportedException e) {
			throw new IllegalStateException(notSupportMessage, e);
		}
		if (textData != null) {
			return CdmBase.deproxy(textData.getInDescription(),
					SpecimenDescription.class);
		} else {
			return null;
		}
	}

	/**
	 * Returns the media for the field object.<BR>
	 *
	 * @return
	 */
	@Transient
	public List<Media> getFieldObjectMedia() {
		try {
			List<Media> result = getMediaList(getFieldUnit(false), false);
			return result == null ? new ArrayList<Media>() : result;
		} catch (DerivedUnitFacadeNotSupportedException e) {
			throw new IllegalStateException(notSupportMessage, e);
		}
	}

	public boolean removeFieldObjectMedia(Media media) {
		try {
			return removeMedia(media, getFieldUnit(false));
		} catch (DerivedUnitFacadeNotSupportedException e) {
			throw new IllegalStateException(notSupportMessage, e);
		}
	}

	// field number
	@Transient
	public String getFieldNumber() {
		if (!hasFieldUnit()) {
			return null;
		} else {
			return getFieldUnit(true).getFieldNumber();
		}
	}

	public void setFieldNumber(String fieldNumber) {
		getFieldUnit(true).setFieldNumber(fieldNumber);
	}

	// primary collector
	@Transient
	public Person getPrimaryCollector() {
		if (!hasFieldUnit()) {
			return null;
		} else {
			return getFieldUnit(true).getPrimaryCollector();
		}
	}

	public void setPrimaryCollector(Person primaryCollector) {
		getFieldUnit(true).setPrimaryCollector(primaryCollector);
	}

	// field notes
	@Transient
	public String getFieldNotes() {
		if (!hasFieldUnit()) {
			return null;
		} else {
			return getFieldUnit(true).getFieldNotes();
		}
	}

	public void setFieldNotes(String fieldNotes) {
		getFieldUnit(true).setFieldNotes(fieldNotes);
	}

	// individual counts
	@Transient
	public Integer getIndividualCount() {
		return (hasFieldUnit() ? getFieldUnit(true).getIndividualCount() : null);
	}

	public void setIndividualCount(Integer individualCount) {
		getFieldUnit(true).setIndividualCount(individualCount);
	}

	// life stage
	@Transient
	public DefinedTerm getLifeStage() {
		return (hasFieldUnit() ? getFieldUnit(true).getLifeStage() : null);
	}

	public void setLifeStage(DefinedTerm lifeStage) {
	    FieldUnit fieldUnit = getFieldUnit(lifeStage != null);
        if (fieldUnit != null){
            fieldUnit.setLifeStage(lifeStage);
        }
	}

	// sex
	@Transient
	public DefinedTerm getSex() {
		return (hasFieldUnit() ? getFieldUnit(true).getSex(): null);
	}

	public void setSex(DefinedTerm sex) {
	    FieldUnit fieldUnit = getFieldUnit(sex != null);
        if (fieldUnit != null){
            fieldUnit.setSex(sex);
        }
	}

	// kind of Unit
	@Transient
	public DefinedTerm getKindOfUnit() {
		return (hasFieldUnit() ? getFieldUnit(true).getKindOfUnit() : null);
	}
//
//   @Transient
//    public DefinedTerm getDerivedUnitKindOfUnit() {
//       checkDerivedUnit();
//       return checkDerivedUnit() ? derivedUnit.getKindOfUnit() : null;
//    }


	/**
	 * Sets the kind-of-unit
	 * @param kindOfUnit
	 */
	public void setKindOfUnit(DefinedTerm kindOfUnit) {
	    FieldUnit fieldUnit = getFieldUnit(kindOfUnit != null);
	    if (fieldUnit != null){
	        fieldUnit.setKindOfUnit(kindOfUnit);
	    }
	}

//    public void setDerivedUnitKindOfUnit(DefinedTerm kindOfUnit) {
//        testDerivedUnit();
//
//        baseUnit().setKindOfUnit(kindOfUnit);
//    }


	// field unit
	public boolean hasFieldUnit() {
		return (getFieldUnit(CREATE_NOT) != null);
	}

	/**
	 * Returns the field unit as an object.
	 *
	 * @return
	 */
	public FieldUnit innerFieldUnit() {
		return getFieldUnit(CREATE_NOT);
	}

	/**
	 * Returns the field unit as an object.
	 *
	 * @return
	 */
	public FieldUnit getFieldUnit(boolean createIfNotExists) {
		if (fieldUnit == null && createIfNotExists) {
			setFieldUnit(FieldUnit.NewInstance());
		}
		return this.fieldUnit;
	}


	public void setFieldUnit(FieldUnit fieldUnit) {
		this.fieldUnit = fieldUnit;
		if (fieldUnit != null){
			if (config.isFirePropertyChangeEvents()){
				addNewEventPropagationListener(fieldUnit);
			}
			if (derivedUnit != null){
				DerivationEvent derivationEvent = getDerivationEvent(CREATE);
				derivationEvent.addOriginal(fieldUnit);
			}
			setFieldUnitCacheStrategy();
		}
	}

	// ****************** Specimen *******************************************


	// Definition
	public void addDerivedUnitDefinition(String text, Language language) {
		innerDerivedUnit().putDefinition(language, text);
	}

	@Transient
	public Map<Language, LanguageString> getDerivedUnitDefinitions() {
		return ! checkDerivedUnit()? null : this.derivedUnit.getDefinition();
	}


	public String getDerivedUnitDefinition(Language language) {
		if (! checkDerivedUnit()){
			return null;
		}
		Map<Language, LanguageString> languageMap = derivedUnit.getDefinition();
		LanguageString languageString = languageMap.get(language);
		if (languageString != null) {
			return languageString.getText();
		} else {
			return null;
		}
	}

	public void removeDerivedUnitDefinition(Language lang) {
		testDerivedUnit();
		derivedUnit.removeDefinition(lang);
	}

	// Determination
	public void addDetermination(DeterminationEvent determination) {
		//TODO implement correct bidirectional mapping in model classes
		determination.setIdentifiedUnit(baseUnit());
		baseUnit().addDetermination(determination);
	}

	@Transient
	public DeterminationEvent getPreferredDetermination() {
		Set<DeterminationEvent> events = baseUnit().getDeterminations();
		for (DeterminationEvent event : events){
			if (event.getPreferredFlag() == true){
				return event;
			}
		}
		return null;
	}

	/**
	 * This method returns the preferred determination.
	 * @see #getOtherDeterminations()
	 * @see #getDeterminations()
	 * @return
	 */
	@Transient
	public void setPreferredDetermination(DeterminationEvent newEvent) {
		Set<DeterminationEvent> events = baseUnit().getDeterminations();
		for (DeterminationEvent event : events){
			if (event.getPreferredFlag() == true){
				event.setPreferredFlag(false);
			}
		}
		newEvent.setPreferredFlag(true);
		events.add(newEvent);
	}

	/**
	 * This method returns all determinations except for the preferred one.
	 * @see #getPreferredDetermination()
	 * @see #getDeterminations()
	 * @return
	 */
	@Transient
	public Set<DeterminationEvent> getOtherDeterminations() {
		Set<DeterminationEvent> events = baseUnit().getDeterminations();
		Set<DeterminationEvent> result = new HashSet<>();
		for (DeterminationEvent event : events){
			if (event.getPreferredFlag() != true){
				result.add(event);
			}
		}
		return result;
	}

	/**
	 * This method returns all determination events. The preferred one {@link #getPreferredDetermination()}
	 * and all others {@link #getOtherDeterminations()}.
	 * @return
	 */
	@Transient
	public Set<DeterminationEvent> getDeterminations() {
		return baseUnit().getDeterminations();
	}

	public void removeDetermination(DeterminationEvent determination) {
		baseUnit().removeDetermination(determination);
	}

	// Media
	public boolean addDerivedUnitMedia(Media media) {
		testDerivedUnit();
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
	public boolean hasDerivedUnitImageGallery() {
		return (getImageGallery(derivedUnit, false) != null);
	}

	public SpecimenDescription getDerivedUnitImageGallery(boolean createIfNotExists) {
		if (!checkDerivedUnit()){
			return null;
		}
		TextData textData;
		try {
			textData = inititializeTextDataWithSupportTest(Feature.IMAGE(),
					derivedUnit, createIfNotExists, true);
		} catch (DerivedUnitFacadeNotSupportedException e) {
			throw new IllegalStateException(notSupportMessage, e);
		}
		if (textData != null) {
			return CdmBase.deproxy(textData.getInDescription(),
					SpecimenDescription.class);
		} else {
			return null;
		}
	}

	public void setDerivedUnitImageGallery(SpecimenDescription imageGallery)
			throws DerivedUnitFacadeNotSupportedException {
		testDerivedUnit();
		SpecimenDescription existingGallery = getDerivedUnitImageGallery(false);

		// test attached specimens contain this.derivedUnit
		SpecimenOrObservationBase facadeDerivedUnit = innerDerivedUnit();
		testSpecimenInImageGallery(imageGallery, facadeDerivedUnit);

		if (existingGallery != null) {
			if (existingGallery != imageGallery) {
				throw new DerivedUnitFacadeNotSupportedException(
						"DerivedUnitFacade does not allow more than one image gallery");
			} else {
				// do nothing
			}
		} else {
			TextData textData = testImageGallery(imageGallery);
			this.derivedUnitMediaTextData = textData;
		}
	}

	/**
	 * @param imageGallery
	 * @throws DerivedUnitFacadeNotSupportedException
	 */
	private void testSpecimenInImageGallery(SpecimenDescription imageGallery, SpecimenOrObservationBase specimen)
				throws DerivedUnitFacadeNotSupportedException {
		SpecimenOrObservationBase imageGallerySpecimen = imageGallery.getDescribedSpecimenOrObservation();
		if (imageGallerySpecimen == null) {
			throw new DerivedUnitFacadeNotSupportedException(
					"Image Gallery has no Specimen attached. Please attache according specimen or field unit.");
		}
		if (! imageGallerySpecimen.equals(specimen)) {
			throw new DerivedUnitFacadeNotSupportedException(
					"Image Gallery has not the facade's field object attached. Please add field object first " +
					"to image gallery specimenOrObservation list.");
		}
	}

	/**
	 * Returns the media for the specimen.<BR>
	 *
	 * @return
	 */
	@Transient
	public List<Media> getDerivedUnitMedia() {
		if (! checkDerivedUnit()){
			return new ArrayList<Media>();
		}
		try {
			List<Media> result = getMediaList(derivedUnit, false);
			return result == null ? new ArrayList<Media>() : result;
		} catch (DerivedUnitFacadeNotSupportedException e) {
			throw new IllegalStateException(notSupportMessage, e);
		}
	}

	public boolean removeDerivedUnitMedia(Media media) {
		testDerivedUnit();
		try {
			return removeMedia(media, derivedUnit);
		} catch (DerivedUnitFacadeNotSupportedException e) {
			throw new IllegalStateException(notSupportMessage, e);
		}
	}

	// Accession Number
	@Transient
	public String getAccessionNumber() {
		return ! checkDerivedUnit()? null : derivedUnit.getAccessionNumber();
	}

	public void setAccessionNumber(String accessionNumber) {
		testDerivedUnit();
		derivedUnit.setAccessionNumber(accessionNumber);
	}

	@Transient
	public String getCatalogNumber() {
		return ! checkDerivedUnit()? null : derivedUnit.getCatalogNumber();
	}

	public void setCatalogNumber(String catalogNumber) {
		testDerivedUnit();
		derivedUnit.setCatalogNumber(catalogNumber);
	}

	@Transient
	public String getBarcode() {
		return ! checkDerivedUnit()? null : derivedUnit.getBarcode();
	}

	public void setBarcode(String barcode) {
		testDerivedUnit();
		derivedUnit.setBarcode(barcode);
	}

	// Preservation Method

	/**
	 * Only supported by specimen and fossils
	 *
	 * @see #DerivedUnitType
	 * @return
	 */
	@Transient
	public PreservationMethod getPreservationMethod() throws MethodNotSupportedByDerivedUnitTypeException {
		if (derivedUnit!=null && derivedUnit.getRecordBasis().isPreservedSpecimen()) {
			return CdmBase.deproxy(derivedUnit, DerivedUnit.class).getPreservation();
		} else {
			if (this.config.isThrowExceptionForNonSpecimenPreservationMethodRequest()) {
				throw new MethodNotSupportedByDerivedUnitTypeException(
						"A preservation method is only available in derived units of type 'Preserved Specimen' or one of its specializations like 'Fossil Specimen' ");
			} else {
				return null;
			}
		}
	}

	/**
	 * Only supported by specimen and fossils
	 *
	 * @see #DerivedUnitType
	 * @return
	 */
	public void setPreservationMethod(PreservationMethod preservation)
			throws MethodNotSupportedByDerivedUnitTypeException {
		if (derivedUnit!=null && derivedUnit.getRecordBasis().isPreservedSpecimen()) {
			CdmBase.deproxy(derivedUnit, DerivedUnit.class).setPreservation(preservation);
		} else {
			if (this.config.isThrowExceptionForNonSpecimenPreservationMethodRequest()) {
				throw new MethodNotSupportedByDerivedUnitTypeException(
						"A preservation method is only available in derived units of type 'Specimen' or 'Fossil'");
			} else {
				return;
			}
		}
	}

	//preferred stable URI  #5606
	@Transient
    public URI getPreferredStableUri(){
        return baseUnit().getPreferredStableUri();
    }
    public void setPreferredStableUri(URI stableUri){
        baseUnit().setPreferredStableUri(stableUri);
    }


	// Stored under name
	@Transient
	public TaxonName getStoredUnder() {
		return ! checkDerivedUnit()? null : derivedUnit.getStoredUnder();
	}

	public void setStoredUnder(TaxonName storedUnder) {
		testDerivedUnit();
		derivedUnit.setStoredUnder(storedUnder);
	}

	// title cache
	public String getTitleCache() {
		SpecimenOrObservationBase<?> titledUnit = baseUnit();

		if (!titledUnit.isProtectedTitleCache()) {
			// always compute title cache anew as long as there are no property
			// change listeners on
			// field unit, gathering event etc
			titledUnit.setTitleCache(null, false);
		}
		return titledUnit.getTitleCache();
	}

	public boolean isProtectedTitleCache() {
		return baseUnit().isProtectedTitleCache();
	}

	public void setTitleCache(String titleCache, boolean isProtected) {
		this.baseUnit().setTitleCache(titleCache, isProtected);
	}

	/**
	 * Returns the derived unit itself.
	 *
	 * @return the derived unit
	 */
	public DerivedUnit innerDerivedUnit() {
		return this.derivedUnit;
	}

//	/**
//	 * Returns the derived unit itself.
//	 *
//	 * @return the derived unit
//	 */
//	public DerivedUnit innerDerivedUnit(boolean createIfNotExists) {
//		DerivedUnit result = this.derivedUnit;
//		if (result == null && createIfNotExists){
//			if (this.fieldUnit == null){
//				String message = "Field unit must exist to create derived unit.";
//				throw new IllegalStateException(message);
//			}else{
//				DerivedUnit =
//				DerivationEvent derivationEvent = getDerivationEvent(true);
//				derivationEvent.addOriginal(fieldUnit);
//				return this.derivedUnit;
//			}
//		}
//	}

	private boolean hasDerivationEvent() {
		return getDerivationEvent() == null ? false : true;
	}

	private DerivationEvent getDerivationEvent() {
		return getDerivationEvent(CREATE_NOT);
	}

	/**
	 * Returns the derivation event. If no derivation event exists and <code>createIfNotExists</code>
	 * is <code>true</code> a new derivation event is created and returned.
	 * Otherwise <code>null</code> is returned.
	 * @param createIfNotExists
	 */
	private DerivationEvent getDerivationEvent(boolean createIfNotExists) {
		DerivationEvent result = null;
		if (derivedUnit != null){
			result = derivedUnit.getDerivedFrom();
		}else{
			return null;
		}
		if (result == null && createIfNotExists) {
			DerivationEventType type = null;
			if (isAccessioned(derivedUnit)){
				type = DerivationEventType.ACCESSIONING();
			}

			result = DerivationEvent.NewInstance(type);
			derivedUnit.setDerivedFrom(result);
		}
		return result;
	}

	/**
	 * TODO still unclear which classes do definetly require accessioning.
	 * Only return true for those classes which are clear.
	 * @param derivedUnit
	 * @return
	 */
	private boolean isAccessioned(DerivedUnit derivedUnit) {
		if (derivedUnit.getRecordBasis().equals(SpecimenOrObservationType.PreservedSpecimen) ){
			return true;   //maybe also subtypes should be true
		}else{
			return false;
		}
	}

	@Transient
	public String getExsiccatum() throws MethodNotSupportedByDerivedUnitTypeException {
		testDerivedUnit();
		if (derivedUnit.getRecordBasis().isPreservedSpecimen()) {
			return derivedUnit.getExsiccatum();
		} else {
			if (this.config.isThrowExceptionForNonSpecimenPreservationMethodRequest()) {
				throw new MethodNotSupportedByDerivedUnitTypeException(
						"An exsiccatum is only available in derived units of type 'Specimen' or 'Fossil'");
			} else {
				return null;
			}
		}
	}

	public void setExsiccatum(String exsiccatum) throws Exception {
		testDerivedUnit();
		if (derivedUnit.getRecordBasis().isPreservedSpecimen()) {
			derivedUnit.setExsiccatum(exsiccatum);
		} else {
			if (this.config.isThrowExceptionForNonSpecimenPreservationMethodRequest()) {
				throw new MethodNotSupportedByDerivedUnitTypeException(
						"An exsiccatum is only available in derived units of type 'Specimen' or 'Fossil'");
			} else {
				return;
			}
		}
	}

	/**
	 * Returns the original label information of the derived unit.
	 * @return
	 */
	@Transient
	public String getOriginalLabelInfo() {
		return ! checkDerivedUnit()? null : derivedUnit.getOriginalLabelInfo();
	}
	public void setOriginalLabelInfo(String originalLabelInfo) {
		testDerivedUnit();
		derivedUnit.setOriginalLabelInfo(originalLabelInfo);
	}

	// **** sources **/
	public void addSource(IdentifiableSource source) {
		this.baseUnit().addSource(source);
	}

	/**
	 * Creates an {@link IOriginalSource orignal source} or type ,
	 * adds it to the specimen and returns it.
	 *
	 * @param reference
	 * @param microReference
	 * @param originalNameString
	 * @return
	 */
	public IdentifiableSource addSource(OriginalSourceType type, Reference reference, String microReference, String originalNameString) {
		IdentifiableSource source = IdentifiableSource.NewInstance(type, null, null, reference, microReference);
		source.setOriginalNameString(originalNameString);
		addSource(source);
		return source;
	}

	@Transient
	public Set<IdentifiableSource> getSources() {
		return baseUnit().getSources();
	}

	public void removeSource(IdentifiableSource source) {
		this.baseUnit().removeSource(source);
	}

	//*** identifiers ***/


    public void addIdentifier(Identifier identifier) {
        this.baseUnit().addIdentifier(identifier);
    }

	@Transient
	public List<Identifier> getIdentifiers() {
	    return baseUnit().getIdentifiers();
	}

	public void removeIdentifier(Identifier identifier) {
	    this.baseUnit().removeIdentifier(identifier);
	}

	@Transient
	public Set<Rights> getRights() {
		return baseUnit().getRights();
	}

	/**
	 * @return the collection
	 */
	@Transient
	public Collection getCollection() {
		return ! checkDerivedUnit()? null :  derivedUnit.getCollection();
	}

	/**
	 * @param collection
	 *            the collection to set
	 */
	public void setCollection(Collection collection) {
		testDerivedUnit();
		derivedUnit.setCollection(collection);
	}

	// annotation
	public void addAnnotation(Annotation annotation) {
		this.baseUnit().addAnnotation(annotation);
	}

	@Transient
	public void getAnnotations() {
		this.baseUnit().getAnnotations();
	}

	public void removeAnnotation(Annotation annotation) {
		this.baseUnit().removeAnnotation(annotation);
	}

	// ******************************* Events ***************************

	//set of events that were currently fired by this facades field unit
	//to avoid recursive fireing of the same event
	private final Set<PropertyChangeEvent> fireingEvents = new HashSet<>();

	/**
	 * @return
	 */
	private void addNewEventPropagationListener(CdmBase listeningObject) {
		//if there is already a listener, don't do anything
		for (PropertyChangeListener listener : this.listeners.keySet()){
			if (listeners.get(listener) == listeningObject){
				return;
			}
		}
		//create new listener
		PropertyChangeListener listener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (derivedUnit != null){
					derivedUnit.firePropertyChange(event);
				}else{
					if (! event.getSource().equals(fieldUnit) && ! fireingEvents.contains(event)  ){
						fireingEvents.add(event);
						fieldUnit.firePropertyChange(event);
						fireingEvents.remove(event);
					}
				}
			}
		};
		//add listener to listening object and to list of listeners
		listeningObject.addPropertyChangeListener(listener);
		listeners.put(listener, listeningObject);
	}

	// **************** Other Collections ********************************

	/**
	 * Creates a duplicate specimen which derives from the same derivation event
	 * as the facade specimen and adds collection data to it (all data available
	 * in DerivedUnit and Specimen. Data from SpecimenOrObservationBase and
	 * above are not yet shared at the moment.
	 *
	 * @param collection
	 * @param catalogNumber
	 * @param accessionNumber
	 * @param storedUnder
	 * @param preservation
	 * @return
	 */
	public DerivedUnit addDuplicate(Collection collection, String catalogNumber,
			String accessionNumber, TaxonName storedUnder, PreservationMethod preservation){
		testDerivedUnit();
		DerivedUnit duplicate = DerivedUnit.NewPreservedSpecimenInstance();
		duplicate.setDerivedFrom(getDerivationEvent(CREATE));
		duplicate.setCollection(collection);
		duplicate.setCatalogNumber(catalogNumber);
		duplicate.setAccessionNumber(accessionNumber);
		duplicate.setStoredUnder(storedUnder);
		duplicate.setPreservation(preservation);
		return duplicate;
	}

	public void addDuplicate(DerivedUnit duplicateSpecimen) {
		testDerivedUnit();
		getDerivationEvent(CREATE).addDerivative(duplicateSpecimen);
	}

	@Transient
	public Set<DerivedUnit> getDuplicates() {
		if (! checkDerivedUnit()){
			return new HashSet<>();
		}
		Set<DerivedUnit> result = new HashSet<>();
		if (hasDerivationEvent()) {
			for (DerivedUnit derivedUnit : getDerivationEvent(CREATE)
					.getDerivatives()) {
				if (derivedUnit.isInstanceOf(DerivedUnit.class)
						&& !derivedUnit.equals(this.derivedUnit)) {
					result.add(CdmBase.deproxy(derivedUnit, DerivedUnit.class));
				}
			}
		}
		return result;
	}

	public void removeDuplicate(DerivedUnit duplicateSpecimen) {
		testDerivedUnit();
		if (hasDerivationEvent()) {
			getDerivationEvent(CREATE).removeDerivative(duplicateSpecimen);
		}
	}

	public SpecimenOrObservationBase<?> baseUnit(){
	    if(derivedUnit!=null){
	        return derivedUnit;
	    }
	    else if(fieldUnit!=null){
	        return fieldUnit;
	    }
	    else{
	        throw new IllegalStateException("A DerivedUnitFacade must always have either a field unit or a derived unit");
	    }
	}

	/**
	 * @return true if <code>this.derivedUnit</code> exists
	 */
	private boolean checkDerivedUnit()  {
		if (derivedUnit == null){
			return false;
		}else{
			return true;
		}
	}

	private void testDerivedUnit() /* throws MethodNotSupportedByDerivedUnitTypeException */ {
		if (derivedUnit == null){
			throw new IllegalStateException("This method is not allowed for this specimen or observation type. Probably you have tried to add specimen(derived unit) information to a field unit");
		}
	}

	public void setType(SpecimenOrObservationType type) {
		if (type == null){
			throw new IllegalArgumentException("The type of a specimen or observation may not be null");
		}
		SpecimenOrObservationBase<?> baseUnit = baseUnit();
		if(baseUnit.isInstanceOf(FieldUnit.class) && !type.isFieldUnit()){
		    throw new IllegalArgumentException("A FieldUnit may only be of type FieldUnit") ;
		}
		else if(baseUnit.isInstanceOf(DerivedUnit.class) && type.isFieldUnit()){
		    throw new IllegalArgumentException("A derived unit may not be of type FieldUnit") ;
		}
		baseUnit.setRecordBasis(type);
	}

	public SpecimenOrObservationType getType() {
	    return baseUnit().getRecordBasis();
	}

	/**
	 * Closes this facade. As a minimum this method removes all listeners created by this facade from their
	 * listening objects.
	 */
	public void close(){
		for (PropertyChangeListener listener : this.listeners.keySet()){
			CdmBase listeningObject = listeners.get(listener);
			listeningObject.removePropertyChangeListener(listener);
		}
	}


	/**
	 * Computes the correct distance string for given values for min, max and text.
	 * If text is not blank, text is returned, otherwise "min - max" or a single value is returned.
	 * @param min min value as number
	 * @param max max value as number
	 * @param text text representation of distance
	 * @return the formatted distance string
	 */
	public static String distanceString(Number min, Number max, String text, String unit) {
		if (StringUtils.isNotBlank(text)){
			return text;
		}else{
			String minStr = min == null? null : String.valueOf(min);
			String maxStr = max == null? null : String.valueOf(max);
			String result = CdmUtils.concat(UTF8.EN_DASH_SPATIUM.toString(), minStr, maxStr);
			if (StringUtils.isNotBlank(result) && StringUtils.isNotBlank(unit)){
				result = result + " " + unit;
			}
			return result;
		}
	}

	/**
	 * First checks the inner field unit for the publish flag. If set to <code>true</code>
	 * then <code>true</code> is returned. If the field unit is <code>null</code> the inner derived unit
	 * is checked.
	 * @return <code>true</code> if this facade can be published
	 */
	public boolean isPublish(){
	    if(fieldUnit!=null){
	        return fieldUnit.isPublish();
	    }
	    if(derivedUnit!=null){
	        return derivedUnit.isPublish();
	    }
	    return false;
	}
}
