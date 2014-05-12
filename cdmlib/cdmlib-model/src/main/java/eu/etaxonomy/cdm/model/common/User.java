/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.util.Collection;
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
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
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

    @XmlElement(name = "Username")
    @NaturalId
    @Field(analyze = Analyze.NO)
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
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.REFRESH}) // see #2414 (Group updating doesn't work)
    @NotAudited
    protected Set<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();  //authorities of this user only

    @XmlElementWrapper(name = "Groups")
    @XmlElement(name = "Group")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY)
    @Cascade(CascadeType.REFRESH) // see #2414 (Group updating doesn't work)
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
    private Set<GrantedAuthority> authorities;  //authorities of this user and of all groups the user belongs to

    /**
     * Initializes or refreshes the collection of authorities, See
     * {@link #getAuthorities()}
     */
    //FIXME made public as preliminary solution to #4053 (Transient field User.authorities not refreshed on reloading entity)
    public void initAuthorities() {
        authorities = new HashSet<GrantedAuthority>();
        authorities.addAll(grantedAuthorities);
        for(Group group : groups) {
            authorities.addAll(group.getGrantedAuthorities());
        }
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
        if(authorities == null || authorities.size() == 0) {
            initAuthorities();
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
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
            result.setPerson((Person)this.person.clone());
            return result;
        } catch (CloneNotSupportedException e){
            logger.warn("Object does not implement cloneable");
            e.printStackTrace();
            return null;
        }


    }
}
