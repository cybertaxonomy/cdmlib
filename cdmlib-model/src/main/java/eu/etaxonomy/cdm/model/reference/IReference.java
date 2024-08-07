/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.reference;

import java.beans.Transient;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IParsable;
import eu.etaxonomy.cdm.strategy.cache.reference.IReferenceCacheStrategy;
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
public interface IReference
            extends IIdentifiableEntity,
                   IParsable, IMergable, IMatchable, Cloneable {

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
	 * Returns the references title
	 */
	public String getTitle();

	/**
	 * Sets the references title
	 * @param title
	 */
	public void setTitle(String title);

    /**
     * The abbreviated title is the short form for the title. It is usually used
     * as a representation of the title of references used in a nomenclatural
     * context.
     * @return
     */
    public String getAbbrevTitle();

    /**
     * Sets the {@link #getAbbrevTitle() abbreviated title}.
     * @param abbrevTitle
     */
    public void setAbbrevTitle(String abbrevTitle);

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
     * @param defaultStrategy
     */
    public void setCacheStrategy(IReferenceCacheStrategy defaultStrategy);

    public IReferenceCacheStrategy cacheStrategy();

    /**
	 * @see Cloneable
	 * @return
	 */
	public Object clone();

    /**
     * Sets both caches and protects them.
     * This is a convenience method to avoid
     * references with only one cache protected
     * leading to strange results in case the other
     * cache is used.
     *
     * https://dev.e-taxonomy.eu/redmine/issues/6449
     *
     * @see #setTitleCache(String)
     * @see #setTitleCache(String, boolean)
     * @see #setProtectedTitleCache(boolean)
     * @see #setAbbrevTitleCache(String)
     * @see #setAbbrevTitleCache(String, boolean)
     * @see #setProtectedAbbrevTitleCache(boolean)
     */
	@Transient
	@javax.persistence.Transient
    void setTitleCaches(String cache);
}