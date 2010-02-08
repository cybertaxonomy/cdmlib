/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.specimen.abcd206;


import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.CdmIoBase;


/**
 * @author p.kelbert
 * @created 20.10.2008
 * @version 1.0
 */
public abstract class SpecimenIoBase  extends CdmIoBase<SpecimenImportState> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SpecimenIoBase.class);

	protected abstract boolean doInvoke(SpecimenImportState state);

}