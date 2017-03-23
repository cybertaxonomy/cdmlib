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
import org.apache.poi.ss.formula.functions.T;

import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 01.07.2008
 * @version 1.0
 */
public abstract class CdmExportBase<CONFIG extends IExportConfigurator<STATE, TRANSFORM>, STATE extends ExportStateBase, TRANSFORM extends IExportTransformer>
            extends CdmIoBase<STATE>
            implements ICdmExport<CONFIG, STATE>{

    private static Logger logger = Logger.getLogger(CdmExportBase.class);

    protected ByteArrayOutputStream exportStream;

    protected ExportDataWrapper<T> exportData;

	public Object getDbId(CdmBase cdmBase, STATE state){
		logger.warn("Not yet implemented for export base class");
		return null;
	}

	@Override
	public  ExportDataWrapper getExportData() {
	    if (exportStream != null){
	        ExportDataWrapper data = ExportDataWrapper.NewByteArrayInstance();
	        data.addExportData( exportStream.toByteArray());
	        return data;
	    }else{
	        return null;
	    }
	}

	//TODO move up to CdmIoBase once ImportResult is also implemented
    @Override
    public ExportResult invoke(STATE state) {
        if (isIgnore( state)){
            logger.info("No invoke for " + ioName + " (ignored)");
            return ExportResult.NewNoDataInstance(((IExportConfigurator)state.config).getResultType());
        }else{
            updateProgress(state, "Invoking " + ioName);
            state.setResult(ExportResult.NewInstance(((IExportConfigurator)state.config).getResultType()));
            doInvoke(state);
            return state.getResult();
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getByteArray() {
        if (this.exportStream != null){
            return this.exportStream.toByteArray();
        }
        return null;
    }
}
