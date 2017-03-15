/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;

/**
 * @author a.mueller
 * @created 11.05.2009
 */
//TODO make it CONFIG extends DBExportConfigurator
public abstract class ExportStateBase<CONFIG extends ExportConfiguratorBase<?,?,TRANSFORM>, TRANSFORM extends IExportTransformer> extends IoStateBase<CONFIG, CdmExportBase> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ExportStateBase.class);

	protected ExportStateBase(CONFIG config){
		this.config = config;
	}

	public TRANSFORM getTransformer(){
		return this.config.getTransformer();
	}

}
