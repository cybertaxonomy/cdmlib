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
import java.util.*;
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
	
	
	@ManyToOne
	public Contact getContact() {
		return contact;
	}
	/** 
	 * Assigns this postal address to a new contact.
	 * This method also updates the sets of postal addresses
	 * which belong to the two contacts (the new one and the substituted one). 
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

	
	@ManyToOne
	public WaterbodyOrCountry getCountry(){
		return this.country;
	}

	/**
	 * Assigns a country to this postal address.
	 * 
	 * @param country  the (waterbody or) country 
	 */
	public void setCountry(WaterbodyOrCountry country){
		this.country = country;
	}

	public Point getLocation(){
		return this.location;
	}

	/**
	 * Assigns a geophysical location to this postal address.
	 * 
	 * @param location  the point corresponding to this address
	 * @see				location.Point
	 */
	public void setLocation(Point location){
		this.location = location;
	}

	public String getPobox(){
		return this.pobox;
	}

	/**
	 * Assigns a post office box to this postal address.
	 * 
	 * @param pobox  string describing a post office box
	 */
	public void setPobox(String pobox){
		this.pobox = pobox;
	}

	public String getStreet(){
		return this.street;
	}

	/**
	 * Assigns a street name and number to this postal address.
	 * 
	 * @param street  string containing a street name and a street number
	 */
	public void setStreet(String street){
		this.street = street;
	}

	public String getPostcode(){
		return this.postcode;
	}

	/**
	 * Assigns a post code number to this postal address.
	 * 
	 * @param postcode  string representing a post code
	 */
	public void setPostcode(String postcode){
		this.postcode = postcode;
	}

	public String getLocality(){
		return this.locality;
	}

	/**
	 * Assigns a town (possibly with locality or suburb) to this postal address.
	 * 
	 * @param locality  string representing a town (may include locality or suburb)
	 */
	public void setLocality(String locality){
		this.locality = locality;
	}

	public String getRegion(){
		return this.region;
	}

	/**
	 * Assigns a region or state to this postal address.
	 * 
	 * @param region  string representing a region or a state
	 */
	public void setRegion(String region){
		this.region = region;
	}

}