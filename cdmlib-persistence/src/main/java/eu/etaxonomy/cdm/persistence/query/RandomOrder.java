package eu.etaxonomy.cdm.persistence.query;

import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Order;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.util.StringHelper;

import eu.etaxonomy.cdm.persistence.dao.common.OperationNotSupportedInPriorViewException;


public class RandomOrder extends OrderHint {
	private final static String PROPERTY_NAME = "uselessAnyways"; 

	public RandomOrder() {
		super(RandomOrder.PROPERTY_NAME,SortOrder.ASCENDING);
    }
	
	@Override
	public void add(Criteria criteria, Map<String, Criteria> criteriaMap) {
		criteria.addOrder(new RandomHibernateOrder());
	}
    
	public class RandomHibernateOrder extends Order {

		protected RandomHibernateOrder() {
			super(RandomOrder.PROPERTY_NAME, true);
		}
		

		@Override
		public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery)
		throws HibernateException {
		StringBuilder fragment = new StringBuilder();
		fragment.append(" rand()");
		return StringHelper.replace(fragment.toString(), "{alias}",
		criteriaQuery.getSQLAlias(criteria));
		} 
	}
	@Override
	public void add(AuditQuery query) {
			throw new OperationNotSupportedInPriorViewException("You cannot sort in a random order in the history view");
	}
}
