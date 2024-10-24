/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.distribution.in;

import java.util.UUID;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.fact.in.FactExcelImportConfiguratorBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * @author a.mueller
 * @since 08.10.2024
 */
public class DistributionExcelImportConfigurator
        extends FactExcelImportConfiguratorBase<DistributionExcelFormatAnalyzer>{

    private static final long serialVersionUID = -2088509987577490895L;

    private UUID defaultStatusUuid;
    private UUID defaultAreaUuid;
    private UUID descriptionMarkerTypeUuid;
    private UUID distributionFeatureUuid;
    private boolean useAcceptedNameAsNameUsedInSource = false;

    public static DistributionExcelImportConfigurator NewInstance(URI uri, ICdmDataSource destination){
        return new DistributionExcelImportConfigurator(uri, destination, null);
    }

    private DistributionExcelImportConfigurator(URI uri, ICdmDataSource destination, IInputTransformer transformer) {
        super(uri, destination, transformer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public DistributionExcelImportState getNewState() {
        return new DistributionExcelImportState(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void makeIoClassList() {
        ioClassList = new Class[]{
                DistributionExcelImport.class,
        };
    }

    @Override
    public DistributionExcelFormatAnalyzer getAnalyzer() {
        return new DistributionExcelFormatAnalyzer(this);
    }

    @Override
    public NomenclaturalCode getNomenclaturalCode() {
        NomenclaturalCode result = super.getNomenclaturalCode();
        if (result == null){
            result = NomenclaturalCode.ICNAFP;
        }
        return result;
    }

    public void setStatusLabelColumnLabel(String label) {
        putLabelReplacement(DistributionExcelImport.COL_STATUS_LABEL, label);
    }
    public void setStatusUuidColumnLabel(String label) {
        putLabelReplacement(DistributionExcelImport.COL_STATUS_UUID, label);
    }

    public void setAreaLabelColumnLabel(String label) {
        putLabelReplacement(DistributionExcelImport.COL_AREA_LABEL, label);
    }
    public void setAreaUuidColumnLabel(String label) {
        putLabelReplacement(DistributionExcelImport.COL_AREA_UUID, label);
    }

    public UUID getDefaultStatusUuid() {
        return defaultStatusUuid;
    }
    public void setDefaultStatusUuid(UUID defaultStatusUuid) {
        this.defaultStatusUuid = defaultStatusUuid;
    }

    public UUID getDefaultAreaUuid() {
        return defaultAreaUuid;
    }
    public void setDefaultAreaUuid(UUID defaultAreaUuid) {
        this.defaultAreaUuid = defaultAreaUuid;
    }

    /**
     * If set, the descriptions will get a marker of the
     * given marker type. Reuse of markers will only take
     * place if a description with the given marker exists
     * already.
     */
    public UUID getDescriptionMarkerTypeUuid() {
        return descriptionMarkerTypeUuid;
    }
    public void setDescriptionMarkerTypeUuid(UUID descriptionMarkerTypeUuid) {
        this.descriptionMarkerTypeUuid = descriptionMarkerTypeUuid;
    }

    /**
     * If set, the given feature is used for distributions
     * instead of the default distribution feature
     */
    public UUID getDistributionFeatureUuid() {
        return distributionFeatureUuid;
    }
    public void setDistributionFeatureUuid(UUID distributionFeatureUuid) {
        this.distributionFeatureUuid = distributionFeatureUuid;
    }

    public boolean isUseAcceptedNameAsNameUsedInSource() {
        return useAcceptedNameAsNameUsedInSource;
    }
    public void setUseAcceptedNameAsNameUsedInSource(boolean useAcceptedNameAsNameUsedInSource) {
        this.useAcceptedNameAsNameUsedInSource = useAcceptedNameAsNameUsedInSource;
    }
}
