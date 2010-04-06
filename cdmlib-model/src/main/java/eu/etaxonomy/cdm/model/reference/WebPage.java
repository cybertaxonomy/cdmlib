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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.strategy.cache.reference.IReferenceBaseCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.ReferenceBaseDefaultCacheStrategy;

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
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WebPage")
@XmlRootElement(name = "WebPage")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.reference.ReferenceBase")
@Audited
@Configurable
@Deprecated
public class WebPage extends PublicationBase<IReferenceBaseCacheStrategy<WebPage>> implements Cloneable {
	private static final long serialVersionUID = -1527430409075839226L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(WebPage.class);

	/** 
	 * Class constructor: creates a new empty web page instance.
	 */
	protected WebPage(){
		super();
		this.type = ReferenceType.WebPage;
		this.cacheStrategy = new ReferenceBaseDefaultCacheStrategy<WebPage>();
	}
	
	/** 
	 * Creates a new empty web page instance.
	 */
	public static WebPage NewInstance(){
		return new WebPage();
	}
	
	
	/** 
	 * Clones <i>this</i> web page instance. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i>
	 * web page instance by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link PublicationBase PublicationBase}.
	 * 
	 * @see PublicationBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public WebPage clone(){
		WebPage result = (WebPage)super.clone();
		//no changes to: -
		return result;
	}
}