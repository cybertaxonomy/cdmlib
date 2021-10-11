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

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.ext.geo.CondensedDistributionConfiguration;
import eu.etaxonomy.cdm.io.common.CsvIOConfigurator;
import eu.etaxonomy.cdm.io.common.ExportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.ExportResultType;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;

/**
 * @author k.luther
 * @since 15.03.2017
 *
 */
public class CdmLightExportConfigurator
        extends ExportConfiguratorBase<CdmLightExportState, IExportTransformer, File>{

    private static final long serialVersionUID = -1562074221435082060L;

    private CsvIOConfigurator csvIOConfig = CsvIOConfigurator.NewInstance();
    {
        csvIOConfig.setFieldsTerminatedBy(",");
    }

    private boolean isHighlightPrimaryCollector = false;

    private boolean createZipFile = false;

    private boolean isFilterIntextReferences = true;
    private boolean isCreateCondensedDistributionString = true;
    private CondensedDistributionConfiguration condensedDistributionConfiguration = CondensedDistributionConfiguration.NewDefaultInstance();
    private boolean isExcludeImportSources = true;
    private boolean isShowAllNameRelationsInHomotypicGroup = false;
    /**
     * @return the isShowAllNameRelationsInHomotypicGroup
     */
    public boolean isShowAllNameRelationsInHomotypicGroup() {
        return isShowAllNameRelationsInHomotypicGroup;
    }

    /**
     * @param isShowAllNameRelationsInHomotypicGroup the isShowAllNameRelationsInHomotypicGroup to set
     */
    public void setShowAllNameRelationsInHomotypicGroup(boolean isShowAllNameRelationsInHomotypicGroup) {
        this.isShowAllNameRelationsInHomotypicGroup = isShowAllNameRelationsInHomotypicGroup;
    }

    /**
     * @return the isShowInverseNameRelationsInHomotypicGroup
     */
    public boolean isShowInverseNameRelationsInHomotypicGroup() {
        return isShowInverseNameRelationsInHomotypicGroup;
    }

    /**
     * @param isShowInverseNameRelationsInHomotypicGroup the isShowInverseNameRelationsInHomotypicGroup to set
     */
    public void setShowInverseNameRelationsInHomotypicGroup(boolean isShowInverseNameRelationsInHomotypicGroup) {
        this.isShowInverseNameRelationsInHomotypicGroup = isShowInverseNameRelationsInHomotypicGroup;
    }
    private boolean isShowInverseNameRelationsInHomotypicGroup = false;

    private boolean isAddHTML = true;

    private Comparator<TaxonNodeDto> comparator;

    //metadata /gfbio
    private String description;
    private String creator;
    private String contributor;
    private String title;
    private Language language;
    private String dataSet_landing_page;
    private String dataset_download_link;
    private String base_url;
    private String recommended_citation;
    private String location;
    private String keywords;
    private String licence;


    public static CdmLightExportConfigurator NewInstance(){
        CdmLightExportConfigurator result = new CdmLightExportConfigurator(null);
        return result;
    }

    public static CdmLightExportConfigurator NewInstance(ICdmDataSource source, File destination){
        CdmLightExportConfigurator result = new CdmLightExportConfigurator(null);
        result.setSource(source);
        result.setDestination(destination);
        return result;
    }

    //TODO AM: do we need the transformer parameter here?
    private CdmLightExportConfigurator(IExportTransformer transformer) {
        super(transformer);
        this.resultType = ExportResultType.MAP_BYTE_ARRAY;
        this.setTarget(TARGET.EXPORT_DATA);
        setUserFriendlyIOName("Cdm Light Export");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void makeIoClassList() {
        ioClassList = new Class[] {
                CdmLightClassificationExport.class
        };
    }

    @Override
    public CdmLightExportState getNewState() {
        return new CdmLightExportState(this);
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

    @Override
    public String getDestinationNameString() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreator() {
        return creator;
    }
    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getContributor() {
        return contributor;
    }
    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public Language getLanguage() {
        return language;
    }
    public void setLanguage(Language language) {
        this.language = language;
    }

    public String getDataSet_landing_page() {
        return dataSet_landing_page;
    }
    public void setDataSet_landing_page(String dataSet_landing_page) {
        this.dataSet_landing_page = dataSet_landing_page;
    }

    public String getDataset_download_link() {
        return dataset_download_link;
    }
    public void setDataset_download_link(String dataset_download_link) {
        this.dataset_download_link = dataset_download_link;
    }

    public String getBase_url() {
        return base_url;
    }
    public void setBase_url(String base_url) {
        this.base_url = base_url;
    }

    public String getRecommended_citation() {
        return recommended_citation;
    }
    public void setRecommended_citation(String recommended_citation) {
        this.recommended_citation = recommended_citation;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public String getKeywords() {
        return keywords;
    }
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getLicence() {
        return licence;
    }
    public void setLicence(String licence) {
        this.licence = licence;
    }


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

    public Comparator<TaxonNodeDto> getComparator() {
        return comparator;
    }
    public void setComparator(Comparator<TaxonNodeDto> comparator) {
        this.comparator = comparator;
    }

    public boolean isExcludeImportSources() {
        return isExcludeImportSources;
    }
    public void setExcludeImportSources(boolean isFilterImportSources) {
        this.isExcludeImportSources = isFilterImportSources;
    }

    public boolean isAddHTML() {
        return isAddHTML;
    }
    public void setAddHTML(boolean isAddHTML) {
        this.isAddHTML = isAddHTML;
    }

    public boolean isCreateCondensedDistributionString() {
        return this.isCreateCondensedDistributionString;
    }
    public void setCreateCondensedDistributionString(boolean isCreateCondensedDistributionString) {
         this.isCreateCondensedDistributionString = isCreateCondensedDistributionString;
    }

    public CondensedDistributionConfiguration getCondensedDistributionConfiguration() {
        return this.condensedDistributionConfiguration;
    }
    public void setCondensedDistributionConfiguration(CondensedDistributionConfiguration condensedDistributionConfiguration) {
        this.condensedDistributionConfiguration = condensedDistributionConfiguration;
    }
}
