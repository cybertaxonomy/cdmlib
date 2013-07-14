/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.markup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeCacheStrategy;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.SpecimenTypeParser;
import eu.etaxonomy.cdm.strategy.parser.SpecimenTypeParser.TypeInfo;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.mueller
 * @created 30.05.2012
 * 
 */
public class MarkupSpecimenImport extends MarkupImportBase  {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MarkupSpecimenImport.class);
	
	private static final String ALTERNATIVE_COLLECTION_TYPE_STATUS = "alternativeCollectionTypeStatus";
	private static final String ALTERNATIVE_COLLECTOR = "alternativeCollector";
	private static final String ALTERNATIVE_FIELD_NUM = "alternativeFieldNum";
	private static final String COLLECTOR = "collector";
	private static final String COLLECTION = "collection";
	private static final String COLLECTION_AND_TYPE = "collectionAndType";
	private static final String COLLECTION_TYPE_STATUS = "collectionTypeStatus";
	private static final String DAY = "day";
	private static final String DESTROYED = "destroyed";
	private static final String FIELD_NUM = "fieldNum";
	private static final String FULL_TYPE = "fullType";
	private static final String FULL_DATE = "fullDate";
	private static final String LOCALITY = "locality";
	private static final String LOST = "lost";
	private static final String MONTH = "month";
	private static final String SUB_GATHERING = "subGathering";
	private static final String NOT_FOUND = "notFound";
	private static final String NOT_SEEN = "notSeen";
	private static final String ORIGINAL_DETERMINATION = "originalDetermination";

	private static final String UNKNOWN = "unknown";
	private static final String YEAR = "year";



	public MarkupSpecimenImport(MarkupDocumentImport docImport) {
		super(docImport);
	}
	

	public void handleSpecimenType(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent,
				HomotypicalGroup homotypicalGroup) throws XMLStreamException {
	
		// attributes
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		String typeStatus = getAndRemoveAttributeValue(attributes, TYPE_STATUS);
		String notSeen = getAndRemoveAttributeValue(attributes, NOT_SEEN);
		String unknown = getAndRemoveAttributeValue(attributes, UNKNOWN);
		String notFound = getAndRemoveAttributeValue(attributes, NOT_FOUND);
		String destroyed = getAndRemoveAttributeValue(attributes, DESTROYED);
		String lost = getAndRemoveAttributeValue(attributes, LOST);
		checkNoAttributes(attributes, parentEvent);
		if (StringUtils.isNotEmpty(typeStatus)) {
			// TODO
			// currently not needed
		} else if (StringUtils.isNotEmpty(notSeen)) {
			handleNotYetImplementedAttribute(attributes, NOT_SEEN);
		} else if (StringUtils.isNotEmpty(unknown)) {
			handleNotYetImplementedAttribute(attributes, UNKNOWN);
		} else if (StringUtils.isNotEmpty(notFound)) {
			handleNotYetImplementedAttribute(attributes, NOT_FOUND);
		} else if (StringUtils.isNotEmpty(destroyed)) {
			handleNotYetImplementedAttribute(attributes, DESTROYED);
		} else if (StringUtils.isNotEmpty(lost)) {
			handleNotYetImplementedAttribute(attributes, LOST);
		}

		NonViralName<?> firstName = null;
		Set<TaxonNameBase> names = homotypicalGroup.getTypifiedNames();
		if (names.isEmpty()) {
			String message = "There is no name in a homotypical group. Can't create the specimen type";
			fireWarningEvent(message, parentEvent, 8);
		} else {
			firstName = CdmBase.deproxy(names.iterator().next(),NonViralName.class);
		}

		DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
		String text = "";
		String collectionAndType = "";
		// elements
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				makeSpecimenType(state, facade, text, collectionAndType, firstName, parentEvent);
				return;
			} else if (isStartingElement(next, FULL_TYPE)) {
				handleNotYetImplementedElement(next);
				// homotypicalGroup = handleNom(state, reader, next, taxon,
				// homotypicalGroup);
			} else if (isStartingElement(next, TYPE_STATUS)) {
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, GATHERING)) {
				handleGathering(state, reader, next, facade);
			} else if (isStartingElement(next, ORIGINAL_DETERMINATION)) {
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, SPECIMEN_TYPE)) {
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, COLLECTION_AND_TYPE)) {
				collectionAndType += getCData(state, reader, next, true);
			} else if (isStartingElement(next, CITATION)) {
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, NOTES)) {
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, ANNOTATION)) {
				handleNotYetImplementedElement(next);
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();
			} else {
				handleUnexpectedElement(next);
			}
		}
		// TODO handle missing end element
		throw new IllegalStateException("Specimen type has no closing tag"); 
	}

	

	private void makeSpecimenType(MarkupImportState state, DerivedUnitFacade facade, String text, String collectionAndType, 
			NonViralName<?> name, XMLEvent parentEvent) {
		text = text.trim();
		if (isPunctuation(text)){
			return;
		}else{
			String message = "Text '%s' not handled for <SpecimenType>";
			this.fireWarningEvent(String.format(message, text), parentEvent, 4);
		}
		
		// remove brackets
		if (collectionAndType.matches("^\\(.*\\)\\.?$")) {
			collectionAndType = collectionAndType.replaceAll("\\.", "");
			collectionAndType = collectionAndType.substring(1, collectionAndType.length() - 1);
		}
		
		String[] split = collectionAndType.split("[;,]");
		for (String str : split) {
			str = str.trim();
			boolean addToAllNamesInGroup = true;
			TypeInfo typeInfo = makeSpecimenTypeTypeInfo(str, parentEvent);
			SpecimenTypeDesignationStatus typeStatus = typeInfo.status;
			Collection collection = createCollection(typeInfo.collectionString);

			// TODO improve cache strategy handling
			DerivedUnit typeSpecimen = facade.addDuplicate(collection, null, null, null, null);
			typeSpecimen.setCacheStrategy(new DerivedUnitFacadeCacheStrategy());
			name.addSpecimenTypeDesignation(typeSpecimen, typeStatus, null, null, null, false, addToAllNamesInGroup);
		}
	}
	

	private Collection createCollection(String code) {
		// TODO deduplicate
		// TODO code <-> name
		Collection result = Collection.NewInstance();
		result.setCode(code);
		return result;
	}
	

	private TypeInfo makeSpecimenTypeTypeInfo(String originalString, XMLEvent event) {
		TypeInfo result = new TypeInfo();
		String[] split = originalString.split("\\s+");
		for (String str : split) {
			if (str.matches(SpecimenTypeParser.typeTypePattern)) {
				SpecimenTypeDesignationStatus status;
				try {
					status = SpecimenTypeParser.parseSpecimenTypeStatus(str);
				} catch (UnknownCdmTypeException e) {
					String message = "Specimen type status '%s' not recognized by parser";
					fireWarningEvent(String.format(message, str), event, 4);
					status = null;
				}
				result.status = status;
			} else if (str.matches(SpecimenTypeParser.collectionPattern)) {
				result.collectionString = str;
			} else {
				String message = "Type part '%s' could not be recognized";
				fireWarningEvent(String.format(message, str), event, 2);
			}
		}

		return result;
	}
	
	
	private void handleGathering(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent , DerivedUnitFacade facade) throws XMLStreamException {
		checkNoAttributes(parentEvent);
		boolean hasCollector = false;
		boolean hasFieldNum = false;

		// elements
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				if (! hasCollector){
					if (state.getCurrentCollector() == null){
						checkMandatoryElement(hasCollector,parentEvent.asStartElement(), COLLECTOR);
					}else{
						facade.setCollector(state.getCurrentCollector());
					}
				}
				checkMandatoryElement(hasFieldNum,parentEvent.asStartElement(), FIELD_NUM);
				return;
			}else if (isStartingElement(next, COLLECTOR)) {
				hasCollector = true;
				String collectorStr = getCData(state, reader, next);
				TeamOrPersonBase<?> collector = createCollector(collectorStr);
				facade.setCollector(collector);
				state.setCurrentCollector(collector);
			} else if (isStartingElement(next, ALTERNATIVE_COLLECTOR)) {
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, FIELD_NUM)) {
				hasFieldNum = true;
				String fieldNumStr = getCData(state, reader, next);
				facade.setFieldNumber(fieldNumStr);
			} else if (isStartingElement(next, ALTERNATIVE_FIELD_NUM)) {
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, COLLECTION_TYPE_STATUS)) {
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, COLLECTION_AND_TYPE)) {  //does this make sense here?
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, ALTERNATIVE_COLLECTION_TYPE_STATUS)) {
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, SUB_GATHERING)) {
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, COLLECTION)) {
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, LOCALITY)) {
				handleLocality(state, reader, next, facade);
			} else if (isStartingElement(next, DATES)) {
				TimePeriod timePeriod = handleDates(state, reader, next);
				facade.setGatheringPeriod(timePeriod);
			} else if (isStartingElement(next, NOTES)) {
				handleNotYetImplementedElement(next);
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("Collection has no closing tag.");

	}
	

	private TimePeriod handleDates(MarkupImportState state, XMLEventReader reader, XMLEvent parent) throws XMLStreamException {
		checkNoAttributes(parent);
		TimePeriod result = TimePeriod.NewInstance();
		String parseMessage = "%s can not be parsed: %s";
		boolean hasFullDate = false;
		boolean hasAtomised = false;
		boolean hasUnparsedAtomised = false;
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parent)) {
				if (! isAlternative(hasFullDate, hasAtomised, hasUnparsedAtomised)){
					String message = "Some problems exist when defining the date";
					fireWarningEvent(message, parent, 4);
				}
				return result;
			} else if (isStartingElement(next, FULL_DATE)) {
				String fullDate = getCData(state, reader, next, true);
				result = TimePeriodParser.parseString(fullDate);
				if (result.getFreeText() != null){
					fireWarningEvent(String.format(parseMessage, FULL_DATE, fullDate), parent, 1);
				}
				hasFullDate = true;
			} else if (isStartingElement(next, DAY)) {
				String day = getCData(state, reader, next, true).trim();
				day = normalizeDate(day);
				if (CdmUtils.isNumeric(day)){
					result.setStartDay(Integer.valueOf(day));
					hasAtomised = true;
				}else{
					fireWarningEvent(String.format(parseMessage,"Day", day), parent, 2);
					hasUnparsedAtomised = true;
				}
			} else if (isStartingElement(next, MONTH)) {
				String month = getCData(state, reader, next, true).trim();
				month = normalizeDate(month);
				if (CdmUtils.isNumeric(month)){
					result.setStartMonth(Integer.valueOf(month));
					hasAtomised = true;
				}else{
					fireWarningEvent(String.format(parseMessage,"Month", month), parent, 2);
					hasUnparsedAtomised = true;
				}
			} else if (isStartingElement(next, YEAR)) {
				String year = getCData(state, reader, next, true).trim();
				year = normalizeDate(year);
				if (CdmUtils.isNumeric(year)){
					result.setStartYear(Integer.valueOf(year));
					hasAtomised = true;
				}else{
					fireWarningEvent(String.format(parseMessage,"Year", year), parent, 2);
					hasUnparsedAtomised = true;
				}
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("Dates has no closing tag.");
	}


	private String normalizeDate(String partOfDate) {
		if (isBlank(partOfDate)){
			return null;
		}
		partOfDate = partOfDate.trim();
		while (partOfDate.startsWith("-")){
			partOfDate = partOfDate.substring(1);
		}
		return partOfDate;
	}


	private boolean isAlternative(boolean first, boolean second, boolean third) {
		return ( (first ^ second) && !third)  || 
				(! first && ! second && third) ;
	}


	private void handleLocality(MarkupImportState state, XMLEventReader reader,XMLEvent parentEvent, DerivedUnitFacade facade)throws XMLStreamException {
		String classValue = getClassOnlyAttribute(parentEvent);
		boolean isLocality = false;
		NamedAreaLevel areaLevel = null;
		if ("locality".equalsIgnoreCase(classValue)) {
			isLocality = true;
		} else {
			areaLevel = makeNamedAreaLevel(state, classValue, parentEvent);
		}

		String text = "";
		// elements
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				if (StringUtils.isNotBlank(text)) {
					text = normalize(text);
					if (isLocality) {
						facade.setLocality(text, getDefaultLanguage(state));
					} else {
						text = CdmUtils.removeTrailingDot(text);
						NamedArea area = makeArea(state, text, areaLevel);
						facade.addCollectingArea(area);
					}
				}
				// TODO
				return;
			}else if (isStartingElement(next, ALTITUDE)) {
				handleNotYetImplementedElement(next);
				// homotypicalGroup = handleNom(state, reader, next, taxon,
				// homotypicalGroup);
			} else if (isStartingElement(next, COORDINATES)) {
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, ANNOTATION)) {
				handleNotYetImplementedElement(next);
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("<SpecimenType> has no closing tag"); 
	}



	private TeamOrPersonBase<?> createCollector(String collectorStr) {
		return createAuthor(collectorStr);
	}

	
	public List<DescriptionElementBase> handleMaterialsExamined(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, Feature feature) throws XMLStreamException {
		List<DescriptionElementBase> result = new ArrayList<DescriptionElementBase>();
		//reset current areas
		state.removeCurrentAreas();
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				if (result.isEmpty()){
					fireWarningEvent("Materials examined created empty Individual Associations list", parentEvent, 4);
				}
				state.removeCurrentAreas();
				return result;
			} else if (isStartingElement(next, SUB_HEADING)) {
//				Map<String, Object> inlineMarkup = new HashMap<String, Object>();
				String text = getCData(state, reader, next, true);
				if (isFeatureHeading(state, next, text)){
					feature = makeHeadingFeature(state, next, text, feature);
				}else{
					String message = "Unhandled subheading: %s";
					fireWarningEvent(String.format(message,  text), next, 4);
				}
//				for (String key : inlineMarkup.keySet()){
//					handleInlineMarkup(state, key, inlineMarkup);
//				}
				
			} else if (isStartingElement(next, BR) || isEndingElement(next, BR)) {
				//do nothing
			} else if (isStartingElement(next, GATHERING)) {
				DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(SpecimenOrObservationType.DerivedUnit);
				addCurrentAreas(state, next, facade);
				handleGathering(state, reader, next, facade);
				SpecimenOrObservationBase<?> specimen;
				if (facade.innerDerivedUnit() != null){
					specimen = facade.innerDerivedUnit();
				}else{
					specimen = facade.innerFieldUnit();
				}
				IndividualsAssociation individualsAssociation = IndividualsAssociation.NewInstance();
				individualsAssociation.setAssociatedSpecimenOrObservation(specimen);
				result.add(individualsAssociation);
			}else if (next.isCharacters()) {
				String text = next.asCharacters().getData().trim();
				if (isPunctuation(text)){
					//do nothing
				}else{
					String message = "Unrecognized text: %s";
					fireWarningEvent(String.format(message, text), next, 6);
				}
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("<String> has no closing tag");
		
	}

	

