/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.reference.endnote.in;

import org.apache.log4j.Logger;
import org.jdom.Element;

import eu.etaxonomy.cdm.io.common.CdmImportBase;

/**
 * @author a.mueller
 * @since 04.08.2008
 */
public abstract class EndNoteImportBase
        extends CdmImportBase<EndnoteImportConfigurator, EndnoteImportState> {

    private static final long serialVersionUID = 6644348267081220104L;
    private static final Logger logger = Logger.getLogger(EndNoteImportBase.class);

	protected Element getXmlElement(EndnoteImportConfigurator tcsConfig){
		Element root = tcsConfig.getSourceRoot();

		if (! "xml".equals(root.getName())){
			logger.error("Root element is not 'xml'");
			return null;
		}
		if (tcsConfig.getEndnoteNamespace() == null){
			logger.error("No namespace defined for tcs");
			return null;
		}
		if (! tcsConfig.getEndnoteNamespace().equals(root.getNamespace())){
			logger.error("Wrong namespace for element 'xml'");
			return null;
		}
		//TODO prevent multiple elements

		return root;
	}
}
