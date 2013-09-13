package eu.etaxonomy.cdm.persistence.query;

import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Order;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.internal.util.StringHelper;

import eu.etaxonomy.cdm.persistence.dao.common.OperationNotSupportedInPriorViewException;


public class NativeSqlOrderHint extends OrderHint {
	private final static String PROPERTY_NAME = "uselessAnyways";
	private String nativeSQL;

	public NativeSqlOrderHint(String nativeSQL, SortOrder sortOrder) {
		super(NativeSqlOrderHint.PROPERTY_NAME,sortOrder);
		this.nativeSQL = nativeSQL;
    }
	
	@Override
	public void add(Criteria criteria, Map<String, Criteria> criteriaMap) {
		criteria.addOrder(new NativeSqlHibernateOrder(this.getSortOrder().equals(SortOrder.ASCENDING) ? true : false,nativeSQL));
	}
    
	public class NativeSqlHibernateOrder extends Order {
		private static final long serialVersionUID = 6439553377404790090L;
		
		private String nativeSQL;
		private Boolean ascending;

		protected NativeSqlHibernateOrder(Boolean ascending, String nativeSQL) {
			super(NativeSqlOrderHint.PROPERTY_NAME, ascending);
			this.nativeSQL = nativeSQL; 
			this.ascending = ascending;
		}
		

		@Override
		public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
		    StringBuilder fragment = new StringBuilder();
//		    fragment.append("(");
		    fragment.append(this.nativeSQL);
//		    fragment.append(")");
		    fragment.append(ascending ? " asc" : " desc");
		    return StringHelper.replace(fragment.toString(), "{alias}", criteriaQuery.getSQLAlias(criteria));
		} 
	}
	@Override
	public void add(AuditQuery query) {
			throw new OperationNotSupportedInPriorViewException("You cannot sort using native SQL in  history view");
	}
}
