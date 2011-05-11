/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.NaturalId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.springframework.security.core.GrantedAuthority;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Group", propOrder = {
    "name",
    "members",
    "grantedAuthorities"
})
@XmlRootElement(name = "Group")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.Group")
@Table(name = "PermissionGroup")
public class Group extends CdmBase {
	private static final long serialVersionUID = 7216686200093054648L;
	private static final Logger logger = Logger.getLogger(Group.class);
	
	@XmlElement(name = "Name")
	@NaturalId
	@Field(index = Index.UN_TOKENIZED)
	protected String name;
	
	@XmlElementWrapper(name = "Members")
	@XmlElement(name = "Member")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "groups")
	protected Set<User> members = new HashSet<User>();
	
	@XmlElementWrapper(name = "GrantedAuthorities")
	@XmlElement(name = "GrantedAuthority", type = GrantedAuthorityImpl.class)
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToMany(fetch = FetchType.LAZY, targetEntity = GrantedAuthorityImpl.class)
	@Cascade({CascadeType.SAVE_UPDATE})
	protected Set <GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();
	
	protected Group(){
		super();
	}
	
	public static Group NewInstance(){
		return new Group();
	}
	
	public static Group NewInstance(String name){
		Group group = Group.NewInstance();
		group.setName(name);
		return group;
	}
	
	public Set<GrantedAuthority> getGrantedAuthorities() {
		return grantedAuthorities;
	}
	
	public boolean addGrantedAuthority(GrantedAuthority grantedAuthority){
		return grantedAuthorities.add(grantedAuthority);
	}
	
	public boolean removeGrantedAuthority(GrantedAuthority grantedAuthority){
		return grantedAuthorities.remove(grantedAuthority);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public Set<User> getMembers() {
		return members;
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
//*********************** CLONE ********************************************************/
	
	/** 
	 * Clones <i>this</i> Group. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> group by
	 * modifying only some of the attributes.
	 * 
	 * @see eu.etaxonomy.cdm.model.common.TermBase#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		Group result;
		try{
			result = (Group)super.clone();
			result.grantedAuthorities = new HashSet<GrantedAuthority>();
			for (GrantedAuthority grantedauthority: this.grantedAuthorities){
				result.addGrantedAuthority(grantedauthority);
			}
			
			result.members = new HashSet<User>();
			for (User member: this.members){
				result.addMember(member);
			}
			
			//no changes to name
			return result;
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
			
		}
		
		
	}
	
}
