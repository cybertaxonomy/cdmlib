/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.validation.Level2;
import eu.etaxonomy.cdm.validation.annotation.ValidTypeDesignation;

/**
 * The (abstract) class representing a typification of one or several {@link TaxonNameBase taxon names}.<BR>
 * All taxon names which have a {@link Rank rank} "species aggregate" or lower
 * can only be typified by specimens (a {@link SpecimenTypeDesignation specimen type designation}), but taxon
 * names with a higher rank might be typified by an other taxon name with
 * rank "species" or "genus" (a {@link NameTypeDesignation name type designation}).
 *
 * @see		TaxonNameBase
 * @see		NameTypeDesignation
 * @see		SpecimenTypeDesignation
 * @author  a.mueller
 * @created 07.08.2008
 * @version 1.0
 */
@XmlRootElement(name = "TypeDesignationBase")
@XmlType(name = "TypeDesignationBase", propOrder = {
    "typifiedNames",
    "notDesignated",
    "typeStatus"
})
@XmlSeeAlso({
    NameTypeDesignation.class,
    SpecimenTypeDesignation.class
})
@Entity
@Audited
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@ValidTypeDesignation(groups=Level2.class)
public abstract class TypeDesignationBase<T extends TypeDesignationStatusBase> extends ReferencedEntityBase implements ITypeDesignation {
    private static final long serialVersionUID = 8622351017235131355L;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TypeDesignationBase.class);

    @XmlElement(name = "IsNotDesignated")
    private boolean notDesignated;

    @XmlElementWrapper(name = "TypifiedNames")
    @XmlElement(name = "TypifiedName")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY , mappedBy="typeDesignations")
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    private Set<TaxonNameBase> typifiedNames = new HashSet<TaxonNameBase>();

    @XmlElement(name = "TypeStatus")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = TypeDesignationStatusBase.class)
    private T typeStatus;

// **************** CONSTRUCTOR *************************************/

    /**
     * Class constructor: creates a new empty type designation.
     *
     * @see	#TypeDesignationBase(Reference, String, String, Boolean)
     */
    protected TypeDesignationBase(){
        super();
    }

    /**
     * Class constructor: creates a new type designation
     * (including its {@link Reference reference source} and eventually
     * the taxon name string originally used by this reference when establishing
     * the former designation).
     *
     * @param citation				the reference source for the new designation
     * @param citationMicroReference	the string with the details describing the exact localisation within the reference
     * @param originalNameString	the taxon name string used originally in the reference source for the new designation
     * @see							#TypeDesignationBase()
     * @see							#isNotDesignated()
     * @see							TaxonNameBase#getTypeDesignations()
     */
    protected TypeDesignationBase(Reference citation, String citationMicroReference, String originalNameString) {
        this(citation, citationMicroReference, originalNameString, false);
    }

    /**
     * Class constructor: creates a new type designation
     * (including its {@link Reference reference source} and eventually
     * the taxon name string originally used by this reference when establishing
     * the former designation).
     *
     * @param citation				the reference source for the new designation
     * @param citationMicroReference	the string with the details describing the exact localisation within the reference
     * @param originalNameString	the taxon name string used originally in the reference source for the new designation
     * @param isNotDesignated		the boolean flag indicating whether there is no type at all for
     * 								<i>this</i> type designation
     * @see							#TypeDesignationBase()
     * @see							#isNotDesignated()
     * @see							TaxonNameBase#getTypeDesignations()
     */
    protected TypeDesignationBase(Reference citation, String citationMicroReference, String originalNameString, boolean notDesignated){
        super(citation, citationMicroReference, originalNameString);
        this.notDesignated = notDesignated;
    }


// **************** METHODS *************************************/


    /**
     * Returns the {@link TypeDesignationStatusBase type designation status} for <i>this</i> specimen type
     * designation. This status describes which of the possible categories of
     * types like "holotype", "neotype", "syntype" or "isotype" applies to <i>this</i>
     * specimen type designation.
     */
    public T getTypeStatus(){
        return this.typeStatus;
    }
    /**
     * @see  #getTypeStatus()
     */
    public void setTypeStatus(T typeStatus){
        this.typeStatus = typeStatus;
    }

    /**
     * Returns the set of {@link TaxonNameBase taxon names} typified in <i>this</i>
     * type designation. This is a subset of the taxon names belonging to the
     * corresponding {@link #getHomotypicalGroup() homotypical group}.
     */
    @Override
    public Set<TaxonNameBase> getTypifiedNames() {
        return typifiedNames;
    }

    /**
     * Returns the boolean value "true" if it is known that a type does not
     * exist and therefore the {@link TaxonNameBase taxon name} to which <i>this</i>
     * type designation is assigned must still be typified. Two
     * cases must be differentiated: <BR><ul>
     * <li> a) it is unknown whether a type exists and
     * <li> b) it is known that no type exists
     *  </ul>
     * If a) is true there should be no TypeDesignation instance at all
     * assigned to the "typified" taxon name.<BR>
     * If b) is true there should be a TypeDesignation instance with the
     * flag isNotDesignated set. The typeName attribute, in case of a
     * {@link NameTypeDesignation name type designation}, or the typeSpecimen attribute,
     * in case of a {@link SpecimenTypeDesignation specimen type designation}, should then be "null".
     */
    public boolean isNotDesignated() {
        return notDesignated;
    }

    /**
     * @see   #isNotDesignated()
     */
    public void setNotDesignated(boolean notDesignated) {
        this.notDesignated = notDesignated;
    }

    /**
     * @deprecated for bidirectional use only
     */
    @Deprecated
    protected void addTypifiedName(TaxonNameBase taxonName){
        this.typifiedNames.add(taxonName);
    }

    /**
     * @deprecated for bidirectional use only
     */
    @Deprecated
    protected void removeTypifiedName(TaxonNameBase taxonName){
        this.typifiedNames.remove(taxonName);
        if (taxonName.getTypeDesignations().contains(this)){
            taxonName.removeTypeDesignation(this);
        }
    }

    public abstract void removeType();

//*********************** CLONE ********************************************************/

    /**
     * Clones <i>this</i> type designation. This is a shortcut that enables to create
     * a new instance that differs only slightly from <i>this</i> type designation by
     * modifying only some of the attributes.<BR>
     * CAUTION: the typifiedNames set is not cloned but empty after cloning as the typified
     * names is considered to be the not owning part of a bidirectional relationship.
     * This may be changed in future.
     *
     * @throws CloneNotSupportedException
     *
     * @see eu.etaxonomy.cdm.model.common.ReferencedEntityBase#clone()
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        TypeDesignationBase result = (TypeDesignationBase)super.clone();

        result.typifiedNames = new HashSet<TaxonNameBase>();
//		for (TaxonNameBase taxonNameBase : getTypifiedNames()){
//			result.typifiedNames.add(taxonNameBase);
//		}


        //no changes to: notDesignated, typeStatus, homotypicalGroup
        return result;
    }
}
