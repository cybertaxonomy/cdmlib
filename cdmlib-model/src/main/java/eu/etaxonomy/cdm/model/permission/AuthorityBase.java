/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.permission;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * Base class for persistable authorities.
 *
 * see https://dev.e-taxonomy.eu/redmine/issues/7099
 *
 * @author a.mueller
 * @since 09.08.2019
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AuthorityBase", propOrder = {
         }
)
@XmlRootElement(name = "Authority")
@Table(name="Authority"
   //for some reason this does not work, see comment #8464#note-6
 //  ,uniqueConstraints=@UniqueConstraint(columnNames={"property","permissionClass","targetUuid","operations"})
)
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class AuthorityBase extends CdmBase{

    private static final long serialVersionUID = -3786639494325014624L;

 // ************* CONSTRUCTOR ********************/

    //for hibernate use only, *packet* private required by bytebuddy
    @Deprecated
    AuthorityBase() {}
}
