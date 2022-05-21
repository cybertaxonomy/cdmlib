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

import org.hibernate.Hibernate;
import org.hibernate.criterion.Criterion;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.permission.Group;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;
import eu.etaxonomy.cdm.persistence.dao.permission.IUserDao;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

@Repository
public class UserDaoImpl extends CdmEntityDaoBase<User> implements IUserDao {

    public UserDaoImpl() {
        super(User.class);
    }

    @Override
    public User findUserByUsername(String username) {
        Query<User> query = getSession().createQuery("SELECT user FROM User user WHERE user.username = :username", User.class);
        query.setParameter("username", username);

        User user = query.uniqueResult(); // username is a @NaturalId
        return initializeUser(user);
    }

    @Override
    public boolean userNameExists(String username) {
        Query<Long> query = getSession().createQuery("SELECT count(user) FROM User user WHERE user.username = :username", long.class);
        query.setParameter("username", username);

        long count = query.uniqueResult();
        return count > 0;
    }

    @Override
    public boolean emailAddressExists(String emailAddress) {
        Query<Long> query = getSession().createQuery("SELECT count(user) FROM User user WHERE user.emailAddress = :emailAddress", long.class);
        query.setParameter("emailAddress", emailAddress);

        long count = query.uniqueResult();
        return count > 0;
    }

    @Override
    public User findByEmailAddress(String emailAddress) {
        Query<User> query = getSession().createQuery("SELECT user FROM User user where user.emailAddress = :emailAddress", User.class);
        query.setParameter("emailAddress", emailAddress);

        User user = query.uniqueResult(); // emailAddress to be unique, see https://dev.e-taxonomy.eu/redmine/issues/7276
        return initializeUser(user);
    }

    @Override
    public long countByUsername(String queryString, MatchMode matchmode, List<Criterion> criterion) {
        return countByParam(type, "username",queryString,matchmode,criterion);
    }

    @Override
    public List<User> findByUsername(String queryString, MatchMode matchmode, List<Criterion> criterion, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        return findByParam(type, "username", queryString, matchmode, criterion, pageSize, pageNumber, orderHints, propertyPaths);
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

}
