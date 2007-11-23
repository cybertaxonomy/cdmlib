/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;


import eu.etaxonomy.cdm.model.agent.Institution;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * publisher is "institution" in BibTex ???
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:49
 */
@Entity
public class Report extends PublicationBase {
	static Logger logger = Logger.getLogger(Report.class);
	private Institution institution;

	@ManyToOne
	public Institution getInstitution(){
		return this.institution;
	}

	/**
	 * 
	 * @param institution    institution
	 */
	public void setInstitution(Institution institution){
		this.institution = institution;
	}

	@Override
	public String generateTitle(){
		return "";
	}

}