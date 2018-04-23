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


/**
 * Sax2 handler for the MarkupImport publication element
 * @author a.mueller
 * @since 28.06.2011
 *
 */
public class TreatmentHandler extends ImportHandlerBase{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TreatmentHandler.class);
	
	private boolean isInTaxon;

	public TreatmentHandler(ImportHandlerBase previousHandler) {
		super(previousHandler);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.markup.handler.ImportHandlerBase#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		//publication
		if ("taxon".equals(qName)){
			this.isInTaxon = true;
			//TODO check attributes
			return;
		}else if (this.isInTaxon == false){
			handleUnexpectedStartElement(uri, localName, qName);
			return;
		}
		//children
		if ("heading".equalsIgnoreCase(qName)){
			handleNotYetImplementedElement(uri, localName, qName);
		}else if ("taxontitle".equalsIgnoreCase(qName)){
			handleNotYetImplementedElement(uri, localName, qName);
		}else if ("writer".equalsIgnoreCase(qName)){
			handleNotYetImplementedElement(uri, localName, qName);
		}else if ("textSection".equalsIgnoreCase(qName)){
			handleNotYetImplementedElement(uri, localName, qName);
		}else if ("key".equalsIgnoreCase(qName)){
			handleNotYetImplementedElement(uri, localName, qName);
		}else if ("nomenclature".equalsIgnoreCase(qName)){
			handleNotYetImplementedElement(uri, localName, qName);
		}else if ("feature".equalsIgnoreCase(qName)){
			handleNotYetImplementedElement(uri, localName, qName);
		}else if ("notes".equalsIgnoreCase(qName)){
			handleNotYetImplementedElement(uri, localName, qName);
		}else if ("references".equalsIgnoreCase(qName)){
			handleNotYetImplementedElement(uri, localName, qName);
		}else if ("figure".equalsIgnoreCase(qName)){
			handleNotYetImplementedElement(uri, localName, qName);
		}else if ("footnote".equalsIgnoreCase(qName)){
			handleNotYetImplementedElement(uri, localName, qName);
		}else{
			handleUnexpectedStartElement(uri, localName, qName);
		}
		if (attributes.getLength() > 0){
			handleUnexpectedAttributes(attributes);
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.markup.handler.ImportHandlerBase#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
//		super.endElement(uri, localName, qName);
		if ("taxon".equalsIgnoreCase(qName)){
			if (this.isInTaxon == true){
				this.isInTaxon = false;
				return;
			}else{
				fireUnexpectedEndElement(uri, localName, qName, 1);
			}
		}else{
			if ("metaData".equalsIgnoreCase(qName)){
				
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
