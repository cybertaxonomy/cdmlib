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

/**
 * http://rs.tdwg.org/ontology/voc/ContactDetails#ContactDetails
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:43:12
 */
public class Contact extends VersionableEntity {
	static Logger logger = Logger.getLogger(Contact.class);

	@Description("")
	private String email;
	@Description("")
	private String url;
	@Description("")
	private String phone;
	@Description("")
	private String fax;
	private ArrayList addresses;

	public ArrayList getAddresses(){
		return addresses;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setAddresses(ArrayList newVal){
		addresses = newVal;
	}

	public String getEmail(){
		return email;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setEmail(String newVal){
		email = newVal;
	}

	public String getUrl(){
		return url;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setUrl(String newVal){
		url = newVal;
	}

	public String getPhone(){
		return phone;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setPhone(String newVal){
		phone = newVal;
	}

	public String getFax(){
		return fax;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setFax(String newVal){
		fax = newVal;
	}

}