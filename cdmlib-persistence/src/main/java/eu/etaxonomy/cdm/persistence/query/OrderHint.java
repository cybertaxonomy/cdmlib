/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.persistence.query;

import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;

import eu.etaxonomy.cdm.persistence.dao.common.OperationNotSupportedInPriorViewException;

public class OrderHint {

	public enum SortOrder {

		/**
		 * items are sorted in increasing
		 * order.
		 */
		ASCENDING, 
		/**
		 * items are sorted in decreasing
		 * order.
		 */
		DESCENDING
	}
	
	private String propertyName;
	
	private SortOrder sortOrder;

	public OrderHint(String fieldName, SortOrder sortOrder) {
		super();
		this.propertyName = fieldName;
		this.sortOrder = sortOrder;
	}

	/**
	 * The property of a bean
	 * @return
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * possible sort orders are {@link SortOrder.ASCENDING} or {@link SortOrder.DESCENDING} 
	 * @return
	 */
	public SortOrder getSortOrder() {
		return sortOrder;
	}
	
	public boolean isAscending(){
		return sortOrder.equals(SortOrder.ASCENDING);
	}
	
	public void add(Criteria criteria, Map<String, Criteria> criteriaMap) {
		if(getPropertyName().indexOf(".") != -1) {
			/**
			 * Here we have to work a bit of magic as currently hibernate will
			 * throw an error if we attempt to join the same association twice.
			 * 
			 * http://opensource.atlassian.com/projects/hibernate/browse/HHH-879
			 */
			Order order;
			
			String[] assocObjs = getPropertyName().split("\\.");
			String path = "";
			Criteria c = criteria;
			for(int i = 0; i < assocObjs.length - 1; i++) {
				path = path + assocObjs[i];
				if(criteriaMap.get(path) == null) {
					c = c.createCriteria(assocObjs[i]);
					criteriaMap.put(path, c);
				} else {
					c = criteriaMap.get(path);
				}		               
                path = path + '.';
            }
			String propname = assocObjs[assocObjs.length - 1];
			if(isAscending()){
				c.addOrder(Order.asc(propname));					
			} else {
				c.addOrder(Order.desc(propname));
			}
		} else {
			if(isAscending()){
				criteria.addOrder(Order.asc(getPropertyName()));					
			} else {
				criteria.addOrder(Order.desc(getPropertyName()));
			}
		}
	}
	
	public void add(AuditQuery query) {
	
		if(getPropertyName().indexOf('.', 0) >= 0){
			throw new OperationNotSupportedInPriorViewException("Sorting by related properties is not supported in the history view");
		} else {
			if(isAscending()){
				query.addOrder(AuditEntity.property(getPropertyName()).asc());					
			} else {
				query.addOrder(AuditEntity.property(getPropertyName()).desc());
			}
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this){
			return true;
		}
		if (obj == null){
			return false;
		}
		if (!OrderHint.class.isAssignableFrom(obj.getClass())){
			return false;
		}
		OrderHint orderHint= (OrderHint)obj;
		boolean propertyNameEqual = orderHint.getPropertyName().equals(this.getPropertyName());
		boolean sortOrderEqual = orderHint.getSortOrder().equals(this.getSortOrder());
		if (! propertyNameEqual || !sortOrderEqual){
				return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		   int hashCode = 7;
		   hashCode = 29 * hashCode + this.getPropertyName().hashCode() * this.getSortOrder().hashCode();
		   return hashCode;
	}
}
