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
 * This class represents electronic publications available on the world wide web.
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * term (from PublicationTypeTerm): "WebPage".
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:07:03
 */
@Entity
public class WebPage extends PublicationBase implements Cloneable {
	public static final Logger logger = Logger.getLogger(WebPage.class);

	/** 
	 * Class constructor: creates a new empty web page instance.
	 */
	protected WebPage(){
		super();
	}
	
	/** 
	 * Creates a new empty web page instance.
	 */
	public static WebPage NewInstance(){
		return new WebPage();
	}
	
	/**
	 * Generates and returns an empty string as title since for web pages
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
//*********** CLONE **********************************/	
	
	/** 
	 * Clones <i>this</i> web page instance. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i>
	 * web page instance by modifying only some of the attributes.<BR>
	 * This method overrides the {@link PublicationBase#clone() method} from PublicationBase.
	 * 
	 * @see PublicationBase#clone()
	 * @see media.IdentifyableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	public WebPage clone(){
		WebPage result = (WebPage)super.clone();
		//no changes to: -
		return result;
	}
}