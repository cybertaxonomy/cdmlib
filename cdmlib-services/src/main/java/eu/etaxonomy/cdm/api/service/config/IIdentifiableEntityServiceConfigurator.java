// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service.config;

import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * @author a.babadshanjan
 * @created 03.03.2009
 * @version 1.0
 */
public interface IIdentifiableEntityServiceConfigurator {

	public String getTitleSearchString();
	
	public void setTitleSearchString(String titleSearchString);
	
	public MatchMode getMatchMode();

	public void setMatchMode(MatchMode matchMode);
	
	public Integer getPageSize();

	public void setPageSize(Integer pageSize);

	public Integer getPageNumber();
	
	public void setPageNumber(Integer pageNumber);
}
