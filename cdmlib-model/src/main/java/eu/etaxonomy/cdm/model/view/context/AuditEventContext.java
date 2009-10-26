/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.model.view.context;

import java.io.Serializable;

import eu.etaxonomy.cdm.model.view.AuditEvent;

public interface AuditEventContext extends Serializable {
	public AuditEvent getAuditEvent();
	
	public void setAuditEvent(AuditEvent auditEvent);

}
