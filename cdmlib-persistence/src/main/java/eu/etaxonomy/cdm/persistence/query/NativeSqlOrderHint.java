package eu.etaxonomy.cdm.persistence.query;

import org.hibernate.envers.query.AuditQuery;

import eu.etaxonomy.cdm.persistence.common.OperationNotSupportedInPriorViewException;

public class NativeSqlOrderHint extends OrderHint {

    private static final long serialVersionUID = 5209803557532593413L;
    private final static String PROPERTY_NAME = "uselessAnyways";

    private String nativeSQL;

	public NativeSqlOrderHint(String nativeSQL, SortOrder sortOrder) {
		super(NativeSqlOrderHint.PROPERTY_NAME, sortOrder);
		this.nativeSQL = nativeSQL;
    }

	public String getNativeSQL() {
		return nativeSQL;
	}

	@Override
	public void add(AuditQuery query) {
			throw new OperationNotSupportedInPriorViewException("You cannot sort using native SQL in  history view");
	}
}
