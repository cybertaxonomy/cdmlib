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
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:08
 */
@Entity
public class GenBankAccession extends VersionableEntity {
	static Logger logger = Logger.getLogger(GenBankAccession.class);

	@Description("")
	private String accessionNumber;
	@Description("")
	private String uri;

	public String getAccessionNumber(){
		return accessionNumber;
	}

	/**
	 * 
	 * @param accessionNumber
	 */
	public void setAccessionNumber(String accessionNumber){
		;
	}

	public String getUri(){
		return uri;
	}

	/**
	 * 
	 * @param uri
	 */
	public void setUri(String uri){
		;
	}

}