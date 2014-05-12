/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.model.view.context;

/**
 * Class based heavily on SecurityContextHolderStrategy, part of
 * spring-security, but instead binding a View object to the context.
 *
 * @author ben
 * @author Ben Alex
 *
 */
public interface AuditEventContextHolderStrategy {

	/**
	 * Clears the context by setting the AuditEventContext in the current thread to null
	 */
	void clearContext();

	AuditEventContext getContext();

	void setContext(AuditEventContext context);

}
