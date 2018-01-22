/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author cmathew
 * @date 19 Feb 2015
 *
 */
public interface ICdmCacher {

    /**
     * Retrieves the cached version of the passed entity from the cache if it
     * exists in there.
     *
     * @param cdmBase
     *            the cdm entity to find in the cache
     *
     * @return the cached version of the passed entity or <code>null</code>
     */
    public CdmBase getFromCache(CdmBase cdmBase);

    /**
     * Puts the passed <code>cdmEntity</code> into the cache as long it does not
     * yet exist in the cache.
     *
     * @param cdmEntity
     */
    public void put(CdmBase cdmEntity);

    /**
     * load into the cache and return the entity from the cache. The the entity
     * might already exist in the cache. In case the entity in the cache might
     * get updated whereas the returned entity represents is the entity from the
     * cache not the <code>cdmEntity</code> passed to this method.
     *
     * @param cdmEntity
     * @return
     */
    public CdmBase load(CdmBase cdmEntity);

    /**
     *
     * @param cdmEntity
     * @return returns true if the <code>cdmEntity</code> is cacheable by the
     *         implementation
     */
    public boolean isCachable(CdmBase cdmEntity);

    /**
     * @param cdmBase
     * @return returns true if the <code>cdmEntity</code> is found in the cache
     */
    public boolean exists(CdmBase cdmBase);

}
