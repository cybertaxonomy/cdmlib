/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.markup;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.ext.geo.GeoServiceArea;
import eu.etaxonomy.cdm.ext.geo.IEditGeoService;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.CdmImportBase.TermMatchMode;
import eu.etaxonomy.cdm.io.common.events.IIoEvent;
import eu.etaxonomy.cdm.io.common.events.IoProblemEvent;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author a.mueller
 * @created 04.08.2008
 */
public abstract class MarkupImportBase  {
	private static final Logger logger = Logger.getLogger(MarkupImportBase.class);

	//Base
	protected static final String ALTITUDE = "altitude";
	protected static final String ANNOTATION = "annotation";
	protected static final String BOLD = "bold";
	protected static final String BR = "br";
	protected static final String DOUBTFUL = "doubtful";
	protected static final String CITATION = "citation";
	protected static final String CLASS = "class";
	protected static final String COORDINATES = "coordinates";
	protected static final String DATES = "dates";
	protected static final String GATHERING = "gathering";
	protected static final String GATHERING_GROUP = "gatheringGroup";
	protected static final String GENUS_ABBREVIATION = "genus abbreviation";
	protected static final String FOOTNOTE = "footnote";
	protected static final String FOOTNOTE_REF = "footnoteRef";
	protected static final String FULL_NAME = "fullName";
	protected static final String ITALICS = "italics";
	protected static final String NUM = "num";
	protected static final String NOTES = "notes";
	protected static final String PUBLICATION = "publication";
	protected static final String SPECIMEN_TYPE = "specimenType";
	protected static final String STATUS = "status";
	protected static final String SUB_HEADING = "subHeading";
	protected static final String TYPE = "type";
	protected static final String TYPE_STATUS = "typeStatus";
	protected static final String UNKNOWN = "unknown";


	protected static final boolean CREATE_NEW = true;
	protected static final boolean NO_IMAGE_GALLERY = false;
	protected static final boolean IMAGE_GALLERY = true;

	protected static final String ADDENDA = "addenda";
	protected static final String BIBLIOGRAPHY = "bibliography";
	protected static final String BIOGRAPHIES = "biographies";
	protected static final String CHAR = "char";
	protected static final String DEDICATION = "dedication";
	protected static final String DEFAULT_MEDIA_URL = "defaultMediaUrl";
	protected static final String DISTRIBUTION_LIST = "distributionList";
	protected static final String DISTRIBUTION_LOCALITY = "distributionLocality";
	protected static final String FEATURE = "feature";
	protected static final String FIGURE = "figure";
	protected static final String FIGURE_LEGEND = "figureLegend";
	protected static final String FIGURE_PART = "figurePart";
	protected static final String FIGURE_REF = "figureRef";
	protected static final String FIGURE_TITLE = "figureTitle";
	protected static final String FOOTNOTE_STRING = "footnoteString";
	protected static final String FREQUENCY = "frequency";
	protected static final String HEADING = "heading";
	protected static final String HABITAT = "habitat";
	protected static final String HABITAT_LIST = "habitatList";
	protected static final String IS_FREETEXT = "isFreetext";
	protected static final String ID = "id";
	protected static final String KEY = "key";
	protected static final String LIFE_CYCLE_PERIODS = "lifeCyclePeriods";
	protected static final String META_DATA = "metaData";
	protected static final String MODS = "mods";

	protected static final String NOMENCLATURE = "nomenclature";
	protected static final String QUOTE = "quote";
	protected static final String RANK = "rank";
	protected static final String REF = "ref";
	protected static final String REF_NUM = "refNum";
	protected static final String REFERENCE = "reference";
	protected static final String REFERENCES = "references";
	protected static final String SUB_CHAR = "subChar";
	protected static final String TAXON = "taxon";
	protected static final String TAXONTITLE = "taxontitle";
	protected static final String TAXONTYPE = "taxontype";
	protected static final String TEXT_SECTION = "textSection";
	protected static final String TREATMENT = "treatment";
	protected static final String SERIALS_ABBREVIATIONS = "serialsAbbreviations";
	protected static final String STRING = "string";
	protected static final String URL = "url";
	protected static final String WRITER = "writer";

	protected static final String LOCALITY = "locality";



	//Nomenclature
	protected static final String ACCEPTED = "accepted";
	protected static final String ACCEPTED_NAME = "acceptedName";
	protected static final String ALTERNATEPUBTITLE = "alternatepubtitle";
	protected static final String AUTHOR = "author";
	protected static final String DETAILS = "details";
	protected static final String EDITION = "edition";
	protected static final String EDITORS = "editors";
	protected static final String HOMONYM = "homonym";
	protected static final String HOMOTYPES = "homotypes";
	protected static final String NOMENCLATURAL_NOTES = "nomenclaturalNotes";
	protected static final String INFRANK = "infrank";
	protected static final String INFRAUT = "infraut";
	protected static final String INFRPARAUT = "infrparaut";
	protected static final String ISSUE = "issue";
	protected static final String NAME = "name";
	protected static final String NAME_TYPE = "nameType";
	protected static final String NOM = "nom";
	protected static final String PAGES = "pages";
	protected static final String PARAUT = "paraut";
	protected static final String PUBFULLNAME = "pubfullname";
	protected static final String PUBLOCATION = "publocation";
	protected static final String PUBLISHER = "publisher";
	protected static final String PUBNAME = "pubname";
	protected static final String PUBTITLE = "pubtitle";
	protected static final String PUBTYPE = "pubtype";
	protected static final String REF_PART = "refPart";
	protected static final String SYNONYM = "synonym";
	protected static final String USAGE = "usage";
	protected static final String VOLUME = "volume";
	protected static final String YEAR = "year";


	//keys
	protected static final String COUPLET = "couplet";
	protected static final String IS_SPOTCHARACTERS = "isSpotcharacters";
	protected static final String ONLY_NUMBERED_TAXA_EXIST = "onlyNumberedTaxaExist";
	protected static final String EXISTS = "exists";
	protected static final String KEYNOTES = "keynotes";
	protected static final String KEY_TITLE = "keyTitle";
	protected static final String QUESTION = "question";
	protected static final String TEXT = "text";
	protected static final String TO_COUPLET = "toCouplet";
	protected static final String TO_KEY = "toKey";
	protected static final String TO_TAXON = "toTaxon";


	//Feature
	protected static final String VERNACULAR_NAMES = "vernacularNames";
	protected static final String VERNACULAR_NAME = "vernacularName";
	protected static final String TRANSLATION = "translation";
	protected static final String LOCAL_LANGUAGE = "localLanguage";



	protected MarkupDocumentImport docImport;

	private final IEditGeoService editGeoService;
	protected MarkupFeatureImport featureImport;

	public MarkupImportBase(MarkupDocumentImport docImport) {
		super();
		this.docImport = docImport;
		this.editGeoService = docImport.getEditGeoService();
	}

	private final Stack<QName> unhandledElements = new Stack<QName>();
	private final Stack<QName> handledElements = new Stack<QName>();


	protected <T extends CdmBase> void  save(Collection<T> collection, MarkupImportState state) {
		if (state.isCheck() || collection.isEmpty()){
			return;
		}
		T example = collection.iterator().next();
		if (example.isInstanceOf(TaxonBase.class)){
			Collection<TaxonBase> typedCollection = (Collection<TaxonBase>)collection;
			docImport.getTaxonService().saveOrUpdate(typedCollection);
		}else if (example.isInstanceOf(Classification.class)){
			Collection<Classification> typedCollection = (Collection<Classification>)collection;
			docImport.getClassificationService().saveOrUpdate(typedCollection);
		}else if (example.isInstanceOf(PolytomousKey.class)){
			Collection<PolytomousKey> typedCollection = (Collection<PolytomousKey>)collection;
			docImport.getPolytomousKeyService().saveOrUpdate(typedCollection);
		}else if (example.isInstanceOf(DefinedTermBase.class)){
			Collection<DefinedTermBase> typedCollection = (Collection<DefinedTermBase>)collection;
			getTermService().saveOrUpdate(typedCollection);
		}

	}


	//TODO move to service layer for all IdentifiableEntities
	protected void save(CdmBase cdmBase, MarkupImportState state) {
		if (state.isCheck()){
			return;
		}
		cdmBase = CdmBase.deproxy(cdmBase, CdmBase.class);
		if (cdmBase == null){
			String message = "Tried to save a null object.";
			fireWarningEvent(message, "--location ?? --", 6,1);
		} else if (cdmBase.isInstanceOf(TaxonBase.class)){
			docImport.getTaxonService().saveOrUpdate((TaxonBase<?>)cdmBase);
		}else if (cdmBase.isInstanceOf(Classification.class)){
			docImport.getClassificationService().saveOrUpdate((Classification)cdmBase);
		}else if (cdmBase.isInstanceOf(PolytomousKey.class)){
			docImport.getPolytomousKeyService().saveOrUpdate((PolytomousKey)cdmBase);
		}else if (cdmBase.isInstanceOf(DefinedTermBase.class)){
			docImport.getTermService().saveOrUpdate((DefinedTermBase<?>)cdmBase);
		}else if (cdmBase.isInstanceOf(Media.class)){
			docImport.getMediaService().saveOrUpdate((Media)cdmBase);
		}else if (cdmBase.isInstanceOf(SpecimenOrObservationBase.class)){
			docImport.getOccurrenceService().saveOrUpdate((SpecimenOrObservationBase<?>)cdmBase);
		}else if (cdmBase.isInstanceOf(DescriptionElementBase.class)){
			docImport.getDescriptionService().saveDescriptionElement((DescriptionElementBase)cdmBase);
		}else if (cdmBase.isInstanceOf(Reference.class)){
			docImport.getReferenceService().saveOrUpdate((Reference<?>)cdmBase);
		}else{
			String message = "Unknown cdmBase type to save: " + cdmBase.getClass();
			fireWarningEvent(message, "Unknown location", 8);
		}
		//logger.warn("Saved " +  cdmBase);
	}


