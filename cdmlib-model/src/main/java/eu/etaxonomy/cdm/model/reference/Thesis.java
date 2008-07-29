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
 * This class represents thesis. A thesis is a document that presents the
 * author's research and findings and is submitted at a
 * {@link agent.Institution high school institution} in support of candidature for
 * a degree or professional qualification.
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * term (from PublicationTypeTerm): "Thesis".
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:59
 */
@Entity
public class Thesis extends PublicationBase {
	private static final Logger logger = Logger.getLogger(Thesis.class);
	private Institution school;

	/** 
	 * Creates a new empty thesis instance
	 * 
	 * @see #NewInstance(Institution)
	 */
	public static Thesis NewInstance(){
		Thesis result = new Thesis();
		return result;
	}
	
	/** 
	 * Creates a new thesis instance with the given {@link agent.Institution high school institution}.
	 * 
	 * @param	school		the high school institution where <i>this</i> thesis
	 * 						has been submitted
	 * @see 				#NewInstance()
	 */
	public static Thesis NewInstance(Institution school){
		Thesis result = NewInstance();
		result.setSchool(school);
		return result;
	}
	
	/**
	 * Returns the {@link agent.Institution high school institution} in which <i>this</i>
	 * report has been submitted.
	 * 
	 * @return  the high school institution
	 * @see 	agent.Institution
	 */
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Institution getSchool(){
		return this.school;
	}

	/**
	 * @see #getSchool()
	 */
	public void setSchool(Institution school){
		this.school = school;
	}

	/**
	 * Generates, according to the {@link strategy.cache.reference.IReferenceBaseCacheStrategy cache strategy}
	 * assigned to <i>this</i> report, a string that identifies <i>this</i>
	 * report and returns it. This string may be stored in the inherited
	 * {@link common.IdentifiableEntity#getTitleCache() titleCache} attribute.<BR>
	 * This method overrides the generic and inherited
	 * ReferenceBase#generateTitle() method.
	 *
	 * @return  the string identifying <i>this</i> report
	 * @see  	ReferenceBase#generateTitle()
	 * @see  	common.IdentifiableEntity#getTitleCache()
	 * @see  	common.IdentifiableEntity#generateTitle()
	 * @see  	strategy.cache.common.IIdentifiableEntityCacheStrategy#getTitleCache()
	 */
	@Override
	public String generateTitle(){
		//TODO is this method really needed or is ReferenceBase#generateTitle() enough?
		return "";
	}

}