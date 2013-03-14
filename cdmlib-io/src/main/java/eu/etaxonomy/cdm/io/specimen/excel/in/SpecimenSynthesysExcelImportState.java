// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.specimen.excel.in;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.ImportStateBase;

/**
 * @author a.mueller
 * @created 11.05.2009
 * @version 1.0
 */
public class SpecimenSynthesysExcelImportState extends ImportStateBase<SpecimenSynthesysExcelImportConfigurator, CdmImportBase>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SpecimenSynthesysExcelImportState.class);

	public SpecimenSynthesysExcelImportState(SpecimenSynthesysExcelImportConfigurator config) {
		super(config);
	}



//	/* (non-Javadoc)
//	 * @see eu.etaxonomy.cdm.io.common.IoStateBase#initialize(eu.etaxonomy.cdm.io.common.IoConfiguratorBase)
//	 */
//	@Override
//	public void initialize(SpecimenImportConfigurator config) {
//
//	}

}
