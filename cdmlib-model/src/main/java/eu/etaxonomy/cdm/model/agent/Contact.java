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
 * Information on how to approach a {@link Person person} or an {@link Institution institution}.
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
	
	
	/** 
	 * Returns the set of postal {@link Address addresses} belonging to this contact. 
	 * A {@link Person person} or an {@link Institution institution} cannot have more than one contact,
	 * but a contact may include several postal addresses. 
	 *
	 * @return	the set of postal addresses
	 * @see     Address
	 */
	@OneToMany(mappedBy="contact")
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE_ORPHAN})
	public Set<Address> getAddresses(){
		return this.addresses;
	}
	/** 
	 * @see     #getAddresses()
	 */
	protected void setAddresses(Set<Address> addresses){
		this.addresses = addresses;
	}
	/** 
	 * Adds a new postal {@link Address address} to the set of postal addresses of this contact.
	 *
	 * @param  address  the address to be added
	 * @see     		#getAddresses()
	 * @see 			Address
	 */
	public void addAddress(Address address){
		if (address != null){
			address.setContact(this);
			addresses.add(address);
		}
	}
	/** 
	 * Removes one element from the set of postal addresses of this contact.
	 *
	 * @param  address  the postal address of this contact which should be deleted
	 * @see     		#getAddresses()
	 */
	public void removeAddress(Address address){
		address.setContact(null);
	}

	
	/**
	 * Returns the string representing the electronic mail address
	 * included in this contact.
	 */
	public String getEmail(){
		return this.email;
	}

	/**
	 * @see  #getEmail()
	 */
	public void setEmail(String email){
		this.email = email;
	}

	/**
	 * Returns the string representing the "Uniform Resource Locator" (url)
	 * included in this contact.
	 */
	public String getUrl(){
		return this.url;
	}

	/**
	 * @see  #getUrl()
	 */
	public void setUrl(String url){
		this.url = url;
	}

	/**
	 * Returns the string representing the phone number
	 * included in this contact.
	 */
	public String getPhone(){
		return this.phone;
	}

	/**
	 * @see  #getPhone()
	 */
	public void setPhone(String phone){
		this.phone = phone;
	}

	/**
	 * Returns the string representing the telefax number
	 * included in this contact.
	 */
	public String getFax(){
		return this.fax;
	}

	/**
	 * @see  #getFax()
	 */
	public void setFax(String fax){
		this.fax = fax;
	}

}