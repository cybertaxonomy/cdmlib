package eu.etaxonomy.cdm.api.cache;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmGenericDaoImpl;

/**
 * CDM Entity Cacher class which handles the caching of Defined Terms.
 *
 * @author cmathew
 *
 * @param <T>
 */
@Component
public class CdmTermCacher<T extends CdmBase> extends CdmCacher<T> {

	@Autowired
	ITermService termService;

	@Override
	protected void setup() {
        DefinedTermBase.setCacher(this);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.cache.CdmCacher#findByUuid(java.util.UUID)
	 */
	@Override
	protected  T findByUuid(UUID uuid) {
		return (T)termService.findWithoutFlush(uuid);
	}

}
