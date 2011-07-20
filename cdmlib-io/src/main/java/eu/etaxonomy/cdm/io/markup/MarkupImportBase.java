/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.markup;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.XmlImportBase;
import eu.etaxonomy.cdm.io.common.events.IIoEvent;
import eu.etaxonomy.cdm.io.common.events.IoProblemEvent;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @created 04.08.2008
 * @version 1.0
 */
public abstract class MarkupImportBase  extends XmlImportBase<MarkupImportConfigurator, MarkupImportState> {
	private static final Logger logger = Logger.getLogger(MarkupImportBase.class);

	
	protected abstract void doInvoke(MarkupImportState state);

	protected Stack<QName> unhandledElements = new Stack<QName>();
	protected Stack<QName> handledElements = new Stack<QName>();

	

	protected Map<String, Attribute> getAttributes(StartElement element) {
		Map<String, Attribute> result = new HashMap<String, Attribute>();
		Iterator<Attribute> attributes = element.getAttributes();
		while (attributes.hasNext()){
			Attribute attribute = attributes.next();
			//TODO namespaces
			result.put(attribute.getName().getLocalPart(), attribute);
		}
		return result;
	}
	
	protected void handleUnexpectedAttributes(Location location, StartElement startElement) {
//		attributes = startElement.getAttributes();
//		if (this.unhandledElements.size() == 0 ){
//			fireUnexpectedAttributes(location, attributes, 1);
//		}
	}
	
	protected void handleUnexpectedAttributes(Location location,Map<String, Attribute> attributes) {
		if (this.unhandledElements.size() == 0 ){
			fireUnexpectedAttributes(location, attributes, 1);
		}
	}
	
	protected void fireUnexpectedAttributes(Location location, Map<String, Attribute> attributes, int stackDepth) {
		String attributesString = "";
//		while (attributes.hasNext()){
//			Object next = attributes.next();
//			attributesString = CdmUtils.concat(",", attributesString, next.toString());
//		}
		
//		for (int i = 0; i < attributes.getLength(); i++){
//			attributesString = CdmUtils.concat(",", attributesString, attributes.getQName(i));
//		}
		String message = "Unexpected attributes: %s";
		IoProblemEvent event = makeProblemEvent(location, String.format(message, attributesString), 1 , stackDepth +1 );
		fire(event);	
	}


	protected void fireUnexpectedEvent(XMLEvent xmlEvent, int stackDepth) {
		Location location = xmlEvent.getLocation();
		String message = "Unexpected event: %s";
		IIoEvent event = makeProblemEvent(location, String.format(message, xmlEvent.toString()), 2, stackDepth +1);
		fire(event);		
	}

	protected void fireUnexpectedStartElement(Location location, QName qName, int stackDepth) {
		String message = "Unexpected start element: %s";
		IIoEvent event = makeProblemEvent(location, String.format(message, qName.getLocalPart()), 2, stackDepth +1);
		fire(event);		
	}
	

	protected void fireUnexpectedEndElement(Location location, QName qName, int stackDepth) {
		String message = "Unexpected end element: %s";
		IIoEvent event = makeProblemEvent(location, String.format(message, qName.getLocalPart()), 16, stackDepth+1);
		fire(event);		
	}
	

