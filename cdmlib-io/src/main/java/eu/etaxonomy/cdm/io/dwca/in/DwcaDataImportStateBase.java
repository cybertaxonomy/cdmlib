/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.dwca.in;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.stream.StreamImportBase;
import eu.etaxonomy.cdm.io.stream.StreamImportStateBase;

/**
 * @author a.mueller
 * @since 23.11.2011
 */
public abstract class DwcaDataImportStateBase<CONFIG extends DwcaDataImportConfiguratorBase>
        extends StreamImportStateBase<CONFIG, StreamImportBase>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaDataImportStateBase.class);

	public DwcaDataImportStateBase(CONFIG config) {
		super(config);
	}


}
