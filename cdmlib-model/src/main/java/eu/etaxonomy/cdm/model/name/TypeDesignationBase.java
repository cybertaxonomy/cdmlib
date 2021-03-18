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
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.SourcedEntityBase;
import eu.etaxonomy.cdm.model.reference.ICdmTarget;
import eu.etaxonomy.cdm.model.reference.NamedSource;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.validation.Level2;
import eu.etaxonomy.cdm.validation.annotation.ValidLectotypeSource;
import eu.etaxonomy.cdm.validation.annotation.ValidTypeDesignation;

/**
 * The (abstract) class representing a typification of one or several {@link TaxonName taxon names}.<BR>
 * All taxon names which have a {@link Rank rank} "species aggregate" or lower
 * can only be typified by specimens (a {@link SpecimenTypeDesignation specimen type designation}), but taxon
 * names with a higher rank might be typified by an other taxon name with
 * rank "species" or "genus" (a {@link NameTypeDesignation name type designation}).
 *
 * @see		TaxonName
 * @see		NameTypeDesignation
 * @see		SpecimenTypeDesignation
 * @author  a.mueller
 * @since 07.08.2008
 */
@XmlRootElement(name = "TypeDesignationBase")
@XmlType(name = "TypeDesignationBase", propOrder = {
    "typeStatus",
    "notDesignated",
    "typifiedNames",
    "designationSource",
    "registrations",
})
@XmlSeeAlso({
    NameTypeDesignation.class,
    SpecimenTypeDesignation.class,
    TextualTypeDesignation.class
})
@Entity
@Audited
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@ValidTypeDesignation(groups=Level2.class)
@ValidLectotypeSource(groups=Level2.class)
public abstract class TypeDesignationBase<T extends TypeDesignationStatusBase<T>>
        extends SourcedEntityBase<IdentifiableSource>
        implements ITypeDesignation {

    private static final long serialVersionUID = 4838214337140859787L;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TypeDesignationBase.class);

    @XmlElement(name = "IsNotDesignated")
    private boolean notDesignated;

    @XmlElement(name = "TypeStatus")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = TypeDesignationStatusBase.class)
    private T typeStatus;

    //the source for the lectotypification (or similar)
    @XmlElement(name = "source")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToOne(fetch = FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE, CascadeType.DELETE})
    private NamedSource designationSource;

    @XmlElementWrapper(name = "TypifiedNames")
    @XmlElement(name = "TypifiedName")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY , mappedBy="typeDesignations")
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    private Set<TaxonName> typifiedNames = new HashSet<>();

    //******* REGISTRATION *****************/

    @XmlElementWrapper(name = "Registrations")
    @XmlElement(name = "Registration")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(mappedBy="typeDesignations", fetch= FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    @NotNull
    @IndexedEmbedded(depth=1)
    private Set<Registration> registrations = new HashSet<>();


// **************** CONSTRUCTOR *************************************/

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
     * @see							TaxonName#getTypeDesignations()
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
     * @see							TaxonName#getTypeDesignations()
     */
    protected TypeDesignationBase(Reference citation, String citationMicroReference, String originalNameString, boolean notDesignated){
        this(NamedSource.NewPrimarySourceInstance(citation, citationMicroReference), originalNameString, notDesignated);
    }

    /**
     * Class constructor: creates a new type designation
     * (including its {@link Reference reference source} and eventually
     * the taxon name string originally used by this reference when establishing
     * the former designation).
     *
     * @param source                the reference source for the new designation
     * @param originalNameString    the taxon name string used originally in the reference source for the new designation
     * @param isNotDesignated       the boolean flag indicating whether there is no type at all for
     *                              <i>this</i> type designation
     * @see                         #TypeDesignationBase()
     * @see                         #isNotDesignated()
     * @see                         TaxonName#getTypeDesignations()
     */
    protected TypeDesignationBase(NamedSource designationSource, String originalNameString, boolean notDesignated){
        super();
        this.notDesignated = notDesignated;
        this.designationSource = designationSource;
    }

// **************** METHODS *************************************/

    /**
     * Returns the {@link TypeDesignationStatusBase type designation status} for <i>this</i> specimen type
     * designation. This status describes which of the possible categories of
     * types like "holotype", "neotype", "syntype" or "isotype" applies to <i>this</i>
     * specimen type designation.
     */
    public T getTypeStatus(){
        return (CdmBase.deproxy(this.typeStatus));  //otherwise for some error we get an error in TypeDesignationDaoHibernateImplTest
    }
    /**
     * @see  #getTypeStatus()
     */
    public void setTypeStatus(T typeStatus){
        this.typeStatus = typeStatus;
    }

    /**
     * Returns the boolean value "true" if it is known that a type does not
     * exist and therefore the {@link TaxonName taxon name} to which <i>this</i>
     * type designation is assigned must still be typified. Two
     * cases must be differentiated: <BR>
     * <ul>
     * <li> a) it is unknown whether a type exists and
     * <li> b) it is known that no type exists
     * </ul>
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

    @Transient
    public String getCitationMicroReference() {
        return designationSource == null ? null : this.designationSource.getCitationMicroReference();
    }

    public void setCitationMicroReference(String microReference) {
        this.getDesignationSource(true).setCitationMicroReference(StringUtils.isBlank(microReference)? null : microReference);
        checkNullSource();
    }
    /**
     * Convenience method to retrieve the reference of the type designations
     * designation/lectotype source.
     *
     * @see #getDesignationSource()
     */
    @Transient
    public Reference getCitation(){
        return designationSource == null ? null : this.designationSource.getCitation();
    }
    /**
     * Convenience method to set reference for the designation's
     * {@link #getDesignationSource() designation/lectotype source}.
     * The source is created if reference is not <code>null</code>
     * and the source does not yet exist.
     * <BR>
     * This field should only be used if
     * the {@link #getTypeStatus() status} is lectotype (or similar) which can be
     * retrieved by using method {@link TypeDesignationStatusBase#hasDesignationSource()}
     *
     * @see #getDesignationSource()
     */
    public void setCitation(Reference citation) {
        this.getDesignationSource(true).setCitation(citation);
        checkNullSource();
    }

    /**
     * Returns the {@link #getDesignationSource() designation source}. If a source does not exist
     * yet a new and empty one is created. A source should only be created for
     * lectotype like type designations (see {@link #getDesignationSource()}.
     *
     * @param createIfNotExist
     * @see #getDesignationSource()
     */
    public NamedSource getDesignationSource(boolean createIfNotExist) {
        if (this.designationSource == null && createIfNotExist){
            this.designationSource = NamedSource.NewInstance(OriginalSourceType.PrimaryTaxonomicSource);
        }
        return designationSource;
    }

    /**
     * The primary source of typification as used for lecto-, epi- and neotypes.
     * This field should only be used if
     * the {@link #getTypeStatus() status} is lectotype (or similar) which can be
     * retrieved by using method {@link TypeDesignationStatusBase#hasDesignationSource()}.
     */
    public NamedSource getDesignationSource(){
        return designationSource;
    }
    /**
     * @see #getDesignationSource()
     */
    public void setDesignationSource(NamedSource designationSource) {
        this.designationSource = designationSource;
    }

    private void checkNullSource() {
        if (this.designationSource != null && this.designationSource.checkEmpty(true)){
            this.designationSource = null;
        }
    }

    /**
     * Returns the {@link Registration registrations} available for this
     * type designation.
     */
    public Set<Registration> getRegistrations() {
        return this.registrations;
    }

    /**
     * Remove the type (specimen or name) from this type designation
     */
    public abstract void removeType();

    /**
     * Returns the set of {@link TaxonName taxon names} typified in <i>this</i>
     * type designation. This is a subset of the taxon names belonging to the
     * corresponding {@link #getHomotypicalGroup() homotypical group}.
     */
    @Override
    public Set<TaxonName> getTypifiedNames() {
        return typifiedNames;
    }

    /**
     * @deprecated for bidirectional use only
     */
    @Deprecated
    protected void addTypifiedName(TaxonName taxonName){
        this.typifiedNames.add(taxonName);
    }

    /**
     * @deprecated for bidirectional use only
     */
    @Deprecated
    protected void removeTypifiedName(TaxonName taxonName){
        this.typifiedNames.remove(taxonName);
        if (taxonName.getTypeDesignations().contains(this)){
            taxonName.removeTypeDesignation(this);
        }
    }

    @Override
    protected IdentifiableSource createNewSource(OriginalSourceType type, String idInSource, String idNamespace,
            Reference reference, String microReference, String originalInfo, ICdmTarget target) {
        return IdentifiableSource.NewInstance(type, idInSource, idNamespace, reference, microReference, originalInfo, target);
    }


    @Override
    @Transient
    public boolean hasDesignationSource() {
        if (getTypeStatus() == null) {
            return false;
        }
        return getTypeStatus().hasDesignationSource();
    }

//*********************** CLONE ********************************************************/


    /**
     * Clones <i>this</i> type designation. This is a shortcut that enables to create
     * a new instance that differs only slightly from <i>this</i> type designation by
     * modifying only some of the attributes.<BR>
     * CAUTION: the typifiedNames set is also cloned by adding the new type designation
     * to the typifiedNames of the original type designation. If this is unwanted
     * th
     *
     * not cloned but empty after cloning as the typified
     * names is considered to be the not owning part of a bidirectional relationship.
     * This may be changed in future.
     *
     * @throws CloneNotSupportedException
     *
     * @see eu.etaxonomy.cdm.model.common.SourcedEntityBase#clone()
     * @see java.lang.Object#clone()
     */
    @Override
    public TypeDesignationBase<T> clone() throws CloneNotSupportedException {

        @SuppressWarnings("unchecked")
        TypeDesignationBase<T> result = (TypeDesignationBase<T>)super.clone();

		//registrations
		result.registrations = new HashSet<>();
		for (Registration registration : registrations){
		    registration.addTypeDesignation(result);
		}

        //typified names
        result.typifiedNames = new HashSet<>();
        for (TaxonName taxonName : getTypifiedNames()){
            taxonName.addTypeDesignation(result, false);
        }

        //no changes to: notDesignated, typeStatus, homotypicalGroup
        return result;
    }
}
