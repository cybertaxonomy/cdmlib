/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.markup.handler;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import eu.etaxonomy.cdm.io.common.ImportHandlerBase;
import eu.etaxonomy.cdm.io.common.XmlImportBase;



/**
 * Sax2 handler for the MarkupImport publication element
 * @author a.mueller
 \* @since 28.06.2011
 *
 */
public class PublicationHandler extends ImportHandlerBase{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PublicationHandler.class);
	
	private boolean isInPublication;
	private ImportHandlerBase currentHandler = null;
	
	public PublicationHandler(XmlImportBase importBase) {
		super(importBase);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.markup.handler.ImportHandlerBase#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (currentHandler != null){
			currentHandler.startElement(uri, localName, qName, attributes);
		}else{
			//publication
			if ("publication".equalsIgnoreCase(qName)){
				this.isInPublication = true;
				//TODO check attributes
				return;
			}else if (this.isInPublication == false){
				handleUnexpectedStartElement(uri, localName, qName);
				return;
			}
			//children
			if ("metaData".equalsIgnoreCase(qName)){
				handleNotYetImplementedElement(uri, localName, qName);
			}else if ("treatment".equalsIgnoreCase(qName)){
				currentHandler = new TreatmentHandler(this);
			}else if ("biographies".equalsIgnoreCase(qName)){
				handleNotYetImplementedElement(uri, localName, qName);
			}else if ("references".equalsIgnoreCase(qName)){
				handleNotYetImplementedElement(uri, localName, qName);
			}else if ("textSection".equalsIgnoreCase(qName)){
				handleNotYetImplementedElement(uri, localName, qName);
			}else if ("addenda".equalsIgnoreCase(qName)){
				handleNotYetImplementedElement(uri, localName, qName);
			}else{
				handleUnexpectedStartElement(uri, localName, qName);
			}
			if (attributes.getLength() > 0){
				handleUnexpectedAttributes(attributes);
			}
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.markup.handler.ImportHandlerBase#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
//		super.endElement(uri, localName, qName);
		if (currentHandler != null){
			currentHandler.endElement(uri, localName, qName);
		}else{
			if ("publication".equalsIgnoreCase(qName)){
				if (this.isInPublication == true){
					this.isInPublication = false;
					return;
				}else{
					fireUnexpectedEndElement(uri, localName, qName, 1);
				}
			}else{
				if ("metaData".equalsIgnoreCase(qName)){
					System.out.println("Hallo");
				}else if ("treatment".equalsIgnoreCase(qName)){
					
					
				}else if ("biographies".equalsIgnoreCase(qName)){
					
				}else if ("references".equalsIgnoreCase(qName)){
				}else if ("textSection".equalsIgnoreCase(qName)){
				}else if ("addenda".equalsIgnoreCase(qName)){
				}else{
					handleUnexpectedEndElement(uri, localName, qName);
				}
			}
		}
	}
	
	


}
