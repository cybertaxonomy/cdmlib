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


//    private Set<UUID> classificationUuids = new HashSet<>();
//
//    private Set<UUID> taxonNodeUuids = new HashSet<>();


    private CsvIOConfigurator csvIOConfig = CsvIOConfigurator.NewInstance();
    {
        csvIOConfig.setFieldsTerminatedBy(",");
    }

    private boolean isHighlightPrimaryCollector = false;

    private boolean createZipFile = false;

    private boolean isFilterIntextReferences = true;
    //private boolean isCreateCondensedDistributionString = true;
    //private CondensedDistributionRecipe recipe = CondensedDistributionRecipe.EuroPlusMed;
    private boolean isFilterImportSources = true;

    private final boolean isAddHTML = true;

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

    /**
     * @param transformer
     */
    public CdmLightExportConfigurator(IExportTransformer transformer) {
        super(transformer);
        this.resultType = ExportResultType.MAP_BYTE_ARRAY;
        this.setTarget(TARGET.EXPORT_DATA);
        setUserFriendlyIOName("Cdm Light Export");
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

    @Override
    public CdmLightExportState getNewState() {
        return new CdmLightExportState(this);
    }

    @Override
    public String getDestinationNameString() {
        // TODO Auto-generated method stub
        return null;
    }
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }


    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * @return the creator
     */
    public String getCreator() {
        return creator;
    }


    /**
     * @param creator the creator to set
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }


    public String getContributor() {
        return contributor;
    }


    public void setContributor(String contributor) {
        this.contributor = contributor;
    }


    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }


    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }


    /**
     * @return the language
     */
    public Language getLanguage() {
        return language;
    }


    /**
     * @param language the language to set
     */
    public void setLanguage(Language language) {
        this.language = language;
    }





    /**
     * @return the dataSet_landing_page
     */
    public String getDataSet_landing_page() {
        return dataSet_landing_page;
    }


    /**
     * @param dataSet_landing_page the dataSet_landing_page to set
     */
    public void setDataSet_landing_page(String dataSet_landing_page) {
        this.dataSet_landing_page = dataSet_landing_page;
    }


    /**
     * @return the dataset_download_link
     */
    public String getDataset_download_link() {
        return dataset_download_link;
    }


    /**
     * @param dataset_download_link the dataset_download_link to set
     */
    public void setDataset_download_link(String dataset_download_link) {
        this.dataset_download_link = dataset_download_link;
    }


    /**
     * @return the base_url
     */
    public String getBase_url() {
        return base_url;
    }


    /**
     * @param base_url the base_url to set
     */
    public void setBase_url(String base_url) {
        this.base_url = base_url;
    }


    /**
     * @return the recommended_citation
     */
    public String getRecommended_citation() {
        return recommended_citation;
    }


    /**
     * @param recommended_citation the recommended_citation to set
     */
    public void setRecommended_citation(String recommended_citation) {
        this.recommended_citation = recommended_citation;
    }


    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }


    /**
     * @param location the location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }


    /**
     * @return the keywords
     */
    public String getKeywords() {
        return keywords;
    }


    /**
     * @param keywords the keywords to set
     */
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }


    /**
     * @return the licence
     */
    public String getLicence() {
        return licence;
    }


    /**
     * @param licence the licence to set
     */
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


    public boolean isFilterImportSources() {
        return isFilterImportSources;
    }
    public void setFilterImportSources(boolean isFilterImportSources) {
        this.isFilterImportSources = isFilterImportSources;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void makeIoClassList() {
        ioClassList = new Class[] {
                CdmLightClassificationExport.class
        };
    }


    public boolean isAddHTML() {
        return isAddHTML;
    }


//    public void setAddHTML(boolean isAddHTML) {
//        this.isAddHTML = isAddHTML;
//    }

}
