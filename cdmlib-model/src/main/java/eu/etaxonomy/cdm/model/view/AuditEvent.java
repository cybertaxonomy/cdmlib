/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.view;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.Type;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;
import org.joda.time.DateTime;

@Entity
@RevisionEntity
public class AuditEvent implements Serializable {

	private static final long serialVersionUID = 6584537382484488953L;

	public static final AuditEvent CURRENT_VIEW;

	static {
		CURRENT_VIEW = new AuditEvent();
		CURRENT_VIEW.setUuid(UUID.fromString("966728f0-ae51-11dd-ad8b-0800200c9a66"));
	}

	@Type(type="uuidUserType")
	private UUID uuid;

    @Id
    @GeneratedValue(generator = "custom-enhanced-table")  //see also CdmBase.id
    @RevisionNumber
    private Integer revisionNumber;

    @Type(type="dateTimeUserType")
    @Basic(fetch = FetchType.LAZY)
    private DateTime date;

    @RevisionTimestamp
    private Long timestamp;

//**************** CONSTRUCTOR ********************************/

    public AuditEvent() {
        this.uuid = UUID.randomUUID();
        this.date = new DateTime();
    }

//***************** GETTER/ SETTER *************************/

	public UUID getUuid() {
		return uuid;
	}
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

	public Long getTimestamp() {
		return timestamp;
	}


	public DateTime getDate() {
		return date;
	}
	public void setDate(DateTime date) {
		this.date = date;
	}

	public Integer getRevisionNumber() {
		return revisionNumber;
	}
    public void setRevisionNumber(Integer revisionNumber) {
        this.revisionNumber = revisionNumber;
    }

// ****************** Overrides **************************/

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
            return true;
        }

		if((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }

		AuditEvent auditEvent = (AuditEvent) obj;
		    return uuid == auditEvent.uuid || (uuid != null && uuid.equals(auditEvent.uuid));
	}

	@Override
    public int hashCode() {
		int hash = 7;
		hash = 31 * hash + (null == uuid ? 0 : uuid.hashCode());
		return hash;
	}

}
