package eu.etaxonomy.cdm.model.common;

import javax.persistence.Entity;

import org.hibernate.annotations.NaturalId;
import org.springframework.security.GrantedAuthority;

@Entity
public class GrantedAuthorityImpl extends CdmBase implements GrantedAuthority {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2651969425860655040L;

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
