package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.persistence.dao.common.IGrantedAuthorityDao;

@Repository
public class GrantedAuthorityDaoImpl extends CdmEntityDaoBase<GrantedAuthorityImpl> implements
		IGrantedAuthorityDao {

	public GrantedAuthorityDaoImpl() {
		super(GrantedAuthorityImpl.class);
	}
}
