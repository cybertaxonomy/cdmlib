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
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;
import org.springframework.beans.factory.annotation.Configurable;


import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.strategy.cache.reference.INomenclaturalReferenceCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.ThesisDefaultCacheStrategy;

/**
 * This class represents thesis. A thesis is a document that presents the
 * author's research and findings and is submitted at a
 * {@link eu.etaxonomy.cdm.model.agent.Institution high school institution} in support of candidature for
 * a degree or professional qualification.
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * term (from PublicationTypeTerm): "Thesis".
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:59
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Thesis", propOrder = {
//    "school"
})
@XmlRootElement(name = "Thesis")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.reference.ReferenceBase")
@Audited
@Configurable
@Deprecated
public class Thesis extends PublicationBase<INomenclaturalReferenceCacheStrategy<Thesis>> implements INomenclaturalReference, Cloneable{
	private static final long serialVersionUID = -1554558008861571165L;
	private static final Logger logger = Logger.getLogger(Thesis.class);
	
//	@XmlElement(name = "School")
//    @XmlIDREF
//    @XmlSchemaType(name = "IDREF")
//	@ManyToOne(fetch = FetchType.LAZY)
//	@IndexedEmbedded
//	@Cascade(CascadeType.SAVE_UPDATE)
//	private Institution school;
	
	protected Thesis() {
		super();
		this.type = ReferenceType.Thesis;
		this.cacheStrategy = ThesisDefaultCacheStrategy.NewInstance();
	}

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
	 * Creates a new thesis instance with the given {@link eu.etaxonomy.cdm.model.agent.Institution high school institution}.
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
	 * Returns the {@link eu.etaxonomy.cdm.model.agent.Institution high school institution} in which <i>this</i>
	 * report has been submitted.
	 * 
	 * @return  the high school institution
	 * @see 	agent.Institution
	 */
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
	 * Returns a formatted string containing the entire citation used for
	 * nomenclatural purposes based on <i>this</i> generic reference - including
	 * (abbreviated) title but not authors - and on the given
	 * details.
	 * 
	 * @param  microReference	the string with the details (generally pages)
	 * 							within <i>this</i> generic reference
	 * @return					the formatted string representing the
	 * 							nomenclatural citation
	 * @see  					#getCitation()
	 */
	@Transient
	public String getNomenclaturalCitation(String microReference) {
		if (cacheStrategy == null){
			logger.warn("No CacheStrategy defined for "+ this.getClass() + ": " + this.getUuid());
			return null;
		}else{
			return cacheStrategy.getNomenclaturalCitation(this,microReference);
		}
	}
	
	

	
	/** 
	 * Clones <i>this</i> thesis instance. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i>
	 * thesis instance by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link PublicationBase PublicationBase}.
	 * 
	 * @see PublicationBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Thesis clone(){
		Thesis result = (Thesis)super.clone();
		//no changes to: institution
		return result;
	}

}