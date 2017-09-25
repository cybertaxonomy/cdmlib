/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.jaxb.DateTimeAdapter;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.validation.annotation.NullOrNotEmpty;

/**
 * A registration represents a nomenclatural act, either a {@link TaxonName taxon name}
 * registration or a {@link TypeDesignationBase type} registration.
 * <p>
 * The name and all type designations associated with the Registration must share the same citation and citation detail.
 *
 *
 * @author a.mueller
 * @date 13.03.2017
 *
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Registration", propOrder = {
    "identifier",
    "specificIdentifier",
    "registrationDate",
    "status",
    "institution",
    "name",
    "typeDesignations",
    "blockedBy",
    "submitter"
})
@Entity
@Audited
public class Registration extends AnnotatableEntity {

    private static final long serialVersionUID = -5633923579539766801L;

    @XmlElement(name = "Identifier")
    @NullOrNotEmpty
    @Column(length=255)
    private String identifier;

    //id without http-domain
    @XmlElement(name = "SpecificIdentifier")
    @NullOrNotEmpty
    @Column(length=255)
    private String specificIdentifier;

    @XmlElement (name = "RegistrationDate", type= String.class)
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    @Type(type="dateTimeUserType")
    //TODO ??
    @Basic(fetch = FetchType.LAZY)
//    @Field(analyze = Analyze.NO)
//    @FieldBridge(impl = DateTimeBridge.class)
    private DateTime registrationDate;

    @XmlAttribute(name ="Status")
    @Column(name="status", length=10)
    @Type(type = "eu.etaxonomy.cdm.hibernate.EnumUserType",
        parameters = {@org.hibernate.annotations.Parameter(name  = "enumClass", value = "eu.etaxonomy.cdm.model.name.RegistrationStatus")}
    )
    @NotNull
    private RegistrationStatus status = RegistrationStatus.PREPARATION;

    @XmlElement(name = "Institution")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    @IndexedEmbedded
    private Institution institution;

    @XmlElement(name = "Name")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
//    @IndexedEmbedded(includeEmbeddedObjectId=true)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    private TaxonName name;

    @XmlElementWrapper(name = "TypeDesignations")
    @XmlElement(name = "TypeDesignation")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    @NotNull
    private Set<TypeDesignationBase> typeDesignations = new HashSet<>();

    @XmlElementWrapper(name = "BlockingRegistrations")
    @XmlElement(name = "BlockedBy")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    @NotNull
    private Set<Registration> blockedBy = new HashSet<>();

    @XmlElement (name = "Submitter")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
    private User submitter;

// ****************** Factory ******************/

    public static Registration NewInstance(){
        return new Registration();
    }


    /**
     * @param identifier
     * @param specificIdentifier
     * @param name can be <code>null</code>
     * @param typeDesignations can be <code>null</code>
     * @return
     */
    public static Registration NewInstance(String identifier, String specificIdentifier,
            TaxonName name, Set<TypeDesignationBase> typeDesignations){
        Registration result = new Registration();
        result.setIdentifier(identifier);
        result.setSpecificIdentifier(specificIdentifier);
        result.setName(name);
        if (typeDesignations != null){
            result.setTypeDesignations(typeDesignations);
        }
        return result;
    }

// **************** CONSTRUCTOR ****************/

    private Registration(){}



// *************** GETTER / SETTER  *************/

    public String getIdentifier() {return identifier;}
    public void setIdentifier(String identifier) {this.identifier = identifier;}

    public String getSpecificIdentifier() {return specificIdentifier;}
    public void setSpecificIdentifier(String specificIdentifier) {this.specificIdentifier = specificIdentifier;}

    public RegistrationStatus getStatus() {return status;}
    public void setStatus(RegistrationStatus status) {this.status = status;}

    public DateTime getRegistrationDate() {return registrationDate;}
    public void setRegistrationDate(DateTime registrationDate) {this.registrationDate = registrationDate;}

    public Institution getInstitution() {return institution;}
    public void setInstitution(Institution institution) {this.institution = institution;}

    public TaxonName getName() {return name;}
    public void setName(TaxonName name) {
        if (this.name != null && !this.name.equals(name)){
            this.name.getRegistrations().remove(this);
        }
        if (name != null && !name.equals(this.name)){
            name.getRegistrations().add(this);
        }
        this.name = name;
    }

    public User getSubmitter() {return submitter;}
    public void setSubmitter(User submitter) {this.submitter = submitter;}

    public Set<Registration> getBlockedBy() {return blockedBy;}
    @SuppressWarnings("unused")
    private void setBlockedBy(Set<Registration> blockedBy) {this.blockedBy = blockedBy;}

    public Set<TypeDesignationBase> getTypeDesignations() {return typeDesignations;}
    public void setTypeDesignations(Set<TypeDesignationBase> typeDesignations) {
        this.typeDesignations = typeDesignations;
    }

    public void addTypeDesignation(TypeDesignationBase designation) {
        this.typeDesignations.add(designation);
        if (!designation.getRegistrations().contains(this)){
            designation.getRegistrations().add(this);
        }
    }
    public void removeTypeDesignation(TypeDesignationBase designation) {
        this.typeDesignations.remove(designation);
        if (designation.getRegistrations().contains(this)){
            designation.getRegistrations().remove(this);
        }
    }


}
