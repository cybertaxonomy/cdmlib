/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.outputmodel;

import eu.etaxonomy.cdm.io.common.ExportResult;
import eu.etaxonomy.cdm.io.common.ExportResult.ExportResultState;
import eu.etaxonomy.cdm.io.common.ExportStateBase;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;

/**
 * @author k.luther
 * @date 15.03.2017
 *
 */
public class OutputModelExportState extends ExportStateBase<OutputModelConfigurator, IExportTransformer>{

    private ExportResult result = ExportResult.NewInstance();

    private OutputModelResultProcessor processor = new OutputModelResultProcessor(this);

    /**
     * @param config
     */
    protected OutputModelExportState(OutputModelConfigurator config) {
        super(config);

    }

    /**
     * @return the result
     */
    public ExportResult getResult() {return result;}

    /**
     * @param result the result to set
     */
    public void setResult(ExportResult result) {this.result = result;}

    /**
     *
     */
    public void setEmptyData() {
        this.result.setState(ExportResultState.SUCCESS_BUT_NO_DATA);
    }

    /**
     * @return the processor
     */
    public OutputModelResultProcessor getProcessor() {
        return processor;
    }



}
