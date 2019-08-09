/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.permission;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * CDM authority class.<BR>
 *
 * @see https://dev.e-taxonomy.eu/redmine/issues/7099
 *
 * @author a.mueller
 * @since 09.08.2019
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CdmAuthority", propOrder = {
        "property",
        "targetUuid"}
)
@XmlRootElement(name = "CdmAuthority")
@Entity
public class CdmAuthority extends AuthorityBase {

    private static final long serialVersionUID = 3777547489226033333L;

    @XmlElement(name = "property")
    @Column(unique = true)
    @NotNull
    private String property;

    private UUID targetUuid;
}
