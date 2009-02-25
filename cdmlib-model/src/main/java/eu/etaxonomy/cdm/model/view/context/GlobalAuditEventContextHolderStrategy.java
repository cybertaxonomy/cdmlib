package eu.etaxonomy.cdm.model.view.context;

import org.springframework.util.Assert;
/**
 * Class based heavily on GlobalSecurityContextHolderStrategy, part 
 * of spring-security, but instead binding a View object to the 
 * context. 
 * 
 * @author ben
 *
 */
public class GlobalAuditEventContextHolderStrategy implements
		AuditEventContextHolderStrategy {
	
	private static AuditEventContext contextHolder;

	public void clearContext() {
		contextHolder = null;
	}

	public AuditEventContext getContext() {
		if (contextHolder == null) {
            contextHolder = new AuditEventContextImpl();
        }

        return contextHolder;
	}

	public void setContext(AuditEventContext context) {
		Assert.notNull(context, "Only non-null AuditEventContext instances are permitted");
        contextHolder = context;
	}

}
