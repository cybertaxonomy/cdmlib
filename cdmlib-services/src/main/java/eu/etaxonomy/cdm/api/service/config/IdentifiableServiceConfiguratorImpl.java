/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.api.service.config;

import java.util.List;

import org.hibernate.criterion.Criterion;

import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.babadshanjan
 * @author n.hoffmann
 * @created 03.03.2009
 * @version 1.1
 */
public class IdentifiableServiceConfiguratorImpl<T extends IIdentifiableEntity> implements IIdentifiableEntityServiceConfigurator<T>{
	
	private String titleSearchString;
	protected MatchMode matchMode;
	private Integer pageSize;
	private Integer pageNumber;
	private Class<? extends T> clazz;
	private List<Criterion> criteria;
	private List<String> propertyPaths;
	private List<OrderHint> orderHints;
	
	@Override
	public Class<? extends T> getClazz() {
		return clazz;
	}

	@Override
	public void setClazz(Class<? extends T> clazz) {
		this.clazz = clazz;
	}
	
	public String getTitleSearchString() {
		return titleSearchString;
	}
	
	public String getTitleSearchStringSqlized(){
		return getTitleSearchString().replace("*", "%");
	}
	
	public void setTitleSearchString(String titleSearchString) {
		this.titleSearchString = titleSearchString;
	}
 
	/**
	 * @return the pageSize
	 */
	public Integer getPageSize() {
		return pageSize;
	}

	/**
	 * Sets the number of results that should be shown on current page
	 * 
	 * @param pageSize the pageSize to set
	 */
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	/**
	 * @return the pageNumber
	 */
	public Integer getPageNumber() {
		return pageNumber;
	}

	/**
	 * Sets the number of the page the first result should come from, starting 
	 * with 0 as the first page.
	 * 
	 * @param pageNumber the pageNumber to set
	 */
	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}

	public MatchMode getMatchMode() {
		return matchMode;
	}

	public void setMatchMode(MatchMode matchMode) {
		this.matchMode = matchMode;
	}

	@Override
	public List<Criterion> getCriteria() {
		return criteria;
	}

	@Override
	public void setCriteria(List<Criterion> criteria) {
		this.criteria = criteria;
	}

	@Override
	public List<OrderHint> getOrderHints() {
		return orderHints;
	}

	@Override
	public void setOrderHints(List<OrderHint> orderHints) {
		this.orderHints = orderHints;
	}

	@Override
	public List<String> getPropertyPaths() {
		return propertyPaths;
	}

	@Override
	public void setPropertyPaths(List<String> propertyPaths) {
		this.propertyPaths = propertyPaths;
	}
}
