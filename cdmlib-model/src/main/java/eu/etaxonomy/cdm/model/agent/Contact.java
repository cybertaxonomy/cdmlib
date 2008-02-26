/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;


import eu.etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;
import javax.persistence.*;

/**
 * http://rs.tdwg.org/ontology/voc/ContactDetails#ContactDetails
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:18
 */
@Entity
public class Contact extends VersionableEntity {
	public Contact() {
		super();
		// TODO Auto-generated constructor stub
	}

	static Logger logger = Logger.getLogger(Contact.class);
	private String email;
	private String url;
	private String phone;
	private String fax;
	protected Set<Address> addresses;
	
	
	@OneToMany(mappedBy="contact")
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE_ORPHAN})
	public Set<Address> getAddresses(){
		return this.addresses;
	}
	protected void setAddresses(Set<Address> addresses){
		this.addresses = addresses;
	}
	public void addAddress(Address address){
		address.setContact(this);
	}
	public void removeAddress(Address address){
		address.setContact(null);
	}

	
	public String getEmail(){
		return this.email;
	}

	/**
	 * 
	 * @param email    email
	 */
	public void setEmail(String email){
		this.email = email;
	}

	public String getUrl(){
		return this.url;
	}

	/**
	 * 
	 * @param url    url
	 */
	public void setUrl(String url){
		this.url = url;
	}

	public String getPhone(){
		return this.phone;
	}

	/**
	 * 
	 * @param phone    phone
	 */
	public void setPhone(String phone){
		this.phone = phone;
	}

	public String getFax(){
		return this.fax;
	}

	/**
	 * 
	 * @param fax    fax
	 */
	public void setFax(String fax){
		this.fax = fax;
	}

}