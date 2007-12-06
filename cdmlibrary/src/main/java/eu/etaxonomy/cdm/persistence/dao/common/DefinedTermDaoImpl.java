package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;

@Repository
@Transactional(readOnly = false)
public class DefinedTermDaoImpl extends CdmEntityDaoBase<DefinedTermBase> implements IDefinedTermDao{
	private static final Logger logger = Logger.getLogger(DefinedTermDaoImpl.class);

	public DefinedTermDaoImpl() {
		super(DefinedTermBase.class);
		// TODO Auto-generated constructor stub
	}

	public List<DefinedTermBase> findByTitle(String queryString) {
		// TODO find defined terms by their representation label
		return null;
	}

}
