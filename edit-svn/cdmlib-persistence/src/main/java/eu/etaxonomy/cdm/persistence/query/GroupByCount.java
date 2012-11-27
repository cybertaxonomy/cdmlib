/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.persistence.query;

import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;

import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;

public class GroupByCount extends Grouping {

	public GroupByCount(String name, SortOrder order) {
		super("", name, null, order);
	}
	
	@Override
	public void addProjection(ProjectionList projectionList) {
		projectionList.add(Projections.rowCount(),name);
	}
	
	@Override
	public String getAssociatedObj() {
		return null;
	}

}
