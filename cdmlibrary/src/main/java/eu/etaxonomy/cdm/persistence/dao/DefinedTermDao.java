package eu.etaxonomy.cdm.persistence.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;

@Repository
public class DefinedTermDao extends DaoBase<DefinedTermBase> implements IDefinedTermDao{
	private static final Logger logger = Logger.getLogger(DefinedTermDao.class);

	public DefinedTermDao() {
		super(DefinedTermBase.class);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<DefinedTermBase> find(String queryString) {
		// TODO Auto-generated method stub
		return null;
	}

}