private void addCurrentAreas(MarkupImportState state, XMLEvent event, DerivedUnitFacade facade) {
		for (NamedArea area : state.getCurrentAreas()){
			if (area == null){
				continue;
			}else if (area.isInstanceOf(WaterbodyOrCountry.class)){
				facade.setCountry(area);
			}else{
				String message = "Current area %s is not country. This is not expected for currently known data.";
				fireWarningEvent(String.format(message, area.getTitleCache()), event, 2);
				facade.addCollectingArea(area);
			}
		}
		
	}


//	private void handleInlineMarkup(MarkupImportState state, String key, Map<String, Object> inlineMarkup) {
//		Object obj = inlineMarkup.get(key);
//		if (key.equals(LOCALITY)){
//			if (obj instanceof NamedArea){
//				NamedArea area = (NamedArea)obj;
//				state.addCurrentArea(area);
//			}
//		}
//		
//	}


	/**
	 * Changes the feature if the (sub)-heading implies this. Also recognizes hidden country information
	 * @param state 
	 * @param parent
	 * @param text
	 * @param feature
	 * @return
	 */
	private Feature makeHeadingFeature(MarkupImportState state, XMLEvent parent, String originalText, Feature feature) {
		//expand, provide by config or service
		String materialRegEx = "Mat[\u00E9\u00C9]riel";
		String examinedRegEx = "[\u00E9\u00C9]tudi[\u00E9\u00C9]";
		String countryRegEx = "(gabonais)";
		String postfixCountryRegEx = "\\s+(pour le Gabon)";
		
		String materialExaminedRegEx = "(?i)" + materialRegEx + "\\s+(" + countryRegEx +"\\s+)?" + examinedRegEx + "(" +postfixCountryRegEx + ")?:?";
		
		String text = originalText;
		
		if (isBlank(text)){
			return feature;
		}else{
			if (text.matches(materialExaminedRegEx)){
				//gabon specific
				if (text.contains("gabonais ")){
					text = text.replace("gabonais ", "");
					state.addCurrentArea(WaterbodyOrCountry.GABONGABONESEREPUBLIC());
				}
				if (text.contains(" pour le Gabon")){
					text = text.replace(" pour le Gabon", "");
					state.addCurrentArea(WaterbodyOrCountry.GABONGABONESEREPUBLIC());
				}
				
				//update feature
				feature = Feature.MATERIALS_EXAMINED();
				state.putFeatureToGeneralSorterList(feature);
				return feature;
			}else{
				String message = "Heading/Subheading not recognized: %s";
				fireWarningEvent(String.format(message, originalText), parent, 4);
				return feature;
			}
		}
	}


	/**
	 * True if heading or subheading represents feature information
	 * @param state
	 * @param parent
	 * @param text
	 * @return
	 */
	private boolean isFeatureHeading(MarkupImportState state, XMLEvent parent, String text) {
		return makeHeadingFeature(state, parent, text, null) != null;
	}


	public String handleInLineGathering(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(SpecimenOrObservationType.FieldUnit);
		handleGathering(state, reader, parentEvent, facade);
		SpecimenOrObservationBase<?> specimen  = facade.innerFieldUnit();
		if (specimen == null){
			specimen = facade.innerDerivedUnit();
			String message = "Inline gaterhing has no field unit";
			fireWarningEvent(message, parentEvent, 2);
		}
		
		String result = "<cdm:specimen uuid='%s'>%s</specimen>";
		if (specimen != null){
			result = String.format(result, specimen.getUuid(), specimen.getTitleCache());
		}else{
			String message = "Inline gathering has no specimen";
			fireWarningEvent(message, parentEvent, 4);
		}
		save(specimen, state);
		return result;	
	}





}
