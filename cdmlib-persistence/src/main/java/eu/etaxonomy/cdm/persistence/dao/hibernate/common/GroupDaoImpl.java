package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.Group;
import eu.etaxonomy.cdm.persistence.dao.common.IGroupDao;

@Repository
public class GroupDaoImpl extends CdmEntityDaoBase<Group> implements IGroupDao {

	public GroupDaoImpl() {
		super(Group.class);
	}

	public Group findGroupByName(String groupName) {
		Query query = getSession().createQuery("select group from Group group where group.name = :name");
		query.setParameter("name",groupName);
		
		Group group = (Group)query.uniqueResult();
		if(group != null) {
		  Hibernate.initialize(group.getGrantedAuthorities());
		  Hibernate.initialize(group.getMembers());
		}
		
		return null;
	}

	public List<String> listNames(Integer pageSize, Integer pageNumber) {
		Query query = getSession().createQuery("select group.name from Group group");
		
		if(pageSize != null) {
		    query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		        query.setFirstResult(pageNumber * pageSize);
		    } else {
		    	query.setFirstResult(0);
		    }
		}
		
		return (List<String>)query.list();
	}

	public List<String> listMembers(Group group, Integer pageSize,	Integer pageNumber) {
		Query query = getSession().createQuery("select member.username from Group group join group.members member where group = :group");
		query.setParameter("group", group);
		
		if(pageSize != null) {
		    query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		        query.setFirstResult(pageNumber * pageSize);
		    } else {
		    	query.setFirstResult(0);
		    }
		}
		
		return (List<String>)query.list();
	}

}
