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

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;

/**
 * This class represents isolated parts (usually papers or abstracts) within
 * {@link Proceedings conference proceedings}.
 * <P>
 * This class corresponds, according to the TDWG ontology, partially to the
 * publication type term (from PublicationTypeTerm): "SubReference".
 *   
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:29
 */
@Entity
public class InProceedings extends SectionBase {
	private static final Logger logger = Logger.getLogger(InProceedings.class);
	private Proceedings inProceedings;

	/** 
	 * Creates a new empty "in proceedings" instance.
	 * 
	 * @see #NewInstance(Proceedings)
	 */
	public static InProceedings NewInstance(){
		InProceedings result = new InProceedings();
		return result;
	}
	
	/** 
	 * Creates a new "in proceedings" instance with the given proceedings it belongs to.
	 * 
	 * @param	inProceedings	the proceedings <i>this</i> "in proceedings" is part of
	 * @see 					#NewInstance()
	 * @see 					Proceedings
	 */
	public static InProceedings NewInstance(Proceedings inProceedings){
		InProceedings result = NewInstance();
		result.setInProceedings(inProceedings);
		return result;
	}
	
	
	/**
	 * Returns the {@link Proceedings proceedings} <i>this</i> "in proceedings" (usually 
	 * a paper or an abstract) is part of.
	 * 
	 * @return  the proceedings in which <i>this</i> "in proceedings" has been
	 * 			published
	 * @see 	Proceedings
	 */
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Proceedings getInProceedings(){
		return this.inProceedings;
	}

	/**
	 * @see #getInProceedings()
	 */
	public void setInProceedings(Proceedings inProceedings){
		this.inProceedings = inProceedings;
	}

	/**
	 * Generates, according to the {@link strategy.cache.reference.IReferenceBaseCacheStrategy cache strategy}
	 * assigned to <i>this</i> "in proceedings", a string that identifies <i>this</i>
	 * "in proceedings" and returns it. This string may be stored in the
	 * inherited {@link common.IdentifiableEntity#getTitleCache() titleCache} attribute.<BR>
	 * This method overrides the generic and inherited
	 * ReferenceBase#generateTitle() method.
	 *
	 * @return  the string identifying <i>this</i> "in proceedings"
	 * @see  	ReferenceBase#generateTitle()
	 * @see  	common.IdentifiableEntity#getTitleCache()
	 * @see  	common.IdentifiableEntity#generateTitle()
	 * @see  	strategy.cache.common.IIdentifiableEntityCacheStrategy#getTitleCache()
	 */
	@Override
	public String generateTitle(){
		return "";
	}

}