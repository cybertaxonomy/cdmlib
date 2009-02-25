/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.molecular;


import eu.etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:25
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GenBankAccession", propOrder = {
    "accessionNumber",
    "uri"
})
@XmlRootElement(name = "GenBankAccession")
@Entity
@Audited
public class GenBankAccession extends VersionableEntity {
	private static final long serialVersionUID = -8179493118062601585L;
	private static final Logger logger = Logger.getLogger(GenBankAccession.class);
	
	@XmlElement(name = "AccessionNumber")
	private String accessionNumber;
	
	@XmlElement(name = "URI")
	private String uri;
	
	public String getAccessionNumber(){
		logger.debug("getAccessionNumber");
		return this.accessionNumber;
	}

	/**
	 * 
	 * @param accessionNumber    accessionNumber
	 */
	public void setAccessionNumber(String accessionNumber){
		this.accessionNumber = accessionNumber;
	}

	public String getUri(){
		return this.uri;
	}

	/**
	 * 
	 * @param uri    uri
	 */
	public void setUri(String uri){
		this.uri = uri;
	}

}