/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.agent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.FieldBridge;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.hibernate.search.UriBridge;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.strategy.merge.MergeException;

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
 * @since 08-Nov-2007 13:06:18
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
public class Contact implements Serializable, Cloneable {

	private static final long serialVersionUID = -1851305307069277625L;
    private static final Logger logger = LogManager.getLogger();


	@XmlElementWrapper(name = "EmailAddresses", nillable = true)
	@XmlElement(name = "EmailAddress")
	@ElementCollection(fetch = FetchType.LAZY)
	@Column(name = "contact_emailaddresses_element")
	private List<String> emailAddresses = new ArrayList<>();

	@XmlElementWrapper(name = "URLs", nillable = true)
	@XmlElement(name = "URL")
    @XmlSchemaType(name = "anyURI")
    @FieldBridge(impl = UriBridge.class)
	@ElementCollection(fetch = FetchType.LAZY)
    @Column(name = "contact_urls_element" /*, length=330  */)  //length >255 does not work in InnoDB AUD tables for Key length of (REV, id, url) key
	private final List<String> urls = new ArrayList<>();

	@XmlElementWrapper(name = "PhoneNumbers", nillable = true)
	@XmlElement(name = "PhoneNumber")
	@ElementCollection(fetch = FetchType.LAZY)
    @Column(name = "contact_phonenumbers_element")
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
	private List<String> phoneNumbers = new ArrayList<>();

	@XmlElementWrapper(name = "FaxNumbers", nillable = true)
	@XmlElement(name = "FaxNumber")
	@ElementCollection(fetch = FetchType.LAZY)
    @Column(name = "contact_faxnumbers_element")
	private List<String> faxNumbers = new ArrayList<>();

    @XmlElementWrapper(name = "Addresses", nillable = true)
    @XmlElement(name = "Address")
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval=true)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	protected Set<Address> addresses = new HashSet<>();

	public static Contact NewInstance() {
		return new Contact();
	}

	/**
	 * Creates a new contact
	 * @param street
	 * @param postcode
	 * @param locality
	 * @param country
	 * @param pobox
	 * @param region
	 * @param email
	 * @param faxNumber
	 * @param phoneNumber
	 * @param url
	 * @param location
	 * @return
	 */
	public static Contact NewInstance(String street, String postcode, String locality,
			Country country, String pobox, String region,
			String email, String faxNumber, String phoneNumber, URI url, Point location) {
		Contact result = new Contact();
		if (country != null || StringUtils.isNotBlank(locality) || StringUtils.isNotBlank(pobox) || StringUtils.isNotBlank(postcode) ||
				StringUtils.isNotBlank(region) || StringUtils.isNotBlank(street) ){
			Address newAddress = Address.NewInstance(country, locality, pobox, postcode, region, street, location);
			result.addAddress(newAddress);
		}
		if (email != null){
			result.addEmailAddress(email);
		}
		if (faxNumber != null){
			result.addFaxNumber(faxNumber);
		}
		if (phoneNumber != null){
			result.addPhoneNumber(phoneNumber);
		}
		if (url != null){
			result.addUrl(url);
		}
		return result;
	}


	public static Contact NewInstance(Set<Address> addresses, List<String> emailAddresses,
			List<String> faxNumbers, List<String> phoneNumbers, List<URI> urls) {
		Contact result = new Contact();
		if (addresses != null){
			result.addresses = addresses;
		}
		if (emailAddresses != null){
			result.emailAddresses = emailAddresses;
		}
		if (faxNumbers != null){
			result.faxNumbers = faxNumbers;
		}
		if (phoneNumbers != null){
			result.phoneNumbers = phoneNumbers;
		}
		if (urls != null){
			for (URI uri : urls){
				result.urls.add(uri.toString());
			}
		}
		return result;
	}

// ************************ CONSTRUCTOR **************************/

	public Contact() {
	}

