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
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.mueller
 * @since 01.07.2008
 */
public abstract class CdmExportBase<CONFIG extends ExportConfiguratorBase<STATE, TRANSFORM, DEST>, STATE extends ExportStateBase, TRANSFORM extends IExportTransformer, DEST extends Object>
            extends CdmIoBase<STATE, ExportResult>
            implements ICdmExport<CONFIG, STATE>{

    private static final long serialVersionUID = 3685030095117254235L;

    private static Logger logger = Logger.getLogger(CdmExportBase.class);

    protected ByteArrayOutputStream exportStream;


    @Autowired
    protected IClassificationService classificationService;

    @Autowired
    protected ITaxonNodeService taxonNodeService;


	@Override
	public  ExportDataWrapper createExportData() {
	    if (exportStream != null){
	        ExportDataWrapper<byte[]> data = ExportDataWrapper.NewByteArrayInstance();
	        data.addExportData( exportStream.toByteArray());
	        return data;
	    }else{
	        return null;
	    }
	}

    @Override
    protected ExportResult getNoDataResult(STATE state) {
        return ExportResult.NewNoDataInstance(((IExportConfigurator)state.config).getResultType());
    }

    @Override
    protected ExportResult getDefaultResult(STATE state) {
        return ExportResult.NewInstance(((IExportConfigurator)state.config).getResultType());
    }

    @Override
    public byte[] getByteArray() {
        if (this.exportStream != null){
            return this.exportStream.toByteArray();
        }
        return null;
    }


    public Object getDbId(CdmBase cdmBase, STATE state){
        logger.warn("Not yet implemented for export base class");
        return null;
    }

    /**
     * <code>true</code> if neither synonym has state publish nor
     * taxon node filter includes unpublished taxa.
     */
    protected boolean isUnpublished(CONFIG config, Synonym synonym) {
        return ! (synonym.isPublish()
                || config.getTaxonNodeFilter().isIncludeUnpublished());
    }


    /**
     * <code>true</code> if neither pro parte synonym or misapplied name has state publish nor
     * taxon node filter includes unpublished taxa.
     */
    protected boolean isUnpublished(CONFIG config, Taxon relatedSynonymOrMisappliedName) {
        return ! (relatedSynonymOrMisappliedName.isPublish()
                || config.getTaxonNodeFilter().isIncludeUnpublished());
    }

}
