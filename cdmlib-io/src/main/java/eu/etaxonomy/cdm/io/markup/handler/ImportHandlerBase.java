// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.markup.handler;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DefaultHandler2;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.XmlImportBase;
import eu.etaxonomy.cdm.io.common.events.IIoEvent;
import eu.etaxonomy.cdm.io.common.events.IIoObserver;
import eu.etaxonomy.cdm.io.common.events.IoProblemEvent;

/**
 * Base class for XMLSax imports
 * @author a.mueller
 * @date 28.06.2011
 *
 */
public class ImportHandlerBase extends DefaultHandler2 {
	private static final Logger logger = Logger.getLogger(ImportHandlerBase.class);
	
	private Set<IIoObserver> observers = new HashSet<IIoObserver>();
	private XmlImportBase importBase; 
	private Locator locator;
	private boolean stateDocumentStarted = false;
	protected Stack<String> unhandledElements = new Stack();
	

	
	
	protected ImportHandlerBase(XmlImportBase importBase){
		this.importBase = importBase;
	}
	

//******************** Observers *********************************************************	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ICdmIO#addObserver(eu.etaxonomy.cdm.io.common.IIoObserver)
	 */
	public void addObserver(IIoObserver observer){
		observers.add(observer);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ICdmIO#countObservers()
	 */
	public int countObservers(){
		return observers.size();
	}

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.io.common.ICdmIO#deleteObserver(eu.etaxonomy.cdm.io.common.IIoObserver)
     */
	public void deleteObserver(IIoObserver observer){
		observers.remove(observer);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ICdmIO#deleteObservers()
	 */
	public void deleteObservers(){
		observers.removeAll(observers);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ICdmIO#fire(eu.etaxonomy.cdm.io.common.IIoEvent)
	 */
	public void fire(IIoEvent event){
		for (IIoObserver observer: observers){
			observer.handleEvent(event);
		}
	}


//******************** End Observers *********************************************************	
	
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#notationDecl(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void notationDecl(String name, String publicId, String systemId) throws SAXException {
		logger.warn("Unexpected parse event: notationDecl");
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#unparsedEntityDecl(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException {
		logger.warn("Unexpected parse event: unparsedEntityDecl");
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#setDocumentLocator(org.xml.sax.Locator)
	 */
	@Override
	public void setDocumentLocator(Locator locator) {
		if (logger.isDebugEnabled()){
			logger.debug("Set Document Locator");
		}
		this.locator = locator;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startDocument()
	 */
	@Override
	public void startDocument() throws SAXException {
		if (logger.isDebugEnabled()){
			logger.debug("startDocument");
		}
		this.stateDocumentStarted = true;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endDocument()
	 */
	@Override
	public void endDocument() throws SAXException {
		if (logger.isDebugEnabled()){
			logger.debug("endDocument");
		}
		this.stateDocumentStarted = false;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startPrefixMapping(java.lang.String, java.lang.String)
	 */
	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		logger.warn("Unexpected parse event: startPrefixMapping");
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endPrefixMapping(java.lang.String)
	 */
	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		logger.warn("Unexpected parse event: endPrefixMapping");
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (logger.isDebugEnabled()){
			logger.debug("startElement");
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		logger.warn("Unexpected parse event: endElement");
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		logger.warn("Unexpected parse event: characters: " + ch);
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#ignorableWhitespace(char[], int, int)
	 */
	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)  throws SAXException {
		logger.warn("Unexpected parse event: ignorableWhitespace");
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#processingInstruction(java.lang.String, java.lang.String)
	 */
	@Override
	public void processingInstruction(String target, String data) throws SAXException {
		logger.warn("Unexpected parse event: processingInstruction");
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#skippedEntity(java.lang.String)
	 */
	@Override
	public void skippedEntity(String name) throws SAXException {
		logger.warn("Unexpected parse event: skippedEntity");
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#warning(org.xml.sax.SAXParseException)
	 */
	@Override
	public void warning(SAXParseException e) throws SAXException {
		logger.warn("Unexpected parse event: warning");
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException)
	 */
	@Override
	public void error(SAXParseException e) throws SAXException {
		logger.warn("Unexpected parse event: error");
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#fatalError(org.xml.sax.SAXParseException)
	 */
	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		super.fatalError(e);
	}

	public void setImportBase(XmlImportBase importBase) {
		this.importBase = importBase;
	}

	public XmlImportBase getImportBase() {
		return importBase;
	}
	

	protected void fireUnexptectedAttriubtes(Attributes attributes) {
		String attributesString = "";
		for (int i = 0; i < attributes.getLength(); i++){
			attributesString = CdmUtils.concat(",", attributesString, attributes.getQName(i));
		}
		String message = "Unexpected attributes: %s";
		IoProblemEvent event = makeProblemEvent(String.format(message, attributesString), 1 );
		fire(event);	
	}



	protected void fireUnexpectedStartElement(String uri, String localName, String qName) {
		String message = "Unexpected start element: %s";
		IIoEvent event = makeProblemEvent(String.format(message, qName), 2);
		fire(event);		
	}
	

	protected void fireUnexpectedEndElement(String uri, String localName, String qName) {
		String message = "Unexpected end element: %s";
		IIoEvent event = makeProblemEvent(String.format(message, qName), 16);
		fire(event);		
	}
	

	protected void fireNotYetImplementedElement(String uri, String localName, String qName) {
		String message = "Element not yet implement: %s";
		IIoEvent event = makeProblemEvent(String.format(message, qName), 1);
		fire(event);		
	}

	/**
	 * Creates a problem event.
	 * Be aware of the right depths of the stack trace !
	 * @param message
	 * @param severity
	 * @return
	 */
	private IoProblemEvent makeProblemEvent(String message, int severity) {
		StackTraceElement[] stackTrace = new Exception().getStackTrace();
		int lineNumber = stackTrace[2].getLineNumber();
		String methodName = stackTrace[1].getMethodName();
		String location = locator.getLineNumber() + "/"+ locator.getColumnNumber();
		IoProblemEvent event = IoProblemEvent.NewInstance(this.getClass(), message, 
				location, lineNumber, severity, methodName);
		return event;
	}
	
	
	protected void handleUnexpectedStartElement(String uri, String localName, String qName) {
		if (! unhandledElements.empty()){
			unhandledElements.push(qName);
		}else{
			fireUnexpectedStartElement(uri, localName, qName);
		}
		
	}

	protected void handleUnexpectedEndElement(String uri, String localName, String qName) {
		if (unhandledElements.peek().equals(qName)){
			unhandledElements.pop();
		}else{
			fireUnexpectedEndElement(uri, localName, qName);
		}
		
	}

	protected void handleNotYetImplementedElement(String uri, String localName, String qName) {
		unhandledElements.push(qName);
		fireNotYetImplementedElement(uri, localName, qName);
	}

	
}
