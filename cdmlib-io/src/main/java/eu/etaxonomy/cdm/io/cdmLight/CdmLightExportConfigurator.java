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
import java.util.Comparator;

import eu.etaxonomy.cdm.io.common.CsvIOConfigurator;
import eu.etaxonomy.cdm.io.common.ExportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.ExportResultType;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;

/**
 * @author k.luther
 * @since 15.03.2017
 *
 */
public class CdmLightExportConfigurator
        extends ExportConfiguratorBase<CdmLightExportState, IExportTransformer, File>{

    private static final long serialVersionUID = -1562074221435082060L;


//    private Set<UUID> classificationUuids = new HashSet<>();
//
//    private Set<UUID> taxonNodeUuids = new HashSet<>();


    private CsvIOConfigurator csvIOConfig = CsvIOConfigurator.NewInstance();

    private boolean isHighlightPrimaryCollector = false;

    private boolean createZipFile = false;

    private boolean isFilterIntextReferences = true;
    //private boolean isCreateCondensedDistributionString = true;
    //private CondensedDistributionRecipe recipe = CondensedDistributionRecipe.EuroPlusMed;
    private boolean isFilterImportSources = true;

    private Comparator<TaxonNodeDto> comparator;

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

//    public Set<UUID> getClassificationUuids() {
//        return classificationUuids;
//    }
//    public void setClassificationUuids(Set<UUID> classificationUuids) {
//        this.classificationUuids = classificationUuids;
//    }
    public boolean isHighLightPrimaryCollector() {
        return isHighlightPrimaryCollector;
    }

    public boolean isCreateZipFile() {
        return createZipFile;
    }
    public void setCreateZipFile(boolean createZipFile) {
        this.createZipFile = createZipFile;
    }


    public boolean isFilterIntextReferences() {
        return isFilterIntextReferences;
    }


    public void setRemoveIntextReferences(boolean isRemoveIntextReferences) {
        this.isFilterIntextReferences = isRemoveIntextReferences;
    }


    /**
     * @return the comparator
     */
    public Comparator<TaxonNodeDto> getComparator() {
        return comparator;
    }


    /**
     * @param comparator the comparator to set
     */
    public void setComparator(Comparator<TaxonNodeDto> comparator) {
        this.comparator = comparator;
    }


    /**
     * @return
     */
//    public boolean isCreateCondensedDistributionString() {
//        return isCreateCondensedDistributionString;
//    }
//
//    public void setCreateCondensedDistributionString(boolean isCreateCondensedDistributionString) {
//        this.isCreateCondensedDistributionString = isCreateCondensedDistributionString;
//    }
//
//
//    /**
//     * @return
//     */
//    public CondensedDistributionRecipe getCondensedDistributionRecipe() {
//        return recipe;
//    }
//
//    public void setCondensedDistributionRecipe(CondensedDistributionRecipe recipe) {
//        this.recipe = recipe;
//    }


    /**
     * @return
     */
    public boolean isFilterImportSources() {
        return isFilterImportSources;
    }

    public void setFilterImportSources(boolean isFilterImportSources) {
        this.isFilterImportSources = isFilterImportSources;
    }

//    public Set<UUID> getTaxonNodeUuids() {
//        return taxonNodeUuids;
//    }
//
//
//    public void setTaxonNodeUuids(Set<UUID> taxonNodeUuids) {
//        this.taxonNodeUuids = taxonNodeUuids;
//    }




}
