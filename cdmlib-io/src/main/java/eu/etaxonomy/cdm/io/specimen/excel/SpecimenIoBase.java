/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.specimen.excel;


import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.CdmIoBase;

/**
 * @author p.kelbert
 * @created 29.10.2008
 * @version 1.0
 */
public abstract class SpecimenIoBase extends CdmIoBase<SpecimenImportState> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SpecimenIoBase.class);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(SpecimenImportState state) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doInvoke(SpecimenImportState state) {
		// TODO Auto-generated method stub
		return false;
	}




}