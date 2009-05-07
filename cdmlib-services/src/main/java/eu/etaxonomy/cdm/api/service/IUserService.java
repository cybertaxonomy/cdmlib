package eu.etaxonomy.cdm.api.service;

import org.springframework.security.userdetails.GroupManager;
import org.springframework.security.userdetails.UserDetailsManager;

import eu.etaxonomy.cdm.model.common.User;

public interface IUserService extends IService<User>, UserDetailsManager, GroupManager {

}
