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
import java.util.UUID;

import eu.etaxonomy.cdm.api.dto.portal.DistributionInfoDto.InfoPart;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;

/**
 * @author a.mueller
 * @date 09.02.2023
 */
public class DistributionInfoConfiguration {

    private boolean preferSubAreas = true;

    private boolean statusOrderPreference = true;

    private Set<MarkerType> fallbackAreaMarkerTypes = new HashSet<>();

    private Set<MarkerType> alternativeRootAreaMarkerTypes = new HashSet<>();

    private Set<NamedAreaLevel> omitLevels = new HashSet<>();

    private String statusColorsString;

    private DistributionOrder distributionOrder = getDefaultDistributionOrder();

    private Set<UUID> features = new HashSet<>();

    private UUID areaTree = null;

    private UUID statusTree = null;

    private CondensedDistributionConfiguration condensedDistrConfig = CondensedDistributionConfiguration.NewDefaultInstance();

    private EnumSet<InfoPart> infoParts = getDefaultInfoParts();

    private boolean includeUnpublished = false;

    boolean neverUseFallbackAreaAsParent = true;  //true is E+M status

//********************* GETTER / SETTER ***************************/

    public boolean isPreferSubareas() {
        return preferSubAreas;
    }

    public void setPreferSubAreas(boolean preferSubAreas) {
        this.preferSubAreas = preferSubAreas;
    }

    public boolean isStatusOrderPreference() {
        return statusOrderPreference;
    }
    public void setStatusOrderPreference(boolean statusOrderPreference) {
        this.statusOrderPreference = statusOrderPreference;
    }

    public Set<MarkerType> getFallbackAreaMarkerTypes() {
        return fallbackAreaMarkerTypes;
    }
    public void setFallbackAreaMarkerTypes(Set<MarkerType>fallbackAreaMarkerTypes) {
        this.fallbackAreaMarkerTypes = fallbackAreaMarkerTypes == null? new HashSet<>() : fallbackAreaMarkerTypes;
    }

    public Set<MarkerType> getAlternativeRootAreaMarkerTypes() {
        return alternativeRootAreaMarkerTypes;
    }
    public void setAlternativeRootAreaMarkerTypes(Set<MarkerType> alternativeRootAreaMarkerTypes) {
        this.alternativeRootAreaMarkerTypes = alternativeRootAreaMarkerTypes == null? new HashSet<>() : alternativeRootAreaMarkerTypes;
    }

    public Set<NamedAreaLevel> getOmitLevels() {
        return omitLevels;
    }
    public void setOmitLevels(Set<NamedAreaLevel> omitLevels) {
        this.omitLevels = omitLevels == null? new HashSet<>() : omitLevels;
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
        this.distributionOrder = distributionOrder == null? getDefaultDistributionOrder() : distributionOrder;
    }
    private DistributionOrder getDefaultDistributionOrder() {
        return DistributionOrder.LABEL;
    }

    public EnumSet<InfoPart> getInfoParts() {
        return infoParts;
    }
    public void setInfoParts(EnumSet<InfoPart> infoParts) {
        this.infoParts = infoParts == null ? getDefaultInfoParts() : infoParts;
    }
    private EnumSet<InfoPart> getDefaultInfoParts() {
        return EnumSet.of(InfoPart.condensedDistribution, InfoPart.mapUriParams, InfoPart.tree);
    }

    public CondensedDistributionConfiguration getCondensedDistributionConfiguration() {
        return condensedDistrConfig;
    }
    public void setCondensedDistributionConfiguration(CondensedDistributionConfiguration condensedDistrConfig) {
        this.condensedDistrConfig = condensedDistrConfig == null ? CondensedDistributionConfiguration.NewDefaultInstance() : condensedDistrConfig;
    }

    public Set<UUID> getFeatures() {
        return features;
    }
    public void setFeatures(Set<UUID> features) {
        this.features = features == null? new HashSet<>() : features;
    }

    public UUID getAreaTree() {
        return areaTree;
    }
    public void setAreaTree(UUID areaTree) {
        this.areaTree = areaTree;
    }

    public UUID getStatusTree() {
        return statusTree;
    }
    public void setStatusTree(UUID statusTree) {
        this.statusTree = statusTree;
    }

    public boolean isIncludeUnpublished() {
        return includeUnpublished;
    }
    public void setIncludeUnpublished(boolean includeUnpublished) {
        this.includeUnpublished = includeUnpublished;
    }

    public boolean isNeverUseFallbackAreaAsParent() {
        return neverUseFallbackAreaAsParent;
    }
    public void setNeverUseFallbackAreaAsParent(boolean neverUseFallbackAreaAsParent) {
        this.neverUseFallbackAreaAsParent = neverUseFallbackAreaAsParent;
    }
}