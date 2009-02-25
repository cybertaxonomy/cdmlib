package eu.etaxonomy.cdm.persistence.dao.common;

import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.LSIDAuthority;

public interface ILsidAuthorityDao extends ICdmEntityDao<LSIDAuthority> {
	
	public Class<? extends IIdentifiableEntity> getClassForNamespace(LSID lsid);
}