// ************************ MERGE /MATCH ***************************/

	public void merge(Contact contact2) throws MergeException{
		if (contact2 != null){
			mergeList(this.getEmailAddresses(), contact2.getEmailAddresses());
			mergeList(this.getFaxNumbers(), contact2.getFaxNumbers());
			mergeList(this.getPhoneNumbers(), contact2.getPhoneNumbers());
			mergeList(this.getUrls(), contact2.getUrls());
			for (Address address : contact2.getAddresses()){
				try {
					if (this.addresses == null){
						this.addresses = new HashSet<>();
					}
					this.addresses.add(address.clone());
				} catch (CloneNotSupportedException e) {
					throw new MergeException("Address must implement Cloneable");
				}
			}
		}
	}

	private void mergeList(List list1, List list2){
		for (Object obj2 : list2){
			if (! list1.contains(obj2)){
				list1.add(obj2);
			}
		}
	}

    /**
     * True, if no contact data exists in any of the lists (email, phone, ...).
     */
    @Transient
    public boolean isEmpty(){
        if (isEmpty(emailAddresses) && isEmpty(faxNumbers) && isEmpty(phoneNumbers)
                && isEmpty(urls) && isEmpty(addresses)){
            return true;
        }else{
            return false;
        }
    }

    private boolean isEmpty(Collection<? extends Object> collection) {
        return collection == null || collection.isEmpty();
    }

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
			getAddresses().add(address);
		}
	}

	public Address addAddress(String street, String postcode, String locality,
			Country country, String pobox, String region, Point location){
		Address newAddress = Address.NewInstance(country, locality, pobox, postcode, region, street, location);
		getAddresses().add(newAddress);
		return newAddress;
	}

	/**
	 * Removes one element from the set of postal addresses of <i>this</i> contact.
	 *
	 * @param  address  the postal address of <i>this</i> contact which should be deleted
	 * @see     		#getAddresses()
	 */
	public void removeAddress(Address address){
		getAddresses().remove(address);
	}


	/**
	 * Returns the List of strings representing the electronic mail addresses
	 * included in <i>this</i> contact.
	 */
	public List<String> getEmailAddresses(){
		if(this.emailAddresses == null) {
			this.emailAddresses = new ArrayList<>();
		}
		return this.emailAddresses;
	}

	/**
	 * @see  #getEmailAddress()
	 */
	public void addEmailAddress(String emailAddress){
		getEmailAddresses().add(emailAddress);
	}

	/**
	 * Removes one element from the list of email addresses of <i>this</i> contact.
	 *
	 * @param  emailAddress  the email address of <i>this</i> contact which should be deleted
	 * @see     		#getEmailAddresses()
	 */
	public void removeEmailAddress(String emailAddress){
		getEmailAddresses().remove(emailAddress);
	}

//	/**
//	 * Returns the list of {@link URI URIs} representing this contact
//	 * included in <i>this</i> contact.
//	 */
//	@Transient   //TODO preliminary workaround as we get LazyInit Exception in JSON #4444
//	public List<URI> getUrls(){
//		List<URI> result = new ArrayList<URI>();
//		if(this.urls != null) {
//			for (String uri : this.urls){
//				result.add(URI.create(uri));
//			}
//		}
//		return result;
//	}

	/**
	 * Returns the list of {@link URI URIs} representing this contact
	 * included in <i>this</i> contact.
	 */
	public List<String> getUrls(){
		return this.urls;
	}

	/**
	 * @see  #getUrls()
	 */
	public void addUrl(URI url){
		this.urls.add(url.toString());
	}

	/**
	 * Removes one element from the list of urls of <i>this</i> contact.
	 *
	 * @param  url  the url of <i>this</i> contact which should be deleted
	 * @see     		#getUrls()
	 */
	public void removeUrl(URI url){
		this.urls.remove(url.toString());
	}


	/**
	 * Returns the list of strings representing the phone numbers
	 * included in <i>this</i> contact.
	 */
	public List<String> getPhoneNumbers(){
		if(this.phoneNumbers == null) {
			this.phoneNumbers = new ArrayList<>();
		}
		return this.phoneNumbers;
	}

	/**
	 * @see  #getPhone()
	 */
	public void addPhoneNumber(String phoneNumber){
		getPhoneNumbers().add(phoneNumber);
	}

	/**
	 * Removes one element from the list of phone numbers of <i>this</i> contact.
	 *
	 * @param  phoneNumber  the phone number of <i>this</i> contact which should be deleted
	 * @see     		#getPhoneNumber()
	 */
	public void removePhoneNumber(String phoneNumber){
		getPhoneNumbers().remove(phoneNumber);
	}

	/**
	 * Returns the list of strings representing the telefax numbers
	 * included in <i>this</i> contact.
	 */
	public List<String> getFaxNumbers(){
		if(this.faxNumbers == null) {
			this.faxNumbers = new ArrayList<>();
		}
		return this.faxNumbers;
	}

	/**
	 * @see  #getFaxNumbers()
	 */
	public void addFaxNumber(String faxNumber){
		getFaxNumbers().add(faxNumber);
	}

	/**
	 * Removes one element from the list of telefax numbers of <i>this</i> contact.
	 *
	 * @param  faxNumber  the telefax number of <i>this</i> contact which should be deleted
	 * @see     		#getFaxNumber()
	 */
	public void removeFaxNumber(String faxNumber){
		getFaxNumbers().remove(faxNumber);
	}

//*********************** CLONE ********************************************************/

	/**
	 * Clones <i>this</i> Contact. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> Contact.
	 *
	 *
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Contact clone() {
		try{
			Contact result = (Contact) super.clone();
			result.addresses = new HashSet<>();
			for (Address adr : this.addresses){
				result.addAddress(adr.clone());
			}
			//no changes to emailAdresses, faxnumbers, phonenumbers, urls
			return result;
		}catch (CloneNotSupportedException e){
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}
}
