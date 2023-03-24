/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal.config;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import eu.etaxonomy.cdm.api.dto.portal.DistributionInfoDto.InfoPart;
import eu.etaxonomy.cdm.format.description.distribution.CondensedDistributionConfiguration;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;

/**
 * @author a.mueller
 * @date 09.02.2023
 */
public class DistributionInfoConfiguration {

    private boolean subAreaPreference = false;

    private boolean statusOrderPreference = false;

    private Set<MarkerType> hiddenAreaMarkerTypeList = new HashSet<>();   //was list before

    private Set<NamedAreaLevel> omitLevels = new HashSet<>();

    private String statusColorsString;

    private DistributionOrder distributionOrder = DistributionOrder.LABEL;

    private CondensedDistributionConfiguration condensedDistrConfig = CondensedDistributionConfiguration.NewDefaultInstance();

    private EnumSet<InfoPart> infoParts = EnumSet.of(
            InfoPart.condensedDistribution, InfoPart.mapUriParams, InfoPart.tree);

    private boolean useTreeDto = false;

    private boolean ignoreDistributionStatusUndefined = true;

//********************* GETTER / SETTER ***************************/

    public boolean isSubAreaPreference() {
        return subAreaPreference;
    }
    public void setSubAreaPreference(boolean subAreaPreference) {
        this.subAreaPreference = subAreaPreference;
    }

    public boolean isStatusOrderPreference() {
        return statusOrderPreference;
    }
    public void setStatusOrderPreference(boolean statusOrderPreference) {
        this.statusOrderPreference = statusOrderPreference;
    }

    public Set<MarkerType> getHiddenAreaMarkerTypeList() {
        return hiddenAreaMarkerTypeList;
    }
    public void setHiddenAreaMarkerTypeList(Set<MarkerType> hiddenAreaMarkerTypeList) {
        this.hiddenAreaMarkerTypeList = hiddenAreaMarkerTypeList;
    }

    public Set<NamedAreaLevel> getOmitLevels() {
        return omitLevels;
    }
    public void setOmitLevels(Set<NamedAreaLevel> omitLevels) {
        this.omitLevels = omitLevels;
    }

    public String getStatusColorsString() {
        return statusColorsString;
    }
    public void setStatusColorsString(String statusColorsString) {
        this.statusColorsString = statusColorsString;
    }

    public DistributionOrder getDistributionOrder() {
        return distributionOrder;
    }
    public void setDistributionOrder(DistributionOrder distributionOrder) {
        this.distributionOrder = distributionOrder;
    }

    public EnumSet<InfoPart> getInfoParts() {
        return infoParts;
    }
    public void setInfoParts(EnumSet<InfoPart> infoParts) {
        this.infoParts = infoParts;
    }

    public boolean isUseTreeDto() {
        return useTreeDto;
    }
    public void setUseTreeDto(boolean useTreeDto) {
        this.useTreeDto = useTreeDto;
    }

    public boolean isIgnoreDistributionStatusUndefined() {
        return ignoreDistributionStatusUndefined;
    }
    public void setIgnoreDistributionStatusUndefined(boolean ignoreDistributionStatusUndefined) {
        this.ignoreDistributionStatusUndefined = ignoreDistributionStatusUndefined;
    }

    public CondensedDistributionConfiguration getCondensedDistrConfig() {
        return condensedDistrConfig;
    }
    public void setCondensedDistrConfig(CondensedDistributionConfiguration condensedDistrConfig) {
        this.condensedDistrConfig = condensedDistrConfig;
    }
}