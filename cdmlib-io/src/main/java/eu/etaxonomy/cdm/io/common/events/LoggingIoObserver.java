/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This IoObserver logs all events.
 *
 * @author a.mueller
 * @since 24.06.2011
 */
public class LoggingIoObserver implements IIoObserver {

	private static final Logger logger = LogManager.getLogger();

	@Override
	public void handleEvent(IIoEvent event) {
		logger.warn(event);
	}
}