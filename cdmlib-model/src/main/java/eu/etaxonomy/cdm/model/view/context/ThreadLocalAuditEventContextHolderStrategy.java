/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.view.context;

import org.springframework.util.Assert;

/**
 * Class based heavily on ThreadLocalSecurityContextHolderStrategy, part
 * of spring-security, but instead binding a View object to the
 * context.
 *
 * @author ben
 * @author Ben Alex
 *
 */
public class ThreadLocalAuditEventContextHolderStrategy implements
		AuditEventContextHolderStrategy {

	private static ThreadLocal contextHolder = new ThreadLocal();

	@Override
    public void clearContext() {
		contextHolder.set(null);
	}

	@Override
    public AuditEventContext getContext() {
		if (contextHolder.get() == null) {
            contextHolder.set(new AuditEventContextImpl());
        }

		return (AuditEventContext) contextHolder.get();
	}

	@Override
    public void setContext(AuditEventContext context) {
		Assert.notNull(context, "Only non-null AuditEventContext instances are permitted");
        contextHolder.set(context);
	}

}
