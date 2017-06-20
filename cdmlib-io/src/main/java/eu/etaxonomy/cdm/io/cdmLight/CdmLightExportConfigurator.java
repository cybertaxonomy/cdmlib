/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdmLight;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.io.common.CsvIOConfigurator;
import eu.etaxonomy.cdm.io.common.ExportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.ExportResultType;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;

/**
 * @author k.luther
 * @date 15.03.2017
 *
 */
public class CdmLightExportConfigurator extends ExportConfiguratorBase<File, CdmLightExportState, IExportTransformer>{


    private static final long serialVersionUID = -1562074221435082060L;

    private Set<UUID> classificationUuids = new HashSet<>();

    private CsvIOConfigurator csvIOConfig = CsvIOConfigurator.NewInstance();

    private boolean isHighlightPrimaryCollector = false;

    private boolean createZipFile = false;

    /**
     * @param transformer
     */
    public CdmLightExportConfigurator(IExportTransformer transformer) {
        super(transformer);
        this.resultType = ExportResultType.MAP_BYTE_ARRAY;
        this.setTarget(TARGET.EXPORT_DATA);
    }


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
    public boolean isHasHeaderLines() {
        return  csvIOConfig.isHasHeaderLines();
    }
    public void setHasHeaderLines(boolean hasHeaderLines) {
        this.csvIOConfig.setHasHeaderLines(hasHeaderLines);
    }
    public String getFieldsTerminatedBy() {
        return  csvIOConfig.getFieldsTerminatedBy();
    }
    public void setFieldsTerminatedBy(String fieldsTerminatedBy) {
        this.csvIOConfig.setFieldsTerminatedBy(fieldsTerminatedBy);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public CdmLightExportState getNewState() {

        return new CdmLightExportState(this);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDestinationNameString() {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    @SuppressWarnings("unchecked")
    protected void makeIoClassList() {
        ioClassList = new Class[] {
                CdmLightClassificationExport.class
        };
    }

    /**
     * @return the classificationUuids
     */
    public Set<UUID> getClassificationUuids() {
        return classificationUuids;
    }


    /**
     * @param classificationUuids the classificationUuids to set
     */
    public void setClassificationUuids(Set<UUID> classificationUuids) {
        this.classificationUuids = classificationUuids;
    }


    /**
     * @return
     */
    public boolean isHighLightPrimaryCollector() {

        return isHighlightPrimaryCollector;
    }


    public boolean isCreateZipFile() {
        return createZipFile;
    }


    public void setCreateZipFile(boolean createZipFile) {
        this.createZipFile = createZipFile;
    }

}
