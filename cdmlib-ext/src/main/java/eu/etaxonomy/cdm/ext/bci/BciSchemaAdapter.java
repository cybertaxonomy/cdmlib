/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.bci;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import eu.etaxonomy.cdm.ext.common.SchemaAdapterBase;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;



/**
 * @author a.kohlbecker
 * @since 25.08.2010
 */
public class BciSchemaAdapter extends SchemaAdapterBase<Reference>{



	static URI identifier = null;

	static String nameSpace = "http://purl.org/dc/elements/1.1/";

	static {
		try {
			identifier = new URI("info:srw/schema/1/dc-v1.1");
		} catch (URISyntaxException e) {
			// should never happen
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.ext.schema.SchemaAdapter#getIdentifier()
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
		return "dc";
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.ext.schema.SchemaAdapter#getCmdEntities(java.io.Reader)
	 */
	@Override
	public List<Reference> getCmdEntities(InputStream inputStream) {

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


		DcSaxHandler handler = new DcSaxHandler();

	    try {
	    	if(parser != null){
	    		parser.parse(inputStream, handler);
	    	} else {
	    		logger.error("parser is null");
	    	}
		} catch (SAXException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}


		return handler.referenceList;
	}

	class DcSaxHandler extends DefaultHandler {

		private static final String DC_DC = "dc:dc";

		private static final String DC_TITLE = "dc:title";
		private static final String DC_CREATOR = "dc:creator";
		private static final String DC_PUBLISHER = "dc:publisher";
		private static final String DC_DATE = "dc:date";

		List<Reference> referenceList = new ArrayList<Reference>();

		Reference reference = null;

		String dcFieldName = null;



		@Override
		public void startElement(String uri, String localName,
				String qName, Attributes attributes) throws SAXException {

			if (uri.equals(nameSpace)) {
				logger.debug("Start Element :" + qName + "; " + uri);

				if (qName.equals(DC_DC)) {
					reference = ReferenceFactory.newGeneric();
				} else {
					dcFieldName = qName;
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {

			if (uri.equals(nameSpace)) {
				if(reference != null) {
					logger.debug("End Element :" + qName + "; " + uri);

					if (qName.equals(DC_DC)) {
						referenceList.add(reference);
						reference = null;
					} else {
						dcFieldName = null;
					}
				}
			}

		}

		@Override
		public void characters(char ch[], int start, int length)
				throws SAXException {

			if(reference != null && dcFieldName != null){
				String text = new String(ch, start, length);
				logger.debug("Characters : " + text);
				if(dcFieldName.equals(DC_TITLE)){
					reference.setTitleCache(text, true);
				}
				if(dcFieldName.equals(DC_DATE)){
					reference.setDatePublished(TimePeriodParser.parseString(text));
				}
				if(dcFieldName.equals(DC_PUBLISHER)){
					reference.setPublisher(text);
				}
				if(dcFieldName.equals(DC_CREATOR)){
					TeamOrPersonBase authorship = new Team();
					authorship.setTitleCache(text, true);
					reference.setAuthorship(authorship);
				}

			}
		}

	}

}