	protected ITermService getTermService() {
		return docImport.getTermService();
	}

	protected IClassificationService getClassificationService() {
		return docImport.getClassificationService();
	}

//*********************** Attribute methods *************************************/

	/**
	 * Returns a map for all attributes of an start element
	 * @param event
	 * @return
	 */
	protected Map<String, Attribute> getAttributes(XMLEvent event) {
		Map<String, Attribute> result = new HashMap<String, Attribute>();
		if (!event.isStartElement()){
			fireWarningEvent("Event is not an startElement. Can't check attributes", makeLocationStr(event.getLocation()), 1, 1);
			return result;
		}
		StartElement element = event.asStartElement();
		Iterator<Attribute> attributes = element.getAttributes();
		while (attributes.hasNext()){
			Attribute attribute = attributes.next();
			//TODO namespaces
			result.put(attribute.getName().getLocalPart(), attribute);
		}
		return result;
	}

	/**
	 * Throws an unexpected attributes event if the event has any attributes.
	 * @param event
	 */
	protected void checkNoAttributes(Map<String, Attribute> attributes, XMLEvent event) {
		String[] exceptions = new String[]{};
		handleUnexpectedAttributes(event.getLocation(), attributes, 1, exceptions);
	}



	/**
	 * Throws an unexpected attributes event if the event has any attributes.
	 * @param event
	 */
	protected void checkNoAttributes(XMLEvent event) {
		String[] exceptions = new String[]{};
		checkNoAttributes(event, 1, exceptions);
	}

	/**
	 * Throws an unexpected attributes event if the event has any attributes except those mentioned in "exceptions".
	 * @param event
	 * @param exceptions
	 */
	protected void checkNoAttributes(XMLEvent event, int stackDepth, String... exceptions) {
		if (! event.isStartElement()){
			fireWarningEvent("Event is not an startElement. Can't check attributes", makeLocationStr(event.getLocation()), 1, 1);
			return;
		}
		StartElement startElement = event.asStartElement();
		Map<String, Attribute> attributes = getAttributes(startElement);
		handleUnexpectedAttributes(startElement.getLocation(), attributes, stackDepth+1, exceptions);
	}


	/**
	 * Checks if the given attribute exists and has the given value.
	 * If yes, true is returned and the attribute is removed from the attributes map.
	 * Otherwise false is returned.
	 * @param attributes
	 * @param attrName
	 * @param value
	 * @return <code>true</code> if attribute has given value, <code>false</code> otherwise
	 */
	protected boolean checkAndRemoveAttributeValue( Map<String, Attribute> attributes, String attrName, String value) {
		Attribute attr = attributes.get(attrName);
		if (attr == null ||value == null ){
			return false;
		}else{
			if (value.equals(attr.getValue())){
				attributes.remove(attrName);
				return true;
			}else{
				return false;
			}
		}
	}


	/**
	 * Returns the value of a given attribute name and removes the attribute from the attributes map.
	 * @param attributes
	 * @param attrName
	 * @return
	 */
	protected String getAndRemoveAttributeValue(Map<String, Attribute> attributes, String attrName) {
		return getAndRemoveAttributeValue(null, attributes, attrName, false, 1);
	}

	/**
	 * Returns the value of a boolean attribute with the given name and removes the attribute from the attributes map.
	 * Returns <code>defaultValue</code> if the attribute does not exist. ALso returns <code>defaultValue</code> and throws a warning if the
	 * attribute has no boolean value (true, false).
	 * @param
	 * @param attributes the
	 * @param attrName the name of the attribute
	 * @param defaultValue the default value to return if attribute does not exist or can not be defined
	 * @return
	 */
	protected Boolean getAndRemoveBooleanAttributeValue(XMLEvent event, Map<String, Attribute> attributes, String attrName, Boolean defaultValue) {
		String value = getAndRemoveAttributeValue(null, attributes, attrName, false, 1);
		Boolean result = defaultValue;
		if (value != null){
			if (value.equalsIgnoreCase("true")){
				result = true;
			}else if (value.equalsIgnoreCase("false")){
				result = false;
			}else{
				String message = "Boolean attribute has no boolean value ('true', 'false') but '%s'";
				fireWarningEvent(String.format(message, value), makeLocationStr(event.getLocation()), 6, 1);
			}
		}
		return result;
	}


	/**
	 * Returns the value of a given attribute name and returns the attribute from the attributes map.
	 * Fires a mandatory field is missing event if the attribute does not exist.
	 * @param xmlEvent
	 * @param attributes
	 * @param attrName
	 * @return
	 */
	protected String getAndRemoveRequiredAttributeValue(XMLEvent xmlEvent, Map<String, Attribute> attributes, String attrName) {
		return getAndRemoveAttributeValue(xmlEvent, attributes, attrName, true, 1);
	}

	/**
	 * Returns the value of a given attribute name and returns the attribute from the attributes map.
	 * If required is <code>true</code> and the attribute does not exist a mandatory field is missing event is fired.
	 * @param xmlEvent
	 * @param attributes
	 * @param attrName
	 * @param isRequired
	 * @return
	 */
	private String getAndRemoveAttributeValue(XMLEvent xmlEvent, Map<String, Attribute> attributes, String attrName, boolean isRequired, int stackDepth) {
		Attribute attr = attributes.get(attrName);
		if (attr == null ){
			if (isRequired){
				fireMandatoryElementIsMissing(xmlEvent, attrName, 8, stackDepth+1);
			}
			return null;
		}else{
			attributes.remove(attrName);
			return attr.getValue();
		}
	}

	/**
	 * Fires an not yet implemented event if the given attribute exists in attributes.
	 * @param attributes
	 * @param attrName
	 */
	protected void handleNotYetImplementedAttribute(Map<String, Attribute>  attributes, String attrName) {
		Attribute attr = attributes.get(attrName);
		if (attr != null){
			attributes.remove(attrName);
			QName qName = attr.getName();
			fireNotYetImplementedAttribute(attr.getLocation(), qName, 1);
		}
	}

	/**
	 * Fires an unhandled attributes event, if attributes exist in attributes map not covered by the exceptions.
	 * No event is fired if the unhandled elements stack is not empty.
	 * @param location
	 * @param attributes
	 * @param exceptions
	 */
	protected void handleUnexpectedAttributes(Location location,Map<String, Attribute> attributes, String... exceptions) {
		handleUnexpectedAttributes(location, attributes, 1, exceptions);
	}

	/**
	 * see {@link #handleUnexpectedAttributes(Location, Map, String...)}
     *
	 * @param location
	 * @param attributes
	 * @param stackDepth the stack trace depth
	 * @param exceptions
	 */
	private void handleUnexpectedAttributes(Location location,Map<String, Attribute> attributes, int stackDepth, String... exceptions) {
		if (attributes.size() > 0){
			if (this.unhandledElements.size() == 0 ){
				boolean hasUnhandledAttributes = false;
				for (String key : attributes.keySet()){
					boolean isException = false;
					for (String exception : exceptions){
						if(key.equals(exception)){
							isException = true;
						}
					}
					if (!isException){
						hasUnhandledAttributes = true;
					}
				}
				if (hasUnhandledAttributes){
					fireUnexpectedAttributes(location, attributes, stackDepth+1);
				}
			}
		}
	}


	private void fireUnexpectedAttributes(Location location, Map<String, Attribute> attributes, int stackDepth) {
		String attributesString = "";
		for (String key : attributes.keySet()){
			Attribute attribute = attributes.get(key);
			attributesString = CdmUtils.concat(",", attributesString, attribute.getName().getLocalPart() + ":" + attribute.getValue());
		}
		String message = "Unexpected attributes: %s";
		IoProblemEvent event = makeProblemEvent(location, String.format(message, attributesString), 1 , stackDepth +1 );
		fire(event);
	}


	protected void fireUnexpectedAttributeValue(XMLEvent parentEvent, String attrName, String attrValue) {
		String message = "Unexpected attribute value %s='%s'";
		message = String.format(message, attrName, attrValue);
		IoProblemEvent event = makeProblemEvent(parentEvent.getLocation(), message, 1 , 1 );
		fire(event);
	}

	protected void handleNotYetImplementedAttributeValue(XMLEvent xmlEvent, String attrName, String attrValue) {
		String message = "Attribute %s not yet implemented for value '%s'";
		message = String.format(message, attrName, attrValue);
		IIoEvent event = makeProblemEvent(xmlEvent.getLocation(), message, 1, 1 );
		fire(event);
	}

