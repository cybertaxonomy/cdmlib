/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;

/**
 * @author a.mueller
 * @since 30.11.2020
 */
public class SubtreeCloneConfigurator implements Serializable {

    private static final long serialVersionUID = 5599009171476893297L;

    //might be replaced by a TaxonNodeFilter in future (but requires treebuilding and handling for missing inbetween taxa a tree)
    private Set<UUID> subTreeUuids;

    private String classificationName = "Taxon subtree clone";

    private boolean reuseClassificationReference = false;
     //used only if reuseClassificationReferenceUuid == false
     private UUID classificationReferenceUuid;
     private Reference classificationReference;

    private boolean reuseTaxa = false;
     //used only if reuseTaxa == false
     private boolean includeSynonymsIncludingManAndProParte = true;
     //used only if reuseTaxa == false
     private boolean includeDescriptiveData = true;
     //used only if reuseTaxa == false
     private boolean includeMedia = true;
     //used only if reuseTaxa == false
     private boolean includeTaxonRelationshipsExcludingManAndProParte = false;
     //used only if reuseTaxa == false
     private boolean reuseNames = true;
     //used only if reuseTaxa == false
     private boolean reuseTaxonSecundum = true;
      //used only if reuseTaxa == false AND reuseTaxonSecundum = false
      private UUID taxonSecundumUuid = null;
      private Reference taxonSecundum;

    private boolean reuseParentChildReference = true;
     //used only if reuseParentChildReference == false
     private UUID parentChildReferenceUuid = null;
     private Reference parentChildReference;

    private TaxonRelationshipType relationTypeToOldTaxon;
     //used only if relationTypeToOldTaxon != null
     private boolean relationDoubtful = true;
     //used only if relationTypeToOldTaxon != null
     private UUID relationshipReferenceUuid = null;
     private Reference relationshipReference;


    /**
     * Creates a default instance with default values. E.g. reuseNames is <code>true</code>.
     * @param subTreeUuid
     * @param classificationName
     * @return
     */
    public static SubtreeCloneConfigurator NewBaseInstance(UUID subTreeUuid, String classificationName
            ){
        Set<UUID> subTreeUuids = new HashSet<>();
        subTreeUuids.add(subTreeUuid);
        return new SubtreeCloneConfigurator(subTreeUuids, classificationName,
                false, null,
                false, true, null,
                true, null,
                null,
                true
                );
    }

    public static SubtreeCloneConfigurator NewInstance(Set<UUID> subTreeUuids, String classificationName,
            boolean reuseClassificationReference, UUID classificationReferenceUuid, boolean reuseTaxa,
            boolean reuseTaxonSecundum, UUID taxonSecundumUuid, boolean reuseParentChildReference,
            UUID parentChildReferenceUuid, TaxonRelationshipType relationTypeToOldTaxon, boolean reuseNames){
        return new SubtreeCloneConfigurator(subTreeUuids, classificationName,
            reuseClassificationReference, classificationReferenceUuid,
            reuseTaxa, reuseTaxonSecundum, taxonSecundumUuid,
            reuseParentChildReference, parentChildReferenceUuid,
            relationTypeToOldTaxon,
            reuseNames);
    }


// ******************************** CONSTRUCTOR ********************************/

    private SubtreeCloneConfigurator(Set<UUID> subTreeUuids, String classificationName,
            boolean reuseClassificationReference, UUID classificationReferenceUuid,
            boolean reuseTaxa, boolean reuseTaxonSecundum, UUID taxonSecundumUuid,
            boolean reuseParentChildReference, UUID parentChildReferenceUuid,
            TaxonRelationshipType relationTypeToOldTaxon,
            boolean reuseNames) {
        this.subTreeUuids = subTreeUuids;
        this.classificationName = classificationName;
        this.reuseClassificationReference = reuseClassificationReference;
        this.classificationReferenceUuid = classificationReferenceUuid;
        this.reuseTaxa = reuseTaxa;
        this.reuseTaxonSecundum = reuseTaxonSecundum;
        this.taxonSecundumUuid = taxonSecundumUuid;
        this.reuseParentChildReference = reuseParentChildReference;
        this.parentChildReferenceUuid = parentChildReferenceUuid;
        this.relationTypeToOldTaxon = relationTypeToOldTaxon;
        this.reuseNames = reuseNames;
    }

// ******************** GETTER / SETTER ********************************/

