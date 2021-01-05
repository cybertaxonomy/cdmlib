/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.hibernate.search.UriBridge;
import eu.etaxonomy.cdm.jaxb.DateTimeAdapter;
import eu.etaxonomy.cdm.validation.annotation.NullOrNotEmpty;

/**
 * Embedabble class to embed attributes to use externally managed data
 * @author a.mueller
 * @since 12.08.2019
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExternallyManaged", propOrder = {
    "lastRetrieved",
    "externalId",
    "externalLink",
    "authorityType",
    "importMethod"
})
@XmlRootElement(name = "ExternallyManaged")
@Embeddable
public class ExternallyManaged implements Cloneable, Serializable, ICheckEmpty{

    private static final long serialVersionUID = -2254347420863435872L;


    //attributes for externally managed

//  @XmlElement (name = "LastRetrieved", type= String.class)
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    @Type(type="dateTimeUserType")
    //TODO needed??
    @Basic(fetch = FetchType.LAZY)
    @Column(name="lastRetrieved")
    private DateTime lastRetrieved;

    @XmlElement(name ="ExternalId" )
//  @Field
//  @Match(MatchMode.EQUAL)  //TODO check if this is correct
    @NullOrNotEmpty
    @Column(name="externalId", length=255)
    private String externalId;

    //Actionable link on e.g. on a webservice
    @XmlElement(name = "ExternalLink")
    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = UriBridge.class)
    @Type(type="uriUserType")
    @Column(name="externalLink")
    private URI externalLink;

    @XmlAttribute(name ="authority")
    @Column(name="authorityType", length=10)
    @Type(type = "eu.etaxonomy.cdm.hibernate.EnumUserType",
        parameters = {@org.hibernate.annotations.Parameter(name  = "enumClass", value = "eu.etaxonomy.cdm.model.common.AuthorityType")}
    )
    @NotNull
    private AuthorityType authorityType;

    @XmlAttribute(name ="importMethod")
    @Column(name="importMethod", length=30)
    @Type(type = "eu.etaxonomy.cdm.hibernate.EnumUserType",
        parameters = {@org.hibernate.annotations.Parameter(name  = "enumClass", value = "eu.etaxonomy.cdm.model.common.ExternallyManagedImport")}
    )
    @NotNull
    private ExternallyManagedImport importMethod;

//************************* **********************/

    @Override
    public boolean checkEmpty() {
        return authorityType == null && StringUtils.isBlank(externalId)
                && externalLink == null && importMethod == null && lastRetrieved == null;
    }

// ************************ CLONE ***********************/

    @Override
    protected Object clone() throws CloneNotSupportedException {
        ExternallyManaged result = (ExternallyManaged)super.clone();

        return result;
    }

}
