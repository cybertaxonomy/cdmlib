/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.permission;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.search.annotations.Field;
import org.springframework.security.core.GrantedAuthority;

import eu.etaxonomy.cdm.model.common.CdmBase;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Group", propOrder = {
    "name",
    "members",
    "grantedAuthorities",
    "authorities",
})
@XmlRootElement(name = "Group")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.common.Group")
@Table(name = "PermissionGroup")
public class Group extends CdmBase {
    private static final long serialVersionUID = 7216686200093054648L;
    private static final Logger logger = Logger.getLogger(Group.class);

    public final static UUID GROUP_EDITOR_UUID = UUID.fromString("22e5e8af-b99c-4884-a92f-71978efd3770");
    public final static UUID GROUP_EDITOR_EXTENDED_CREATE_UUID = UUID.fromString("89a7f47f-6f2b-45ac-88d4-a99a4cf29f07");
    public final static UUID GROUP_PROJECT_MANAGER_UUID = UUID.fromString("645191ae-32a4-4d4e-9b86-c90e0d41944a");
    public final static UUID GROUP_PUBLISH_UUID = UUID.fromString("c1f20ad8-1782-40a7-b06b-ce4773acb5ea");
    public final static UUID GROUP_ADMIN_UUID = UUID.fromString("1739df71-bf73-4dc6-8320-aaaf72cb555f");
    public static final UUID GROUP_EDITOR_REFERENCE_UUID = UUID.fromString("c3efd156-3a0a-4a5c-a99c-c6262fd734bd");
    public static final UUID GROUP_ALLOW_ALL_TAXA_UUID = UUID.fromString("08eadacd-e060-4e96-a40d-75cd495c304a");
    public final static UUID GROUP_USER_MANAGER_UUID = UUID.fromString("7dee66be-121a-4d7f-a91d-b37390a4d2cc");

    public final static String GROUP_EDITOR_NAME = "Editor";
    /**
     * This group will in future replace the group Editor, see issue #7150
     */
    public final static String GROUP_EDITOR_EXTENDED_CREATE_NAME = "EditorExtendedCreate";
    public final static String GROUP_PROJECT_MANAGER_NAME = "ProjectManager";
    public final static String GROUP_ADMIN_NAME = "Admin";
    public static final String GROUP_EDITOR_REFERENCE_NAME = "Editor-Reference";
    public static final String GROUP_ALLOW_ALL_TAXA_NAME = "Allow_for_all_taxa";
    public static final String GROUP_PUBLISH_NAME = "Publish";
    public final static String GROUP_USER_MANAGER_NAME = "User_Manager";

//*********************** FACTORY *********************/

    public static Group NewInstance(){
        return new Group();
    }

    public static Group NewInstance(String name){
        Group group = Group.NewInstance();
        group.setName(name);
        return group;
    }

//**************** FIELDS ******************************/

    @XmlElement(name = "Name")
    @Column(unique = true)
    @Field
    @NotNull
    @Pattern(regexp=User.USERNAME_REGEX)
    private String name;

    @XmlElementWrapper(name = "Members")
    @XmlElement(name = "Member")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "groups")
    private Set<User> members = new HashSet<>();

    @XmlElementWrapper(name = "GrantedAuthorities")
    @XmlElement(name = "GrantedAuthority", type = GrantedAuthorityImpl.class)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY, targetEntity = GrantedAuthorityImpl.class)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    private Set <GrantedAuthority> grantedAuthorities = new HashSet<>();

    @XmlElementWrapper(name = "Authorities")
    @XmlElement(name = "Authority", type = AuthorityBase.class)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY, targetEntity = AuthorityBase.class)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    private Set <AuthorityBase> authorities = new HashSet<>();

// ********************* CONSTRUCTOR ************************/

    protected Group(){
        super();
    }

// *************** GETTER / SETTER ***********************************/

    public Set<GrantedAuthority> getGrantedAuthorities() {
        return grantedAuthorities;
    }
    public boolean addGrantedAuthority(GrantedAuthority grantedAuthority){
        return grantedAuthorities.add(grantedAuthority);
    }
    public boolean removeGrantedAuthority(GrantedAuthority grantedAuthority){
        return grantedAuthorities.remove(grantedAuthority);
    }

    public Set<AuthorityBase> getAuthorities() {
        return authorities;
    }
    public boolean addAuthority(AuthorityBase authority){
        return authorities.add(authority);
    }
    public boolean removeAuthority(AuthorityBase authority){
        return authorities.remove(authority);
    }

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}


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
     * @see eu.etaxonomy.cdm.model.term.TermBase#clone()
     * @see java.lang.Object#clone()
     */
    @Override
    public Group clone() {
        try{
            Group result = (Group)super.clone();
            result.grantedAuthorities = new HashSet<>();
            for (GrantedAuthority grantedauthority: this.grantedAuthorities){
                result.addGrantedAuthority(grantedauthority);
            }

            result.members = new HashSet<>();
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

//************************************** toString ***************************************

    @Override
    public String toString() {
        if (StringUtils.isNotBlank(name)){
            return name;
        }else{
            return super.toString();
        }
    }
}
