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
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade.DerivedUnitType;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeCacheStrategy;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.SpecimenTypeParser;
import eu.etaxonomy.cdm.strategy.parser.SpecimenTypeParser.TypeInfo;

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
	private static final String DESTROYED = "destroyed";
	private static final String FIELD_NUM = "fieldNum";
	private static final String FULL_TYPE = "fullType";
	private static final String LOCALITY = "locality";
	private static final String LOST = "lost";
	private static final String SUB_COLLECTION = "subCollection";
	private static final String NOT_FOUND = "notFound";
	private static final String NOT_SEEN = "notSeen";
	private static final String ORIGINAL_DETERMINATION = "originalDetermination";

	private static final String UNKNOWN = "unknown";


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

		DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(DerivedUnitType.Specimen);
		String text = "";
		// elements
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (next.isEndElement()) {
				if (isMyEndingElement(next, parentEvent)) {
					makeSpecimenType(state, facade, text, firstName, parentEvent);
					return;
				} else {
					if (isEndingElement(next, FULL_TYPE)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, TYPE_STATUS)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, ORIGINAL_DETERMINATION)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, SPECIMEN_TYPE)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, COLLECTION_AND_TYPE)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, CITATION)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, NOTES)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, ANNOTATION)) {
						// NOT YET IMPLEMENTED  //TODO test handleSimpleAnnotation
						popUnimplemented(next.asEndElement());
					} else {
						handleUnexpectedEndElement(next.asEndElement());
					}
				}
			} else if (next.isStartElement()) {
				if (isStartingElement(next, FULL_TYPE)) {
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
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, CITATION)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, NOTES)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, ANNOTATION)) {
					handleNotYetImplementedElement(next);
				} else {
					handleUnexpectedStartElement(next);
				}
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();
			} else {
				handleUnexpectedElement(next);
			}
		}
		// TODO handle missing end element
		throw new IllegalStateException("Specimen type has no closing tag"); 
	}

	

	private void makeSpecimenType(MarkupImportState state, DerivedUnitFacade facade, String text, 
			NonViralName name, XMLEvent parentEvent) {
		text = text.trim();
		// remove brackets
		if (text.matches("^\\(.*\\)\\.?$")) {
			text = text.replaceAll("\\.", "");
			text = text.substring(1, text.length() - 1);
		}
		String[] split = text.split("[;,]");
		for (String str : split) {
			str = str.trim();
			boolean addToAllNamesInGroup = true;
			TypeInfo typeInfo = makeSpecimenTypeTypeInfo(str, parentEvent);
			SpecimenTypeDesignationStatus typeStatus = typeInfo.status;
			Collection collection = createCollection(typeInfo.collectionString);

			// TODO improve cache strategy handling
			DerivedUnitBase typeSpecimen = facade.addDuplicate(collection,
					null, null, null, null);
			typeSpecimen.setCacheStrategy(new DerivedUnitFacadeCacheStrategy());
			name.addSpecimenTypeDesignation((Specimen) typeSpecimen, typeStatus, null, null, null, false, addToAllNamesInGroup);
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
					message = String.format(message, str);
					fireWarningEvent(message, event, 4);
					status = null;
				}
				result.status = status;
			} else if (str.matches(SpecimenTypeParser.collectionPattern)) {
				result.collectionString = str;
			} else {
				String message = "Type part '%s' could not be recognized";
				message = String.format(message, str);
				fireWarningEvent(message, event, 2);
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
			if (next.isEndElement()) {
				if (isMyEndingElement(next, parentEvent)) {
					checkMandatoryElement(hasCollector,parentEvent.asStartElement(), COLLECTOR);
					checkMandatoryElement(hasFieldNum,parentEvent.asStartElement(), FIELD_NUM);
					return;
				} else {
					if (isEndingElement(next, ALTERNATIVE_COLLECTOR)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, ALTERNATIVE_FIELD_NUM)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, COLLECTION_TYPE_STATUS)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, COLLECTION_AND_TYPE)) {
						// NOT YET IMPLEMENTED , does this make sense here? 
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next,
							ALTERNATIVE_COLLECTION_TYPE_STATUS)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, SUB_COLLECTION)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, COLLECTION)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, DATES)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, NOTES)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else {
						handleUnexpectedEndElement(next.asEndElement());
					}
				}
			} else if (next.isStartElement()) {
				if (isStartingElement(next, COLLECTOR)) {
					hasCollector = true;
					String collectorStr = getCData(state, reader, next);
					AgentBase<?> collector = createCollector(collectorStr);
					facade.setCollector(collector);
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
				} else if (isStartingElement(next, SUB_COLLECTION)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, COLLECTION)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, LOCALITY)) {
					handleLocality(state, reader, next, facade);
				} else if (isStartingElement(next, DATES)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, NOTES)) {
					handleNotYetImplementedElement(next);
				} else {
					handleUnexpectedStartElement(next);
				}
			} else {
				handleUnexpectedElement(next);
			}
		}
		// TODO handle missing end element
		throw new IllegalStateException("Collection has no closing tag");

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
			if (next.isEndElement()) {
				if (isMyEndingElement(next, parentEvent)) {
					if (StringUtils.isNotBlank(text)) {
						text = normalize(text);
						if (isLocality) {
							facade.setLocality(text);
						} else {
							text = CdmUtils.removeTrailingDot(text);
							NamedArea area = makeArea(state, text, areaLevel);
							facade.addCollectingArea(area);
						}
					}
					// TODO
					return;
				} else {
					if (isEndingElement(next, ALTITUDE)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, COORDINATES)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, ANNOTATION)) {
						// NOT YET IMPLEMENTED  //TODO test handleSimpleAnnotation
						popUnimplemented(next.asEndElement());
					} else {
						handleUnexpectedEndElement(next.asEndElement());
					}
				}
			} else if (next.isStartElement()) {
				if (isStartingElement(next, ALTITUDE)) {
					handleNotYetImplementedElement(next);
					// homotypicalGroup = handleNom(state, reader, next, taxon,
					// homotypicalGroup);
				} else if (isStartingElement(next, COORDINATES)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, ANNOTATION)) {
					handleNotYetImplementedElement(next);
				} else {
					handleUnexpectedStartElement(next);
				}
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("<SpecimenType> has no closing tag"); 
	}



	private AgentBase<?> createCollector(String collectorStr) {
		return createAuthor(collectorStr);
	}

	
	public List<DescriptionElementBase> handleMaterialsExamined(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		List<DescriptionElementBase> result = new ArrayList<DescriptionElementBase>();
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				if (result.isEmpty()){
					fireWarningEvent("Materials examined created empty Individual Associations list", parentEvent, 4);
				}
				return result;
			} else if (isStartingElement(next, SUB_HEADING)) {
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, BR)) {
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, GATHERING)) {
				DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(DerivedUnitType.DerivedUnit.DerivedUnit);
				handleGathering(state, reader, next, facade);
				SpecimenOrObservationBase<?> specimen;
				if (facade.innerDerivedUnit() != null){
					specimen = facade.innerDerivedUnit();
				}else{
					specimen = facade.innerFieldObservation();
				}
				IndividualsAssociation individualsAssociation = IndividualsAssociation.NewInstance();
				individualsAssociation.setAssociatedSpecimenOrObservation(specimen);
				result.add(individualsAssociation);
			}else if (next.isCharacters()) {
				String text = next.asCharacters().getData();
				if (text.matches("\\.")){
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

	

	public String handleInLineGathering(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(DerivedUnitType.DerivedUnit.FieldObservation);
		handleGathering(state, reader, parentEvent, facade);
		FieldObservation fieldObservation = facade.innerFieldObservation();
		String result = "<cdm:specimen uuid='%s'>%s</specimen>";
		result = String.format(result, fieldObservation.getUuid(), fieldObservation.getTitleCache());
		save(fieldObservation, state);
		return result;	
	}





}
