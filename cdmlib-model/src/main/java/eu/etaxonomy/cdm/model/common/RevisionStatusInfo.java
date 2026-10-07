/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import java.time.LocalDate;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Revision status for a taxon (and other entities in future).
 *
 * See #11040
 *
 * @author muellera
 * @since 07.10.2026
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RevisionStatusInfo", propOrder = {
        "status",
        "changed"
})
@XmlRootElement(name = "RevisionStatusInfo")
@Embeddable
public class RevisionStatusInfo {

    @ManyToOne(fetch = FetchType.LAZY)
    private RevisionStatus status;

    private LocalDate changed;

    public static final RevisionStatusInfo NewInstance(RevisionStatus status, LocalDate changed) {
        return new RevisionStatusInfo(status, changed);
    }

    //for hibernate use only
    protected RevisionStatusInfo() {}

    private RevisionStatusInfo(RevisionStatus status, LocalDate changed) {
        this.status = status;
        this.changed = changed;
    }

//***************** GETTER/ SETTER *********************************/

    //status
    public RevisionStatus getStatus() {
        return status;
    }
    public void setStatus(RevisionStatus status) {
        this.status = status;
    }

    //changed
    public LocalDate getChanged() {
        return changed;
    }
    public void setChanged(LocalDate changed) {
        this.changed = changed;
    }

//***************** CLONE **************************************/

    @Override
    public RevisionStatusInfo clone() {
        return NewInstance(this.status, this.changed);
    }
}