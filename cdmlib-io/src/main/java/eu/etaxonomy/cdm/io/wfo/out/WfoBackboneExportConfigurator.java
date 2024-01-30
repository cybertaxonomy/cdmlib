/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.wfo.out;

import java.io.File;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CsvIOConfigurator;
import eu.etaxonomy.cdm.io.common.ExportResultType;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.io.out.TaxonTreeExportConfiguratorBase;

/**
 * Configurator for WFO Backbone export.
 *
 * @see https://dev.e-taxonomy.eu/redmine/issues/10446
 *
 * @author a.mueller
 * @since 2023-12-08
 */
public class WfoBackboneExportConfigurator
        extends TaxonTreeExportConfiguratorBase<WfoBackboneExportState,WfoBackboneExportConfigurator> {

    private static final long serialVersionUID = -6543105949709811075L;

    private CsvIOConfigurator csvIOConfig = CsvIOConfigurator.NewInstance();
    {
        csvIOConfig.setFieldsTerminatedBy(",");
    }

    private boolean createZipFile = true;

    private String familyStr = null;

    private static final WfoBackboneExportTransformer transformer = new WfoBackboneExportTransformer();

//************************* FACTORY ******************************/

    public static WfoBackboneExportConfigurator NewInstance(){
        WfoBackboneExportConfigurator result = new WfoBackboneExportConfigurator(transformer);
        return result;
    }

    public static WfoBackboneExportConfigurator NewInstance(ICdmDataSource source, File destination){
        WfoBackboneExportConfigurator result = new WfoBackboneExportConfigurator(transformer);
        result.setSource(source);
        result.setDestination(destination);
        return result;
    }

//************************ CONSTRUCTOR *******************************/

    private WfoBackboneExportConfigurator(IExportTransformer transformer) {
        super(transformer);
        this.resultType = ExportResultType.MAP_BYTE_ARRAY;
        this.setTarget(TARGET.EXPORT_DATA);
        setUserFriendlyIOName("WFO Classification Export");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void makeIoClassList() {
        ioClassList = new Class[] {
                WfoBackboneExport.class
        };
    }

    @Override
    public WfoBackboneExportState getNewState() {
        return new WfoBackboneExportState(this);
    }

    @Override
    public String getDestinationNameString() {
        // TODO Auto-generated method stub
        return null;
    }

//******************** GETTER / SETTER *******************************/

    public String getEncoding() {
        return csvIOConfig.getEncoding();
    }
    public void setEncoding(String encoding) {
        this.csvIOConfig.setEncoding(encoding);
    }

    public String getLinesTerminatedBy() {
        return csvIOConfig.getLinesTerminatedBy();
    }
    public void setLinesTerminatedBy(String linesTerminatedBy) {
        this.csvIOConfig.setLinesTerminatedBy(linesTerminatedBy);
    }

    public String getFieldsEnclosedBy() {
        return  csvIOConfig.getFieldsEnclosedBy();
    }
    public void setFieldsEnclosedBy(String fieldsEnclosedBy) {
        this.csvIOConfig.setFieldsEnclosedBy(fieldsEnclosedBy);
    }

    public boolean isIncludeHeaderLines() {
        return  csvIOConfig.isIncludeHeaderLines();
    }
    public void setIncludeHeaderLines(boolean hasHeaderLines) {
        this.csvIOConfig.setIncludeHeaderLines(hasHeaderLines);
    }

    public String getFieldsTerminatedBy() {
        return  csvIOConfig.getFieldsTerminatedBy();
    }
    public void setFieldsTerminatedBy(String fieldsTerminatedBy) {
        this.csvIOConfig.setFieldsTerminatedBy(fieldsTerminatedBy);
    }

    public boolean isCreateZipFile() {
        return createZipFile;
    }
    public void setCreateZipFile(boolean createZipFile) {
        this.createZipFile = createZipFile;
    }

    //familyStr
    public String getFamilyStr() {
        return familyStr;
    }
    public void setFamilyStr(String familyStr) {
        this.familyStr = familyStr;
    }
}