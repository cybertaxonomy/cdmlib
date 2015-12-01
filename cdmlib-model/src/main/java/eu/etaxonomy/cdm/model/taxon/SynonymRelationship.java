/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.IRelated;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.validation.Level2;
import eu.etaxonomy.cdm.validation.Level3;
import eu.etaxonomy.cdm.validation.annotation.HomotypicSynonymsShouldBelongToGroup;

/**
 * The class representing the assignation of a {@link Synonym synonym} to an
 * ("accepted/correct") {@link Taxon taxon}. This includes a {@link SynonymRelationshipType synonym relationship type}
 * (for instance "heterotypic synonym of"). Within a synonym relationship the
 * synonym plays the source role and the taxon the target role. Between a
 * synonym and an ("accepted/correct") taxon there should exist at most one
 * synonym relationship.<BR>
 * Both, synonym and ("accepted/correct") taxon, must have the same
 * {@link TaxonBase#getSec() concept reference}.
 * <P>
 * This class corresponds in part to: <ul>
 * <li> Relationship according to the TDWG ontology
 * <li> TaxonRelationship according to the TCS
 * </ul>
 *
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:55
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SynonymRelationship", propOrder = {
    "relatedFrom",
    "relatedTo",
    "type",
    "proParte",
    "partial"
})
@XmlRootElement(name = "SynonymRelationship")
@Entity
@Audited
@HomotypicSynonymsShouldBelongToGroup(groups = Level3.class)
public class SynonymRelationship extends RelationshipBase<Synonym, Taxon, SynonymRelationshipType> {
    private static final long serialVersionUID = 1615082389452680243L;
    private static final Logger logger = Logger.getLogger(SynonymRelationship.class);

    @XmlElement(name = "IsProParte")
    private boolean proParte = false;

    @XmlElement(name = "IsPartial")
    private boolean partial = false;

    @XmlElement(name = "RelatedFrom")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.EAGER)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    @NotNull(groups = Level2.class)
    @Valid
    private Synonym relatedFrom;

    @XmlElement(name = "RelatedTo")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.EAGER)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    @NotNull(groups = Level2.class)
    @Valid
    private Taxon relatedTo;

    @XmlElement(name = "Type")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.EAGER)
    private SynonymRelationshipType type;

    /**
     * @deprecated for hibernate only, don't use
     */
    @Deprecated
    private SynonymRelationship(){
    }

    /**
     * Class constructor: creates a new synonym relationship instance (with the
     * given {@link Synonym synonym}, the given "accepted/correct" {@link Taxon taxon},
     * the given {@link SynonymRelationshipType synonym relationship type} and with the
     * {@link eu.etaxonomy.cdm.model.reference.Reference reference source} on which the relationship assertion is based).
     * Moreover the new synonym relationship will be added to the respective
     * sets of synonym relationships assigned to the synonym and to the
     * "accepted/correct" taxon.
     *
     * @param synonym 					the synonym instance involved in the new synonym relationship
     * @param taxon						the taxon instance involved in the new synonym relationship
     * @param type						the synonym relationship type of the new synonym relationship
     * @param citation					the reference source for the new synonym relationship
     * @param citationMicroReference	the string with the details describing the exact localisation within the reference
     * @see 							eu.etaxonomy.cdm.model.common.RelationshipBase#RelationshipBase(IRelated, IRelated, RelationshipTermBase, Reference, String)
     */
    protected SynonymRelationship(Synonym synonym, Taxon taxon, SynonymRelationshipType type, Reference citation, String citationMicroReference) {
        super(synonym, taxon, type, citation, citationMicroReference);
        if (type != null && type.equals(SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF()) && taxon != null && taxon.getName() != null && synonym != null && synonym != null){
            taxon.getName().getHomotypicalGroup().addTypifiedName(synonym.getName());
        }
    }


    /**
     * Returns "true" if the ProParte flag is set. This indicates that, within
     * <i>this</i> synonym relationship, the {@link name.TaxonNameBase taxon name} used as a
     * {@link Synonym synonym} designated originally a real taxon which later has
     * been split. In this case the synonym is therefore the synonym of at least
     * two different ("accepted/correct") {@link Taxon taxa} and at least one
     * more synonym relationship with the same synonym should exist.
     */
    public boolean isProParte() {
        return proParte;
    }

    /**
     * @see #isProParte()
     */
    public void setProParte(boolean proParte) {
        this.proParte = proParte;
    }

    /**
     * Returns "true" if the ProParte flag is set. This indicates that, within
     * <i>this</i> synonym relationship, the {@link name.TaxonNameBase taxon name} used as a
     * {@link Synonym synonym} designated originally a real taxon which later has
     * been lumped together with another one. In this case the
     * ("accepted/correct") {@link Taxon taxon} has therefore at least
     * two different synonyms (for the two lumped real taxa) and at least one
     * more synonym relationship with the same ("accepted/correct") taxon should
     * exist.
     */
    public boolean isPartial() {
        return partial;
    }

    /**
     * @see #isPartial()
     */
    public void setPartial(boolean partial) {
        this.partial = partial;
    }

    /**
     * Returns the ("accepted/correct") {@link Taxon taxon} involved in <i>this</i>
     * synonym relationship. The taxon plays the target role in the relationship.
     *
     * @see    #getSynonym()
     * @see    Synonym#getAcceptedTaxa()
     * @see    eu.etaxonomy.cdm.model.common.RelationshipBase#getRelatedTo()
     * @see    eu.etaxonomy.cdm.model.common.RelationshipBase#getType()
     */

    @Transient
    public Taxon getAcceptedTaxon(){
        return this.getRelatedTo();
    }

    /**
     * Sets the given ("accepted/valid") {@link Taxon taxon} to <i>this</i>
     * synonym relationship. Therefore <i>this</i> synonym relationship will be
     * added to the corresponding set of synonym relationships assigned to the
     * given taxon.
     *
     * @param acceptedTaxon	the taxon instance to be set in <i>this</i> synonym relationship
     * @see   				#getAcceptedTaxon()
     * @see   				Taxon#getSynonymRelations()
     */
    public void setAcceptedTaxon(Taxon acceptedTaxon){
        this.setRelatedTo(acceptedTaxon);
    }

    /**
     * Returns the {@link Synonym synonym} involved in <i>this</i> synonym
     * relationship. The synonym plays the source role in the relationship.
     *
     * @see    #getAcceptedTaxon()
     * @see    Taxon#getSynonyms()
     * @see    eu.etaxonomy.cdm.model.common.RelationshipBase#getRelatedFrom()
     * @see    eu.etaxonomy.cdm.model.common.RelationshipBase#getType()
     */
    @Transient
    public Synonym getSynonym(){
        return this.getRelatedFrom();
    }
    /**
     * Sets the given {@link Synonym synonym} to <i>this</i> synonym relationship.
     * Therefore <i>this</i> synonym relationship will be
     * added to the corresponding set of synonym relationships assigned to the
     * given synonym. Furthermore if the given synonym replaces an "old" one
     * <i>this</i> synonym relationship will be removed from the set of synonym
     * relationships assigned to the "old" synonym.
     *
     * @param synonym	the synonym instance to be set in <i>this</i> synonym relationship
     * @see    			#getSynonym()
     * @see   			Synonym#getSynonymRelations()
     */
    public void setSynonym(Synonym synonym){
        this.setRelatedFrom(synonym);
    }

    //FIXME Why was this protected - especially since setSynonym is public,
    // making relatedFrom inaccessible outside the package
    @Override
    protected Synonym getRelatedFrom() {
        return relatedFrom;
    }

    //FIXME Why was this protected - especially since setAcceptedTaxon is public,
    // making relatedTo inaccessible outside the package
    @Override
    protected Taxon getRelatedTo() {
        return relatedTo;
    }

    @Override
    public SynonymRelationshipType getType() {
        return type;
    }

    @Override
    protected void setRelatedFrom(Synonym relatedFrom) {
        if (relatedFrom == null){
         //   this.deletedObjects.add(this.relatedFrom);
        }
        this.relatedFrom = relatedFrom;
    }

    @Override
    protected void setRelatedTo(Taxon relatedTo) {
        if (relatedTo == null){
          //  this.deletedObjects.add(this.relatedTo);
        }
        this.relatedTo = relatedTo;
    }

    @Override
    public void setType(SynonymRelationshipType type) {
        this.type = type;
    }

    //*********************************** CLONE *****************************************/

    /**
     * Clones <i>this</i> SynonymRelationship. This is a shortcut that enables to create
     * a new instance that differs only slightly from <i>this</i> SynonymRelationship by
     * modifying only some of the attributes.
     *
     * @see eu.etaxonomy.cdm.model.common.RelationshipBase#clone()
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {
        SynonymRelationship result;

        try{
            result = (SynonymRelationship) super.clone();
            //no changes to relatedFrom, relatedTo, type, partial, proParte

            return result;
        } catch (CloneNotSupportedException e) {
            logger.warn("Object does not implement cloneable");
            e.printStackTrace();
            return null;
        }
    }
}