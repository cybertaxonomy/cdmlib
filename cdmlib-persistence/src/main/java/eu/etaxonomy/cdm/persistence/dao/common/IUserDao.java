package eu.etaxonomy.cdm.persistence.dao.common;

import eu.etaxonomy.cdm.model.common.User;

public interface IUserDao extends ICdmEntityDao<User> {
	
	public User findUserByUsername(String username);

}
