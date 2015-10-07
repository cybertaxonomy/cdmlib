/**
 * Copyright (C) 2008 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 */

package eu.etaxonomy.cdm.io.common;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;


/**
 * @author a.babadshanjan
 * @created 16.11.2008
 * @version 1.0
 */
public interface IExportConfigurator<STATE extends ExportStateBase, TRANSFORM extends IExportTransformer> extends IIoConfigurator {

    public static enum CHECK{
        CHECK_ONLY,
        EXPORT_WITHOUT_CHECK,
        CHECK_AND_EXPORT,
    }

    public static enum DO_REFERENCES{
        NONE,
        NOMENCLATURAL,
        CONCEPT_REFERENCES,
        ALL
    }

    public static enum TARGET{
        FILE,
        EXPORT_DATA
    }

    public boolean isValid();


    public CHECK getCheck();

    public void setTarget(TARGET target);

    public TARGET getTarget();

    public Class<ICdmIO>[] getIoClassList();

    /**
     * The CDM data source for the export
     * Don't use when using a spring data source
     * @return
     */
    public ICdmDataSource getSource();

    public void setSource(ICdmDataSource source);

    /**
     * Factory method. Creates a new state for the export type and adds this coniguration to it.
     * @return
     */
    public STATE getNewState();


    public TRANSFORM getTransformer();

    public void setTransformer(TRANSFORM transformer);


}
