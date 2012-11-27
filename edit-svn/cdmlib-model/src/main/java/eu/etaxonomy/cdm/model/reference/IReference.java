/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.model.reference;

import java.net.URI;

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IParsable;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.strategy.cache.reference.IReferenceBaseCacheStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchable;
import eu.etaxonomy.cdm.strategy.merge.IMergable;


/**
 * The upmost interface for references (information sources). 
 * <P>
 * This class corresponds to: <ul>
 * <li> PublicationCitation according to the TDWG ontology
 * <li> Publication according to the TCS
 * <li> Reference according to the ABCD schema
 * </ul>
 */
public interface IReference extends IIdentifiableEntity, IParsable, IMergable, IMatchable{

	/**
	 * Returns the reference type
	 */
	public ReferenceType getType() ;
	
	/**
	 * Sets the reference type
	 * @param type
	 */
	public void setType(ReferenceType type) ;
	
	/**
	 * Returns true if the type of the reference is the same as the passed parameter
	 * @param type
	 * @return boolean
	 */
	public boolean isOfType(ReferenceType type);
	
	/**
	 * Returns the references author(s)
	 */
	public TeamOrPersonBase getAuthorTeam();
	
	/**
	 * Sets the references author(s)
	 */
	public void setAuthorTeam(TeamOrPersonBase authorTeam);
	
	/**
	 * Returns the references title
	 */
	public String getTitle();
	
	/**
	 * Sets the references title
	 * @param title
	 */
	public void setTitle(String title);
	
	/**
	 * Returns the date when the reference was published as a {@link TimePeriod}
	 */
	public TimePeriod getDatePublished();
	
	/**
	 * Sets the date when the reference was published.
	 */
	public void setDatePublished(TimePeriod datePublished);
	
	/**
	 * Returns the Uniform Resource Identifier (URI) corresponding to <i>this</i>
	 * reference. An URI is a string of characters used to identify a resource
	 * on the Internet.
	 * 
	 * @return  the URI of <i>this</i> reference
	 */
	public URI getUri();
	/**
	 * @see #getUri()
	 */
	public void setUri(URI uri);

	
	/**
	 * Returns the references abstract which is a summary of the content
	 */
	public String getReferenceAbstract();
	
	/**
	 * Sets the references abstract which is a summary of the content
	 * @param referenceAbstract
	 */
	public void setReferenceAbstract(String referenceAbstract);
		
	/**
	 * Returns the citation string including the detail (micro reference) information.
	 * E.g. if the references title cache is <i>L., Sp. Pl. 3. 1757</i> the nomenclatural citation
	 * may be something like <i>L., Sp. Pl. 3: 45. 1757</i>
	 * @param microReference the detail, e.g. a page number, a figure, ...
	 * @return String
	 */
	public String getNomenclaturalCitation(String microReference);
	
	/**
	 * Sets the cache strategy for this reference
	 * @param cacheStrategy
	 */
	void setCacheStrategy(IReferenceBaseCacheStrategy cacheStrategy);
	

}
