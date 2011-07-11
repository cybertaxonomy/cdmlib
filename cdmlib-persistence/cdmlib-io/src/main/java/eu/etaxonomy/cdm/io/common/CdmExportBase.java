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

/**
 * @author a.mueller
 * @created 01.07.2008
 * @version 1.0
 */
public abstract class CdmExportBase<CONFIG extends IExportConfigurator, STATE extends ExportStateBase> extends CdmIoBase<STATE> implements ICdmExport<CONFIG, STATE>{
	private static Logger logger = Logger.getLogger(CdmExportBase.class);



	

	

}
