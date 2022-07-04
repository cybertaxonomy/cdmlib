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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author a.mueller
 * @since 11.05.2009
 */
public class XmlImportState<CONFIG extends XmlImportConfiguratorBase, IO extends XmlImportBase>
        extends ImportStateBase<CONFIG, IO> {

    @SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(XmlImportState.class);

	private XMLEventReader reader;

	public XmlImportState(CONFIG config) {
		super(config);
	}

	public XMLEventReader getReader() {
		return reader;
	}
	public void setReader(XMLEventReader reader) {
		this.reader = reader;
	}
}