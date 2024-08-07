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

/**
 * Configurator for WFO Content export.
 *
 * @author a.mueller
 * @since 2024-01-30
 */
public class WfoContentExportConfigurator
        extends WfoExportConfiguratorBase<WfoContentExportState,WfoContentExportConfigurator> {

    private static final long serialVersionUID = -5696517221605512545L;

    private CsvIOConfigurator csvIOConfig = CsvIOConfigurator.NewInstance();
    {
        csvIOConfig.setFieldsTerminatedBy(",");
    }

    private boolean createZipFile = true;

    private String familyStr = null;

    private boolean normalizeAuthorsToIpniStandard = true;

    private static final WfoContentExportTransformer transformer = new WfoContentExportTransformer();

//************************* FACTORY ******************************/

    public static WfoContentExportConfigurator NewInstance(){
        WfoContentExportConfigurator result = new WfoContentExportConfigurator(transformer);
        return result;
    }

    public static WfoContentExportConfigurator NewInstance(ICdmDataSource source, File destination){
        WfoContentExportConfigurator result = new WfoContentExportConfigurator(transformer);
        result.setSource(source);
        result.setDestination(destination);
        return result;
    }

//************************ CONSTRUCTOR *******************************/

    private WfoContentExportConfigurator(IExportTransformer transformer) {
        super(transformer);
        this.resultType = ExportResultType.MAP_BYTE_ARRAY;
        this.setTarget(TARGET.EXPORT_DATA);
        setUserFriendlyIOName("WFO Content Export");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void makeIoClassList() {
        ioClassList = new Class[] {
                WfoContentExport.class
        };
    }

    @Override
    public WfoContentExportState getNewState() {
        return new WfoContentExportState(this);
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

    public boolean isNormalizeAuthorsToIpniStandard() {
        return normalizeAuthorsToIpniStandard;
    }
    public void setNormalizeAuthorsToIpniStandard(boolean normalizeAuthorsToIpniStandard) {
        this.normalizeAuthorsToIpniStandard = normalizeAuthorsToIpniStandard;
    }
}