/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.agent;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.strategy.cache.agent.INomenclaturalAuthorCacheStrategy;

/**
* Interface providing methods for nomenclatural authorship.<BR><BR>
*
* Note: As this interface is only needed in context of
* {@link INomenclaturalAuthorCacheStrategy} only getter methods are provided.
*
* @author a.mueller
* @since 17-APR-2008
*/
public interface INomenclaturalAuthor {

	/**
	 * @see TeamOrPersonBase#getNomenclaturalTitleCache()
	 */
    public String getNomenclaturalTitleCache();

	/**
     * Sets the nomenclatural titlecache and the protectedNomenclaturalTitle flag.
     * If protected is set to <code>false</code> the nomenclaturalTitleCache may be
     * recomputed so this should be handled with care. Usually this method is expected
     * to be called with <code>protectCache = true</code>.
     *
     * @see TeamOrPersonBase#getNomenclaturalTitleCache()
     * @see IdentifiableEntity#setTitleCache(String, boolean)
     */
    public void setNomenclaturalTitleCache(String nomenclaturalTitle, boolean protectCache);
}
