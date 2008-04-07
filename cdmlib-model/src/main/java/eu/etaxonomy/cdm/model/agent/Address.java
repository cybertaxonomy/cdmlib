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
	//Post Office Box
	private String pobox;
	//including number
	private String street;
	private String postcode;
	//town,locality,suburb
	private String locality;
	//Region/State
	private String region;
	private WaterbodyOrCountry country;
	private Point location;
	//Bidirectional only private
	private Contact contact;
	
	
	@ManyToOne
	public Contact getContact() {
		return contact;
	}
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
	 * 
	 * @param country    country
	 */
	public void setCountry(WaterbodyOrCountry country){
		this.country = country;
	}

	public Point getLocation(){
		return this.location;
	}

	/**
	 * 
	 * @param location    location
	 */
	public void setLocation(Point location){
		this.location = location;
	}

	public String getPobox(){
		return this.pobox;
	}

	/**
	 * 
	 * @param pobox    pobox
	 */
	public void setPobox(String pobox){
		this.pobox = pobox;
	}

	public String getStreet(){
		return this.street;
	}

	/**
	 * 
	 * @param street    street
	 */
	public void setStreet(String street){
		this.street = street;
	}

	public String getPostcode(){
		return this.postcode;
	}

	/**
	 * 
	 * @param postcode    postcode
	 */
	public void setPostcode(String postcode){
		this.postcode = postcode;
	}

	public String getLocality(){
		return this.locality;
	}

	/**
	 * 
	 * @param locality    locality
	 */
	public void setLocality(String locality){
		this.locality = locality;
	}

	public String getRegion(){
		return this.region;
	}

	/**
	 * 
	 * @param region    region
	 */
	public void setRegion(String region){
		this.region = region;
	}

}