	protected void fireNotYetImplementedAttribute(Location location, QName qName, int stackDepth) {
		String message = "Attribute not yet implemented: %s";
		IIoEvent event = makeProblemEvent(location, String.format(message, qName.getLocalPart()), 1, stackDepth+1 );
		fire(event);
	}


	protected void fireUnexpectedEvent(XMLEvent xmlEvent, int stackDepth) {
		Location location = xmlEvent.getLocation();
		String message = "Unexpected event: %s";
		IIoEvent event = makeProblemEvent(location, String.format(message, xmlEvent.toString()), 2, stackDepth +1);
		fire(event);
	}

	protected void fireUnexpectedStartElement(Location location, StartElement startElement, int stackDepth) {
		QName qName = startElement.getName();
		String message = "Unexpected start element: %s";
		IIoEvent event = makeProblemEvent(location, String.format(message, qName.getLocalPart()), 2, stackDepth +1);
		fire(event);
	}


	protected void fireUnexpectedEndElement(Location location, EndElement endElement, int stackDepth) {
		QName qName = endElement.getName();
		String message = "Unexpected end element: %s";
		IIoEvent event = makeProblemEvent(location, String.format(message, qName.getLocalPart()), 16, stackDepth+1);
		fire(event);
	}

	protected void fireNotYetImplementedElement(Location location, QName qName, int stackDepth) {
		String message = "Element not yet implemented: %s";
		IIoEvent event = makeProblemEvent(location, String.format(message, qName.getLocalPart()), 1, stackDepth+1 );
		fire(event);
	}

	protected void fireNotYetImplementedCharacters(Location location, Characters chars, int stackDepth) {
		String message = "Characters not yet handled: %s";
		IIoEvent event = makeProblemEvent(location, String.format(message, chars.getData()), 1, stackDepth+1 );
		fire(event);
	}

	/**
	 * Creates a problem event.
	 * Be aware of the right depths of the stack trace !
	 * @param location
	 * @param message
	 * @param severity
	 * @return
	 */
	private IoProblemEvent makeProblemEvent(Location location, String message, int severity, int stackDepth) {
		stackDepth++;
		StackTraceElement[] stackTrace = new Exception().getStackTrace();
		int lineNumber = stackTrace[stackDepth].getLineNumber();
		String methodName = stackTrace[stackDepth].getMethodName();
		String locationStr = makeLocationStr(location);
		String className = stackTrace[stackDepth].getClassName();
		Class<?> declaringClass;
		try {
			declaringClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			declaringClass = this.getClass();
		}
		IoProblemEvent event = IoProblemEvent.NewInstance(declaringClass, message,
				locationStr, lineNumber, severity, methodName);
		return event;
	}

	/**
	 * Creates a string from a location
	 * @param location
	 * @return
	 */
	protected String makeLocationStr(Location location) {
		String locationStr = location == null ? " - no location - " : "l." + location.getLineNumber() + "/c."+ location.getColumnNumber();
		return locationStr;
	}


	/**
	 * Fires an unexpected element event if the unhandled elements stack is empty.
	 * Otherwise adds the element to the stack.
	 * @param event
	 */
	protected void handleUnexpectedStartElement(XMLEvent event) {
		handleUnexpectedStartElement(event, 1);
	}

	/**
	 * Fires an unexpected element event if the unhandled elements stack is empty.
	 * Otherwise adds the element to the stack.
	 * @param event
	 */
	protected void handleUnexpectedStartElement(XMLEvent event, int stackDepth) {
		QName qName = event.asStartElement().getName();
		if (! unhandledElements.empty()){
			unhandledElements.push(qName);
		}else{
			fireUnexpectedStartElement(event.getLocation(), event.asStartElement(), stackDepth + 1);
		}
	}


	protected void handleUnexpectedEndElement(EndElement event) {
		handleUnexpectedEndElement(event, 1);
	}

	/**
	 * Fires an unexpected element event if the event is not the last on the stack.
	 * Otherwise removes last stack element.
	 * @param event
	 */
	protected void handleUnexpectedEndElement(EndElement event, int stackDepth) {
		QName qName = event.asEndElement().getName();
		if (!unhandledElements.isEmpty() && unhandledElements.peek().equals(qName)){
			unhandledElements.pop();
		}else{
			fireUnexpectedEndElement(event.getLocation(), event.asEndElement(), stackDepth + 1);
		}
	}

	/**
	 *
	 * @param endElement
	 */
	protected void popUnimplemented(EndElement endElement) {
		QName qName = endElement.asEndElement().getName();
		if (unhandledElements.peek().equals(qName)){
			unhandledElements.pop();
		}else{
			String message = "End element is not last on stack: %s";
			message = String.format(message, qName.getLocalPart());
			IIoEvent event = makeProblemEvent(endElement.getLocation(), message, 16, 1);
			fire(event);
		}

	}


	/**
	 * Fires an unexpected element event if the unhandled element stack is empty.
	 * @param event
	 */
	protected void handleUnexpectedElement(XMLEvent event) {
		if (event.isStartElement()){
			handleUnexpectedStartElement(event, 2);
		}else if (event.isEndElement()){
			handleUnexpectedEndElement(event.asEndElement(), 2);
		}else if (event.getEventType() == XMLStreamConstants.COMMENT){
			//do nothing
		}else if (! unhandledElements.empty()){
			//do nothing
		}else{
			fireUnexpectedEvent(event, 1);
		}
	}

	/**
	 * Fires an not yet implemented event and adds the element name to the unhandled elements stack.
	 * @param event
	 */
	protected void handleNotYetImplementedCharacters(XMLEvent event) {
		Characters chars = event.asCharacters();
		fireNotYetImplementedCharacters(event.getLocation(), chars, 1);
	}

	/**
	 * Fires an not yet implemented event and adds the element name to the unhandled elements stack.
	 * @param event
	 */
	protected void handleNotYetImplementedElement(XMLEvent event) {
		QName qName = event.asStartElement().getName();
		boolean isTopLevel = unhandledElements.isEmpty();
		unhandledElements.push(qName);
		if (isTopLevel){
			fireNotYetImplementedElement(event.getLocation(), qName, 1);
		}
	}

	/**
	 * Fires an not yet implemented event and adds the element name to the unhandled elements stack.
	 * @param event
	 */
	protected void handleIgnoreElement(XMLEvent event) {
		QName qName = event.asStartElement().getName();
		unhandledElements.push(qName);
	}

	protected void handleAmbigousManually(MarkupImportState state,
			XMLEventReader reader, StartElement startElement) {
		QName qName = startElement.getName();
		unhandledElements.push(qName);
		fireWarningEvent(
				"Handle manually: " + qName.getLocalPart() + " is ambigous and should therefore be handled manually",
				makeLocationStr(startElement.getLocation()), 2, 2);
	}

	/**
	 * Checks if a mandatory text is not empty or null.
	 * Returns true if text is given.
	 * Fires an mandatory element is missing event otherwise and returns <code>null</code>.
	 * @param text
	 * @param parentEvent
	 * @return
	 */
	protected boolean checkMandatoryText(String text, XMLEvent parentEvent) {
		if (! StringUtils.isNotBlank(text)){
			fireMandatoryElementIsMissing(parentEvent, "CData", 4, 1);
			return false;
		}
		return true;
	}

	/**
	 * Fires an mandatory element is missing event if exists is <code>false</code>.
	 * @param hasMandatory
	 * @param parentEvent
	 * @param string
	 */
	protected void checkMandatoryElement(boolean exists, StartElement parentEvent, String attrName) {
		if (! exists){
			fireMandatoryElementIsMissing(parentEvent, attrName, 5, 1);
		}
	}


	/**
	 * Fires an element is missing event.
	 * @param xmlEvent
	 * @param string
	 * @param severity
	 * @param stackDepth
	 * @throws IllegalStateException if xmlEvent is not a StartElement and not an Attribute
	 */
	private void fireMandatoryElementIsMissing(XMLEvent xmlEvent, String missingEventName, int severity, int stackDepth) throws IllegalStateException{
		Location location = xmlEvent.getLocation();
		String typeName;
		QName qName;
		if (xmlEvent.isAttribute()){
			Attribute attribute = ((Attribute)xmlEvent);
			typeName = "attribute";
			qName = attribute.getName();
		}else if (xmlEvent.isStartElement()){
			typeName = "element";
			qName = xmlEvent.asStartElement().getName();
		}else{
			throw new IllegalStateException("mandatory element only allowed for attributes and start tags in " + makeLocationStr(location));
		}
		String message = "Mandatory %s '%s' is missing in %s";
		message = String.format(message, typeName , missingEventName, qName.getLocalPart());
		IIoEvent event = makeProblemEvent(location, message, severity, stackDepth +1);
		fire(event);
	}




	/**
	 * Returns true if the "next" event is the ending tag for the "parent" event.
	 * @param next end element to test, must not be null
	 * @param parentEvent start element to test
	 * @return true if the "next" event is the ending tag for the "parent" event.
	 * @throws XMLStreamException
	 */
	protected boolean isMyEndingElement(XMLEvent next, XMLEvent parentEvent) throws XMLStreamException {
		if (! parentEvent.isStartElement()){
			String message = "Parent event should be start tag";
			fireWarningEvent(message, makeLocationStr(next.getLocation()), 6);
			return false;
		}
		return isEndingElement(next, parentEvent.asStartElement().getName().getLocalPart());
	}

