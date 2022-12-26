/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.reference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.SingleSourcedEntityBase;
import eu.etaxonomy.cdm.model.reference.ISourceable;
import eu.etaxonomy.cdm.model.reference.NamedSourceBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;
import eu.etaxonomy.cdm.persistence.dao.reference.IOriginalSourceDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.mueller
 * @since 17.07.2008
 */
@Repository
public class OriginalSourceDaoImpl
        extends CdmEntityDaoBase<OriginalSourceBase>
        implements IOriginalSourceDao {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	public OriginalSourceDaoImpl() {
		super(OriginalSourceBase.class);
	}

    @Override
    public <S extends SingleSourcedEntityBase> S findSingleSourceBySourceId(Class<S> clazz, int sourceId){
        if (clazz == null){
            clazz = (Class)SingleSourcedEntityBase.class;
        }
        Query<S> q = getSession().createQuery(
                "SELECT c " +
                "FROM " + clazz.getName() + " AS c " +
                "INNER JOIN c.source AS source " +
                "WHERE source.id= :sourceId ", clazz
            );
        q.setParameter("sourceId", sourceId);

        S result = q.uniqueResult();
        return result;
    }

	@Override
	public <S extends IdentifiableEntity> S findIdentifiableBySourceId(Class<S> clazz, int sourceId){
        if (clazz == null){
            clazz = (Class)IdentifiableEntity.class;
        }
	    Query<S> q = getSession().createQuery(
                "SELECT c " +
                "FROM " + clazz.getName() + " AS c " +
                "INNER JOIN c.sources AS source " +
                "WHERE source.id= :sourceId ", clazz
            );
	    q.setParameter("sourceId", sourceId);

        S result = q.uniqueResult();
	    return result;
	}

	@Override
    public <S extends ISourceable> Map<String, S> findOriginalSourcesByIdInSource(Class<S> clazz, Set<String> idInSourceSet, String idNamespace) {

	    Session session = getSession();
		String idInSourceString = "";
		for (String idInSource : idInSourceSet){
			idInSourceString = CdmUtils.concat("','", idInSourceString, idInSource);
		}
		idInSourceString = "'"+ idInSourceString + "'";

		Query<Object[]> q = session.createQuery(
                "SELECT source.idInSource, c " +
                "FROM " + clazz.getName() + " AS c " +
                "INNER JOIN c.sources AS source " +
                "WHERE source.idInSource IN ( " + idInSourceString + " )" +
                	" AND source.idNamespace = :idNamespace",
                	Object[].class
            );
		q.setParameter("idNamespace", idNamespace);
		//TODO integrate reference in where

		Map<String, S> result = new HashMap<>();

        List<Object[]> list = q.list();
		for (Object[] pair : list){
			result.put((String)pair[0], (S)pair[1]);
		}

		return result;
	}

	@Override
    public <S extends ISourceable> List<S> findOriginalSourceByIdInSource(Class<S> clazz, String idInSource, String idNamespace) {
		Session session = getSession();
		Query<S> q = session.createQuery(
                "SELECT c FROM " + clazz.getSimpleName() + " as c " +
                "  INNER JOIN c.sources as source " +
                "WHERE source.idInSource = :idInSource " +
                	" AND source.idNamespace = :idNamespace",
                	clazz
            );
		q.setParameter("idInSource", idInSource);
		q.setParameter("idNamespace", idNamespace);
		//TODO integrate reference in where

        List<S> results = q.list();

		return results;
	}

	@Override
    public List<OriginalSourceBase> findOriginalSourceByIdInSource(String idInSource, String idNamespace) {
		Session session = getSession();
		Criteria crit = session.createCriteria(type);
		crit.add(Restrictions.eq("idInSource", idInSource));
		if (idNamespace == null){
			crit.add(Restrictions.isNull("idNamespace"));
		}else{
			crit.add(Restrictions.eq("idNamespace", idNamespace));
		}
		crit.addOrder(Order.desc("created"));
		@SuppressWarnings({ "unchecked"})
        List<OriginalSourceBase> results = crit.list();

		return results;
	}

	@Override
    public <T extends NamedSourceBase> Long countWithNameUsedInSource(Class<T> clazz){

        clazz = clazz != null? clazz : (Class<T>)NamedSourceBase.class;
        Criteria crit = getSession().createCriteria(clazz);
        //count
        crit.setProjection(Projections.rowCount());
        long result = (Long)crit.uniqueResult();

        return result;
	}


	@Override
	public <T extends NamedSourceBase> List<T> listWithNameUsedInSource(Class<T> clazz,
	        Integer pageSize, Integer pageNumber,List<OrderHint> orderHints, List<String> propertyPaths){

	    clazz = clazz != null? clazz : (Class<T>) NamedSourceBase.class;
	    Criteria crit = getSession().createCriteria(clazz);
	    crit.add(Restrictions.isNotNull("nameUsedInSource"));

	    crit.addOrder(Order.desc("created"));
	    @SuppressWarnings({ "unchecked" })
	    List<T> results = crit.list();

	    return results;
	}
}
