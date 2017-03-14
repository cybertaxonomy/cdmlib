/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

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
import org.hibernate.search.annotations.IndexedEmbedded;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.jaxb.DateTimeAdapter;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.validation.annotation.NullOrNotEmpty;

/**
 * A registration represents a nomenclatural act, either a {@link TaxonNameBase taxon name}
 * registration or a {@link TypeDesignationBase type} registration.
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
public class Registration {

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
    @Column(name="status")
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
    private TaxonNameBase name;

    @XmlElementWrapper(name = "TypeDesignations")
    @XmlElement(name = "TypeDesignation")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    @NotNull
    private Set<TypeDesignationBase> typeDesignations;

    private Set<Registration> blockedBy;

    @XmlElement (name = "Submitter")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
    private User submitter;

// ****************** Factory ******************/

    public static Registration NewInstance(){
        return new Registration();
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

    public TaxonNameBase getName() {return name;}
    public void setName(TaxonNameBase name) {this.name = name;}

    public User getSubmitter() {return submitter;}
    public void setSubmitter(User submitter) {this.submitter = submitter;}

    public Set<Registration> getBlockedBy() {return blockedBy;}
    private void setBlockedBy(Set<Registration> blockedBy) {this.blockedBy = blockedBy;}

    public Set<TypeDesignationBase> getTypeDesignations() {return typeDesignations;}
    public void setTypeDesignations(Set<TypeDesignationBase> typeDesignations) {this.typeDesignations = typeDesignations;}


}
