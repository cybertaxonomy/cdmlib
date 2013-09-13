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
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.type.IntegerType;
import org.hibernate.type.Type;

import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;

public class GroupByDate extends Grouping {
	
	private Resolution resolution;
	
	public GroupByDate(String propertyPath,String name, SortOrder order,Resolution resolution) {
		super(propertyPath, name, null, order);
		this.resolution = resolution;
	}
	
	@Override
	public void addProjection(ProjectionList projectionList) {
		if(resolution.equals(Resolution.YEAR)) {
			StringBuffer selectSqlString = getYearSelect();
			StringBuffer projectSqlString = getYearProjection();
			projectionList.add(Projections.sqlGroupProjection(selectSqlString.toString(), projectSqlString.toString(), new String[] {"year"}, new Type[] { IntegerType.INSTANCE }),name);
		} else if(resolution.equals(Resolution.MONTH)) {
			StringBuffer selectSqlString = getYearMonthSelect();
			StringBuffer projectSqlString = getYearMonthProjection();
			projectionList.add(Projections.sqlGroupProjection(selectSqlString.toString(), projectSqlString.toString(), new String[] {"year","month"}, new Type[] { IntegerType.INSTANCE, IntegerType.INSTANCE }),name);
		} else {
			StringBuffer selectSqlString = getYearMonthDaySelect();
			StringBuffer projectSqlString = getYearMonthDayProjection();
			projectionList.add(Projections.sqlGroupProjection(selectSqlString.toString(), projectSqlString.toString(), new String[] {"year","month", "day"}, new Type[] { IntegerType.INSTANCE, IntegerType.INSTANCE, IntegerType.INSTANCE }),name);
		}
	}
	
	public void addOrder(Criteria criteria) {
		if(getOrder() != null) {
			if(getOrder().equals(SortOrder.ASCENDING)) {
				if(resolution.equals(Resolution.YEAR)) {
				  criteria.addOrder(asc(this.getPropertyName(),"year"));
				} else if(resolution.equals(Resolution.MONTH)) {
				  criteria.addOrder(asc(this.getPropertyName(),"year"));
			      criteria.addOrder(asc(this.getPropertyName(),"month"));
				} else {
				  criteria.addOrder(asc(this.getPropertyName(),"year"));
				  criteria.addOrder(asc(this.getPropertyName(),"month"));
				  criteria.addOrder(asc(this.getPropertyName(),"day"));
				}
			} else {
				if(resolution.equals(Resolution.YEAR)) {
					  criteria.addOrder(desc(this.getPropertyName(),"year"));
				} else if(resolution.equals(Resolution.MONTH)) {
					  criteria.addOrder(desc(this.getPropertyName(),"year"));
				      criteria.addOrder(desc(this.getPropertyName(),"month"));
				} else {
					  criteria.addOrder(desc(this.getPropertyName(),"year"));
					  criteria.addOrder(desc(this.getPropertyName(),"month"));
					  criteria.addOrder(desc(this.getPropertyName(),"month"));
				}
			}
		}
	}
	
	//"year({alias}.property) as year"
	private StringBuffer getYearSelect() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("year({alias}.");
		stringBuffer.append(getPropertyName());
		stringBuffer.append(") as year");
		return stringBuffer;
	}
	
	private StringBuffer getYearMonthSelect() {
		StringBuffer stringBuffer = getYearSelect();
		stringBuffer.append(", month({alias}.");
		stringBuffer.append(getPropertyName());
		stringBuffer.append(") as month");
		return stringBuffer;
	}
	
	private StringBuffer getYearMonthDaySelect() {
		StringBuffer stringBuffer = getYearProjection();
		stringBuffer.append(", day(");
		if(getAssociatedObj() != null) {
		  stringBuffer.append(getAssociatedObjectAlias());
		  stringBuffer.append(".");
		}
		stringBuffer.append(getPropertyName());
		stringBuffer.append(") as day");
		return stringBuffer;
	}

	//"year({alias}.property) as year"
	private StringBuffer getYearProjection() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("year({alias}.");
		stringBuffer.append(getPropertyName());
		stringBuffer.append(")");
		return stringBuffer;
	}
	
	private StringBuffer getYearMonthProjection() {
		StringBuffer stringBuffer = getYearProjection();
		stringBuffer.append(", month({alias}.");
		stringBuffer.append(getPropertyName());
		stringBuffer.append(")");
		return stringBuffer;
	}
	
	private StringBuffer getYearMonthDayProjection() {
		StringBuffer stringBuffer = getYearProjection();
		stringBuffer.append(", day(");
		if(getAssociatedObj() != null) {
		  stringBuffer.append(getAssociatedObjectAlias());
		  stringBuffer.append(".");
		}
		stringBuffer.append(getPropertyName());
		stringBuffer.append(")");
		return stringBuffer;
	}
	
	public enum Resolution {
		DAY,
		MONTH,
		YEAR;
	}
	
	public  Order asc(String propertyName, String function) {
		return new GroupByDateOrder(propertyName,function, true);
	}
	
	public  Order desc(String propertyName, String function) {
		return new GroupByDateOrder(propertyName,function, false);
	}

	public class GroupByDateOrder extends Order {
        String function;
		String propertyName;
		boolean ascending;		
		
		protected GroupByDateOrder(String propertyName, String function, boolean ascending) {
			super(propertyName,ascending);
			this.propertyName = propertyName;
			this.ascending = ascending;
			this.function = function;
		}
		
		@Override
		public String 	toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) {
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append(function);
			stringBuffer.append("(this_.");
			stringBuffer.append(propertyName);
			stringBuffer.append(")");
			
			if(ascending) {
				stringBuffer.append(" asc");
			} else {
				stringBuffer.append(" desc");
			}
			
			return stringBuffer.toString();
		} 
		
		
		
	}
}
