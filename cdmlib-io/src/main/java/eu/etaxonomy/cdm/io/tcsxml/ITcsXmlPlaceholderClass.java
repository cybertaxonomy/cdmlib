/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.io.tcsxml;

import org.jdom.Element;

import eu.etaxonomy.cdm.io.tcsxml.in.TcsXmlImportConfigurator;
import eu.etaxonomy.cdm.model.reference.Reference;

public interface ITcsXmlPlaceholderClass {

	public abstract boolean makeMetaDataDetailed(
			TcsXmlImportConfigurator tcsConfig, Element elMetaDataDetailed);

	public abstract boolean makePublicationDetailed(TcsXmlImportConfigurator tcsConfig, 
			Element elPublicationDetailed, Reference publication);

}
