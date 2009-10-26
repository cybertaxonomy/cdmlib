/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.List;

import eu.etaxonomy.cdm.model.common.Representation;

public interface IRepresentationDao extends ILanguageStringBaseDao<Representation> {

	public List<Representation> getAllRepresentations(Integer limit, Integer start);

}
