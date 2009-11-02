/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.persistence.query;

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
	
	public void add(Criteria criteria) {
		Order order;
		String assocObj = null, propname;
		int pos;
		if((pos = getPropertyName().indexOf('.', 0)) >= 0){
			assocObj = getPropertyName().substring(0, pos);
			propname = getPropertyName().substring(pos + 1);
		} else {
			propname = getPropertyName();
		}
		if(isAscending()){
			order = Order.asc(propname);					
		} else {
			order = Order.desc(propname);
		}
		if(assocObj != null){
			criteria.createCriteria(assocObj).addOrder(order);
		} else {
			criteria.addOrder(order);				
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


}
