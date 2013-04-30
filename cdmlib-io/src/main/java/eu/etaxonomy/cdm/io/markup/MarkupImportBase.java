/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.markup;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.CdmImportBase.TermMatchMode;
import eu.etaxonomy.cdm.io.common.events.IIoEvent;
import eu.etaxonomy.cdm.io.common.events.IoProblemEvent;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @created 04.08.2008
 */
public abstract class MarkupImportBase  {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MarkupImportBase.class);
	
	protected static final String CLASS = "class";
	protected static final String NUM = "num";

	protected MarkupDocumentImport docImport;
	
	public MarkupImportBase(MarkupDocumentImport docImport) {
		super();
		this.docImport = docImport;
	}

	private Stack<QName> unhandledElements = new Stack<QName>();
	private Stack<QName> handledElements = new Stack<QName>();


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
	


	protected void save(CdmBase cdmBase, MarkupImportState state) {
		if (state.isCheck()){
			return;
		}
		if (cdmBase.isInstanceOf(TaxonBase.class)){
			TaxonBase<?> taxonBase = CdmBase.deproxy(cdmBase, TaxonBase.class);
			docImport.getTaxonService().saveOrUpdate(taxonBase);
		}else if (cdmBase.isInstanceOf(Classification.class)){
			Classification classification = CdmBase.deproxy(cdmBase, Classification.class);
			docImport.getClassificationService().saveOrUpdate(classification);
		}else if (cdmBase.isInstanceOf(PolytomousKey.class)){
			PolytomousKey key = CdmBase.deproxy(cdmBase, PolytomousKey.class);
			docImport.getPolytomousKeyService().saveOrUpdate(key);
		}else if (cdmBase.isInstanceOf(DefinedTermBase.class)){
			DefinedTermBase<?> term = CdmBase.deproxy(cdmBase, DefinedTermBase.class);
			docImport.getTermService().saveOrUpdate(term);
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
		IoProblemEvent event = IoProblemEvent.NewInstance(this.getClass(), message, 
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
		QName qName = event.asStartElement().getName();
		if (! unhandledElements.empty()){
			unhandledElements.push(qName);
		}else{
			fireUnexpectedStartElement(event.getLocation(), event.asStartElement(), 1);
		}	
	}

	/**
	 * Fires an unexpected element event if the event is not the last on the stack.
	 * Otherwise removes last stack element.
	 * @param event
	 */
	protected void handleUnexpectedEndElement(EndElement event) {
		QName qName = event.asEndElement().getName();
		if (!unhandledElements.isEmpty() && unhandledElements.peek().equals(qName)){
			unhandledElements.pop();
		}else{
			fireUnexpectedEndElement(event.getLocation(), event.asEndElement(), 1);
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
			handleUnexpectedStartElement(event);
		}else if (event.isEndElement()){
			handleUnexpectedEndElement(event.asEndElement());
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
	protected void handleNotYetImplementedElement(XMLEvent event) {
		QName qName = event.asStartElement().getName();
		boolean isTopLevel = unhandledElements.isEmpty();
		unhandledElements.push(qName);
		if (isTopLevel){
			fireNotYetImplementedElement(event.getLocation(), qName, 1);
		}
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

	

	protected void fireWarningEvent(String message, String makeLocationStr, int severity) {
		docImport.fireWarningEvent(message, makeLocationStr, severity);	
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

	public TaxonDescription getTaxonDescription(Taxon taxon, Reference ref, boolean isImageGallery, boolean createNewIfNotExists) {
		return docImport.getTaxonDescription(taxon, isImageGallery, createNewIfNotExists);	
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
	
	protected AnnotationType getAnnotationType(MarkupImportState state, UUID uuid, String label, String text, String labelAbbrev, TermVocabulary<AnnotationType> voc){
		return docImport.getAnnotationType(state, uuid, label, text, labelAbbrev, voc);
	}
	
	protected NamedAreaLevel getNamedAreaLevel(MarkupImportState state, UUID uuid, String label, String text, String labelAbbrev, TermVocabulary<NamedAreaLevel> voc){
		return docImport.getNamedAreaLevel(state, uuid, label, text, labelAbbrev, voc);
	}
	
	protected NamedArea getNamedArea(MarkupImportState state, UUID uuid, String label, String text, String labelAbbrev, NamedAreaType areaType, NamedAreaLevel level, TermVocabulary voc, TermMatchMode matchMode){
		return docImport.getNamedArea(state, uuid, label, text, labelAbbrev, areaType, level, voc, matchMode);
	}
	
	protected Language getLanguage(MarkupImportState state, UUID uuid, String label, String text, String labelAbbrev, TermVocabulary voc){
		return docImport.getLanguage(state, uuid, label, text, labelAbbrev, voc);
	}
	
// *************************************** Concrete methods **********************************************/
	
	
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
	 * @return
	 * @throws XMLStreamException
	 */
	protected String getCData(MarkupImportState state, XMLEventReader reader, XMLEvent next,boolean checkAttributes) throws XMLStreamException {
		if (checkAttributes){
			checkNoAttributes(next);
		}

		String text = "";
		while (reader.hasNext()) {
			XMLEvent myNext = readNoWhitespace(reader);
			if (isMyEndingElement(myNext, next)) {
				return text;
			} else if (myNext.isCharacters()) {
				text += myNext.asCharacters().getData();
			} else {
				handleUnexpectedElement(myNext);
			}
		}
		throw new IllegalStateException("Event has no closing tag");

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
