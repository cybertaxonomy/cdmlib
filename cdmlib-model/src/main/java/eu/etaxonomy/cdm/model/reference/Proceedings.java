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

import org.apache.log4j.Logger;

/**
 * This class represents conference proceedings. Proceedings are a
 * collection of academic papers that are published in the context of an
 * academic conference. Each paper typically is quite isolated from the other
 * papers in the proceedings. Proceedings are published in-house, by the
 * organizing institution of the conference, or via an academic publisher. 
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * term (from PublicationTypeTerm): "ConferenceProceedings".
 *   
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:45
 */
@Entity
public class Proceedings extends PrintedUnitBase implements Cloneable {
	private static final Logger logger = Logger.getLogger(Proceedings.class);
	
	//The conference sponsor
	private String organization;
	
	
	/** 
	 * Creates a new empty proceedings instance.
	 * 
	 * @see #NewInstance(String)
	 */
	public static Proceedings NewInstance(){
		Proceedings result = new Proceedings();
		return result;
	}
	
	/** 
	 * Creates a new proceedings instance with the given organization
	 * responsible for the conference.
	 * 
	 * @see #NewInstance(String)
	 */
	public static Proceedings NewInstance(String organization){
		Proceedings result = NewInstance();
		result.setOrganization(organization);
		return result;
	}
	


	/**
	 * Returns the string representing the organization responsible for the
	 * conference in the context of which <i>this</i> conference proceedings
	 * has been printed.
	 * 
	 * @return  the string with the responsible organization
	 */
	public String getOrganization(){
		return this.organization;
	}
	/**
	 * @see #getOrganization()
	 */
	public void setOrganization(String organization){
		this.organization = organization;
	}

	/**
	 * Generates, according to the {@link strategy.cache.reference.IReferenceBaseCacheStrategy cache strategy}
	 * assigned to <i>this</i> reference, a string that identifies <i>this</i>
	 * conference proceedings and returns it. This string may be stored in the
	 * inherited {@link common.IdentifiableEntity#getTitleCache() titleCache} attribute.<BR>
	 * This method overrides the generic and inherited
	 * ReferenceBase#generateTitle() method.
	 *
	 * @return  the string identifying <i>this</i> conference proceedings
	 * @see  	ReferenceBase#generateTitle()
	 * @see  	common.IdentifiableEntity#getTitleCache()
	 * @see  	common.IdentifiableEntity#generateTitle()
	 * @see  	strategy.cache.common.IIdentifiableEntityCacheStrategy#getTitleCache()
	 */
	@Override
	public String generateTitle(){
		logger.warn("generateTitle not yet fully implemented");
		return this.getTitle();
	}
	
	
	//*********** CLONE **********************************/	
			
	/** 
	 * Clones <i>this</i> conference proceedings. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> conference
	 * proceedings by modifying only some of the attributes.<BR>
	 * This method overrides the {@link StrictReferenceBase#clone() method} from StrictReferenceBase.
	 * 
	 * @see StrictReferenceBase#clone()
	 * @see media.IdentifyableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
		public Proceedings clone(){
			Proceedings result = (Proceedings)super.clone();
			//no changes to: organization
			return result;
		}

}