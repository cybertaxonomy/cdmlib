/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.excel.stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.io.dwca.in.DwcaDataImportStateBase;
import eu.etaxonomy.cdm.io.stream.excel.ExcelStreamImportConfigurator;

/**
 * @author a.oppermann
 * @since 08.05.2013
 *
 *<ROW extends ExcelRowBase>
 */
public class ExcelStreamImportState extends DwcaDataImportStateBase<ExcelStreamImportConfigurator>{
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	public ExcelStreamImportState(ExcelStreamImportConfigurator config) {
		super(config);
	}
}