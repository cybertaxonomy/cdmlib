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
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;

/**
 * @author ben.clark
 */
public class GroupByCount extends Grouping {

	public GroupByCount(String name, SortOrder order) {
		super("", name, null, order);
	}

	@Override
	public void addToCriteria(CriteriaBuilder cb, Root<?> root, Map<String, From<?, ?>> joins,
			List<Selection<?>> selections, List<Expression<?>> groupByExpressions,
			List<Order> orders) {

		Expression<Long> count = cb.count(root);
		selections.add(count.alias(getName()));
		addOrder(cb, count, orders);
	}

	@Override
	public String getAssociatedObj() {
		return null;
	}
}
