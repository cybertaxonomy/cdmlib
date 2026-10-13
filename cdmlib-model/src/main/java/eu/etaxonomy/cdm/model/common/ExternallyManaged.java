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
import java.time.ZonedDateTime;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Length;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.search.mapper.pojo.bridge.mapping.annotation.ValueBridgeRef;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;
import org.hibernate.type.SqlTypes;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.hibernate.search.UriBridge;
import eu.etaxonomy.cdm.jaxb.DateTimeAdapter;
import eu.etaxonomy.cdm.jpa.UriConverter;
import eu.etaxonomy.cdm.validation.annotation.NullOrNotEmpty;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Embedabble class to embed attributes to use externally managed data.
 *
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
    //TODO needed??
    @Basic(fetch = FetchType.LAZY)
    @Column(name="lastRetrieved")
    private ZonedDateTime lastRetrieved;

    @XmlElement(name ="ExternalId" )
//  @Field
//  @Match(MatchMode.EQUAL)  //TODO check if this is correct
    @NullOrNotEmpty
    @Column(name="externalId", length=255)
    private String externalId;

    //Actionable link e.g. to a webservice
    @XmlElement(name = "ExternalLink")
    @KeywordField(valueBridge = @ValueBridgeRef(type = UriBridge.class))
    @Convert(converter = UriConverter.class)
    // Workaround for Hibernate 6.1 / Envers (HHH-16253): Envers keeps length but drops LOB on _AUD
    // (falls back to varchar(255)). LONG32 exceeds dialect max VARCHAR → expected type upgrades to CLOB.
    // Remove @JdbcTypeCode / reconsider length when on a Hibernate version that preserves CLOB on audit tables.
    @Column(name="externalLink", length = Length.LONG32)
    @Lob
    @JdbcTypeCode(SqlTypes.CLOB)
    private URI externalLink;

    @XmlAttribute(name ="authority")
    @Column(name="authorityType", length=10)
    @NotNull
    private AuthorityType authorityType;

    @XmlAttribute(name ="importMethod")
    @Column(name="importMethod", length=30)
    @NotNull
    private ExternallyManagedImport importMethod;

//************************* **********************/

    @Override
    public boolean checkEmpty() {
        return authorityType == null && StringUtils.isBlank(externalId)
                && externalLink == null && importMethod == null && lastRetrieved == null;
    }

// ************************ GETTER /SETTER ***********************/

    public ZonedDateTime getLastRetrieved() {
        return lastRetrieved;
    }
    public void setLastRetrieved(ZonedDateTime lastRetrieved) {
        this.lastRetrieved = lastRetrieved;
    }

    public String getExternalId() {
        return externalId;
    }
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public URI getExternalLink() {
        return externalLink;
    }
    public void setExternalLink(URI externalLink) {
        this.externalLink = externalLink;
    }

    public AuthorityType getAuthorityType() {
        return authorityType;
    }
    public void setAuthorityType(AuthorityType authorityType) {
        this.authorityType = authorityType;
    }

    public ExternallyManagedImport getImportMethod() {
        return importMethod;
    }
    public void setImportMethod(ExternallyManagedImport importMethod) {
        this.importMethod = importMethod;
    }

// ********************** CLONE ***********************************/

    @Override
    protected Object clone() throws CloneNotSupportedException {
        ExternallyManaged result = (ExternallyManaged)super.clone();

        return result;
    }

}
