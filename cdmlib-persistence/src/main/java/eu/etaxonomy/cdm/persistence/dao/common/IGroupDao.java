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

import eu.etaxonomy.cdm.model.common.Group;

public interface IGroupDao extends ICdmEntityDao<Group> {
	
	public Group findGroupByName(String groupName);
	
	public List<String> listNames(Integer pageSize, Integer pageNumber);
	
	public List<String> listMembers(Group group, Integer pageSize, Integer pageNumber);
}
