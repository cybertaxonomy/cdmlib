/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;


import org.apache.log4j.Logger;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:31
 */
@Entity
public class Journal extends PublicationBase {
	static Logger logger = Logger.getLogger(Journal.class);
	private String issn;

	public static Journal NewInstance(){
		Journal result = new Journal();
		return result;
	}
	
	protected Journal(){
		super();
		logger.warn("No Strategy attached to Journal");
	}
	
	
	public String getIssn(){
		return this.issn;
	}

	/**
	 * 
	 * @param issn    issn
	 */
	public void setIssn(String issn){
		this.issn = issn;
	}

	@Override
	public String generateTitle(){
		return "";
	}

}