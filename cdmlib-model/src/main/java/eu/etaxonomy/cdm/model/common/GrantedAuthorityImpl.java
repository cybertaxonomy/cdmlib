package eu.etaxonomy.cdm.model.common;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.NaturalId;
import org.springframework.security.GrantedAuthority;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GrantedAuthority", propOrder = {
    "authority"
})
@XmlRootElement(name = "Group")
@Entity
public class GrantedAuthorityImpl extends CdmBase implements GrantedAuthority {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2651969425860655040L;

	@XmlElement(name = "Authority")
	@NaturalId
	private String authority;

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
