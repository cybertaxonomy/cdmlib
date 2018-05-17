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

import org.apache.log4j.Logger;
import org.jdom.Document;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.print.out.PublishOutputModuleBase;

/**
 * @author n.hoffmann
 * @since Aug 4, 2010
 * @version 1.0
 */
public class TaxPubOutputModule extends PublishOutputModuleBase {
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
