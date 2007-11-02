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

/**
 * taken from TDWG & VCard
 * 
 * http://rs.tdwg.org/ontology/voc/ContactDetails#Address
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:17:59
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
	private WaterbodyOrCountry country;
	private Point location;

	public WaterbodyOrCountry getCountry(){
		return country;
	}

	/**
	 * 
	 * @param country
	 */
	public void setCountry(WaterbodyOrCountry country){
		;
	}

	public Point getLocation(){
		return location;
	}

	/**
	 * 
	 * @param location
	 */
	public void setLocation(Point location){
		;
	}

	public String getPobox(){
		return pobox;
	}

	/**
	 * 
	 * @param pobox
	 */
	public void setPobox(String pobox){
		;
	}

	public String getStreet(){
		return street;
	}

	/**
	 * 
	 * @param street
	 */
	public void setStreet(String street){
		;
	}

	public String getPostcode(){
		return postcode;
	}

	/**
	 * 
	 * @param postcode
	 */
	public void setPostcode(String postcode){
		;
	}

	public String getLocality(){
		return locality;
	}

	/**
	 * 
	 * @param locality
	 */
	public void setLocality(String locality){
		;
	}

	public String getRegion(){
		return region;
	}

	/**
	 * 
	 * @param region
	 */
	public void setRegion(String region){
		;
	}

}