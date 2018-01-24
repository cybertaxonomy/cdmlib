package eu.etaxonomy.cdm.api.cache;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;

/**
 * CDM Entity Cacher class which handles the caching of Defined Terms.
 *
 * @author cmathew
 *
 * @param <T>
 */
@Component
public class CdmTermCacher extends CdmCacher {

	@Autowired
	ITermService termService;

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
            put(cdmEntity);
           cachedCdmEntity = cdmEntity;
        }
        return cachedCdmEntity;

    }
}
