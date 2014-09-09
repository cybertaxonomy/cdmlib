package eu.etaxonomy.cdm.api.cache;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmGenericDaoImpl;

/**
 * CDM Entity Cacher class which uses CDM DAOs to find / load cdm entities
 *
 * @author cmathew
 *
 * @param <T>
 */
@Component
public class CdmDaoCacher<T extends CdmBase> extends CdmCacher<T> {

	@Autowired
	CdmGenericDaoImpl genericDao;

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.cache.CdmCacher#initDefaultTerms()
	 */
	@Override
	protected void initDefaultTerms() {
	    // adding default language to the cdm cacher
	    if(Language.ENGLISH() != null) {
	        put(Language.uuidEnglish,Language.ENGLISH());
	    }
    }

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.cache.CdmCacher#findByUuid(java.util.UUID)
	 */
	@Override
	protected T findByUuid(UUID uuid) {
		return (T)genericDao.findByUuidWithoutFlush(uuid);
	}

}
