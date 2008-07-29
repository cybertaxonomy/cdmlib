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
 * This class represents published maps from which information can be derived.
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * term (from PublicationTypeTerm): "Map".
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:33
 */
@Entity
public class Map extends PublicationBase {
	private static final Logger logger = Logger.getLogger(Map.class);

	/** 
	 * Creates a new empty map instance.
	 */
	public static Map NewInstance(){
	Map result = new Map();
	return result;
	}
	

	/**
	 * Generates and returns an empty string as title since for maps
	 * no standard information exist on which a title can be build.<BR>
	 * This method overrides the generic and inherited
	 * ReferenceBase#generateTitle() method.
	 *
	 * @return  the empty string
	 * @see  	ReferenceBase#generateTitle()
	 * @see  	NomenclaturalReferenceHelper#generateTitle()
	 * @see  	common.IdentifiableEntity#getTitleCache()
	 * @see  	common.IdentifiableEntity#generateTitle()
	 */
	@Override
	public String generateTitle(){
		return "";
	}

}