	protected void fireNotYetImplementedElement(Location location, QName qName, int stackDepth) {
		String message = "Element not yet implement: %s";
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
	 * @param location
	 * @return
	 */
	private String makeLocationStr(Location location) {
		String locationStr = location == null ? " - no location - " : "l." + location.getLineNumber() + "/c."+ location.getColumnNumber();
		return locationStr;
	}
	
	
	protected void handleUnexpectedStartElement(XMLEvent event) {
		QName qName = event.asStartElement().getName();
		if (! unhandledElements.empty()){
			unhandledElements.push(qName);
		}else{
			fireUnexpectedStartElement(event.getLocation(), qName, 1);
		}	
	}

	protected void handleUnexpectedEndElement(XMLEvent event) {
		QName qName = event.asEndElement().getName();
		if (unhandledElements.peek().equals(qName)){
			unhandledElements.pop();
		}else{
			fireUnexpectedEndElement(event.getLocation(), qName, 1);
		}
	}
	
	
	protected void handleUnexpectedElement(XMLEvent event) {
		if (event.isStartElement()){
			handleUnexpectedStartElement(event);
		}else if (event.isEndElement()){
			handleUnexpectedEndElement(event);
		}else if (! unhandledElements.empty()){
			//do nothing
		}else{
			fireUnexpectedEvent(event, 1);
		}	
	}

	protected void handleNotYetImplementedElement(XMLEvent event) {
		QName qName = event.asStartElement().getName();
		unhandledElements.push(qName);
		fireNotYetImplementedElement(event.getLocation(), qName, 1);
	}
	
	protected void handleMandatoryElement(boolean hasMandatory, StartElement parentEvent, String string) {
		if (! hasMandatory){
			fireMandatoryElementIsMissing(parentEvent, string, 5, 1);
		}
		
	}

	
	private void fireMandatoryElementIsMissing(StartElement parentEvent, String string, int severity, int stackDepth) {
		Location location = parentEvent.getLocation();
		String message = "Mandatory element '%s' is missing in ";
		IIoEvent event = makeProblemEvent(location, String.format(message, parentEvent.getName().getLocalPart()), severity, stackDepth +1);
		fire(event);		

	}


	
	
//********************************************** OLD *************************************	

	protected boolean testAdditionalElements(Element parentElement, List<String> excludeList){
		boolean result = true;
		List<Element> list = parentElement.getChildren();
		for (Element element : list){
			if (! excludeList.contains(element.getName())){
				logger.warn("Unknown element (" + element.getName() + ") in parent element (" + parentElement.getName() + ")");
				result = false;
			}
		}
		return result;
	}
	
	
	protected <T extends IdentifiableEntity> T makeReferenceType(Element element, Class<? extends T> clazz, MapWrapper<? extends T> objectMap, ResultWrapper<Boolean> success){
		T result = null;
		String linkType = element.getAttributeValue("linkType");
		String ref = element.getAttributeValue("ref");
		if(ref == null && linkType == null){
			result = getInstance(clazz);
			if (result != null){
				String title = element.getTextNormalize();
				result.setTitleCache(title, true);
			}
		}else if (linkType == null || linkType.equals("local")){
			//TODO
			result = objectMap.get(ref);
			if (result == null){
				logger.warn("Object (ref = " + ref + ")could not be found in WrapperMap");
			}
		}else if(linkType.equals("external")){
			logger.warn("External link types not yet implemented");
		}else if(linkType.equals("other")){
			logger.warn("Other link types not yet implemented");
		}else{
			logger.warn("Unknown link type or missing ref");
		}
		if (result == null){
			success.setValue(false);
		}
		return result;
	}
	
	
	protected Reference makeAccordingTo(Element elAccordingTo, MapWrapper<Reference> referenceMap, ResultWrapper<Boolean> success){
		Reference result = null;
		if (elAccordingTo != null){
			String childName = "AccordingToDetailed";
			boolean obligatory = false;
			Element elAccordingToDetailed = XmlHelp.getSingleChildElement(success, elAccordingTo, childName, elAccordingTo.getNamespace(), obligatory);

			childName = "Simple";
			obligatory = true;
			Element elSimple = XmlHelp.getSingleChildElement(success, elAccordingTo, childName, elAccordingTo.getNamespace(), obligatory);
			
			if (elAccordingToDetailed != null){
				result = makeAccordingToDetailed(elAccordingToDetailed, referenceMap, success);
			}else{
				result = ReferenceFactory.newGeneric();
				String title = elSimple.getTextNormalize();
				result.setTitleCache(title, true);
			}
		}
		return result;
	}
	
	
	private Reference makeAccordingToDetailed(Element elAccordingToDetailed, MapWrapper<Reference> referenceMap, ResultWrapper<Boolean> success){
		Reference result = null;
		Namespace tcsNamespace = elAccordingToDetailed.getNamespace();
		if (elAccordingToDetailed != null){
			//AuthorTeam
			String childName = "AuthorTeam";
			boolean obligatory = false;
			Element elAuthorTeam = XmlHelp.getSingleChildElement(success, elAccordingToDetailed, childName, tcsNamespace, obligatory);
			makeAccordingToAuthorTeam(elAuthorTeam, success);
			
			//PublishedIn
			childName = "PublishedIn";
			obligatory = false;
			Element elPublishedIn = XmlHelp.getSingleChildElement(success, elAccordingToDetailed, childName, tcsNamespace, obligatory);
			result = makeReferenceType(elPublishedIn, Reference.class, referenceMap, success);
			
			//MicroReference
			childName = "MicroReference";
			obligatory = false;
			Element elMicroReference = XmlHelp.getSingleChildElement(success, elAccordingToDetailed, childName, tcsNamespace, obligatory);
			String microReference = elMicroReference.getTextNormalize();
			if (CdmUtils.Nz(microReference).equals("")){
				//TODO
				logger.warn("MicroReference not yet implemented for AccordingToDetailed");	
			}
		}
		return result;
	}

	private Team makeAccordingToAuthorTeam(Element elAuthorTeam, ResultWrapper<Boolean> succes){
		Team result = null;
		if (elAuthorTeam != null){
			//TODO
			logger.warn("AuthorTeam not yet implemented for AccordingToDetailed");
		}
		return result;
	}



}
