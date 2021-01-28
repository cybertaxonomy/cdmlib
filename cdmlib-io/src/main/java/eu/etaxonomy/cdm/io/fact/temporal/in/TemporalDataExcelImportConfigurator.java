/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.temporal.in;

import eu.etaxonomy.cdm.common.URI;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.fact.in.FactExcelImportConfiguratorBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * Configurator for taxon based temporal data import.
 *
 * @author a.mueller
 * @since 15.07.2020
 */
public class TemporalDataExcelImportConfigurator<ANALYZE extends TemporalDataExcelFormatAnalyzer<?>>
        extends FactExcelImportConfiguratorBase<ANALYZE>{

    private static final long serialVersionUID = 2413575026028295925L;

    private String columnLabelStart = "Start";
    private String columnLabelEnd = "End";

    public static final TemporalDataExcelImportConfigurator<TemporalDataExcelFormatAnalyzer<?>> NewTemporalInstance(URI uri, ICdmDataSource destination){
        return new TemporalDataExcelImportConfigurator<TemporalDataExcelFormatAnalyzer<?>>(uri, destination, null);
    }

    protected TemporalDataExcelImportConfigurator(URI uri, ICdmDataSource destination, IInputTransformer transformer) {
        super(uri, destination, transformer);
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public TemporalDataExcelImportState<?> getNewState() {
        return new TemporalDataExcelImportState<>(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void makeIoClassList() {
        ioClassList = new Class[]{
                TemporalDataExcelImport.class,
        };
    }

    /**
     * Note: This method needs to be overriden by potential subclasses.
     */
    @SuppressWarnings("unchecked")
    @Override
    public ANALYZE getAnalyzer() {
        return (ANALYZE)new TemporalDataExcelFormatAnalyzer(this);
    }

    @Override
    public NomenclaturalCode getNomenclaturalCode() {
        NomenclaturalCode result = super.getNomenclaturalCode();
        if (result == null){
            result = NomenclaturalCode.ICNAFP;
        }
        return result;
    }

    public void setRowToNeglect(int row){

    }

    public String getColumnLabelStart() {
        return columnLabelStart;
    }
    public void setColumnLabelStart(String columnLabelStart) {
        this.columnLabelStart = columnLabelStart;
    }

    public String getColumnLabelEnd() {
        return columnLabelEnd;
    }
    public void setColumnLabelEnd(String columnLabelEnd) {
        this.columnLabelEnd = columnLabelEnd;
    }

}
