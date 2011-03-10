/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.NaturalId;
import org.springframework.security.core.GrantedAuthority;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GrantedAuthority", propOrder = {
    "authority"
})
@XmlRootElement(name = "Group")
@Entity
public class GrantedAuthorityImpl extends CdmBase implements GrantedAuthority {
	private static final long serialVersionUID = 2651969425860655040L;

	@XmlElement(name = "Authority")
	@NaturalId
	private String authority;

	protected GrantedAuthorityImpl(){
		super();
	}
	
	public static GrantedAuthorityImpl NewInstance(){
		return new GrantedAuthorityImpl();
	}
	
	public String getAuthority() {
		return authority;
	}
	
	public void setAuthority(String authority) {
		this.authority = authority;
	}

	public int compareTo(Object o) {
		if(o instanceof GrantedAuthority) {
			return this.authority.compareTo(((GrantedAuthority)o).getAuthority());
		}
    	return 0;
	}
}
