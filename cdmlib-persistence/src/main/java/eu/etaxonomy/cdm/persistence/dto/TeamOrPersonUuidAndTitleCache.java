/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.util.UUID;

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;

/**
 * @author k.luther
 * @since Jun 30, 2021
 */
public class TeamOrPersonUuidAndTitleCache<T extends TeamOrPersonBase<T>> extends UuidAndTitleCache<T> {

    private static final long serialVersionUID = 3083330169541901724L;

    private String collectorTitleCache;

    public TeamOrPersonUuidAndTitleCache(UUID uuid, Integer id, String titleCache, String abbrevTitleCache, String collectorTitleCache) {
        super(uuid, id, titleCache, abbrevTitleCache);
        this.setCollectorTitleCache(collectorTitleCache);
    }

    public String getCollectorTitleCache() {
        return collectorTitleCache;
    }

    public void setCollectorTitleCache(String collectorTitleCache) {
        this.collectorTitleCache = collectorTitleCache;
    }
}