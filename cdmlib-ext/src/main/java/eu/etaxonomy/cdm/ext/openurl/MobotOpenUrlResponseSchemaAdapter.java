/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.openurl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import eu.etaxonomy.cdm.ext.common.SchemaAdapterBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.Reference;



/**
 * @author a.kohlbecker
 * @since 25.08.2010
 */
public class MobotOpenUrlResponseSchemaAdapter extends SchemaAdapterBase<Reference>{

	static URI identifier = null;

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.ext.common.SchemaAdapterBase#getIdentifier()
	 */
	@Override
	public URI getIdentifier() {
		return identifier;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.ext.schema.SchemaAdapter#getShortName()
	 */
	@Override
	public String getShortName() {
		return "MOBOT.OpenUrl.Utilities.OpenUrlResponse";
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.ext.schema.SchemaAdapter#getCmdEntities(java.io.Reader)
	 */
	@Override
	public List<Reference> getCmdEntities(InputStream inputStream) throws IOException {

		SAXParserFactory factory = SAXParserFactory.newInstance();
	    factory.setNamespaceAware(true);
	    SAXParser parser = null;
		try {
			parser = factory.newSAXParser();
		} catch (ParserConfigurationException e) {
			logger.error(e);
		} catch (SAXException e) {
			logger.error(e);
		}

	    
		OpenUrlResponseHandler handler = new OpenUrlResponseHandler();
	    
	    try {
	    	if(parser != null){
	    		Reader reader = new InputStreamReader(inputStream, "UTF-8");
	    		InputSource inputSource = new InputSource(reader);
	    		parser.parse(inputSource, handler);
	    		if(handler.status != ResponseStatus.Success){
	    			throw new IOException("MOBOT.OpenUrl.Utilities.OpenUrlResponse - Status:" + handler.status.toString() + (handler.message != null ? handler.message : ""));
	    		}
	    	} else {
	    		logger.error("parser is null");
	    	}
		} catch (SAXException e) {
			logger.error(e);
		} 

		
		return handler.referenceList;
	}
	
	class OpenUrlResponseHandler extends DefaultHandler {
		
		/*
		 * Fields of OpenUrlResponse
		 *  see http://code.google.com/p/bhl-bits/source/browse/trunk/portal/OpenUrlUtilities/OpenUrlResponse.cs
		 */
		private static final String OPENURL_RESPONSE = "OpenUrlResponse";
		private static final String STATUS = "Status";
		private static final String MESSAGE = "Message";
		private static final String CITATIONS = "citations";
		private static final String OPENURL_RESPONSE_CITATION = "OpenUrlResponseCitation";
		
		/*
		 * Fields of OpenUrlResponseCitation
		 *  see http://code.google.com/p/bhl-bits/source/browse/trunk/portal/OpenUrlUtilities/OpenUrlResponseCitation.cs
		 */
		
		/**
		 * references the specific page in the title
		 */
		private static final String URL = "Url";
		/**
		 * references the according entry in the bibliography
		 */
		private static final String ITEM_URL = "ItemUrl";

		/**
		 * references the specific book or journal, that is to the front page
		 */
		private static final String TITLE_URL = "TitleUrl";
		private static final String TITLE = "Title";
		private static final String STITLE = "STitle";
		/**
		 * seems to contain the type of the reference : book
		 */
		private static final String GENRE = "Genre";
		private static final String AUTHORS = "Authors";
		private static final String SUBJECTS = "Subjects";
		private static final String PUBLISHER_NAME = "PublisherName";
		private static final String PUBLISHER_PLACE = "PublisherPlace";
		private static final String DATE = "Date";
		private static final String VOLUME = "Volume";
		private static final String EDITION = "Edition";
		private static final String PUBLICATION_FREQUENCY = "PublicationFrequency";
		private static final String LANGUAGE = "Language";
		private static final String OCLC = "Oclc";
		private static final String LCCN = "Lccn";
		private static final String ISSN = "Issn";
		private static final String ATITLE = "ATitle";
		private static final String SPAGE = "SPage";
		private static final String EPAGE = "EPage";
		private static final String PAGES = "Pages";
		
	
		List<Reference> referenceList = new ArrayList<Reference>();

		OpenUrlReference reference = null;
		
		ResponseStatus status = null;
		Team authorship = null;
		String message = null;
		
		String elementName = null;
		private String elementNameToStore;
		private StringBuilder textBuffer = new StringBuilder();
		

		@Override
		public void startElement(String uri, String localName, 
				String qName, Attributes attributes) throws SAXException {

			if (qName.equals(OPENURL_RESPONSE)) {
				logger.debug("Start " + OPENURL_RESPONSE + "; ");
				status = ResponseStatus.Undefined; // indicates that the OPENURL_RESPONSE element has ben detected
			} else if (status != null && qName.equals(OPENURL_RESPONSE_CITATION)) {
				reference = new OpenUrlReference();
			} else if (reference != null && qName.equals(AUTHORS)) {
				authorship = Team.NewInstance();
			} else if (reference != null && qName.equals(SUBJECTS)) {
				//TODO implement, but no equivalent in the cdm model			
			} else {
				elementName = qName;
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {

			if (qName.equals(OPENURL_RESPONSE)) {
				
			} else if (qName.equals(OPENURL_RESPONSE_CITATION)) {
				referenceList.add(reference);
				reference = null;
			} else if (reference != null && qName.equals(AUTHORS)) {
				reference.setAuthorship(authorship);
				authorship = null;
			} else if (reference != null && qName.equals(SUBJECTS)) {
				//TODO implement, but no equivalent in the cdm model		
			}else {
				elementNameToStore = elementName;
				elementName = null;
			}

		}

		@Override
		public void characters(char ch[], int start, int length)
				throws SAXException {
			
			if(elementNameToStore  == null){
				
				textBuffer.append(new String(ch, start, length));
				
			} else {
				
				logger.debug("Characters [" + elementNameToStore + "]: " + textBuffer);
				String trimmedText = textBuffer.toString().trim();
				// empty the text buffer
				textBuffer.delete(0, textBuffer.length());
				
				// --- Reference --- //  
				if(reference != null){
					
					if(elementNameToStore.equals(URL)){
						try {
							reference.setUri(new URI(trimmedText));
						} catch (URISyntaxException e) {
							logger.warn(e.getMessage());
						}
					}
					if(elementNameToStore.equals(ITEM_URL)){
						try {
							reference.setItemUri(new URI(trimmedText));
						} catch (URISyntaxException e) {
							logger.warn(e.getMessage());
						}
					}
					if(elementNameToStore.equals(TITLE_URL)){
						try {
							reference.setTitleUri(new URI(trimmedText));
						} catch (URISyntaxException e) {
							logger.warn(e.getMessage());
						}
					}
					if(elementNameToStore.equals(TITLE)){
						reference.setTitleCache(trimmedText, true);
					}
					if(elementNameToStore.equals(STITLE)){
						logger.debug(elementNameToStore + " not yet implemented!");//TODO
					}
					if(elementNameToStore.equals(ATITLE)){
						logger.debug(elementNameToStore + " not yet implemented!");//TODO
					}
					if(elementNameToStore.equals(PUBLISHER_NAME)){
						reference.setPublisher(trimmedText);
					}
					if(elementNameToStore.equals(PUBLISHER_PLACE)){
						reference.setPlacePublished(trimmedText);
					}
					if(elementNameToStore.equals(DATE)){
						/* may be a single year or a range of years 1797-1830 */
						Integer startYear = null;
						Integer endYear = null;
						if(trimmedText.length() == 9 && trimmedText.indexOf("-") == 4){
							try {
								startYear = Integer.valueOf(trimmedText.substring(0, 4));
								endYear = Integer.valueOf(trimmedText.substring(5));
								reference.setDatePublished(TimePeriod.NewInstance(startYear, endYear));
							} catch (NumberFormatException e) {	
								logger.error("date can not be parsed: "+ trimmedText);
							}
						} else if(trimmedText.length() == 4) {
							try {
								startYear = Integer.valueOf(trimmedText);
							} catch (NumberFormatException e) {
								logger.error("date can not be parsed: "+ trimmedText);
							}
							reference.setDatePublished(TimePeriod.NewInstance(startYear));
						}
					}
					if(elementNameToStore.equals(VOLUME)){
						reference.setVolume(trimmedText);
					}
					if(elementNameToStore.equals(EDITION)){
						reference.setEdition(trimmedText);
					}
					if(elementNameToStore.equals(SPAGE)){
						reference.setPages(trimmedText);
					}
					if(elementNameToStore.equals(EPAGE)){
						logger.debug(elementNameToStore + " not yet implemented!");//TODO
					}
					if(elementNameToStore.equals(PAGES)){
						// IGNORE we rather need the start page value SPAGE
					}
					if(elementNameToStore.equals(PUBLICATION_FREQUENCY)){
						logger.debug(elementNameToStore + " not yet implemented!");//TODO
					}
					if(elementNameToStore.equals(LANGUAGE)){
						logger.debug(elementNameToStore + " not yet implemented!");//TODO
					}
					if(elementNameToStore.equals(OCLC)){
						logger.debug(elementNameToStore + " not yet implemented!");//TODO
					}
					if(elementNameToStore.equals(LCCN)){
						logger.debug(elementNameToStore + " not yet implemented!");//TODO
					}
					if(elementNameToStore.equals(ISSN)){
						reference.setIssn(trimmedText);
					}
				}
				
				// --- Reference.authorship --- //
				if(authorship != null && reference != null){
					if(elementNameToStore.equals("String")){
						authorship.addTeamMember(Person.NewTitledInstance(trimmedText));
					}
				}
				
				// openUrlResponse // 
				if(reference == null){
					if(elementNameToStore.equals(STATUS)){
						status = ResponseStatus.valueOf(trimmedText);
					}
				}

				elementNameToStore = null;
			}
		}
		
	}
	
	 /**
	 * @see http://code.google.com/p/bhl-bits/source/browse/trunk/portal/OpenUrlUtilities/IOpenUrlResponse.cs
	 */
	public enum ResponseStatus {
		Undefined, // Query not submitted
		Success, Error
	}

	
}
