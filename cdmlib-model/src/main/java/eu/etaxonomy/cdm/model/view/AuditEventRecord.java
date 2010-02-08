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

public interface AuditEventRecord<T extends CdmBase> {
	public T getAuditableObject();
    public AuditEvent getAuditEvent();
	public RevisionType getRevisionType();
}
