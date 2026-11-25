/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.permission;

import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.query.SelectionQuery;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.api.filter.EntityFilter;
import eu.etaxonomy.cdm.api.filter.MatchMode;
import eu.etaxonomy.cdm.model.permission.Group;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;
import eu.etaxonomy.cdm.persistence.dao.permission.IUserDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

@Repository
public class UserDaoImpl extends CdmEntityDaoBase<User> implements IUserDao {

    private static final Logger logger = LogManager.getLogger();

    public UserDaoImpl() {
        super(User.class);
    }

    @Override
    public User findUserByUsername(String username) {
        SelectionQuery<User> query = getSession().createSelectionQuery("SELECT user FROM User user WHERE user.username = :username", User.class);
        query.setParameter("username", username);

        User user = query.uniqueResult(); // username is a @NaturalId
        return initializeUser(user);
    }

    @Override
    public boolean userNameExists(String username) {
        SelectionQuery<Long> query = getSession().createSelectionQuery("SELECT count(user) FROM User user WHERE user.username = :username", Long.class);
        query.setParameter("username", username);

        long count = query.uniqueResult();
        return count > 0;
    }

    @Override
    public boolean emailAddressExists(String emailAddress) {
        SelectionQuery<Long> query = getSession().createSelectionQuery("SELECT count(user) FROM User user WHERE user.emailAddress = :emailAddress", Long.class);
        query.setParameter("emailAddress", emailAddress);

        long count = query.uniqueResult();
        return count > 0;
    }

    @Override
    public User findByEmailAddress(String emailAddress) {
        SelectionQuery<User> query = getSession().createSelectionQuery("SELECT user FROM User user where user.emailAddress = :emailAddress", User.class);
        query.setParameter("emailAddress", emailAddress);

        User user = query.uniqueResult(); // emailAddress to be unique, see https://dev.e-taxonomy.eu/redmine/issues/7276
        return initializeUser(user);
    }

    @Override
    public long countByUsername(String queryString, MatchMode matchmode, List<EntityFilter<User>> filter) {
        return countByParam(type, "username", queryString, matchmode, filter);
    }

    @Override
    public List<User> findByUsername(String queryString, MatchMode matchmode, List<EntityFilter<User>> filter,
            Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        return findByParam(type, "username", queryString, matchmode, filter, pageSize, pageNumber, orderHints, propertyPaths);
    }

    public User initializeUser(User user) {
        if(user != null) {
            getSession().refresh(user); // make sure the user is always up to date
            Hibernate.initialize(user.getPerson());
            Hibernate.initialize(user.getGrantedAuthorities());
            for(Group group : user.getGroups()) {
                Hibernate.initialize(group.getGrantedAuthorities());
            }
        }
        return user;
    }

    @Override
    public void updatePassword(String username, String newEncodedPassword) {

        getSession().createMutationQuery("UPDATE User u SET u.password = :password WHERE u.username = :username")
            .setParameter("password", newEncodedPassword)
            .setParameter("username", username)
            .executeUpdate();
    }

    @Override
    public UUID update(User user) throws DataAccessException {
        if (user == null) {
            logger.warn("User to update should not be null. NOP");
            return null;
        }
        user = getSession().merge(user);
        return user.getUuid();
    }
}
