package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.hibernate.Query;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;

@Repository
public class DefinedTermDaoImpl extends CdmEntityDaoBase<DefinedTermBase> implements IDefinedTermDao{
	private static final Logger logger = Logger.getLogger(DefinedTermDaoImpl.class);

	public DefinedTermDaoImpl() {
		super(DefinedTermBase.class);
	}

	public List<DefinedTermBase> findByTitle(String queryString) {
		Query query = getSession().createQuery("select term from DefinedTermBase term join fetch term.representations representation where representation.label = :label");
		query.setParameter("label", queryString);
		return (List<DefinedTermBase>) query.list();
	}

//	@Override
//	public List<DefinedTermBase> list(int limit, int start) {
//		Query query = getSession().createQuery("select term from DefinedTermBase term join fetch term.representations representation ");
//		query.setMaxResults(limit);
//		query.setFirstResult(start);
//		return (List<DefinedTermBase>) query.list();
//	}

}
