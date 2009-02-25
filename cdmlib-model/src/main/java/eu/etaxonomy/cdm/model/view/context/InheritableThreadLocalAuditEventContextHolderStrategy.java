package eu.etaxonomy.cdm.model.view.context;

import org.springframework.util.Assert;

/**
 * Class based heavily on InheritableThreadLocalSecurityContextHolderStrategy, part 
 * of spring-security, but instead binding a View object to the 
 * context.
 * 
 * @author ben
 * @author Ben Alex
 *
 */
public class InheritableThreadLocalAuditEventContextHolderStrategy implements
		AuditEventContextHolderStrategy {
	
	private static ThreadLocal contextHolder = new InheritableThreadLocal();

	public void clearContext() {
		contextHolder.set(null);
	}

	public AuditEventContext getContext() {
		if (contextHolder.get() == null) {
            contextHolder.set(new AuditEventContextImpl());
        }

        return (AuditEventContext) contextHolder.get();
	}

	public void setContext(AuditEventContext context) {
		Assert.notNull(context, "Only non-null AuditEventContext instances are permitted");
        contextHolder.set(context);
	}

}
