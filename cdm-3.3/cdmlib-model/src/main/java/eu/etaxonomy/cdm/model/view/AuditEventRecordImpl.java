/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.model.view;

import org.hibernate.envers.RevisionType;

import eu.etaxonomy.cdm.model.common.CdmBase;

public class AuditEventRecordImpl<T extends CdmBase> implements AuditEventRecord<T> {
	
	private AuditEvent auditEvent;
	private T auditableObject;
	private RevisionType revisionType;

	public AuditEventRecordImpl(Object[] obj) {
		assert obj.length == 3 : "The array must have three elements";
		auditableObject = (T)obj[0];
		auditEvent = (AuditEvent)obj[1];
		revisionType = (RevisionType)obj[2];
	}
	
	public AuditEvent getAuditEvent() {
		return auditEvent;
	}

	public T getAuditableObject() {
		return auditableObject;
	}

	public RevisionType getRevisionType() {
		return revisionType;
	}

}
