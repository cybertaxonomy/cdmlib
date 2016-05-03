/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.view.context;

import eu.etaxonomy.cdm.model.view.AuditEvent;

/**
 * Class based heavily on SecurityContextImpl, part
 * of spring-security, but instead binding a View object to the
 * context.
 *
 * @author ben
 * @author Ben Alex
 *
 */
public class AuditEventContextImpl implements AuditEventContext {
    private static final long serialVersionUID = 4477662916416534368L;

    private AuditEvent auditEvent;

    @Override
	public AuditEvent getAuditEvent() {
		return auditEvent;
	}

    @Override
    public void setAuditEvent(AuditEvent auditEvent) {
		this.auditEvent = auditEvent;
	}

    @Override
    public boolean equals(Object obj) {
		if (obj instanceof AuditEventContextImpl) {
			AuditEventContextImpl test = (AuditEventContextImpl) obj;

			if ((this.getAuditEvent() == null) && (test.getAuditEvent() == null)) {
				return true;
			}

			if ((this.getAuditEvent() != null) && (test.getAuditEvent() != null)
					&& this.getAuditEvent().equals(test.getAuditEvent())) {
                return true;
			}
		}

		return false;
	}

    @Override
	public int hashCode() {
		if (this.auditEvent == null) {
			return -1;
		} else {
            return this.auditEvent.hashCode();
		}
	}

    @Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(super.toString());

		if (this.auditEvent == null) {
			sb.append(": Null auditEvent");
		} else {
			sb.append(": AuditEvent: ").append(this.auditEvent);
		}

		return sb.toString();
	}
}
