/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

import java.util.List;
import java.util.Set;

import javax.persistence.Transient;

import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IParsable;
import eu.etaxonomy.cdm.model.common.IRelated;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonComparator;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.match.IMatchable;

/**
 *
 * The upmost interface for taxon names.
 * <P>
 * This class corresponds partially to: <ul>
 * <li> TaxonName according to the TDWG ontology
 * <li> ScientificName and CanonicalName according to the TCS
 * <li> ScientificName according to the ABCD schema
 * </ul>
 *
 * ITaxonNameBase and it's extensions should only be used for type safety
 * of {@link TaxonName} instances. It should not be used to interface
 * instances of any other class
 *
 * @author a.mueller
 * @since 21.01.2017
 *
 */
public interface ITaxonNameBase
        extends IIdentifiableEntity, IParsable, IRelated, IMatchable, Cloneable{


    public NomenclaturalCode getNameType();
    public void setNameType(NomenclaturalCode nameType);

    public Object clone();

    public String generateFullTitle();

    @Transient
    public String getFullTitleCache();

    @Transient
    public NomenclaturalCode getNomenclaturalCode();

    public List<TaggedText> getTaggedName();

    public void setFullTitleCache(String fullTitleCache);

    public void setFullTitleCache(String fullTitleCache, boolean protectCache);

    public boolean isProtectedFullTitleCache();

    void setProtectedFullTitleCache(boolean protectedFullTitleCache);

    /**
     * Returns the set of all {@link NameRelationship name relationships}
     * in which <i>this</i> taxon name is involved. A taxon name can be both source
     * in some name relationships or target in some others.
     *
     * @see    #getRelationsToThisName()
     * @see    #getRelationsFromThisName()
     * @see    #addNameRelationship(NameRelationship)
     * @see    #addRelationshipToName(TaxonName, NameRelationshipType, String)
     * @see    #addRelationshipFromName(TaxonName, NameRelationshipType, String)
     */
    public Set<NameRelationship> getNameRelations();

    /**
     * Creates a new {@link NameRelationship#NameRelationship(TaxonName, TaxonName, NameRelationshipType, String) name relationship} from <i>this</i> taxon name to another taxon name
     * and adds it both to the set of {@link #getRelationsFromThisName() relations from <i>this</i> taxon name} and
     * to the set of {@link #getRelationsToThisName() relations to the other taxon name}.
     *
     * @param toName          the taxon name of the target for this new name relationship
     * @param type            the type of this new name relationship
     * @param ruleConsidered  the string which specifies the rule on which this name relationship is based
     * @return
     * @see                   #getRelationsToThisName()
     * @see                   #getNameRelations()
     * @see                   #addRelationshipFromName(TaxonName, NameRelationshipType, String)
     * @see                   #addNameRelationship(NameRelationship)
     */
    public NameRelationship addRelationshipToName(TaxonName toName, NameRelationshipType type, String ruleConsidered);

    /**
     * Creates a new {@link NameRelationship#NameRelationship(TaxonName, TaxonName, NameRelationshipType, String) name relationship} from <i>this</i> taxon name to another taxon name
     * and adds it both to the set of {@link #getRelationsFromThisName() relations from <i>this</i> taxon name} and
     * to the set of {@link #getRelationsToThisName() relations to the other taxon name}.
     *
     * @param toName          the taxon name of the target for this new name relationship
     * @param type            the type of this new name relationship
     * @param ruleConsidered  the string which specifies the rule on which this name relationship is based
     * @return
     * @see                   #getRelationsToThisName()
     * @see                   #getNameRelations()
     * @see                   #addRelationshipFromName(TaxonName, NameRelationshipType, String)
     * @see                   #addNameRelationship(NameRelationship)
     */
    public NameRelationship addRelationshipToName(TaxonName toName, NameRelationshipType type, Reference citation,
            String microCitation, String ruleConsidered);

    /**
     * Creates a new {@link NameRelationship#NameRelationship(TaxonName, TaxonName, NameRelationshipType, String) name relationship} from another taxon name to <i>this</i> taxon name
     * and adds it both to the set of {@link #getRelationsToThisName() relations to <i>this</i> taxon name} and
     * to the set of {@link #getRelationsFromThisName() relations from the other taxon name}.
     *
     * @param fromName        the taxon name of the source for this new name relationship
     * @param type            the type of this new name relationship
     * @param ruleConsidered  the string which specifies the rule on which this name relationship is based
     * @param citation        the reference in which this relation was described
     * @param microCitation   the reference detail for this relation (e.g. page)
     * @see                   #getRelationsFromThisName()
     * @see                   #getNameRelations()
     * @see                   #addRelationshipToName(TaxonName, NameRelationshipType, String)
     * @see                   #addNameRelationship(NameRelationship)
     */
    public NameRelationship addRelationshipFromName(TaxonName fromName, NameRelationshipType type, String ruleConsidered);

    /**
     * Creates a new {@link NameRelationship#NameRelationship(TaxonName, TaxonName, NameRelationshipType, String) name relationship} from another taxon name to <i>this</i> taxon name
     * and adds it both to the set of {@link #getRelationsToThisName() relations to <i>this</i> taxon name} and
     * to the set of {@link #getRelationsFromThisName() relations from the other taxon name}.
     *
     * @param fromName        the taxon name of the source for this new name relationship
     * @param type            the type of this new name relationship
     * @param ruleConsidered  the string which specifies the rule on which this name relationship is based
     * @param citation        the reference in which this relation was described
     * @param microCitation   the reference detail for this relation (e.g. page)
     * @see                   #getRelationsFromThisName()
     * @see                   #getNameRelations()
     * @see                   #addRelationshipToName(TaxonName, NameRelationshipType, String)
     * @see                   #addNameRelationship(NameRelationship)
     */
    public NameRelationship addRelationshipFromName(TaxonName fromName, NameRelationshipType type, Reference citation,
            String microCitation, String ruleConsidered);

    /**
     * Removes one {@link NameRelationship name relationship} from one of both sets of
     * {@link #getNameRelations() name relationships} in which <i>this</i> taxon name is involved.
     * The name relationship will also be removed from one of both sets belonging
     * to the second taxon name involved. Furthermore the fromName and toName
     * attributes of the name relationship object will be nullified.
     *
     * @param  nameRelation  the name relationship which should be deleted from one of both sets
     * @see                  #getNameRelations()
     */
    public void removeNameRelationship(NameRelationship nameRelation);

    public void removeRelationToTaxonName(TaxonName toTaxonName);

    /**
     * Returns the set of all {@link NameRelationship name relationships}
     * in which <i>this</i> taxon name is involved as a source ("from"-side).
     *
     * @see    #getNameRelations()
     * @see    #getRelationsToThisName()
     * @see    #addRelationshipFromName(TaxonName, NameRelationshipType, String)
     */
    public Set<NameRelationship> getRelationsFromThisName();

    /**
     * Returns the set of all {@link NameRelationship name relationships}
     * in which <i>this</i> taxon name is involved as a target ("to"-side).
     *
     * @see    #getNameRelations()
     * @see    #getRelationsFromThisName()
     * @see    #addRelationshipToName(TaxonName, NameRelationshipType, String)
     */
    public Set<NameRelationship> getRelationsToThisName();

    /**
     * Returns the set of {@link NomenclaturalStatus nomenclatural status} assigned
     * to <i>this</i> taxon name according to its corresponding nomenclature code.
     * This includes the {@link NomenclaturalStatusType type} of the nomenclatural status
     * and the nomenclatural code rule considered.
     *
     * @see     NomenclaturalStatus
     * @see     NomenclaturalStatusType
     */
    public Set<NomenclaturalStatus> getStatus();

    /**
     * Adds a new {@link NomenclaturalStatus nomenclatural status}
     * to <i>this</i> taxon name's set of nomenclatural status.
     *
     * @param  nomStatus  the nomenclatural status to be added
     * @see               #getStatus()
     */
    public void addStatus(NomenclaturalStatus nomStatus);

    public NomenclaturalStatus addStatus(NomenclaturalStatusType statusType, Reference citation, String microCitation);

    /**
     * Removes one element from the set of nomenclatural status of <i>this</i> taxon name.
     * Type and ruleConsidered attributes of the nomenclatural status object
     * will be nullified.
     *
     * @param  nomStatus  the nomenclatural status of <i>this</i> taxon name which should be deleted
     * @see               #getStatus()
     */
    public void removeStatus(NomenclaturalStatus nomStatus);

    /**
     * Indicates whether <i>this</i> taxon name is a {@link NameRelationshipType#BASIONYM() basionym}
     * or a {@link NameRelationshipType#REPLACED_SYNONYM() replaced synonym}
     * of any other taxon name. Returns "true", if a basionym or a replaced
     * synonym {@link NameRelationship relationship} from <i>this</i> taxon name to another taxon name exists,
     * false otherwise (also in case <i>this</i> taxon name is the only one in the
     * homotypical group).
     */
    public boolean isOriginalCombination();

    /**
     * Indicates <i>this</i> taxon name is a {@link NameRelationshipType#REPLACED_SYNONYM() replaced synonym}
     * of any other taxon name. Returns "true", if a replaced
     * synonym {@link NameRelationship relationship} from <i>this</i> taxon name to another taxon name exists,
     * false otherwise (also in case <i>this</i> taxon name is the only one in the
     * homotypical group).
     */
    public boolean isReplacedSynonym();

    /**
     * Returns the taxon name which is the {@link NameRelationshipType#BASIONYM() basionym} of <i>this</i> taxon name.
     * The basionym of a taxon name is its epithet-bringing synonym.
     * For instance <i>Pinus abies</i> L. was published by Linnaeus and the botanist
     * Karsten transferred later <i>this</i> taxon to the genus Picea. Therefore,
     * <i>Pinus abies</i> L. is the basionym of the new combination <i>Picea abies</i> (L.) H. Karst.
     *
     * If more than one basionym exists one is choosen at radom.
     *
     * If no basionym exists null is returned.
     */
    public TaxonName getBasionym();

    /**
     * Returns the set of taxon names which are the {@link NameRelationshipType#BASIONYM() basionyms} of <i>this</i> taxon name.
     * The basionym of a taxon name is its epithet-bringing synonym.
     * For instance <i>Pinus abies</i> L. was published by Linnaeus and the botanist
     * Karsten transferred later <i>this</i> taxon to the genus Picea. Therefore,
     * <i>Pinus abies</i> L. is the basionym of the new combination <i>Picea abies</i> (L.) H. Karst.
     */
    public Set<TaxonName> getBasionyms();

    /**
     * Assigns a taxon name as {@link NameRelationshipType#BASIONYM() basionym} of <i>this</i> taxon name.
     * The basionym {@link NameRelationship relationship} will be added to <i>this</i> taxon name
     * and to the basionym. The basionym cannot have itself a basionym.
     * The {@link HomotypicalGroup homotypical groups} of <i>this</i> taxon name and of the basionym
     * will be {@link HomotypicalGroup#merge(HomotypicalGroup) merged}.
     *
     * @param  basionym     the taxon name to be set as the basionym of <i>this</i> taxon name
     * @see                 #getBasionym()
     * @see                 #addBasionym(TaxonName, String)
     */
    public void addBasionym(TaxonName basionym);

    /**
     * Assigns a taxon name as {@link NameRelationshipType#BASIONYM() basionym} of <i>this</i> taxon name
     * and keeps the nomenclatural rule considered for it. The basionym
     * {@link NameRelationship relationship} will be added to <i>this</i> taxon name and to the basionym.
     * The basionym cannot have itself a basionym.
     * The {@link HomotypicalGroup homotypical groups} of <i>this</i> taxon name and of the basionym
     * will be {@link HomotypicalGroup#merge(HomotypicalGroup) merged}.
     *
     * @param  basionym         the taxon name to be set as the basionym of <i>this</i> taxon name
     * @param  ruleConsidered   the string identifying the nomenclatural rule
     * @return
     * @see                     #getBasionym()
     * @see                     #addBasionym(TaxonName)
     */
    public NameRelationship addBasionym(TaxonName basionym, Reference citation, String microcitation, String ruleConsidered);

    /**
     * Returns the set of taxon names which are the {@link NameRelationshipType#REPLACED_SYNONYM() replaced synonyms} of <i>this</i> taxon name.
     *
     */
    public Set<TaxonName> getReplacedSynonyms();

    /**
     * Assigns a taxon name as {@link NameRelationshipType#REPLACED_SYNONYM() replaced synonym} of <i>this</i> taxon name
     * and keeps the nomenclatural rule considered for it. The replaced synonym
     * {@link NameRelationship relationship} will be added to <i>this</i> taxon name and to the replaced synonym.
     * The {@link HomotypicalGroup homotypical groups} of <i>this</i> taxon name and of the replaced synonym
     * will be {@link HomotypicalGroup#merge(HomotypicalGroup) merged}.
     *
     * @param  basionym         the taxon name to be set as the basionym of <i>this</i> taxon name
     * @param  ruleConsidered   the string identifying the nomenclatural rule
     * @see                     #getBasionym()
     * @see                     #addBasionym(TaxonName)
     */
    //TODO: Check if true: The replaced synonym cannot have itself a replaced synonym (?).
    public void addReplacedSynonym(TaxonName replacedSynonym, Reference citation, String microcitation, String ruleConsidered);

    /**
     * Removes the {@link NameRelationshipType#BASIONYM() basionym} {@link NameRelationship relationship} from the set of
     * {@link #getRelationsToThisName() name relationships to} <i>this</i> taxon name. The same relationhip will be
     * removed from the set of {@link #getRelationsFromThisName() name relationships from} the taxon name
     * previously used as basionym.
     *
     * @see   #getBasionym()
     * @see   #addBasionym(TaxonName)
     */
    public void removeBasionyms();

    /**
     * Returns the taxonomic {@link Rank rank} of <i>this</i> taxon name.
     *
     * @see     Rank
     */
    public Rank getRank();

    /**
     * @see  #getRank()
     */
    public void setRank(Rank rank);

    /**
     * Returns the {@link eu.etaxonomy.cdm.model.reference.INomenclaturalReference nomenclatural reference} of <i>this</i> taxon name.
     * The nomenclatural reference is here meant to be the one publication
     * <i>this</i> taxon name was originally published in while fulfilling the formal
     * requirements as specified by the corresponding {@link NomenclaturalCode nomenclatural code}.
     *
     * @see     eu.etaxonomy.cdm.model.reference.INomenclaturalReference
     * @see     eu.etaxonomy.cdm.model.reference.Reference
     */
    public INomenclaturalReference getNomenclaturalReference();

    /**
     * Assigns a {@link eu.etaxonomy.cdm.model.reference.INomenclaturalReference nomenclatural reference} to <i>this</i> taxon name.
     * The corresponding {@link eu.etaxonomy.cdm.model.reference.Reference.isNomenclaturallyRelevant nomenclaturally relevant flag} will be set to true
     * as it is obviously used for nomenclatural purposes.
     *
     * @throws IllegalArgumentException if parameter <code>nomenclaturalReference</code> is not assignable from {@link INomenclaturalReference}
     * @see  #getNomenclaturalReference()
     */
    public void setNomenclaturalReference(INomenclaturalReference nomenclaturalReference);

    /**
     * Returns the appended phrase string assigned to <i>this</i> taxon name.
     * The appended phrase is a non-atomised addition to a name. It is
     * not ruled by a nomenclatural code.
     */
    public String getAppendedPhrase();

    /**
     * @see  #getAppendedPhrase()
     */
    public void setAppendedPhrase(String appendedPhrase);

    /**
     * Returns the details string of the {@link #getNomenclaturalReference() nomenclatural reference} assigned
     * to <i>this</i> taxon name. The details describe the exact localisation within
     * the publication used as nomenclature reference. These are mostly
     * (implicitly) pages but can also be figures or tables or any other
     * element of a publication. A nomenclatural micro reference (details)
     * requires the existence of a nomenclatural reference.
     */
    //Details of the nomenclatural reference (protologue).
    public String getNomenclaturalMicroReference();

    /**
     * @see  #getNomenclaturalMicroReference()
     */
    public void setNomenclaturalMicroReference(String nomenclaturalMicroReference);

    /**
     * @param warnings
     */
    public void addParsingProblems(int problems);

    /**
     * Returns the set of {@link TypeDesignationBase type designations} assigned
     * to <i>this</i> taxon name.
     * @see     NameTypeDesignation
     * @see     SpecimenTypeDesignation
     */
    public Set<TypeDesignationBase> getTypeDesignations();

    /**
     * Removes one element from the set of {@link TypeDesignationBase type designations} assigned to
     * <i>this</i> taxon name. The type designation itself will be nullified.
     *
     * @param  typeDesignation  the type designation which should be deleted
     */
    public void removeTypeDesignation(TypeDesignationBase typeDesignation);

    /**
     * Returns the set of {@link SpecimenTypeDesignation specimen type designations} assigned
     * to <i>this</i> taxon name. The {@link Rank rank} of <i>this</i> taxon name is generally
     * "species" or below. The specimen type designations include all the
     * specimens on which the typification of this name is based (which are
     * exclusively used to typify taxon names belonging to the same
     * {@link HomotypicalGroup homotypical group} to which <i>this</i> taxon name
     * belongs) and eventually the status of these designations.
     *
     * @see     SpecimenTypeDesignation
     * @see     NameTypeDesignation
     * @see     HomotypicalGroup
     */
    public Set<SpecimenTypeDesignation> getSpecimenTypeDesignationsOfHomotypicalGroup();

    /**
     * Returns the set of {@link NameTypeDesignation name type designations} assigned
     * to <i>this</i> taxon name the rank of which must be above "species".
     * The name type designations include all the taxon names used to typify
     * <i>this</i> taxon name and eventually the rejected or conserved status
     * of these designations.
     *
     * @see     NameTypeDesignation
     * @see     SpecimenTypeDesignation
     */
    public Set<NameTypeDesignation> getNameTypeDesignations();

    /**
     * Creates and adds a new {@link NameTypeDesignation name type designation}
     * to <i>this</i> taxon name's set of type designations.
     *
     * @param  typeSpecies              the taxon name to be used as type of <i>this</i> taxon name
     * @param  citation                 the reference for this new designation
     * @param  citationMicroReference   the string with the details (generally pages) within the reference
     * @param  originalNameString       the taxon name string used in the reference to assert this designation
     * @param  isRejectedType           the boolean status for a rejected name type designation
     * @param  isConservedType          the boolean status for a conserved name type designation
     * @param  isLectoType              the boolean status for a lectotype name type designation
     * @param  isNotDesignated          the boolean status for a name type designation without name type
     * @param  addToAllHomotypicNames   the boolean indicating whether the name type designation should be
     *                                  added to all taxon names of the homotypical group this taxon name belongs to
     * @return
     * @see                             #getNameTypeDesignations()
     * @see                             NameTypeDesignation
     * @see                             TypeDesignationBase#isNotDesignated()
     */
    public NameTypeDesignation addNameTypeDesignation(TaxonName typeSpecies, Reference citation,
            String citationMicroReference, String originalNameString, NameTypeDesignationStatus status,
            boolean isRejectedType, boolean isConservedType,
            /*boolean isLectoType, */
            boolean isNotDesignated, boolean addToAllHomotypicNames);

    /**
     * Creates and adds a new {@link NameTypeDesignation name type designation}
     * to <i>this</i> taxon name's set of type designations.
     *
     * @param  typeSpecies              the taxon name to be used as type of <i>this</i> taxon name
     * @param  citation                 the reference for this new designation
     * @param  citationMicroReference   the string with the details (generally pages) within the reference
     * @param  originalNameString       the taxon name string used in the reference to assert this designation
     * @param  status                   the name type designation status
     * @param  addToAllHomotypicNames   the boolean indicating whether the name type designation should be
     *                                  added to all taxon names of the homotypical group this taxon name belongs to
     * @return
     * @see                             #getNameTypeDesignations()
     * @see                             NameTypeDesignation
     * @see                             TypeDesignationBase#isNotDesignated()
     */
    public NameTypeDesignation addNameTypeDesignation(TaxonName typeSpecies, Reference citation,
            String citationMicroReference, String originalNameString, NameTypeDesignationStatus status,
            boolean addToAllHomotypicNames);

    /**
     * Returns the set of {@link SpecimenTypeDesignation specimen type designations}
     * that typify <i>this</i> taxon name.
     */
    public Set<SpecimenTypeDesignation> getSpecimenTypeDesignations();

    /**
     * Creates and adds a new {@link SpecimenTypeDesignation specimen type designation}
     * to <i>this</i> taxon name's set of type designations.
     *
     * @param  typeSpecimen             the specimen to be used as a type for <i>this</i> taxon name
     * @param  status                   the specimen type designation status
     * @param  citation                 the reference for this new specimen type designation
     * @param  citationMicroReference   the string with the details (generally pages) within the reference
     * @param  originalNameString       the taxon name used in the reference to assert this designation
     * @param  isNotDesignated          the boolean status for a specimen type designation without specimen type
     * @param  addToAllHomotypicNames   the boolean indicating whether the specimen type designation should be
     *                                  added to all taxon names of the homotypical group the typified
     *                                  taxon name belongs to
     * @return
     * @see                             #getSpecimenTypeDesignations()
     * @see                             SpecimenTypeDesignationStatus
     * @see                             SpecimenTypeDesignation
     * @see                             TypeDesignationBase#isNotDesignated()
     */
    public SpecimenTypeDesignation addSpecimenTypeDesignation(DerivedUnit typeSpecimen, SpecimenTypeDesignationStatus status,
            Reference citation, String citationMicroReference, String originalNameString, boolean isNotDesignated,
            boolean addToAllHomotypicNames);

    /**
     * Adds a {@link TypeDesignationBase type designation} to <code>this</code> taxon name's set of type designations
     *
     * @param typeDesignation           the typeDesignation to be added to <code>this</code> taxon name
     * @param addToAllNames             the boolean indicating whether the type designation should be
     *                                  added to all taxon names of the homotypical group the typified
     *                                  taxon name belongs to
     * @return                          true if the operation was succesful
     *
     * @throws IllegalArgumentException if the type designation already has typified names, an {@link IllegalArgumentException exception}
     *                                  is thrown. We do this to prevent a type designation to be used for multiple taxon names.
     *
     */
    public boolean addTypeDesignation(TypeDesignationBase typeDesignation, boolean addToAllNames);

    /**
     * Returns the {@link HomotypicalGroup homotypical group} to which
     * <i>this</i> taxon name belongs. A homotypical group represents all taxon names
     * that share the same types.
     *
     * @see     HomotypicalGroup
     */

    public HomotypicalGroup getHomotypicalGroup();

    /**
     * @see #getHomotypicalGroup()
     */
    public void setHomotypicalGroup(HomotypicalGroup homotypicalGroup);

    /**
     * Returns the complete string containing the
     * {@link eu.etaxonomy.cdm.model.reference.INomenclaturalReference#getNomenclaturalCitation() nomenclatural reference citation}
     * and the {@link #getNomenclaturalMicroReference() details} assigned to <i>this</i> taxon name.
     *
     * @return  the string containing the nomenclatural reference of <i>this</i> taxon name
     * @see     eu.etaxonomy.cdm.model.reference.INomenclaturalReference#getNomenclaturalCitation()
     * @see     #getNomenclaturalReference()
     * @see     #getNomenclaturalMicroReference()
     */
    public String getCitationString();

    /**
     * Returns the string containing the publication date (generally only year)
     * of the {@link #getNomenclaturalReference() nomenclatural reference} for <i>this</i> taxon name, null if there is
     * no nomenclatural reference.
     *
     * @return  the string containing the publication date of <i>this</i> taxon name
     * @see     eu.etaxonomy.cdm.model.reference.INomenclaturalReference#getYear()
     */
    public String getReferenceYear();

    /**
     * Returns the set of {@link eu.etaxonomy.cdm.model.taxon.TaxonBase taxon bases} that refer to <i>this</i> taxon name.
     * In this context a taxon base means the use of a taxon name by a reference
     * either as a {@link eu.etaxonomy.cdm.model.taxon.Taxon taxon} ("accepted/correct" name) or
     * as a (junior) {@link eu.etaxonomy.cdm.model.taxon.Synonym synonym}.
     * A taxon name can be used by several distinct {@link eu.etaxonomy.cdm.model.reference.Reference references} but only once
     * within a taxonomic treatment (identified by one reference).
     *
     * @see #getTaxa()
     * @see #getSynonyms()
     */
    public Set<TaxonBase> getTaxonBases();

    /**
     * Adds a new {@link eu.etaxonomy.cdm.model.taxon.TaxonBase taxon base}
     * to the set of taxon bases using <i>this</i> taxon name.
     *
     * @param  taxonBase  the taxon base to be added
     * @see               #getTaxonBases()
     * @see               #removeTaxonBase(TaxonBase)
     */
    //TODO protected
    public void addTaxonBase(TaxonBase taxonBase);

    /**
     * Removes one element from the set of {@link eu.etaxonomy.cdm.model.taxon.TaxonBase taxon bases} that refer to <i>this</i> taxon name.
     *
     * @param  taxonBase    the taxon base which should be removed from the corresponding set
     * @see                 #getTaxonBases()
     * @see                 #addTaxonBase(TaxonBase)
     */
    public void removeTaxonBase(TaxonBase taxonBase);

    /**
     * Returns the set of {@link eu.etaxonomy.cdm.model.taxon.Taxon taxa} ("accepted/correct" names according to any
     * reference) that are based on <i>this</i> taxon name. This set is a subset of
     * the set returned by getTaxonBases().
     *
     * @see eu.etaxonomy.cdm.model.taxon.Taxon
     * @see #getTaxonBases()
     * @see #getSynonyms()
     */
    public Set<Taxon> getTaxa();

    /**
     * Returns the set of {@link eu.etaxonomy.cdm.model.taxon.Synonym (junior) synonyms} (according to any
     * reference) that are based on <i>this</i> taxon name. This set is a subset of
     * the set returned by getTaxonBases().
     *
     * @see eu.etaxonomy.cdm.model.taxon.Synonym
     * @see #getTaxonBases()
     * @see #getTaxa()
     */
    public Set<Synonym> getSynonyms();

    /**
     * Returns the set of {@link eu.etaxonomy.cdm.model.description.TaxonNameDescription taxon name descriptions} assigned
     * to <i>this</i> taxon name. A taxon name description is a piece of information
     * concerning the taxon name like for instance the content of its first
     * publication (protolog) or a picture of this publication.
     *
     * @see #addDescription(TaxonNameDescription)
     * @see #removeDescription(TaxonNameDescription)
     * @see eu.etaxonomy.cdm.model.description.TaxonNameDescription
     */
    public Set<TaxonNameDescription> getDescriptions();

    /**
     * Adds a new {@link eu.etaxonomy.cdm.model.description.TaxonNameDescription taxon name description}
     * to the set of taxon name descriptions assigned to <i>this</i> taxon name. The
     * content of the {@link eu.etaxonomy.cdm.model.description.TaxonNameDescription#getTaxonName() taxonName attribute} of the
     * taxon name description itself will be replaced with <i>this</i> taxon name.
     *
     * @param  description  the taxon name description to be added
     * @see                 #getDescriptions()
     * @see                 #removeDescription(TaxonNameDescription)
     */
    public void addDescription(TaxonNameDescription description);

    /**
     * Removes one element from the set of {@link eu.etaxonomy.cdm.model.description.TaxonNameDescription taxon name descriptions} assigned
     * to <i>this</i> taxon name. The content of the {@link eu.etaxonomy.cdm.model.description.TaxonNameDescription#getTaxonName() taxonName attribute}
     * of the description itself will be set to "null".
     *
     * @param  description  the taxon name description which should be removed
     * @see                 #getDescriptions()
     * @see                 #addDescription(TaxonNameDescription)
     * @see                 eu.etaxonomy.cdm.model.description.TaxonNameDescription#getTaxonName()
     */
    public void removeDescription(TaxonNameDescription description);

    public void mergeHomotypicGroups(TaxonName name);

    /**
     * Returns the boolean value indicating whether a given taxon name belongs
     * to the same {@link HomotypicalGroup homotypical group} as <i>this</i> taxon name (true)
     * or not (false). Returns "true" only if the homotypical groups of both
     * taxon names exist and if they are identical.
     *
     * @param   homoTypicName  the taxon name the homotypical group of which is to be checked
     * @return                 the boolean value of the check
     * @see                    HomotypicalGroup
     */
    public boolean isHomotypic(TaxonName homoTypicName);

    /**
     * Checks whether name is a basionym for ALL names
     * in its homotypical group.
     * Returns <code>false</code> if there are no other names in the group
     * @param name
     * @return
     */
    public boolean isGroupsBasionym();

    /**
     * Checks whether a basionym relationship exists between fromName and toName.
     *
     * @param fromName
     * @param toName
     * @return
     */
    public boolean isBasionymFor(TaxonName newCombinationName);

    /**
     * Creates a basionym relationship to all other names in this names homotypical
     * group.
     *
     * @see HomotypicalGroup.setGroupBasionym(TaxonName basionymName)
     */
    public void makeGroupsBasionym();

    //*********  Rank comparison shortcuts   ********************//
    /**
     * Returns the boolean value indicating whether the taxonomic {@link Rank rank} of <i>this</i>
     * taxon name is higher than the genus rank (true) or not (false).
     * Suprageneric non viral names are monomials.
     * Returns false if rank is null.
     *
     * @see  #isGenus()
     * @see  #isInfraGeneric()
     * @see  #isSpecies()
     * @see  #isInfraSpecific()
     * @see  #isGenusOrSupraGeneric()
     */
    public boolean isSupraGeneric();

    /**
     * Returns the boolean value indicating whether the taxonomic {@link Rank rank} of <i>this</i>
     * taxon name is the genus rank (true) or not (false). Non viral names with
     * genus rank are monomials. Returns false if rank is null.
     *
     * @see  #isSupraGeneric()
     * @see  #isInfraGeneric()
     * @see  #isSpecies()
     * @see  #isInfraSpecific()
     * @see  #isGenusOrSupraGeneric()
     */
    public boolean isGenus();


    /**
     * Returns the boolean value indicating whether the taxonomic {@link Rank rank} of <i>this</i>
     * taxon name is the genus rank or higher (<code>true</code>) or not (<code>false</code>).
     * Non viral names with
     * genus rank or higher are monomials. Returns false if rank is null.<BR>
     * This is a shortcut for {@link #isGenus()} || {@link #isSupraGeneric()}
     *
     * @see  #isGenus()
     * @see  #isSupraGeneric()
     * @see  #isInfraGeneric()
     * @see  #isSpecies()
     * @see  #isInfraSpecific()
     */
    boolean isGenusOrSupraGeneric();

    /**
     * Returns the boolean value indicating whether the taxonomic {@link Rank rank} of <i>this</i>
     * taxon name is higher than the species rank and lower than the
     * genus rank (true) or not (false). Infrageneric non viral names are
     * binomials. Returns false if rank is null.
     *
     * @see  #isSupraGeneric()
     * @see  #isGenus()
     * @see  #isSpecies()
     * @see  #isInfraSpecific()
     */
    public boolean isInfraGeneric();

    /**
     * Returns the boolean value indicating whether the taxonomic {@link Rank rank} of <i>this</i>
     * taxon name is higher than the species rank (true) or not (false).
     * Returns false if rank is null.
     *
     * @see  #isGenus()
     * @see  #isInfraGeneric()
     * @see  #isSpecies()
     * @see  #isInfraSpecific()
     */
    public boolean isSupraSpecific();

    /**
     * Returns the boolean value indicating whether the taxonomic {@link Rank rank} of <i>this</i>
     * taxon name is the species rank (true) or not (false). Non viral names
     * with species rank are binomials.
     * Returns false if rank is null.
     *
     * @see  #isSupraGeneric()
     * @see  #isGenus()
     * @see  #isInfraGeneric()
     * @see  #isInfraSpecific()
     */
    public boolean isSpecies();

    /**
     * Returns the boolean value indicating whether the taxonomic {@link Rank rank} of <i>this</i>
     * taxon name is lower than the species rank (true) or not (false).
     * Infraspecific non viral names are trinomials.
     * Returns false if rank is null.
     *
     * @see  #isSupraGeneric()
     * @see  #isGenus()
     * @see  #isInfraGeneric()
     * @see  #isSpecies()
     */
    public boolean isInfraSpecific();

    /**
     * Returns true if this name's rank indicates a rank that aggregates species like species
     * aggregates or species groups, false otherwise. This methods currently returns false
     * for all user defined ranks.
     *
     *@see Rank#isSpeciesAggregate()
     *
     * @return
     */
    public boolean isSpeciesAggregate();

    /**
     * Creates a basionym relationship between this name and
     *  each name in its homotypic group.
     *
     * @param basionymName
     */
    public void setAsGroupsBasionym();

    /**
     * Removes basionym relationship between this name and
     *  each name in its homotypic group.
     *
     * @param basionymName
     */
    public void removeAsGroupsBasionym();

    /**
     * This method compares 2 taxon names on it's name titles and caches.
     * Maybe in future more parts will be added.
     * It is not fully clear/defined how this method relates to
     * explicit comparators like {@link TaxonNameComparator}.
     * Historically it was a compareTo method in {@link IdentifiableEntity}
     * but did not fulfill the {@link Comparable} contract.
     * <BR><BR>
     * {@link  https://dev.e-taxonomy.eu/redmine/issues/922}<BR>
     * {@link https://dev.e-taxonomy.eu/redmine/issues/6311}
     *
     * @see TaxonName#compareToName(TaxonName)
     * @see TaxonNameComparator
     * @see TaxonComparator
     * @param otherTaxon
     * @return the compareTo result similar to {@link Comparable#compareTo(Object)}
     * @throws NullPointerException if otherTaxon is <code>null</code>
     */
    public int compareToName(TaxonName otherName);

    /**
     * This method returns als {@link Registration registrations} for the given name.
     * @return
     */
    public Set<Registration> getRegistrations();


    /**
     * Returns <code>true</code> if this name is of type {@link NomenclaturalCode#ICZN} or
     * any sub type and is supposed to be handled via {@link IZoologicalName}
     */
    public boolean isZoological();

    /**
     * Returns <code>true</code> if this name is of type {@link NomenclaturalCode#NonViral} or
     * any sub type and is supposed to be handled via {@link INonViralName}
     */
    public boolean isNonViral();

    /**
     * Returns <code>true</code> if this name is of type {@link NomenclaturalCode#ICNAFP} or
     * any sub type and is supposed to be handled via {@link IBotanicalName}
     */
    public boolean isBotanical();

    /**
     * Returns <code>true</code> if this name is of type {@link NomenclaturalCode#ICNCP} or
     * any sub type and is supposed to be handled via {@link ICultivarPlantName}
     */
    boolean isCultivar();

    /**
     * Returns <code>true</code> if this name is of type {@link NomenclaturalCode#ICNB} or
     * any sub type and is supposed to be handled via {@link IBacterialName}
     */
    boolean isBacterial();

    /**
     * Returns <code>true</code> if this name is of type {@link NomenclaturalCode#ICVCN} or
     * any sub type and is supposed to be handled via {@link IViralName}
     */
    boolean isViral();

}
