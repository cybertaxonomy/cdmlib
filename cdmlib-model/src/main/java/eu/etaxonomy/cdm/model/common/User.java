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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.NaturalId;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import eu.etaxonomy.cdm.model.agent.Person;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "User", propOrder = {
    "username",
    "password",
    "emailAddress",
    "grantedAuthorities",
    "groups",
    "enabled",
    "accountNonExpired",
    "credentialsNonExpired",
    "accountNonLocked",
    "person"    
})
@XmlRootElement(name = "User")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.User")
@Audited
@Table(name = "UserAccount")
public class User extends CdmBase implements UserDetails {
	private static final long serialVersionUID = 6582191171369439163L;
	@SuppressWarnings(value="unused")
	private static final Logger logger = Logger.getLogger(User.class);
	
	protected User(){
		super();
	}
	
	public static User NewInstance(String username, String pwd){
		User user = new User();
		user.setUsername(username);
		user.setPassword(pwd);
		
		user.setAccountNonExpired(true);
		user.setAccountNonLocked(true);
		user.setCredentialsNonExpired(true);
		user.setEnabled(true);
		
		return user;
	}
	
	@XmlElement(name = "Username")
	@NaturalId
	@Field(index = Index.UN_TOKENIZED)
	protected String username;
	
	/**
	 * a salted, MD5 encoded hash of the plaintext password
	 */
	@XmlElement(name = "Password")
	@NotAudited
	protected String password;
	
	@XmlElement(name = "EmailAddress")
	protected String emailAddress;
	
	@XmlElementWrapper(name = "GrantedAuthorities")
	@XmlElement(name = "GrantedAuthority", type = GrantedAuthorityImpl.class)
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToMany(fetch = FetchType.LAZY, targetEntity = GrantedAuthorityImpl.class)
	@NotAudited
	protected Set<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();
	
	@XmlElementWrapper(name = "Groups")
	@XmlElement(name = "Group")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
	@IndexedEmbedded(depth = 1)
	@NotAudited
	protected Set<Group> groups = new HashSet<Group>();
	
	@XmlElement(name = "Enabled")
	protected boolean enabled;
	
	@XmlElement(name = "AccountNonExpired")
	protected boolean accountNonExpired;

	@XmlElement(name = "CredentialsNonExpired")
	protected boolean credentialsNonExpired;
	
	@XmlElement(name = "AccountNonLocked")
	protected boolean accountNonLocked;	
	
	@XmlElement(name = "Person")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@OneToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
	@IndexedEmbedded(depth = 1)
	protected Person person;
	
	@XmlTransient
	@Transient
	private Set<GrantedAuthority> authorities;
	
	private void initAuthorities() {
		authorities = new HashSet<GrantedAuthority>();
		authorities.addAll(grantedAuthorities);
		for(Group group : groups) {
			authorities.addAll(group.getGrantedAuthorities());
		}
	}
	
	@Transient
	public Set<GrantedAuthority> getAuthorities() {
		if(authorities == null) initAuthorities();
		return authorities;
	}

	public String getPassword() {
		return password;
	}

	public String getUsername() {
		return username;
	}

	public boolean isAccountNonExpired() {
		return accountNonExpired;
	}

	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}

	public boolean isCredentialsNonExpired() {
		return credentialsNonExpired;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public Set<GrantedAuthority> getGrantedAuthorities() {
		return grantedAuthorities;
	}

	public void setGrantedAuthorities(Set<GrantedAuthority> grantedAuthorities) {
		this.grantedAuthorities = grantedAuthorities;
		initAuthorities();
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setAccountNonExpired(boolean accountNonExpired) {
		this.accountNonExpired = accountNonExpired;
	}

	public void setCredentialsNonExpired(boolean credentialsNonExpired) {
		this.credentialsNonExpired = credentialsNonExpired;
	}

	public void setAccountNonLocked(boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}
	
	protected void setGroups(Set<Group> groups) {
		this.groups = groups;
		initAuthorities();
	}
	
	public Set<Group> getGroups() {
		return groups;
	}
	
	
	public Person getPerson() {
		return person;
	}
	
	public void setPerson(Person person) {
		this.person = person;
	}
}
