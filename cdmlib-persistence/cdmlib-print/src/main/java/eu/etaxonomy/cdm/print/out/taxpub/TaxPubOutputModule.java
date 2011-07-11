// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.print.out.taxpub;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;

import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.print.out.AbstractPublishOutputModule;

/**
 * @author n.hoffmann
 * @created Aug 4, 2010
 * @version 1.0
 */
public class TaxPubOutputModule extends AbstractPublishOutputModule {
	private static final Logger logger = Logger
			.getLogger(TaxPubOutputModule.class);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.printpublisher.out.IPublishOutputModule#getOutputFileSuffix()
	 */
	public String getOutputFileSuffix() {
		return "taxpub.xml";
	}
	
	@Override
	public void output(Document document, File exportFolder,
			IProgressMonitor progressMonitor) {
		super.output(document, exportFolder, progressMonitor);
		
		progressMonitor.subTask("Not implemented yet");
	}
}