    public Set<UUID> getSubTreeUuids() {
        return subTreeUuids;
    }
    public void setSubTreeUuids(Set<UUID> subTreeUuid) {
        this.subTreeUuids = subTreeUuid;
    }

    public String getClassificationName() {
        return classificationName;
    }
    public void setClassificationName(String classificationName) {
        this.classificationName = classificationName;
    }

    public UUID getClassificationReferenceUuid() {
        return classificationReferenceUuid;
    }
    public void setClassificationReferenceUuid(UUID classificationReferenceUuid) {
        this.classificationReferenceUuid = classificationReferenceUuid;
    }

    public UUID getTaxonSecundumUuid() {
        return taxonSecundumUuid;
    }
    public void setTaxonSecundumUuid(UUID taxonSecundumUuid) {
        this.taxonSecundumUuid = taxonSecundumUuid;
    }

    public UUID getParentChildReferenceUuid() {
        return parentChildReferenceUuid;
    }
    public void setParentChildReferenceUuid(UUID parentChildReferenceUuid) {
        this.parentChildReferenceUuid = parentChildReferenceUuid;
    }

    public TaxonRelationshipType getRelationTypeToOldTaxon() {
        return relationTypeToOldTaxon;
    }
    public void setRelationTypeToOldTaxon(TaxonRelationshipType relationTypeToOldTaxon) {
        this.relationTypeToOldTaxon = relationTypeToOldTaxon;
    }

    public boolean isReuseTaxa() {
        return reuseTaxa;
    }
    public void setReuseTaxa(boolean reuseTaxa) {
        this.reuseTaxa = reuseTaxa;
    }

    public boolean isReuseNames() {
        return reuseNames;
    }
    /**
     * @param reuseNames
     * @deprecated  as it is not fully implemented yet and generally not recommended
     *          to clone names in a database; see comments in #9349 why name cloning
     *          needs to be done with care
     */
    @Deprecated
    public void setReuseNames(boolean reuseNames) {
        this.reuseNames = reuseNames;
    }
    public boolean isReuseClassificationReference() {
        return reuseClassificationReference;
    }
    public void setReuseClassificationReference(boolean reuseClassificationReference) {
        this.reuseClassificationReference = reuseClassificationReference;
    }
    public boolean isReuseTaxonSecundum() {
        return reuseTaxonSecundum;
    }
    public void setReuseTaxonSecundum(boolean reuseTaxonSecundum) {
        this.reuseTaxonSecundum = reuseTaxonSecundum;
    }
    public boolean isReuseParentChildReference() {
        return reuseParentChildReference;
    }
    public void setReuseParentChildReference(boolean reuseParentChildReference) {
        this.reuseParentChildReference = reuseParentChildReference;
    }

    public boolean isRelationDoubtful() {
        return relationDoubtful;
    }

    public void setRelationDoubtful(boolean relationDoubtful) {
        this.relationDoubtful = relationDoubtful;
    }

    public UUID getRelationshipReferenceUuid() {
        return relationshipReferenceUuid;
    }

    public void setRelationshipReferenceUuid(UUID relationshipReferenceUuid) {
        this.relationshipReferenceUuid = relationshipReferenceUuid;
    }

    public Reference getClassificationReference() {
        return classificationReference;
    }

    /**
     * Sets the reference for the classification reference. Also the {@link #getClassificationReferenceUuid()
     * classification reference uuid} is set by this method.<BR>
     * This parameter is used only if <code>{@link #isReuseClassificationReference() reuse classification reference} == false</code>
     * <BR>
     * NOTE: Only use persistent references if configurator is not used within a single transaction.
     * @param parentChildReference
     */
    public void setClassificationReference(Reference classificationReference) {
        this.classificationReference = classificationReference;
    }

