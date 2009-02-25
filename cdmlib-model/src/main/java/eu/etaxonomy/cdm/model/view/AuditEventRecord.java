package eu.etaxonomy.cdm.model.view;

import org.hibernate.envers.RevisionType;

import eu.etaxonomy.cdm.model.common.CdmBase;

public interface AuditEventRecord<T extends CdmBase> {
	public T getAuditableObject();
    public AuditEvent getAuditEvent();
	public RevisionType getRevisionType();
}
