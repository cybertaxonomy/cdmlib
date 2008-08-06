/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;


import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.agent.Institution;

/**
 * This class represents reports. A report is a document characterized by 
 * information reflective of inquiry or investigation. Reports often address
 * questions posed by individuals in government or science and are generally
 * elaborated within an {@link agent.Institution institution}.
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * term (from PublicationTypeTerm): "Report".
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:49
 */
@Entity
public class Report extends PublicationBase implements Cloneable {
	static Logger logger = Logger.getLogger(Report.class);
	private Institution institution;

	/** 
	 * Creates a new empty report instance
	 * 
	 * @see #NewInstance(Institution)
	 */
	public static Report NewInstance(){
		Report result = new Report();
		return result;
	}
	
	/** 
	 * Creates a new report instance with the given {@link agent.Institution institution}.
	 * 
	 * @param	institution		the institution where <i>this</i> report has
	 * 							been elaborated
	 * @see 					#NewInstance()
	 */
	public static Report NewInstance(Institution institution){
		Report result = NewInstance();
		result.setInstitution(institution);
		return result;
	}
	
	
	/**
	 * Returns the {@link agent.Institution institution} in which <i>this</i>
	 * report has been elaborated.
	 * 
	 * @return  the institution
	 * @see 	agent.Institution
	 */
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Institution getInstitution(){
		return this.institution;
	}
	/**
	 * @see #getInstitution()
	 */
	public void setInstitution(Institution institution){
		this.institution = institution;
	}


	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.reference.PublicationBase#clone()
	 */
	@Override
	public Report clone(){
		Report result = (Report)super.clone();
		//no changes to: institution
		return result;
	}


}