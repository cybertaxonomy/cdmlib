package eu.etaxonomy.cdm.model.view.context;

import java.io.Serializable;

import eu.etaxonomy.cdm.model.view.AuditEvent;

public interface AuditEventContext extends Serializable {
	public AuditEvent getAuditEvent();
	
	public void setAuditEvent(AuditEvent auditEvent);

}