	/**
	 * Trims the text and removes turns all whitespaces into single empty space.
	 * @param text
	 * @return
	 */
	protected String normalize(String text) {
		text = StringUtils.trimToEmpty(text);
		text = text.replaceAll("\\s+", " ");
		return text;
	}



	/**
	 * Removes whitespaces at beginning and end and makes the first letter
	 * a capital letter and all other letters small letters.
	 * @param value
	 * @return
	 */
	protected String toFirstCapital(String value) {
		if (StringUtils.isBlank(value)){
			return value;
		}else{
			String result = "";
			value = value.trim();
			result += value.trim().substring(0,1).toUpperCase();
			if (value.length()>1){
				result += value.substring(1).toLowerCase();
			}
			return result;
		}
	}

	/**
	 * Currently not used.
	 * @param str
	 * @param allowedNumberOfCharacters
	 * @param onlyFirstCapital
	 * @return
	 */
	protected boolean isAbbreviation(String str, int allowedNumberOfCharacters, boolean onlyFirstCapital){
		if (isBlank(str)){
			return false;
		}
		str = str.trim();
		if (! str.endsWith(".")){
			return false;
		}
		str = str.substring(0, str.length() -1);
		if (str.length() > allowedNumberOfCharacters){
			return false;
		}
		final String re = "^\\p{javaUpperCase}\\p{javaLowerCase}*$";
		if (str.matches(re)){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Checks if <code>abbrev</code> is the short form for the genus name (strGenusName).
	 * Usually this is the case if <code>abbrev</code> is the first letter (optional with ".")
	 * of strGenusName. But in older floras it may also be the first 2 or 3 letters (optional with dot).
	 * However, we allow only a maximum of 2 letters to be anambigous. In cases with 3 letters better
	 * change the original markup data.
	 * @param single
	 * @param strGenusName
	 * @return
	 */
	protected boolean isGenusAbbrev(String abbrev, String strGenusName) {
		if (! abbrev.matches("[A-Z][a-z]?\\.?")) {
			return false;
		}else if (abbrev.length() == 0 || strGenusName == null || strGenusName.length() == 0){
			return false;
		}else{
			abbrev = abbrev.replace(".", "");
			return strGenusName.startsWith(abbrev);
//			boolean result = true;
//			for (int i = 0 ; i < abbrev.length(); i++){
//				result &= ( abbrev.charAt(i) == strGenusName.charAt(i));
//			}
//			return result;
		}
	}


	/**
	 * Checks if all words in the given string start with a capital letter but do not have any further capital letter.
	 * @param word the string to be checekd. Usually should be a single word.
	 * @return true if the above is the case, false otherwise
	 */
	protected boolean isFirstCapitalWord(String word) {
		if (WordUtils.capitalizeFully(word).equals(word)){
			return true;
		}else if (WordUtils.capitalizeFully(word,new char[]{'-'}).equals(word)){
			//for words like Le-Testui (which is a species epithet)
			return true;
		}else{
			return false;
		}
	}


	/**
	 * Read next event. Ignore whitespace events.
	 * @param reader
	 * @return
	 * @throws XMLStreamException
	 */
	protected XMLEvent readNoWhitespace(XMLEventReader reader) throws XMLStreamException {
		XMLEvent event = reader.nextEvent();
		while (!unhandledElements.isEmpty()){
			if (event.isStartElement()){
				handleNotYetImplementedElement(event);
			}else if (event.isEndElement()){
				popUnimplemented(event.asEndElement());
			}
			event = reader.nextEvent();
		}
		while (event.isCharacters() && event.asCharacters().isWhiteSpace()){
			event = reader.nextEvent();
		}
		return event;
	}

	/**
	 * Returns the REQUIRED "class" attribute for a given event and checks that it is the only attribute.
	 * @param parentEvent
	 * @return
	 */
	protected String getClassOnlyAttribute(XMLEvent parentEvent) {
		return getClassOnlyAttribute(parentEvent, true);
	}


	/**
	 * Returns the "class" attribute for a given event and checks that it is the only attribute.
	 * @param parentEvent
	 * @return
	 */
	protected String getClassOnlyAttribute(XMLEvent parentEvent, boolean required) {
		return getOnlyAttribute(parentEvent, CLASS, required);
	}

	/**
	 * Returns the value for the only attribute for a given event and checks that it is the only attribute.
	 * @param parentEvent
	 * @return
	 */
	protected String getOnlyAttribute(XMLEvent parentEvent, String attrName, boolean required) {
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		String classValue =getAndRemoveAttributeValue(parentEvent, attributes, attrName, required, 1);
		checkNoAttributes(attributes, parentEvent);
		return classValue;
	}


	protected void fireWarningEvent(String message, String locationStr, Integer severity, Integer depth) {
		docImport.fireWarningEvent(message, locationStr, severity, depth);
	}

	protected void fireWarningEvent(String message, XMLEvent event, Integer severity) {
		docImport.fireWarningEvent(message, makeLocationStr(event.getLocation()), severity, 1);
	}

	protected void fireSchemaConflictEventExpectedStartTag(String elName, XMLEventReader reader) throws XMLStreamException {
		docImport.fireSchemaConflictEventExpectedStartTag(elName, reader);
	}


	protected void fireWarningEvent(String message, String locationStr, int severity) {
		docImport.fireWarningEvent(message, locationStr, severity, 1);
	}

	protected void fire(IIoEvent event) {
		docImport.fire(event);
	}

	protected boolean isNotBlank(String str){
		return StringUtils.isNotBlank(str);
	}

	protected boolean isBlank(String str){
		return StringUtils.isBlank(str);
	}

	protected TaxonDescription getTaxonDescription(Taxon taxon, Reference<?> ref, boolean isImageGallery, boolean createNewIfNotExists) {
		return docImport.getTaxonDescription(taxon, isImageGallery, createNewIfNotExists);
	}


	/**
	 * Returns the default language defined in the state. If no default language is defined in the state,
	 * the CDM default language is returned.
	 * @param state
	 * @return
	 */
	protected Language getDefaultLanguage(MarkupImportState state) {
		Language result = state.getDefaultLanguage();
		if (result == null){
			result = Language.DEFAULT();
		}
		return result;
	}


//*********************** FROM XML IMPORT BASE ****************************************
	protected boolean isEndingElement(XMLEvent event, String elName) throws XMLStreamException {
		return docImport.isEndingElement(event, elName);
	}

	protected boolean isStartingElement(XMLEvent event, String elName) throws XMLStreamException {
		return docImport.isStartingElement(event, elName);
	}


	protected void fillMissingEpithetsForTaxa(Taxon parentTaxon, Taxon childTaxon) {
		docImport.fillMissingEpithetsForTaxa(parentTaxon, childTaxon);
	}

	protected Feature getFeature(MarkupImportState state, UUID uuid, String label, String text, String labelAbbrev, TermVocabulary<Feature> voc){
		return docImport.getFeature(state, uuid, label, text, labelAbbrev, voc);
	}

	protected ExtensionType getExtensionType(MarkupImportState state, UUID uuid, String label, String text, String labelAbbrev){
		return docImport.getExtensionType(state, uuid, label, text, labelAbbrev);
	}

	protected DefinedTerm getIdentifierType(MarkupImportState state, UUID uuid, String label, String text, String labelAbbrev, TermVocabulary<DefinedTerm> voc){
		return docImport.getIdentifierType(state, uuid, label, text, labelAbbrev, voc);
	}

	protected AnnotationType getAnnotationType(MarkupImportState state, UUID uuid, String label, String text, String labelAbbrev, TermVocabulary<AnnotationType> voc){
		return docImport.getAnnotationType(state, uuid, label, text, labelAbbrev, voc);
	}

	protected MarkerType getMarkerType(MarkupImportState state, UUID uuid, String label, String text, String labelAbbrev, TermVocabulary<MarkerType> voc){
		return docImport.getMarkerType(state, uuid, label, text, labelAbbrev, voc);
	}

	protected NamedAreaLevel getNamedAreaLevel(MarkupImportState state, UUID uuid, String label, String text, String labelAbbrev, TermVocabulary<NamedAreaLevel> voc){
		return docImport.getNamedAreaLevel(state, uuid, label, text, labelAbbrev, voc);
	}

	protected NamedArea getNamedArea(MarkupImportState state, UUID uuid, String label, String text, String labelAbbrev, NamedAreaType areaType, NamedAreaLevel level, TermVocabulary voc, TermMatchMode matchMode){
		return docImport.getNamedArea(state, uuid, label, text, labelAbbrev, areaType, level, voc, matchMode);
	}

	protected Language getLanguage(MarkupImportState state, UUID uuid, String label, String text, String labelAbbrev, TermVocabulary<?> voc){
		return docImport.getLanguage(state, uuid, label, text, labelAbbrev, voc);
	}

// *************************************** Concrete methods **********************************************/


	/**
	 * @param state
	 * @param classValue
	 * @param byAbbrev
	 * @return
	 */
	protected Rank makeRank(MarkupImportState state, String value, boolean byAbbrev) {
		Rank rank = null;
		if (StringUtils.isBlank(value)) {
			return null;
		}
		try {
			boolean useUnknown = true;
			NomenclaturalCode nc = makeNomenclaturalCode(state);
			if (value.equals(GENUS_ABBREVIATION)){
				rank = Rank.GENUS();
			}else if (byAbbrev) {
				rank = Rank.getRankByIdInVoc(value, nc, useUnknown);
			} else {
				rank = Rank.getRankByEnglishName(value, nc, useUnknown);
			}
			if (rank.equals(Rank.UNKNOWN_RANK())) {
				rank = null;
			}
			if (rank == null && "sous-genre".equalsIgnoreCase(value)){
				rank = Rank.SUBGENUS();
			}
		} catch (UnknownCdmTypeException e) {
			// doNothing
		}
		return rank;
	}



	protected TeamOrPersonBase<?> createAuthor(String authorTitle) {
		// TODO atomize and also use by name creation
		TeamOrPersonBase<?> result = Team.NewTitledInstance(authorTitle, authorTitle);
		return result;
	}

	protected String getAndRemoveMapKey(Map<String, String> map, String key) {
		String result = map.get(key);
		map.remove(key);
		if (result != null) {
			result = normalize(result);
		}
		return StringUtils.stripToNull(result);
	}


	/**
	 * Creates a {@link NonViralName} object depending on the defined {@link NomenclaturalCode}
	 * and the given parameters.
	 * @param state
	 * @param rank
	 * @return
	 */
	protected NonViralName<?> createNameByCode(MarkupImportState state, Rank rank) {
		NonViralName<?> name;
		NomenclaturalCode nc = makeNomenclaturalCode(state);
		name = (NonViralName<?>) nc.getNewTaxonNameInstance(rank);
		return name;
	}

	protected void handleFullName(MarkupImportState state, XMLEventReader reader,
			NonViralName<?> name, XMLEvent next) throws XMLStreamException {
		String fullNameStr;
		Map<String, Attribute> attrs = getAttributes(next);
		String rankStr = getAndRemoveRequiredAttributeValue(next,
				attrs, "rank");
		Rank rank = makeRank(state, rankStr, false);
		name.setRank(rank);
		if (rank == null) {
			String message = "Rank was computed as null. This must not be.";
			fireWarningEvent(message, next, 6);
			name.setRank(Rank.UNKNOWN_RANK());
		}
		if (!attrs.isEmpty()) {
			handleUnexpectedAttributes(next.getLocation(), attrs);
		}
//		next = readNoWhitespace(reader);
		fullNameStr = getCData(state, reader, next, false);
		NonViralNameParserImpl.NewInstance().parseFullName(name, fullNameStr, rank, false);
//		name.setTitleCache(fullNameStr, true);
	}


	/**
	 * Returns the {@link NomenclaturalCode} for this import. Default is {@link NomenclaturalCode#ICBN} if
	 * no code is defined.
	 * @param state
	 * @return
	 */
	protected NomenclaturalCode makeNomenclaturalCode(MarkupImportState state) {
		NomenclaturalCode nc = state.getConfig().getNomenclaturalCode();
		if (nc == null) {
			nc = NomenclaturalCode.ICNAFP; // default;
		}
		return nc;
	}


	/**
	 * @param state
	 * @param levelString
	 * @param next
	 * @return
	 */
	protected NamedAreaLevel makeNamedAreaLevel(MarkupImportState state, String levelString, XMLEvent next) {
		NamedAreaLevel level;
		try {
			level = state.getTransformer().getNamedAreaLevelByKey(levelString);
			if (level == null) {
				UUID levelUuid = state.getTransformer().getNamedAreaLevelUuid(levelString);
				if (levelUuid == null) {
					String message = "Unknown distribution locality class (named area level): %s. Create new level instead.";
					message = String.format(message, levelString);
					fireWarningEvent(message, next, 6);
				}
				level = getNamedAreaLevel(state, levelUuid, levelString, levelString, levelString, null);
			}
		} catch (UndefinedTransformerMethodException e) {
			throw new RuntimeException(e);
		}
		return level;
	}


	/**
	 * @param state
	 * @param areaName
	 * @param level
	 * @return
	 */
	protected NamedArea makeArea(MarkupImportState state, String areaName, NamedAreaLevel level) {

		//TODO FM vocabulary
		TermVocabulary<NamedArea> voc = null;
		NamedAreaType areaType = null;

		NamedArea area = null;
		try {
			area = state.getTransformer().getNamedAreaByKey(areaName);
		} catch (UndefinedTransformerMethodException e) {
			throw new RuntimeException(e);
		}
		if (area == null){
			boolean isNewInState = false;
			UUID uuid = state.getAreaUuid(areaName);
			if (uuid == null){
				isNewInState = true;


				try {
					uuid = state.getTransformer().getNamedAreaUuid(areaName);
				} catch (UndefinedTransformerMethodException e) {
					throw new RuntimeException(e);
				}
			}

			CdmImportBase.TermMatchMode matchMode = CdmImportBase.TermMatchMode.UUID_LABEL;
			area = getNamedArea(state, uuid, areaName, areaName, areaName, areaType, level, voc, matchMode);
			if (isNewInState){
				state.putAreaUuid(areaName, area.getUuid());

				//TODO just for testing -> make generic and move to better place
				String geoServiceLayer="vmap0_as_bnd_political_boundary_a";
				String layerFieldName ="nam";

				if ("Bangka".equals(areaName)){
					String areaValue = "PULAU BANGKA#SUMATERA SELATAN";
					GeoServiceArea geoServiceArea = new GeoServiceArea();
					geoServiceArea.add(geoServiceLayer, layerFieldName, areaValue);
					this.editGeoService.setMapping(area, geoServiceArea);
//					save(area, state);
				}
				if ("Luzon".equals(areaName)){
					GeoServiceArea geoServiceArea = new GeoServiceArea();

					List<String> list = Arrays.asList("HERMANA MAYOR ISLAND#CENTRAL LUZON",
							"HERMANA MENOR ISLAND#CENTRAL LUZON",
							"CENTRAL LUZON");
					for (String areaValue : list){
						geoServiceArea.add(geoServiceLayer, layerFieldName, areaValue);
					}

					this.editGeoService.setMapping(area, geoServiceArea);
//					save(area, state);
				}
				if ("Mindanao".equals(areaName)){
					GeoServiceArea geoServiceArea = new GeoServiceArea();

					List<String> list = Arrays.asList("NORTHERN MINDANAO",
							"SOUTHERN MINDANAO",
							"WESTERN MINDANAO");
					//TODO to be continued
					for (String areaValue : list){
						geoServiceArea.add(geoServiceLayer, layerFieldName, areaValue);
					}

					this.editGeoService.setMapping(area, geoServiceArea);
//					save(area, state);
				}
				if ("Palawan".equals(areaName)){
					GeoServiceArea geoServiceArea = new GeoServiceArea();

					List<String> list = Arrays.asList("PALAWAN#SOUTHERN TAGALOG");
					for (String areaValue : list){
						geoServiceArea.add(geoServiceLayer, layerFieldName, areaValue);
					}

					this.editGeoService.setMapping(area, geoServiceArea);
//					save(area, state);
				}

			}
		}
		return area;
	}



	/**
	 * Reads character data. Any element other than character data or the ending
	 * tag will fire an unexpected element event.
     *
	 * @see #getCData(MarkupImportState, XMLEventReader, XMLEvent, boolean)
	 * @param state
	 * @param reader
	 * @param next
	 * @return
	 * @throws XMLStreamException
	 */
	protected String getCData(MarkupImportState state, XMLEventReader reader, XMLEvent next) throws XMLStreamException {
		return getCData(state, reader, next, true);
	}

	/**
	 * Reads character data. Any element other than character data or the ending
	 * tag will fire an unexpected element event.
	 *
	 * @param state
	 * @param reader
	 * @param next
	 * @param inlineMarkup map for inline markup, this is used for e.g. the locality markup within a subheading
	 * The map will be filled by the markup element name as key. The value may be a String, a CdmBase or any other object.
	 * If null any markup text will be neglected but a warning will be fired if they exist.
	 * @param removeInlineMarkupText if true the markedup text will be removed from the returned String
	 * @param checkAttributes
	 * @return
	 * @throws XMLStreamException
	 */
	protected String getCData(MarkupImportState state, XMLEventReader reader, XMLEvent parent, /*Map<String, Object> inlineMarkup, *boolean removeInlineMarkupText,*/ boolean checkAttributes) throws XMLStreamException {
		if (checkAttributes){
			checkNoAttributes(parent);
		}

		String text = "";
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parent)) {
				return text;
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();
			} else if (isStartingElement(next, FOOTNOTE_REF)){
				handleNotYetImplementedElement(next);
//			} else if (isStartingElement(next, LOCALITY)){
//				handleCDataLocality(state, reader, parent);
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("Event has no closing tag");

	}

//	private void handleCDataLocality(MarkupImportState state, XMLEventReader reader, XMLEvent parent) {
//		checkAndRemoveAttributeValue(attributes, attrName, value)
//
//	}



	/**
	 * For it returns a pure CData annotation string. This behaviour may change in future. More complex annotations
	 * should be handled differently.
	 * @param state
	 * @param reader
	 * @param parentEvent
	 * @return
	 * @throws XMLStreamException
	 */
	protected String handleSimpleAnnotation(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		String annotation = getCData(state, reader, parentEvent);
		return annotation;
	}

	/**
	 * True if text is single "." oder "," or ";" or ":"
	 * @param text
	 * @return
	 */
	protected boolean isPunctuation(String text) {
		return text == null ? false : text.trim().matches("^[\\.,;:]$");
	}


	/**
	 * Text indicating that type information is following but no information about the type of the type
	 * @param text
	 * @return
	 */
	protected boolean charIsSimpleType(String text) {
		return text.matches("(?i)Type:");
	}

	protected String getXmlTag(XMLEvent event) {
		String result;
		if (event.isStartElement()) {
			result = "<" + event.asStartElement().getName().getLocalPart()
					+ ">";
		} else if (event.isEndElement()) {
			result = "</" + event.asEndElement().getName().getLocalPart() + ">";
		} else {
			String message = "Only start or end elements are allowed as Html tags";
			throw new IllegalStateException(message);
		}
		return result;
	}

	protected WriterDataHolder handleWriter(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		String text = "";
		checkNoAttributes(parentEvent);
		WriterDataHolder dataHolder = new WriterDataHolder();
		List<FootnoteDataHolder> footnotes = new ArrayList<FootnoteDataHolder>();

		// TODO handle attributes
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				text = CdmUtils.removeBrackets(text);
				if (checkMandatoryText(text, parentEvent)) {
					text = normalize(text);
					dataHolder.writer = text;
					dataHolder.footnotes = footnotes;

					// Extension
					UUID uuidWriterExtension = MarkupTransformer.uuidWriterExtension;
					ExtensionType writerExtensionType =
							this.getExtensionType(state, uuidWriterExtension,"Writer", "writer", "writer");
					Extension extension = Extension.NewInstance();
					extension.setType(writerExtensionType);
					extension.setValue(text);
					dataHolder.extension = extension;

					// Annotation
					UUID uuidWriterAnnotation = MarkupTransformer.uuidWriterAnnotation;
					AnnotationType writerAnnotationType = this.getAnnotationType(state, uuidWriterAnnotation, "Writer", "writer", "writer", null);
					Annotation annotation = Annotation.NewInstance(text, writerAnnotationType, getDefaultLanguage(state));
					dataHolder.annotation = annotation;

					return dataHolder;
				} else {
					return null;
				}
			} else if (isStartingElement(next, FOOTNOTE_REF)) {
				FootnoteDataHolder footNote = handleFootnoteRef(state, reader, next);
				if (footNote.isRef()) {
					footnotes.add(footNote);
				} else {
					logger.warn("Non ref footnotes not yet impelemnted");
				}
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();

			} else {
				handleUnexpectedElement(next);
				state.setUnsuccessfull();
			}
		}
		throw new IllegalStateException("<writer> has no end tag");
	}


	protected void registerFootnotes(MarkupImportState state, AnnotatableEntity entity, List<FootnoteDataHolder> footnotes) {
		for (FootnoteDataHolder footNote : footnotes) {
			registerFootnoteDemand(state, entity, footNote);
		}
	}


	private void registerFootnoteDemand(MarkupImportState state, AnnotatableEntity entity, FootnoteDataHolder footnote) {
		FootnoteDataHolder existingFootnote = state.getFootnote(footnote.ref);
		if (existingFootnote != null) {
			attachFootnote(state, entity, existingFootnote);
		} else {
			Set<AnnotatableEntity> demands = state.getFootnoteDemands(footnote.ref);
			if (demands == null) {
				demands = new HashSet<AnnotatableEntity>();
				state.putFootnoteDemands(footnote.ref, demands);
			}
			demands.add(entity);
		}
	}


	protected void attachFootnote(MarkupImportState state, AnnotatableEntity entity, FootnoteDataHolder footnote) {
		AnnotationType annotationType = this.getAnnotationType(state, MarkupTransformer.uuidFootnote, "Footnote", "An e-flora footnote", "fn", null);
		Annotation annotation = Annotation.NewInstance(footnote.string, annotationType, getDefaultLanguage(state));
		// TODO transient objects
		entity.addAnnotation(annotation);
		save(entity, state);
	}


	protected void attachFigure(MarkupImportState state, XMLEvent next, AnnotatableEntity entity, Media figure) {
		// IdentifiableEntity<?> toSave;
		if (entity.isInstanceOf(TextData.class)) {
			TextData deb = CdmBase.deproxy(entity, TextData.class);
			deb.addMedia(figure);
			// toSave = ((TaxonDescription)deb.getInDescription()).getTaxon();
		} else if (entity.isInstanceOf(SpecimenOrObservationBase.class)) {
			String message = "figures for specimen should be handled as Textdata";
			fireWarningEvent(message, next, 4);
			// toSave = ime;
		} else if (entity.isInstanceOf(IdentifiableMediaEntity.class)) {
			IdentifiableMediaEntity<?> ime = CdmBase.deproxy(entity, IdentifiableMediaEntity.class);
			ime.addMedia(figure);
			// toSave = ime;
		} else {
			String message = "Unsupported entity to attach media: %s";
			message = String.format(message, entity.getClass().getName());
			// toSave = null;
		}
		save(entity, state);
	}


	protected void registerGivenFootnote(MarkupImportState state, FootnoteDataHolder footnote) {
		state.registerFootnote(footnote);
		Set<AnnotatableEntity> demands = state.getFootnoteDemands(footnote.id);
		if (demands != null) {
			for (AnnotatableEntity entity : demands) {
				attachFootnote(state, entity, footnote);
			}
		}
	}


	protected FootnoteDataHolder handleFootnote(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent,
			MarkupSpecimenImport specimenImport, MarkupNomenclatureImport nomenclatureImport) throws XMLStreamException {
		FootnoteDataHolder result = new FootnoteDataHolder();
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		result.id = getAndRemoveAttributeValue(attributes, ID);
		// result.ref = getAndRemoveAttributeValue(attributes, REF);
		checkNoAttributes(attributes, parentEvent);

		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isStartingElement(next, FOOTNOTE_STRING)) {
				String string = handleFootnoteString(state, reader, next, specimenImport, nomenclatureImport);
				result.string = string;
			} else if (isMyEndingElement(next, parentEvent)) {
				return result;
			} else {
				fireUnexpectedEvent(next, 0);
			}
		}
		return result;
	}


	protected Media handleFigure(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent,
			MarkupSpecimenImport specimenImport, MarkupNomenclatureImport nomenclatureImport) throws XMLStreamException {
		// FigureDataHolder result = new FigureDataHolder();

		Map<String, Attribute> attributes = getAttributes(parentEvent);
		String id = getAndRemoveAttributeValue(attributes, ID);
		String type = getAndRemoveAttributeValue(attributes, TYPE);
		String urlAttr = getAndRemoveAttributeValue(attributes, URL);
		checkNoAttributes(attributes, parentEvent);

		String urlString = null;
		String legendString = null;
		String titleString = null;
		String numString = null;
		String text = null;
		if (isNotBlank(urlAttr)){
			urlString = CdmUtils.Nz(state.getBaseMediaUrl()) + urlAttr;
		}
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				if (isNotBlank(text)){
					fireWarningEvent("Text not yet handled for figures: " + text, next, 4);
				}
				Media media = makeFigure(state, id, type, urlString, legendString, titleString, numString, next);
				return media;
			} else if (isStartingElement(next, FIGURE_LEGEND)) {
				// TODO same as figure string ?
				legendString = handleFootnoteString(state, reader, next, specimenImport, nomenclatureImport);
			} else if (isStartingElement(next, FIGURE_TITLE)) {
				titleString = getCData(state, reader, next);
			} else if (isStartingElement(next, URL)) {
				String localUrl = getCData(state, reader, next);
				String url = CdmUtils.Nz(state.getBaseMediaUrl()) + localUrl;
				if (isBlank(urlString)){
					urlString = url;
				}
				if (! url.equals(urlString)){
					String message = "URL attribute and URL element differ. Attribute: %s, Element: %s";
					fireWarningEvent(String.format(message, urlString, url), next, 2);
				}
			} else if (isStartingElement(next, NUM)) {
				numString = getCData(state, reader, next);
			} else if (next.isCharacters()) {
				text += CdmUtils.concat("", text, next.asCharacters().getData());
			} else {
				fireUnexpectedEvent(next, 0);
			}
		}
		throw new IllegalStateException("<figure> has no end tag");
	}


	/**
	 * @param state
	 * @param id
	 * @param type
	 * @param urlString
	 * @param legendString
	 * @param titleString
	 * @param numString
	 * @param next
	 */
	private Media makeFigure(MarkupImportState state, String id, String type, String urlString,
			String legendString, String titleString, String numString, XMLEvent next) {
		Media media = null;
//		boolean isFigure = false;  //no difference between figure and media since v3.3
		try {
			//TODO maybe everything is a figure as it is all taken from a book
			if ("lineart".equals(type)) {
//				isFigure = true;
//				media = Figure.NewInstance(url.toURI(), null, null,	null);
			} else if (type == null || "photo".equals(type)
					|| "signature".equals(type)
					|| "others".equals(type)) {
				//TODO
			} else {
				String message = "Unknown figure type '%s'";
				message = String.format(message, type);
				fireWarningEvent(message, next, 2);
			}
			media = docImport.getImageMedia(urlString, docImport.getReadMediaData());

			if (media != null){
				// title
				if (StringUtils.isNotBlank(titleString)) {
					media.putTitle(getDefaultLanguage(state), titleString);
				}
				// legend
				if (StringUtils.isNotBlank(legendString)) {
					media.putDescription(getDefaultLanguage(state), legendString);
				}
				if (StringUtils.isNotBlank(numString)) {
					// TODO use concrete source (e.g. DAPHNIPHYLLACEAE in FM
					// vol.13)
					Reference<?> citation = state.getConfig().getSourceReference();
					media.addSource(OriginalSourceType.Import, numString, "num", citation, null);
					// TODO name used in source if available
				}
				// TODO which citation
				if (StringUtils.isNotBlank(id)) {
					media.addSource(OriginalSourceType.Import, id, null, state.getConfig().getSourceReference(), null);
				} else {
					String message = "Figure id should never be empty or null";
					fireWarningEvent(message, next, 6);
				}

				// text
				// do nothing
				registerGivenFigure(state, next, id, media);

			}else{
				String message = "No media found: ";
				fireWarningEvent(message, next, 4);
			}
		} catch (MalformedURLException e) {
			String message = "Media uri has incorrect syntax: %s";
			message = String.format(message, urlString);
			fireWarningEvent(message, next, 4);
//		} catch (URISyntaxException e) {
//			String message = "Media uri has incorrect syntax: %s";
//			message = String.format(message, urlString);
//			fireWarningEvent(message, next, 4);
		}

		return media;
	}


	private void registerGivenFigure(MarkupImportState state, XMLEvent next, String id, Media figure) {
		state.registerFigure(id, figure);
		Set<AnnotatableEntity> demands = state.getFigureDemands(id);
		if (demands != null) {
			for (AnnotatableEntity entity : demands) {
				attachFigure(state, next, entity, figure);
			}
		}
		save(figure, state);
	}


	private FootnoteDataHolder handleFootnoteRef(MarkupImportState state,
			XMLEventReader reader, XMLEvent parentEvent)
			throws XMLStreamException {
		FootnoteDataHolder result = new FootnoteDataHolder();
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		result.ref = getAndRemoveAttributeValue(attributes, REF);
		checkNoAttributes(attributes, parentEvent);

		// text is not handled, needed only for debugging purposes
		String text = "";
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			// if (isStartingElement(next, FOOTNOTE_STRING)){
			// String string = handleFootnoteString(state, reader, next);
			// result.string = string;
			// }else
			if (isMyEndingElement(next, parentEvent)) {
				if (StringUtils.isNotBlank(text)){
					fireWarningEvent("text is not empty but not handled during import", parentEvent, 4);
				}
				return result;
			} else if (next.isCharacters() && unhandledElements.isEmpty()) {
				text += next.asCharacters().getData();
			} else if (isStartingElement(next, NUM)) {
				//ignore numbering of footnotes as they are numbered differently in the CDM
				handleIgnoreElement(next);
			} else {
				handleUnexpectedElement(next);
			}
		}
		return result;
	}



	private String handleFootnoteString(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, MarkupSpecimenImport specimenImport, MarkupNomenclatureImport nomenclatureImport) throws XMLStreamException {
		boolean isTextMode = true;
		String text = "";
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				return text;
			} else if (next.isEndElement()) {
				if (isEndingElement(next, FULL_NAME)) {
					popUnimplemented(next.asEndElement());
				} else if (isEndingElement(next, BR)) {
					isTextMode = true;
				} else if (isHtml(next)) {
					text += getXmlTag(next);
				} else {
					handleUnexpectedEndElement(next.asEndElement());
				}
			} else if (next.isStartElement()) {
				if (isStartingElement(next, FULL_NAME)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, GATHERING)) {
					text += specimenImport.handleInLineGathering(state, reader, next);
				} else if (isStartingElement(next, REFERENCES)) {
					text += " " + handleInLineReferences(state, reader, next, nomenclatureImport)+ " ";
				} else if (isStartingElement(next, BR)) {
					text += "<br/>";
					isTextMode = false;
				} else if (isStartingElement(next, NOMENCLATURE)) {
					handleNotYetImplementedElement(next);
				} else if (isHtml(next)) {
					text += getXmlTag(next);
				} else {
					handleUnexpectedStartElement(next.asStartElement());
				}
			} else if (next.isCharacters()) {
				if (!isTextMode) {
					String message = "footnoteString is not in text mode";
					fireWarningEvent(message, next, 6);
				} else {
					text += next.asCharacters().getData().trim();
					// getCData(state, reader, next); does not work as we have inner tags like <references>
				}
			} else {
				handleUnexpectedEndElement(next.asEndElement());
			}
		}
		throw new IllegalStateException("<footnoteString> has no closing tag");

	}

	private static final List<String> htmlList = Arrays.asList("sub", "sup",
			"ol", "ul", "li", "i", "b", "table", "br","tr","td","th");

	protected boolean isHtml(XMLEvent event) {
		if (event.isStartElement()) {
			String tag = event.asStartElement().getName().getLocalPart();
			return htmlList.contains(tag);
		} else if (event.isEndElement()) {
			String tag = event.asEndElement().getName().getLocalPart();
			return htmlList.contains(tag);
		} else {
			return false;
		}

	}


	private String handleInLineReferences(MarkupImportState state,XMLEventReader reader, XMLEvent parentEvent, MarkupNomenclatureImport nomenclatureImport) throws XMLStreamException {
		checkNoAttributes(parentEvent);

		boolean hasReference = false;
		String text = "";
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				checkMandatoryElement(hasReference, parentEvent.asStartElement(), REFERENCE);
				return text;
			} else if (isStartingElement(next, REFERENCE)) {
				text += handleInLineReference(state, reader, next, nomenclatureImport);
				hasReference = true;
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("<References> has no closing tag");
	}

	private String handleInLineReference(MarkupImportState state,XMLEventReader reader, XMLEvent parentEvent, MarkupNomenclatureImport nomenclatureImport)throws XMLStreamException {
		Reference<?> reference = nomenclatureImport.handleReference(state, reader, parentEvent);
		String result = "<cdm:ref uuid='%s'>%s</ref>";
		result = String.format(result, reference.getUuid(), reference.getTitleCache());
		save(reference, state);
		return result;
	}


	/**
	 * Handle string
	 * @param state
	 * @param reader
	 * @param parentEvent
	 * @param feature only needed for distributionLocalities
	 * @return
	 * @throws XMLStreamException
	 */
	protected Map<String, String> handleString(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, Feature feature)throws XMLStreamException {
		// attributes
		String classValue = getClassOnlyAttribute(parentEvent, false);
		if (StringUtils.isNotBlank(classValue)) {
			String message = "class attribute for <string> not yet implemented";
			fireWarningEvent(message, parentEvent, 2);
		}
		boolean isHabitat = false;

		// subheadings
		Map<String, String> subHeadingMap = new HashMap<String, String>();
		String currentSubheading = null;

		boolean isTextMode = true;
		String text = "";
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				putCurrentSubheading(subHeadingMap, currentSubheading, text);
				if (isHabitat ){
				    if (currentSubheading != null && ! isHabitatHeading(currentSubheading) || isNotBlank(text)){
				        String message = "String is habitat but currentSubHeading or text is not blank: " + CdmUtils.concat(", ", currentSubheading, text);
				        fireWarningEvent(message, next, 4);
				    }
				}
				return subHeadingMap;
			} else if (isStartingElement(next, BR)) {
				text += "<br/>";
				isTextMode = false;
			} else if (isEndingElement(next, BR)) {
				isTextMode = true;
			} else if (isHtml(next)) {
				text += getXmlTag(next);
			} else if (isStartingElement(next, SUB_HEADING)) {
				text = putCurrentSubheading(subHeadingMap,currentSubheading, text);
				// TODO footnotes
				currentSubheading = getCData(state, reader, next).trim();
			} else if (isStartingElement(next, DISTRIBUTION_LOCALITY)) {
				if (feature != null && !feature.equals(Feature.DISTRIBUTION())) {
					String message = "Distribution locality only allowed for feature of type 'distribution'";
					fireWarningEvent(message, next, 4);
				}
				text += handleDistributionLocality(state, reader, next);
			} else if (next.isCharacters()) {
				if (! isTextMode) {
					String message = "String is not in text mode";
					fireWarningEvent(message, next, 6);
				} else {
					text += next.asCharacters().getData();
				}
			} else if (isStartingElement(next, HEADING)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, VERNACULAR_NAMES)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, QUOTE)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, DEDICATION)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, TAXONTYPE)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, FULL_NAME)) {
				//TODO
				handleNotYetImplementedElement(next);
			}else if (isStartingElement(next, REFERENCES)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, GATHERING)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, ANNOTATION)) {
				//TODO  //TODO test handleSimpleAnnotation
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, HABITAT)) {
			    featureImport.handleHabitat(state, reader, next);
			    isHabitat = true;
			} else if (isStartingElement(next, FIGURE_REF)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, FIGURE)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, FOOTNOTE_REF)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, FOOTNOTE)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, WRITER)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, DATES)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("<String> has no closing tag");
	}


    /**
     * @param currentSubheading
     * @return
     */
    private boolean isHabitatHeading(String heading) {
        return heading.trim().matches("Ecol(ogy)?\\.?");
    }


    /**
	 * @param subHeadingMap
	 * @param currentSubheading
	 * @param text
	 * @return
	 */
	private String putCurrentSubheading(Map<String, String> subHeadingMap, String currentSubheading, String text) {
		if (StringUtils.isNotBlank(text)) {
			text = removeStartingMinus(text);
			subHeadingMap.put(currentSubheading, text.trim());
		}
		return "";
	}

	private String removeStartingMinus(String string) {
		string = replaceStart(string, "-");
		string = replaceStart(string, "\u002d");
		string = replaceStart(string, "\u2013");
		string = replaceStart(string, "\u2014");
		string = replaceStart(string, "--");
		return string;
	}


	/**
	 * @param value
	 * @param replacementString
	 */
	private String replaceStart(String value, String replacementString) {
		if (value.startsWith(replacementString) ){
			value = value.substring(replacementString.length()).trim();
		}
		while (value.startsWith("-") || value.startsWith("\u2014") ){
			value = value.substring("-".length()).trim();
		}
		return value;
	}


	private String handleDistributionLocality(MarkupImportState state,XMLEventReader reader, XMLEvent parentEvent)throws XMLStreamException {
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		String classValue = getAndRemoveRequiredAttributeValue(parentEvent, attributes, CLASS);
		String statusValue =getAndRemoveAttributeValue(attributes, STATUS);
		String frequencyValue =getAndRemoveAttributeValue(attributes, FREQUENCY);


		Taxon taxon = state.getCurrentTaxon();
		// TODO which ref to take?
		Reference<?> ref = state.getConfig().getSourceReference();

		String text = "";
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				if (StringUtils.isNotBlank(text)) {
					String label = CdmUtils.removeTrailingDot(normalize(text));
					TaxonDescription description = getTaxonDescription(taxon, ref, false, true);
					NamedAreaLevel level = makeNamedAreaLevel(state,classValue, next);

					//status
					PresenceAbsenceTerm status = null;
					if (isNotBlank(statusValue)){
						try {
							status = state.getTransformer().getPresenceTermByKey(statusValue);
							if (status == null){
								//TODO
								String message = "The presence/absence status '%s' could not be transformed to an CDM status";
								fireWarningEvent(String.format(message, statusValue), next, 4);
							}
						} catch (UndefinedTransformerMethodException e) {
							throw new RuntimeException(e);
						}
					}else{
						status = PresenceAbsenceTerm.PRESENT();
					}
					//frequency
					if (isNotBlank(frequencyValue)){
						String message = "The frequency attribute is currently not yet available in CDM";
						fireWarningEvent(message, parentEvent, 6);
					}

					NamedArea higherArea = null;
					List<NamedArea> areas = new ArrayList<NamedArea>();

					String patSingleArea = "([^,\\(]{3,})";
					String patSeparator = "(,|\\sand\\s)";
					String hierarchiePattern = String.format("%s\\((%s(%s%s)*)\\)",patSingleArea, patSingleArea, patSeparator, patSingleArea);
					Pattern patHierarchie = Pattern.compile(hierarchiePattern, Pattern.CASE_INSENSITIVE);
					Matcher matcher = patHierarchie.matcher(label);
					if (matcher.matches()){
						String higherAreaStr = matcher.group(1).trim();
						higherArea =  makeArea(state, higherAreaStr, level);
						String[] innerAreas = matcher.group(2).split(patSeparator);
						for (String innerArea : innerAreas){
							if (isNotBlank(innerArea)){
								NamedArea singleArea = makeArea(state, innerArea.trim(), level);
								areas.add(singleArea);
								NamedArea partOf = singleArea.getPartOf();
//								if (partOf == null){
//									singleArea.setPartOf(higherArea);
//								}
							}
						}
					}else{
						NamedArea singleArea = makeArea(state, label, level);
						areas.add(singleArea);
					}

					for (NamedArea area : areas){
						//create distribution
						Distribution distribution = Distribution.NewInstance(area,status);
						description.addElement(distribution);
					}
				} else {
					String message = "Empty distribution locality";
					fireWarningEvent(message, next, 4);
				}
				return text;
			} else if (isStartingElement(next, COORDINATES)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isEndingElement(next, COORDINATES)) {
				//TODO
				popUnimplemented(next.asEndElement());
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("<DistributionLocality> has no closing tag");
	}


