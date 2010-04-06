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
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.strategy.cache.reference.IReferenceBaseCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.ReferenceBaseDefaultCacheStrategy;

/**
 * This class represents reports. A report is a document characterized by 
 * information reflective of inquiry or investigation. Reports often address
 * questions posed by individuals in government or science and are generally
 * elaborated within an {@link eu.etaxonomy.cdm.model.agent.Institution institution}.
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * term (from PublicationTypeTerm): "Report".
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:49
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Report", propOrder = {
//    "institution"
})
@XmlRootElement(name = "Report")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.reference.ReferenceBase")
@Audited
@Configurable
@Deprecated
public class Report extends PublicationBase<IReferenceBaseCacheStrategy<Report>> implements Cloneable {
	private static final long serialVersionUID = 2224085476416095383L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Report.class);
	
//	@XmlElement(name = "Institution")
//	@XmlIDREF
//	@XmlSchemaType(name = "IDREF")
//	@ManyToOne(fetch = FetchType.LAZY)
//	@IndexedEmbedded
//	@Cascade(CascadeType.SAVE_UPDATE)
//	private Institution institution;

	protected Report() {
		super();
		this.type = ReferenceType.Report;
		this.cacheStrategy = new ReferenceBaseDefaultCacheStrategy<Report>();
	}
	
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
	 * Creates a new report instance with the given {@link eu.etaxonomy.cdm.model.agent.Institution institution}.
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
	 * Returns the {@link eu.etaxonomy.cdm.model.agent.Institution institution} in which <i>this</i>
	 * report has been elaborated.
	 * 
	 * @return  the institution
	 * @see 	eu.etaxonomy.cdm.model.agent.Institution
	 */
	public Institution getInstitution(){
		return this.institution;
	}
	
	/**
	 * @see #getInstitution()
	 */
	public void setInstitution(Institution institution){
		this.institution = institution;
	}
	
	/** 
	 * Clones <i>this</i> report instance. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i>
	 * report instance by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link PublicationBase PublicationBase}.
	 * 
	 * @see PublicationBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Report clone(){
		Report result = (Report)super.clone();
		//no changes to: institution
		return result;
	}


}