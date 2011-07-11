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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Transient;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
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
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * This class is a facade to the eu.etaxonomy.cdm.model.occurrence package from
 * a specimen based view. It does not support all functionality available in the
 * occurrence package.<BR>
 * The most significant restriction is that a specimen may derive only from one
 * direct derivation event and there must be only one field observation
 * (gathering event) it derives from.<BR>
 * 
 * @author a.mueller
 * @date 14.05.2010
 */
public class DerivedUnitFacade {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DerivedUnitFacade.class);

	private static final String notSupportMessage = "A specimen facade not supported exception has occurred at a place where this should not have happened. The developer should implement not support check properly during class initialization ";

	private static final boolean CREATE = true;
	private static final boolean CREATE_NOT = false;

	/**
	 * Enum that defines the class the "Specimen" belongs to. Some methods of
	 * the facade are not available for certain classes and will throw an
	 * Exception when invoking them.
	 */
	public enum DerivedUnitType {
		Specimen("Specimen"), 
		Observation("Observation"), 
		LivingBeing("Living Being"), 
		Fossil("Fossil"), 
		DerivedUnit("Derived Unit"),
		//Field Observation is experimental, please handle with care (it is the only type which does not
		//have a derivedUnit and therefore throws exceptions for all method on derivedUnit attributes
		FieldObservation("FieldObservation");

		String representation;

		private DerivedUnitType(String representation) {
			this.representation = representation;
		}

		/**
		 * @return the representation
		 */
		public String getRepresentation() {
			return representation;
		}

		private DerivedUnitBase getNewDerivedUnitInstance() {
			if (this == DerivedUnitType.Specimen) {
				return eu.etaxonomy.cdm.model.occurrence.Specimen.NewInstance();
			} else if (this == DerivedUnitType.Observation) {
				return eu.etaxonomy.cdm.model.occurrence.Observation.NewInstance();
			} else if (this == DerivedUnitType.LivingBeing) {
				return eu.etaxonomy.cdm.model.occurrence.LivingBeing.NewInstance();
			} else if (this == DerivedUnitType.Fossil) {
				return eu.etaxonomy.cdm.model.occurrence.Fossil.NewInstance();
			} else if (this == DerivedUnitType.DerivedUnit) {
				return eu.etaxonomy.cdm.model.occurrence.DerivedUnit.NewInstance();
			} else if (this == DerivedUnitType.FieldObservation) {
				return null;
			} else {
				String message = "Unknown derived unit type %s";
				message = String.format(message, this.getRepresentation());
				throw new IllegalStateException(message);
			}
		}

		public static DerivedUnitType valueOf2(String type) {
			if (type == null) {
				return null;
			}
			type = type.replace(" ", "").toLowerCase();
			if (type.equals("specimen")) {
				return Specimen;
			} else if (type.equals("livingbeing")) {
				return LivingBeing;
			} else if (type.equals("observation")) {
				return Observation;
			} else if (type.equals("fossil")) {
				return Fossil;
			} else if (type.equals("fieldobservation")) {
				return DerivedUnitType.FieldObservation;
			} else if (type.equals("unknown")) {
				return DerivedUnitType.DerivedUnit;
			} else if (type.equals("derivedunit")) {
				return DerivedUnitType.DerivedUnit;
			}
			return null;
		}
		
		public static DerivedUnitType valueOf2(Class<? extends SpecimenOrObservationBase> clazz) {
			if (clazz == null) {
				return null;
			}
			if (clazz.equals(Specimen.class)) {
				return Specimen;
			} else if (clazz.equals(eu.etaxonomy.cdm.model.occurrence.LivingBeing.class)) {
				return LivingBeing;
			} else if (clazz.equals(eu.etaxonomy.cdm.model.occurrence.Observation.class)) {
				return Observation;
			} else if (clazz.equals(eu.etaxonomy.cdm.model.occurrence.Fossil.class)) {
				return Fossil;
			} else if (clazz.equals(FieldObservation.class)) {
				return DerivedUnitType.FieldObservation;
			} else if (clazz.equals(DerivedUnit.class)) {
				return DerivedUnitType.DerivedUnit;
			}
			return null;
		}

	}

	private final DerivedUnitFacadeConfigurator config;
	
	private Map<PropertyChangeListener, CdmBase> listeners = new HashMap<PropertyChangeListener, CdmBase>();

	// private GatheringEvent gatheringEvent;
	private DerivedUnitType type; // needed?

	private FieldObservation fieldObservation;

	private final DerivedUnitBase derivedUnit;

	// media - the text data holding the media
	private TextData derivedUnitMediaTextData;
	private TextData fieldObjectMediaTextData;

	private TextData ecology;
	private TextData plantDescription;

	/**
	 * Creates a derived unit facade for a new derived unit of type
	 * <code>type</code>.
	 * 
	 * @param type
	 * @return
	 */
	public static DerivedUnitFacade NewInstance(DerivedUnitType type) {
		return new DerivedUnitFacade(type, null, null);
	}
	
	/**
	 * Creates a derived unit facade for a new derived unit of type
	 * <code>type</code>.
	 * 
	 * @param type
	 * @return
	 */
	public static DerivedUnitFacade NewInstance(DerivedUnitType type, FieldObservation fieldObservation) {
		return new DerivedUnitFacade(type, fieldObservation, null);
	}

	/**
	 * Creates a derived unit facade for a new derived unit of type
	 * <code>type</code>.
	 * 
	 * @param type
	 * @param fieldObservation the field observation to use
	 * @param config the facade configurator to use
	 * //TODO are there any ambiguities to solve with defining a field observation or a configurator 
	 * @return
	 */
	public static DerivedUnitFacade NewInstance(DerivedUnitType type, FieldObservation fieldObservation, DerivedUnitFacadeConfigurator config) {
		return new DerivedUnitFacade(type, fieldObservation, config);
	}

	
	/**
	 * Creates a derived unit facade for a given derived unit using the default
	 * configuration.
	 * 
	 * @param derivedUnit
	 * @return
	 * @throws DerivedUnitFacadeNotSupportedException
	 */
	public static DerivedUnitFacade NewInstance(DerivedUnitBase derivedUnit)
			throws DerivedUnitFacadeNotSupportedException {
		return new DerivedUnitFacade(derivedUnit, null);
	}

	public static DerivedUnitFacade NewInstance(DerivedUnitBase derivedUnit,
			DerivedUnitFacadeConfigurator config)
			throws DerivedUnitFacadeNotSupportedException {
		return new DerivedUnitFacade(derivedUnit, config);
	}

	// ****************** CONSTRUCTOR ******************************************

	private DerivedUnitFacade(DerivedUnitType type, FieldObservation fieldObservation, DerivedUnitFacadeConfigurator config) {
		if (config == null){
			config = DerivedUnitFacadeConfigurator.NewInstance();
		}
		this.config = config;
		this.type = type;
		// derivedUnit
		derivedUnit = type.getNewDerivedUnitInstance();
		setFieldObservation(fieldObservation);
		if (derivedUnit != null){
			setCacheStrategy();
		}else{
			setFieldObservationCacheStrategy();
		}
	}

	private DerivedUnitFacade(DerivedUnitBase derivedUnit,
			DerivedUnitFacadeConfigurator config)
			throws DerivedUnitFacadeNotSupportedException {

		if (config == null) {
			config = DerivedUnitFacadeConfigurator.NewInstance();
		}
		this.config = config;

		// derived unit
		this.derivedUnit = derivedUnit;
		this.type = DerivedUnitType.valueOf2(this.derivedUnit.getClass());
		
		// derivation event
		if (this.derivedUnit.getDerivedFrom() != null) {
			DerivationEvent derivationEvent = getDerivationEvent(CREATE);
			// fieldObservation
			Set<FieldObservation> fieldOriginals = getFieldObservationsOriginals(
					derivationEvent, null);
			if (fieldOriginals.size() > 1) {
				throw new DerivedUnitFacadeNotSupportedException(
						"Specimen must not have more than 1 derivation event");
			} else if (fieldOriginals.size() == 0) {
				// fieldObservation = FieldObservation.NewInstance();
			} else if (fieldOriginals.size() == 1) {
				fieldObservation = fieldOriginals.iterator().next();
				// ###fieldObservation =
				// getInitializedFieldObservation(fieldObservation);
				if (config.isFirePropertyChangeEvents()){
					addNewEventPropagationListener(fieldObservation);
				}
			} else {
				throw new IllegalStateException("Illegal state");
			}
		}
		
		this.derivedUnitMediaTextData = inititializeTextDataWithSupportTest(
				Feature.IMAGE(), this.derivedUnit, false, true);

		fieldObjectMediaTextData = initializeFieldObjectTextDataWithSupportTest(
				Feature.IMAGE(), false, true);

		// handle derivedUnit.getMedia()
		if (derivedUnit.getMedia().size() > 0) {
			// TODO better changed model here to allow only one place for images
			if (this.config.isMoveDerivedUnitMediaToGallery()) {
				Set<Media> mediaSet = derivedUnit.getMedia();
				for (Media media : mediaSet) {
					this.addDerivedUnitMedia(media);
				}
				mediaSet.removeAll(getDerivedUnitMedia());
			} else {
				throw new DerivedUnitFacadeNotSupportedException(
						"Specimen may not have direct media. Only (one) image gallery is allowed");
			}
		}

		// handle fieldObservation.getMedia()
		if (fieldObservation != null && fieldObservation.getMedia() != null
				&& fieldObservation.getMedia().size() > 0) {
			// TODO better changed model here to allow only one place for images
			if (this.config.isMoveFieldObjectMediaToGallery()) {
				Set<Media> mediaSet = fieldObservation.getMedia();
				for (Media media : mediaSet) {
					this.addFieldObjectMedia(media);
				}
				mediaSet.removeAll(getFieldObjectMedia());
			} else {
				throw new DerivedUnitFacadeNotSupportedException(
						"Field object may not have direct media. Only (one) image gallery is allowed");
			}
		}

		// test if descriptions are supported
		ecology = initializeFieldObjectTextDataWithSupportTest(
				Feature.ECOLOGY(), false, false);
		plantDescription = initializeFieldObjectTextDataWithSupportTest(
				Feature.DESCRIPTION(), false, false);
		
		setCacheStrategy();

	}

	private DerivedUnitBase getInitializedDerivedUnit(
			DerivedUnitBase derivedUnit) {
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
		DerivedUnitBase result = (DerivedUnitBase) occurrenceService.load(
				derivedUnit.getUuid(), propertyPaths);
		return result;
	}

	/**
	 * Initializes the derived unit according to the configuartions property
	 * path. If the property path is <code>null</code> or no occurrence service
	 * is given the returned object is the same as the input parameter.
	 * 
	 * @param fieldObservation2
	 * @return
	 */
	private FieldObservation getInitializedFieldObservation(
			FieldObservation fieldObservation) {
		IOccurrenceService occurrenceService = this.config
				.getOccurrenceService();
		if (occurrenceService == null) {
			return fieldObservation;
		}
		List<String> propertyPaths = this.config.getPropertyPaths();
		if (propertyPaths == null) {
			return fieldObservation;
		}
		propertyPaths = getFieldObjectPropertyPaths(propertyPaths);
		FieldObservation result = (FieldObservation) occurrenceService.load(
				fieldObservation.getUuid(), propertyPaths);
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
			// storedUnder (TaxonNameBase)
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
			setFieldObservationCacheStrategy();
		}
	}

	private void setFieldObservationCacheStrategy() {
		if (this.hasFieldObject()){
			DerivedUnitFacadeFieldObservationCacheStrategy strategy = new DerivedUnitFacadeFieldObservationCacheStrategy();
			this.fieldObservation.setCacheStrategy(strategy);
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
		FieldObservation fieldObject = getFieldObservation(createIfNotExists);
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
			SpecimenOrObservationBase specimen, boolean createIfNotExists,
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
		Set<DescriptionElementBase> existingTextData = new HashSet<DescriptionElementBase>();
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
			this.fieldObjectMediaTextData = getImageGalleryTextData(
					fieldObservation, "Field observation");
		}
		return this.fieldObjectMediaTextData;
	}

	/**
	 * @param derivationEvent2
	 * @return
	 * @throws DerivedUnitFacadeNotSupportedException
	 */
	private Set<FieldObservation> getFieldObservationsOriginals(
			DerivationEvent derivationEvent,
			Set<SpecimenOrObservationBase> recursionAvoidSet)
			throws DerivedUnitFacadeNotSupportedException {
		if (recursionAvoidSet == null) {
			recursionAvoidSet = new HashSet<SpecimenOrObservationBase>();
		}
		Set<FieldObservation> result = new HashSet<FieldObservation>();
		Set<SpecimenOrObservationBase> originals = derivationEvent
				.getOriginals();
		for (SpecimenOrObservationBase original : originals) {
			if (original.isInstanceOf(FieldObservation.class)) {
				result.add(CdmBase.deproxy(original, FieldObservation.class));
			} else if (original.isInstanceOf(DerivedUnitBase.class)) {
				// if specimen has already been tested exclude it from further
				// recursion
				if (recursionAvoidSet.contains(original)) {
					continue;
				}
				DerivedUnitBase derivedUnit = CdmBase.deproxy(original,
						DerivedUnitBase.class);
				DerivationEvent originalDerivation = derivedUnit
						.getDerivedFrom();
				// Set<DerivationEvent> derivationEvents =
				// original.getDerivationEvents();
				// for (DerivationEvent originalDerivation : derivationEvents){
				Set<FieldObservation> fieldObservations = getFieldObservationsOriginals(
						originalDerivation, recursionAvoidSet);
				result.addAll(fieldObservations);
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
	private boolean addMedia(Media media, SpecimenOrObservationBase<?> specimen)
			throws DerivedUnitFacadeNotSupportedException {
		if (media != null) {
			List<Media> mediaList = getMedia(specimen, true);
			return mediaList.add(media);
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
		List<Media> mediaList = getMedia(specimen, true);
		return mediaList == null ? null : mediaList.remove(media);
	}

	private List<Media> getMedia(SpecimenOrObservationBase<?> specimen,
			boolean createIfNotExists)
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
	// }else if (specimen == this.fieldObservation){
	// return getObservationImageGalleryTextData();
	// }else{
	// return getImageGalleryMedia(specimen, "Undefined specimen ");
	// }
	// }

	/**
	 * Returns the one media list of a specimen which is part of the only image
	 * gallery that this specimen is part of.<BR>
	 * If these conditions are not hold an exception is thrwon.
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
		} else if (specimen == this.fieldObservation) {
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

	// absolute elevation
	/**
	 * meter above/below sea level of the surface
	 * 
	 * @see #getAbsoluteElevationError()
	 * @see #getAbsoluteElevationRange()
	 **/
	@Transient
	public Integer getAbsoluteElevation() {
		return (hasGatheringEvent() ? getGatheringEvent(true)
				.getAbsoluteElevation() : null);
	}

	public void setAbsoluteElevation(Integer absoluteElevation) {
		getGatheringEvent(true).setAbsoluteElevation(absoluteElevation);
	}

	// absolute elevation error
	@Transient
	public Integer getAbsoluteElevationError() {
		return (hasGatheringEvent() ? getGatheringEvent(true)
				.getAbsoluteElevationError() : null);
	}

	public void setAbsoluteElevationError(Integer absoluteElevationError) {
		getGatheringEvent(true).setAbsoluteElevationError(
				absoluteElevationError);
	}

	/**
	 * @see #getAbsoluteElevation()
	 * @see #getAbsoluteElevationError()
	 * @see #setAbsoluteElevationRange(Integer, Integer)
	 * @see #getAbsoluteElevationMaximum()
	 */
	@Transient
	public Integer getAbsoluteElevationMinimum() {
		if (!hasGatheringEvent()) {
			return null;
		}
		Integer minimum = getGatheringEvent(true).getAbsoluteElevation();
		if (getGatheringEvent(true).getAbsoluteElevationError() != null) {
			minimum = minimum
					- getGatheringEvent(true).getAbsoluteElevationError();
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
	public Integer getAbsoluteElevationMaximum() {
		if (!hasGatheringEvent()) {
			return null;
		}
		Integer maximum = getGatheringEvent(true).getAbsoluteElevation();
		if (getGatheringEvent(true).getAbsoluteElevationError() != null) {
			maximum = maximum
					+ getGatheringEvent(true).getAbsoluteElevationError();
		}
		return maximum;
	}

	/**
	 * This method replaces absoluteElevation and absoulteElevationError by
	 * internally translating minimum and maximum values into average and error
	 * values. As all these values are integer based it is necessary that the
	 * distance is between minimum and maximum is <b>even</b>, otherwise we will
	 * get a rounding error resulting in a maximum that is increased by 1.
	 * 
	 * @see #setAbsoluteElevation(Integer)
	 * @see #setAbsoluteElevationError(Integer)
	 * @param minimumElevation
	 *            minimum of the range
	 * @param maximumElevation
	 *            maximum of the range
	 * @throws IllegalArgumentException
	 */
	public void setAbsoluteElevationRange(Integer minimumElevation, Integer maximumElevation) throws IllegalArgumentException{
		if (minimumElevation == null || maximumElevation == null) {
			Integer elevation = minimumElevation;
			Integer error = 0;
			if (minimumElevation == null) {
				elevation = maximumElevation;
				if (elevation == null) {
					error = null;
				}
			}
			getGatheringEvent(true).setAbsoluteElevation(elevation);
			getGatheringEvent(true).setAbsoluteElevationError(error);
		} else {
			if (!isEvenDistance(minimumElevation, maximumElevation)) {
				throw new IllegalArgumentException(
						"Distance between minimum and maximum elevation must be even but was "
								+ Math.abs(minimumElevation - maximumElevation));
			}
			Integer absoluteElevationError = Math.abs(maximumElevation
					- minimumElevation);
			absoluteElevationError = absoluteElevationError / 2;
			Integer absoluteElevation = minimumElevation
					+ absoluteElevationError;
			getGatheringEvent(true).setAbsoluteElevation(absoluteElevation);
			getGatheringEvent(true).setAbsoluteElevationError(
					absoluteElevationError);
		}
	}

	/**
	 * @param minimumElevation
	 * @param maximumElevation
	 * @return
	 */
	public boolean isEvenDistance(Integer minimumElevation,
			Integer maximumElevation) {
		Integer diff = (maximumElevation - minimumElevation);
		return diff % 2 == 0;
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
		return (hasGatheringEvent() ? getGatheringEvent(true)
				.getCollectingMethod() : null);
	}

	public void setCollectingMethod(String collectingMethod) {
		getGatheringEvent(true).setCollectingMethod(collectingMethod);
	}

	// distance to ground
	@Transient
	public Integer getDistanceToGround() {
		return (hasGatheringEvent() ? getGatheringEvent(true)
				.getDistanceToGround() : null);
	}

	public void setDistanceToGround(Integer distanceToGround) {
		getGatheringEvent(true).setDistanceToGround(distanceToGround);
	}

	// distance to water surface
	@Transient
	public Integer getDistanceToWaterSurface() {
		return (hasGatheringEvent() ? getGatheringEvent(true)
				.getDistanceToWaterSurface() : null);
	}

	public void setDistanceToWaterSurface(Integer distanceToWaterSurface) {
		getGatheringEvent(true).setDistanceToWaterSurface(
				distanceToWaterSurface);
	}

	// exact location
	@Transient
	public Point getExactLocation() {
		return (hasGatheringEvent() ? getGatheringEvent(true)
				.getExactLocation() : null);
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
		LanguageString langString = LanguageString.NewInstance(locality,
				language);
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
		getFieldObservation(true).setGatheringEvent(gatheringEvent);
	}

	public boolean hasGatheringEvent() {
		return (getGatheringEvent(false) != null);
	}

	public GatheringEvent innerGatheringEvent() {
		return getGatheringEvent(false);
	}

	public GatheringEvent getGatheringEvent(boolean createIfNotExists) {
		if (!hasFieldObservation() && !createIfNotExists) {
			return null;
		}
		if (createIfNotExists
				&& getFieldObservation(true).getGatheringEvent() == null) {
			GatheringEvent gatheringEvent = GatheringEvent.NewInstance();
			getFieldObservation(true).setGatheringEvent(gatheringEvent);
		}
		return getFieldObservation(true).getGatheringEvent();
	}

	// ****************** Field Object ************************************/

	/**
	 * Returns true if a field observation exists (even if all attributes are
	 * empty or <code>null<code>.
	 * 
	 * @return
	 */
	public boolean hasFieldObject() {
		return this.fieldObservation != null;
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
				return new HashMap<Language, LanguageString>();
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
		if (ecology == null) {
			try {
				ecology = initializeFieldObjectTextDataWithSupportTest(
						Feature.ECOLOGY(), true, false);
			} catch (DerivedUnitFacadeNotSupportedException e) {
				throw new IllegalStateException(notSupportMessage, e);
			}
		}
		if (ecologyText == null) {
			ecology.removeText(language);
		} else {
			ecology.putText(language, ecologyText);
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

	public void removeEcologyAll() {

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
				return new HashMap<Language, LanguageString>();
			}
		}
		return plantDescription.getMultilanguageText();
	}

	public void setPlantDescription(String plantDescription) {
		setPlantDescription(plantDescription, null);
	}

	public void setPlantDescription(String plantDescriptionText,
			Language language) {
		if (language == null) {
			language = Language.DEFAULT();
		}
		if (plantDescription == null) {
			try {
				plantDescription = initializeFieldObjectTextDataWithSupportTest(
						Feature.DESCRIPTION(), true, false);
			} catch (DerivedUnitFacadeNotSupportedException e) {
				throw new IllegalStateException(notSupportMessage, e);
			}
		}
		if (plantDescriptionText == null) {
			plantDescription.removeText(language);
		} else {
			plantDescription.putText(language, plantDescriptionText);
		}
	}

	public void removePlantDescription(Language language) {
		setPlantDescription(null, language);
	}

	// field object definition
	public void addFieldObjectDefinition(String text, Language language) {
		getFieldObservation(true).addDefinition(text, language);
	}

	@Transient
	public Map<Language, LanguageString> getFieldObjectDefinition() {
		if (!hasFieldObservation()) {
			return new HashMap<Language, LanguageString>();
		} else {
			return getFieldObservation(true).getDefinition();
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
		if (hasFieldObservation()) {
			getFieldObservation(true).removeDefinition(lang);
		}
	}

	// media
	public boolean addFieldObjectMedia(Media media) {
		try {
			return addMedia(media, getFieldObservation(true));
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
			return (getImageGallery(fieldObservation, false) != null);
		}
	}

	public void setFieldObjectImageGallery(SpecimenDescription imageGallery)
			throws DerivedUnitFacadeNotSupportedException {
		SpecimenDescription existingGallery = getFieldObjectImageGallery(false);

		// test attached specimens contain this.derivedUnit
		SpecimenOrObservationBase<?> facadeFieldObservation = innerFieldObservation();
		testSpecimenInImageGallery(imageGallery, facadeFieldObservation);

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

	// field number
	@Transient
	public String getFieldNumber() {
		if (!hasFieldObservation()) {
			return null;
		} else {
			return getFieldObservation(true).getFieldNumber();
		}
	}

	public void setFieldNumber(String fieldNumber) {
		getFieldObservation(true).setFieldNumber(fieldNumber);
	}

	// primary collector
	@Transient
	public Person getPrimaryCollector() {
		if (!hasFieldObservation()) {
			return null;
		} else {
			return getFieldObservation(true).getPrimaryCollector();
		}
	}

	public void setPrimaryCollector(Person primaryCollector) {
		getFieldObservation(true).setPrimaryCollector(primaryCollector);
	}

	// field notes
	@Transient
	public String getFieldNotes() {
		if (!hasFieldObservation()) {
			return null;
		} else {
			return getFieldObservation(true).getFieldNotes();
		}
	}

	public void setFieldNotes(String fieldNotes) {
		getFieldObservation(true).setFieldNotes(fieldNotes);
	}

	// individual counts
	@Transient
	public Integer getIndividualCount() {
		return (hasFieldObservation() ? getFieldObservation(true)
				.getIndividualCount() : null);
	}

	public void setIndividualCount(Integer individualCount) {
		getFieldObservation(true).setIndividualCount(individualCount);
	}

	// life stage
	@Transient
	public Stage getLifeStage() {
		return (hasFieldObservation() ? getFieldObservation(true)
				.getLifeStage() : null);
	}

	public void setLifeStage(Stage lifeStage) {
		getFieldObservation(true).setLifeStage(lifeStage);
	}

	// sex
	@Transient
	public Sex getSex() {
		return (hasFieldObservation() ? getFieldObservation(true).getSex()
				: null);
	}

	public void setSex(Sex sex) {
		getFieldObservation(true).setSex(sex);
	}

	// field observation
	public boolean hasFieldObservation() {
		return (getFieldObservation(false) != null);
	}

	/**
	 * Returns the field observation as an object.
	 * 
	 * @return
	 */
	public FieldObservation innerFieldObservation() {
		return getFieldObservation(false);
	}

	/**
	 * Returns the field observation as an object.
	 * 
	 * @return
	 */
	public FieldObservation getFieldObservation(boolean createIfNotExists) {
		if (fieldObservation == null && createIfNotExists) {
			setFieldObservation(FieldObservation.NewInstance());
		}
		return this.fieldObservation;
	}
	

	private void setFieldObservation(FieldObservation fieldObservation) {
		this.fieldObservation = fieldObservation;
		if (fieldObservation != null){
			if (config.isFirePropertyChangeEvents()){
				addNewEventPropagationListener(fieldObservation);
			}
			if (derivedUnit != null){
				DerivationEvent derivationEvent = getDerivationEvent(CREATE);
				derivationEvent.addOriginal(fieldObservation);
			}
			setFieldObservationCacheStrategy();
		}
	}

	// ****************** Specimen *******************************************

	// Definition
	public void addDerivedUnitDefinition(String text, Language language) {
		innerDerivedUnit().putDefinition(language, text);
	}

	@Transient
	public Map<Language, LanguageString> getDerivedUnitDefinitions() {
		testDerivedUnit();
		return this.derivedUnit.getDefinition();
	}


	public String getDerivedUnitDefinition(Language language) {
		testDerivedUnit();
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
		testDerivedUnit();
		determination.setIdentifiedUnit(derivedUnit);
		derivedUnit.addDetermination(determination);
	}

	@Transient
	public DeterminationEvent getPreferredDetermination() {
		testDerivedUnit();
		Set<DeterminationEvent> events = derivedUnit.getDeterminations();
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
		testDerivedUnit();
		Set<DeterminationEvent> events = derivedUnit.getDeterminations();
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
		testDerivedUnit();
		Set<DeterminationEvent> events = derivedUnit.getDeterminations();
		Set<DeterminationEvent> result = new HashSet<DeterminationEvent>();
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
		testDerivedUnit();
		return derivedUnit.getDeterminations();
	}

	public void removeDetermination(DeterminationEvent determination) {
		testDerivedUnit();
		derivedUnit.removeDetermination(determination);
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
		testDerivedUnit();
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
		Set<SpecimenOrObservationBase> imageGallerySpecimens = imageGallery.getDescribedSpecimenOrObservations();
		if (imageGallerySpecimens.size() < 1) {
			throw new DerivedUnitFacadeNotSupportedException(
					"Image Gallery has no Specimen attached. Please attache according specimen or field observation.");
		}
		if (!imageGallerySpecimens.contains(specimen)) {
			throw new DerivedUnitFacadeNotSupportedException(
					"Image Gallery has not the facade's field object attached. Please add field object first to image gallery specimenOrObservation list.");
		}
	}

	/**
	 * Returns the media for the specimen.<BR>
	 * 
	 * @return
	 */
	@Transient
	public List<Media> getDerivedUnitMedia() {
		testDerivedUnit();
		try {
			List<Media> result = getMedia(derivedUnit, false);
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
		testDerivedUnit();
		return derivedUnit.getAccessionNumber();
	}

	public void setAccessionNumber(String accessionNumber) {
		testDerivedUnit();
		derivedUnit.setAccessionNumber(accessionNumber);
	}

	@Transient
	public String getCatalogNumber() {
		testDerivedUnit();
		return derivedUnit.getCatalogNumber();
	}

	public void setCatalogNumber(String catalogNumber) {
		testDerivedUnit();
		derivedUnit.setCatalogNumber(catalogNumber);
	}

	@Transient
	public String getBarcode() {
		testDerivedUnit();
		return derivedUnit.getBarcode();
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
		testDerivedUnit();
		if (derivedUnit.isInstanceOf(Specimen.class)) {
			return CdmBase.deproxy(derivedUnit, Specimen.class)
					.getPreservation();
		} else {
			if (this.config
					.isThrowExceptionForNonSpecimenPreservationMethodRequest()) {
				throw new MethodNotSupportedByDerivedUnitTypeException(
						"A preservation method is only available in derived units of type 'Specimen' or 'Fossil'");
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
		testDerivedUnit();
		if (derivedUnit.isInstanceOf(Specimen.class)) {
			CdmBase.deproxy(derivedUnit, Specimen.class).setPreservation(
					preservation);
		} else {
			if (this.config
					.isThrowExceptionForNonSpecimenPreservationMethodRequest()) {
				throw new MethodNotSupportedByDerivedUnitTypeException(
						"A preservation method is only available in derived units of type 'Specimen' or 'Fossil'");
			} else {
				return;
			}
		}
	}

	// Stored under name
	@Transient
	public TaxonNameBase getStoredUnder() {
		testDerivedUnit();
		return derivedUnit.getStoredUnder();
	}

	public void setStoredUnder(TaxonNameBase storedUnder) {
		testDerivedUnit();
		derivedUnit.setStoredUnder(storedUnder);
	}

	// title cache
	public String getTitleCache() {
		SpecimenOrObservationBase<?> titledUnit = getTitledUnit();
		
		if (!titledUnit.isProtectedTitleCache()) {
			// always compute title cache anew as long as there are no property
			// change listeners on
			// field observation, gathering event etc
			titledUnit.setTitleCache(null, false);
		}
		return titledUnit.getTitleCache();
	}
	
	private SpecimenOrObservationBase<?> getTitledUnit(){
		return (derivedUnit != null )? derivedUnit : fieldObservation;
	}

	public boolean isProtectedTitleCache() {
		return getTitledUnit().isProtectedTitleCache();
	}

	public void setTitleCache(String titleCache, boolean isProtected) {
		this.getTitledUnit().setTitleCache(titleCache, isProtected);
	}

	/**
	 * Returns the derived unit itself.
	 * 
	 * @return the derived unit
	 */
	public DerivedUnitBase innerDerivedUnit() {
		return this.derivedUnit;
	}
	
//	/**
//	 * Returns the derived unit itself.
//	 * 
//	 * @return the derived unit
//	 */
//	public DerivedUnitBase innerDerivedUnit(boolean createIfNotExists) {
//		DerivedUnit result = this.derivedUnit; 
//		if (result == null && createIfNotExists){
//			if (this.fieldObservation == null){
//				String message = "Field observation must exist to create derived unit.";
//				throw new IllegalStateException(message);
//			}else{
//				DerivedUnit = 
//				DerivationEvent derivationEvent = getDerivationEvent(true);
//				derivationEvent.addOriginal(fieldObservation);
//				return this.derivedUnit;
//			}
//		}
//	}

	private boolean hasDerivationEvent() {
		return getDerivationEvent() == null ? false : true;
	}

	private DerivationEvent getDerivationEvent() {
		return getDerivationEvent(false);
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
			result = DerivationEvent.NewInstance();
			derivedUnit.setDerivedFrom(result);
		}
		return result;
	}

	@Transient
	public String getExsiccatum()
			throws MethodNotSupportedByDerivedUnitTypeException {
		testDerivedUnit();
		if (derivedUnit.isInstanceOf(Specimen.class)) {
			return CdmBase.deproxy(derivedUnit, Specimen.class).getExsiccatum();
		} else {
			if (this.config
					.isThrowExceptionForNonSpecimenPreservationMethodRequest()) {
				throw new MethodNotSupportedByDerivedUnitTypeException(
						"An exsiccatum is only available in derived units of type 'Specimen' or 'Fossil'");
			} else {
				return null;
			}
		}
	}

	public void setExsiccatum(String exsiccatum) throws Exception {
		testDerivedUnit();
		if (derivedUnit.isInstanceOf(Specimen.class)) {
			CdmBase.deproxy(derivedUnit, Specimen.class).setExsiccatum(
					exsiccatum);
		} else {
			if (this.config
					.isThrowExceptionForNonSpecimenPreservationMethodRequest()) {
				throw new MethodNotSupportedByDerivedUnitTypeException(
						"An exsiccatum is only available in derived units of type 'Specimen' or 'Fossil'");
			} else {
				return;
			}
		}
	}

	// **** sources **/
	public void addSource(IdentifiableSource source) {
		testDerivedUnit();
		this.derivedUnit.addSource(source);
	}

	/**
	 * Creates an orignal source, adds it to the specimen and returns it.
	 * 
	 * @param reference
	 * @param microReference
	 * @param originalNameString
	 * @return
	 */
	public IdentifiableSource addSource(Reference reference, String microReference, String originalNameString) {
		IdentifiableSource source = IdentifiableSource.NewInstance(reference, microReference);
		source.setOriginalNameString(originalNameString);
		addSource(source);
		return source;
	}

	@Transient
	public Set<IdentifiableSource> getSources() {
		testDerivedUnit();
		return derivedUnit.getSources();
	}

	public void removeSource(IdentifiableSource source) {
		testDerivedUnit();
		this.derivedUnit.removeSource(source);
	}

	/**
	 * @return the collection
	 */
	@Transient
	public Collection getCollection() {
		testDerivedUnit();
		return derivedUnit.getCollection();
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
		testDerivedUnit();
		this.derivedUnit.addAnnotation(annotation);
	}

	@Transient
	public void getAnnotations() {
		testDerivedUnit();
		this.derivedUnit.getAnnotations();
	}

	public void removeAnnotation(Annotation annotation) {
		testDerivedUnit();
		this.derivedUnit.removeAnnotation(annotation);
	}

	// ******************************* Events ***************************
	
	//set of events that were currently fired by this facades field observation
	//to avoid recursive fireing of the same event
	private Set<PropertyChangeEvent> fireingEvents = new HashSet<PropertyChangeEvent>();
	
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
					if (! event.getSource().equals(fieldObservation) && ! fireingEvents.contains(event)  ){
						fireingEvents.add(event);
						fieldObservation.firePropertyChange(event);
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
	 * in DerivedUnitBase and Specimen. Data from SpecimenOrObservationBase and
	 * above are not yet shared at the moment.
	 * 
	 * @param collection
	 * @param catalogNumber
	 * @param accessionNumber
	 * @param collectorsNumber
	 * @param storedUnder
	 * @param preservation
	 * @return
	 */
	public Specimen addDuplicate(Collection collection, String catalogNumber,
			String accessionNumber,
			TaxonNameBase storedUnder, PreservationMethod preservation) {
		testDerivedUnit();
		Specimen duplicate = Specimen.NewInstance();
		duplicate.setDerivedFrom(getDerivationEvent(CREATE));
		duplicate.setCollection(collection);
		duplicate.setCatalogNumber(catalogNumber);
		duplicate.setAccessionNumber(accessionNumber);
		duplicate.setStoredUnder(storedUnder);
		duplicate.setPreservation(preservation);
		return duplicate;
	}

	public void addDuplicate(DerivedUnitBase duplicateSpecimen) {
		// TODO check derivedUnitType
		testDerivedUnit();
		getDerivationEvent(CREATE).addDerivative(duplicateSpecimen);
	}

	@Transient
	public Set<Specimen> getDuplicates() {
		testDerivedUnit();
		Set<Specimen> result = new HashSet<Specimen>();
		if (hasDerivationEvent()) {
			for (DerivedUnitBase derivedUnit : getDerivationEvent(CREATE)
					.getDerivatives()) {
				if (derivedUnit.isInstanceOf(Specimen.class)
						&& !derivedUnit.equals(this.derivedUnit)) {
					result.add(CdmBase.deproxy(derivedUnit, Specimen.class));
				}
			}
		}
		return result;
	}

	public void removeDuplicate(Specimen duplicateSpecimen) {
		testDerivedUnit();
		if (hasDerivationEvent()) {
			getDerivationEvent(CREATE).removeDerivative(duplicateSpecimen);
		}
	}
	
	

	private void testDerivedUnit() {
		if (derivedUnit == null){
			throw new IllegalStateException("This method is not allowed for this specimen or observation type. Probably you have tried to add specimen(derived unit) information to a field observation");
		}
	}

	public void setType(DerivedUnitType type) {
		this.type = type;
	}

	public DerivedUnitType getType() {
		return type;
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
}
