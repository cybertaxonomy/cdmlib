/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.coldp;

import java.io.File;
import java.util.Comparator;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CsvIOConfigurator;
import eu.etaxonomy.cdm.io.common.ExportResultType;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.io.out.IFactExportConfigurator;
import eu.etaxonomy.cdm.io.out.TaxonTreeExportConfiguratorBase;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;

/**
 * @author a.mueller
 * @since 2023-07-17
 */
public class ColDpExportConfigurator
        extends TaxonTreeExportConfiguratorBase<ColDpExportState,ColDpExportConfigurator>
        implements IFactExportConfigurator {

    private static final long serialVersionUID = -1562074221435082060L;

    private CsvIOConfigurator csvIOConfig = CsvIOConfigurator.NewInstance();
    {
        csvIOConfig.setFieldsTerminatedBy(",");
    }

    private boolean isHighlightPrimaryCollector = false;

    private boolean createZipFile = true;

    private Comparator<TaxonNodeDto> taxonNodeComparator;

    private boolean includeFullName = false;

    private boolean normalizeAuthorsToIpniStandard = false;

    //filter
    private boolean doFactualData = true;
    private boolean includeUnpublishedFacts = false;

    private static final ColDpExportTransformer transformer = new ColDpExportTransformer();

//************************* FACTORY ******************************/

    public static ColDpExportConfigurator NewInstance(){
        ColDpExportConfigurator result = new ColDpExportConfigurator(transformer);
        return result;
    }

    public static ColDpExportConfigurator NewInstance(ICdmDataSource source, File destination){
        ColDpExportConfigurator result = new ColDpExportConfigurator(transformer);
        result.setSource(source);
        result.setDestination(destination);
        return result;
    }

//************************ CONSTRUCTOR *******************************/

    private ColDpExportConfigurator(IExportTransformer transformer) {
        super(transformer);
        this.resultType = ExportResultType.MAP_BYTE_ARRAY;
        this.setTarget(TARGET.EXPORT_DATA);
        setUserFriendlyIOName("Col DP Export");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void makeIoClassList() {
        ioClassList = new Class[] {
                ColDpClassificationExport.class
        };
    }

    @Override
    public ColDpExportState getNewState() {
        return new ColDpExportState(this);
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

    //TODO really needed?
    public boolean isHighLightPrimaryCollector() {
        return isHighlightPrimaryCollector;
    }

    public boolean isCreateZipFile() {
        return createZipFile;
    }
    public void setCreateZipFile(boolean createZipFile) {
        this.createZipFile = createZipFile;
    }

    public Comparator<TaxonNodeDto> getTaxonNodeComparator() {
        return taxonNodeComparator;
    }
    public void setTaxonNodeComparator(Comparator<TaxonNodeDto> taxonNodeComparator) {
        this.taxonNodeComparator = taxonNodeComparator;
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

    public boolean isIncludeFullName() {
        return includeFullName;
    }
    public void setIncludeFullName(boolean includeFullName) {
        this.includeFullName = includeFullName;
    }


    public boolean isNormalizeAuthorsToIpniStandard() {
        return normalizeAuthorsToIpniStandard;
    }
    public void setNormalizeAuthorsToIpniStandard(boolean normalizeAuthorsToIpniStandard) {
        this.normalizeAuthorsToIpniStandard = normalizeAuthorsToIpniStandard;
    }
}