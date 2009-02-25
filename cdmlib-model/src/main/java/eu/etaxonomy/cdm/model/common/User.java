package eu.etaxonomy.cdm.model.common;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.NaturalId;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.userdetails.UserDetails;

@Entity
public class User extends CdmBase implements UserDetails {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6582191171369439163L;

	@NaturalId
	protected String username;
	
	/**
	 * a salted, MD5 encoded hash of the plaintext password
	 */
	protected String password;
	
	protected String emailAddress;
	
	@ManyToMany(fetch = FetchType.LAZY, targetEntity = GrantedAuthorityImpl.class)
	protected Set <GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();
	
	@ManyToMany(fetch = FetchType.LAZY)
	protected Set<Group> groups = new HashSet<Group>();
	
	protected boolean enabled;
	
	protected boolean accountNonExpired;
	
	protected boolean credentialsNonExpired;
	
	protected boolean accountNonLocked;	
	
	@Transient
	private GrantedAuthority[] authorities;
	
	private void initAuthorities() {
		Set<GrantedAuthority> allAuthorities = new TreeSet<GrantedAuthority>();
		allAuthorities.addAll(grantedAuthorities);
		for(Group group : groups) {
			allAuthorities.addAll(group.getGrantedAuthorities());
		}
		
		authorities = allAuthorities.toArray(new GrantedAuthority[allAuthorities.size()]);
	}
	
	public GrantedAuthority[] getAuthorities() {
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
}
