/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;


import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;
import javax.persistence.*;

/**
 * Representation of an atomized postal address.
 * <p>
 * See also the <a href="http://rs.tdwg.org/ontology/voc/ContactDetails#Address">TDWG Ontology</a>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:09
 */
@Entity
public class Address extends VersionableEntity {
	static Logger logger = Logger.getLogger(Address.class);
	private String pobox;
	private String street;
	private String postcode;
	private String locality;
	private String region;
	private WaterbodyOrCountry country;
	private Point location;
	//Bidirectional only private
	private Contact contact;
	
	
	/** 
	 * Returns the {@link Contact contact} (of a {@link Person person} or of an {@link Institution institution})
	 * to which this address belongs.
	 * Both kinds of agents cannot have more than one contact, but a contact may include
	 * several postal addresses. 
	 *
	 * @return	the contact this postal address belongs to
	 * @see     Contact
	 */
	@ManyToOne
	public Contact getContact() {
		return contact;
	}


	/** 
	 * Adds this postal address to the set of addresses of a {@link Contact contact}.
	 * The same address instance cannot be assigned to different persons
	 * or institutions (if they do have the same postal address several
	 * address instances must be created). If this address already belongs to a
	 * contact this method shifts it from this contact to a new one.
	 * Therefore this address will be removed from the set of addresses of the old
	 * contact and added to the set of the new one. 
	 *
	 * @param  newContact  the new contact to which this postal address should belong
	 * @see                Contact#addAddress(Address)
	 * @see                Contact#removeAddress(Address)
	 */
	protected void setContact(Contact newContact) {
		// Hibernate bidirectional cascade hack: 
		// http://opensource.atlassian.com/projects/hibernate/browse/HHH-1054
		if(this.contact == newContact) return;
		if (contact != null) { 
			contact.addresses.remove(this);
		}
		if (newContact!= null) { 
			newContact.addresses.add(this);
		}
		this.contact = newContact;
	}

	
	/**
	 * Returns the {@link WaterbodyOrCountry country} involved in this postal address.
	 * 
	 * @return	the country 
	 */
	@ManyToOne
	public WaterbodyOrCountry getCountry(){
		return this.country;
	}

	/**
	 * @see			   #getCountry()
	 */
	public void setCountry(WaterbodyOrCountry country){
		this.country = country;
	}

	/**
	 * Returns the geophysical location (coordinates) of this postal address.
	 * 
	 * @return  the point corresponding to this address
	 * @see		eu.etaxonomy.cdm.model.location.Point
	 */
	public Point getLocation(){
		//TODO do coordinates make sense for an address?
		logger.warn("do coordinates (point) make sense for an address?");
		return this.location;
	}

	/**
	 * @see			#getLocation()
	 */
	public void setLocation(Point location){
		//TODO do coordinates make sense for an address?
		logger.warn("do coordinates (point) make sense for an address?");
		this.location = location;
	}

	/**
	 * Returns a string corresponding to the post office box
	 * involved in this postal address.
	 * 
	 * @return	the post office box string 
	 */
	public String getPobox(){
		return this.pobox;
	}

	/**
	 * @see			#getPobox()
	 */
	public void setPobox(String pobox){
		this.pobox = pobox;
	}

	/**
	 * Returns the street name and number involved in this postal address.
	 * Street numbers are part of the street string.
	 * 
	 * @return	the string composed of street name and number  
	 */
	public String getStreet(){
		return this.street;
	}

	/**
	 * @see			#getStreet()
	 */
	public void setStreet(String street){
		this.street = street;
	}

	/**
	 * Returns the post code number involved in this postal address.
	 * 
	 * @return	the post code number string
	 */
	public String getPostcode(){
		return this.postcode;
	}

	/**
	 * @see			#getPostcode()
	 */
	public void setPostcode(String postcode){
		this.postcode = postcode;
	}

	/**
	 * Returns the town (possibly with locality or suburb) involved in this postal address.
	 * 
	 * @return  the string representing a town
	 */
	public String getLocality(){
		return this.locality;
	}

	/**
	 * @see			#getLocality()
	 */
	public void setLocality(String locality){
		this.locality = locality;
	}

	/**
	 * Returns the region or state involved in this postal address.
	 * 
	 * @return  the string representing a region or a state
	 */
	public String getRegion(){
		return this.region;
	}

	/**
	 * @see			#getRegion()
	 */
	public void setRegion(String region){
		this.region = region;
	}

}