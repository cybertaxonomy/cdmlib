package eu.etaxonomy.cdm.model.common;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.NaturalId;
import org.springframework.security.GrantedAuthority;

@Entity
@Table(name = "PermissionGroup")
public class Group extends CdmBase {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7216686200093054648L;
	
	protected String name;
	
	protected Set<User> members = new HashSet<User>();
	
	protected Set <GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();
	
	@ManyToMany(fetch = FetchType.LAZY, targetEntity = GrantedAuthorityImpl.class)
	public Set<GrantedAuthority> getGrantedAuthorities() {
		return grantedAuthorities;
	}

	public void setGrantedAuthorities(Set<GrantedAuthority> grantedAuthorities) {
		this.grantedAuthorities = grantedAuthorities;
	}
	
	@NaturalId
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "groups")
	public Set<User> getMembers() {
		return members;
	}
	
	protected void setMembers(Set<User> members) {
		this.members = members;
	}

	public boolean addMember(User user) {
		user.getGroups().add(this);
		return this.members.add(user);
	}

	public boolean removeMember(User user) {
		if(members.contains(user)) {
			user.getGroups().remove(this);
		    return this.members.remove(user);
		} else {
			return false;
		}
	}
}
