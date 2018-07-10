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
import eu.etaxonomy.cdm.persistence.dao.common.Restriction;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.babadshanjan
 * @author n.hoffmann
 * @since 03.03.2009
 */
public class IdentifiableServiceConfiguratorImpl<T extends IIdentifiableEntity> implements IIdentifiableEntityServiceConfigurator<T>{

    private static final long serialVersionUID = -8126736101861741087L;

    private String titleSearchString;
	protected MatchMode matchMode;
	private Integer pageSize;
	private Integer pageNumber;
	private Class<? extends T> clazz;
    private List<Criterion> criteria;
	private List<Restriction<?>> restrictions;
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

	@Override
    public String getTitleSearchString() {
		return titleSearchString;
	}

	@Override
    public String getTitleSearchStringSqlized(){
	    return getTitleSearchString() == null ? null : getTitleSearchString().replace("*", "%");
	}

	@Override
    public void setTitleSearchString(String titleSearchString) {
		this.titleSearchString = titleSearchString;
	}

	/**
	 * @return the pageSize
	 */
	@Override
    public Integer getPageSize() {
		return pageSize;
	}

	/**
	 * Sets the number of results that should be shown on current page
	 *
	 * @param pageSize the pageSize to set
	 */
	@Override
    public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	/**
	 * @return the pageNumber
	 */
	@Override
    public Integer getPageNumber() {
		return pageNumber;
	}

	/**
	 * Sets the number of the page the first result should come from, starting
	 * with 0 as the first page.
	 *
	 * @param pageNumber the pageNumber to set
	 */
	@Override
    public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}

	@Override
    public MatchMode getMatchMode() {
		return matchMode;
	}

	@Override
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
    public List<Restriction<?>> getRestrictions() {
        return restrictions;
    }

    @Override
    public void setRestrictions(List<Restriction<?>> restrictions) {
        this.restrictions = restrictions;
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
