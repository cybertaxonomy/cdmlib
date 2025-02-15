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
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.api.filter.TaxonOccurrenceRelationType;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.term.IdentifierType;

/**
 * @author a.mueller
 * @date 07.01.2023
 */
public class TaxonPageDtoConfiguration implements ISourceableLoaderConfiguration, Serializable {

    private static final long serialVersionUID = -3017154740995350103L;

    //TODO should this be part of the configuration??
    private UUID taxonUuid;

    private boolean useDtoLoading = false;

    //data
    private boolean withFacts = true;
    private boolean withSynonyms = true;
    private boolean withSpecimens = true;
    private boolean withKeys = true;
    private boolean withMedia = true;
    private boolean withTaxonNodes = true;

    private boolean includeUnpublished = false;

    private EnumSet<TaxonOccurrenceRelationType> specimenAssociationFilter;

    private Set<UUID> annotationTypes = defaultAnnotationTypes();
    private Set<UUID> markerTypes = new HashSet<>();
    private Set<UUID> identifierTypes = defaultIdentifierTypes();
    //#10622
    private Set<UUID> excludedFactDatasetMarkerTypes = new HashSet<>();
    private Set<UUID> directNameRelTyes = null;
    private Set<UUID> inverseNameRelTyes = null;


    //synonymy
    //TODO taxonrelations
    //should withSynonyms includeProparte and misapplications
    // => yes as long as there are no specific parameters in dataportals to handle them differently
    private boolean withTaxonRelationships = true;
    private UUID taxonRelationshipTypeTree = null;

    //typification
    private boolean withAccessionType = true;

    //facts
    private UUID featureTree = null;
    private DistributionInfoConfiguration distributionInfoConfiguration = new DistributionInfoConfiguration();

    private Map<UUID,DistributionInfoConfiguration> perFeatureDistributionInfoConfiguration = new HashMap<>();

    //supplemental data
    private EnumSet<OriginalSourceType> sourceTypes = defaultSourceTypes();

    //formatting
    private List<Locale> locales = new ArrayList<>();  //is this data or formatting??

    //temporary until we change to locale
    private Language language = Language.DEFAULT();

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

    //temporary
    public Language getLanguage() {
        return language;
    }
    public List<Language> getLanguages() {
        List<Language> result = new ArrayList<>();
        if (language != null) {
            result.add(language);
        }
        return result;
    }
    public void setLanguage(Language language) {
        this.language = language;
        //TODO temporary until CDM uses locales only
        this.locales.clear();
        String locale = language.getIso639_1();
        if (StringUtils.isNotBlank(locale)) {
            this.locales.add(Locale.forLanguageTag(locale));
        }
    }
    //end temporary

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

    @Override
    public EnumSet<OriginalSourceType> getSourceTypes() {
        return sourceTypes;
    }
    public void setSourceTypes(EnumSet<OriginalSourceType> sourceTypes) {
        this.sourceTypes = sourceTypes == null ? defaultSourceTypes() : sourceTypes;
        this.distributionInfoConfiguration.setSourceTypes(this.sourceTypes);
    }

    @Override
    public Set<UUID> getMarkerTypes() {
        return markerTypes;
    }
    /**
     * Note: this method also sets the marker type filter
     * for the included distributionInfoConfiguration. If the latter
     * needs a separate filter this has to be set afterwards.
     */
    public void setMarkerTypes(Set<UUID> markerTypes) {
        this.markerTypes = markerTypes == null ? new HashSet<>() : markerTypes;
        this.distributionInfoConfiguration.setMarkerTypes(this.markerTypes);
    }

    @Override
    public Set<UUID> getAnnotationTypes() {
        return annotationTypes;
    }
    /**
     * Note: this method also sets the annotation type filter
     * for the included distributionInfoConfiguration. If the latter
     * needs a separate filter this has to be set afterwards.
     */
    public void setAnnotationTypes(Set<UUID> annotationTypes) {
        this.annotationTypes = annotationTypes == null ? defaultAnnotationTypes() : annotationTypes;
        this.distributionInfoConfiguration.setAnnotationTypes(this.annotationTypes);
    }
    public void addAnnotationType(UUID annotationType) {
        if (this.annotationTypes == null) {
            this.annotationTypes = new HashSet<>();
        }
        this.annotationTypes.add(annotationType);
        this.distributionInfoConfiguration.addAnnotationType(annotationType);
    }

    @Override
    public Set<UUID> getIdentifierTypes() {
        return identifierTypes;
    }
    public void setIdentifierTypes(Set<UUID> identifierTypes) {
        this.identifierTypes = identifierTypes == null ? defaultIdentifierTypes() : identifierTypes;
    }

    //name relationship types
    public Set<UUID> getDirectNameRelTyes() {
        return directNameRelTyes;
    }
    public void setDirectNameRelTyes(Set<UUID> directNameRelTyes) {
        this.directNameRelTyes = directNameRelTyes == null ? new HashSet<>() : directNameRelTyes;
    }

    public Set<UUID> getInverseNameRelTyes() {
        return inverseNameRelTyes;
    }
    public void setInverseNameRelTyes(Set<UUID> inverseNameRelTyes) {
        this.inverseNameRelTyes = inverseNameRelTyes == null ? new HashSet<>() : inverseNameRelTyes;
    }

    public boolean isUseDtoLoading() {
        return useDtoLoading;
    }
    public void setUseDtoLoading(boolean useDtoLoading) {
        this.useDtoLoading = useDtoLoading;
    }

    public boolean isWithAccessionType() {
        return withAccessionType;
    }
    public void setWithAccessionType(boolean withAccessionType) {
        this.withAccessionType = withAccessionType;
    }

    public Set<UUID> getExcludedFactDatasetMarkerTypes() {
        return excludedFactDatasetMarkerTypes;
    }
    public void setExcludedFactDatasetMarkerTypes(Set<UUID> excludedFactDatasetMarkerTypes) {
        this.excludedFactDatasetMarkerTypes = excludedFactDatasetMarkerTypes == null ? new HashSet<>() : excludedFactDatasetMarkerTypes;
    }

    //defaults
    private final Set<UUID> defaultAnnotationTypes() {
        return new HashSet<>(Arrays.asList(new UUID[] {AnnotationType.uuidEditorial}));
    }

    private final Set<UUID> defaultIdentifierTypes() {
        return new HashSet<>(Arrays.asList(new UUID[] {IdentifierType.uuidWfoNameIdentifier}));
    }

    private final EnumSet<OriginalSourceType> defaultSourceTypes() {
        return OriginalSourceType.allPublicTypes();
    }
}