package eu.etaxonomy.cdm.api.service;

import org.springframework.dao.DataAccessException;
import org.springframework.security.userdetails.GroupManager;
import org.springframework.security.userdetails.UserDetailsManager;
import org.springframework.security.userdetails.UsernameNotFoundException;

import eu.etaxonomy.cdm.model.common.User;

public interface IUserService extends IService<User>, UserDetailsManager, GroupManager {
	
	public void changePasswordForUser(String username, String password) throws UsernameNotFoundException, DataAccessException;
}
