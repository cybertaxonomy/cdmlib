package eu.etaxonomy.cdm.persistence.query;

import org.hibernate.envers.query.AuditQuery;

import eu.etaxonomy.cdm.persistence.common.OperationNotSupportedInPriorViewException;

public class RandomOrder extends OrderHint {

    private static final long serialVersionUID = 9013839425292632385L;
    private final static String PROPERTY_NAME = "uuid"; //Note by AM: was "uselessAnyways" before switching to JPA, with JPA an incorrect property name is not allowed;

	public RandomOrder() {
		super(RandomOrder.PROPERTY_NAME,SortOrder.ASCENDING);
    }

	@Override
	public void add(AuditQuery query) {
			throw new OperationNotSupportedInPriorViewException("You cannot sort in a random order in the history view");
	}
}
