package eu.etaxonomy.cdm.model.view;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.Type;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

@Entity
@RevisionEntity
public class AuditEvent {
public static final AuditEvent CURRENT_VIEW;
	
	static {
		CURRENT_VIEW = new AuditEvent();
		CURRENT_VIEW.setUuid(UUID.fromString("966728f0-ae51-11dd-ad8b-0800200c9a66"));
	};
	
	@Type(type="uuidUserType")
	private UUID uuid;
	
	public UUID getUuid() {
		return uuid;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public AuditEvent() {
		this.uuid = UUID.randomUUID();
	}
	
	@Id
	@GeneratedValue
	@RevisionNumber
	private Integer revisionNumber;
	
	@RevisionTimestamp
	private Long timestamp;

	public Integer getRevisionNumber() {
		return revisionNumber;
	}
	
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
	
	public boolean equals(Object obj) {
		if(this == obj)
		    return true;
			
		if((obj == null) || (obj.getClass() != this.getClass()))
		    return false;
			
		AuditEvent auditEvent = (AuditEvent) obj;
		    return uuid == auditEvent.uuid || (uuid != null && uuid.equals(auditEvent.uuid));		
	}
		  	
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + (null == uuid ? 0 : uuid.hashCode());
		return hash;
	}

	public void setRevisionNumber(Integer revisionNumber) {
		this.revisionNumber = revisionNumber;
	}
}
