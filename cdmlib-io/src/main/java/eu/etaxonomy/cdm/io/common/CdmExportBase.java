/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.io.ByteArrayOutputStream;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 01.07.2008
 * @version 1.0
 */
public abstract class CdmExportBase<CONFIG extends IExportConfigurator<STATE, TRANSFORM>, STATE extends ExportStateBase, TRANSFORM extends IExportTransformer> extends CdmIoBase<STATE> implements ICdmExport<CONFIG, STATE>{
	private static Logger logger = Logger.getLogger(CdmExportBase.class);

    protected ByteArrayOutputStream exportStream;

	public Object getDbId(CdmBase cdmBase, STATE state){
		logger.warn("Not yet implemented for export base class");
		return null;
	}

	@Override
	public  byte[] getByteArray() {
	    return exportStream.toByteArray();
	}
}
