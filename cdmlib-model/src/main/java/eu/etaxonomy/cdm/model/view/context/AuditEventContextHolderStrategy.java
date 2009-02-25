package eu.etaxonomy.cdm.model.view.context;

/**
 * Class based heavily on SecurityContextHolderStrategy, part 
 * of spring-security, but instead binding a View object to the 
 * context.
 * 
 * @author ben
 * @author Ben Alex
 *
 */
public interface AuditEventContextHolderStrategy {
	void clearContext();
	AuditEventContext getContext();
	void setContext(AuditEventContext context);

}
