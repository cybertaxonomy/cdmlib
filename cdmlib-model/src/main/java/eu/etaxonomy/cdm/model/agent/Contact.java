/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;


import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;
import javax.persistence.*;

/**
 * Information on how to contact a {@link Person person} or an {@link Institution institution}.
 * It includes telecommunication data
 * and electronic as well as multiple postal addresses.
 * <p>
 * See also the <a href="http://rs.tdwg.org/ontology/voc/ContactDetails#ContactDetails">TDWG Ontology</a>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:18
 */
@Entity
public class Contact extends VersionableEntity {
	/** 
	 * Class constructor.
	 */
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
	/** 
	 * Adds a new postal address to the set of postal addresses of this contact.
	 *
	 * @param  address  the address to be added to the the set of addresses
	 * 					of postal addresses of this contact
	 * @see 			Address
	 */
	public void addAddress(Address address){
		address.setContact(this);
	}
	/** 
	 * Removes one element from the set of postal addresses of this contact.
	 *
	 * @param  address  the postal address of this contact which should be deleted
	 * @see         	#addAddress(Address)
	 */
	public void removeAddress(Address address){
		address.setContact(null);
	}

	
	public String getEmail(){
		return this.email;
	}

	/**
	 * Assigns an email address to this contact.
	 * 
	 * @param email  string representing an electronic mail address
	 */
	public void setEmail(String email){
		this.email = email;
	}

	public String getUrl(){
		return this.url;
	}

	/**
	 * Assigns an url address to this contact.
	 * 
	 * @param url  string representing an "Uniform Resource Locator"
	 */
	public void setUrl(String url){
		this.url = url;
	}

	public String getPhone(){
		return this.phone;
	}

	/**
	 * Assigns a phone number to this contact.
	 * 
	 * @param phone  string representing a phone number
	 */
	public void setPhone(String phone){
		this.phone = phone;
	}

	public String getFax(){
		return this.fax;
	}

	/**
	 * Assigns a fax number to this contact.
	 * 
	 * @param fax  string representing a fax number
	 */
	public void setFax(String fax){
		this.fax = fax;
	}

}