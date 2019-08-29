/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.permission;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.CdmBase;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "User", propOrder = {
    "username",
    "password",
    "salt",
    "emailAddress",
    "grantedAuthorities",
    "authorities",
    "groups",
    "enabled",
    "accountNonExpired",
    "credentialsNonExpired",
    "accountNonLocked",
    "person"
})
@XmlRootElement(name = "User")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.common.User")
@Audited
@Table(name = "UserAccount")
public class User extends CdmBase implements UserDetails {
    private static final long serialVersionUID = 6582191171369439163L;
    private static final Logger logger = Logger.getLogger(User.class);

    public static final String USERNAME_REGEX = "[A-Za-z0-9_\\.\\-]+";

 // **************************** FACTORY *****************************************/

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

    public static User NewInstance(String personTitle, String username, String pwd){
        User user = new User();
        user.setUsername(username);
        user.setPassword(pwd);

        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(true);
        Person userPerson = Person.NewTitledInstance(personTitle);
        user.setPerson(userPerson);

        return user;
    }

//***************************** Fields *********************** /

    @XmlElement(name = "Username")
    @Column(unique = true)
    @Field(analyze = Analyze.NO)
    @NotNull
    @Pattern(regexp=USERNAME_REGEX)
    protected String username;

    /**
     * a salted, MD5 encoded hash of the plain text password
     */
    @XmlElement(name = "Password")
    @NotAudited
    protected String password;


    /**
     * The salt for password hashing.
     * @see https://dev.e-taxonomy.eu/redmine/issues/7210
     * @see https://code-bude.net/2015/03/30/grundlagen-sicheres-passwort-hashing-mit-salts/
     */
    @XmlElement(name = "Salt")
    @NotAudited
    protected String salt;

    @XmlElement(name = "EmailAddress")
    protected String emailAddress;

    @XmlElementWrapper(name = "GrantedAuthorities")
    @XmlElement(name = "GrantedAuthority", type = GrantedAuthorityImpl.class)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY, targetEntity = GrantedAuthorityImpl.class)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE, CascadeType.REFRESH}) // see #2414 (Group updating doesn't work)
    @NotAudited
    protected Set<GrantedAuthority> grantedAuthorities = new HashSet<>();  //authorities of this user only

    @XmlElementWrapper(name = "Groups")
    @XmlElement(name = "Group")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY)
    @IndexedEmbedded(depth = 1)
    @NotAudited
    protected Set<Group> groups = new HashSet<>();

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
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    @IndexedEmbedded(depth = 1)
    protected Person person;

    @XmlTransient
    @Transient
    private Set<GrantedAuthority> transientGrantedAuthorities;  //authorities of this user and of all groups the user belongs to

    @XmlElementWrapper(name = "Authorities")
    @XmlElement(name = "Authority", type = AuthorityBase.class)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY, targetEntity = AuthorityBase.class)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    @NotAudited
    protected Set<AuthorityBase> authorities = new HashSet<>();

    @XmlTransient
    @Transient
    private Set<AuthorityBase> transientAuthorities;  //authorities of this user and of all groups the user belongs to

//***************************** Constructor *********************** /

    protected User(){
        super();
    }

// ***************************** GETTER / SETTER ******************************/

    public Person getPerson() {return person;}
    public void setPerson(Person person) {this.person = person;}

    @Override
    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}

    @Override
    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}

    @Override
    public boolean isAccountNonLocked() {return accountNonLocked;}
    public void setAccountNonLocked(boolean accountNonLocked) {this.accountNonLocked = accountNonLocked;}

    @Override
    public boolean isCredentialsNonExpired() {return credentialsNonExpired;}
    public void setCredentialsNonExpired(boolean credentialsNonExpired) {this.credentialsNonExpired = credentialsNonExpired;}

    public String getEmailAddress() {return emailAddress;}
    public void setEmailAddress(String emailAddress) {this.emailAddress = emailAddress;}

    @Override
    public boolean isEnabled() {return enabled;}
    public void setEnabled(boolean enabled) {this.enabled = enabled;}

    @Override
    public boolean isAccountNonExpired() {return accountNonExpired;}
    public void setAccountNonExpired(boolean accountNonExpired) {this.accountNonExpired = accountNonExpired;}

    protected void setGroups(Set<Group> groups) {
        this.groups = groups;
        initAuthorities();
    }
    public Set<Group> getGroups() {return groups;}

    public Set<GrantedAuthority> getGrantedAuthorities() {return grantedAuthorities;}
    public void setGrantedAuthorities(Set<GrantedAuthority> grantedAuthorities) {
        this.grantedAuthorities = grantedAuthorities;
        initAuthorities();
    }

    public Set<AuthorityBase> getAuthoritiesB() {return authorities;}
    public void setAuthorities(Set<AuthorityBase> authorities) {
        this.authorities = authorities;
        initAuthorities();
    }
    public void addAuthority(AuthorityBase authority){
        this.authorities.add(authority);
        initAuthorities();
    }

// ************************** METHODS *********************/

    /**
     * Initializes or refreshes the collection of authorities, See
     * {@link #getAuthorities()}
     */
    //FIXME made public as preliminary solution to #4053 (Transient field User.authorities not refreshed on reloading entity)
    public void initAuthorities() {
        //GrantedAuthority
        transientGrantedAuthorities = new HashSet<>();
        transientGrantedAuthorities.addAll(grantedAuthorities);
        for(Group group : groups) {
            transientGrantedAuthorities.addAll(group.getGrantedAuthorities());
        }
        //CdmAuthority
        //TODO activating this currently leads to LIE in AuthenticationPresenterTest in Vaadin project
//        transientAuthorities = new HashSet<>();
//        transientAuthorities.addAll(authorities);
//        for(Group group : groups) {
//            transientAuthorities.addAll(group.getAuthorities());
//        }
    }

    /**
     * Implementation of {@link UserDetails#getAuthorities()}
     *
     * {@inheritDoc}
     *
     * @return returns all {@code Set<GrantedAuthority>} instances contained in
     *         the sets {@link #getGrantedAuthorities()} and
     *         {@link #getGroups()}
     */
    @Override
    @Transient
    public Collection<GrantedAuthority> getAuthorities() {
        if(transientGrantedAuthorities == null || transientGrantedAuthorities.size() == 0) {
            initAuthorities();
        }
        return transientGrantedAuthorities;
    }

    public static User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.getPrincipal() != null && authentication.getPrincipal() instanceof User) {
            return (User)authentication.getPrincipal();
        }
        return null;
    }

//*********************** CLONE ********************************************************/

    /**
     * Clones <i>this</i> User. This is a shortcut that enables to create
     * a new instance that differs only slightly from <i>this</i> User.
     * The corresponding person is cloned.
     *
     * @see eu.etaxonomy.cdm.model.common.CdmBase#clone()
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {
        try{
            User result = (User)super.clone();
            if (this.person != null){
                result.setPerson((Person)this.person.clone());
            }
            return result;
        } catch (CloneNotSupportedException e){
            logger.warn("Object does not implement cloneable");
            e.printStackTrace();
            return null;
        }


    }

//************************************** toString ***************************************

    @Override
    public String toString() {
        if (StringUtils.isNotBlank(username)){
            return username;
        }else{
            return super.toString();
        }
    }
}
