/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.agent;


import etaxonomy.cdm.model.location.WaterbodyOrCountry;
import etaxonomy.cdm.model.location.Point;
import etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;

/**
 * taken from TDWG & VCard
 * 
 * http://rs.tdwg.org/ontology/voc/ContactDetails#Address
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:14:33
 */
public class Address extends VersionableEntity {
	static Logger logger = Logger.getLogger(Address.class);

	//Post Office Box
	@Description("Post Office Box")
	private String pobox;
	//including number
	@Description("including number")
	private String street;
	@Description("")
	private String postcode;
	//town,locality,suburb
	@Description("town,locality,suburb")
	private String locality;
	//Region/State
	@Description("Region/State")
	private String region;
	private Point location;
	private WaterbodyOrCountry country;

	public WaterbodyOrCountry getCountry(){
		return country;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCountry(WaterbodyOrCountry newVal){
		country = newVal;
	}

	public Point getLocation(){
		return location;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setLocation(Point newVal){
		location = newVal;
	}

	public String getPobox(){
		return pobox;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setPobox(String newVal){
		pobox = newVal;
	}

	public String getStreet(){
		return street;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setStreet(String newVal){
		street = newVal;
	}

	public String getPostcode(){
		return postcode;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setPostcode(String newVal){
		postcode = newVal;
	}

	public String getLocality(){
		return locality;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setLocality(String newVal){
		locality = newVal;
	}

	public String getRegion(){
		return region;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setRegion(String newVal){
		region = newVal;
	}

}