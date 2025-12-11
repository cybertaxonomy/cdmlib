/**
* Copyright (C) 2025 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdmprintpub;

import java.io.File;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.ExportResultType;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.io.out.IFactExportConfigurator;
import eu.etaxonomy.cdm.io.out.TaxonTreeExportConfiguratorBase;

/**
 * The configurator for the Print/Publication export.
 * Defines input source, output destination, and content filters.
 *
 * @author veldmap97
 * @date Dec 2, 2025
 */
public class PrintPubExportConfigurator
        extends TaxonTreeExportConfiguratorBase<PrintPubExportState, PrintPubExportConfigurator>
        implements IFactExportConfigurator {

    private static final long serialVersionUID = -5958099339227666207L;

    // Filters
    private boolean doFactualData = true;
    private boolean includeUnpublishedFacts = false;

    // Metadata (Optional, but useful for Documents)
    private String documentTitle = "Taxonomic Export";

    // ************************* FACTORY ******************************/

    public static PrintPubExportConfigurator NewInstance(ICdmDataSource source, File destination){
        PrintPubExportConfigurator result = new PrintPubExportConfigurator(null);
        result.setSource(source);
        result.setDestination(destination);
        return result;
    }

    // ************************ CONSTRUCTOR *******************************/

    private PrintPubExportConfigurator(IExportTransformer transformer) {
        super(transformer);
        // Important: We are producing a SINGLE file (MD/PDF), not a Map/Zip of CSVs.
        this.resultType = ExportResultType.BYTE_ARRAY;
        this.setTarget(TARGET.EXPORT_DATA);
        setUserFriendlyIOName("Print/Publication Export");
    }

    // ************************ IMPLEMENTATION ****************************/

    @Override
    public PrintPubExportState getNewState() {
        return new PrintPubExportState(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void makeIoClassList() {
        // This tells the "defaultExport" bean: "When you run me, use THIS worker class."
        ioClassList = new Class[] {
            PrintPubClassificationExport.class
        };
    }

    @Override
    public String getDestinationNameString() {
        if (this.getDestination() != null) {
            return this.getDestination().getName();
        }
        return null;
    }

    @Override
    public boolean isDoFactualData() {
        return doFactualData;
    }

    @Override
    public void setDoFactualData(boolean doFactualData) {
        this.doFactualData = doFactualData;
    }

    @Override
    public boolean isIncludeUnpublishedFacts() {
        return includeUnpublishedFacts;
    }

    @Override
    public void setIncludeUnpublishedFacts(boolean includeUnpublishedFacts) {
        this.includeUnpublishedFacts = includeUnpublishedFacts;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }
}