    public Reference getTaxonSecundum() {
        return taxonSecundum;
    }
    /**
     * Sets the taxon secundum reference. Also the {@link #getTaxonSecundumUuid() taxon secundum reference uuid} is set by this method.<BR>
     * This parameter is used only if <code>{@link #isReuseTaxa() reuseTaxa} == false && {@link #isReuseTaxonSecundum()
     * reuseTaxonSecundum} == false</code>
     * <BR>
     * NOTE: Only use persistent references if configurator is not used within a single transaction.
     * @param taxonSecundum
     */
    public void setTaxonSecundum(Reference taxonSecundum) {
        this.taxonSecundum = taxonSecundum;
        this.taxonSecundumUuid = taxonSecundum == null ? null : taxonSecundum.getUuid();
    }

    public Reference getParentChildReference() {
        return parentChildReference;
    }

    /**
     * Sets the reference for the parent child relationship. Also the {@link #getParentChildReferenceUuid()
     * parent child reference uuid} is set by this method.<BR>
     * This parameter is used only if <code>{@link #isReuseParentChildReference() reuse parent child reference} == false</code>
     * <BR>
     * NOTE: Only use persistent references if configurator is not used within a single transaction.
     * @param parentChildReference
     */
    public void setParentChildReference(Reference parentChildReference) {
        this.parentChildReference = parentChildReference;
    }

    public Reference getRelationshipReference() {
        return relationshipReference;
    }

    /**
     * Sets the reference for the new taxon to old taxon {@link TaxonRelationship taxon relationship}.
     * Also the {@link #getRelationshipReferenceUuid() relationship reference uuid} is set by this method.<BR>
     * This parameter is used only if <code>{@link #relationTypeToOldTaxon() relationTypeToOldTaxon} != null</code>
     * <BR>
     * NOTE: Only use persistent references if configurator is not used within a single transaction.
     * @param parentChildReference
     */
    public void setRelationshipReference(Reference relationshipReference) {
        this.relationshipReference = relationshipReference;
    }

    public boolean isIncludeSynonymsIncludingManAndProParte() {
        return this.includeSynonymsIncludingManAndProParte;
    }
    /**
     * If <code>true</code> the synonyms relationships of this taxon are cloned and attached to the new taxon.
     * <BR>
     * This parameter is used only if <code>{@link #isReuseTaxa() reuseTaxa} == false</code>
     */
    public void setIncludeSynonymsIncludingManAndProParte(boolean includeSynonyms) {
        this.includeSynonymsIncludingManAndProParte = includeSynonyms;
    }

    public boolean isIncludeDescriptiveData() {
        return includeDescriptiveData;
    }
    /**
     * If <code>true</code> the descriptive data attached to this taxon are included in the copy
     * and attached to the new taxon.
     * <BR>
     * This parameter is used only if <code>{@link #isReuseTaxa() reuseTaxa} == false</code>
     */
    public void setIncludeDescriptiveData(boolean includeDescriptiveData) {
        this.includeDescriptiveData = includeDescriptiveData;
    }

    public boolean isIncludeMedia() {
        return includeMedia;
    }
    /**
     * If <code>true</code> the media attached to this taxon are also attached to the new taxon.
     * Media itself are always reused.
     * <BR>
     * This parameter is used only if <code>{@link #isReuseTaxa() reuseTaxa} == false</code>
     */
    public void setIncludeMedia(boolean includeMedia) {
        this.includeMedia = includeMedia;
    }

    public boolean isIncludeTaxonRelationshipsExcludingManAndProParte() {
        return includeTaxonRelationshipsExcludingManAndProParte;
    }
    /**
     * If <code>true</code> the taxon (concept) relationships to and from this taxon are also cloned.
     * This includes all taxon relationships except those for {@link TaxonRelationshipType#isAnyMisappliedName() any misapplied names}
     * and {@link TaxonRelationshipType#isAnySynonym() any (pro parte synonyms)}.
     * <BR>
     * This parameter is used only if <code>{@link #isReuseTaxa() reuseTaxa} == false</code>
     */
    public void setIncludeTaxonRelationshipsExcludingManAndProParte(boolean includeTaxonRelationshipsExcludingManAndProParte) {
        this.includeTaxonRelationshipsExcludingManAndProParte = includeTaxonRelationshipsExcludingManAndProParte;
    }
}
