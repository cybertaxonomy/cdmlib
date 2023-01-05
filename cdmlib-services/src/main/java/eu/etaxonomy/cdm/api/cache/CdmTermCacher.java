/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.cache;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;

/**
 *
 * CDM Entity Cacher class which handles the caching of Defined Terms.
 *
 * NOTE AM: is this used at all?
 *
 * @author cmathew
 */
@Component
public class CdmTermCacher extends CdmPermanentCacheBase {

	@Autowired
	private ITermService termService;

	@Override
	protected void setup() {
        DefinedTermBase.setCacher(this);
	}

	@Override
	protected  CdmBase findByUuid(UUID uuid) {
		return termService.findWithoutFlush(uuid);
	}

    @Override
    public boolean isCachable(CdmBase cdmEntity) {
        if(cdmEntity != null && cdmEntity instanceof DefinedTermBase) {
            return true;
        }
        return false;
    }

    @Override
    public <T extends CdmBase> T load(T cdmEntity) {

        T cachedCdmEntity = getFromCache(cdmEntity);
        if(cachedCdmEntity == null && isCachable(cdmEntity)) {
            putToCache(cdmEntity);
            cachedCdmEntity = cdmEntity;
        }
        return cachedCdmEntity;
    }
}