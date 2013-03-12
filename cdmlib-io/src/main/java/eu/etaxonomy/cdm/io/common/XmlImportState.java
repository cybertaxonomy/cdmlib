// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 * @created 11.05.2009
 * @version 1.0
 */
public class XmlImportState<CONFIG extends XmlImportConfiguratorBase, IO extends XmlImportBase> extends ImportStateBase<CONFIG, IO> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(XmlImportState.class);

	private XMLEventReader reader;

	
	public XmlImportState(CONFIG config) {
		super(config);
	}

	

	/**
	 * @return the reader
	 */
	public XMLEventReader getReader() {
		return reader;
	}

	public void setReader(XMLEventReader reader) {
		this.reader = reader;
		
	}



	

}
