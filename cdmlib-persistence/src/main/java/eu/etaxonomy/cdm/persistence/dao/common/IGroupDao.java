package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.List;

import eu.etaxonomy.cdm.model.common.Group;

public interface IGroupDao extends ICdmEntityDao<Group> {
	
	public Group findGroupByName(String groupName);
	
	public List<String> listNames(Integer pageSize, Integer pageNumber);
	
	public List<String> listMembers(Group group, Integer pageSize, Integer pageNumber);
}
