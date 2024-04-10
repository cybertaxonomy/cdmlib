/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.api.filter.TaxonOccurrenceRelationType;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;

/**
 * @author a.mueller
 * @date 07.01.2023
 */
public class TaxonPageDtoConfiguration implements Serializable {

    private static final long serialVersionUID = -3017154740995350103L;

    //TODO should this be part of the configuration??
    private UUID taxonUuid;

    //data
    private boolean withFacts = true;
    private boolean withSynonyms = true;
    private boolean withSpecimens = true;
    private boolean withKeys = true;
    private boolean withMedia = true;
    private boolean withTaxonNodes = true;

    private boolean includeUnpublished = false;

    private EnumSet<TaxonOccurrenceRelationType> specimenAssociationFilter;


    //synonymy
    //TODO taxonrelations
    //should withSynonyms includeProparte and misapplications
    // => yes as long as there are no specific parameters in dataportals to handle them differently
    private boolean withTaxonRelationships = true;
    private UUID taxonRelationshipTypeTree = null;

    //facts
    private UUID featureTree = null;
    private DistributionInfoConfiguration distributionInfoConfiguration = new DistributionInfoConfiguration();

    private Map<UUID,DistributionInfoConfiguration> perFeatureDistributionInfoConfiguration = new HashMap<>();

    //supplemental data
    EnumSet<OriginalSourceType> sourceTypes = EnumSet.of(OriginalSourceType.PrimaryTaxonomicSource,
            OriginalSourceType.PrimaryMediaSource);

    //formatting
    private List<Locale> locales = new ArrayList<>();  //is this data or formatting??
    public boolean formatSec = false;  //!!
    private Integer etAlPosition;


// ******************************* GETTER / SETTER ***********************************/

    public DistributionInfoConfiguration getDistributionInfoConfiguration() {
        return distributionInfoConfiguration;
    }
    public void setDistributionInfoConfiguration(DistributionInfoConfiguration distributionInfoConfiguration) {
        this.distributionInfoConfiguration = distributionInfoConfiguration;
    }

    public DistributionInfoConfiguration putDistributionInfoConfiguration(UUID featureUuid, DistributionInfoConfiguration config) {
        return perFeatureDistributionInfoConfiguration.put(featureUuid, config);
    }
    public DistributionInfoConfiguration getDistributionInfoConfiguration(UUID featureUuid) {
        DistributionInfoConfiguration result = perFeatureDistributionInfoConfiguration.get(featureUuid);
        if (result == null) {
            result = distributionInfoConfiguration;
        }
        return result;
    }

    public UUID getTaxonUuid() {
        return taxonUuid;
    }
    public void setTaxonUuid(UUID taxonUuid) {
        this.taxonUuid = taxonUuid;
    }

    public boolean isWithFacts() {
        return withFacts;
    }
    public void setWithFacts(boolean withFacts) {
        this.withFacts = withFacts;
    }

    public boolean isWithSynonyms() {
        return withSynonyms;
    }
    public void setWithSynonyms(boolean withSynonyms) {
        this.withSynonyms = withSynonyms;
    }

    public boolean isWithSpecimens() {
        return withSpecimens;
    }
    public void setWithSpecimens(boolean withSpecimens) {
        this.withSpecimens = withSpecimens;
    }

    public boolean isWithKeys() {
        return withKeys;
    }
    public void setWithKeys(boolean withKeys) {
        this.withKeys = withKeys;
    }

    public boolean isWithMedia() {
        return withMedia;
    }
    public void setWithMedia(boolean withMedia) {
        this.withMedia = withMedia;
    }

    public boolean isWithTaxonNodes() {
        return withTaxonNodes;
    }
    public void setWithTaxonNodes(boolean withTaxonNodes) {
        this.withTaxonNodes = withTaxonNodes;
    }
    public boolean isWithTaxonRelationships() {
        return withTaxonRelationships;
    }
    public void setWithTaxonRelationships(boolean withTaxonRelationships) {
        this.withTaxonRelationships = withTaxonRelationships;
    }
    public UUID getTaxonRelationshipTypeTree() {
        return taxonRelationshipTypeTree;
    }
    public void setTaxonRelationshipTypeTree(UUID taxonRelationshipTypeTree) {
        this.taxonRelationshipTypeTree = taxonRelationshipTypeTree;
    }

    public UUID getFeatureTree() {
        return featureTree;
    }
    public void setFeatureTree(UUID featureTree) {
        this.featureTree = featureTree;
    }

    public List<Locale> getLocales() {
        return locales;
    }
    public void setLocales(List<Locale> locales) {
        this.locales = locales;
    }

    public Integer getEtAlPosition() {
        return etAlPosition;
    }
    public void setEtAlPosition(Integer etAlPosition) {
        this.etAlPosition = etAlPosition;
    }

    public boolean isIncludeUnpublished() {
        return includeUnpublished;
    }
    public void setIncludeUnpublished(boolean includeUnpublished) {
        this.includeUnpublished = includeUnpublished;
    }

    public EnumSet<TaxonOccurrenceRelationType> getSpecimenAssociationFilter() {
        return specimenAssociationFilter;
    }
    public void setSpecimenAssociationFilter(EnumSet<TaxonOccurrenceRelationType> specimenAssociationFilter) {
        this.specimenAssociationFilter = specimenAssociationFilter;
    }

    public EnumSet<OriginalSourceType> getSourceTypes() {
        return sourceTypes;
    }
    public void setSourceTypes(EnumSet<OriginalSourceType> sourceTypes) {
        this.sourceTypes = sourceTypes;
    }
}