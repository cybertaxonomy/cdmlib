/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.model.common;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.springframework.security.core.GrantedAuthority;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GrantedAuthority", propOrder = { "authority" })
@XmlRootElement(name = "Group")
@Entity
public class GrantedAuthorityImpl extends CdmBase implements GrantedAuthority {

    private static final long serialVersionUID = 2651969425860655040L;
    private static final Logger logger = Logger.getLogger(GrantedAuthority.class);

    @XmlElement(name = "Authority")
    @Column(unique = true)
    @NotNull
    private String authority;

    protected GrantedAuthorityImpl() {
        super();
    }

    public static GrantedAuthorityImpl NewInstance() {
        return new GrantedAuthorityImpl();
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
    	this.authority = authority;
    }

    /**
     * @param o
     * @return
     */
    public int compareTo(Object o) {
        if (o instanceof GrantedAuthority) {
            return this.authority.compareTo(((GrantedAuthority) o).getAuthority());
        }
        return 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof GrantedAuthority) {
        	if(this.authority == null && ((GrantedAuthority) o).getAuthority() == null) {
        		return true;
        	} else {
        		return this.authority.equals(((GrantedAuthority) o).getAuthority());
        	}
        }
        return false;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.CdmBase#toString()
     */
    @Override
    public String toString() {
        return getAuthority();
    }

    // *********** CLONE **********************************/

    /**
     * Clones <i>this</i> Granted Authority. This is a shortcut that enables to
     * create a new instance that differs only slightly from <i>this</i> Granted
     * Authority by modifying only some of the attributes.<BR>
     *
     *
     *
     * @see eu.etaxonomy.cdm.model.common.CdmBase#clone()
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {
        GrantedAuthority result;
        try {
            result = (GrantedAuthority) super.clone();
            // no changes to authority
            return result;
        } catch (CloneNotSupportedException e) {
            logger.warn("Object does not implement cloneable");
            e.printStackTrace();
            return null;
        }
    }
}
