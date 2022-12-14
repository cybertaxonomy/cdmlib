/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.reference.endnote.in;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.Element;

import eu.etaxonomy.cdm.io.tcsxml.ITcsXmlPlaceholderClass;
import eu.etaxonomy.cdm.io.tcsxml.in.TcsXmlImportConfigurator;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @since 30.10.2008
 */
public class IEndnotePlaceholderClass implements ITcsXmlPlaceholderClass {

    private static final Logger logger = LogManager.getLogger();

	@Override
    public boolean makeMetaDataDetailed(TcsXmlImportConfigurator tcsConfig, Element elMetaDataDetailed){
		if (tcsConfig == null){
			return false;
		}
		if (elMetaDataDetailed == null){
			return true;
		}

		//Do nothing
		//TODO implement EDIT TcsMetaData extension
		logger.info("MetaDataElement found: " +  elMetaDataDetailed.getName());
		return true;
	}

	@Override
    public boolean makePublicationDetailed(TcsXmlImportConfigurator tcsConfig, Element elPublicationDetailed, Reference publication){
		if (tcsConfig == null){
			return false;
		}
		if (elPublicationDetailed == null){
			return true;
		}

		//Do nothing
		//TODO implement EDIT TcsMetaData extension
		logger.info("PublicationDetailed element found: " +  elPublicationDetailed.getName());
		return true;
	}
}