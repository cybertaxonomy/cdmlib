/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.model.reference;

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IParsable;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.strategy.cache.reference.IReferenceBaseCacheStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchable;
import eu.etaxonomy.cdm.strategy.merge.IMergable;

public interface IReferenceBase extends IIdentifiableEntity, IParsable, IMergable, IMatchable{

	//public ReferenceBase getInReference() ;

	//public void setInReference(ReferenceBase inReference) ;

	public void setType(ReferenceType type) ;
	
	public ReferenceType getType() ;
	
	public String getTitle();
	
	public void setTitle(String title);
	
	public TimePeriod getDatePublished();
	
	public void setDatePublished(TimePeriod datePublished);
	
	public String getNomenclaturalCitation(String microReference);
	
	public void setAuthorTeam(TeamOrPersonBase authorTeam);
	
	public TeamOrPersonBase getAuthorTeam();
	
	void setCacheStrategy(IReferenceBaseCacheStrategy cacheStrategy);
	
	/**
	 * Returns the Uniform Resource Identifier (URI) corresponding to <i>this</i>
	 * reference. An URI is a string of characters used to identify a resource
	 * on the Internet.
	 * 
	 * @return  the URI of <i>this</i> reference
	 */
	public String getUri();
	/**
	 * @see #getUri()
	 */
	public void setUri(String uri);
		
	public String getReferenceAbstract();
	
	public void setReferenceAbstract(String referenceAbstract);
	
	public boolean isOfType(ReferenceType type);
}
