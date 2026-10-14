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
 * Groups by year / month / day of a date property using JPA Criteria functions
 * (Hibernate 6 compatible).
 *
 * @author ben.clark
 */
public class GroupByDate extends Grouping {

	private Resolution resolution;

	public GroupByDate(String propertyPath, String name, SortOrder order, Resolution resolution) {
		super(propertyPath, name, null, order);
		this.resolution = resolution;
	}

	@Override
	public void addToCriteria(CriteriaBuilder cb, Root<?> root, Map<String, From<?, ?>> joins,
			List<Selection<?>> selections, List<Expression<?>> groupByExpressions,
			List<Order> orders) {

		Expression<?> datePath = resolvePath(root, joins);
		Expression<Integer> year = cb.function("year", Integer.class, datePath);
		selections.add(year.alias("year"));
		groupByExpressions.add(year);
		addOrder(cb, year, orders);

		if (resolution == Resolution.MONTH || resolution == Resolution.DAY) {
			Expression<Integer> month = cb.function("month", Integer.class, datePath);
			selections.add(month.alias("month"));
			groupByExpressions.add(month);
			addOrder(cb, month, orders);
		}
		if (resolution == Resolution.DAY) {
			Expression<Integer> day = cb.function("day", Integer.class, datePath);
			selections.add(day.alias("day"));
			groupByExpressions.add(day);
			addOrder(cb, day, orders);
		}
	}

	public enum Resolution {
		DAY,
		MONTH,
		YEAR;
	}
}
