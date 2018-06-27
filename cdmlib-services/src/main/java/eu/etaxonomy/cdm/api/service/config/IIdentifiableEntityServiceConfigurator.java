/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service.config;

import java.io.Serializable;
import java.util.List;

import org.hibernate.criterion.Criterion;

import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.babadshanjan
 * @since 03.03.2009
 */
public interface IIdentifiableEntityServiceConfigurator<T extends IIdentifiableEntity> extends Serializable {

	public Class<? extends T> getClazz();

	public void setClazz(Class<? extends T> clazz);

	public String getTitleSearchString();

	/**
	 * Replaces all occurrences of '*' in titleSearchString with '%'
	 *
	 * @return
	 */
	public String getTitleSearchStringSqlized();
	public void setTitleSearchString(String titleSearchString);

	public MatchMode getMatchMode();
	public void setMatchMode(MatchMode matchMode);

	public Integer getPageSize();
	public void setPageSize(Integer pageSize);

	public Integer getPageNumber();
	public void setPageNumber(Integer pageNumber);

	public List<Criterion> getCriteria();
	public void setCriteria(List<Criterion> criteria);

	public List<OrderHint> getOrderHints();
	public void setOrderHints(List<OrderHint> orderHints);

	public List<String> getPropertyPaths();
	public void setPropertyPaths(List<String> propertyPaths);

}
