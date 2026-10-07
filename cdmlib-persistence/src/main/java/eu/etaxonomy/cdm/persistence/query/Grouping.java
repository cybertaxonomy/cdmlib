/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.query;

import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;

/**
 * Grouping definition for {@code groupBy} queries using the JPA Criteria API
 * (Hibernate 6 compatible; replaces the former Hibernate Criteria API).
 *
 * @author ben.clark
 */
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

	/**
	 * Adds this grouping to a JPA Criteria query.
	 *
	 * @param joins map of already created joins, keyed by association path
	 */
	public void addToCriteria(CriteriaBuilder cb, Root<?> root, Map<String, From<?, ?>> joins,
			List<Selection<?>> selections, List<Expression<?>> groupByExpressions,
			List<Order> orders) {

		Expression<?> expression = resolvePath(root, joins);
		selections.add(expression.alias(name));
		groupByExpressions.add(expression);
		addOrder(cb, expression, orders);
	}

	protected Expression<?> resolvePath(Root<?> root, Map<String, From<?, ?>> joins) {
		From<?, ?> from = root;
		if (associatedObject != null) {
			from = joins.computeIfAbsent(associatedObject,
					key -> root.join(key, JoinType.INNER));
		}
		if (propertyName == null || propertyName.isEmpty()) {
			return from;
		}
		if ("class".equals(propertyName)) {
			return from.type();
		}
		return from.get(propertyName);
	}

	protected void addOrder(CriteriaBuilder cb, Expression<?> expression, List<Order> orders) {
		if (order != null) {
			if (order.equals(SortOrder.ASCENDING)) {
				orders.add(cb.asc(expression));
			} else {
				orders.add(cb.desc(expression));
			}
		}
	}
}
