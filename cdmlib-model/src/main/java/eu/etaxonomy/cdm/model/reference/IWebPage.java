/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;

import java.time.ZonedDateTime;

/**
 * This interface represents electronic publications available on the world wide web.
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * term (from PublicationTypeTerm): "WebPage".
 */
public interface IWebPage extends IPublicationBase {


	/**
	 * Returns this websites inReference (e.g. the underlying database).
	 * @return the inReference
	 */
	public Reference getInReference();


	/**
	 * Sets this websites inreference (e.g. the underlying database)
	 * @param inReference The inReference.
	 */
	public void setInReference(Reference inReference);

    /**
     * Date (and time) when a WebPage was accessed.
     * @return the accessed date
     */
	//#5258
    public ZonedDateTime getAccessed();

    /**
     * @param accessed
     * @see #getAccessed()
     */
    public void setAccessed(ZonedDateTime accessed);

}
