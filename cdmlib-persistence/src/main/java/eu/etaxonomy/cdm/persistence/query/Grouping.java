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
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Property;

import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;

public class Grouping {
	private String associatedObject;
	private String associatedObjectAlias;
	private String propertyName;
	protected String name;
	private SortOrder order;
	
	public Grouping(String propertyPath, String name,  String associatedObjectAlias, SortOrder order) {
        int pos;
        if((pos = propertyPath.indexOf('.', 0)) >= 0){
    	    this.associatedObject = propertyPath.substring(0, pos);
            this.propertyName = propertyPath.substring(pos + 1);
        } else {
            this.propertyName = propertyPath;
        }
        this.name = name;
        this.order = order;
        this.associatedObjectAlias = associatedObjectAlias;
	}
	
	protected void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public String getAssociatedObj() {
		return associatedObject;
	}
	
	public String getAssociatedObjectAlias() {
		return associatedObjectAlias;
	}

	public String getName() {
		return name;
	}
	
	protected SortOrder getOrder() {
		return order;
	}

	public void addOrder(Criteria criteria) {
		if(order != null) {
			if(order.equals(SortOrder.ASCENDING)) {
				criteria.addOrder(Order.asc(this.name));
			} else {
				criteria.addOrder(Order.desc(this.name));
			}
		}
	}

	public void addProjection(ProjectionList projectionList) {
		if(associatedObjectAlias != null) {
		    projectionList.add(Property.forName(associatedObjectAlias + "." + propertyName).group(),name);
		} else {
			projectionList.add(Property.forName(propertyName).group(),name);
		}
	}

}
