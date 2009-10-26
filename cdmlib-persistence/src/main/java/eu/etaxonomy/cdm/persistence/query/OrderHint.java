/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.persistence.query;

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

}
