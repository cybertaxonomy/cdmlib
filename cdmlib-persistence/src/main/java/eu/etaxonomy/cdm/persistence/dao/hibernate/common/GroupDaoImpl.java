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
		Query query = getSession().createQuery("select g from Group g where g.name = :name");
		query.setParameter("name",groupName);
		
		Group group = (Group)query.uniqueResult();
		if(group != null) {
		  Hibernate.initialize(group.getGrantedAuthorities());
		  Hibernate.initialize(group.getMembers());
		}
		
		return group;
	}

	public List<String> listNames(Integer pageSize, Integer pageNumber) {
		Query query = getSession().createQuery("select g.name from Group g");
		
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
		Query query = getSession().createQuery("select m.username from Group g join g.members m where g = :group");
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