//********************************************** OLD *************************************

//	protected boolean testAdditionalElements(Element parentElement, List<String> excludeList){
//		boolean result = true;
//		List<Element> list = parentElement.getChildren();
//		for (Element element : list){
//			if (! excludeList.contains(element.getName())){
//				logger.warn("Unknown element (" + element.getName() + ") in parent element (" + parentElement.getName() + ")");
//				result = false;
//			}
//		}
//		return result;
//	}
//
//
//	protected <T extends IdentifiableEntity> T makeReferenceType(Element element, Class<? extends T> clazz, MapWrapper<? extends T> objectMap, ResultWrapper<Boolean> success){
//		T result = null;
//		String linkType = element.getAttributeValue("linkType");
//		String ref = element.getAttributeValue("ref");
//		if(ref == null && linkType == null){
//			result = getInstance(clazz);
//			if (result != null){
//				String title = element.getTextNormalize();
//				result.setTitleCache(title, true);
//			}
//		}else if (linkType == null || linkType.equals("local")){
//			//TODO
//			result = objectMap.get(ref);
//			if (result == null){
//				logger.warn("Object (ref = " + ref + ")could not be found in WrapperMap");
//			}
//		}else if(linkType.equals("external")){
//			logger.warn("External link types not yet implemented");
//		}else if(linkType.equals("other")){
//			logger.warn("Other link types not yet implemented");
//		}else{
//			logger.warn("Unknown link type or missing ref");
//		}
//		if (result == null){
//			success.setValue(false);
//		}
//		return result;
//	}
//
//
//	protected Reference makeAccordingTo(Element elAccordingTo, MapWrapper<Reference> referenceMap, ResultWrapper<Boolean> success){
//		Reference result = null;
//		if (elAccordingTo != null){
//			String childName = "AccordingToDetailed";
//			boolean obligatory = false;
//			Element elAccordingToDetailed = XmlHelp.getSingleChildElement(success, elAccordingTo, childName, elAccordingTo.getNamespace(), obligatory);
//
//			childName = "Simple";
//			obligatory = true;
//			Element elSimple = XmlHelp.getSingleChildElement(success, elAccordingTo, childName, elAccordingTo.getNamespace(), obligatory);
//
//			if (elAccordingToDetailed != null){
//				result = makeAccordingToDetailed(elAccordingToDetailed, referenceMap, success);
//			}else{
//				result = ReferenceFactory.newGeneric();
//				String title = elSimple.getTextNormalize();
//				result.setTitleCache(title, true);
//			}
//		}
//		return result;
//	}
//
//
//	private Reference makeAccordingToDetailed(Element elAccordingToDetailed, MapWrapper<Reference> referenceMap, ResultWrapper<Boolean> success){
//		Reference result = null;
//		Namespace tcsNamespace = elAccordingToDetailed.getNamespace();
//		if (elAccordingToDetailed != null){
//			//AuthorTeam
//			String childName = "AuthorTeam";
//			boolean obligatory = false;
//			Element elAuthorTeam = XmlHelp.getSingleChildElement(success, elAccordingToDetailed, childName, tcsNamespace, obligatory);
//			makeAccordingToAuthorTeam(elAuthorTeam, success);
//
//			//PublishedIn
//			childName = "PublishedIn";
//			obligatory = false;
//			Element elPublishedIn = XmlHelp.getSingleChildElement(success, elAccordingToDetailed, childName, tcsNamespace, obligatory);
//			result = makeReferenceType(elPublishedIn, Reference.class, referenceMap, success);
//
//			//MicroReference
//			childName = "MicroReference";
//			obligatory = false;
//			Element elMicroReference = XmlHelp.getSingleChildElement(success, elAccordingToDetailed, childName, tcsNamespace, obligatory);
//			String microReference = elMicroReference.getTextNormalize();
//			if (CdmUtils.Nz(microReference).equals("")){
//				//TODO
//				logger.warn("MicroReference not yet implemented for AccordingToDetailed");
//			}
//		}
//		return result;
//	}
//
//	private Team makeAccordingToAuthorTeam(Element elAuthorTeam, ResultWrapper<Boolean> succes){
//		Team result = null;
//		if (elAuthorTeam != null){
//			//TODO
//			logger.warn("AuthorTeam not yet implemented for AccordingToDetailed");
//		}
//		return result;
//	}



}
