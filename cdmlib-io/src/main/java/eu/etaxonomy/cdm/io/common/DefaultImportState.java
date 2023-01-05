/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author a.mueller
 * @since 23.06.2009
 */
public class DefaultImportState<CONFIG extends ImportConfiguratorBase>
        extends ImportStateBase<CONFIG, CdmImportBase> {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	public DefaultImportState(CONFIG config) {
		super(config);
	}
}