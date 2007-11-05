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
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * http://rs.tdwg.org/ontology/voc/ContactDetails#ContactDetails
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:01
 */
@Entity
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
	private ArrayList<Address> addresses;

	public ArrayList<Address> getAddresses(){
		return addresses;
	}

	/**
	 * 
	 * @param addresses
	 */
	public void setAddresses(ArrayList<Address> addresses){
		;
	}

	public String getEmail(){
		return email;
	}

	/**
	 * 
	 * @param email
	 */
	public void setEmail(String email){
		;
	}

	public String getUrl(){
		return url;
	}

	/**
	 * 
	 * @param url
	 */
	public void setUrl(String url){
		;
	}

	public String getPhone(){
		return phone;
	}

	/**
	 * 
	 * @param phone
	 */
	public void setPhone(String phone){
		;
	}

	public String getFax(){
		return fax;
	}

	/**
	 * 
	 * @param fax
	 */
	public void setFax(String fax){
		;
	}

}