/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.envers.Audited;

/**
 * The class for information on how to approach a {@link Person person} or an {@link Institution institution}.
 * It includes telecommunication data and an electronic as well as
 * multiple postal addresses.
* <P>
 * This class corresponds to: <ul>
 * <li> ContactDetails according to the TDWG ontology
 * <li> Contact (partially) according to the ABCD schema
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:18
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Contact", propOrder = {
    "emailAddresses",
    "urls",
    "phoneNumbers",
    "faxNumbers",
    "addresses"
})
@XmlRootElement(name = "Contact")
@Embeddable
@Audited
public class Contact {
	private static final long serialVersionUID = -1851305307069277625L;
	private static final Logger logger = Logger.getLogger(Contact.class);
	

	/** 
	 * Class constructor.
	 */
	public Contact() {
		super();
		logger.debug("Constructor call");
	}

	@XmlElementWrapper(name = "EmailAddresses")
	@XmlElement(name = "EmailAddress")
	@CollectionOfElements(fetch = FetchType.LAZY)
	private List<String> emailAddresses = new ArrayList<String>();
	
	@XmlElementWrapper(name = "URLs")
	@XmlElement(name = "URL")
    @XmlSchemaType(name = "anyURI")
    @CollectionOfElements(fetch = FetchType.LAZY)
	private List<String> urls = new ArrayList<String>();
	
	@XmlElementWrapper(name = "PhoneNumbers")
	@XmlElement(name = "PhoneNumber")
	@CollectionOfElements(fetch = FetchType.LAZY)
	private List<String> phoneNumbers = new ArrayList<String>();
	
	@XmlElementWrapper(name = "FaxNumbers")
	@XmlElement(name = "FaxNumber")
	@CollectionOfElements(fetch = FetchType.LAZY)
	private List<String> faxNumbers = new ArrayList<String>();
	
    @XmlElementWrapper(name = "Addresses")
    @XmlElement(name = "Address")
    @OneToMany(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE_ORPHAN})
	protected Set<Address> addresses = new HashSet<Address>();
	
	
	/** 
	 * Returns the set of postal {@link Address addresses} belonging to <i>this</i> contact. 
	 * A {@link Person person} or an {@link Institution institution} cannot have more than one contact,
	 * but a contact may include several postal addresses. 
	 *
	 * @return	the set of postal addresses
	 * @see     Address
	 */
	public Set<Address> getAddresses(){
		return this.addresses;
	}
	
	/** 
	 * Adds a new postal {@link Address address} to the set of postal addresses of <i>this</i> contact.
	 *
	 * @param  address  the address to be added
	 * @see     		#getAddresses()
	 * @see 			Address
	 */
	public void addAddress(Address address){
		if (address != null){
			addresses.add(address);
		}
	}
	
	/** 
	 * Removes one element from the set of postal addresses of <i>this</i> contact.
	 *
	 * @param  address  the postal address of <i>this</i> contact which should be deleted
	 * @see     		#getAddresses()
	 */
	public void removeAddress(Address address){
		addresses.remove(address);
	}

	
	/**
	 * Returns the List of strings representing the electronic mail addresses
	 * included in <i>this</i> contact.
	 */
	public List<String> getEmailAddresses(){
		return this.emailAddresses;
	}

	/**
	 * @see  #getEmailAddress()
	 */
	public void addEmailAddress(String emailAddress){
		this.emailAddresses.add(emailAddress);
	}
	
	/** 
	 * Removes one element from the list of email addresses of <i>this</i> contact.
	 *
	 * @param  emailAddress  the email address of <i>this</i> contact which should be deleted
	 * @see     		#getEmailAddresses()
	 */
	public void removeEmailAddress(String emailAddress){
		emailAddresses.remove(emailAddress);
	}

	/**
	 * Returns the list of strings representing the "Uniform Resource Locators" (urls)
	 * included in <i>this</i> contact.
	 */
	public List<String> getUrls(){
		return this.urls;
	}

	/**
	 * @see  #getUrls()
	 */
	public void addUrl(String url){
		this.urls.add(url);
	}
	
	/** 
	 * Removes one element from the list of urls of <i>this</i> contact.
	 *
	 * @param  url  the url of <i>this</i> contact which should be deleted
	 * @see     		#getUrls()
	 */
	public void removeUrl(String url){
		urls.remove(url);
	}

	/**
	 * Returns the list of strings representing the phone numbers
	 * included in <i>this</i> contact.
	 */
	public List<String> getPhoneNumbers(){
		return this.phoneNumbers;
	}

	/**
	 * @see  #getPhone()
	 */
	public void addPhoneNumber(String phoneNumber){
		this.phoneNumbers.add(phoneNumber);
	}
	
	/** 
	 * Removes one element from the list of phone numbers of <i>this</i> contact.
	 *
	 * @param  phoneNumber  the phone number of <i>this</i> contact which should be deleted
	 * @see     		#getPhoneNumber()
	 */
	public void removePhoneNumber(String phoneNumber){
		phoneNumbers.remove(phoneNumber);
	}

	/**
	 * Returns the list of strings representing the telefax numbers
	 * included in <i>this</i> contact.
	 */
	public List<String> getFaxNumbers(){
		return this.faxNumbers;
	}

	/**
	 * @see  #getFaxNumbers()
	 */
	public void addFaxNumber(String faxNumber){
		this.faxNumbers.add(faxNumber);
	}

	/** 
	 * Removes one element from the list of telefax numbers of <i>this</i> contact.
	 *
	 * @param  faxNumber  the telefax number of <i>this</i> contact which should be deleted
	 * @see     		#getFaxNumber()
	 */
	public void removeFaxNumber(String faxNumber){
		faxNumbers.remove(faxNumber);
	}
}